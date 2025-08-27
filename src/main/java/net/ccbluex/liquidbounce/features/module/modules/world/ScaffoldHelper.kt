package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.features.module.modules.world.scaffolds.Scaffold
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.extensions.tryJump
import net.ccbluex.liquidbounce.value._boolean
import net.ccbluex.liquidbounce.value.intValue

object ScaffoldHelper : Module("ScaffoldHelper", Category.WORLD) {

    // 配置
    private val onlyJump by _boolean("OnlyJump", false)
    private val onlyJumpDelay by intValue("OnlyJumpDelay", 5, 1..20) { onlyJump }

    private val addTime by _boolean("AddTime", false)
    private val addTimeDelay by intValue("AddTimeDelay", 20, 1..40) { addTime }

    private var canJump = false
    private var jumpTicks = 0
    private var addTimeTicks = 0
    private var isInJump = false

    override fun onEnable() {
        val scaffold = ModuleManager.get(Scaffold::class.java)
        if (scaffold != null && !scaffold.state) {
            scaffold.state = true
        }
        canJump = true
        resetStates()
    }

    override fun onDisable() {
        val scaffold = ModuleManager.get(Scaffold::class.java)
        if (scaffold != null && scaffold.state) {
            scaffold.state = false
        }
        canJump = false
        resetStates()
    }

    private fun resetStates() {
        jumpTicks = 0
        addTimeTicks = 0
        isInJump = false
    }

    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        val player = mc.thePlayer ?: return
        if (player.onGround && player.isMoving && canJump && !addTimeActive()) {
            player.tryJump()
            isInJump = true
            jumpTicks = 0
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val player = mc.thePlayer ?: return
        val scaffold = ModuleManager.get(Scaffold::class.java) ?: return

        if (!canJump) return

        if (onlyJump) {
            if (isInJump && !player.onGround) {
                jumpTicks++
                if (jumpTicks >= onlyJumpDelay && !scaffold.state) {
                    scaffold.state = true
                }
            }
            if (player.onGround && isInJump) {
                isInJump = false
                jumpTicks = 0
                if (!addTime) {
                    if (scaffold.state) scaffold.state = false
                } else {
                    addTimeTicks = addTimeDelay
                }
            }
        }

        // addtime
        if (addTime) {
            if (addTimeTicks > 0) {
                addTimeTicks--
                if (addTimeTicks == 0) {
                    if (scaffold.state) scaffold.state = false
                    if (player.onGround) player.tryJump()
                }
            }
        }

        if (player.movementInput.moveForward > 0.8) {
            player.isSprinting = true
        }
    }

    private fun addTimeActive(): Boolean {
        return addTime && addTimeTicks > 0
    }
}
