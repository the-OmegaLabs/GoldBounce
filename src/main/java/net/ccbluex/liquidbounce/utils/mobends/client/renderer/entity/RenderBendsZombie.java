package net.ccbluex.liquidbounce.utils.mobends.client.renderer.entity;

import com.google.common.collect.Lists;
import net.ccbluex.liquidbounce.utils.mobends.client.model.entity.ModelBendsZombie;
import net.ccbluex.liquidbounce.utils.mobends.client.model.entity.ModelBendsZombieVillager;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class RenderBendsZombie extends RenderBiped
{
    private static final ResourceLocation zombieTextures;
    private static final ResourceLocation zombieVillagerTextures;
    private final ModelBiped field_82434_o;
    private final ModelBendsZombieVillager zombieVillagerModel;
    private final List field_177121_n;
    private final List field_177122_o;
    private static final String __OBFID = "CL_00001037";

    public RenderBendsZombie(final RenderManager renderManagerIn) {
        super(renderManagerIn, (ModelBiped)new ModelBendsZombie(), 0.5f, 1.0f);
        final LayerRenderer layerrenderer = (LayerRenderer) this.layerRenderers.get(0);
        this.field_82434_o = this.modelBipedMain;
        this.zombieVillagerModel = new ModelBendsZombieVillager();
        this.addLayer((LayerRenderer)new LayerHeldItem((RendererLivingEntity)this));
        final LayerBipedArmor layerbipedarmor = new LayerBipedArmor(this) {
            private static final String __OBFID = "CL_00002429";

            protected void initArmor() {
                this.modelLeggings = new ModelBendsZombie(0.5f, true);
                this.modelArmor = new ModelBendsZombie(1.0f, true);
            }
        };
        this.addLayer((LayerRenderer)layerbipedarmor);
        this.field_177122_o = Lists.newArrayList((Iterable)this.layerRenderers);
        if (layerrenderer instanceof LayerCustomHead) {
            this.removeLayer(layerrenderer);
            this.addLayer((LayerRenderer)new LayerCustomHead(this.zombieVillagerModel.bipedHead));
        }
        this.removeLayer((LayerRenderer)layerbipedarmor);
        this.addLayer((LayerRenderer)new LayerVillagerArmor((RendererLivingEntity)this));
        this.field_177121_n = Lists.newArrayList((Iterable)this.layerRenderers);
    }

    public void func_180579_a(final EntityZombie p_180579_1_, final double p_180579_2_, final double p_180579_4_, final double p_180579_6_, final float p_180579_8_, final float p_180579_9_) {
        this.func_82427_a(p_180579_1_);
        super.doRender((EntityLiving)p_180579_1_, p_180579_2_, p_180579_4_, p_180579_6_, p_180579_8_, p_180579_9_);
    }

    protected ResourceLocation func_180578_a(final EntityZombie p_180578_1_) {
        return p_180578_1_.isVillager() ? RenderBendsZombie.zombieVillagerTextures : RenderBendsZombie.zombieTextures;
    }

    private void func_82427_a(final EntityZombie zombie) {
        if (zombie.isVillager()) {
            this.mainModel = (ModelBase)this.zombieVillagerModel;
            this.layerRenderers = this.field_177121_n;
        }
        else {
            this.mainModel = (ModelBase)this.field_82434_o;
            this.layerRenderers = this.field_177122_o;
        }
        this.modelBipedMain = (ModelBiped)this.mainModel;
    }

    protected void rotateCorpse(final EntityZombie bat, final float p_77043_2_, float p_77043_3_, final float partialTicks) {
        if (bat.isConverting()) {
            p_77043_3_ += (float)(Math.cos(bat.ticksExisted * 3.25) * 3.141592653589793 * 0.25);
        }
        super.rotateCorpse((EntityLivingBase)bat, p_77043_2_, p_77043_3_, partialTicks);
    }

    protected ResourceLocation getEntityTexture(final EntityLiving entity) {
        return this.func_180578_a((EntityZombie)entity);
    }

    public void doRender(final EntityLiving entity, final double x, final double y, final double z, final float entityYaw, final float partialTicks) {
        this.func_180579_a((EntityZombie)entity, x, y, z, entityYaw, partialTicks);
    }

    protected void rotateCorpse(final EntityLivingBase bat, final float p_77043_2_, final float p_77043_3_, final float partialTicks) {
        this.rotateCorpse((EntityZombie)bat, p_77043_2_, p_77043_3_, partialTicks);
    }

    public void doRender(final EntityLivingBase entity, final double x, final double y, final double z, final float entityYaw, final float partialTicks) {
        this.func_180579_a((EntityZombie)entity, x, y, z, entityYaw, partialTicks);
    }

    protected ResourceLocation getEntityTexture(final Entity entity) {
        return this.func_180578_a((EntityZombie)entity);
    }

    public void doRender(final Entity entity, final double x, final double y, final double z, final float entityYaw, final float partialTicks) {
        this.func_180579_a((EntityZombie)entity, x, y, z, entityYaw, partialTicks);
    }

    static {
        zombieTextures = new ResourceLocation("textures/entity/zombie/zombie.png");
        zombieVillagerTextures = new ResourceLocation("textures/entity/zombie/zombie_villager.png");
    }
}
