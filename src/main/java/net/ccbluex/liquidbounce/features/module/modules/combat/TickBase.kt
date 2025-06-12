/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 *
 * --- IMPROVED TickBase ---
 * This version introduces a more advanced, resource-based system for tick manipulation
 * to improve bypass capabilities against modern anti-cheats.
 *
 * Key features:
 * - Tick Credit System: "Charges" ticks by slowing down out of combat to "spend" on a speed-up in combat.
 * - Pre-Attack Shifting: Triggers the time shift precisely before an attack for maximum effectiveness.
 * - Blink Integration: Holds packets during the shift to prevent packet spam and desync, mimicking lag.
 * - Dynamic & Safer Logic: Includes cooldowns, better state management, and more robust checks.
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleManager.getModule
import net.ccbluex.liquidbounce.features.module.modules.player.Blink
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.SimulatedPlayer
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.ccbluex.liquidbounce.utils.render.RenderUtils.glColor
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.value.*
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11.*
import java.awt.Color

object TickBase : Module("TickBase", Category.COMBAT) {

    // --- Main Settings ---
    private val maxRangeToAttack: FloatValue = FloatValue("MaxRangeToAttack", 5.0f, 3f..8f)
    private val minRangeToAttack: FloatValue = FloatValue("MinRangeToAttack", 3.5f, 3f..8f)

    // --- Tick Credit System ---
    private val chargeMode by boolean("ChargeMode", true)
    private val chargeSpeed by int("ChargeSpeed", 2, 1..10, ) { chargeMode }
    private val maxCredit by int("MaxCredit", 20, 5..100)
    private val chargeInCombat by boolean("ChargeInCombat", false, ) { chargeMode }

    // --- Shift Mechanics ---
    private val shiftOnAttack by boolean("ShiftOnAttack", true)
    private val blinkOnShift by boolean("BlinkOnShift", true)
    private val shiftCooldown by int("ShiftCooldown", 500, 0..2000)
    private val forceGround by boolean("ForceGround", true)
    private val pauseOnFlag by boolean("PauseOnFlag", true)

    // --- Visuals ---
    private val line by boolean("Line", true)
    private val rainbow by boolean("Rainbow", false, ) { line }
    private val red by int("R", 0, 0..255,  ) { !rainbow && line }
    private val green by int("G", 255, 0..255, ) { !rainbow && line }
    private val blue by int("B", 0, 0..255,  ){ !rainbow && line }

    // --- Internal State ---
    private var tickCredit = 0
    private var isShifting = false
    private var ticksToSkip = 0
    private var chargeCounter = 0
    private val shiftTimer = MSTimer()

    private val tickBuffer = mutableListOf<TickData>()

    override fun onEnable() {
        reset()
    }

    override fun onDisable() {
        reset()
    }

    private fun reset() {
        tickCredit = 0
        isShifting = false
        ticksToSkip = 0
        chargeCounter = 0
        tickBuffer.clear()
        shiftTimer.reset()
        if (Blink.state) {
            Blink.state = false
        }
    }
    var stopMove = false
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer == null || mc.theWorld == null || mc.thePlayer.isRiding) {
            reset()
            return
        }

        // Handle scheduled client-side tick skipping after a shift
        if (ticksToSkip > 0) {
            ticksToSkip--
            return
        }

        // Make sure blink is disabled if we are not shifting
        if (!isShifting && Blink.state && blinkOnShift) {
            Blink.state = false
        }

        // Logic for charging tick credits
        if (chargeMode && tickCredit < maxCredit) {
            val inCombat = KillAura.target != null || (getNearestEntityInRange(10f) != null)
            if (!inCombat || chargeInCombat) {
                chargeCounter++
                if (chargeCounter >= chargeSpeed) {
                    tickCredit++
                    chargeCounter = 0
                }
            }
        }
    }

    @EventTarget
    fun onMovementInput(event: MovementInputEvent){
        if (stopMove) {
            event.originalInput.moveForward = 0f
            event.originalInput.moveStrafe = 0f
            event.originalInput.sneak = false
            event.originalInput.jump = false
            stopMove = false
        }
    }

    @EventTarget
    fun onPreMotion(event: MotionEvent) {
        if (event.eventState == EventState.PRE && chargeMode && tickCredit < maxCredit) {
            val inCombat = KillAura.target != null || (getNearestEntityInRange(10f) != null)
            if (!inCombat || chargeInCombat) {
                if (chargeCounter < chargeSpeed - 1) {
                    stopMove = true
                }
            }
        }
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        if (!shiftOnAttack || isShifting || !shiftTimer.hasTimePassed(shiftCooldown) || tickCredit <= 0) {
            return
        }

        val target = event.targetEntity as? EntityLivingBase ?: return
        val currentDistance = mc.thePlayer.getDistanceToEntity(target)

        // Find the best future position to attack from
        val (ticks, bestTickData) = findOptimalTick(target) ?: return

        // Check if shifting is actually beneficial
        val futureDistance = bestTickData.position.distanceTo(target.positionVector)
        if (futureDistance < currentDistance && futureDistance <= maxRangeToAttack.get()) {
            performShift(ticks)
        }
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        if (mc.thePlayer.isRiding) {
            tickBuffer.clear()
            return
        }

        // Simulate future ticks
        tickBuffer.clear()
        val simulatedPlayer = SimulatedPlayer.fromClientPlayer(mc.thePlayer.movementInput)

        // Simulate up to the number of credits we have, capped by MaxCredit
        repeat(tickCredit.coerceAtMost(maxCredit)) {
            simulatedPlayer.tick()
            tickBuffer.add(
                TickData(
                    simulatedPlayer.pos,
                    simulatedPlayer.onGround,
                    simulatedPlayer.isCollidedHorizontally
                )
            )
        }
    }

    private fun findOptimalTick(target: EntityLivingBase): Pair<Int, TickData>? {
        return tickBuffer.asSequence().mapIndexed { index, tickData ->
            // We need to shift index + 1 ticks to reach this state
            val ticksToShift = index + 1
            val distance = tickData.position.distanceTo(target.positionVector)

            // Filter for valid ticks
            if (distance in minRangeToAttack.get()..maxRangeToAttack.get() &&
                (!forceGround || tickData.onGround) &&
                !tickData.isCollidedHorizontally) {

                Triple(ticksToShift, tickData, distance)
            } else {
                null
            }
        }.filterNotNull()
            .minByOrNull { it.third } // Find the tick that gets us closest while in range
            ?.let { it.first to it.second } // Return (ticksToShift, TickData)
    }

    private fun performShift(ticks: Int) {
        if (ticks <= 0) return

        isShifting = true

        // 1. Enable Blink to hold packets
        if (blinkOnShift) {
            Blink.state = true
        }

        // 2. Fast-forward client state by calling onUpdate multiple times
        repeat(ticks) {
            mc.thePlayer.onUpdate()
            tickCredit--
        }

        // 3. Disable Blink to release all packets at once
        if (blinkOnShift) {
            Blink.state = false
        }

        // 4. Schedule client-side ticks to be skipped to let the game "catch up"
        ticksToSkip = ticks

        isShifting = false
        shiftTimer.reset()
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (event.packet is S08PacketPlayerPosLook && pauseOnFlag) {
            // Server has flagged us, reset everything as a safety measure
            tickCredit = 0
            shiftTimer.reset()
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        reset()
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (!line || tickBuffer.isEmpty()) return

        val color = if (rainbow) rainbow() else Color(red, green, blue)

        synchronized(tickBuffer) {
            glPushMatrix()
            glDisable(GL_TEXTURE_2D)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
            glEnable(GL_LINE_SMOOTH)
            glEnable(GL_BLEND)
            glDisable(GL_DEPTH_TEST)
            mc.entityRenderer.disableLightmap()
            glBegin(GL_LINE_STRIP)
            glColor(color)

            val renderPosX = mc.renderManager.viewerPosX
            val renderPosY = mc.renderManager.viewerPosY
            val renderPosZ = mc.renderManager.viewerPosZ

            // Draw current position
            glVertex3d(
                mc.thePlayer.posX - renderPosX,
                mc.thePlayer.posY - renderPosY,
                mc.thePlayer.posZ - renderPosZ
            )

            for (tick in tickBuffer) {
                glVertex3d(
                    tick.position.xCoord - renderPosX,
                    tick.position.yCoord - renderPosY,
                    tick.position.zCoord - renderPosZ
                )
            }

            glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
            glEnd()
            glEnable(GL_DEPTH_TEST)
            glDisable(GL_LINE_SMOOTH)
            glDisable(GL_BLEND)
            glEnable(GL_TEXTURE_2D)
            glPopMatrix()
        }
    }

    private data class TickData(
        val position: Vec3,
        val onGround: Boolean,
        val isCollidedHorizontally: Boolean
    )

    private fun getNearestEntityInRange(range: Float): EntityLivingBase? {
        return mc.theWorld?.loadedEntityList?.asSequence()
            ?.filterIsInstance<EntityLivingBase>()
            ?.filter { EntityUtils.isSelected(it, true) && mc.thePlayer.getDistanceToEntity(it) <= range }
            ?.minByOrNull { mc.thePlayer.getDistanceToEntity(it) }
    }
}