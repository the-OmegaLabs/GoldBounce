package net.ccbluex.liquidbounce.utils.mobends.animation.spider;

import net.ccbluex.liquidbounce.utils.mobends.animation.Animation;
import net.ccbluex.liquidbounce.utils.mobends.client.model.ModelRendererBends;
import net.ccbluex.liquidbounce.utils.mobends.client.model.entity.ModelBendsSpider;
import net.ccbluex.liquidbounce.utils.mobends.data.Data_Spider;
import net.ccbluex.liquidbounce.utils.mobends.data.EntityData;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.util.MathHelper;

public class Animation_OnGround
extends Animation {
    @Override
    public String getName() {
        return "onGround";
    }

    @Override
    public void animate(EntityLivingBase argEntity, ModelBase argModel, EntityData argData) {
        EntitySpider spider = (EntitySpider)argEntity;
        ModelBendsSpider model = (ModelBendsSpider)argModel;
        Data_Spider data = (Data_Spider)argData;
        float f9 = -(MathHelper.cos(model.armSwing * 0.6662f * 2.0f + 0.0f) * 0.4f) * model.armSwingAmount;
        float f10 = -(MathHelper.cos(model.armSwing * 0.6662f * 2.0f + (float)Math.PI) * 0.4f) * model.armSwingAmount;
        float f11 = -(MathHelper.cos(model.armSwing * 0.6662f * 2.0f + 1.5707964f) * 0.4f) * model.armSwingAmount;
        float f12 = -(MathHelper.cos(model.armSwing * 0.6662f * 2.0f + 4.712389f) * 0.4f) * model.armSwingAmount;
        float f13 = Math.abs(MathHelper.sin(model.armSwing * 0.6662f + 0.0f) * 0.4f) * model.armSwingAmount;
        float f14 = Math.abs(MathHelper.sin(model.armSwing * 0.6662f + (float)Math.PI) * 0.4f) * model.armSwingAmount;
        float f15 = Math.abs(MathHelper.sin(model.armSwing * 0.6662f + 1.5707964f) * 0.4f) * model.armSwingAmount;
        float f16 = Math.abs(MathHelper.sin(model.armSwing * 0.6662f + 4.712389f) * 0.4f) * model.armSwingAmount;
        model.renderOffset.setSmoothY(MathHelper.cos(model.armSwing * 0.6662f * 2.0f + 0.0f) * 0.4f * model.armSwingAmount * -0.2f);
        model.renderRotation.setX(0.0f);
        model.renderOffset.setSmoothX(0.0f, 0.6f);
        model.renderOffset.setSmoothZ(0.0f, 0.6f);
        ((ModelRendererBends)model.spiderHead).rotation.setY(model.headRotationY);
        ((ModelRendererBends)model.spiderHead).rotation.setX(model.headRotationX);
        float f6 = 0.7853982f;
        float sm = 40.0f;
        ((ModelRendererBends)model.spiderLeg1).rotation.setZ(sm + f13 / (float)Math.PI * 180.0f);
        ((ModelRendererBends)model.spiderLeg2).rotation.setZ(-sm - f13 / (float)Math.PI * 180.0f);
        ((ModelRendererBends)model.spiderLeg3).rotation.setZ(sm + f14 / (float)Math.PI * 180.0f);
        ((ModelRendererBends)model.spiderLeg4).rotation.setZ(-sm - f14 / (float)Math.PI * 180.0f);
        ((ModelRendererBends)model.spiderLeg5).rotation.setZ(sm + f15 / (float)Math.PI * 180.0f);
        ((ModelRendererBends)model.spiderLeg6).rotation.setZ(-sm - f15 / (float)Math.PI * 180.0f);
        ((ModelRendererBends)model.spiderLeg7).rotation.setZ(sm + f16 / (float)Math.PI * 180.0f);
        ((ModelRendererBends)model.spiderLeg8).rotation.setZ(-sm - f16 / (float)Math.PI * 180.0f);
        float f7 = -0.0f;
        float f8 = 0.3926991f;
        ((ModelRendererBends)model.spiderLeg1).pre_rotation.setY(-70.0f + f9 / (float)Math.PI * 180.0f);
        ((ModelRendererBends)model.spiderLeg2).pre_rotation.setY(70.0f - f9 / (float)Math.PI * 180.0f);
        ((ModelRendererBends)model.spiderLeg3).pre_rotation.setY(-40.0f + f10 / (float)Math.PI * 180.0f);
        ((ModelRendererBends)model.spiderLeg4).pre_rotation.setY(40.0f - f10 / (float)Math.PI * 180.0f);
        ((ModelRendererBends)model.spiderLeg5).pre_rotation.setY(40.0f + f11 / (float)Math.PI * 180.0f);
        ((ModelRendererBends)model.spiderLeg6).pre_rotation.setY(-40.0f - f11 / (float)Math.PI * 180.0f);
        ((ModelRendererBends)model.spiderLeg7).pre_rotation.setY(70.0f + f12 / (float)Math.PI * 180.0f);
        ((ModelRendererBends)model.spiderLeg8).pre_rotation.setY(-70.0f - f12 / (float)Math.PI * 180.0f);
        float foreBend = 89.0f;
        model.spiderForeLeg1.rotation.setZ(-foreBend);
        model.spiderForeLeg2.rotation.setZ(foreBend);
        model.spiderForeLeg3.rotation.setZ(-foreBend);
        model.spiderForeLeg4.rotation.setZ(foreBend);
        model.spiderForeLeg5.rotation.setZ(-foreBend);
        model.spiderForeLeg6.rotation.setZ(foreBend);
        model.spiderForeLeg7.rotation.setZ(-foreBend);
        model.spiderForeLeg8.rotation.setZ(foreBend);
        ((ModelRendererBends)model.spiderBody).rotation.setX(-30.0f);
    }
}

