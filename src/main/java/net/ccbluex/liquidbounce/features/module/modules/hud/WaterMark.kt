package net.ccbluex.liquidbounce.features.module.modules.hud

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.world.scaffolds.Scaffold
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.GlowUtils
import net.ccbluex.liquidbounce.utils.ServerUtils
import net.ccbluex.liquidbounce.utils.SilentHotbar
import net.ccbluex.liquidbounce.utils.extensions.getPing
import net.ccbluex.liquidbounce.utils.render.AnimationUtils
import net.ccbluex.liquidbounce.utils.render.AnimationUtils水影加加
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedBorderRect
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.value.TextValue
import net.ccbluex.liquidbounce.value.boolean
import net.ccbluex.liquidbounce.value.float
import net.ccbluex.liquidbounce.value.int
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.item.ItemBlock
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin

object WaterMark : Module("WaterMark", Category.HUD) {

    // region Base UI Values
    private val bgColor = Color(0, 0, 0, 120)
    private var animWidth = 0f
    private var animHeight = 0f
    private var lastStateChangeTime = 0L

    private enum class State { NONE, Normal, Scaffolding, Notifying }
    private var currentState = State.NONE

    private val ANIM_DURATION = int("AnimationDuration", 300, 0..1000)
    val normalMode = ListValue("RenderMode", arrayOf("Opai", "Opal"), "Opai")
    private val clientName = TextValue("ClientName", "Obai")
    // endregion

    // region Normal Mode: Opai
    val textColorR = int("TextColorR", 255, 0..255) { normalMode.get() == "Opai" }
    private val textColorG = int("TextColorG", 255, 0..255) { normalMode.get() == "Opai" }
    private val textColorB = int("TextColorB", 255, 0..255) { normalMode.get() == "Opai" }
    val showMemory = boolean("ShowMemory", false) { normalMode.get() == "Opai" }
    val showLatency = boolean("ShowLatency", true) { normalMode.get() == "Opai" }
    private val shadowEnabled = boolean("Shadow", false) { normalMode.get() == "Opai"}
    val shadowStrengh = int("ShadowStrength", 20, 1..20) { normalMode.get() == "Opai"}
    // endregion

    // region Normal Mode: Opal
    private val opaiColorR = int("Opal-R", 255, 0..255) { normalMode.get() == "Opal" }
    private val opaiColorG = int("Opal-G", 255, 0..255) { normalMode.get() == "Opal" }
    private val opaiColorB = int("Opal-B", 255, 0..255) { normalMode.get() == "Opal" }
    private val opaiShadow = boolean("Opal-Shadow", false) { normalMode.get() == "Opal" }
    private val opaiShadowStrength = int("Opal-ShadowStrength", 1, 1..2) { normalMode.get() == "Opal" }
    private val versionNameUp = LiquidBounce.clientVersionText
    private val versionNameDown = LiquidBounce.clientBigVersionText
    // endregion

    // region Scaffold Mode Values
    private var animatedBlocks = 0f
    // endregion

    // region Notification System
    private val notifications = CopyOnWriteArrayList<Notification>()
    private val NOTIFICATION_ANIM_SPEED = 0.2f
    private val NOTIFICATION_PADDING = 5f
    private val NOTIFICATION_HEIGHT = 35f

    // Base class for all notifications
    private abstract class Notification(
        val id: String = UUID.randomUUID().toString(),
        val creationTime: Long = System.currentTimeMillis(),
        val duration: Long,
        var title: String,
        var message: String,
    ) {
        var yOffset = 0f
        var animationProgress = 0f // Controls overall animation (in/out)
        var isMarkedForDelete = false

        // Abstract methods for each notification to implement
        abstract fun getHeight(): Float
        abstract fun getWidth(): Float
        abstract fun draw(x: Float, y: Float, width: Float)

        // Update animation state
        fun update() {
            val targetProgress = if (isFading()) 0f else 1f
            animationProgress = AnimationUtils水影加加.animate(targetProgress, animationProgress, NOTIFICATION_ANIM_SPEED)
            if (isFading() && animationProgress < 0.05f) {
                isMarkedForDelete = true
            }
        }

        // Check if the notification should start fading out
        fun isFading(): Boolean = System.currentTimeMillis() > creationTime + duration || isMarkedForDelete
    }

    // Notification style with a toggle icon
    private class ToggleNotification(
        title: String,
        message: String,
        duration: Long,
        val enabled: Boolean
    ) : Notification(duration = duration, title = title, message = message) {
        private val toggleIconOn = ResourceLocation("liquidbounce/notification/toggle_on.png") // TODO: Replace with your path
        private val toggleIconOff = ResourceLocation("liquidbounce/notification/toggle_off.png") // TODO: Replace with your path

        override fun getHeight(): Float = NOTIFICATION_HEIGHT
        override fun getWidth(): Float = 150f

        override fun draw(x: Float, y: Float, width: Float) {
            val alpha = (255 * animationProgress).toInt()
            if (alpha <= 10) return

            val icon = if (enabled) toggleIconOn else toggleIconOff
            val iconSize = 25f
            val padding = (getHeight() - iconSize) / 2f
            val offset = 10F

            // Draw Icon
            //RenderUtils.drawImage(icon, (x + padding).toInt(), (y + padding).toInt(), iconSize.toInt(), iconSize.toInt(), Color(255, 255, 255, alpha))
            drawToggleButton(x,y,35F,enabled)

            // Draw Text
            val textX = x + iconSize + padding * 2
            Fonts.font40.drawString(title, textX+offset, y + padding+4, Color(255, 255, 255, alpha).rgb)
            Fonts.font35.drawString(message, textX+offset, y + padding + 14, Color(200, 200, 200, alpha).rgb)
        }
    }

    // Notification style with a custom icon
    private class IconNotification(
        title: String,
        message: String,
        duration: Long,
        val icon: ResourceLocation
    ) : Notification(duration = duration, title = title, message = message) {
        override fun getHeight(): Float = NOTIFICATION_HEIGHT
        override fun getWidth(): Float = 160f // Example width, can be dynamic

        override fun draw(x: Float, y: Float, width: Float) {
            val alpha = (255 * animationProgress).toInt()
            if (alpha <= 10) return

            val iconSize = 20f
            val padding = (getHeight() - iconSize) / 2f

            // Draw Icon
            RenderUtils.drawRoundedRect(x + padding - 3, y + padding - 3, x + padding + iconSize + 3, y + padding + iconSize + 3, Color(70, 120, 255, alpha).rgb, 5f)
            RenderUtils.drawImage(icon, (x + padding).toInt(), (y + padding).toInt(), iconSize.toInt(), iconSize.toInt(), Color(255, 255, 255, alpha))

            // Draw Text
            val textX = x + iconSize + padding * 2 + 6
            Fonts.font40.drawString(title, textX, y + padding - 2, Color(255, 255, 255, alpha).rgb)
            Fonts.font35.drawString(message, textX, y + padding + 12, Color(200, 200, 200, alpha).rgb)
        }
    }

    /**
     * Public API to show a toggle-style notification.
     */
    fun showToggleNotification(title: String, message: String, enabled: Boolean, duration: Long = 3000L) {
        notifications.add(ToggleNotification(title, message, duration, enabled))
    }

    /**
     * Public API to show an icon-style notification.
     */
    fun showIconNotification(title: String, message: String, icon: ResourceLocation, duration: Long = 3000L) {
        notifications.add(IconNotification(title, message, duration, icon))
    }

    /**
     * Public API to manually remove a notification.
     */
    fun removeNotification(id: String) {
        notifications.find { it.id == id }?.isMarkedForDelete = true
    }
    // endregion

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        val sr = ScaledResolution(mc)
        val now = System.currentTimeMillis()

        // 1. Update and manage notifications
        updateNotifications()

        // 2. Determine current state
        val isScaffolding = LiquidBounce.moduleManager.getModule(Scaffold::class.java).state
        val newState = when {
            notifications.isNotEmpty() -> State.Notifying
            isScaffolding -> State.Scaffolding
            else -> State.Normal
        }

        if (newState != currentState) {
            currentState = newState
            lastStateChangeTime = now
        }

        // 3. Calculate target dimensions based on state
        val (targetWidth, targetHeight) = when (currentState) {
            State.Notifying -> calculateNotificationsSize()
            State.Scaffolding -> calculateScaffoldSize()
            else -> calculateNormalSize()
        }

        // 4. Animate the main container's dimensions
        updateContainerAnimation(targetWidth, targetHeight)

        // 5. Draw the container background and shadow
        drawBackground(sr)

        // 6. Draw the UI content based on state
        when (currentState) {
            State.Notifying -> drawNotificationsUI(sr)
            State.Scaffolding -> drawScaffoldUI(sr)
            else -> drawNormalUI(sr)
        }
    }

    private fun updateNotifications() {
        notifications.forEach { it.update() }
        notifications.removeAll { it.isMarkedForDelete }
    }

    private fun updateContainerAnimation(targetWidth: Float, targetHeight: Float) {
        val speed = 0.5f
        animWidth = AnimationUtils水影加加.animate(targetWidth, animWidth, speed * RenderUtils.deltaTime * 0.025f)
        animHeight = AnimationUtils水影加加.animate(targetHeight, animHeight, speed * RenderUtils.deltaTime * 0.025f)
    }

    // --- Size Calculation ---
    private fun calculateNormalSize(): Pair<Float, Float> {
        return if (normalMode.get() == "Opal") calculateOpalNormalSize() else calculateOpaiNormalSize()
    }

    private fun calculateOpaiNormalSize(): Pair<Float, Float> {
        val watermarkText = buildString {
            append("${clientName.get()} | ${mc.session.username} | ${Minecraft.getDebugFPS()}fps")
            if (showLatency.get()) append(" | ${mc.thePlayer.getPing()}ms")
            if (showMemory.get()) append(" | RAM: ${getUsedMemory()}/${getMaxMemory()}MB")
        }
        val textWidth = Fonts.fontHonor40.getStringWidth(watermarkText).toFloat()
        return Pair(textWidth + 40f, 30f)
    }

    private fun calculateOpalNormalSize(): Pair<Float, Float> {
        val serverip = ServerUtils.remoteIp
        val playerPing = "${mc.thePlayer.getPing()}ms"
        val textWidth = Fonts.fontHonor40.getStringWidth(clientName.get())
        val imageLen = 21f
        val uiToUIDistance = 4f
        val textBar2 = max(Fonts.fontHonor40.getStringWidth(versionNameUp), Fonts.fontHonor35.getStringWidth(versionNameDown))
        val textBar3 = max(Fonts.fontHonor40.getStringWidth(serverip), Fonts.fontHonor35.getStringWidth(playerPing))
        val lineWidth = 2f
        val allLen = 2 + imageLen + uiToUIDistance + textWidth + uiToUIDistance + lineWidth + uiToUIDistance + textBar2 + uiToUIDistance + lineWidth + uiToUIDistance + textBar3 + 2
        return Pair(allLen, 27f)
    }

    private fun calculateScaffoldSize(): Pair<Float, Float> {
        val stack = mc.thePlayer?.inventory?.getStackInSlot(SilentHotbar.currentSlot)
        val textWidth = Fonts.font40.getStringWidth("${stack?.stackSize ?: 0} blocks").toFloat()
        return Pair(200f + textWidth, 36f)
    }

    private fun calculateNotificationsSize(): Pair<Float, Float> {
        if (notifications.isEmpty()) return Pair(0f, 0f)

        var totalHeight = 0f
        var maxWidth = 0f

        for (notif in notifications) {
            // Use animated height for smooth transitions
            totalHeight += notif.getHeight() * notif.animationProgress
            if (notif.getWidth() > maxWidth) {
                maxWidth = notif.getWidth()
            }
        }

        val padding = if (notifications.size > 1) (notifications.size -1) * NOTIFICATION_PADDING else 0f

        return Pair(maxWidth, totalHeight + padding)
    }


    // --- UI Drawing ---
    private fun drawBackground(sr: ScaledResolution) {
        val screenWidth = sr.scaledWidth.toFloat()
        val fixedTopY = sr.scaledHeight / 9f
        val startX = (screenWidth - animWidth) / 2

        if (animWidth <= 0 || animHeight <= 0) return

        RenderUtils.drawRoundedRect(
            startX,
            fixedTopY,
            startX + animWidth,
            fixedTopY + animHeight,
            bgColor.rgb,
            15f
        )

        // Shadow System
        when {
            shadowEnabled.get() && normalMode.get() == "Opai" -> {
                GlowUtils.drawGlow(startX - 5, fixedTopY, animWidth + 10, animHeight, shadowStrengh.get(), bgColor)
            }
            opaiShadow.get() && normalMode.get() == "Opal" -> {
                GlowUtils.drawGlow(startX, fixedTopY, animWidth, animHeight, opaiShadowStrength.get() * 13, Color(255, 255, 255, 80))
            }
            currentState == State.Notifying -> { // Optional: A dedicated shadow for notifications
                GlowUtils.drawGlow(startX, fixedTopY, animWidth, animHeight, 20, Color(50, 50, 50, 100))
            }
        }
    }

    private fun drawNormalUI(sr: ScaledResolution) {
        if (normalMode.get() == "Opal") {
            drawOpalNormalUI(sr, animWidth, animHeight)
        } else {
            drawOpaiNormalUI(sr, animWidth, animHeight)
        }
    }

    private fun drawOpaiNormalUI(sr: ScaledResolution, width: Float, height: Float) {
        val startX = (sr.scaledWidth - width) / 2
        val startY = sr.scaledHeight / 9f
        val textColor = Color(textColorR.get(), textColorG.get(), textColorB.get())

        val logoWidth = 20
        val watermarkText = buildString {
            append("${clientName.get()} | ${mc.session.username} | ${Minecraft.getDebugFPS()}fps")
            if (showLatency.get()) append(" | ${mc.thePlayer.getPing()}ms")
            if (showMemory.get()) append(" | RAM: ${getUsedMemory()}/${getMaxMemory()}MB")
        }
        val textWidth = Fonts.fontHonor40.getStringWidth(watermarkText)

        val contentWidth = logoWidth + 10 + textWidth
        val offsetX = (width - contentWidth) / 2

        RenderUtils.drawImage(
            ResourceLocation("liquidbounce/obai.png"),
            (startX + offsetX).toInt(),
            (startY + height / 2 - 9).toInt(),
            18, 18, textColor
        )

        var currentX = startX + offsetX + logoWidth + 5
        Fonts.fontHonor40.drawString(clientName.get(), currentX, startY + height/2 - Fonts.fontHonor40.FONT_HEIGHT/2, textColor.rgb)

        currentX += Fonts.fontHonor40.getStringWidth(clientName.get() + " | ")
        Fonts.fontHonor40.drawString(
            "| ${mc.session.username} | ${Minecraft.getDebugFPS()}fps" +
                    (if (showLatency.get()) " | ${mc.thePlayer.getPing()}ms" else "") +
                    (if (showMemory.get()) " | RAM: ${getUsedMemory()}/${getMaxMemory()}MB" else ""),
            currentX - 3,
            startY + height/2 - Fonts.fontHonor40.FONT_HEIGHT/2,
            Color.WHITE.rgb
        )
    }

    private fun drawOpalNormalUI(sr: ScaledResolution, width: Float, height: Float) {
        val startX = (sr.scaledWidth - width) / 2
        val startY = sr.scaledHeight / 9f

        val serverip = ServerUtils.remoteIp
        val playerPing = "${mc.thePlayer.getPing()}ms"
        val textWidth = Fonts.fontHonor40.getStringWidth(clientName.get())

        val colorAL = Color(opaiColorR.get(), opaiColorG.get(), opaiColorB.get(), 255)
        val imageLen = 21F
        val containerToUiDistance = 2F
        val uiToUIDistance = 4F
        val textBar2 = max(Fonts.fontHonor40.getStringWidth(versionNameUp), Fonts.fontHonor35.getStringWidth(versionNameDown))
        val textBar3 = max(Fonts.fontHonor40.getStringWidth(serverip), Fonts.fontHonor35.getStringWidth(playerPing))
        val lineWidth = 2F
        val fastLen1 = containerToUiDistance + imageLen + uiToUIDistance

        RenderUtils.drawImage(ResourceLocation("liquidbounce/obai.png"), (startX + containerToUiDistance + 2).toInt(), (startY + 4).toInt(), 19, 19, colorAL)
        Fonts.fontHonor40.drawString(clientName.get(), startX + fastLen1, startY + 9F, colorAL.rgb)
        Fonts.fontHonor40.drawString("|", startX + fastLen1 + textWidth + uiToUIDistance - 1F, startY + 9F, Color(120, 120, 120, 250).rgb)
        Fonts.fontHonor40.drawString(versionNameUp, startX + fastLen1 + textWidth + uiToUIDistance + lineWidth + uiToUIDistance, startY + 4.5F, Color.WHITE.rgb)
        Fonts.fontHonor35.drawString(versionNameDown, startX + fastLen1 + textWidth + uiToUIDistance + lineWidth + uiToUIDistance, startY + 14F, Color(170, 170, 170, 170).rgb)
        Fonts.fontHonor40.drawString("|", startX + fastLen1 + textWidth + uiToUIDistance + lineWidth + uiToUIDistance + textBar2 + uiToUIDistance - 1F, startY + 9F, Color(120, 120, 120, 250).rgb)
        Fonts.fontHonor40.drawString(serverip, startX + fastLen1 + textWidth + uiToUIDistance + lineWidth + uiToUIDistance + textBar2 + uiToUIDistance + lineWidth + uiToUIDistance, startY + 4.5F, Color.WHITE.rgb)
        Fonts.fontHonor35.drawString(playerPing, startX + fastLen1 + textWidth + uiToUIDistance + lineWidth + uiToUIDistance + textBar2 + uiToUIDistance + lineWidth + uiToUIDistance, startY + 14F, Color(170, 170, 170, 170).rgb)
    }

    private fun drawScaffoldUI(sr: ScaledResolution) {
        val width = animWidth
        val height = animHeight
        val startX = (sr.scaledWidth - width) / 2
        val startY = sr.scaledHeight / 9f

        val containerPadding = 8f
        val elementSpacing = 4f
        val progressWidth = 120f
        val iconSize = 20f

        val stack = mc.thePlayer?.inventory?.getStackInSlot(SilentHotbar.currentSlot)
        val blockAmount = stack?.stackSize ?: 0
        val countText = "$blockAmount blocks"
        val textWidth = Fonts.font40.getStringWidth(countText)

        val rightPadding = containerPadding + (width - (iconSize + elementSpacing * 2 + progressWidth + textWidth)) / 2

        // Item Icon
        if (stack?.item is ItemBlock) {
            glPushMatrix()
            RenderHelper.enableGUIStandardItemLighting()
            mc.renderItem.renderItemAndEffectIntoGUI(stack, (startX + containerPadding).toInt(), (startY + (height - 16) / 2).toInt())
            RenderHelper.disableStandardItemLighting()
            glPopMatrix()
        }

        // Progress Bar BG
        val progressBarY = startY + height / 2 - 2
        RenderUtils.drawRoundedRect(startX + containerPadding + iconSize + elementSpacing, progressBarY, startX + containerPadding + iconSize + elementSpacing + progressWidth, progressBarY + 4f, Color(0, 0, 0, 100).rgb, 2f)

        // Progress Bar FG
        val targetProgress = blockAmount / 64f
        animatedBlocks += (targetProgress - animatedBlocks) * 0.15f
        val animatedWidth = progressWidth * animatedBlocks
        RenderUtils.drawRoundedRect(startX + containerPadding + iconSize + elementSpacing, progressBarY, startX + containerPadding + iconSize + elementSpacing + animatedWidth, progressBarY + 4f, Color(255, 255, 255, 200).rgb, 2f)

        // Text
        Fonts.font40.drawString(countText, startX + width - rightPadding - textWidth, startY + height / 2 - Fonts.font40.FONT_HEIGHT / 2, Color.WHITE.rgb)
    }

    private fun drawNotificationsUI(sr: ScaledResolution) {
        val screenWidth = sr.scaledWidth.toFloat()
        val startX = (screenWidth - animWidth) / 2
        var currentY = sr.scaledHeight / 9f

        // Use scissor test to clip notifications within the rounded rect
        glEnable(GL_SCISSOR_TEST)
        RenderUtils.makeScissorBox(startX, currentY, startX + animWidth, currentY + animHeight)

        for (notif in notifications) {
            val notifHeight = notif.getHeight() * notif.animationProgress
            if (notifHeight > 0) {
                // Calculate each notification's Y position and animate it
                val targetY = currentY
                notif.yOffset = AnimationUtils水影加加.animate(targetY, notif.yOffset, NOTIFICATION_ANIM_SPEED)

                notif.draw(startX, notif.yOffset, animWidth)

                // Increment currentY for the next notification
                currentY += notifHeight + NOTIFICATION_PADDING
            }
        }

        glDisable(GL_SCISSOR_TEST)
    }
    private fun drawToggleButton(StartX:Float, StartY: Float, BigBoardHeight: Float, ModuleState: Boolean){
        val buttonHeight = 19F
        val buttonWidth = 32F
        val buttonRounded = buttonHeight/2
        val buttonToButtonDistance = 4F
        val smallButtonHeight = buttonHeight-buttonToButtonDistance*2
        val smallButtonRounded = smallButtonHeight/2
        val smallButtonWidth = smallButtonHeight
        val toBigBorderLen = 6F
        val ButtonStartX = BigBoardHeight/2 - buttonHeight/2
        var smallButtonStartX = 0F
        if (ModuleState) {
            smallButtonStartX = StartX + toBigBorderLen + buttonWidth - buttonToButtonDistance - smallButtonWidth
        }else{
            smallButtonStartX= StartX + toBigBorderLen + buttonToButtonDistance
        }
        if (ModuleState){
            drawRoundedBorderRect(StartX+toBigBorderLen,StartY+ButtonStartX,StartX+toBigBorderLen+buttonWidth,StartY+ButtonStartX+buttonHeight,1F,Color(opaiColorR.get(),opaiColorG.get(),opaiColorB.get(),255).rgb,Color(opaiColorR.get(),opaiColorG.get(),opaiColorB.get(),255).rgb,buttonRounded)
        }else{
            drawRoundedBorderRect(StartX+toBigBorderLen,StartY+ButtonStartX,StartX+toBigBorderLen+buttonWidth,StartY+ButtonStartX+buttonHeight,color1 = Color(10,10,10,255).rgb,color2 = Color(120,120,120,255).rgb, radius = buttonRounded, width = 3F)
        }
        val awtColorChanges = if (ModuleState){
            Color(safeColor(opaiColorR.get()-120),safeColor(opaiColorG.get()-120),safeColor(opaiColorB.get()-120),255).rgb
        }else{
            Color(120,120,120,255).rgb
        }
        drawRoundedBorderRect(smallButtonStartX,StartY+ButtonStartX+buttonToButtonDistance,smallButtonStartX+smallButtonWidth,StartY+ButtonStartX+buttonToButtonDistance+smallButtonHeight,1F,
            awtColorChanges,awtColorChanges,smallButtonRounded)
        //抗锯齿
    }
    private fun safeColor(ColorA: Int) : Int{
        if (ColorA>255) return 255
        else if (ColorA<0) return 0
        else return ColorA
    }
    // --- Utility ---
    private fun getUsedMemory() = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024)
    private fun getMaxMemory() = Runtime.getRuntime().maxMemory() / (1024 * 1024)
}
