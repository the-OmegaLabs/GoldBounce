package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.injection.forge.mixins.render.CompiledComponent
import java.util.function.Consumer
import net.ccbluex.liquidbounce.injection.forge.mixins.render.FuckingFontRenderMixin
import net.minecraft.client.gui.FontRenderer
import java.lang.reflect.Method

object GCReduce {
    lateinit var m_gc: Method;

    fun init() {
        m_gc = FontRenderer.class. getMethod ("gc")
    }


    fun gc() {
        m_gc.invoke(MinecraftInstance.mc.fontRendererObj)
    }
}