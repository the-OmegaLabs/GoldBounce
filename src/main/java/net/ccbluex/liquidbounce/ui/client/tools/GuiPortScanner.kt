/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.ui.client.tools

import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.TabUtils
import net.ccbluex.liquidbounce.utils.extensions.SharedScopes
import net.ccbluex.liquidbounce.utils.misc.MiscUtils
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiTextField
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.io.FileWriter
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.swing.JOptionPane
import kotlin.concurrent.read
import kotlin.concurrent.write

class GuiPortScanner(private val prevGui: GuiScreen) : GuiScreen() {

    private val ports = LinkedHashSet<Int>()
    private val portsLock = ReentrantReadWriteLock()

    private lateinit var hostField: GuiTextField
    private lateinit var minPortField: GuiTextField
    private lateinit var maxPortField: GuiTextField
    private lateinit var parallelismField: GuiTextField
    private lateinit var buttonToggle: GuiButton
    private var status = "§7Waiting..."
    private var host: String = ""
    private var maxPort = 0
    private var minPort = 0

    private var scanJob: Job? = null
    private val running: Boolean
        get() = scanJob?.isActive == true

    private var checkedPort = AtomicInteger(0)

    override fun initGui() {
        Keyboard.enableRepeatEvents(true)

        hostField = GuiTextField(0, Fonts.minecraftFont, width / 2 - 100, 60, 200, 20).apply {
            isFocused = true
            maxStringLength = Int.MAX_VALUE
            text = "localhost"
        }

        minPortField = GuiTextField(1, Fonts.minecraftFont, width / 2 - 100, 90, 90, 20).apply {
            maxStringLength = 5
            text = "1"
        }

        maxPortField = GuiTextField(2, Fonts.minecraftFont, width / 2 + 10, 90, 90, 20).apply {
            maxStringLength = 5
            text = "65535"
        }

        parallelismField = GuiTextField(3, Fonts.minecraftFont, width / 2 - 100, 120, 200, 20).apply {
            maxStringLength = Int.MAX_VALUE
            text = "500"
        }

        buttonList.add(GuiButton(1, width / 2 - 100, height / 4 + 95, if (running) "Stop" else "Start").also { buttonToggle = it })
        buttonList.add(GuiButton(0, width / 2 - 100, height / 4 + 120, "Back"))
        buttonList.add(GuiButton(2, width / 2 - 100, height / 4 + 155, "Export"))

        super.initGui()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)

        Fonts.font40.drawCenteredString("Port Scanner", (width / 2).toFloat(), 34f, 0xffffff)
        Fonts.font35.drawCenteredString(
            if (running) "§7$checkedPort §8/ §7$maxPort" else status, (width / 2).toFloat(), (height / 4 + 80).toFloat(), 0xffffff
        )

        buttonToggle.displayString = if (running) "Stop" else "Start"

        hostField.drawTextBox()
        minPortField.drawTextBox()
        maxPortField.drawTextBox()
        parallelismField.drawTextBox()

        Fonts.font40.drawString("§c§lPorts:", 2, 2, Color.WHITE.hashCode())

        portsLock.read {
            var yOffset = 12
            for (port in ports) {
                Fonts.minecraftFont.drawString(port.toString(), 2, yOffset, Color.WHITE.hashCode())
                yOffset += Fonts.minecraftFont.FONT_HEIGHT
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            0 -> mc.displayGuiScreen(prevGui)
            1 -> togglePortScanning()
            2 -> exportPorts()
        }
        super.actionPerformed(button)
    }

    private fun togglePortScanning() {
        buttonToggle.displayString = if (running) {
            scanJob?.cancel()
            "Start"
        } else {
            host = hostField.text
            if (host.isEmpty()) {
                status = "§cInvalid host"
                return
            }

            minPort = minPortField.text.toIntOrNull() ?: run {
                status = "§cInvalid min port"
                return
            }

            maxPort = maxPortField.text.toIntOrNull() ?: run {
                status = "§cInvalid max port"
                return
            }

            val parallelism = parallelismField.text.toIntOrNull() ?: run {
                status = "§cInvalid parallelism"
                return
            }

            scanJob = SharedScopes.IO.launch {
                ports.clear()
                checkedPort.set(minPort)

                val semaphore = Semaphore(parallelism)

                (minPort..maxPort).map { port ->
                    launch {
                        semaphore.withPermit {
                            try {
                                Socket().use { socket ->
                                    socket.connect(InetSocketAddress(host, port), 500)
                                }

                                portsLock.write {
                                    ports.add(port)
                                }
                            } catch (ignored: Exception) {
                            }
                            checkedPort.set(port)
                        }
                    }
                }.joinAll()

                buttonToggle.displayString = "Start"
            }

            "Stop"
        }
    }

    private fun exportPorts() {
        val selectedFile = MiscUtils.saveFileChooser() ?: return
        if (selectedFile.isDirectory) return

        try {
            if (!selectedFile.exists()) selectedFile.createNewFile()

            FileWriter(selectedFile).use { fileWriter ->
                fileWriter.write("Portscan\r\n")
                fileWriter.write("Host: $host\r\n\r\n")
                fileWriter.write("Ports ($minPort - $maxPort):\r\n")
                ports.forEach { port -> fileWriter.write("$port\r\n") }
            }
            JOptionPane.showMessageDialog(null, "Exported successfully!", "Port Scanner", JOptionPane.INFORMATION_MESSAGE)
        } catch (e: Exception) {
            e.printStackTrace()
            MiscUtils.showErrorPopup("Error", "Exception class: ${e::class.java.name}\nMessage: ${e.message}")
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(prevGui)
            return
        }

        if (keyCode == Keyboard.KEY_TAB) {
            TabUtils.tab(hostField, minPortField, maxPortField, parallelismField)
        }

        if (running) return

        when {
            hostField.isFocused -> hostField.textboxKeyTyped(typedChar, keyCode)
            minPortField.isFocused && !typedChar.isLetter() -> minPortField.textboxKeyTyped(typedChar, keyCode)
            maxPortField.isFocused && !typedChar.isLetter() -> maxPortField.textboxKeyTyped(typedChar, keyCode)
            parallelismField.isFocused && !typedChar.isLetter() -> parallelismField.textboxKeyTyped(typedChar, keyCode)
        }

        super.keyTyped(typedChar, keyCode)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        hostField.mouseClicked(mouseX, mouseY, mouseButton)
        minPortField.mouseClicked(mouseX, mouseY, mouseButton)
        maxPortField.mouseClicked(mouseX, mouseY, mouseButton)
        parallelismField.mouseClicked(mouseX, mouseY, mouseButton)
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun updateScreen() {
        hostField.updateCursorCounter()
        minPortField.updateCursorCounter()
        maxPortField.updateCursorCounter()
        parallelismField.updateCursorCounter()
        super.updateScreen()
    }

    override fun onGuiClosed() {
        Keyboard.enableRepeatEvents(false)
        scanJob?.cancel()
        super.onGuiClosed()
    }
}