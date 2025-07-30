/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.player.Blink
import net.ccbluex.liquidbounce.features.module.modules.settings.Sounds.playKillSound
import net.ccbluex.liquidbounce.features.module.modules.world.Fucker
import net.ccbluex.liquidbounce.features.module.modules.world.Nuker
import net.ccbluex.liquidbounce.features.module.modules.world.scaffolds.Scaffold
import net.ccbluex.liquidbounce.features.module.modules.world.scaffolds.Tower
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.ClientUtils.runTimeTicks
import net.ccbluex.liquidbounce.utils.CooldownHelper.getAttackCooldownProgress
import net.ccbluex.liquidbounce.utils.CooldownHelper.resetLastAttackedTicks
import net.ccbluex.liquidbounce.utils.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPackets
import net.ccbluex.liquidbounce.utils.PacketUtils.type
import net.ccbluex.liquidbounce.utils.RaycastUtils.raycastEntity
import net.ccbluex.liquidbounce.utils.RotationUtils.currentRotation
import net.ccbluex.liquidbounce.utils.RotationUtils.getVectorForRotation
import net.ccbluex.liquidbounce.utils.RotationUtils.isRotationFaced
import net.ccbluex.liquidbounce.utils.RotationUtils.isVisible
import net.ccbluex.liquidbounce.utils.RotationUtils.rotationDifference
import net.ccbluex.liquidbounce.utils.RotationUtils.searchCenter
import net.ccbluex.liquidbounce.utils.RotationUtils.setTargetRotation
import net.ccbluex.liquidbounce.utils.RotationUtils.toRotation
import net.ccbluex.liquidbounce.utils.attack.EntityUtils
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.serverOpenInventory
import net.ccbluex.liquidbounce.utils.inventory.ItemUtils.isConsumingItem
import net.ccbluex.liquidbounce.utils.misc.RandomUtils.nextInt
import net.ccbluex.liquidbounce.utils.packet.sendOffHandUseItem.sendOffHandUseItem
import net.ccbluex.liquidbounce.utils.render.ColorSettingsInteger
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawEntityBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawPlatform
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawTargetCapsule
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawTextureOnEntity
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.utils.timing.TimeUtils.randomClickDelay
import net.ccbluex.liquidbounce.value.*
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemAxe
import net.minecraft.item.ItemSword
import net.minecraft.network.Packet
import net.minecraft.network.play.client.*
import net.minecraft.network.play.client.C02PacketUseEntity.Action.INTERACT
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.RELEASE_USE_ITEM
import net.minecraft.network.play.server.S45PacketTitle
import net.minecraft.potion.Potion
import net.minecraft.util.*
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.*

object KillAura : Module("KillAura", Category.COMBAT, hideModule = false) {
    /**
     * OPTIONS
     */

    // Cooldown
    private val simulateCooldown by _boolean("SimulateCooldown", false)
    private val simulateDoubleClicking by _boolean("SimulateDoubleClicking", false) { !simulateCooldown }

    // CPS - Attack speed
    private val maxCPSValue = object : IntegerValue("MaxCPS", 8, 1..20) {
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtLeast(minCPS)
        override fun onChanged(oldValue: Int, newValue: Int) {
            attackDelay = randomClickDelay(minCPS, newValue)
        }
        override fun isSupported() = !simulateCooldown
    }
    private val maxCPS by maxCPSValue

    private val minCPS: Int by object : IntegerValue("MinCPS", 5, 1..20) {
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtMost(maxCPS)
        override fun onChanged(oldValue: Int, newValue: Int) {
            attackDelay = randomClickDelay(newValue, maxCPS)
        }
        override fun isSupported() = !maxCPSValue.isMinimal() && !simulateCooldown
    }

    private val hurtTime by intValue("HurtTime", 10, 0..10) { !simulateCooldown }
    private val clickOnly by _boolean("ClickOnly", false)

    // Range
    val range: Float by object : FloatValue("Range", 3.7f, 1f..8f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            blockRange = blockRange.coerceAtMost(newValue)
        }
    }
    private val scanRange by floatValue("ScanRange", 2f, 0f..10f)
    private val throughWallsRange by floatValue("ThroughWallsRange", 3f, 0f..8f)
    private val rangeSprintReduction by floatValue("RangeSprintReduction", 0f, 0f..0.4f)

    // Modes
    val priority by choices(
        "Priority", arrayOf(
            "Health", "Distance", "Direction", "LivingTime", "Armor",
            "HurtResistance", "HurtTime", "HealthAbsorption", "RegenAmplifier",
            "OnLadder", "InLiquid", "InWeb"
        ), "Distance"
    )
    private val targetMode by choices("TargetMode", arrayOf("Single", "Switch", "Multi"), "Switch")
    private val limitedMultiTargets by intValue("LimitedMultiTargets", 0, 0..50) { targetMode == "Multi" }
    private val maxSwitchFOV by floatValue("MaxSwitchFOV", 90f, 30f..180f) { targetMode == "Switch" }
    private val viewingCheck by _boolean("ViewingCheck", true)

    // Delay
    private val switchDelay by intValue("SwitchDelay", 15, 1..1000) { targetMode == "Switch" }

    // Bypass
    private val swing by _boolean("Swing", true)
    private val keepSprint by _boolean("KeepSprint", true)

    // Settings
    private val autoF5 by _boolean("AutoF5", false, subjective = true)
    private val onScaffold by _boolean("OnScaffold", false)
    private val onDestroyBlock by _boolean("OnDestroyBlock", false)

    // AutoBlock
    val autoBlock by choices(
        "AutoBlock",
        arrayOf("Off", "Packet", "Fake", "QuickMarco", "BlocksMC", "HypixelFull", "NCP"),
        "Packet"
    )

    private val maxBlinkPackets by intValue("MaxBlinkPackets", 20, 5..100) { autoBlock == "HypixelFull" }
    private val blockMaxRange by floatValue("BlockMaxRange", 3f, 0f..8f) { autoBlock in listOf("Packet", "QuickMarco") }
    private val unblockMode by choices("UnblockMode", arrayOf("Stop", "Switch", "Empty"), "Stop") {
        autoBlock in listOf(
            "Packet",
            "QuickMarco"
        )
    }
    private val releaseAutoBlock by _boolean("ReleaseAutoBlock", true) {
        autoBlock !in listOf(
            "Off",
            "Fake",
            "BlocksMC",
            "HypixelFull"
        )
    }
    val forceBlockRender by _boolean("ForceBlockRender", true) {
        autoBlock !in listOf(
            "Off",
            "Fake",
            "BlocksMC",
            "HypixelFull"
        ) && releaseAutoBlock
    }
    private val ignoreTickRule by _boolean("IgnoreTickRule", false) {
        autoBlock !in listOf(
            "Off",
            "Fake",
            "BlocksMC",
            "HypixelFull"
        ) && releaseAutoBlock
    }
    private val blockRate by intValue("BlockRate", 100, 1..100) {
        autoBlock !in listOf(
            "Off",
            "Fake",
            "BlocksMC",
            "HypixelFull"
        ) && releaseAutoBlock
    }
    private val switchStartBlock by _boolean("SwitchStartBlock", false) {
        autoBlock !in listOf(
            "Off",
            "Fake",
            "BlocksMC",
            "HypixelFull"
        )
    }
    private val interactAutoBlock by _boolean("InteractAutoBlock", true) {
        autoBlock !in listOf(
            "Off",
            "Fake",
            "BlocksMC",
            "HypixelFull"
        )
    }
    val blinkAutoBlock by _boolean("BlinkAutoBlock", false) {
        autoBlock !in listOf(
            "Off",
            "Fake",
            "BlocksMC",
            "HypixelFull"
        )
    }

    private val blinkBlockTicks by intValue("BlinkBlockTicks", 3, 2..5) {
        autoBlock !in listOf(
            "Off",
            "Fake",
            "BlocksMC",
            "HypixelFull"
        ) && blinkAutoBlock
    }

    // AutoBlock conditions
    private val smartAutoBlock by _boolean("SmartAutoBlock", false) { autoBlock == "Packet" }
    private val forceBlock by _boolean("ForceBlockWhenStill", true) { smartAutoBlock }
    private val checkWeapon by _boolean("CheckEnemyWeapon", true) { smartAutoBlock }
    private var blockRange by object : FloatValue("BlockRange", range, 1f..8f) {
        override fun isSupported() = smartAutoBlock
        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtMost(this@KillAura.range)
    }
    private val maxOwnHurtTime by intValue("MaxOwnHurtTime", 3, 0..10) { smartAutoBlock }
    private val maxDirectionDiff by floatValue("MaxOpponentDirectionDiff", 60f, 30f..180f) { smartAutoBlock }
    private val maxSwingProgress by intValue("MaxOpponentSwingProgress", 1, 0..5) { smartAutoBlock }

    // Rotations
    val options = RotationSettings(this).withoutKeepRotation()

    // Raycast
    private val raycastValue = _boolean("RayCast", true) { options.rotationsActive }
    private val raycast by raycastValue
    private val raycastIgnored by _boolean(
        "RayCastIgnored",
        false
    ) { raycastValue.isActive() && options.rotationsActive }
    private val livingRaycast by _boolean("LivingRayCast", true) { raycastValue.isActive() && options.rotationsActive }

    // Hit delay
    private val useHitDelay by _boolean("UseHitDelay", false)
    private val hitDelayTicks by intValue("HitDelayTicks", 1, 1..5) { useHitDelay }

    private val randomization = RandomizationSettings(this) { options.rotationsActive }
    private val outborder by _boolean("Outborder", false) { options.rotationsActive }

    private val highestBodyPointToTargetValue: ListValue =
        object : ListValue("HighestBodyPointToTarget", arrayOf("Head", "Body", "Feet"), "Head") {
            override fun isSupported() = options.rotationsActive
            override fun onChange(oldValue: String, newValue: String): String {
                val newPoint = RotationUtils.BodyPoint.fromString(newValue)
                val lowestPoint = RotationUtils.BodyPoint.fromString(lowestBodyPointToTarget)
                return RotationUtils.coerceBodyPoint(newPoint, lowestPoint, RotationUtils.BodyPoint.HEAD).name
            }
        }
    private val highestBodyPointToTarget by highestBodyPointToTargetValue
    private val lowestBodyPointToTargetValue: ListValue =
        object : ListValue("LowestBodyPointToTarget", arrayOf("Head", "Body", "Feet"), "Feet") {
            override fun isSupported() = options.rotationsActive
            override fun onChange(oldValue: String, newValue: String): String {
                val newPoint = RotationUtils.BodyPoint.fromString(newValue)
                val highestPoint = RotationUtils.BodyPoint.fromString(highestBodyPointToTarget)
                return RotationUtils.coerceBodyPoint(newPoint, RotationUtils.BodyPoint.FEET, highestPoint).name
            }
        }
    private val lowestBodyPointToTarget by lowestBodyPointToTargetValue

    private val maxHorizontalBodySearch: FloatValue = object : FloatValue("MaxHorizontalBodySearch", 1f, 0f..1f) {
        override fun isSupported() = options.rotationsActive
        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtLeast(minHorizontalBodySearch.get())
    }
    private val minHorizontalBodySearch: FloatValue = object : FloatValue("MinHorizontalBodySearch", 0f, 0f..1f) {
        override fun isSupported() = options.rotationsActive
        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtMost(maxHorizontalBodySearch.get())
    }
    private val fov by floatValue("FOV", 180f, 0f..180f)

    // Prediction
    private val predictClientMovement by intValue("PredictClientMovement", 2, 0..5)
    private val predictOnlyWhenOutOfRange by _boolean("PredictOnlyWhenOutOfRange", false) { predictClientMovement != 0 }
    private val predictEnemyPosition by floatValue("PredictEnemyPosition", 1.5f, -1f..2f)

    // Extra swing
    private val failSwing by _boolean("FailSwing", true) { swing && options.rotationsActive }
    private val maxRotationDifferenceToSwing by floatValue(
        "MaxRotationDifferenceToSwing",
        180f,
        0f..180f
    ) { swing && failSwing && options.rotationsActive }
    private val swingWhenTicksLate = object : BoolValue("SwingWhenTicksLate", false) {
        override fun isSupported() =
            swing && failSwing && maxRotationDifferenceToSwing != 180f && options.rotationsActive
    }
    private val renderBoxOnSwingFail by _boolean("RenderBoxOnSwingFail", false) { failSwing }
    private val renderBoxColor = ColorSettingsInteger(this, "RenderBoxColor") { renderBoxOnSwingFail }.with(0, 255, 255)
    private val renderBoxFadeSeconds by floatValue("RenderBoxFadeSeconds", 1f, 0f..5f) { renderBoxOnSwingFail }

    // Inventory
    private val simulateClosingInventory by _boolean("SimulateClosingInventory", false) { !noInventoryAttack }
    private val noInventoryAttack by _boolean("NoInvAttack", false)
    private val noInventoryDelay by intValue("NoInvDelay", 200, 0..500) { noInventoryAttack }
    private val noConsumeAttack by choices(
        "NoConsumeAttack",
        arrayOf("Off", "NoHits", "NoRotation"),
        "Off",
        subjective = true
    )

    // Visuals
    private val mark by choices("Mark", arrayOf("None", "Platform", "Box"), "Platform", subjective = true)
    private val boxOutline by _boolean("Outline", true, subjective = true) { mark == "Box" }
    private val fakeSharp by _boolean("FakeSharp", true, subjective = true)
    private val renderMode by ListValue("RenderEffect", arrayOf("Capsule", "Nursultan"), "Capsule")
    private val fadeSpeed = FloatValue("FadeSpeed", 0.1f, 0.01f..0.2f) { renderMode == "Capsule" }
    private val circle by BoolValue("Circle", false)
    private val circleAccuracy by IntegerValue("Accuracy", 59, 0..59) { circle }
    private val circleThickness by FloatValue("Thickness", 2f, 0f..20f) { circle }
    private val circleRed by IntegerValue("Red", 255, 0..255) { circle }
    private val circleGreen by IntegerValue("Green", 255, 0..255) { circle }
    private val circleBlue by IntegerValue("Blue", 255, 0..255) { circle }
    private val circleAlpha by IntegerValue("Alpha", 255, 0..255) { circle }

    /**
     * MODULE STATE
     */
    // Target
    var target: EntityLivingBase? = null
    private var hittable = false
    private val prevTargetEntities = mutableListOf<Int>()

    // Attack
    private val attackTimer = MSTimer()
    private var attackDelay = 0
    private var clicks = 0
    private var attackTickTimes = mutableListOf<Pair<MovingObjectPosition, Int>>()
    var attack = 0 // Used by BlocksMC mode

    // AutoBlock
    var renderBlocking = false
    var blockStatus = false
    var slotChangeAutoBlock = false

    // AutoBlock (BlocksMC mode)
    private var asw = 0 // BlocksMC state
    private var blockTick: Int = 0

    // AutoBlock (HypixelFull mode)
    private var hypixelBlinking = false

    // AutoBlock (Blink mode)
    var blinking: Boolean = false
    val blinkedPackets: ArrayList<Packet<*>?> = ArrayList()
    private var blinked = false

    // Switch Delay
    private val switchTimer = MSTimer()

    // Container Delay
    private var containerOpen = -1L

    // Visuals
    private val swingFails = mutableListOf<SwingFailData>()
    private var lastHealth = 0f
    private var currentRed = 255
    private var currentGreen = 255
    private var currentBlue = 255

    /**
     * CORE FUNCTIONS
     */

    override fun onEnable() {
        CombatListener.handleEvents()
    }

    override fun onDisable() {
        reset()
        if (autoF5) {
            mc.gameSettings.thirdPersonView = 0
        }
        blinkedPackets.clear()
    }
    /**
     * Checks if the current autoblock mode is set to "Fake"
     * Used to prevent slowdown when using fake blocking
     */
    fun isFakeBlocking(): Boolean {
        return autoBlock == "Fake"
    }
    private fun reset() {
        target = null
        hittable = false
        prevTargetEntities.clear()
        attackTickTimes.clear()
        attackTimer.reset()
        clicks = 0

        if (autoBlock == "BlocksMC" && blockStatus) {
            sendPacket(C09PacketHeldItemChange((mc.thePlayer.inventory.currentItem + 2) % 8))
            sendPacket(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
        }

        // Reset all mode-specific states
        asw = 0
        blockTick = 0
        attack = 0

        stopBlocking(true)

        if (hypixelBlinking) {
            BlinkUtils.unblink()
            hypixelBlinking = false
        }
        if (blinkAutoBlock && blinked) {
            BlinkUtils.unblink()
            blinked = false
        }
        blinking = false
        blinkedPackets.clear()

        synchronized(swingFails) {
            swingFails.clear()
        }
    }

    @EventTarget
    fun onRotationUpdate(event: RotationUpdateEvent) {
        if (cancelRun || (noInventoryAttack && (mc.currentScreen is GuiContainer || System.currentTimeMillis() - containerOpen < noInventoryDelay))) return

        // Update target and rotations
        updateTarget()

        if (autoF5 && target != null && mc.gameSettings.thirdPersonView != 1) {
            mc.gameSettings.thirdPersonView = 1
        }
    }

    @EventTarget
    fun onWorldChange(event: WorldEvent) {
        reset()
    }

    @EventTarget
    fun onTick(event: GameTickEvent) {
        val player = mc.thePlayer ?: return

        target?.let { if (it.isDead) EventManager.callEvent(EntityKilledEvent(it)) }

        if (shouldPrioritize()) {
            target = null
            renderBlocking = false
            return
        }

        if (cancelRun) {
            target = null
            hittable = false
            stopBlocking()
            return
        }

        if (noInventoryAttack && mc.currentScreen is GuiContainer) {
            containerOpen = System.currentTimeMillis()
        }

        if (noInventoryAttack && (mc.currentScreen is GuiContainer || System.currentTimeMillis() - containerOpen < noInventoryDelay)) {
            target = null
            hittable = false
            return
        }

        if (target == null) {
            if (blockStatus) {
                stopBlocking()
            }
            return
        }

        // Handle special autoblock modes which have their own attack/block timing
        when (autoBlock) {
            "BlocksMC" -> {
                handleBlocksMC()
                return // Prevent default attack logic from running
            }
        }

        if (clickOnly && !mc.gameSettings.keyBindAttack.isKeyDown) {
            return
        }

        if (blockStatus && autoBlock == "Packet" && releaseAutoBlock && !ignoreTickRule) {
            clicks = 0
            stopBlocking()
            return
        }

        if (simulateCooldown && getAttackCooldownProgress() < 1f) {
            return
        }

        // Handle generic blink autoblock timing
        if (blinkAutoBlock) {
            when (player.ticksExisted % (blinkBlockTicks + 1)) {
                0 -> if (blockStatus && !blinked && !BlinkUtils.isBlinking) blinked = true
                1 -> if (blockStatus && blinked && BlinkUtils.isBlinking) stopBlocking()
                blinkBlockTicks -> {
                    if (!blockStatus && blinked && BlinkUtils.isBlinking) {
                        BlinkUtils.unblink()
                        blinked = false
                        startBlocking(target!!, interactAutoBlock, autoBlock == "Fake")
                    }
                }
            }
        }

        if (player.getDistanceToEntityBox(target!!) > blockMaxRange && blockStatus) {
            stopBlocking(true)
            return
        } else if (autoBlock != "Off" && !releaseAutoBlock) {
            renderBlocking = true
        }

        // Standard attack timing
        val extraClicks = if (simulateDoubleClicking && !simulateCooldown) nextInt(-1, 1) else 0
        val maxClicks = clicks + extraClicks

        repeat(maxClicks) {
            val wasBlocking = blockStatus
            runAttack(it == 0, it + 1 == maxClicks)
            clicks--

            if (wasBlocking && !blockStatus && (releaseAutoBlock && !ignoreTickRule || autoBlock == "Off")) {
                return
            }
        }
    }

    /**
     * Run attack logic for the current target(s)
     */
    private fun runAttack(isFirstClick: Boolean, isLastClick: Boolean) {
        var currentTarget = this.target ?: return

        if (viewingCheck && !net.ccbluex.liquidbounce.utils.EntityUtils.isLookingAtEntity(
                mc.thePlayer,
                currentTarget,
                RotationUtils.serverRotation.yaw,
                RotationUtils.serverRotation.pitch,
                range.toDouble()
            )
        ) {
            return
        }

        if (noConsumeAttack == "NoHits" && isConsumingItem()) {
            return
        }

        updateHittable()
        currentTarget = this.target ?: return // Target might change after hittable update (raycastIgnored)

        if (!hittable || currentTarget.hurtTime > hurtTime) {
            return
        }

        val manipulateInventory = simulateClosingInventory && !noInventoryAttack && serverOpenInventory
        if (manipulateInventory && isFirstClick) serverOpenInventory = false

        if (targetMode != "Multi") {
            attackEntity(currentTarget, isLastClick)
        } else {
            var targets = 0
            for (entity in mc.theWorld.loadedEntityList) {
                if (entity is EntityLivingBase && isEnemy(entity) && mc.thePlayer.getDistanceToEntityBox(entity) <= getRange(
                        entity
                    )
                ) {
                    attackEntity(entity, isLastClick)
                    targets++
                    if (limitedMultiTargets != 0 && limitedMultiTargets <= targets) break
                }
            }
        }

        if (!isLastClick) return

        val switchMode = targetMode == "Switch"
        if (!switchMode || switchTimer.hasTimePassed(switchDelay)) {
            prevTargetEntities += currentTarget.entityId
            if (switchMode) {
                switchTimer.reset()
            }
        }

        if (manipulateInventory) serverOpenInventory = true
    }

    /**
     * Perform the attack on a single entity
     */
    private fun attackEntity(entity: EntityLivingBase, isLastClick: Boolean) {
        val thePlayer = mc.thePlayer ?: return

        if (shouldPrioritize()) return

        // Unblock before attacking if necessary
        if (thePlayer.isBlocking && (autoBlock == "Off" && blockStatus || autoBlock == "Packet" && releaseAutoBlock)) {
            stopBlocking()
            if (!ignoreTickRule || autoBlock == "Off") return
        }

        if (shouldDelayClick(MovingObjectPosition.MovingObjectType.ENTITY)) return

        if (!blinkAutoBlock || !BlinkUtils.isBlinking) {
            // Determine if sprint should be affected. `false` means stop sprinting.
            val affectSprint = !(KeepSprint.handleEvents() || keepSprint)
            thePlayer.attackEntityWithModifiedSprint(
                entity,
                if (affectSprint) false else null
            ) { if (swing) thePlayer.swingItem() }

            if (EnchantmentHelper.getModifierForCreature(
                    thePlayer.heldItem,
                    entity.creatureAttribute
                ) <= 0F && fakeSharp
            ) {
                thePlayer.onEnchantmentCritical(entity)
            }
        }

        // Start blocking after attack
        if (autoBlock != "Off" && (thePlayer.isBlocking || canBlock)) {
            if (!blinkAutoBlock && isLastClick || blinkAutoBlock && (!blinked || !BlinkUtils.isBlinking)) {
                startBlocking(entity, interactAutoBlock, autoBlock == "Fake")
            }
        }

        if (autoBlock != "Off" && slotChangeAutoBlock && (!blinked || !BlinkUtils.isBlinking)) {
            when (autoBlock) {
                "QuickMarco" -> sendOffHandUseItem()
                "Packet" -> sendPacket(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
            }
            slotChangeAutoBlock = false
        }

        resetLastAttackedTicks()
    }

    /**
     * TARGETING AND ROTATIONS
     */
    private fun updateTarget() {
        if (shouldPrioritize()) return

        target = null

        val switchMode = targetMode == "Switch"
        val theWorld = mc.theWorld ?: return
        val thePlayer = mc.thePlayer ?: return
        val entities = theWorld.loadedEntityList

        var bestTarget: EntityLivingBase? = null
        var bestValue: Double? = null

        for (entity in entities) {
            if (entity !is EntityLivingBase || !EntityUtils.isSelected(
                    entity,
                    true
                ) || (switchMode && entity.entityId in prevTargetEntities)
            ) continue

            var distance = 0.0
            Backtrack.runWithNearestTrackedDistance(entity) { distance = thePlayer.getDistanceToEntityBox(entity) }
            if (switchMode && distance > range && prevTargetEntities.isNotEmpty()) continue

            val entityFov = rotationDifference(entity)
            if (distance > maxRange || (fov != 180F && entityFov > fov)) continue

            if (switchMode && !EntityUtils.isLookingOnEntities(entity, maxSwitchFOV.toDouble())) continue

            val currentValue = when (priority.lowercase()) {
                "distance" -> distance
                "direction" -> entityFov.toDouble()
                "health" -> entity.health.toDouble()
                "livingtime" -> -entity.ticksExisted.toDouble()
                "armor" -> entity.totalArmorValue.toDouble()
                "hurtresistance" -> entity.hurtResistantTime.toDouble()
                "hurttime" -> entity.hurtTime.toDouble()
                "healthabsorption" -> (entity.health + entity.absorptionAmount).toDouble()
                // BUG FIX: Correctly prioritize higher regen. Negative value makes lower (more negative) better.
                "regenamplifier" -> -(if (entity.isPotionActive(Potion.regeneration)) {
                    (entity.getActivePotionEffect(Potion.regeneration).amplifier + 1).toDouble()
                } else 0.0)
                "inweb" -> if (entity.isInWeb) -1.0 else Double.MAX_VALUE
                "onladder" -> if (entity.isOnLadder) -1.0 else Double.MAX_VALUE
                "inliquid" -> if (entity.isInWater || entity.isInLava) -1.0 else Double.MAX_VALUE
                else -> continue
            }

            if (bestValue == null || currentValue < bestValue) {
                bestValue = currentValue
                bestTarget = entity
            }
        }

        if (bestTarget != null) {
            var success = false
            Backtrack.runWithNearestTrackedDistance(bestTarget) { success = updateRotations(bestTarget) }
            if (success) {
                target = bestTarget
                return
            }
        }

        if (prevTargetEntities.isNotEmpty()) {
            prevTargetEntities.clear()
            updateTarget()
        }
    }

    private fun updateRotations(entity: Entity): Boolean {
        if (shouldPrioritize() || !options.rotationsActive) {
            // For non-rotation mode, still need to check range.
            hittable = mc.thePlayer.getDistanceToEntityBox(entity) <= range
            return hittable
        }

        val player = mc.thePlayer ?: return false
        val (predictX, predictY, predictZ) = entity.currPos.subtract(entity.prevPos)
            .times(2 + predictEnemyPosition.toDouble())
        val boundingBox = entity.hitBox.offset(predictX, predictY, predictZ)
        val (currPos, oldPos) = player.currPos to player.prevPos
        val simPlayer = SimulatedPlayer.fromClientPlayer(player.movementInput)
        var pos = currPos

        (0..predictClientMovement + 1).forEach {
            val previousPos = simPlayer.pos
            simPlayer.tick()
            if (predictOnlyWhenOutOfRange) {
                player.setPosAndPrevPos(simPlayer.pos)
                val currDist = player.getDistanceToEntityBox(entity)
                player.setPosAndPrevPos(previousPos)
                val prevDist = player.getDistanceToEntityBox(entity)
                player.setPosAndPrevPos(currPos, oldPos)
                pos = simPlayer.pos
                if (currDist <= range && currDist <= prevDist) return@forEach
            }
            pos = previousPos
        }

        player.setPosAndPrevPos(pos)
        val rotation = searchCenter(
            boundingBox, outborder && !attackTimer.hasTimePassed(attackDelay / 2),
            randomization, predict = false, lookRange = range + scanRange, attackRange = range,
            throughWallsRange = throughWallsRange,
            bodyPoints = listOf(highestBodyPointToTarget, lowestBodyPointToTarget),
            horizontalSearch = minHorizontalBodySearch.get()..maxHorizontalBodySearch.get(), options
        )
        player.setPosAndPrevPos(currPos, oldPos)

        if (rotation != null) {
            setTargetRotation(rotation, options = options)
            return true
        }
        return false
    }

    private fun updateHittable() {
        val currentTarget = target ?: return
        if (shouldPrioritize()) return

        if (!options.rotationsActive) {
            hittable = mc.thePlayer.getDistanceToEntityBox(currentTarget) <= range
            return
        }

        val eyes = mc.thePlayer.eyes
        val currentRotation = currentRotation ?: mc.thePlayer.rotation
        var chosenEntity: Entity? = null

        if (raycast) {
            chosenEntity = raycastEntity(range.toDouble(), currentRotation.yaw, currentRotation.pitch) { entity ->
                !livingRaycast || (entity is EntityLivingBase && entity !is EntityArmorStand)
            }
            if (chosenEntity is EntityLivingBase && (NoFriends.handleEvents() || !(chosenEntity is EntityPlayer && chosenEntity.isClientFriend()))) {
                if (raycastIgnored && target != chosenEntity) {
                    target = chosenEntity
                }
            }
            hittable = this.target == chosenEntity
        } else {
            hittable = isRotationFaced(currentTarget, range.toDouble(), currentRotation)
        }

        var shouldExcept = false

        chosenEntity ?: this.target?.run {
            if (ForwardTrack.handleEvents()) {
                ForwardTrack.includeEntityTruePos(this) {
                    checkIfAimingAtBox(this, currentRotation, eyes, onSuccess = {
                        hittable = true
                        shouldExcept = true
                    })
                }
            }
        }

        if (hittable && shouldExcept) {
            return
        }

        val targetToCheck = chosenEntity ?: this.target ?: return

        // If player is inside entity, it's always hittable
        if (targetToCheck.hitBox.isVecInside(eyes)) {
            hittable = true
            return
        }

        var checkNormally = true

        if (Backtrack.handleEvents()) {
            Backtrack.loopThroughBacktrackData(targetToCheck) {
                var result = false
                checkIfAimingAtBox(targetToCheck, currentRotation, eyes, onSuccess = {
                    checkNormally = false
                    result = true
                }, onFail = {
                    result = false
                })
                return@loopThroughBacktrackData result
            }
        } else if (ForwardTrack.handleEvents()) {
            ForwardTrack.includeEntityTruePos(targetToCheck) {
                checkIfAimingAtBox(targetToCheck, currentRotation, eyes, onSuccess = { checkNormally = false })
            }
        }

        if (!checkNormally) {
            return
        }

        // Recreate raycast logic for final check
        val intercept = targetToCheck.hitBox.calculateIntercept(
            eyes,
            eyes + getVectorForRotation(currentRotation) * range.toDouble()
        )
        hittable =
            intercept != null && (isVisible(intercept.hitVec) || mc.thePlayer.getDistanceToEntityBox(targetToCheck) <= throughWallsRange)
    }

    private fun checkIfAimingAtBox(
        targetToCheck: Entity, currentRotation: Rotation, eyes: Vec3, onSuccess: () -> Unit,
        onFail: () -> Unit = { },
    ) {
        if (targetToCheck.hitBox.isVecInside(eyes)) {
            onSuccess()
            return
        }

        val intercept = targetToCheck.hitBox.calculateIntercept(
            eyes,
            eyes + getVectorForRotation(currentRotation) * range.toDouble()
        )

        if (intercept != null) {
            val isHittable =
                isVisible(intercept.hitVec) || mc.thePlayer.getDistanceToEntityBox(targetToCheck) <= throughWallsRange
            if (isHittable) {
                onSuccess()
                return
            }
        }
        onFail()
    }

    /**
     * AUTOBLOCK AND RELATED LOGIC
     */

    private fun startBlocking(interactEntity: Entity, interact: Boolean, fake: Boolean = false) {
        if (blockStatus || shouldPrioritize()) return

        if (mc.thePlayer.isBlocking) {
            blockStatus = true
            renderBlocking = true
            return
        }

        if (!fake) {
            if (!(blockRate > 0 && nextInt(endExclusive = 100) < blockRate)) return

            if (interact) {
                val positionEye = mc.thePlayer.eyes
                val (yaw, pitch) = currentRotation ?: mc.thePlayer.rotation
                val vec = getVectorForRotation(Rotation(yaw, pitch))
                val lookAt = positionEye.add(vec * maxRange.toDouble())
                val movingObject = interactEntity.hitBox.calculateIntercept(positionEye, lookAt)
                if (movingObject != null) {
                    sendPackets(
                        C02PacketUseEntity(interactEntity, movingObject.hitVec - interactEntity.positionVector),
                        C02PacketUseEntity(interactEntity, INTERACT)
                    )
                }
            }

            if (switchStartBlock) {
                switchToSlot((SilentHotbar.currentSlot + 1) % 9)
            }
            sendPacket(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
        }

        blockStatus = true
        renderBlocking = true
        CPSCounter.registerClick(CPSCounter.MouseButton.RIGHT)
    }

    private fun stopBlocking(forceStop: Boolean = false) {
        if (autoBlock == "HypixelFull" && (hypixelBlinking || BlinkUtils.isBlinking)) {
            BlinkUtils.unblink()
            hypixelBlinking = false
            sendPacket(C07PacketPlayerDigging(RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
        }

        if (blockStatus && (!mc.thePlayer.isBlocking || forceStop)) {
            if (autoBlock == "NCP" || autoBlock.startsWith("BlocksMC") || forceStop || unblockMode.equals(
                    "stop",
                    true
                )
            ) {
                sendPacket(C07PacketPlayerDigging(RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
            } else if (!forceStop) {
                when (unblockMode.lowercase()) {
                    "switch" -> switchToSlot((SilentHotbar.currentSlot + 1) % 9)
                    "empty" -> switchToSlot(mc.thePlayer.inventory.firstEmptyStack)
                }
            }
            blockStatus = false
        }
        renderBlocking = false
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (blinking && target != null && packet.type == PacketUtils.PacketType.CLIENT) {
            blinkedPackets.add(packet)
            event.cancelEvent()
        }

        if (autoBlock == "HypixelFull" && !hypixelBlinking) {
            if (BlinkUtils.isProcessing) return
            BlinkUtils.isProcessing = true
            try {
                if (BlinkUtils.queuedPacketsCount < maxBlinkPackets) {
                    BlinkUtils.blink(packet, event, true, false)
                    sendPacket(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
                    hypixelBlinking = true
                } else {
                    BlinkUtils.unblink()
                    hypixelBlinking = false
                }
            } finally {
                BlinkUtils.isProcessing = false
            }
        }

        if (autoBlock == "Off" || !blinkAutoBlock || !blinked || Blink.blinkingSend() || Blink.blinkingReceive() || mc.thePlayer.isDead || mc.thePlayer.ticksExisted < 20) {
            if (BlinkUtils.isBlinking) BlinkUtils.unblink()
            return
        }
        BlinkUtils.blink(packet, event)
    }

    /**
     * SPECIAL AUTOBOCK MODES
     */

    // BlocksMC Mode: A complex state machine for attacking and blocking.
    private fun handleBlocksMC() {
        val player = mc.thePlayer ?: return
        val currentTarget = target ?: return

        asw++
        when (asw) {
            1 -> {
                val attackRangeCheck = player.getDistanceToEntityBox(currentTarget) <= (if (blinking) range else 3.0F)

                if (attackRangeCheck && hittable) {
                    attackEntityDirectly(currentTarget)
                    attack++
                } else {
                    attack = 0
                    player.swingItem()
                }

                sendPacket(C08PacketPlayerBlockPlacement(player.heldItem))
                blockStatus = true
                renderBlocking = true

                blinking = false
                releaseBlinkedPackets()
            }
            2 -> {
                if (attack % 3 == 0) {
                    blinking = true
                    sendPacket(C09PacketHeldItemChange((player.inventory.currentItem + 2) % 8))
                    sendPacket(C09PacketHeldItemChange(player.inventory.currentItem))
                    sendPacket(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, player.heldItem, 0f, 0f, 0f))
                    asw = 0
                } else if (attack % 6 == 1) {
                    blinking = true
                    sendPacket(C07PacketPlayerDigging(RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
                    blockStatus = false
                    renderBlocking = false
                    asw = 0
                }
            }
            3 -> {
                blinking = true
                sendPacket(C07PacketPlayerDigging(RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
                blockStatus = false
                renderBlocking = false
                asw = 0
            }
        }
    }

    private fun attackEntityDirectly(entity: EntityLivingBase, interact: Boolean = true) {
        mc.thePlayer.swingItem()
        mc.playerController.attackEntity(mc.thePlayer, entity)
        if (interact) {
            sendPacket(C02PacketUseEntity(entity, INTERACT))
        }
        resetLastAttackedTicks()
    }

    private fun releaseBlinkedPackets() {
        if (blinkedPackets.isNotEmpty()) {
            val packetsToRelease = blinkedPackets.toList()
            blinkedPackets.clear()
            packetsToRelease.forEach { packet ->
                packet?.let { sendPacket(it) }
            }
        }
        blinking = false
    }

    /**
     * RENDER AND VISUALS
     */
    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (cancelRun) {
            target = null
            hittable = false
            return
        }

        val currentTarget = target

        if (renderMode == "Capsule" && currentTarget != null) {
            val color = Color(currentRed, currentGreen, currentBlue, 255)
            drawTargetCapsule(currentTarget, 0.5, true, color)
        } else if(renderMode == "Nursultan" && currentTarget != null) {
            drawTextureOnEntity(-24, -24, 48, 48, 48F, 48F, currentTarget, ResourceLocation("liquidbounce/target.png"), true, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE);
        }

        if (circle) {
            drawRangeCircle()
        }

        handleFailedSwings()

        if (noInventoryAttack && (mc.currentScreen is GuiContainer || System.currentTimeMillis() - containerOpen < noInventoryDelay)) {
            target = null
            hittable = false
            if (mc.currentScreen is GuiContainer) containerOpen = System.currentTimeMillis()
            return
        }

        if (currentTarget == null) return

        if (attackTimer.hasTimePassed(attackDelay)) {
            if (maxCPS > 0) clicks++
            attackTimer.reset()
            attackDelay = randomClickDelay(minCPS, maxCPS)
        }

        if (targetMode != "Multi") {
            val color = if (hittable) Color(37, 126, 255, 70) else Color(255, 0, 0, 70)
            when (mark.lowercase()) {
                "platform" -> drawPlatform(currentTarget, color)
                "box" -> drawEntityBox(currentTarget, color, boxOutline)
            }
        }
    }

    private fun drawRangeCircle() {
        GL11.glPushMatrix()
        GL11.glTranslated(
            mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * mc.timer.renderPartialTicks - mc.renderManager.renderPosX,
            mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * mc.timer.renderPartialTicks - mc.renderManager.renderPosY,
            mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * mc.timer.renderPartialTicks - mc.renderManager.renderPosZ
        )
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        GL11.glLineWidth(circleThickness)
        GL11.glColor4f(circleRed / 255.0F, circleGreen / 255.0F, circleBlue / 255.0F, circleAlpha / 255.0F)
        GL11.glRotatef(90F, 1F, 0F, 0F)
        GL11.glBegin(GL11.GL_LINE_STRIP)

        for (i in 0..360 step (60 - circleAccuracy).coerceAtLeast(1)) {
            GL11.glVertex2f(cos(i * PI / 180.0).toFloat() * range, sin(i * PI / 180.0).toFloat() * range)
        }

        GL11.glEnd()
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GL11.glPopMatrix()
    }

    private fun handleFailedSwings() {
        if (!renderBoxOnSwingFail)
            return

        val box = AxisAlignedBB(0.0, 0.0, 0.0, 0.05, 0.05, 0.05)

        synchronized(swingFails) {
            val fadeSeconds = renderBoxFadeSeconds * 1000L
            val colorSettings = renderBoxColor
            val renderManager = mc.renderManager

            swingFails.removeAll {
                val timestamp = System.currentTimeMillis() - it.startTime
                val transparency = (0f..255f).lerpWith(1 - (timestamp / fadeSeconds).coerceAtMost(1.0F))

                val (posX, posY, posZ) = it.vec3
                val (x, y, z) = it.vec3 - renderManager.renderPos
                val offsetBox = box.offset(posX, posY, posZ).offset(-posX, -posY, -posZ).offset(x, y, z)

                RenderUtils.drawAxisAlignedBB(offsetBox, colorSettings.color(a = transparency.roundToInt()))

                timestamp > fadeSeconds
            }
        }
    }

    /**
     * UTILITY AND HELPER FUNCTIONS
     */
    private val cancelRun: Boolean
        get() = mc.thePlayer.isSpectator || !mc.thePlayer.isEntityAlive || (noConsumeAttack == "NoRotation" && isConsumingItem())

    private fun isEnemy(entity: Entity?): Boolean = isSelected(entity, true)

    private val canBlock: Boolean
        get() {
            val player = mc.thePlayer ?: return false
            val currentTarget = target ?: return false

            if (autoBlock.startsWith("BlocksMC")) return true

            if (player.heldItem?.item is ItemSword) {
                if (smartAutoBlock) {
                    if (!player.isMoving && forceBlock) return true
                    if (checkWeapon && currentTarget.heldItem?.item !is ItemSword && currentTarget.heldItem?.item !is ItemAxe) return false
                    if (player.hurtTime > maxOwnHurtTime) return false

                    val rotationToPlayer = toRotation(player.hitBox.center, true, currentTarget)
                    if (rotationDifference(rotationToPlayer, currentTarget.rotation) > maxDirectionDiff) return false

                    if (currentTarget.swingProgressInt > maxSwingProgress) return false
                    if (currentTarget.getDistanceToEntityBox(player) > blockRange) return false
                }
                return player.getDistanceToEntityBox(currentTarget) <= blockMaxRange
            }
            return false
        }

    private val maxRange: Float
        get() = max(range + scanRange, throughWallsRange)

    private fun getRange(entity: Entity): Float =
        (if (mc.thePlayer.getDistanceToEntityBox(entity) >= throughWallsRange) range + scanRange else throughWallsRange) - if (mc.thePlayer.isSprinting) rangeSprintReduction else 0F

    private fun shouldDelayClick(currentType: MovingObjectPosition.MovingObjectType): Boolean {
        if (!useHitDelay) {
            return false
        }
        val lastAttack = attackTickTimes.lastOrNull()
        return lastAttack != null && lastAttack.first.typeOfHit != currentType && runTimeTicks - lastAttack.second <= hitDelayTicks
    }

    private fun switchToSlot(slot: Int) {
        if (slot == -1) return
        SilentHotbar.selectSlotSilently(this, slot, immediate = true)
        SilentHotbar.resetSlot(this, true)
    }

    private fun shouldPrioritize(): Boolean =
        (!onScaffold && (Scaffold.handleEvents() && (Scaffold.placeRotation != null || currentRotation != null) || Tower.handleEvents() && Tower.isTowering)) ||
                (!onDestroyBlock && (Fucker.handleEvents() && !Fucker.noHit && Fucker.pos != null || Nuker.handleEvents()))

    private fun isTargetInRange(target: EntityLivingBase, customRange: Double): Boolean =
        mc.thePlayer.getDistanceToEntityBox(target) <= customRange

    override val tag: String
        get() = "$targetMode->$autoBlock"

    /**
     * Listener for kill tracking and visual feedback.
     */
    object CombatListener : Listenable {
        var killCounts = 0
        var win = 0
        private var totalPlayed = 0
        private var syncEntity: EntityLivingBase? = null
        private var startTime = 0L

        @EventTarget
        private fun onAttack(event: AttackEvent) {
            if (event.targetEntity is EntityLivingBase) {
                syncEntity = event.targetEntity
                startTime = System.currentTimeMillis()
            }
        }

        @EventTarget
        private fun onUpdate(event: UpdateEvent) {
            if (mc.thePlayer == null || mc.theWorld == null || (mc.netHandler == null && !mc.isSingleplayer)) return
            val currentTarget = target
            // Visual feedback for hitting the target
            if (currentTarget != null) {
                if (currentTarget.health < lastHealth) {
                    currentRed = 255; currentGreen = 0; currentBlue = 0
                }
                lastHealth = currentTarget.health

                if (currentGreen < 255) currentGreen = (currentGreen + fadeSpeed.get() * 255).toInt().coerceAtMost(255)
                if (currentBlue < 255) currentBlue = (currentBlue + fadeSpeed.get() * 255).toInt().coerceAtMost(255)
            }

            // Kill tracking logic
            val tracked = syncEntity ?: return
            if (tracked.isDead || tracked.health <= 0) {
                killCounts++
                playKillSound()
                syncEntity = null
            } else if (System.currentTimeMillis() - startTime > 3000) {
                syncEntity = null // Timeout
            }
        }

        @EventTarget(ignoreCondition = true)
        private fun onPacket(event: PacketEvent) {
            val packet = event.packet
            if (packet is S45PacketTitle) {
                if (packet.type == S45PacketTitle.Type.TITLE) {
                    packet.message?.formattedText?.let { title ->
                        if (title.contains("Winner")) {
                            win++
                        }
                        if (title.contains("BedWar") || title.contains("SkyWar")) {
                            totalPlayed++
                        }
                    }
                }
            }
        }

        override fun handleEvents() = true
    }

    init {
        // Initialize listener on creation
        CombatListener.handleEvents()
    }
}
data class SwingFailData(val vec3: Vec3, val startTime: Long)