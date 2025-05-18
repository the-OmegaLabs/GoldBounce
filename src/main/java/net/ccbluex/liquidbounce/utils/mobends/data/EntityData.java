package net.ccbluex.liquidbounce.utils.mobends.data;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.util.vector.Vector3f;

import java.util.List;

public class EntityData {
    public int entityID;
    public String entityType;
    public ModelBase model;
    public Vector3f position = new Vector3f();
    public Vector3f motion_prev = new Vector3f();
    public Vector3f motion = new Vector3f();
    public float ticks = 0.0f;
    public float ticksPerFrame = 0.0f;
    public float lastTicks = 0.0f;
    public float lastTicksPerFrame = 0.0f;
    public boolean updatedThisFrame = false;
    public float ticksAfterLiftoff = 0.0f;
    public float ticksAfterTouchdown = 0.0f;
    public float ticksAfterPunch = 0.0f;
    public float ticksAfterThrowup = 0.0f;
    public boolean alreadyPunched = false;
    public boolean onGround;

    public EntityData(int argEntityID) {
        this.entityID = argEntityID;
        this.entityType = Minecraft.getMinecraft().theWorld.getEntityByID(argEntityID) != null ? Minecraft.getMinecraft().theWorld.getEntityByID(argEntityID).getName() : "NULL";
        this.model = null;
    }

    public boolean canBeUpdated() {
        return !this.updatedThisFrame;
    }

    public boolean calcOnGround() {
        Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(this.entityID);
        if (entity == null) {
            return false;
        }
        AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox();
        double var1 = this.position.y + this.motion.y;
        int i = 0;
        List<AxisAlignedBB> list = entity.worldObj.getCollidingBoundingBoxes(entity, entity.getEntityBoundingBox().addCoord(0.0, -0.001f, 0.0));
        return i < list.size();
    }

    public boolean calcCollidedHorizontally() {
        Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(this.entityID);
        if (entity == null) {
            return false;
        }
        AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox();
        int i = 0;
        List<AxisAlignedBB> list = entity.worldObj.getCollidingBoundingBoxes(entity, entity.getEntityBoundingBox().addCoord(this.motion.x, 0.0, this.motion.z));
        return i < list.size();
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    public void update(float argPartialTicks) {
        if (this.getEntity() == null) {
            return;
        }
        this.ticksPerFrame = (float)Minecraft.getMinecraft().thePlayer.ticksExisted + argPartialTicks - this.ticks;
        this.ticks = (float)Minecraft.getMinecraft().thePlayer.ticksExisted + argPartialTicks;
        this.updatedThisFrame = false;
        if (this.calcOnGround() & !this.onGround) {
            this.onTouchdown();
            this.onGround = true;
        }
        if (!this.calcOnGround() & this.onGround | (this.motion_prev.y <= 0.0f && this.motion.y - this.motion_prev.y > 0.4f && this.ticksAfterLiftoff > 2.0f)) {
            this.onLiftoff();
            this.onGround = false;
        }
        if (this.getEntity().swingProgress > 0.0f) {
            if (!this.alreadyPunched) {
                this.onPunch();
                this.alreadyPunched = true;
            }
        } else {
            this.alreadyPunched = false;
        }
        if (this.motion_prev.y <= 0.0f && this.motion.y > 0.0f) {
            this.onThrowup();
        }
        if (!this.isOnGround()) {
            this.ticksAfterLiftoff += this.ticksPerFrame;
        }
        if (this.isOnGround()) {
            this.ticksAfterTouchdown += this.ticksPerFrame;
        }
        this.ticksAfterPunch += this.ticksPerFrame;
        this.ticksAfterThrowup += this.ticksPerFrame;
    }

    public EntityLivingBase getEntity() {
        if (Minecraft.getMinecraft().theWorld.getEntityByID(this.entityID) instanceof EntityLivingBase) {
            return (EntityLivingBase)Minecraft.getMinecraft().theWorld.getEntityByID(this.entityID);
        }
        return null;
    }

    public void onTouchdown() {
        this.ticksAfterTouchdown = 0.0f;
    }

    public void onLiftoff() {
        this.ticksAfterLiftoff = 0.0f;
    }

    public void onThrowup() {
        this.ticksAfterThrowup = 0.0f;
    }

    public void onPunch() {
        this.ticksAfterPunch = 0.0f;
    }
}

