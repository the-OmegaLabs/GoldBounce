package net.ccbluex.liquidbounce.utils.mobends.pack;


import net.ccbluex.liquidbounce.utils.mobends.client.model.ModelRendererBends;
import net.ccbluex.liquidbounce.utils.mobends.util.EnumAxis;
import net.ccbluex.liquidbounce.utils.mobends.util.SmoothVector3f;

import java.util.ArrayList;
import java.util.List;

public class BendsTarget {
    public String mob;
    public List<BendsAction> actions = new ArrayList<BendsAction>();
    public float visual_DeletePopUp;

    public BendsTarget(String argMob) {
        this.mob = argMob;
        this.visual_DeletePopUp = 0.0f;
    }

    public void applyToModel(ModelRendererBends box, String anim, String model) {
        for (int i = 0; i < this.actions.size(); ++i) {
            if (!((this.actions.get((int)i).anim.equalsIgnoreCase(anim) | this.actions.get((int)i).anim.equalsIgnoreCase("all")) & this.actions.get((int)i).model.equalsIgnoreCase(model))) continue;
            if (this.actions.get((int)i).prop == BendsAction.EnumBoxProperty.ROT) {
                box.rotation.setSmooth(this.actions.get((int)i).axis, this.actions.get(i).getNumber(this.actions.get((int)i).axis == EnumAxis.X ? box.rotation.vFinal.x : (this.actions.get((int)i).axis == EnumAxis.Y ? box.rotation.vFinal.y : box.rotation.vFinal.z)), this.actions.get((int)i).smooth);
                continue;
            }
            if (this.actions.get((int)i).prop == BendsAction.EnumBoxProperty.PREROT) {
                box.pre_rotation.setSmooth(this.actions.get((int)i).axis, this.actions.get(i).getNumber(this.actions.get((int)i).axis == EnumAxis.X ? box.pre_rotation.vFinal.x : (this.actions.get((int)i).axis == EnumAxis.Y ? box.pre_rotation.vFinal.y : box.pre_rotation.vFinal.z)), this.actions.get((int)i).smooth);
                continue;
            }
            if (this.actions.get((int)i).prop != BendsAction.EnumBoxProperty.SCALE) continue;
            if (this.actions.get((int)i).axis == null | this.actions.get((int)i).axis == EnumAxis.X) {
                box.scaleX = this.actions.get(i).getNumber(box.scaleX);
            }
            if (this.actions.get((int)i).axis == null | this.actions.get((int)i).axis == EnumAxis.Y) {
                box.scaleY = this.actions.get(i).getNumber(box.scaleY);
            }
            if (!(this.actions.get((int)i).axis == null | this.actions.get((int)i).axis == EnumAxis.Z)) continue;
            box.scaleZ = this.actions.get(i).getNumber(box.scaleZ);
        }
    }

    public void applyToModel(SmoothVector3f box, String anim, String model) {
        for (int i = 0; i < this.actions.size(); ++i) {
            if (!((this.actions.get((int)i).anim.equalsIgnoreCase(anim) | this.actions.get((int)i).anim.equalsIgnoreCase("all")) & this.actions.get((int)i).model.equalsIgnoreCase(model)) || this.actions.get((int)i).prop != BendsAction.EnumBoxProperty.ROT) continue;
            box.setSmooth(this.actions.get((int)i).axis, this.actions.get(i).getNumber(this.actions.get((int)i).axis == EnumAxis.X ? box.vFinal.x : (this.actions.get((int)i).axis == EnumAxis.Y ? box.vFinal.y : box.vFinal.z)), this.actions.get((int)i).smooth);
        }
    }
}

