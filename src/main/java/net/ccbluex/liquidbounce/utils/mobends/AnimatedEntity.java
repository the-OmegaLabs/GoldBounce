package net.ccbluex.liquidbounce.utils.mobends;

import com.google.common.collect.Maps;
import net.ccbluex.liquidbounce.utils.mobends.animation.Animation;
import net.ccbluex.liquidbounce.utils.mobends.animation.player.*;
import net.ccbluex.liquidbounce.utils.mobends.animation.spider.Animation_WallClimb;
import net.ccbluex.liquidbounce.utils.mobends.client.renderer.entity.RenderBendsPlayer;
import net.ccbluex.liquidbounce.utils.mobends.client.renderer.entity.RenderBendsSpider;
import net.ccbluex.liquidbounce.utils.mobends.client.renderer.entity.RenderBendsZombie;
import net.ccbluex.liquidbounce.utils.mobends.util.BendsLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AnimatedEntity {
    public static List<AnimatedEntity> animatedEntities = new ArrayList<AnimatedEntity>();
    public static Map<String, RenderBendsPlayer> skinMap = Maps.newHashMap();
    public static RenderBendsPlayer playerRenderer;
    public static RenderBendsZombie zombieRenderer;
    public static RenderBendsSpider spiderRenderer;
    public String id;
    public String displayName;
    public Entity entity;
    public Class<? extends Entity> entityClass;
    public Render renderer;
    public List<Animation> animations = new ArrayList<Animation>();

    public AnimatedEntity(String argID, String argDisplayName, Entity argEntity, Class<? extends Entity> argClass, Render argRenderer) {
        this.id = argID;
        this.displayName = argDisplayName;
        this.entityClass = argClass;
        this.renderer = argRenderer;
        this.entity = argEntity;
    }

    public AnimatedEntity add(Animation argGroup) {
        this.animations.add(argGroup);
        return this;
    }

    public static void register() {
        BendsLogger.log("Registering Animated Entities...", BendsLogger.INFO);
        animatedEntities.clear();
        AnimatedEntity.registerEntity(new AnimatedEntity("player", "Player", Minecraft.getMinecraft().thePlayer, EntityPlayer.class, new RenderBendsPlayer(Minecraft.getMinecraft().getRenderManager())).add(new net.ccbluex.liquidbounce.utils.mobends.animation.player.Animation_Stand()).add(new net.ccbluex.liquidbounce.utils.mobends.animation.player.Animation_Walk()).add(new Animation_Sneak()).add(new Animation_Sprint()).add(new Animation_Jump()).add(new Animation_Attack()).add(new Animation_Swimming()).add(new Animation_Bow()).add(new Animation_Riding()).add(new Animation_Mining()).add(new Animation_Axe()));
        AnimatedEntity.registerEntity(new AnimatedEntity("zombie", "Zombie", new EntityZombie(null), EntityZombie.class, new RenderBendsZombie(Minecraft.getMinecraft().getRenderManager())).add(new net.ccbluex.liquidbounce.utils.mobends.animation.zombie.Animation_Stand()).add(new net.ccbluex.liquidbounce.utils.mobends.animation.zombie.Animation_Walk()));
        AnimatedEntity.registerEntity(new AnimatedEntity("spider", "Spider", new EntitySpider(null), EntitySpider.class, new RenderBendsSpider(Minecraft.getMinecraft().getRenderManager())).add(new net.ccbluex.liquidbounce.utils.mobends.animation.spider.Animation_OnGround()).add(new net.ccbluex.liquidbounce.utils.mobends.animation.spider.Animation_Jump()).add(new Animation_WallClimb()));
        playerRenderer = new RenderBendsPlayer(Minecraft.getMinecraft().getRenderManager());
        zombieRenderer = new RenderBendsZombie(Minecraft.getMinecraft().getRenderManager());
        spiderRenderer = new RenderBendsSpider(Minecraft.getMinecraft().getRenderManager());
        skinMap.put("default", playerRenderer);
        skinMap.put("slim", new RenderBendsPlayer(Minecraft.getMinecraft().getRenderManager(), true));
    }

    public static void registerEntity(AnimatedEntity argEntity) {
        animatedEntities.add(argEntity);
    }

    public Animation get(String argName) {
        for (Animation animation : this.animations) {
            if (!animation.getName().equalsIgnoreCase(argName)) continue;
            return animation;
        }
        return null;
    }

    public static AnimatedEntity getByEntity(Entity argEntity) {
        for (int i = 0; i < animatedEntities.size(); ++i) {
            if (!AnimatedEntity.animatedEntities.get((int)i).entityClass.isInstance(argEntity)) continue;
            return animatedEntities.get(i);
        }
        return null;
    }

    public static RenderBendsPlayer getPlayerRenderer(AbstractClientPlayer player) {
        String s2 = player.getSkinType();
        RenderBendsPlayer renderPlayer = skinMap.get(s2);
        return renderPlayer != null ? renderPlayer : playerRenderer;
    }
}

