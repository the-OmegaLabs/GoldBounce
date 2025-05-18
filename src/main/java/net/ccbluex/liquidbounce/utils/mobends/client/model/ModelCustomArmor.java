package net.ccbluex.liquidbounce.utils.mobends.client.model;

import net.ccbluex.liquidbounce.utils.mobends.client.model.entity.ModelBendsPlayer;
import net.ccbluex.liquidbounce.utils.mobends.util.SmoothVector3f;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

public class ModelCustomArmor
extends ModelBiped {
    public ModelRenderer bipedRightForeArm;
    public ModelRenderer bipedLeftForeArm;
    public ModelRenderer bipedRightForeLeg;
    public ModelRenderer bipedLeftForeLeg;
    public SmoothVector3f renderOffset = new SmoothVector3f();
    public SmoothVector3f renderRotation = new SmoothVector3f();
    public SmoothVector3f renderItemRotation = new SmoothVector3f();
    public float headRotationX;
    public float headRotationY;
    public float armSwing;
    public float armSwingAmount;

    public ModelCustomArmor() {
        this(0.0f);
    }

    public ModelCustomArmor(float p_i1148_1_) {
        this(p_i1148_1_, 0.0f, 64, 32);
    }

    public ModelCustomArmor(float p_i1149_1_, float p_i1149_2_, int p_i1149_3_, int p_i1149_4_) {
        this.textureWidth = p_i1149_3_;
        this.textureHeight = p_i1149_4_;
        this.bipedHead = new ModelRendererBends(this, 0, 0);
        this.bipedHead.addBox(-4.0f, -8.0f, -4.0f, 8, 8, 8, p_i1149_1_);
        this.bipedHead.setRotationPoint(0.0f, 0.0f + p_i1149_2_ - 12.0f, 0.0f);
        this.bipedHeadwear = new ModelRendererBends(this, 32, 0);
        this.bipedHeadwear.addBox(-4.0f, -8.0f, -4.0f, 8, 8, 8, p_i1149_1_ + 0.5f);
        this.bipedHeadwear.setRotationPoint(0.0f, 0.0f, 0.0f);
        this.bipedBody = new ModelRendererBends(this, 16, 16).setShowChildIfHidden(true);
        this.bipedBody.addBox(-4.0f, -12.0f, -2.0f, 8, 12, 4, p_i1149_1_);
        this.bipedBody.setRotationPoint(0.0f, 0.0f + p_i1149_2_ + 12.0f, 0.0f);
        this.bipedRightArm = new ModelRendererBends_SeperatedChild(this, 40, 16).setMother((ModelRendererBends)this.bipedBody);
        this.bipedRightArm.addBox(-3.0f, -2.0f, -2.0f, 4, 6, 4, p_i1149_1_);
        this.bipedRightArm.setRotationPoint(-5.0f, 2.0f + p_i1149_2_ - 12.0f, 0.0f);
        this.bipedLeftArm = new ModelRendererBends_SeperatedChild(this, 40, 16).setMother((ModelRendererBends)this.bipedBody);
        this.bipedLeftArm.mirror = true;
        this.bipedLeftArm.addBox(-1.0f, -2.0f, -2.0f, 4, 6, 4, p_i1149_1_);
        this.bipedLeftArm.setRotationPoint(5.0f, 2.0f + p_i1149_2_ - 12.0f, 0.0f);
        this.bipedRightLeg = new ModelRendererBends(this, 0, 16);
        this.bipedRightLeg.addBox(-2.0f, 0.0f, -2.0f, 4, 6, 4, p_i1149_1_);
        this.bipedRightLeg.setRotationPoint(-1.9f, 12.0f + p_i1149_2_, 0.0f);
        this.bipedLeftLeg = new ModelRendererBends(this, 0, 16);
        this.bipedLeftLeg.mirror = true;
        this.bipedLeftLeg.addBox(-2.0f, 0.0f, -2.0f, 4, 6, 4, p_i1149_1_);
        this.bipedLeftLeg.setRotationPoint(1.9f, 12.0f + p_i1149_2_, 0.0f);
        this.bipedRightForeArm = new ModelRendererBends(this, 40, 22);
        this.bipedRightForeArm.addBox(0.0f, 0.0f, -4.0f, 4, 6, 4, p_i1149_1_);
        this.bipedRightForeArm.setRotationPoint(-3.0f, 4.0f, 2.0f);
        ((ModelRendererBends)this.bipedRightForeArm).getBox().offsetTextureQuad(this.bipedRightForeArm, 3, 0.0f, -6.0f);
        this.bipedLeftForeArm = new ModelRendererBends(this, 40, 22);
        this.bipedLeftForeArm.mirror = true;
        this.bipedLeftForeArm.addBox(0.0f, 0.0f, -4.0f, 4, 6, 4, p_i1149_1_);
        this.bipedLeftForeArm.setRotationPoint(-1.0f, 4.0f, 2.0f);
        ((ModelRendererBends)this.bipedLeftForeArm).getBox().offsetTextureQuad(this.bipedRightForeArm, 3, 0.0f, -6.0f);
        this.bipedRightForeLeg = new ModelRendererBends(this, 0, 22);
        this.bipedRightForeLeg.addBox(-2.0f, 0.0f, 0.0f, 4, 6, 4, p_i1149_1_);
        this.bipedRightForeLeg.setRotationPoint(0.0f, 6.0f, -2.0f);
        ((ModelRendererBends)this.bipedRightForeLeg).getBox().offsetTextureQuad(this.bipedRightForeLeg, 3, 0.0f, -6.0f);
        this.bipedLeftForeLeg = new ModelRendererBends(this, 0, 22);
        this.bipedLeftForeLeg.mirror = true;
        this.bipedLeftForeLeg.addBox(-2.0f, 0.0f, 0.0f, 4, 6, 4, p_i1149_1_);
        this.bipedLeftForeLeg.setRotationPoint(0.0f, 6.0f, -2.0f);
        ((ModelRendererBends)this.bipedLeftForeLeg).getBox().offsetTextureQuad(this.bipedLeftForeLeg, 3, 0.0f, -6.0f);
        this.bipedBody.addChild(this.bipedHead);
        this.bipedBody.addChild(this.bipedRightArm);
        this.bipedBody.addChild(this.bipedLeftArm);
        this.bipedHead.addChild(this.bipedHeadwear);
        this.bipedRightArm.addChild(this.bipedRightForeArm);
        this.bipedLeftArm.addChild(this.bipedLeftForeArm);
        this.bipedRightLeg.addChild(this.bipedRightForeLeg);
        this.bipedLeftLeg.addChild(this.bipedLeftForeLeg);
        ((ModelRendererBends_SeperatedChild)this.bipedRightArm).setSeperatedPart((ModelRendererBends)this.bipedRightForeArm);
        ((ModelRendererBends_SeperatedChild)this.bipedLeftArm).setSeperatedPart((ModelRendererBends)this.bipedLeftForeArm);
    }

    @Override
    public void render(Entity argEntity, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float p_78088_7_) {
        this.setRotationAngles(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, p_78088_7_, argEntity);
        if (this.isChild) {
            float f6 = 2.0f;
            GL11.glPushMatrix();
            GL11.glScalef((float)(1.5f / f6), (float)(1.5f / f6), (float)(1.5f / f6));
            GL11.glTranslatef((float)0.0f, (float)(16.0f * p_78088_7_), (float)0.0f);
            this.bipedHead.render(p_78088_7_);
            GL11.glPopMatrix();
            GL11.glPushMatrix();
            GL11.glScalef((float)(1.0f / f6), (float)(1.0f / f6), (float)(1.0f / f6));
            GL11.glTranslatef((float)0.0f, (float)(24.0f * p_78088_7_), (float)0.0f);
            this.bipedBody.render(p_78088_7_);
            this.bipedRightArm.render(p_78088_7_);
            this.bipedLeftArm.render(p_78088_7_);
            this.bipedRightLeg.render(p_78088_7_);
            this.bipedLeftLeg.render(p_78088_7_);
            this.bipedHeadwear.render(p_78088_7_);
            GL11.glPopMatrix();
        } else {
            this.bipedBody.render(p_78088_7_);
            this.bipedRightLeg.render(p_78088_7_);
            this.bipedLeftLeg.render(p_78088_7_);
        }
    }

    @Override
    public void setRotationAngles(float argSwingTime, float argSwingAmount, float argArmSway, float argHeadY, float argHeadX, float argNr6, Entity argEntity) {
    }

    public void updateWithModelData(ModelBendsPlayer argModel) {
        if (argModel == null) {
            return;
        }
        ((ModelRendererBends)this.bipedHead).sync((ModelRendererBends)argModel.bipedHead);
        ((ModelRendererBends)this.bipedHeadwear).sync((ModelRendererBends)argModel.bipedHeadwear);
        ((ModelRendererBends)this.bipedBody).sync((ModelRendererBends)argModel.bipedBody);
        ((ModelRendererBends)this.bipedLeftArm).sync((ModelRendererBends)argModel.bipedLeftArm);
        ((ModelRendererBends)this.bipedLeftForeArm).sync(argModel.bipedLeftForeArm);
        ((ModelRendererBends)this.bipedLeftForeLeg).sync(argModel.bipedLeftForeLeg);
        ((ModelRendererBends)this.bipedLeftLeg).sync((ModelRendererBends)argModel.bipedLeftLeg);
        ((ModelRendererBends)this.bipedRightArm).sync((ModelRendererBends)argModel.bipedRightArm);
        ((ModelRendererBends)this.bipedRightForeArm).sync(argModel.bipedRightForeArm);
        ((ModelRendererBends)this.bipedRightForeLeg).sync(argModel.bipedRightForeLeg);
        ((ModelRendererBends)this.bipedRightLeg).sync((ModelRendererBends)argModel.bipedRightLeg);
    }
}

