/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.features.command.commands


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.file.FileManager.keybindsDir
import net.ccbluex.liquidbounce.ui.client.hud.HUD.addNotification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.utils.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.SettingsUtils
import org.lwjgl.input.Keyboard
import java.awt.Desktop
import java.io.File
import java.io.IOException

object LocalKeybindsCommand : Command("localkeybinds", "localkeybind", "keybinds") {

    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        val usedAlias = args[0].lowercase()

        if (args.size <= 1) {
            chatSyntax("$usedAlias <load/save/list/delete/folder>")
            return
        }

        GlobalScope.launch {
            when (args[1].lowercase()) {
                "load" -> loadSettings(args)
                "save" -> saveSettings(args)
                "delete" -> deleteSettings(args)
                "list" -> listSettings()
                "folder" -> openSettingsFolder()
                else -> chatSyntax("$usedAlias <load/save/list/delete/folder>")
            }
        }
    }

    private suspend fun loadSettings(args: Array<String>) {
        withContext(Dispatchers.IO) {
            if (args.size <= 2) {
                chatSyntax("${args[0].lowercase()} load <name>")
                return@withContext
            }
            val k = File(keybindsDir, args[2] + ".txt")

            if (!k.exists()) {
                chat("§cKeybinds file does not exist! §e(Ensure its .txt)")
                return@withContext
            }

            try {
                chat("§eLoading settings...")
                val settings = k.readText()
                chat("§eSet settings...")
                SettingsUtils.applyKeybinds(settings)
                chat("§6Settings applied successfully.")
                addNotification(Notification("Updated Keybinds"))
                playEdit()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun saveSettings(args: Array<String>) {
        withContext(Dispatchers.IO) {
            if (args.size <= 2) {
                chatSyntax("${args[0].lowercase()} save <name> [all/values/binds/states]...")
                return@withContext
            }

            try {
                // create a file first
                val file = File(keybindsDir, args[2] + ".txt")
                file.createNewFile()
                ModuleManager.modules.forEach {
                    if (it.keyBind != Keyboard.KEY_NONE) {
                        var key = Keyboard.getKeyName(it.keyBind)
                        var moduleName = it.getName()
                        // write keybinds line-by-line
                        file.appendText("$key $moduleName\n")
                    }
                }
            } catch (e: Exception) {
                chat("$e Failed to create local keybinds.")
            }
            chat("Successfully created local keybinds.")
        }
    }

    private suspend fun deleteSettings(args: Array<String>) {
        withContext(Dispatchers.IO) {
            if (args.size <= 2) {
                chatSyntax("${args[0].lowercase()} delete <name>")
                return@withContext
            }

            val settingsFile = File(keybindsDir, args[2] + ".txt")

            if (!settingsFile.exists()) {
                chat("§cKeybinds file does not exist!")
                return@withContext
            }

            settingsFile.delete()
            chat("§6Keybinds file deleted successfully.")
        }
    }

    private suspend fun listSettings() {
        withContext(Dispatchers.IO) {
            chat("§cKeybinds:")

            val settings = keybindsDir.listFiles()?.filterNotNull() ?: return@withContext
            for (file in settings) {
                chat("> " + file.name.removeSuffix(".txt"))
            }
        }
    }

    private suspend fun openSettingsFolder() {
        withContext(Dispatchers.IO) {
            try {
                Desktop.getDesktop().open(keybindsDir)
            } catch (e: IOException) {
                LOGGER.error("Failed to open Keybinds folder.", e)
                chat("§cFailed to open Keybinds folder.")
            }
        }
    }


    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty()) return emptyList()

        return when (args.size) {
            1 -> listOf("delete", "list", "load", "save", "folder").filter { it.startsWith(args[0], true) }

            2 -> {
                when (args[0].lowercase()) {
                    "delete", "load", "save" -> {
                        val settings = keybindsDir.listFiles() ?: return emptyList()
                        settings.map { file: File -> file.name.removeSuffix(".txt") }
                    }

                    else -> emptyList()
                }
            }

            3 -> {
                when (args[0].lowercase()) {
                    "save" -> listOf("all", "default", "values", "binds", "states").filter {
                        it.startsWith(
                            args[2],
                            true
                        )
                    }

                    else -> emptyList()
                }
            }

            else -> emptyList()
        }
    }
}