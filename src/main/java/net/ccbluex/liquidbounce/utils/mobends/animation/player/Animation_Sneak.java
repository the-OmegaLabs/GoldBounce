package net.ccbluex.liquidbounce.utils.mobends.animation.player;

import net.ccbluex.liquidbounce.utils.mobends.animation.Animation;
import net.ccbluex.liquidbounce.utils.mobends.client.model.ModelRendererBends;
import net.ccbluex.liquidbounce.utils.mobends.client.model.entity.ModelBendsPlayer;
import net.ccbluex.liquidbounce.utils.mobends.data.EntityData;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

public class Animation_Sneak
extends Animation {
    @Override
    public String getName() {
        return "sneak";
    }

    @Override
    public void animate(EntityLivingBase argEntity, ModelBase argModel, EntityData argData) {
        ModelBendsPlayer model = (ModelBendsPlayer)argModel;
        float var = (float)((double)(model.armSwing * 0.6662f) / Math.PI) % 2.0f;
        ((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothX(-5.0f + 1.1f * (float)((double)(MathHelper.cos(model.armSwing * 0.6662f) * 1.4f * model.armSwingAmount) / Math.PI * 180.0), 1.0f);
        ((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothX(-5.0f + 1.1f * (float)((double)(MathHelper.cos(model.armSwing * 0.6662f + (float)Math.PI) * 1.4f * model.armSwingAmount) / Math.PI * 180.0), 1.0f);
        ((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothZ(10.0f);
        ((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothZ(-10.0f);
        ((ModelRendererBends)model.bipedRightArm).rotation.setSmoothX(-20.0f + 20.0f * MathHelper.cos(model.armSwing * 0.6662f + (float)Math.PI));
        ((ModelRendererBends)model.bipedLeftArm).rotation.setSmoothX(-20.0f + 20.0f * MathHelper.cos(model.armSwing * 0.6662f));
        model.bipedLeftForeLeg.rotation.setSmoothX(var > 1.0f ? 45 : 10, 0.3f);
        model.bipedRightForeLeg.rotation.setSmoothX(var > 1.0f ? 10 : 45, 0.3f);
        model.bipedLeftForeArm.rotation.setSmoothX(var > 1.0f ? -10 : -45, 0.01f);
        model.bipedRightForeArm.rotation.setSmoothX(var > 1.0f ? -45 : -10, 0.01f);
        float var2 = 25.0f + (float)Math.cos(model.armSwing * 0.6662f * 2.0f) * 5.0f;
        ((ModelRendererBends)model.bipedBody).rotation.setSmoothX(var2);
        ((ModelRendererBends)model.bipedHead).rotation.setX(model.headRotationX - ((ModelRendererBends)model.bipedBody).rotation.getX());
    }
}

