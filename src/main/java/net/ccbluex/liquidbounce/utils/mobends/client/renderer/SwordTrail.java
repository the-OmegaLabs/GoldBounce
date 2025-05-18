package net.ccbluex.liquidbounce.utils.mobends.client.renderer;

import net.ccbluex.liquidbounce.features.module.modules.render.MoreBends;
import net.ccbluex.liquidbounce.utils.mobends.client.model.ModelRendererBends;
import net.ccbluex.liquidbounce.utils.mobends.client.model.entity.ModelBendsPlayer;
import net.ccbluex.liquidbounce.utils.mobends.util.GUtil;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.ReadableVector3f;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class SwordTrail {
    public List<TrailPart> trailPartList = new ArrayList<TrailPart>();

    public void reset() {
        this.trailPartList.clear();
    }

    public void render(ModelBendsPlayer model) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(MoreBends.INSTANCE.getTexture_NULL());
        GL11.glDepthFunc((int)515);
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glDisable((int)2884);
        GL11.glDisable((int)2896);
        GL11.glHint((int)3152, (int)4354);
        GL11.glShadeModel((int)7425);
        GL11.glPushMatrix();
        GL11.glBegin((int)7);
        for (int i = 0; i < this.trailPartList.size(); ++i) {
            TrailPart part = this.trailPartList.get(i);
            float alpha = part.ticksExisted / 5.0f;
            alpha = GUtil.max(alpha, 1.0f);
            alpha = 1.0f - alpha;
            GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)alpha);
            Vector3f[] point = new Vector3f[]{new Vector3f(0.0f, 0.0f, -8.0f + 8.0f * alpha), new Vector3f(0.0f, 0.0f, -8.0f - 8.0f * alpha)};
            GUtil.rotateX(point, part.itemRotation.getX());
            GUtil.rotateY(point, part.itemRotation.getY());
            GUtil.rotateZ(point, part.itemRotation.getZ());
            GUtil.translate(point, new Vector3f(-1.0f, -6.0f, 0.0f));
            GUtil.rotateX(point, part.foreArm.rotateAngleX / (float)Math.PI * 180.0f);
            GUtil.rotateY(point, part.foreArm.rotateAngleY / (float)Math.PI * 180.0f);
            GUtil.rotateZ(point, part.foreArm.rotateAngleZ / (float)Math.PI * 180.0f);
            GUtil.rotateX(point, part.foreArm.pre_rotation.getX());
            GUtil.rotateY(point, part.foreArm.pre_rotation.getY());
            GUtil.rotateZ(point, -part.foreArm.pre_rotation.getZ());
            GUtil.translate(point, new Vector3f(0.0f, -4.0f, 0.0f));
            GUtil.rotateX(point, part.arm.rotateAngleX / (float)Math.PI * 180.0f);
            GUtil.rotateY(point, part.arm.rotateAngleY / (float)Math.PI * 180.0f);
            GUtil.rotateZ(point, part.arm.rotateAngleZ / (float)Math.PI * 180.0f);
            GUtil.rotateX(point, part.arm.pre_rotation.getX());
            GUtil.rotateY(point, part.arm.pre_rotation.getY());
            GUtil.rotateZ(point, -part.arm.pre_rotation.getZ());
            GUtil.translate(point, new Vector3f(-5.0f, 10.0f, 0.0f));
            GUtil.rotateX(point, part.body.rotateAngleX / (float)Math.PI * 180.0f);
            GUtil.rotateY(point, part.body.rotateAngleY / (float)Math.PI * 180.0f);
            GUtil.rotateZ(point, part.body.rotateAngleZ / (float)Math.PI * 180.0f);
            GUtil.rotateX(point, part.body.pre_rotation.getX());
            GUtil.rotateY(point, part.body.pre_rotation.getY());
            GUtil.rotateZ(point, part.body.pre_rotation.getZ());
            GUtil.translate(point, new Vector3f(0.0f, 12.0f, 0.0f));
            GUtil.rotateX(point, part.renderRotation.getX());
            GUtil.rotateY(point, part.renderRotation.getY());
            GUtil.translate(point, part.renderOffset);
            if (i > 0) {
                GL11.glVertex3f((float)point[1].x, (float)point[1].y, (float)point[1].z);
                GL11.glVertex3f((float)point[0].x, (float)point[0].y, (float)point[0].z);
                GL11.glVertex3f((float)point[0].x, (float)point[0].y, (float)point[0].z);
                GL11.glVertex3f((float)point[1].x, (float)point[1].y, (float)point[1].z);
            } else {
                GL11.glVertex3f((float)point[0].x, (float)point[0].y, (float)point[0].z);
                GL11.glVertex3f((float)point[1].x, (float)point[1].y, (float)point[1].z);
            }
            if (i != this.trailPartList.size() - 1) continue;
            GL11.glVertex3f((float)point[1].x, (float)point[1].y, (float)point[1].z);
            GL11.glVertex3f((float)point[0].x, (float)point[0].y, (float)point[0].z);
        }
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glEnable((int)3553);
        GL11.glDisable((int)2884);
        GL11.glEnable((int)2896);
    }

    public void add(ModelBendsPlayer argModel) {
        TrailPart newPart = new TrailPart(argModel);
        newPart.body.sync((ModelRendererBends)argModel.bipedBody);
        newPart.body.setPosition(argModel.bipedBody.rotationPointX, argModel.bipedBody.rotationPointY, argModel.bipedBody.rotationPointZ);
        newPart.body.setOffset(argModel.bipedBody.offsetX, argModel.bipedBody.offsetY, argModel.bipedBody.offsetZ);
        newPart.arm.sync((ModelRendererBends)argModel.bipedRightArm);
        newPart.arm.setPosition(argModel.bipedRightArm.rotationPointX, argModel.bipedRightArm.rotationPointY, argModel.bipedRightArm.rotationPointZ);
        newPart.arm.setOffset(argModel.bipedRightArm.offsetX, argModel.bipedRightArm.offsetY, argModel.bipedRightArm.offsetZ);
        newPart.foreArm.sync(argModel.bipedRightForeArm);
        newPart.foreArm.setPosition(argModel.bipedRightForeArm.rotationPointX, argModel.bipedRightForeArm.rotationPointY, argModel.bipedRightForeArm.rotationPointZ);
        newPart.foreArm.setOffset(argModel.bipedRightForeArm.offsetX, argModel.bipedRightForeArm.offsetY, argModel.bipedRightForeArm.offsetZ);
        newPart.renderOffset.set((ReadableVector3f)argModel.renderOffset.vSmooth);
        newPart.renderRotation.set((ReadableVector3f)argModel.renderRotation.vSmooth);
        newPart.itemRotation.set((ReadableVector3f)argModel.renderItemRotation.vSmooth);
        this.trailPartList.add(newPart);
    }

    public void update(float argPartialTicks) {
        int i;
        for (i = 0; i < this.trailPartList.size(); ++i) {
            this.trailPartList.get((int)i).ticksExisted += argPartialTicks;
        }
        for (i = 0; i < this.trailPartList.size(); ++i) {
            if (!(this.trailPartList.get((int)i).ticksExisted > 20.0f)) continue;
            this.trailPartList.remove(this.trailPartList.get(i));
        }
    }

    public class TrailPart {
        public ModelRendererBends body;
        public ModelRendererBends arm;
        public ModelRendererBends foreArm;
        public Vector3f renderRotation = new Vector3f();
        public Vector3f renderOffset = new Vector3f();
        public Vector3f itemRotation = new Vector3f();
        float ticksExisted;

        public TrailPart(ModelBendsPlayer argModel) {
            this.body = new ModelRendererBends(argModel);
            this.arm = new ModelRendererBends(argModel);
            this.foreArm = new ModelRendererBends(argModel);
        }
    }
}

