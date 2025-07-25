package net.ccbluex.liquidbounce.injection.implementations;

import java.util.List;

import net.minecraft.client.shader.Shader;

public interface IMixinShaderGroup {
	List<Shader> getListShaders();
}