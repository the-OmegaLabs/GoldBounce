package net.ccbluex.liquidbounce.utils.client

import net.ccbluex.liquidbounce.LiquidBounce
import java.awt.AWTException
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon
import java.io.IOException

class TrayUtils {
    private val support = SystemTray.isSupported()
    private var trayIcon: TrayIcon? = null
    private val isKDE = checkKDEEnvironment()

    @Throws(AWTException::class)
    fun start() {
        if (support) {
            displayTray()
        } else {
            System.err.println("System tray not supported!")
        }
    }

    fun getSupport(): Boolean {
        return support
    }

    @Throws(AWTException::class)
    private fun displayTray() {
        if (support) {
            val tray = SystemTray.getSystemTray()
            val imageUrl = LiquidBounce::class.java.classLoader.getResource("assets/minecraft/liquidbounce/icon_64x64.png")
                ?: throw IllegalArgumentException("Icon resource not found")
            val image = Toolkit.getDefaultToolkit().createImage(imageUrl)
            val icon = TrayIcon(image, "GoldBounce")
            icon.isImageAutoSize = true
            icon.toolTip = "GoldBounce Backend"
            tray.add(icon)
            trayIcon = icon
            addNotification("GoldBounce", "Starting mod...", TrayIcon.MessageType.INFO)
        }
    }

    @Throws(AWTException::class)
    fun addNotification(title: String, message: String, type: TrayIcon.MessageType) {
        if (isKDE && tryKdeNotification(title, message)) {
            return
        }

        if (support && trayIcon != null) {
            trayIcon?.displayMessage(title, message, type)
        }
    }

    /**
     * 检测KDE桌面环境
     */
    private fun checkKDEEnvironment(): Boolean {
        val desktop = System.getenv("XDG_CURRENT_DESKTOP")?.lowercase() ?: ""
        val session = System.getenv("DESKTOP_SESSION")?.lowercase() ?: ""

        return desktop.contains("kde") ||
                session.contains("kde") ||
                desktop.contains("plasma") ||
                session.contains("plasma")
    }

    /**
     * 尝试使用KDE的notify-send发送通知
     * @return true 发送成功, false 发送失败
     */
    private fun tryKdeNotification(title: String, message: String): Boolean {
        return try {
            val iconPath = getNotificationIconPath()

            val command = if (iconPath != null) {
                arrayOf("notify-send", title, message, "-a GoldBounce")
            } else {
                arrayOf("notify-send", title, message, "-a GoldBounce")
            }
            val process = Runtime.getRuntime().exec(command)
            process.waitFor()
            process.exitValue() == 0
        } catch (e: IOException) {
            System.err.println("Failed to send KDE notification: ${e.message}")
            false
        } catch (e: InterruptedException) {
            System.err.println("Notification process interrupted")
            false
        } catch (e: Exception) {
            System.err.println("Error in KDE notification: ${e.message}")
            false
        }
    }

    /**
     * 获取通知图标路径
     */
    private fun getNotificationIconPath(): String? {
        return try {
            val iconUrl = LiquidBounce::class.java.classLoader.getResource("assets/minecraft/liquidbounce/icon_64x64.png")
            if (iconUrl != null) {
                val input = iconUrl.openStream()
                val tempFile = java.io.File.createTempFile("liquidbounce_icon", ".png")
                tempFile.deleteOnExit()

                java.io.FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
                tempFile.absolutePath
            } else {
                null
            }
        } catch (e: Exception) {
            System.err.println("Failed to extract notification icon: ${e.message}")
            null
        }
    }
}