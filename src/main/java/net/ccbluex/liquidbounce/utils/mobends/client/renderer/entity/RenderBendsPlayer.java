package net.ccbluex.liquidbounce.utils.mobends.client.renderer.entity;

import net.ccbluex.liquidbounce.utils.mobends.client.model.entity.ModelBendsPlayer;
import net.ccbluex.liquidbounce.utils.mobends.client.renderer.entity.layers.LayerBendsCustomHead;
import net.ccbluex.liquidbounce.utils.mobends.client.renderer.entity.layers.LayerBendsPlayerArmor;
import net.ccbluex.liquidbounce.utils.mobends.data.Data_Player;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderBendsPlayer
extends RenderPlayer {
    private final boolean smallArms;

    public RenderBendsPlayer(RenderManager renderManager) {
        super(renderManager, false);
        this.smallArms = false;
        this.mainModel = new ModelBendsPlayer(0.0f, false);
        this.layerRenderers.clear();
        this.addLayer(new LayerBendsPlayerArmor(this));
        this.addLayer(new LayerHeldItem(this));
        this.addLayer(new LayerBendsCustomHead((ModelBendsPlayer)this.getMainModel()));
    }

    public RenderBendsPlayer(RenderManager renderManager, boolean useSmallArms) {
        super(renderManager, useSmallArms);
        this.smallArms = useSmallArms;
        this.mainModel = new ModelBendsPlayer(0.0f, useSmallArms);
        this.layerRenderers.clear();
        this.addLayer(new LayerBendsPlayerArmor(this));
        this.addLayer(new LayerHeldItem(this));
        this.addLayer(new LayerBendsCustomHead((ModelBendsPlayer)this.getMainModel()));
    }

    @Override
    public ModelPlayer getMainModel() {
        if (!(this.mainModel instanceof ModelBendsPlayer)) {
            this.mainModel = new ModelBendsPlayer(0.0f, this.smallArms);
        }
        return (ModelBendsPlayer)this.mainModel;
    }

    private void setModelVisibilities(AbstractClientPlayer clientPlayer) {
        ModelBendsPlayer modelplayer = (ModelBendsPlayer)this.getMainModel();
        if (clientPlayer.isSpectator()) {
            modelplayer.setInvisible(false);
            modelplayer.bipedHead.showModel = true;
            modelplayer.bipedHeadwear.showModel = true;
        } else {
            ItemStack itemstack = clientPlayer.inventory.getCurrentItem();
            modelplayer.setInvisible(true);
            modelplayer.bipedHeadwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.HAT);
            modelplayer.bipedBodyWear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.JACKET);
            modelplayer.bipedLeftLegwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.LEFT_PANTS_LEG);
            modelplayer.bipedRightLegwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.RIGHT_PANTS_LEG);
            modelplayer.bipedLeftArmwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.LEFT_SLEEVE);
            modelplayer.bipedRightArmwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.RIGHT_SLEEVE);
            modelplayer.heldItemLeft = 0;
            modelplayer.aimedBow = false;
            modelplayer.isSneak = clientPlayer.isSneaking();
            if (itemstack == null) {
                modelplayer.heldItemRight = 0;
            } else {
                modelplayer.heldItemRight = 1;
                if (clientPlayer.getItemInUseCount() > 0) {
                    EnumAction enumaction = itemstack.getItemUseAction();
                    if (enumaction == EnumAction.BLOCK) {
                        modelplayer.heldItemRight = 3;
                    } else if (enumaction == EnumAction.BOW) {
                        modelplayer.aimedBow = true;
                    }
                }
            }
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(AbstractClientPlayer entity) {
        return entity.getLocationSkin();
    }

    @Override
    protected void preRenderCallback(AbstractClientPlayer clientPlayer, float partialTicks) {
        float f1 = 0.9375f;
        GlStateManager.scale(f1, f1, f1);
        ((ModelBendsPlayer)this.getMainModel()).updateWithEntityData(clientPlayer);
        ((ModelBendsPlayer)this.mainModel).postRenderTranslate(0.0625f);
        Data_Player data = Data_Player.get(clientPlayer.getEntityId());
        GL11.glPushMatrix();
        float f5 = 0.0625f;
        GL11.glScalef((float)(-f5), (float)(-f5), (float)f5);
        data.swordTrail.render((ModelBendsPlayer)this.getMainModel());
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        GL11.glPopMatrix();
        ((ModelBendsPlayer)this.getMainModel()).postRenderRotate(0.0625f);
    }

    @Override
    public void renderRightArm(AbstractClientPlayer clientPlayer) {
        float f = 1.0f;
        GlStateManager.color(f, f, f);
        ModelPlayer modelplayer = this.getMainModel();
        this.setModelVisibilities(clientPlayer);
        modelplayer.swingProgress = 0.0f;
        modelplayer.isSneak = false;
        modelplayer.setRotationAngles(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0625f, clientPlayer);
        modelplayer.renderRightArm();
    }

    @Override
    public void renderLeftArm(AbstractClientPlayer clientPlayer) {
        float f = 1.0f;
        GlStateManager.color(f, f, f);
        ModelPlayer modelplayer = this.getMainModel();
        this.setModelVisibilities(clientPlayer);
        modelplayer.isSneak = false;
        modelplayer.swingProgress = 0.0f;
        modelplayer.setRotationAngles(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0625f, clientPlayer);
        modelplayer.renderLeftArm();
    }

    @Override
    protected void renderLivingAt(AbstractClientPlayer p_77039_1_, double p_77039_2_, double p_77039_4_, double p_77039_6_) {
        super.renderLivingAt(p_77039_1_, p_77039_2_, p_77039_4_, p_77039_6_);
    }
}

