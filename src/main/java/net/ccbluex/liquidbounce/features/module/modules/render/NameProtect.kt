/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.LiquidBounce.CLIENT_NAME
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.file.FileManager.friendsConfig
import net.ccbluex.liquidbounce.utils.render.ColorUtils.translateAlternateColorCodes
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value._boolean
import net.ccbluex.liquidbounce.value.intValue
import net.ccbluex.liquidbounce.value.text
import net.minecraft.network.play.server.S01PacketJoinGame
import net.minecraft.network.play.server.S40PacketDisconnect
import java.util.*
import kotlin.random.Random

object NameProtect : Module("NameProtect", Category.RENDER, subjective = true, gameDetecting = false, hideModule = false) {

    val allPlayers by _boolean("AllPlayers", false)

    val skinProtect by _boolean("SkinProtect", true)
    private val fakeName by text("FakeName", "&cMe")

    private val randomNames by _boolean("RandomNames", false) { allPlayers }
    private val randomNameLength by _boolean("RandomNameLength", false) { allPlayers && randomNames }

    private var nameLength by intValue("NameLength", 6, 6..16) {
        randomNames && allPlayers && !randomNameLength
    }

    private val minNameLength: IntegerValue = object : IntegerValue("MinNameLength", 6, 6..16) {
        override fun isSupported() = allPlayers && randomNames && randomNameLength
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtMost(maxNameLength.get())
    }

    private val maxNameLength: IntegerValue = object : IntegerValue("MaxNameLength", 14, 6..16) {
        override fun isSupported() = allPlayers && randomNames && randomNameLength
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtLeast(minNameLength.get())
    }

    private val playerRandomNames = mutableMapOf<UUID, Pair<String, Int>>()
    private val characters =  ('a'..'z') + ('0'..'9') + ('A'..'Z') + "_"

    private var savedName = -1
    private var savedMinName = -1
    private var savedMaxName = -1

    override fun onEnable() {
        if (!allPlayers) {
            return
        }

        generateRandomNames()

        // Saving other player changed name length
        if (randomNames) {
            savedName = nameLength
        }

        // Saving other player random changed name length
        if (randomNameLength) {
            savedMinName = minNameLength.get()
            savedMaxName = maxNameLength.get()
        }
    }

    override fun onDisable() {
        playerRandomNames.clear()
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (mc.thePlayer == null || mc.theWorld == null)
            return

        // Check for new players
        if (packet is S01PacketJoinGame) {
            for (playerInfo in mc.netHandler.playerInfoMap) {
                handleNewPlayer(playerInfo.gameProfile.id)
            }
        }

        // Check if player in game leave
        if (packet is S40PacketDisconnect) {
            for (playerInfo in mc.netHandler.playerInfoMap) {
                handlePlayerLeave(playerInfo.gameProfile.id)
            }
        }
    }

    /**
     * Generate random names for players
     */
    private fun generateRandomNames() {
        playerRandomNames.clear()

        if (randomNames) {
            for (playerInfo in mc.netHandler.playerInfoMap) {
                val playerUUID = playerInfo.gameProfile.id
                val randomizeName = (1..nameLength).joinToString("") { characters.random().toString() }
                playerRandomNames[playerUUID] = randomizeName to nameLength
            }
        }

        if (randomNameLength) {
            for (playerInfo in mc.netHandler.playerInfoMap) {
                val playerUUID = playerInfo.gameProfile.id

                val randomMinLength = Random.nextInt(minNameLength.get(), maxNameLength.get() + 1)
                val randomMaxLength = Random.nextInt(randomMinLength, maxNameLength.get() + 1)

                val randomLength = Random.nextInt(randomMinLength, randomMaxLength + 1)
                val randomizeName = (1..randomLength).joinToString("") { characters.random().toString() }

                playerRandomNames[playerUUID] = randomizeName to randomLength
            }
        }
    }

    /**
     * Handle text messages from font renderer
     */
    fun handleTextMessage(text: String): String {
        val p = mc.thePlayer ?: return text

        // If the message includes the client name, don't change it
        if ("§8[§e§l$CLIENT_NAME§8] §3" in text) {
            return text
        }

        // Modify
        var newText = text

        for (friend in friendsConfig.friends) {
            newText = newText.replace(friend.playerName, translateAlternateColorCodes(friend.alias) + "§f")
        }

        // If the Name Protect module is disabled, return the text already without further processing
        if (!state) {
            return newText
        }

        // Replace original name with fake name
        newText = newText.replace(p.name, translateAlternateColorCodes(fakeName) + "§f")

        // Replace all other player names with "Protected User" or Random Characters
        for (playerInfo in mc.netHandler.playerInfoMap) {
            val playerUUID = playerInfo.gameProfile.id

            if (allPlayers) {
                if (randomNames) {
                    val (protectedUsername, _) = playerRandomNames.getOrPut(playerUUID) {
                        val randomizeName = (1..nameLength).joinToString("") { characters.random().toString() }
                        randomizeName to nameLength
                    }

                    val escapedName = Regex.escape(playerInfo.gameProfile.name)
                    newText = newText.replace(Regex(escapedName), protectedUsername)

                    // Update all other player names when nameLength & min/maxNameLength value are changed
                    if (savedName != nameLength || savedMinName != minNameLength.get() || savedMaxName != maxNameLength.get()) {
                        generateRandomNames()
                        savedName = nameLength
                        savedMinName = minNameLength.get()
                        savedMaxName = maxNameLength.get()
                    }

                } else {
                    // Default
                    newText = newText.replace(playerInfo.gameProfile.name, "Protected User")
                }
            }
        }

        return newText
    }

    /**
     * Handle new players name
     */
    private fun handleNewPlayer(playerUUID: UUID) {

        if (allPlayers && randomNames) {
            if (randomNameLength) {
                val randomMinLength = Random.nextInt(minNameLength.get(), maxNameLength.get() + 1)
                val randomMaxLength = Random.nextInt(randomMinLength, maxNameLength.get() + 1)

                val randomLength = Random.nextInt(randomMinLength, randomMaxLength + 1)
                val randomizeName = (1..randomLength).joinToString("") { characters.random().toString() }

                playerRandomNames[playerUUID] = randomizeName to randomLength
            } else {
                val randomizeName = (1..nameLength).joinToString("") { characters.random().toString() }
                playerRandomNames[playerUUID] = randomizeName to nameLength
            }
        }
    }

    /**
     * Remove players name from map when they leaved
     */
    private fun handlePlayerLeave(playerUUID: UUID) {
        playerRandomNames.remove(playerUUID)
    }

}