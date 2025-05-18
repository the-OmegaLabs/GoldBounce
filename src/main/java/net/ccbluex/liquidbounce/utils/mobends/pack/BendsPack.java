package net.ccbluex.liquidbounce.utils.mobends.pack;


import net.ccbluex.liquidbounce.utils.mobends.client.model.ModelRendererBends;
import net.ccbluex.liquidbounce.utils.mobends.client.model.entity.ModelBendsPlayer;
import net.ccbluex.liquidbounce.utils.mobends.client.model.entity.ModelBendsSpider;
import net.ccbluex.liquidbounce.utils.mobends.client.model.entity.ModelBendsZombie;

import java.util.ArrayList;
import java.util.List;

public class BendsPack {
    public String filename;
    public String displayName;
    public String author;
    public String description;
    public static List<BendsTarget> targets = new ArrayList<BendsTarget>();

    public static BendsTarget getTargetByID(String argID) {
        for (BendsTarget target : targets) {
            if (!target.mob.equalsIgnoreCase(argID)) continue;
            return target;
        }
        return null;
    }

    public static void animate(ModelBendsPlayer model, String target, String anim) {
        BendsTarget bendsTarget = BendsPack.getTargetByID(target);
        if (bendsTarget == null) {
            return;
        }
        bendsTarget.applyToModel((ModelRendererBends)model.bipedBody, anim, "body");
        bendsTarget.applyToModel((ModelRendererBends)model.bipedHead, anim, "head");
        bendsTarget.applyToModel((ModelRendererBends)model.bipedLeftArm, anim, "leftArm");
        bendsTarget.applyToModel((ModelRendererBends)model.bipedRightArm, anim, "rightArm");
        bendsTarget.applyToModel((ModelRendererBends)model.bipedLeftLeg, anim, "leftLeg");
        bendsTarget.applyToModel((ModelRendererBends)model.bipedRightLeg, anim, "rightLeg");
        bendsTarget.applyToModel(model.bipedLeftForeArm, anim, "leftForeArm");
        bendsTarget.applyToModel(model.bipedRightForeArm, anim, "rightForeArm");
        bendsTarget.applyToModel(model.bipedLeftForeLeg, anim, "leftForeLeg");
        bendsTarget.applyToModel(model.bipedRightForeLeg, anim, "rightForeLeg");
        bendsTarget.applyToModel(model.renderItemRotation, anim, "itemRotation");
        bendsTarget.applyToModel(model.renderRotation, anim, "playerRotation");
    }

    public static void animate(ModelBendsZombie model, String target, String anim) {
        BendsTarget bendsTarget = BendsPack.getTargetByID(target);
        if (bendsTarget == null) {
            return;
        }
        bendsTarget.applyToModel((ModelRendererBends)model.bipedBody, anim, "body");
        bendsTarget.applyToModel((ModelRendererBends)model.bipedHead, anim, "head");
        bendsTarget.applyToModel((ModelRendererBends)model.bipedLeftArm, anim, "leftArm");
        bendsTarget.applyToModel((ModelRendererBends)model.bipedRightArm, anim, "rightArm");
        bendsTarget.applyToModel((ModelRendererBends)model.bipedLeftLeg, anim, "leftLeg");
        bendsTarget.applyToModel((ModelRendererBends)model.bipedRightLeg, anim, "rightLeg");
        bendsTarget.applyToModel((ModelRendererBends)model.bipedLeftForeArm, anim, "leftForeArm");
        bendsTarget.applyToModel((ModelRendererBends)model.bipedRightForeArm, anim, "rightForeArm");
        bendsTarget.applyToModel((ModelRendererBends)model.bipedLeftForeLeg, anim, "leftForeLeg");
        bendsTarget.applyToModel((ModelRendererBends)model.bipedRightForeLeg, anim, "rightForeLeg");
    }

    public static void animate(ModelBendsSpider model, String target, String anim) {
        BendsTarget bendsTarget = BendsPack.getTargetByID(target);
        if (bendsTarget == null) {
            return;
        }
        bendsTarget.applyToModel((ModelRendererBends)model.spiderBody, anim, "body");
        bendsTarget.applyToModel((ModelRendererBends)model.spiderNeck, anim, "neck");
        bendsTarget.applyToModel((ModelRendererBends)model.spiderHead, anim, "head");
        bendsTarget.applyToModel((ModelRendererBends)model.spiderLeg1, anim, "leg1");
        bendsTarget.applyToModel((ModelRendererBends)model.spiderLeg2, anim, "leg2");
        bendsTarget.applyToModel((ModelRendererBends)model.spiderLeg3, anim, "leg3");
        bendsTarget.applyToModel((ModelRendererBends)model.spiderLeg4, anim, "leg4");
        bendsTarget.applyToModel((ModelRendererBends)model.spiderLeg5, anim, "leg5");
        bendsTarget.applyToModel((ModelRendererBends)model.spiderLeg6, anim, "leg6");
        bendsTarget.applyToModel((ModelRendererBends)model.spiderLeg7, anim, "leg7");
        bendsTarget.applyToModel((ModelRendererBends)model.spiderLeg8, anim, "leg8");
        bendsTarget.applyToModel(model.spiderForeLeg1, anim, "foreLeg1");
        bendsTarget.applyToModel(model.spiderForeLeg2, anim, "foreLeg2");
        bendsTarget.applyToModel(model.spiderForeLeg3, anim, "foreLeg3");
        bendsTarget.applyToModel(model.spiderForeLeg4, anim, "foreLeg4");
        bendsTarget.applyToModel(model.spiderForeLeg5, anim, "foreLeg5");
        bendsTarget.applyToModel(model.spiderForeLeg6, anim, "foreLeg7");
        bendsTarget.applyToModel(model.spiderForeLeg7, anim, "foreLeg7");
        bendsTarget.applyToModel(model.spiderForeLeg8, anim, "foreLeg8");
    }
}

