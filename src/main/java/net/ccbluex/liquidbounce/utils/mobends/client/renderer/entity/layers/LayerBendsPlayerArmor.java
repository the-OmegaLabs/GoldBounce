package net.ccbluex.liquidbounce.utils.mobends.client.renderer.entity.layers;

import net.ccbluex.liquidbounce.utils.mobends.client.model.entity.ModelBendsPlayerArmor;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.entity.EntityLivingBase;

public class LayerBendsPlayerArmor
extends LayerArmorBase<ModelBiped> {
    public LayerBendsPlayerArmor(RendererLivingEntity<?> rendererIn) {
        super(rendererIn);
    }

    @Override
    protected void initArmor() {
        this.modelLeggings = new ModelBendsPlayerArmor(0.5f);
        this.modelArmor = new ModelBendsPlayerArmor(1.5f);
    }

    @Override
    public void doRenderLayer(EntityLivingBase p_177141_1_, float p_177141_2_, float p_177141_3_, float p_177141_4_, float p_177141_5_, float p_177141_6_, float p_177141_7_, float p_177141_8_) {
        super.doRenderLayer(p_177141_1_, p_177141_2_, p_177141_3_, p_177141_4_, p_177141_5_, p_177141_6_, p_177141_7_, p_177141_8_);
    }

    protected void func_177179_a(ModelBiped p_177179_1_, int p_177179_2_) {
        this.func_177194_a(p_177179_1_);
        switch (p_177179_2_) {
            case 1: {
                p_177179_1_.bipedRightLeg.showModel = true;
                p_177179_1_.bipedLeftLeg.showModel = true;
                break;
            }
            case 2: {
                p_177179_1_.bipedBody.showModel = true;
                p_177179_1_.bipedRightLeg.showModel = true;
                p_177179_1_.bipedLeftLeg.showModel = true;
                break;
            }
            case 3: {
                p_177179_1_.bipedBody.showModel = true;
                p_177179_1_.bipedRightArm.showModel = true;
                p_177179_1_.bipedLeftArm.showModel = true;
                break;
            }
            case 4: {
                p_177179_1_.bipedHead.showModel = true;
                p_177179_1_.bipedHeadwear.showModel = true;
            }
        }
    }

    protected void func_177194_a(ModelBiped p_177194_1_) {
        p_177194_1_.setInvisible(false);
    }

    @Override
    protected void setModelPartVisible(ModelBiped model, int armorSlot) {
        this.func_177179_a(model, armorSlot);
    }
}

