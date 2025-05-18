package net.ccbluex.liquidbounce.utils.mobends.client.renderer.entity;

import net.ccbluex.liquidbounce.utils.mobends.client.model.entity.ModelBendsSpider;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSpider;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySpider;

public class RenderBendsSpider
extends RenderSpider {
    public RenderBendsSpider(RenderManager renderManager) {
        super(renderManager);
        this.mainModel = new ModelBendsSpider();
    }

    @Override
    protected void preRenderCallback(EntityLivingBase argEntity, float p_77041_2_) {
        float f1 = 0.9375f;
        GlStateManager.scale(f1, f1, f1);
        ((ModelBendsSpider)this.mainModel).updateWithEntityData((EntitySpider)argEntity);
        ((ModelBendsSpider)this.mainModel).postRenderTranslate(0.0625f);
        ((ModelBendsSpider)this.mainModel).postRenderRotate(0.0625f);
    }
}

