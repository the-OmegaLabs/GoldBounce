package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.client.shader.Shader
import net.minecraft.util.ResourceLocation

object MotionBlur : Module(name = "MotionBlur",  category = Category.RENDER) {
    private val blurAmount = IntegerValue("Amount", 7, 1..10)

    override fun onDisable() {
        if (mc.entityRenderer.isShaderActive) mc.entityRenderer.stopUseShader()
    }

    @EventTarget
    fun onTick(event: GameTickEvent) {
        try {
            if (mc.thePlayer != null) {
                if (mc.entityRenderer.shaderGroup == null) mc.entityRenderer.loadShader(
                    ResourceLocation(
                        "minecraft",
                        "shaders/post/motion_blur.json"
                    )
                )
                val uniform = 1f - (blurAmount.get() / 10f).coerceAtMost(0.9f)
                val shaderGroup = mc.entityRenderer.shaderGroup
                try {
                    val listShadersField = shaderGroup::class.java.getDeclaredField("listShaders")
                    listShadersField.isAccessible = true
                    val shaders = listShadersField.get(shaderGroup) as List<Shader>

                    shaders[0].shaderManager.getShaderUniform("Phosphor").set(uniform, 0f, 0f)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        } catch (a: Exception) {
            a.printStackTrace()
        }
    }
}