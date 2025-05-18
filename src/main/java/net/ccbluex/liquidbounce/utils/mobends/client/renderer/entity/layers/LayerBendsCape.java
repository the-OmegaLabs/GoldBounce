package net.ccbluex.liquidbounce.utils.mobends.client.renderer.entity.layers;

import net.ccbluex.liquidbounce.utils.mobends.client.renderer.entity.RenderBendsPlayer;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.MathHelper;

public class LayerBendsCape
implements LayerRenderer<AbstractClientPlayer> {
    private final RenderBendsPlayer playerRenderer;

    public LayerBendsCape(RenderBendsPlayer playerRendererIn) {
        this.playerRenderer = playerRendererIn;
    }

    @Override
    public void doRenderLayer(AbstractClientPlayer entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale) {
        if (entitylivingbaseIn.hasPlayerInfo() && !entitylivingbaseIn.isInvisible() && entitylivingbaseIn.isWearing(EnumPlayerModelParts.CAPE) && entitylivingbaseIn.getLocationCape() != null) {
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            this.playerRenderer.bindTexture(entitylivingbaseIn.getLocationCape());
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0f, 0.0f, 0.125f);
            double d0 = entitylivingbaseIn.prevChasingPosX + (entitylivingbaseIn.chasingPosX - entitylivingbaseIn.prevChasingPosX) * (double)partialTicks - (entitylivingbaseIn.prevPosX + (entitylivingbaseIn.posX - entitylivingbaseIn.prevPosX) * (double)partialTicks);
            double d1 = entitylivingbaseIn.prevChasingPosY + (entitylivingbaseIn.chasingPosY - entitylivingbaseIn.prevChasingPosY) * (double)partialTicks - (entitylivingbaseIn.prevPosY + (entitylivingbaseIn.posY - entitylivingbaseIn.prevPosY) * (double)partialTicks);
            double d2 = entitylivingbaseIn.prevChasingPosZ + (entitylivingbaseIn.chasingPosZ - entitylivingbaseIn.prevChasingPosZ) * (double)partialTicks - (entitylivingbaseIn.prevPosZ + (entitylivingbaseIn.posZ - entitylivingbaseIn.prevPosZ) * (double)partialTicks);
            float f = entitylivingbaseIn.prevRenderYawOffset + (entitylivingbaseIn.renderYawOffset - entitylivingbaseIn.prevRenderYawOffset) * partialTicks;
            double d3 = MathHelper.sin(f * (float)Math.PI / 180.0f);
            double d4 = -MathHelper.cos(f * (float)Math.PI / 180.0f);
            float f1 = (float)d1 * 10.0f;
            f1 = MathHelper.clamp_float(f1, -6.0f, 32.0f);
            float f2 = (float)(d0 * d3 + d2 * d4) * 100.0f;
            float f3 = (float)(d0 * d4 - d2 * d3) * 100.0f;
            if (f2 < 0.0f) {
                f2 = 0.0f;
            }
            float f4 = entitylivingbaseIn.prevCameraYaw + (entitylivingbaseIn.cameraYaw - entitylivingbaseIn.prevCameraYaw) * partialTicks;
            f1 += MathHelper.sin((entitylivingbaseIn.prevDistanceWalkedModified + (entitylivingbaseIn.distanceWalkedModified - entitylivingbaseIn.prevDistanceWalkedModified) * partialTicks) * 6.0f) * 32.0f * f4;
            if (entitylivingbaseIn.isSneaking()) {
                f1 += 25.0f;
            }
            GlStateManager.rotate(6.0f + f2 / 2.0f + f1, 1.0f, 0.0f, 0.0f);
            GlStateManager.rotate(f3 / 2.0f, 0.0f, 0.0f, 1.0f);
            GlStateManager.rotate(-f3 / 2.0f, 0.0f, 1.0f, 0.0f);
            GlStateManager.rotate(180.0f, 0.0f, 1.0f, 0.0f);
            this.playerRenderer.getMainModel().renderCape(0.0625f);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}

