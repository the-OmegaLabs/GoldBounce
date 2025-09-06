/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.misc

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

object AntiStaff : Module("AntiStaff", Category.MISC) {
    private val staffMode = object : ListValue(
        "StaffMode",
        arrayOf(
            "BlocksMC", "CubeCraft", "Gamster",
            "AgeraPvP", "HypeMC", "Hypixel", "SuperCraft", "PikaNetwork", "GommeHD", "KKCraft"
        ), "BlocksMC"
    ) {
    }

    private val tab1 = BoolValue("TAB", true)
    private val packet = BoolValue("Packet", true)
    private val velocity = BoolValue("Velocity", false)

    private val autoLeave = ListValue("AutoLeave", arrayOf("Off", "Leave", "Lobby", "Quit"), "Off") { tab1.get() || packet.get() }

    private val spectator = BoolValue("StaffSpectator", false) { tab1.get() || packet.get() }
    private val otherSpectator = BoolValue("OtherSpectator", false) { tab1.get() || packet.get() }

    private val inGame = BoolValue("InGame", true) { autoLeave.get() != "Off" }
    private val warn = ListValue("Warn", arrayOf("Chat", "Notification"), "Chat")

    private val checkedStaff = ConcurrentHashMap.newKeySet<String>()
    private val checkedSpectator = ConcurrentHashMap.newKeySet<String>()
    private val playersInSpectatorMode = ConcurrentHashMap.newKeySet<String>()

    private var attemptLeave = false

    // staff set used for checks (BlocksMC local or parsed from StaffList constants)
    private var staffSet: Set<String> = emptySet()
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
     * Local BlocksMC staff list (copied from StaffDetector)
     * Converted to a HashSet for fast membership checks.
     */
    private val LOCAL_BLOCKSMC_STAFF: Set<String> = setOf(
        "iDhoom","BasselFTW","7sO","1Sweet","Jinaaan","Ev2n","Eissaa","mohmad_q8","1Daykel","xImTaiG_",
        "Nshme","comsterr","e9_","1MeKo","1LaB","MK_F16","loovq","_sadeq","nv0ola","xMz7","Harbi",
        "xiDayzer","Firas","EyesO_Diamond","1Rana","DeFiCeNcY","DouglasF15","1HeyImHasson_","devilsvul",
        "Meedo_qb","Ahm2d","LuxRosea","Casteret","curiousmyths","420kinaka","1flyn","_NonameIsHere_",
        "Bunkrat","n8al3lomblocks","Aymann_","unusunusunus","1ZEYAD","i7ilin","Zerokame44","ss3ed","akthem",
        "Postme","3Mmr","Iv2a","Y2men","quinque0quinque","RamboKinq","1Ahmd","bogdanpvp1","1Elyy","rR3L",
        "R1tanium","Sanfoor_J","A2boD","Jrx7","Hunter47","0hFault","xL2d","xfahadq","1Abdllah",
        "7rBe_8aAad7","Mr_1990","lt1x","vxom","zChangerr","Mzad","Wyssap","GsOMAR","BeastToggled",
        "CigsAfterBeer","Miyxmura","Endersent","Exhaustr","1S3oD","iKubca","Werthly","_R3","StrongesT0ne",
        "Ventz_","938awy","Xd3M_","1SPEEDO_","7be","6nabh","N0uf","alk52","qLaith","zixgamer","xz7a",
        "Bnmv123","zSwift","ITsMo7amed_","Watchdog","FexoraNEP","MVP11","akash1004","Neeres","TheOldDays_",
        "Time98","Vhagardracarys","khaled12341234","0RyZe","1Reyleigh","ohhhhQls","Nshmee","_Revoox_",
        "Rieus","ToxicLayer","Mr3nb_","1NotForMaster","1Lzye","Pynifical","Sexyz","M7mmqd","OnlySpam",
        "UnderTest","1hry","M_7B","Blzxer","Escoco","smckingweed","SweetyAlice","1S4L","1Alii_","DarkVzd",
        "uxvv","DrW2rden","ilerz_","Dr_Kratos1","Raqxklps","_1HypersX_","Vanitas_0","HDZT","Rma7o",
        "3bdooooooo","420WaFFLe","Wacros","EmpatheticEyes","Yarin","Yawelkm","Lordui","rivsci","kingpvp90",
        "izLORDeX","DreadPirateR0B","OpGaming2009","Pipars6","Mvjed","LovelyLayan","savobabo","GlowDown_",
        "_i_b","_odex","sh59","Luffy404","Io2n","ixd7vl","Laarcii","0hQxmar_","1Ashu","Rayqquaza",
        "Zaytook","Krejums","Razen555","1Mostyy","iMizuki","Mohvm3d","N13M_","Refolt","3zal","1F5aMH___3oo",
        "hnxrr","ammaradi1","xDiaa","yas0","EmeraldEyex","LuvDark","Xiaolv_FIX","GeorgeBeingReal","rukia_1",
        "Alxii","4Click_","i6iicv","xiiHerox","1ScarFace","deficency","us9","werfault","CyVD","toriset",
        "AbuMeshal","Negotiatorr","epicmines33","J7aa","Woipa","1Lefan","KinderBueno__","Reflxctively",
        "NotriousAsser","uhtori_","deqressing","Exvqlt","In4_n","BotsisDunatos","moesh21","PrimeRiseNan",
        "ChairmanSteve","echoofeteinty","1Bl0oD_","Tvrki","2mtm","1M7mmD","AquixSucralos","IlyasDePoot",
        "qEGYPT","qHadz","Hotrixe","IRealMohammad","YousefXOfficial","1Shnider","runiedoll","502x","3rfu",
        "Y2sF","Zywolt","A_ns","IV0lT_","Kasprov","Veeep","Valyard","1_Tamim_1","shuacu","robglr",
        "0HqSon_","0Lefan","0PvP_","0fRanddy","0ylq","1GokuBlack","1LaB_","1Meran","1Mukhtar","1N3xus",
        "1Nabulsi","1Retired","1Y2sR","1Yossry","1flyn","3ayb","420zero","4Fl1ckz_","ADHMG3AN",
        "Abo_Jmal","AbuHmD","AbuTariq","AlOwAgamer_YT","Andyiraqi","Angels0fFear","Arbazzz","BokiX__",
        "Bunkrat","Cukkyy_","Emadd","Ev2n","Exvqlt","EyesO_Diamond","FastRank","Firas","GlowDown_","GsOMAR",
        "GymIsTherapy","ILoveDcT","ILoveLGN","ItzDitto","Iv2a","Jinaaan","JustRois_","K1ngHO","KinderBueno__",
        "Lyrnxx","M4_4","MH0D","MHNDG3AN","MILG511","MSavege","MVP11","Magdi_Moslih","Morninng","Mwl4",
        "Neeres","NotriousAsser","OnlyK1nq","OtuerBanks","Pipars6","Pixerrr","Postme","Pynifical","RYSnya",
        "Reflxctively","Revolu1ion_","Rma7","SJOD","SKILLEN","Sanziro","Schneider01","SenpaiB7r","Teqches",
        "TheDestroyer24","Tugcee","Unavailabl7","Werthly","Y27X","Y2sF","Yarin","YousefXOfficial","_Ka5",
        "_LMY","_iMuslim_","_sadeq","comsterr","decaliis","deqressing","dragon_hma","e9_","fallenintosleep",
        "hox4o","hussee862","i6iicv","iDhoom","iNERO0","iidouble","im3losh","isfa7_","mitsichu","mohmad_q8",
        "osm2","qHadz","rqom","scarletsolider","snowpieces","swhq","tHeViZi","teddynicelol","uBarbie","vS1nT",
        "xKh2l3d","xiDayzer","younes_dz_xx_","yzed","z1HypersXz","z47z","LordFisk","t6q","iEusKid","7yrx",
        "KWZ","SiSalahh","_Kian97","0_0","0Tired","solviera","XkawaiiHinoiiri","anass","b2ssam","BanaPeel",
        "__SKYWALKER","__ChippedBlade__","0viq","ugass","ImadeThisAcc","Werlthy","IMADEVIEL11","Fqvies",
        "CroSsUser1Fdx0F9","ICrusader","Killua_7y","RealALY","jonas2246","sorryfull","bedoisdead","1Meran"
    ).toHashSet()

    private fun parseStaffConstant(constant: String?): Set<String> {
        if (constant.isNullOrBlank()) return emptySet()
        // Split on newlines, commas or semicolons, trim and filter blanks
        return constant.split(Regex("\\r?\\n|,|;"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toHashSet()
    }

    private fun isStaff(name: String?): Boolean {
        if (name.isNullOrBlank()) return false
        val n = name.trim()
        // First check exact ignore-case match
        if (staffSet.any { it.equals(n, ignoreCase = true) }) return true
        // Then check contains (ignore-case) either direction for robustness
        if (staffSet.any { s -> n.contains(s, ignoreCase = true) || s.contains(n, ignoreCase = true) }) return true
        return false
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
        // Update staffSet every tick based on selected server mode.
        // BlocksMC -> use local set; otherwise parse the StaffList constants.
        staffSet = when (staffMode.get().lowercase()) {
            "cubecraft" -> parseStaffConstant(StaffList.CUBECRAFT)
            "kkcraft" -> parseStaffConstant(StaffList.KKCRAFT)
            "hypixel" -> parseStaffConstant(StaffList.HYPIXEL)
            "pikanetwork" -> parseStaffConstant(StaffList.PIKA)
            "blocksmc" -> LOCAL_BLOCKSMC_STAFF
            "agerapvp" -> parseStaffConstant(StaffList.ARERAPVP)
            "hypemc" -> parseStaffConstant(StaffList.HYPEMC)
            "supercraft" -> parseStaffConstant(StaffList.SUERPCRAFT)
            "gommehd" -> parseStaffConstant(StaffList.GOMMA)
            "gamster" -> parseStaffConstant(StaffList.GAMSTER)
            "vimemc" -> parseStaffConstant(StaffList.VIMEMC)
            else -> emptySet()
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

                    val staffSpectateList = players.filter { it !in checkedSpectator && isStaff(it) }
                    val nonStaffSpectateList = players.filter { it !in checkedSpectator && !isStaff(it) }

                    // Check for players who are using spectator menu
                    val miscSpectatorList = playersInSpectatorMode - players.toSet()

                    staffSpectateList.forEach { player ->
                        notifySpectators(player)
                    }

                    nonStaffSpectateList.forEach { player ->
                        if (otherSpectator.get()) {
                            notifySpectators(player)
                        }
                    }

                    miscSpectatorList.forEach { player ->
                        val isStaff = isStaff(player)

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
                        hud.addNotification(Notification("[AntiStaff] Staff is Watching", 5000F))
                    }
                }
            }
        }
    }

    private fun notifySpectators(player: String) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return
        }

        val isStaff: Boolean = isStaff(player)

        if (isStaff && spectator.get()) {
            if (warn.get() == "Chat") {
                Chat.print("§c[STAFF] §d${player} §3is a spectators")
            } else {
                hud.addNotification(Notification("[STAFF] §d${player} §3is a spectators", 5000F))
            }
        }

        if (!isStaff && otherSpectator.get()) {
            if (warn.get() == "Chat") {
                Chat.print("§d${player} §3is a spectators")
            } else {
                hud.addNotification(Notification("[Non-STAFF] §d${player} §3is a spectators", 5000F))
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
            val isStaff: Boolean = isStaff(player)

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
                        hud.addNotification(Notification(warnings, 5000F))
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
            isStaff(playerName)
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
