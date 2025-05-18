package net.ccbluex.liquidbounce.utils.mobends.animation.player;

import net.ccbluex.liquidbounce.utils.mobends.client.model.ModelRendererBends;
import net.ccbluex.liquidbounce.utils.mobends.client.model.entity.ModelBendsPlayer;
import net.ccbluex.liquidbounce.utils.mobends.data.Data_Player;
import net.minecraft.entity.player.EntityPlayer;

public class Animation_Attack_Punch {
    public static void animate(EntityPlayer player, ModelBendsPlayer model, Data_Player data) {
        if (data.motion.x == 0.0f & data.motion.z == 0.0f) {
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
            if (data.fistPunchArm) {
                ((ModelRendererBends)model.bipedRightArm).pre_rotation.setSmoothZ(100.0f, 0.9f);
                ((ModelRendererBends)model.bipedRightArm).rotation.setSmoothX(-90.0f, 0.9f);
                ((ModelRendererBends)model.bipedRightArm).pre_rotation.setSmoothX(model.headRotationX, 0.9f);
                model.bipedRightForeArm.rotation.setSmoothX(0.0f, 0.9f);
                ((ModelRendererBends)model.bipedBody).rotation.setSmoothY(-20.0f, 0.6f);
                ((ModelRendererBends)model.bipedHead).rotation.setY(model.headRotationY - ((ModelRendererBends)model.bipedBody).rotation.getY() - 20.0f);
                ((ModelRendererBends)model.bipedHead).rotation.setX(model.headRotationX - 10.0f);
            } else {
                ((ModelRendererBends)model.bipedLeftArm).pre_rotation.setSmoothZ(-100.0f, 0.9f);
                ((ModelRendererBends)model.bipedLeftArm).pre_rotation.setSmoothY(30.0f, 0.9f);
                ((ModelRendererBends)model.bipedLeftArm).rotation.setSmoothX(-90.0f, 0.9f);
                ((ModelRendererBends)model.bipedLeftArm).pre_rotation.setSmoothX(model.headRotationX, 0.9f);
                model.bipedLeftForeArm.rotation.setSmoothX(0.0f, 0.9f);
                ((ModelRendererBends)model.bipedBody).rotation.setSmoothY(20.0f, 0.6f);
                ((ModelRendererBends)model.bipedHead).rotation.setY(model.headRotationY - 20.0f - ((ModelRendererBends)model.bipedBody).rotation.getY());
                ((ModelRendererBends)model.bipedHead).rotation.setX(model.headRotationX - 10.0f);
            }
        } else {
            ((ModelRendererBends)model.bipedRightArm).rotation.setSmoothX(-90.0f, 0.3f);
            model.bipedRightForeArm.rotation.setSmoothX(-80.0f, 0.3f);
            ((ModelRendererBends)model.bipedLeftArm).rotation.setSmoothX(-90.0f, 0.3f);
            model.bipedLeftForeArm.rotation.setSmoothX(-80.0f, 0.3f);
            ((ModelRendererBends)model.bipedRightArm).rotation.setSmoothZ(20.0f, 0.3f);
            ((ModelRendererBends)model.bipedLeftArm).rotation.setSmoothZ(-20.0f, 0.3f);
            ((ModelRendererBends)model.bipedBody).rotation.setSmoothX(10.0f, 0.3f);
            ((ModelRendererBends)model.bipedBody).rotation.setSmoothY(0.0f, 0.3f);
            if (data.fistPunchArm) {
                ((ModelRendererBends)model.bipedRightArm).pre_rotation.setSmoothZ(100.0f, 0.9f);
                ((ModelRendererBends)model.bipedRightArm).rotation.setSmoothX(-90.0f, 0.9f);
                ((ModelRendererBends)model.bipedRightArm).pre_rotation.setSmoothX(-20.0f + model.headRotationX, 0.9f);
                model.bipedRightForeArm.rotation.setSmoothX(0.0f, 0.9f);
                ((ModelRendererBends)model.bipedBody).rotation.setSmoothY(-20.0f, 0.6f);
                ((ModelRendererBends)model.bipedHead).rotation.setY(model.headRotationY + 20.0f);
                ((ModelRendererBends)model.bipedHead).rotation.setX(model.headRotationX - 10.0f);
            } else {
                ((ModelRendererBends)model.bipedLeftArm).pre_rotation.setSmoothZ(-100.0f, 0.9f);
                ((ModelRendererBends)model.bipedLeftArm).pre_rotation.setSmoothY(-15.0f, 0.9f);
                ((ModelRendererBends)model.bipedLeftArm).rotation.setSmoothX(-90.0f, 0.9f);
                ((ModelRendererBends)model.bipedLeftArm).pre_rotation.setSmoothX(-20.0f + model.headRotationX, 0.9f);
                model.bipedLeftForeArm.rotation.setSmoothX(0.0f, 0.9f);
                ((ModelRendererBends)model.bipedBody).rotation.setSmoothY(20.0f, 0.6f);
                ((ModelRendererBends)model.bipedHead).rotation.setY(model.headRotationY - 20.0f);
                ((ModelRendererBends)model.bipedHead).rotation.setX(model.headRotationX - 10.0f);
            }
        }
    }
}

