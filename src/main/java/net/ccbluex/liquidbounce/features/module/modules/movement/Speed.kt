/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.aac.AACHop3313
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.aac.AACHop350
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.aac.AACHop4
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.aac.AACHop5
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.hypixel.HypixelHop
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.hypixel.HypixelLowHop
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.intave.IntaveHop14
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.intave.IntaveTimer14
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.matrix.MatrixHop
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.matrix.MatrixSlowHop
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.matrix.OldMatrixHop
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.ncp.*
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.other.*
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.spartan.SpartanYPort
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.spectre.SpectreBHop
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.spectre.SpectreLowHop
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.spectre.SpectreOnGround
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.verus.VerusFHop
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.verus.VerusHop
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.verus.VerusLowHop
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.verus.VerusLowHopNew
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.vulcan.VulcanGround288
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.vulcan.VulcanHop
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.vulcan.VulcanLowHop
import net.ccbluex.liquidbounce.features.module.modules.world.scaffolds.Scaffold
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.value.*

object Speed : Module("Speed", Category.MOVEMENT, hideModule = false) {

    private val speedModes = arrayOf(

        // NCP
        NCPBHop,
        NCPFHop,
        SNCPBHop,
        NCPHop,
        NCPYPort,
        UNCPHop,
        UNCPHopNew,

        // AAC
        AACHop3313,
        AACHop350,
        AACHop4,
        AACHop5,

        // Spartan
        SpartanYPort,

        // Spectre
        SpectreLowHop,
        SpectreBHop,
        SpectreOnGround,

        // Verus
        VerusHop,
        VerusFHop,
        VerusLowHop,
        VerusLowHopNew,

        // Vulcan
        VulcanHop,
        VulcanLowHop,
        VulcanGround288,

        // Matrix
        OldMatrixHop,
        MatrixHop,
        MatrixSlowHop,



        // Server specific
        HypixelHop,

        // Other
        Boost,
        Frame,
        MiJump,
        OnGround,
        SlowHop,
        Legit,
        CustomSpeed,
        GrimAC
    )

    /**
     * Old/Deprecated Modes
     */
    private val deprecatedMode = arrayOf(
        TeleportCubeCraft,

        OldMatrixHop,

        VerusLowHop,

        SpectreLowHop, SpectreBHop, SpectreOnGround,

        AACHop3313, AACHop350, AACHop4,

        NCPBHop, NCPFHop, SNCPBHop, NCPHop, NCPYPort,

        MiJump, Frame, BlocksMCHop,
        // Intave
        IntaveHop14,
        IntaveTimer14,
    )

    private val showDeprecatedValue = object : BoolValue("DeprecatedMode", true) {
        override fun onUpdate(value: Boolean) {
            mode.changeValue(modesList.first { it !in deprecatedMode }.modeName)
            mode.updateValues(modesList.filter { value || it !in deprecatedMode }.map { it.modeName }.toTypedArray())
        }
    }

    private val showDeprecated by showDeprecatedValue

    private var modesList = speedModes

    val mode = choices("Mode", modesList.map { it.modeName }.toTypedArray(), "NCPBHop")
    val disableSameY by _boolean("DisableSameY", false)
    // Custom Speed
    val customY by floatValue("CustomY", 0.42f, 0f..4f) { mode.get() == "Custom" }
    val customGroundStrafe by floatValue("CustomGroundStrafe", 1.6f, 0f..2f) { mode.get() == "Custom" }
    val customAirStrafe by floatValue("CustomAirStrafe", 0f, 0f..2f) { mode.get() == "Custom" }
    val customGroundTimer by floatValue("CustomGroundTimer", 1f, 0.1f..2f) { mode.get() == "Custom" }
    val customAirTimerTick by intValue("CustomAirTimerTick", 5, 1..20) { mode.get() == "Custom" }
    val customAirTimer by floatValue("CustomAirTimer", 1f, 0.1f..2f) { mode.get() == "Custom" }

    // Extra options
    val resetXZ by _boolean("ResetXZ", false) { mode.get() == "Custom" }
    val resetY by _boolean("ResetY", false) { mode.get() == "Custom" }
    val notOnConsuming by _boolean("NotOnConsuming", false) { mode.get() == "Custom" }
    val notOnFalling by _boolean("NotOnFalling", false) { mode.get() == "Custom" }
    val notOnVoid by _boolean("NotOnVoid", true) { mode.get() == "Custom" }

    // TeleportCubecraft Speed
    val cubecraftPortLength by floatValue("CubeCraft-PortLength", 1f, 0.1f..2f)
    { mode.get() == "TeleportCubeCraft" }

    // IntaveHop14 Speed
    val boost by _boolean("Boost", true) { mode.get() == "IntaveHop14" }
    val initialBoostMultiplier by floatValue("InitialBoostMultiplier", 1f, 0.01f..10f)
    { boost && mode.get() == "IntaveHop14" }
    val intaveLowHop by _boolean("LowHop", true) { mode.get() == "IntaveHop14" }
    val strafeStrength by floatValue("StrafeStrength", 0.29f, 0.1f..0.29f)
    { mode.get() == "IntaveHop14" }
    val groundTimer by floatValue("GroundTimer", 0.5f, 0.1f..5f) { mode.get() == "IntaveHop14" }
    val airTimer by floatValue("AirTimer", 1.09f, 0.1f..5f) { mode.get() == "IntaveHop14" }

    // UNCPHopNew Speed
    private val pullDown by _boolean("PullDown", true) { mode.get() == "UNCPHopNew" }
    val onTick by intValue("OnTick", 5, 5..9) { pullDown && mode.get() == "UNCPHopNew" }
    val onHurt by _boolean("OnHurt", true) { pullDown && mode.get() == "UNCPHopNew" }
    val shouldBoost by _boolean("ShouldBoost", true) { mode.get() == "UNCPHopNew" }
    val timerBoost by _boolean("TimerBoost", true) { mode.get() == "UNCPHopNew" }
    val damageBoost by _boolean("DamageBoost", true) { mode.get() == "UNCPHopNew" }
    val lowHop by _boolean("LowHop", true) { mode.get() == "UNCPHopNew" }
    val airStrafe by _boolean("AirStrafe", true) { mode.get() == "UNCPHopNew" }

    // MatrixHop Speed
    val matrixLowHop by _boolean("LowHop", true)
    { mode.get() == "MatrixHop" || mode.get() == "MatrixSlowHop" }
    val extraGroundBoost by floatValue("ExtraGroundBoost", 0.2f, 0f..0.5f)
    { mode.get() == "MatrixHop" || mode.get() == "MatrixSlowHop" }

    // HypixelLowHop Speed
    val glide by _boolean("Glide", true) { mode.get() == "HypixelLowHop" }

    // BlocksMCHop Speed
    val fullStrafe by _boolean("FullStrafe", true) { mode.get() == "BlocksMCHop" }
    val bmcLowHop = _boolean("LowHop", true) { mode.get() == "BlocksMCHop" }
    val bmcDamageBoost by _boolean("DamageBoost", true) { mode.get() == "BlocksMCHop" }
    val damageLowHop by _boolean("DamageLowHop", false) { mode.get() == "BlocksMCHop" }
    val safeY by _boolean("SafeY", true) { mode.get() == "BlocksMCHop" }
    val groundSpeed by _boolean("GroundSpeed", false) {mode.get() == "BlocksMCHop"}
    var playerOnGroundTicks = 0

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val thePlayer = mc.thePlayer ?: return

        if (thePlayer.isSneaking)
            return

        if (thePlayer.isMoving && !sprintManually)
            thePlayer.isSprinting = true

        modeModule.onUpdate()
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        val thePlayer = mc.thePlayer ?: return

        if (thePlayer.isSneaking || event.eventState != EventState.PRE)
            return

        if (thePlayer.isMoving && !sprintManually)
            thePlayer.isSprinting = true

        modeModule.onMotion()
    }
    @EventTarget
    fun onMove(event: MoveEvent) {
        if (mc.thePlayer?.isSneaking == true)
            return

        modeModule.onMove(event)
    }

    @EventTarget
    fun onTick(event: GameTickEvent) {
        modeModule.onTick()
    }

    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        if (mc.thePlayer?.isSneaking == true)
            return

        modeModule.onStrafe()
    }
    @EventTarget
    fun onPlayerTick(event: PlayerTickEvent) {
        if (mc.thePlayer?.isSneaking == true)
            return

        modeModule.onPlayerTick()
    }
    @EventTarget
    fun onJump(event: JumpEvent) {
        if (mc.thePlayer?.isSneaking == true)
            return

        modeModule.onJump(event)
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (mc.thePlayer?.isSneaking == true)
            return

        modeModule.onPacket(event)
    }

    override fun onEnable() {
        if (mc.thePlayer == null)
            return
        if (disableSameY){
            Scaffold.sameY.set(false)
        }
        mc.timer.timerSpeed = 1f

        modeModule.onEnable()
    }

    override fun onDisable() {
        if (mc.thePlayer == null)
            return

        mc.timer.timerSpeed = 1f
        mc.thePlayer.speedInAir = 0.02f
        if (disableSameY){
            Scaffold.sameY.set(true)
        }
        modeModule.onDisable()
    }

    override val tag
        get() = mode.get()

    private val modeModule
        get() = speedModes.find { it.modeName == mode.get() }!!

    private val sprintManually
        // Maybe there are more but for now there's the Legit mode.get().
        get() = modeModule in arrayOf(Legit)
}
