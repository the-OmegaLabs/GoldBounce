/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MovementInputEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.features.module.modules.world.scaffolds.Scaffold
import net.ccbluex.liquidbounce.utils.SimulatedPlayer
import net.ccbluex.liquidbounce.utils.extensions.isMoving

object Clutch : Module("AutoScaffold", Category.PLAYER, subjective = true, gameDetecting = false, hideModule = false) {

    @EventTarget
    fun onMovementInput(event: MovementInputEvent) {
        val thePlayer = mc.thePlayer ?: return
        val scaffold = ModuleManager.getModule(Scaffold::class.java)
        val simPlayer = SimulatedPlayer.fromClientPlayer(event.originalInput)

        simPlayer.tick()

        // 检查玩家脚下是否有方块
        val blockBelow = mc.theWorld.getBlockState(thePlayer.position.down()).block
        val isEmptyBlockBelow = blockBelow.material.isReplaceable

        // 判断玩家是否处于下落状态且可能会受到伤害
        val isFalling = thePlayer.fallDistance > 2.0f

        if ((isEmptyBlockBelow || isFalling) && thePlayer.isMoving && thePlayer.onGround && !thePlayer.isSneaking && !mc.gameSettings.keyBindSneak.isKeyDown && !simPlayer.onGround) {
            scaffold.state = true
        }
    }

}
