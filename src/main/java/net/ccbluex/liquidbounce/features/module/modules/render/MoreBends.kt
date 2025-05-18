package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.mobends.AnimatedEntity
import net.ccbluex.liquidbounce.utils.mobends.client.renderer.entity.RenderBendsPlayer
import net.ccbluex.liquidbounce.utils.mobends.client.renderer.entity.RenderBendsSpider
import net.ccbluex.liquidbounce.utils.mobends.client.renderer.entity.RenderBendsZombie
import net.ccbluex.liquidbounce.utils.mobends.data.Data_Player
import net.ccbluex.liquidbounce.utils.mobends.data.Data_Spider
import net.ccbluex.liquidbounce.utils.mobends.data.Data_Zombie
import net.ccbluex.liquidbounce.utils.mobends.data.EntityData
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.renderer.entity.RendererLivingEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.monster.EntitySpider
import net.minecraft.entity.monster.EntityZombie
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation
import org.lwjgl.util.vector.ReadableVector3f
import org.lwjgl.util.vector.Vector3f
import kotlin.math.max
import kotlin.math.min

object MoreBends : Module("MoreBends", Category.RENDER) {
    var partialTicks: Float = 0.0f
    var ticks: Float = 0.0f
    var ticksPerFrame: Float = 0.0f
    val texture_NULL: ResourceLocation = ResourceLocation("mobends/textures/white.png")
    override fun onEnable() {
        AnimatedEntity.register()
    }

    @EventTarget
    fun onRender3D(e: Render3DEvent) {
        try{
        var i: Int
        if (mc.theWorld == null) {
            return
        }
        i = 0
        while (i < Data_Player.dataList.size) {
            Data_Player.dataList.get(i).update(e.partialTicks)
            ++i
        }
        i = 0
        while (i < Data_Zombie.dataList.size) {
            Data_Zombie.dataList.get(i).update(e.partialTicks)
            ++i
        }
        i = 0
        while (i < Data_Spider.dataList.size) {
            Data_Spider.dataList.get(i).update(e.partialTicks)
            ++i
        }
        if (mc.thePlayer != null) {
            val newTicks: Float =
                mc.thePlayer.ticksExisted.toInt() as Float + e.partialTicks.toInt()
            if (!mc.theWorld.isRemote || !mc.isGamePaused()) {
                ticksPerFrame =
                    min(max(0.0f, newTicks - ticks), 1.0f)
                ticks = newTicks
            } else {
                ticksPerFrame = 0.0f
            }
        }} catch (e: Exception){}
    }

    @EventTarget
    fun onTick(event: GameTickEvent) {
        var entity: Entity?
        var data: EntityData
        var i: Int
        if (mc.theWorld == null) {
            return
        }
        i = 0
        while (i < Data_Player.dataList.size) {
            data = Data_Player.dataList.get(i)
            entity = mc.theWorld.getEntityByID(data.entityID)
            if (entity != null) {
                if (!data.entityType.equals(entity.getName(),true)) {
                    Data_Player.dataList.remove(data)
                    Data_Player.add(Data_Player(entity.getEntityId()))
                    ++i
                    continue
                }
                data.motion_prev.set(data.motion as ReadableVector3f?)
                data.motion.x = entity.posX.toFloat() - data.position.x
                data.motion.y = entity.posY.toFloat() - data.position.y
                data.motion.z = entity.posZ.toFloat() - data.position.z
                data.position = Vector3f(entity.posX.toFloat(), entity.posY.toFloat(), entity.posZ.toFloat())
                ++i
                continue
            }
            Data_Player.dataList.remove(data)
            ++i
        }
        i = 0
        while (i < Data_Zombie.dataList.size) {
            data = Data_Zombie.dataList.get(i)
            entity =
                mc.theWorld.getEntityByID((data as Data_Zombie).entityID)
            if (entity != null) {
                if (!(data as Data_Zombie).entityType.equals(entity.getName(),true)) {
                    Data_Zombie.dataList.remove(data)
                    Data_Zombie.add(Data_Zombie(entity.getEntityId()))
                    ++i
                    continue
                }
                (data as Data_Zombie).motion_prev.set((data as Data_Zombie).motion as ReadableVector3f?)
                (data as Data_Zombie).motion.x = entity.posX.toFloat() - (data as Data_Zombie).position.x
                (data as Data_Zombie).motion.y = entity.posY.toFloat() - (data as Data_Zombie).position.y
                (data as Data_Zombie).motion.z = entity.posZ.toFloat() - (data as Data_Zombie).position.z
                (data as Data_Zombie).position =
                    Vector3f(entity.posX.toFloat(), entity.posY.toFloat(), entity.posZ.toFloat())
                ++i
                continue
            }
            Data_Zombie.dataList.remove(data)
            ++i
        }
        i = 0
        while (i < Data_Spider.dataList.size) {
            data = Data_Spider.dataList.get(i)
            entity =
                mc.theWorld.getEntityByID((data as Data_Spider).entityID)
            if (entity != null) {
                if (!(data as Data_Spider).entityType.equals(entity.getName(),true)) {
                    Data_Spider.dataList.remove(data)
                    Data_Spider.add(Data_Spider(entity.getEntityId()))
                    ++i
                    continue
                }
                (data as Data_Spider).motion_prev.set((data as Data_Spider).motion as ReadableVector3f?)
                (data as Data_Spider).motion.x = entity.posX.toFloat() - (data as Data_Spider).position.x
                (data as Data_Spider).motion.y = entity.posY.toFloat() - (data as Data_Spider).position.y
                (data as Data_Spider).motion.z = entity.posZ.toFloat() - (data as Data_Spider).position.z
                (data as Data_Spider).position =
                    Vector3f(entity.posX.toFloat(), entity.posY.toFloat(), entity.posZ.toFloat())
                ++i
                continue
            }
            Data_Spider.dataList.remove(data)
            ++i
        }
    }

    fun onRenderLivingEvent(
        renderer: RendererLivingEntity<*>?,
        entity: EntityLivingBase?,
        x2: Double,
        y2: Double,
        z: Double,
        entityYaw: Float,
        partialTicks: Float
    ): Boolean {
        if (!this.state || renderer is RenderBendsPlayer || renderer is RenderBendsZombie || renderer is RenderBendsSpider) {
            return false
        }
        val animatedEntity: AnimatedEntity? = AnimatedEntity.getByEntity(entity)
        if (animatedEntity != null && (entity is EntityPlayer || entity is EntityZombie) || entity is EntitySpider) {
            if (entity is EntityPlayer) {
                val player = entity as AbstractClientPlayer
                AnimatedEntity.getPlayerRenderer(player).doRender(player, x2, y2, z, entityYaw, partialTicks)
            } else if (entity is EntityZombie) {
                val zombie = entity
                AnimatedEntity.zombieRenderer.doRender(zombie, x2, y2, z, entityYaw, partialTicks)
            } else {
                val spider = entity as EntitySpider
                AnimatedEntity.spiderRenderer.doRender(spider, x2, y2, z, entityYaw, partialTicks)
            }
            return true
        }
        return false
    }
}