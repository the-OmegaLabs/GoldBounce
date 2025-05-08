/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import com.google.common.base.Predicates;
import net.ccbluex.liquidbounce.event.EventManager;
import net.ccbluex.liquidbounce.event.Render3DEvent;
import net.ccbluex.liquidbounce.features.module.modules.combat.Backtrack;
import net.ccbluex.liquidbounce.features.module.modules.combat.ForwardTrack;
import net.ccbluex.liquidbounce.features.module.modules.misc.OverrideRaycast;
import net.ccbluex.liquidbounce.features.module.modules.player.Reach;
import net.ccbluex.liquidbounce.features.module.modules.render.*;
import net.ccbluex.liquidbounce.features.module.modules.settings.Camera;
import net.ccbluex.liquidbounce.utils.Rotation;
import net.ccbluex.liquidbounce.utils.RotationUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Mixin(EntityRenderer.class)
@SideOnly(Side.CLIENT)
public abstract class MixinEntityRenderer {

    @Shadow
    private Entity pointedEntity;

    @Shadow
    private Minecraft mc;

    @Shadow
    private float thirdPersonDistance;

    @Shadow
    private float thirdPersonDistanceTemp;

    private boolean cloudFog;


    @Inject(method = "renderWorldPass", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/EntityRenderer;renderHand:Z", shift = At.Shift.BEFORE))
    private void renderWorldPass(int pass, float partialTicks, long finishTimeNano, CallbackInfo callbackInfo) {
        EventManager.INSTANCE.callEvent(new Render3DEvent(partialTicks));
    }

    @Inject(method = "hurtCameraEffect", at = @At("HEAD"), cancellable = true)
    private void injectHurtCameraEffect(CallbackInfo callbackInfo) {
        if (NoHurtCam.INSTANCE.handleEvents()) {
            callbackInfo.cancel();
        }
    }

    @ModifyConstant(method = "orientCamera", constant = @Constant(intValue = 8))
    private int injectCameraClip(int eight) {
        return CameraClip.INSTANCE.handleEvents() ? 0 : eight;
    }

    @Inject(at = @At("HEAD"), method = "updateCameraAndRender")
    private void injectCameraModifications(float p_updateCameraAndRender_1_, long p_updateCameraAndRender_2_, CallbackInfo ci) {
        FreeCam.INSTANCE.useModifiedPosition();
    }
    @Unique
    public double prevRenderX = 0d;
    @Unique
    public double prevRenderY = 0d;
    @Unique
    public double prevRenderZ = 0d;
    @Inject(method = "orientCamera", at = @At(value = "HEAD"))
    private void injectFreeLook(float p_orientCamera_1_, CallbackInfo ci) {
        FreeLook.INSTANCE.useModifiedRotation();
    }
    @Inject(method = "orientCamera", at = @At(value = "HEAD"), cancellable = true)
    private void change(float p_orientCamera_1_, CallbackInfo ci) {
        Camera camera = Camera.INSTANCE;
        Entity entity = this.mc.getRenderViewEntity();
        float f = entity.getEyeHeight();
        double d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double)p_orientCamera_1_;
        double d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double)p_orientCamera_1_ + (double)f;
        double d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double)p_orientCamera_1_;
        double currentInterpolation = 0.5f;
        if(camera.isActive() && camera.getMotionCamera().get()){
            currentInterpolation = camera.getInterpolation().get();
        }
        if(camera!=null){
            prevRenderX = prevRenderX + (d0 - prevRenderX) * currentInterpolation;
            prevRenderY = prevRenderY + (d1-prevRenderY) * currentInterpolation;
            prevRenderZ = prevRenderZ + (d2-prevRenderZ) * currentInterpolation;
        }
        if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isPlayerSleeping()) {
            f = (float)((double)f + (double)1.0F);
            GlStateManager.translate(0.0F, 0.3F, 0.0F);
            if (!this.mc.gameSettings.debugCamEnable) {
                BlockPos blockpos = new BlockPos(entity);
                IBlockState iblockstate = this.mc.theWorld.getBlockState(blockpos);
                ForgeHooksClient.orientBedCamera(this.mc.theWorld, blockpos, iblockstate, entity);
                GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * p_orientCamera_1_ + 180.0F, 0.0F, -1.0F, 0.0F);
                GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * p_orientCamera_1_, -1.0F, 0.0F, 0.0F);
            }
        } else if (this.mc.gameSettings.thirdPersonView > 0) {
            double d3 = this.thirdPersonDistanceTemp + (this.thirdPersonDistance - this.thirdPersonDistanceTemp) * p_orientCamera_1_;
            if (this.mc.gameSettings.debugCamEnable) {
                GlStateManager.translate(0.0F, 0.0F, (float)(-d3));
            } else {
                float f1 = entity.rotationYaw;
                float f2 = entity.rotationPitch;
                if (this.mc.gameSettings.thirdPersonView == 2) {
                    f2 += 180.0F;
                }

                double d4 = (double)(-MathHelper.sin(f1 / 180.0F * (float)Math.PI) * MathHelper.cos(f2 / 180.0F * (float)Math.PI)) * d3;
                double d5 = (double)(MathHelper.cos(f1 / 180.0F * (float)Math.PI) * MathHelper.cos(f2 / 180.0F * (float)Math.PI)) * d3;
                double d6 = (double)(-MathHelper.sin(f2 / 180.0F * (float)Math.PI)) * d3;

                for(int i = 0; i < 8; ++i) {
                    float f3 = (float)((i & 1) * 2 - 1);
                    float f4 = (float)((i >> 1 & 1) * 2 - 1);
                    float f5 = (float)((i >> 2 & 1) * 2 - 1);
                    f3 *= 0.1F;
                    f4 *= 0.1F;
                    f5 *= 0.1F;
                    MovingObjectPosition movingobjectposition = this.mc.theWorld.rayTraceBlocks(new Vec3(d0 + (double)f3, d1 + (double)f4, d2 + (double)f5), new Vec3(d0 - d4 + (double)f3 + (double)f5, d1 - d6 + (double)f4, d2 - d5 + (double)f5));
                    if (movingobjectposition != null) {
                        double d7 = movingobjectposition.hitVec.distanceTo(new Vec3(d0, d1, d2));
                        if (d7 < d3) {
                            d3 = d7;
                        }
                    }
                }

                if (this.mc.gameSettings.thirdPersonView == 2) {
                    GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                }

                GlStateManager.rotate(entity.rotationPitch - f2, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(entity.rotationYaw - f1, 0.0F, 1.0F, 0.0F);
                GlStateManager.translate(0.0F, 0.0F, (float)(-d3));
                GlStateManager.rotate(f1 - entity.rotationYaw, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(f2 - entity.rotationPitch, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(entity.rotationYaw,0.0F,1.0F,0.0F);
                GlStateManager.translate(prevRenderX-d0,d1-prevRenderY,prevRenderZ-d2);
                GlStateManager.rotate(-entity.rotationYaw,0.0F,1.0F,0.0F);
            }
        } else {
            GlStateManager.translate(0.0F, 0.0F, -0.1F);
        }

        if (!this.mc.gameSettings.debugCamEnable) {
            float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * p_orientCamera_1_ + 180.0F;
            float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * p_orientCamera_1_;
            float roll = 0.0F;
            if (entity instanceof EntityAnimal) {
                EntityAnimal entityanimal = (EntityAnimal)entity;
                yaw = entityanimal.prevRotationYawHead + (entityanimal.rotationYawHead - entityanimal.prevRotationYawHead) * p_orientCamera_1_ + 180.0F;
            }

            Block block = ActiveRenderInfo.getBlockAtEntityViewpoint(this.mc.theWorld, entity, p_orientCamera_1_);
            EntityViewRenderEvent.CameraSetup event = new EntityViewRenderEvent.CameraSetup(this.mc.entityRenderer, entity, block, p_orientCamera_1_, yaw, pitch, roll);
            MinecraftForge.EVENT_BUS.post(event);
            GlStateManager.rotate(event.roll, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(event.pitch, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(event.yaw, 0.0F, 1.0F, 0.0F);
            ci.cancel();
        }

        GlStateManager.translate(0.0F, -f, 0.0F);
        d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double)p_orientCamera_1_;
        d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double)p_orientCamera_1_ + (double)f;
        d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double)p_orientCamera_1_;
        this.cloudFog = this.mc.renderGlobal.hasCloudFog(d0, d1, d2, p_orientCamera_1_);
        ci.cancel();
    }
    @Inject(at = @At("TAIL"), method = "updateCameraAndRender")
    private void injectCameraRestorations(float p_updateCameraAndRender_1_, long p_updateCameraAndRender_2_, CallbackInfo ci) {
        FreeLook.INSTANCE.restoreOriginalRotation();
        FreeCam.INSTANCE.restoreOriginalPosition();
    }

    /**
     * @author CCBlueX
     */
    @Inject(method = "getMouseOver", at = @At("HEAD"), cancellable = true)
    private void getMouseOver(float p_getMouseOver_1_, CallbackInfo ci) {
        Entity entity = mc.getRenderViewEntity();
        if (entity != null && mc.theWorld != null) {
            mc.mcProfiler.startSection("pick");
            mc.pointedEntity = null;

            final Reach reach = Reach.INSTANCE;

            double d0 = reach.handleEvents() ? reach.getMaxRange() : mc.playerController.getBlockReachDistance();
            Vec3 vec3 = entity.getPositionEyes(p_getMouseOver_1_);
            Rotation rotation = new Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
            Vec3 vec31 = RotationUtils.INSTANCE.getVectorForRotation(RotationUtils.INSTANCE.getCurrentRotation() != null && OverrideRaycast.INSTANCE.shouldOverride() ? RotationUtils.INSTANCE.getCurrentRotation() : rotation);
            double p_rayTrace_1_ = (reach.handleEvents() ? reach.getBuildReach() : d0);
            Vec3 vec32 = vec3.addVector(vec31.xCoord * p_rayTrace_1_, vec31.yCoord * p_rayTrace_1_, vec31.zCoord * p_rayTrace_1_);
            mc.objectMouseOver = entity.worldObj.rayTraceBlocks(vec3, vec32, false, false, true);
            double d1 = d0;
            boolean flag = false;
            if (mc.playerController.extendedReach()) {
                // d0 = 6;
                d1 = 6;
            } else if (d0 > 3) {
                flag = true;
            }

            if (mc.objectMouseOver != null) {
                d1 = mc.objectMouseOver.hitVec.distanceTo(vec3);
            }

            if (reach.handleEvents()) {
                double p_rayTrace_1_2 = reach.getBuildReach();
                Vec3 vec322 = vec3.addVector(vec31.xCoord * p_rayTrace_1_2, vec31.yCoord * p_rayTrace_1_2, vec31.zCoord * p_rayTrace_1_2);
                final MovingObjectPosition movingObjectPosition = entity.worldObj.rayTraceBlocks(vec3, vec322, false, false, true);

                if (movingObjectPosition != null) d1 = movingObjectPosition.hitVec.distanceTo(vec3);
            }

            pointedEntity = null;
            Vec3 vec33 = null;
            List<Entity> list = mc.theWorld.getEntities(Entity.class, Predicates.and(EntitySelectors.NOT_SPECTATING, p_apply_1_ -> p_apply_1_ != null && p_apply_1_.canBeCollidedWith() && p_apply_1_ != entity));
            double d2 = d1;

            for (Entity entity1 : list) {
                float f1 = entity1.getCollisionBorderSize();

                final ArrayList<AxisAlignedBB> boxes = new ArrayList<>();
                boxes.add(entity1.getEntityBoundingBox().expand(f1, f1, f1));

                Backtrack.INSTANCE.loopThroughBacktrackData(entity1, () -> {
                    boxes.add(entity1.getEntityBoundingBox().expand(f1, f1, f1));
                    return false;
                });

                ForwardTrack.INSTANCE.includeEntityTruePos(entity1, () -> {
                    boxes.add(entity1.getEntityBoundingBox().expand(f1, f1, f1));
                    return null;
                });

                for (final AxisAlignedBB axisalignedbb : boxes) {
                    MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);
                    if (axisalignedbb.isVecInside(vec3)) {
                        if (d2 >= 0) {
                            pointedEntity = entity1;
                            vec33 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
                            d2 = 0;
                        }
                    } else if (movingobjectposition != null) {
                        double d3 = vec3.distanceTo(movingobjectposition.hitVec);
                        if (d3 < d2 || d2 == 0) {
                            if (entity1 == entity.ridingEntity && !entity.canRiderInteract()) {
                                if (d2 == 0) {
                                    pointedEntity = entity1;
                                    vec33 = movingobjectposition.hitVec;
                                }
                            } else {
                                pointedEntity = entity1;
                                vec33 = movingobjectposition.hitVec;
                                d2 = d3;
                            }
                        }
                    }
                }
            }

            if (pointedEntity != null && flag && vec3.distanceTo(vec33) > (reach.handleEvents() ? reach.getCombatReach() : 3)) {
                pointedEntity = null;
                mc.objectMouseOver = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS, Objects.requireNonNull(vec33), null, new BlockPos(vec33));
            }

            if (pointedEntity != null && (d2 < d1 || mc.objectMouseOver == null)) {
                mc.objectMouseOver = new MovingObjectPosition(pointedEntity, vec33);
                if (pointedEntity instanceof EntityLivingBase || pointedEntity instanceof EntityItemFrame) {
                    mc.pointedEntity = pointedEntity;
                }
            }

            mc.mcProfiler.endSection();
        }

    }

    /**
     * Properly implement the confusion option from AntiBlind module
     */
    @Redirect(method = "setupCameraTransform", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;isPotionActive(Lnet/minecraft/potion/Potion;)Z"))
    private boolean injectAntiBlindA(EntityPlayerSP instance, Potion potion) {
        AntiBlind module = AntiBlind.INSTANCE;

        return (!module.handleEvents() || !module.getConfusionEffect()) && instance.isPotionActive(potion);
    }

    @Redirect(method = {"setupFog", "updateFogColor"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;isPotionActive(Lnet/minecraft/potion/Potion;)Z"))
    private boolean injectAntiBlindB(EntityLivingBase instance, Potion potion) {
        if (instance != mc.thePlayer) {
            return instance.isPotionActive(potion);
        }

        AntiBlind module = AntiBlind.INSTANCE;

        return (!module.handleEvents() || !module.getConfusionEffect()) && instance.isPotionActive(potion);
    }
}
