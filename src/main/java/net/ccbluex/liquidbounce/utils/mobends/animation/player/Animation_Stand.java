package net.ccbluex.liquidbounce.utils.mobends.animation.player;

import net.ccbluex.liquidbounce.utils.mobends.animation.Animation;
import net.ccbluex.liquidbounce.utils.mobends.client.model.ModelRendererBends;
import net.ccbluex.liquidbounce.utils.mobends.client.model.entity.ModelBendsPlayer;
import net.ccbluex.liquidbounce.utils.mobends.data.Data_Player;
import net.ccbluex.liquidbounce.utils.mobends.data.EntityData;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.EntityLivingBase;
import org.lwjgl.util.vector.Vector3f;

public class Animation_Stand
extends Animation {
    @Override
    public String getName() {
        return "stand";
    }

    @Override
    public void animate(EntityLivingBase argEntity, ModelBase argModel, EntityData argData) {
        ModelBendsPlayer model = (ModelBendsPlayer)argModel;
        Data_Player data = (Data_Player)argData;
        ((ModelRendererBends)model.bipedBody).rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f), 0.5f);
        ((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothZ(2.0f, 0.2f);
        ((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothZ(-2.0f, 0.2f);
        ((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothX(0.0f, 0.1f);
        ((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothX(0.0f, 0.1f);
        ((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothY(5.0f);
        ((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothY(-5.0f);
        ((ModelRendererBends)model.bipedRightArm).rotation.setSmoothX(0.0f, 0.1f);
        ((ModelRendererBends)model.bipedLeftArm).rotation.setSmoothX(0.0f, 0.1f);
        model.bipedRightForeLeg.rotation.setSmoothX(4.0f, 0.1f);
        model.bipedLeftForeLeg.rotation.setSmoothX(4.0f, 0.1f);
        model.bipedRightForeArm.rotation.setSmoothX(-4.0f, 0.1f);
        model.bipedLeftForeArm.rotation.setSmoothX(-4.0f, 0.1f);
        ((ModelRendererBends)model.bipedHead).rotation.setX(model.headRotationX);
        ((ModelRendererBends)model.bipedHead).rotation.setY(model.headRotationY);
        ((ModelRendererBends)model.bipedBody).rotation.setSmoothX((float)((Math.cos(data.ticks / 10.0f) - 1.0) / 2.0) * -3.0f);
        ((ModelRendererBends)model.bipedLeftArm).rotation.setSmoothZ(-((float)((Math.cos((double)(data.ticks / 10.0f) + 1.5707963267948966) - 1.0) / 2.0)) * -5.0f);
        ((ModelRendererBends)model.bipedRightArm).rotation.setSmoothZ(-((float)((Math.cos((double)(data.ticks / 10.0f) + 1.5707963267948966) - 1.0) / 2.0)) * 5.0f);
    }
}

