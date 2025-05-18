package net.ccbluex.liquidbounce.utils.mobends.animation.player;

import net.ccbluex.liquidbounce.utils.mobends.client.model.ModelRendererBends;
import net.ccbluex.liquidbounce.utils.mobends.client.model.entity.ModelBendsPlayer;
import net.ccbluex.liquidbounce.utils.mobends.data.Data_Player;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.util.vector.Vector3f;

public class Animation_Attack_Stance {
    public static void animate(EntityPlayer player, ModelBendsPlayer model, Data_Player data) {
        if (!data.isOnGround()) {
            return;
        }
        if (data.motion.x == 0.0f & data.motion.z == 0.0f) {
            model.renderRotation.setSmoothY(30.0f, 0.3f);
            Vector3f bodyRot = new Vector3f(0.0f, 0.0f, 0.0f);
            bodyRot.x = 20.0f;
            ((ModelRendererBends)model.bipedBody).rotation.setSmooth(bodyRot, 0.3f);
            ((ModelRendererBends)model.bipedHead).rotation.setY(model.headRotationY - 30.0f);
            ((ModelRendererBends)model.bipedHead).rotation.setX(model.headRotationX);
            ((ModelRendererBends)model.bipedHead).pre_rotation.setSmoothX(-bodyRot.x, 0.3f);
            ((ModelRendererBends)model.bipedHead).pre_rotation.setSmoothY(-bodyRot.y, 0.3f);
            ((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothX(-30.0f, 0.3f);
            ((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothX(-30.0f, 0.3f);
            ((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothY(-25.0f, 0.3f);
            ((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothZ(10.0f);
            ((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothZ(-10.0f);
            model.bipedRightForeLeg.rotation.setSmoothX(30.0f, 0.3f);
            model.bipedLeftForeLeg.rotation.setSmoothX(30.0f, 0.3f);
            ((ModelRendererBends)model.bipedRightArm).pre_rotation.setSmoothZ(60.0f, 0.3f);
            ((ModelRendererBends)model.bipedRightArm).rotation.setSmoothX(60.0f, 0.3f);
            ((ModelRendererBends)model.bipedLeftArm).rotation.setSmoothZ(20.0f, 0.3f);
            ((ModelRendererBends)model.bipedLeftArm).pre_rotation.setSmoothZ(-80.0f, 0.3f);
            model.bipedRightForeArm.rotation.setSmoothX(-20.0f, 0.3f);
            model.bipedLeftForeArm.rotation.setSmoothX(-60.0f, 0.3f);
            model.renderItemRotation.setSmoothX(65.0f, 0.3f);
            model.renderOffset.setSmoothY(-2.0f);
        } else if (player.isSprinting()) {
            ((ModelRendererBends)model.bipedBody).rotation.setSmoothY(20.0f, 0.3f);
            ((ModelRendererBends)model.bipedHead).rotation.setY(model.headRotationY - 20.0f);
            ((ModelRendererBends)model.bipedHead).rotation.setX(model.headRotationX - 15.0f);
            ((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothY(0.0f);
            ((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothY(0.0f);
            ((ModelRendererBends)model.bipedRightArm).rotation.setSmoothX(60.0f, 0.3f);
            model.renderItemRotation.setSmoothX(90.0f, 0.3f);
        }
    }
}

