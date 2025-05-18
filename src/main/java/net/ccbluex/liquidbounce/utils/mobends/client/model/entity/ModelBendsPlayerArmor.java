package net.ccbluex.liquidbounce.utils.mobends.client.model.entity;

import net.ccbluex.liquidbounce.utils.mobends.client.model.ModelRendererBends;
import net.ccbluex.liquidbounce.utils.mobends.client.model.ModelRendererBends_SeperatedChild;
import net.ccbluex.liquidbounce.utils.mobends.client.renderer.SwordTrail;
import net.ccbluex.liquidbounce.utils.mobends.data.Data_Player;
import net.ccbluex.liquidbounce.utils.mobends.util.SmoothVector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

public class ModelBendsPlayerArmor
extends ModelBiped {
    public ModelRendererBends bipedRightForeArm;
    public ModelRendererBends bipedLeftForeArm;
    public ModelRendererBends bipedRightForeLeg;
    public ModelRendererBends bipedLeftForeLeg;
    public ModelRendererBends bipedCloak;
    public ModelRendererBends bipedEars;
    public SmoothVector3f renderOffset = new SmoothVector3f();
    public SmoothVector3f renderRotation = new SmoothVector3f();
    public SmoothVector3f renderItemRotation = new SmoothVector3f();
    public SwordTrail swordTrail = new SwordTrail();
    public float headRotationX;
    public float headRotationY;
    public float armSwing;
    public float armSwingAmount;

    public ModelBendsPlayerArmor(float p_i46304_1_) {
        super(p_i46304_1_);
        this.textureWidth = 64;
        this.textureHeight = 32;
        this.bipedEars = new ModelRendererBends(this, 24, 0);
        this.bipedEars.addBox(-3.0f, -6.0f, -1.0f, 6, 6, 1, p_i46304_1_);
        this.bipedCloak = new ModelRendererBends(this, 0, 0);
        this.bipedCloak.setTextureSize(64, 32);
        this.bipedCloak.addBox(-5.0f, 0.0f, -1.0f, 10, 16, 1, p_i46304_1_);
        this.bipedHead = new ModelRendererBends(this, 0, 0).setShowChildIfHidden(true);
        this.bipedHead.addBox(-4.0f, -8.0f, -4.0f, 8, 8, 8, p_i46304_1_);
        this.bipedHead.setRotationPoint(0.0f, -12.0f, 0.0f);
        this.bipedHeadwear = new ModelRendererBends(this, 32, 0);
        this.bipedHeadwear.addBox(-4.0f, -8.0f, -4.0f, 8, 8, 8, p_i46304_1_ + 0.5f);
        this.bipedHeadwear.setRotationPoint(0.0f, 0.0f, 0.0f);
        this.bipedBody = new ModelRendererBends(this, 16, 16);
        this.bipedBody.addBox(-4.0f, -12.0f, -2.0f, 8, 12, 4, p_i46304_1_);
        this.bipedBody.setRotationPoint(0.0f, 12.0f, 0.0f);
        this.bipedLeftArm = new ModelRendererBends_SeperatedChild(this, 40, 16).setMother((ModelRendererBends)this.bipedBody).setShowChildIfHidden(true);
        this.bipedLeftArm.addBox(-1.0f, -2.0f, -2.0f, 4, 6, 4, p_i46304_1_);
        this.bipedLeftArm.setRotationPoint(5.0f, -10.0f, 0.0f);
        this.bipedLeftArm.mirror = true;
        this.bipedRightArm = new ModelRendererBends_SeperatedChild(this, 40, 16).setMother((ModelRendererBends)this.bipedBody).setShowChildIfHidden(true);
        this.bipedRightArm.addBox(-3.0f, -2.0f, -2.0f, 4, 6, 4, p_i46304_1_);
        this.bipedRightArm.setRotationPoint(-5.0f, -10.0f, 0.0f);
        ((ModelRendererBends)this.bipedRightArm).offsetBox_Add(-0.01f, 0.0f, -0.01f).resizeBox(4.02f, 6.0f, 4.02f).updateVertices();
        ((ModelRendererBends)this.bipedLeftArm).offsetBox_Add(-0.01f, 0.0f, -0.01f).resizeBox(4.02f, 6.0f, 4.02f).updateVertices();
        this.bipedLeftForeArm = new ModelRendererBends(this, 40, 22);
        this.bipedLeftForeArm.addBox(-1.0f, 0.0f, -4.0f, 4, 6, 4, p_i46304_1_);
        this.bipedLeftForeArm.setRotationPoint(0.0f, 4.0f, 2.0f);
        this.bipedLeftForeArm.mirror = true;
        this.bipedLeftForeArm.getBox().offsetTextureQuad(this.bipedLeftForeArm, 3, 0.0f, -6.0f);
        this.bipedRightForeArm = new ModelRendererBends(this, 40, 22);
        this.bipedRightForeArm.addBox(-3.0f, 0.0f, -4.0f, 4, 6, 4, p_i46304_1_);
        this.bipedRightForeArm.setRotationPoint(0.0f, 4.0f, 2.0f);
        this.bipedRightForeArm.getBox().offsetTextureQuad(this.bipedRightForeArm, 3, 0.0f, -6.0f);
        this.bipedRightLeg = new ModelRendererBends(this, 0, 16);
        this.bipedRightLeg.addBox(-2.0f, 0.0f, -2.0f, 4, 6, 4, p_i46304_1_);
        this.bipedRightLeg.setRotationPoint(-1.9f, 12.0f, 0.0f);
        this.bipedLeftLeg = new ModelRendererBends(this, 0, 16);
        this.bipedLeftLeg.mirror = true;
        this.bipedLeftLeg.addBox(-2.0f, 0.0f, -2.0f, 4, 6, 4, p_i46304_1_);
        this.bipedLeftLeg.setRotationPoint(1.9f, 12.0f, 0.0f);
        this.bipedRightForeLeg = new ModelRendererBends(this, 0, 22);
        this.bipedRightForeLeg.addBox(-2.0f, 0.0f, 0.0f, 4, 6, 4, p_i46304_1_);
        this.bipedRightForeLeg.setRotationPoint(0.0f, 6.0f, -2.0f);
        this.bipedRightForeLeg.getBox().offsetTextureQuad(this.bipedRightForeLeg, 3, 0.0f, -6.0f);
        this.bipedLeftForeLeg = new ModelRendererBends(this, 0, 22);
        this.bipedLeftForeLeg.mirror = true;
        this.bipedLeftForeLeg.addBox(-2.0f, 0.0f, 0.0f, 4, 6, 4, p_i46304_1_);
        this.bipedLeftForeLeg.setRotationPoint(0.0f, 6.0f, -2.0f);
        this.bipedLeftForeLeg.getBox().offsetTextureQuad(this.bipedLeftForeLeg, 3, 0.0f, -6.0f);
        this.bipedBody.addChild(this.bipedRightArm);
        this.bipedBody.addChild(this.bipedLeftArm);
        this.bipedHead.addChild(this.bipedHeadwear);
        this.bipedRightArm.addChild(this.bipedRightForeArm);
        this.bipedLeftArm.addChild(this.bipedLeftForeArm);
        this.bipedRightLeg.addChild(this.bipedRightForeLeg);
        this.bipedLeftLeg.addChild(this.bipedLeftForeLeg);
        ((ModelRendererBends_SeperatedChild)this.bipedRightArm).setSeperatedPart(this.bipedRightForeArm);
        ((ModelRendererBends_SeperatedChild)this.bipedLeftArm).setSeperatedPart(this.bipedLeftForeArm);
        ((ModelRendererBends)this.bipedRightLeg).offsetBox_Add(-0.01f, 0.0f, -0.01f).resizeBox(4.02f, 6.0f, 4.02f).updateVertices();
        ((ModelRendererBends)this.bipedLeftLeg).offsetBox_Add(-0.01f, 0.0f, -0.01f).resizeBox(4.02f, 6.0f, 4.02f).updateVertices();
    }

    @Override
    public void render(Entity argEntity, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float p_78088_7_) {
        this.setRotationAngles(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, p_78088_7_, argEntity);
        GL11.glPushMatrix();
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
            GL11.glPushMatrix();
            this.bipedBody.postRender(p_78088_7_);
            this.bipedHead.render(p_78088_7_);
            GL11.glPopMatrix();
        }
        GL11.glPopMatrix();
    }

    @Override
    public void setRotationAngles(float argSwingTime, float argSwingAmount, float argArmSway, float argHeadY, float argHeadX, float argNr6, Entity argEntity) {
        if (Minecraft.getMinecraft().theWorld == null) {
            return;
        }
        if (Minecraft.getMinecraft().theWorld.isRemote && Minecraft.getMinecraft().isGamePaused()) {
            return;
        }
        Data_Player data = Data_Player.get(argEntity.getEntityId());
        this.armSwing = argSwingTime;
        this.armSwingAmount = argSwingAmount;
        this.headRotationX = argHeadX;
        this.headRotationY = argHeadY;
        if (Minecraft.getMinecraft().currentScreen != null) {
            this.headRotationY = 0.0f;
        }
        ((ModelRendererBends)this.bipedHead).sync(data.head);
        ((ModelRendererBends)this.bipedHeadwear).sync(data.headwear);
        ((ModelRendererBends)this.bipedBody).sync(data.body);
        ((ModelRendererBends)this.bipedRightArm).sync(data.rightArm);
        ((ModelRendererBends)this.bipedLeftArm).sync(data.leftArm);
        ((ModelRendererBends)this.bipedRightLeg).sync(data.rightLeg);
        ((ModelRendererBends)this.bipedLeftLeg).sync(data.leftLeg);
        this.bipedRightForeArm.sync(data.rightForeArm);
        this.bipedLeftForeArm.sync(data.leftForeArm);
        this.bipedRightForeLeg.sync(data.rightForeLeg);
        this.bipedLeftForeLeg.sync(data.leftForeLeg);
        this.renderOffset.set(data.renderOffset);
        this.renderRotation.set(data.renderRotation);
        this.renderItemRotation.set(data.renderItemRotation);
        this.swordTrail = data.swordTrail;
    }

    public void postRender(float argScale) {
        GlStateManager.translate(this.renderOffset.vSmooth.x * argScale, -this.renderOffset.vSmooth.y * argScale, this.renderOffset.vSmooth.z * argScale);
        GlStateManager.rotate(this.renderRotation.getX(), 1.0f, 0.0f, 0.0f);
        GlStateManager.rotate(this.renderRotation.getY(), 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(this.renderRotation.getZ(), 0.0f, 0.0f, 1.0f);
    }

    public void postRenderTranslate(float argScale) {
        GlStateManager.translate(this.renderOffset.vSmooth.x * argScale, -this.renderOffset.vSmooth.y * argScale, this.renderOffset.vSmooth.z * argScale);
    }

    public void postRenderRotate(float argScale) {
        GlStateManager.rotate(this.renderRotation.getX(), 1.0f, 0.0f, 0.0f);
        GlStateManager.rotate(this.renderRotation.getY(), 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(this.renderRotation.getZ(), 0.0f, 0.0f, 1.0f);
    }

    public void updateWithEntityData(AbstractClientPlayer argPlayer) {
        Data_Player data = Data_Player.get(argPlayer.getEntityId());
        if (data != null) {
            this.renderOffset.set(data.renderOffset);
            this.renderRotation.set(data.renderRotation);
            this.renderItemRotation.set(data.renderItemRotation);
        }
    }
}

