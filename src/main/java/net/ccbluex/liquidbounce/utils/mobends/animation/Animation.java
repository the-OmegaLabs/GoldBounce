package net.ccbluex.liquidbounce.utils.mobends.animation;

import net.ccbluex.liquidbounce.utils.mobends.data.EntityData;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.EntityLivingBase;

public abstract class Animation {
    public abstract void animate(EntityLivingBase var1, ModelBase var2, EntityData var3);

    public abstract String getName();
}

