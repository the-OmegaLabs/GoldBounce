package net.ccbluex.liquidbounce.utils.client

import net.ccbluex.liquidbounce.LiquidBounce
import java.awt.AWTException
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon

class TrayUtils {
    private val support = SystemTray.isSupported()
    private val trayIcon: TrayIcon? = null
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
    fun displayTray() {
        if(getSupport()){
            val tray = SystemTray.getSystemTray()
            val imageUrl = LiquidBounce::class.java.classLoader.getResource("assets/minecraft/liquidbounce/icon_64x64.png")
                ?: throw IllegalArgumentException("Icon resource not found")
            val image = Toolkit.getDefaultToolkit().createImage(imageUrl)
            val trayIcon = TrayIcon(image, "GoldBounce")
            trayIcon.isImageAutoSize = true
            trayIcon.toolTip = "Notification Tool"
            tray.add(trayIcon)
            trayIcon.displayMessage("GoldBounce", "Starting mod...", TrayIcon.MessageType.INFO)
            
        }
    }

    @Throws(AWTException::class)
    fun addNotification(title: String, message: String, type: TrayIcon.MessageType) {
        if(getSupport()){
            trayIcon?.displayMessage(title, message, type)
        }
    }
}