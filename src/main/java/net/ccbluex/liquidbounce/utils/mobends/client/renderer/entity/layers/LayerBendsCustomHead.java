package net.ccbluex.liquidbounce.utils.mobends.client.renderer.entity.layers;

import com.mojang.authlib.GameProfile;
import net.ccbluex.liquidbounce.utils.mobends.client.model.entity.ModelBendsPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumFacing;

import java.util.UUID;

public class LayerBendsCustomHead
implements LayerRenderer<EntityLivingBase> {
    private final ModelBendsPlayer model;

    public LayerBendsCustomHead(ModelBendsPlayer p_i46120_1_) {
        this.model = p_i46120_1_;
    }

    @Override
    public void doRenderLayer(EntityLivingBase livingBase, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale) {
        ItemStack itemstack = livingBase.getCurrentArmor(3);
        if (itemstack != null && itemstack.getItem() != null) {
            float f7;
            boolean flag;
            Item item = itemstack.getItem();
            Minecraft minecraft = Minecraft.getMinecraft();
            GlStateManager.pushMatrix();
            if (livingBase.isSneaking()) {
                GlStateManager.translate(0.0f, 0.2f, 0.0f);
            }
            boolean bl = flag = livingBase instanceof EntityVillager || livingBase instanceof EntityZombie && ((EntityZombie)livingBase).isVillager();
            if (!flag && livingBase.isChild()) {
                f7 = 2.0f;
                float f8 = 1.4f;
                GlStateManager.scale(f8 / f7, f8 / f7, f8 / f7);
                GlStateManager.translate(0.0f, 16.0f * scale, 0.0f);
            }
            this.model.bipedBody.postRender(0.0625f);
            this.model.bipedHead.postRender(0.0625f);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            if (item instanceof ItemBlock) {
                f7 = 0.625f;
                GlStateManager.translate(0.0f, -0.25f, 0.0f);
                GlStateManager.rotate(180.0f, 0.0f, 1.0f, 0.0f);
                GlStateManager.scale(f7, -f7, -f7);
                if (flag) {
                    GlStateManager.translate(0.0f, 0.1875f, 0.0f);
                }
                minecraft.getItemRenderer().renderItem(livingBase, itemstack, ItemCameraTransforms.TransformType.HEAD);
            } else if (item == Items.skull) {
                f7 = 1.1875f;
                GlStateManager.scale(f7, -f7, -f7);
                if (flag) {
                    GlStateManager.translate(0.0f, 0.0625f, 0.0f);
                }
                GameProfile gameprofile = null;
                if (itemstack.hasTagCompound()) {
                    NBTTagCompound nbttagcompound = itemstack.getTagCompound();
                    if (nbttagcompound.hasKey("SkullOwner", 10)) {
                        gameprofile = NBTUtil.readGameProfileFromNBT(nbttagcompound.getCompoundTag("SkullOwner"));
                    } else if (nbttagcompound.hasKey("SkullOwner", 8)) {
                        gameprofile = TileEntitySkull.updateGameprofile(new GameProfile((UUID)null, nbttagcompound.getString("SkullOwner")));
                        nbttagcompound.setTag("SkullOwner", NBTUtil.writeGameProfile(new NBTTagCompound(), gameprofile));
                    }
                }
                TileEntitySkullRenderer.instance.renderSkull(-0.5f, 0.0f, -0.5f, EnumFacing.UP, 180.0f, itemstack.getMetadata(), gameprofile, -1);
            }
            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}

