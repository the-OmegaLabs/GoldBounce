package net.ccbluex.liquidbounce.utils.mobends.animation.zombie;

import net.ccbluex.liquidbounce.utils.mobends.animation.Animation;
import net.ccbluex.liquidbounce.utils.mobends.client.model.ModelRendererBends;
import net.ccbluex.liquidbounce.utils.mobends.client.model.entity.ModelBendsZombie;
import net.ccbluex.liquidbounce.utils.mobends.data.Data_Zombie;
import net.ccbluex.liquidbounce.utils.mobends.data.EntityData;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.MathHelper;

public class Animation_Walk
extends Animation {
    @Override
    public String getName() {
        return "walk";
    }

    @Override
    public void animate(EntityLivingBase argEntity, ModelBase argModel, EntityData argData) {
        EntityZombie zombie = (EntityZombie)argEntity;
        ModelBendsZombie model = (ModelBendsZombie)argModel;
        Data_Zombie data = (Data_Zombie)argData;
        model.renderOffset.setSmoothY(-3.0f);
        float var2 = 30.0f + MathHelper.cos(model.armSwing * 0.6662f * 2.0f) * 10.0f;
        ((ModelRendererBends)model.bipedBody).rotation.setSmoothX(var2, 0.3f);
        ((ModelRendererBends)model.bipedRightArm).rotation.setSmoothX(0.9f * (float)((double)(MathHelper.cos(model.armSwing * 0.6662f + (float)Math.PI) * 2.0f * model.armSwingAmount * 0.5f) / Math.PI * 180.0));
        ((ModelRendererBends)model.bipedLeftArm).rotation.setSmoothX(0.9f * (float)((double)(MathHelper.cos(model.armSwing * 0.6662f) * 2.0f * model.armSwingAmount * 0.5f) / Math.PI * 180.0));
        ((ModelRendererBends)model.bipedRightArm).rotation.setSmoothZ(5.0f, 0.3f);
        ((ModelRendererBends)model.bipedLeftArm).rotation.setSmoothZ(-5.0f, 0.3f);
        ((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothX(-5.0f + 0.9f * (float)((double)(MathHelper.cos(model.armSwing * 0.6662f) * 1.4f * model.armSwingAmount) / Math.PI * 180.0), 1.0f);
        ((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothX(-5.0f + 0.9f * (float)((double)(MathHelper.cos(model.armSwing * 0.6662f + (float)Math.PI) * 1.4f * model.armSwingAmount) / Math.PI * 180.0), 1.0f);
        ((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothY(0.0f);
        ((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothY(0.0f);
        ((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothZ(10.0f, 0.2f);
        ((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothZ(-10.0f, 0.2f);
        float var = (float)((double)(model.armSwing * 0.6662f) / Math.PI) % 2.0f;
        ((ModelRendererBends)model.bipedLeftForeLeg).rotation.setSmoothX(var > 1.0f ? 45 : 0, 0.3f);
        ((ModelRendererBends)model.bipedRightForeLeg).rotation.setSmoothX(var > 1.0f ? 0 : 45, 0.3f);
        ((ModelRendererBends)model.bipedLeftForeArm).rotation.setSmoothX((float)(Math.cos((double)(model.armSwing * 0.6662f) + 1.5707963267948966) + 1.0) / 2.0f * -20.0f, 1.0f);
        ((ModelRendererBends)model.bipedRightForeArm).rotation.setSmoothX((float)(Math.cos(model.armSwing * 0.6662f) + 1.0) / 2.0f * -20.0f, 0.3f);
        float var1 = MathHelper.cos(model.armSwing * 0.6662f + (float)Math.PI) / (float)Math.PI * 180.0f * 0.5f;
        ((ModelRendererBends)model.bipedBody).rotation.setSmoothY(MathHelper.cos(model.armSwing * 0.6662f + (float)Math.PI) / (float)Math.PI * 180.0f * 0.5f, 0.3f);
        if (data.currentWalkingState == 1) {
            ((ModelRendererBends)model.bipedRightArm).rotation.setSmoothX(-120.0f);
            ((ModelRendererBends)model.bipedLeftArm).rotation.setSmoothX(-120.0f);
        }
        ((ModelRendererBends)model.bipedHead).rotation.setX(model.headRotationX - 30.0f);
        ((ModelRendererBends)model.bipedHead).rotation.setY(model.headRotationY - var1);
    }
}

