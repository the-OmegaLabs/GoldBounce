package net.ccbluex.liquidbounce.utils.mobends.animation.player;

import net.ccbluex.liquidbounce.utils.mobends.client.model.ModelRendererBends;
import net.ccbluex.liquidbounce.utils.mobends.client.model.entity.ModelBendsPlayer;
import net.ccbluex.liquidbounce.utils.mobends.data.Data_Player;
import net.minecraft.entity.player.EntityPlayer;

public class Animation_Attack_PunchStance {
    public static void animate(EntityPlayer player, ModelBendsPlayer model, Data_Player data) {
        if (!(data.motion.x == 0.0f & data.motion.z == 0.0f)) {
            return;
        }
        model.renderRotation.setSmoothY(20.0f);
        model.renderOffset.setSmoothY(-2.0f);
        ((ModelRendererBends)model.bipedRightArm).rotation.setSmoothX(-90.0f, 0.3f);
        model.bipedRightForeArm.rotation.setSmoothX(-80.0f, 0.3f);
        ((ModelRendererBends)model.bipedLeftArm).rotation.setSmoothX(-90.0f, 0.3f);
        model.bipedLeftForeArm.rotation.setSmoothX(-80.0f, 0.3f);
        ((ModelRendererBends)model.bipedRightArm).rotation.setSmoothZ(20.0f, 0.3f);
        ((ModelRendererBends)model.bipedLeftArm).rotation.setSmoothZ(-20.0f, 0.3f);
        ((ModelRendererBends)model.bipedBody).rotation.setSmoothX(10.0f, 0.3f);
        ((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothX(-30.0f, 0.3f);
        ((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothX(-30.0f, 0.3f);
        ((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothY(-25.0f, 0.3f);
        ((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothZ(10.0f);
        ((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothZ(-10.0f);
        model.bipedRightForeLeg.rotation.setSmoothX(30.0f, 0.3f);
        model.bipedLeftForeLeg.rotation.setSmoothX(30.0f, 0.3f);
        ((ModelRendererBends)model.bipedHead).rotation.setY(model.headRotationY - 20.0f);
        ((ModelRendererBends)model.bipedHead).rotation.setX(model.headRotationX - 10.0f);
    }
}

