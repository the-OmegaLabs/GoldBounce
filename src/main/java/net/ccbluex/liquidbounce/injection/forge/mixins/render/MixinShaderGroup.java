package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import java.util.List;

import net.ccbluex.liquidbounce.injection.implementations.IMixinShaderGroup;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.google.common.collect.Lists;

import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;

@Mixin(ShaderGroup.class)
public class MixinShaderGroup implements IMixinShaderGroup {

	@Shadow
	@Final
	private final List<Shader> listShaders = Lists.<Shader>newArrayList();
	
	@Override
	public List<Shader> getListShaders() {
		return listShaders;
	}
}