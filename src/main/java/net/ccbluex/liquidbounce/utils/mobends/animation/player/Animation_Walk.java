package net.ccbluex.liquidbounce.utils.mobends.animation.player;

import net.ccbluex.liquidbounce.utils.mobends.animation.Animation;
import net.ccbluex.liquidbounce.utils.mobends.client.model.ModelRendererBends;
import net.ccbluex.liquidbounce.utils.mobends.client.model.entity.ModelBendsPlayer;
import net.ccbluex.liquidbounce.utils.mobends.data.EntityData;
import net.ccbluex.liquidbounce.utils.mobends.util.GUtil;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

public class Animation_Walk
extends Animation {
    @Override
    public String getName() {
        return "walk";
    }

    @Override
    public void animate(EntityLivingBase argEntity, ModelBase argModel, EntityData argData) {
        ModelBendsPlayer model = (ModelBendsPlayer)argModel;
        ((ModelRendererBends)model.bipedRightArm).rotation.setSmoothX(0.5f * (float)((double)(MathHelper.cos(model.armSwing * 0.6662f + (float)Math.PI) * 2.0f * model.armSwingAmount * 0.5f) / Math.PI * 180.0));
        ((ModelRendererBends)model.bipedLeftArm).rotation.setSmoothX(0.5f * (float)((double)(MathHelper.cos(model.armSwing * 0.6662f) * 2.0f * model.armSwingAmount * 0.5f) / Math.PI * 180.0));
        ((ModelRendererBends)model.bipedRightArm).rotation.setSmoothZ(5.0f, 0.3f);
        ((ModelRendererBends)model.bipedLeftArm).rotation.setSmoothZ(-5.0f, 0.3f);
        ((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothX(-5.0f + 0.5f * (float)((double)(MathHelper.cos(model.armSwing * 0.6662f) * 1.4f * model.armSwingAmount) / Math.PI * 180.0), 1.0f);
        ((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothX(-5.0f + 0.5f * (float)((double)(MathHelper.cos(model.armSwing * 0.6662f + (float)Math.PI) * 1.4f * model.armSwingAmount) / Math.PI * 180.0), 1.0f);
        ((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothY(0.0f);
        ((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothY(0.0f);
        ((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothZ(2.0f, 0.2f);
        ((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothZ(-2.0f, 0.2f);
        float var = (float)((double)(model.armSwing * 0.6662f) / Math.PI) % 2.0f;
        model.bipedLeftForeLeg.rotation.setSmoothX(var > 1.0f ? 45 : 0, 0.3f);
        model.bipedRightForeLeg.rotation.setSmoothX(var > 1.0f ? 0 : 45, 0.3f);
        model.bipedLeftForeArm.rotation.setSmoothX((float)(Math.cos((double)(model.armSwing * 0.6662f) + 1.5707963267948966) + 1.0) / 2.0f * -20.0f, 1.0f);
        model.bipedRightForeArm.rotation.setSmoothX((float)(Math.cos(model.armSwing * 0.6662f) + 1.0) / 2.0f * -20.0f, 0.3f);
        float var2 = (float)Math.cos(model.armSwing * 0.6662f) * -20.0f;
        float var3 = (float)(Math.cos(model.armSwing * 0.6662f * 2.0f) * 0.5 + 0.5) * 10.0f - 2.0f;
        ((ModelRendererBends)model.bipedBody).rotation.setSmoothY(var2, 0.5f);
        ((ModelRendererBends)model.bipedBody).rotation.setSmoothX(var3);
        ((ModelRendererBends)model.bipedHead).rotation.setSmoothY(model.headRotationY - var2, 0.5f);
        ((ModelRendererBends)model.bipedHead).rotation.setSmoothX(model.headRotationX - var3);
        float var10 = model.headRotationY * 0.1f;
        var10 = GUtil.max(var10, 10.0f);
        var10 = GUtil.min(var10, -10.0f);
        ((ModelRendererBends)model.bipedBody).rotation.setSmoothZ(-var10, 0.3f);
    }
}

