/*
* LiquidBounce Hacked Client
* A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
* https://github.com/CCBlueX/LiquidBounce/
*/
package net.ccbluex.liquidbounce.features.module.modules.other

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import net.ccbluex.liquidbounce.LiquidBounce.hud
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.script.api.global.Chat
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.utils.StaffList
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.network.Packet
import net.minecraft.network.play.server.*
import java.util.concurrent.ConcurrentHashMap

object AntiStaff : Module("AntiStaff",Category.MISC) {
    private val staffMode = object : ListValue("StaffMode", arrayOf("BlocksMC", "CubeCraft", "Gamster",
        "AgeraPvP", "HypeMC", "Hypixel", "SuperCraft", "PikaNetwork", "GommeHD","KKCraft"), "BlocksMC") {
    }

    private val tab1 = BoolValue("TAB", true)
    private val packet = BoolValue("Packet", true)
    private val velocity = BoolValue("Velocity", false)

    private val autoLeave = ListValue("AutoLeave", arrayOf("Off", "Leave", "Lobby", "Quit"), "Off"){ tab1.get() || packet.get() }

    private val spectator = BoolValue("StaffSpectator", false){ tab1.get() || packet.get() }
    private val otherSpectator = BoolValue("OtherSpectator", false){ tab1.get() || packet.get() }

    private val inGame = BoolValue("InGame", true){ autoLeave.get() != "Off" }
    private val warn = ListValue("Warn", arrayOf("Chat", "Notification"), "Chat")

    private val checkedStaff = ConcurrentHashMap.newKeySet<String>()
    private val checkedSpectator = ConcurrentHashMap.newKeySet<String>()
    private val playersInSpectatorMode = ConcurrentHashMap.newKeySet<String>()

    private var attemptLeave = false

    private var staffList: String = ""
    private var serverIp = ""

    private val moduleJob = SupervisorJob()
    private val moduleScope = CoroutineScope(Dispatchers.IO + moduleJob)

    override fun onDisable() {
        serverIp = ""
        moduleJob.cancel()
        checkedStaff.clear()
        checkedSpectator.clear()
        playersInSpectatorMode.clear()
        attemptLeave = false
    }

    /**
     * Reset on World Change
     */
    @EventTarget
    fun onWorld(event: WorldEvent) {
        checkedStaff.clear()
        checkedSpectator.clear()
        playersInSpectatorMode.clear()
    }

    private fun checkedStaffRemoved() {
        val onlinePlayers = mc.netHandler?.playerInfoMap?.mapNotNull { it?.gameProfile?.name }

        synchronized(checkedStaff) {
            onlinePlayers?.toSet()?.let { checkedStaff.retainAll(it) }
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        staffList = when (staffMode.get().lowercase()) {
            "cubecraft" -> StaffList.CUBECRAFT
            "kkcraft" -> StaffList.KKCRAFT
            "hypixel" -> StaffList.HYPIXEL
            "pikanetwork" -> StaffList.PIKA
            "blocksmc" -> StaffList.BMC
            "agerapvp" -> StaffList.ARERAPVP
            "hypemc" -> StaffList.HYPEMC
            "supercraft" -> StaffList.SUERPCRAFT
            "gommehd" -> StaffList.GOMMA
            "gamster" -> StaffList.GAMSTER
            "vimemc" -> StaffList.VIMEMC
            else -> ""
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return
        }

        val packet = event.packet

        /**
         * OLD BlocksMC Staff Spectator Check
         * Credit: @HU & Modified by @EclipsesDev
         *
         * NOTE: Doesn't detect staff spectator all the time.
         */
        if (spectator.get()) {
            if (packet is S3EPacketTeams) {
                val teamName = packet.name

                if (teamName.equals("Z_Spectator", true)) {
                    val players = packet.players ?: return

                    val staffSpectateList = players.filter { it in staffList } - checkedSpectator
                    val nonStaffSpectateList = players.filter { it !in staffList } - checkedSpectator

                    // Check for players who are using spectator menu
                    val miscSpectatorList = playersInSpectatorMode - players.toSet()

                    staffSpectateList.forEach { player ->
                        notifySpectators(player!!)
                    }

                    nonStaffSpectateList.forEach { player ->
                        if (otherSpectator.get()) {
                            notifySpectators(player!!)
                        }
                    }

                    miscSpectatorList.forEach { player ->
                        val isStaff = player in staffList

                        if (isStaff && spectator.get()) {
                            Chat.print("§c[STAFF] §d${player} §3is using the spectator menu §e(compass/left)")
                        }

                        if (!isStaff && otherSpectator.get()) {
                            Chat.print("§d${player} §3is using the spectator menu §e(compass/left)")
                        }
                        checkedSpectator.remove(player)
                    }

                    // Update the set of players in spectator mode
                    playersInSpectatorMode.clear()
                    playersInSpectatorMode.addAll(players)
                }
            }

            // Handle other packets
            handleOtherChecks(packet)
        }

        /**
         * Velocity Check
         * Credit: @azureskylines / Nextgen
         *
         * Check if this is a regular velocity update
         */
        if (velocity.get()) {
            if (packet is S12PacketEntityVelocity && packet.entityID == mc.thePlayer?.entityId) {
                if (packet.motionX == 0 && packet.motionZ == 0 && packet.motionY / 8000.0 > 0.075) {
                    attemptLeave = false
                    autoLeave()

                    if (warn.get() == "Chat") {
                        Chat.print("§3Staff is Watching")
                    } else {
                        hud.addNotification(Notification("[AntiStaff] Staff is Watching",5000F))
                    }
                }
            }
        }
    }

    private fun notifySpectators(player: String) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return
        }

        val isStaff: Boolean = staffList.contains(player)

        if (isStaff && spectator.get()) {
            if (warn.get() == "Chat") {
                Chat.print("§c[STAFF] §d${player} §3is a spectators")
            } else {
                hud.addNotification(Notification("[STAFF] §d${player} §3is a spectators",5000F))
            }
        }

        if (!isStaff && otherSpectator.get()) {
            if (warn.get() == "Chat") {
                Chat.print("§d${player} §3is a spectators")
            } else {
                hud.addNotification(Notification("[Non-STAFF] §d${player} §3is a spectators",5000F))
            }
        }

        attemptLeave = false
        checkedSpectator.add(player)

        if (isStaff) {
            autoLeave()
        }
    }

    /**
     * Check staff using TAB
     */
    private fun notifyStaff() {
        if (!tab1.get())
            return

        if (mc.thePlayer == null || mc.theWorld == null) {
            return
        }

        val playerInfoMap = mc.netHandler?.playerInfoMap ?: return

        val playerInfos = synchronized(playerInfoMap) {
            playerInfoMap.mapNotNull { playerInfo ->
                playerInfo?.gameProfile?.name?.let { playerName ->
                    playerName to playerInfo.responseTime
                }
            }
        }

        playerInfos.forEach { (player, responseTime) ->
            val isStaff : Boolean = staffList.contains(player)

            val condition = when {
                responseTime > 0 -> "§e(${responseTime}ms)"
                responseTime == 0 -> "§a(Joined)"
                else -> "§c(Ping error)"
            }

            val warnings = "§c[STAFF] §d${player} §3is a staff §b(TAB) $condition"

            synchronized(checkedStaff) {
                if (isStaff && player !in checkedStaff) {
                    if (warn.get() == "Chat") {
                        Chat.print(warnings)
                    } else {
                        hud.addNotification(Notification(warnings,5000F))
                    }

                    attemptLeave = false
                    checkedStaff.add(player)

                    autoLeave()
                }
            }
        }
    }

    /**
     * Check staff using Packet
     */
    private fun notifyStaffPacket(staff: Entity) {
        if (!packet.get())
            return

        if (mc.thePlayer == null || mc.theWorld == null) {
            return
        }

        val isStaff: Boolean = if (staff is EntityPlayer) {
            val playerName = staff.gameProfile.name

            staffList.contains(playerName)
        } else {
            false
        }

        val condition = when (staff) {
            is EntityPlayer -> {
                val responseTime = mc.netHandler?.getPlayerInfo(staff.uniqueID)?.responseTime ?: 0
                when {
                    responseTime > 0 -> "§e(${responseTime}ms)"
                    responseTime == 0 -> "§a(Joined)"
                    else -> "§c(Ping error)"
                }
            }
            else -> ""
        }

        val playerName = if (staff is EntityPlayer) staff.gameProfile.name else ""

        val warnings = "§c[STAFF] §d${playerName} §3is a staff §b(Packet) $condition"

        synchronized(checkedStaff) {
            if (isStaff && playerName !in checkedStaff) {
                if (warn.get() == "Chat") {
                    Chat.print(warnings)
                } else {
                    hud.addNotification(Notification(warnings, 5000F))
                }

                attemptLeave = false
                checkedStaff.add(playerName)

                autoLeave()
            }
        }
    }

    private fun autoLeave() {
        val firstSlotItemStack = mc.thePlayer.inventory.mainInventory[0] ?: return

        if (inGame.get() && (firstSlotItemStack.item == Items.compass || firstSlotItemStack.item == Items.bow)) {
            return
        }

        if (!attemptLeave && autoLeave.get() != "Off") {
            when (autoLeave.get().lowercase()) {
                "leave" -> mc.thePlayer.sendChatMessage("/leave")
                "lobby" -> mc.thePlayer.sendChatMessage("/lobby")
                "quit" -> mc.theWorld.sendQuittingDisconnectingPacket()
            }
            attemptLeave = true
        }
    }

    private fun handleOtherChecks(packet: Packet<*>?) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return
        }

        when (packet) {
            is S01PacketJoinGame -> handleStaff(mc.theWorld.getEntityByID(packet.entityId) ?: null)
            is S0CPacketSpawnPlayer -> handleStaff(mc.theWorld.getEntityByID(packet.entityID) ?: null)
            is S18PacketEntityTeleport -> handleStaff(mc.theWorld.getEntityByID(packet.entityId) ?: null)
            is S1CPacketEntityMetadata -> handleStaff(mc.theWorld.getEntityByID(packet.entityId) ?: null)
            is S1DPacketEntityEffect -> handleStaff(mc.theWorld.getEntityByID(packet.entityId) ?: null)
            is S1EPacketRemoveEntityEffect -> handleStaff(mc.theWorld.getEntityByID(packet.entityId) ?: null)
            is S19PacketEntityStatus -> handleStaff(mc.theWorld.getEntityByID(packet.entityId) ?: null)
            is S19PacketEntityHeadLook -> handleStaff(packet.getEntity(mc.theWorld) ?: null)
            is S49PacketUpdateEntityNBT -> handleStaff(packet.getEntity(mc.theWorld) ?: null)
            is S1BPacketEntityAttach -> handleStaff(mc.theWorld.getEntityByID(packet.entityId) ?: null)
            is S04PacketEntityEquipment -> handleStaff(mc.theWorld.getEntityByID(packet.entityID) ?: null)
        }
    }

    private fun handleStaff(staff: Entity?) {
        if (mc.thePlayer == null || mc.theWorld == null || staff == null) {
            return
        }

        checkedStaffRemoved()

        notifyStaff()
        notifyStaffPacket(staff)
    }

    /**
     * HUD TAG
     */
    override val tag
        get() = staffMode.get()
}