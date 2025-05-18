package net.ccbluex.liquidbounce.utils.mobends.client.model;

import net.ccbluex.liquidbounce.utils.mobends.util.SmoothVector3f;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.ReadableVector3f;

public class ModelRendererBends
extends ModelRenderer {
    public SmoothVector3f rotation = new SmoothVector3f();
    public SmoothVector3f pre_rotation = new SmoothVector3f();
    public float scaleX;
    public float scaleY;
    public float scaleZ;
    public int txOffsetX;
    public int txOffsetY;
    public boolean compiled;
    private int displayList;
    public boolean showChildIfHidden = false;

    public ModelRendererBends(ModelBase argModel) {
        super(argModel);
        this.scaleZ = 1.0f;
        this.scaleY = 1.0f;
        this.scaleX = 1.0f;
    }

    public ModelRendererBends(ModelBase argModel, String argName) {
        super(argModel, argName);
        this.scaleZ = 1.0f;
        this.scaleY = 1.0f;
        this.scaleX = 1.0f;
    }

    public ModelRendererBends(ModelBase argModel, int argTexOffsetX, int argTexOffsetY) {
        super(argModel, argTexOffsetX, argTexOffsetY);
        this.txOffsetX = argTexOffsetX;
        this.txOffsetY = argTexOffsetY;
        this.scaleZ = 1.0f;
        this.scaleY = 1.0f;
        this.scaleX = 1.0f;
    }

    public void updateBends(float argTicksPerFrame) {
        this.rotateAngleX = (float)((double)(this.rotation.getX() / 180.0f) * Math.PI);
        this.rotateAngleY = (float)((double)(this.rotation.getY() / 180.0f) * Math.PI);
        this.rotateAngleZ = (float)((double)(this.rotation.getZ() / 180.0f) * Math.PI);
    }

    public ModelRendererBends setShowChildIfHidden(boolean arg0) {
        this.showChildIfHidden = arg0;
        return this;
    }

    public void compileDisplayList(float p_78788_1_) {
        this.displayList = GLAllocation.generateDisplayLists(1);
        GL11.glNewList((int)this.displayList, (int)4864);
        WorldRenderer worldrenderer = Tessellator.getInstance().getWorldRenderer();
        for (int i = 0; i < this.cubeList.size(); ++i) {
            ((ModelBox)this.cubeList.get(i)).render(worldrenderer, p_78788_1_);
        }
        GL11.glEndList();
        this.compiled = true;
    }

    @Override
    public void render(float p_78785_1_) {
        this.updateBends(p_78785_1_);
        if (!this.compiled) {
            this.compileDisplayList(p_78785_1_);
        }
        GL11.glTranslatef((float)this.offsetX, (float)this.offsetY, (float)this.offsetZ);
        if (this.rotateAngleX == 0.0f && this.rotateAngleY == 0.0f && this.rotateAngleZ == 0.0f) {
            if (this.rotationPointX == 0.0f && this.rotationPointY == 0.0f && this.rotationPointZ == 0.0f) {
                GL11.glRotatef((float)(-this.pre_rotation.getY()), (float)0.0f, (float)1.0f, (float)0.0f);
                GL11.glRotatef((float)this.pre_rotation.getX(), (float)1.0f, (float)0.0f, (float)0.0f);
                GL11.glRotatef((float)this.pre_rotation.getZ(), (float)0.0f, (float)0.0f, (float)1.0f);
                GL11.glScalef((float)this.scaleX, (float)this.scaleY, (float)this.scaleZ);
                if (!this.isHidden & this.showModel) {
                    GL11.glCallList((int)this.displayList);
                }
                if ((this.showChildIfHidden || !this.isHidden & this.showModel) && this.childModels != null) {
                    for (int i = 0; i < this.childModels.size(); ++i) {
                        ((ModelRenderer)this.childModels.get(i)).render(p_78785_1_);
                    }
                }
            } else {
                GL11.glTranslatef((float)(this.rotationPointX * p_78785_1_), (float)(this.rotationPointY * p_78785_1_), (float)(this.rotationPointZ * p_78785_1_));
                GL11.glRotatef((float)(-this.pre_rotation.getY()), (float)0.0f, (float)1.0f, (float)0.0f);
                GL11.glRotatef((float)this.pre_rotation.getX(), (float)1.0f, (float)0.0f, (float)0.0f);
                GL11.glRotatef((float)this.pre_rotation.getZ(), (float)0.0f, (float)0.0f, (float)1.0f);
                GL11.glScalef((float)this.scaleX, (float)this.scaleY, (float)this.scaleZ);
                if (!this.isHidden & this.showModel) {
                    GL11.glCallList((int)this.displayList);
                }
                if ((this.showChildIfHidden || !this.isHidden & this.showModel) && this.childModels != null) {
                    for (int i = 0; i < this.childModels.size(); ++i) {
                        ((ModelRenderer)this.childModels.get(i)).render(p_78785_1_);
                    }
                }
                GL11.glTranslatef((float)(-this.rotationPointX * p_78785_1_), (float)(-this.rotationPointY * p_78785_1_), (float)(-this.rotationPointZ * p_78785_1_));
            }
        } else {
            GL11.glPushMatrix();
            GL11.glTranslatef((float)(this.rotationPointX * p_78785_1_), (float)(this.rotationPointY * p_78785_1_), (float)(this.rotationPointZ * p_78785_1_));
            GL11.glRotatef((float)(-this.pre_rotation.getY()), (float)0.0f, (float)1.0f, (float)0.0f);
            GL11.glRotatef((float)this.pre_rotation.getX(), (float)1.0f, (float)0.0f, (float)0.0f);
            GL11.glRotatef((float)this.pre_rotation.getZ(), (float)0.0f, (float)0.0f, (float)1.0f);
            if (this.rotateAngleZ != 0.0f) {
                GL11.glRotatef((float)(this.rotateAngleZ * 57.295776f), (float)0.0f, (float)0.0f, (float)1.0f);
            }
            if (this.rotateAngleY != 0.0f) {
                GL11.glRotatef((float)(this.rotateAngleY * 57.295776f), (float)0.0f, (float)1.0f, (float)0.0f);
            }
            if (this.rotateAngleX != 0.0f) {
                GL11.glRotatef((float)(this.rotateAngleX * 57.295776f), (float)1.0f, (float)0.0f, (float)0.0f);
            }
            GL11.glScalef((float)this.scaleX, (float)this.scaleY, (float)this.scaleZ);
            if (!this.isHidden & this.showModel) {
                GL11.glCallList((int)this.displayList);
            }
            if ((this.showChildIfHidden || !this.isHidden & this.showModel) && this.childModels != null) {
                for (int i = 0; i < this.childModels.size(); ++i) {
                    ((ModelRenderer)this.childModels.get(i)).render(p_78785_1_);
                }
            }
            GL11.glPopMatrix();
        }
        GL11.glTranslatef((float)(-this.offsetX), (float)(-this.offsetY), (float)(-this.offsetZ));
    }

    public void update(float p_78785_1_) {
        this.rotation.update(p_78785_1_);
        this.pre_rotation.update(p_78785_1_);
        this.updateBends(p_78785_1_);
    }

    @Override
    public void renderWithRotation(float p_78791_1_) {
        this.updateBends(p_78791_1_);
        super.renderWithRotation(p_78791_1_);
    }

    @Override
    public void postRender(float p_78794_1_) {
        this.updateBends(p_78794_1_);
        if (this.rotateAngleX == 0.0f && this.rotateAngleY == 0.0f && this.rotateAngleZ == 0.0f) {
            if (this.rotationPointX != 0.0f || this.rotationPointY != 0.0f || this.rotationPointZ != 0.0f) {
                GL11.glTranslatef((float)(this.rotationPointX * p_78794_1_), (float)(this.rotationPointY * p_78794_1_), (float)(this.rotationPointZ * p_78794_1_));
                GL11.glRotatef((float)(-this.pre_rotation.getY()), (float)0.0f, (float)1.0f, (float)0.0f);
                GL11.glRotatef((float)this.pre_rotation.getX(), (float)1.0f, (float)0.0f, (float)0.0f);
                GL11.glRotatef((float)this.pre_rotation.getZ(), (float)0.0f, (float)0.0f, (float)1.0f);
                GL11.glScalef((float)this.scaleX, (float)this.scaleY, (float)this.scaleZ);
            }
        } else {
            GL11.glTranslatef((float)(this.rotationPointX * p_78794_1_), (float)(this.rotationPointY * p_78794_1_), (float)(this.rotationPointZ * p_78794_1_));
            GL11.glRotatef((float)(-this.pre_rotation.getY()), (float)0.0f, (float)1.0f, (float)0.0f);
            GL11.glRotatef((float)this.pre_rotation.getX(), (float)1.0f, (float)0.0f, (float)0.0f);
            GL11.glRotatef((float)this.pre_rotation.getZ(), (float)0.0f, (float)0.0f, (float)1.0f);
            if (this.rotateAngleZ != 0.0f) {
                GL11.glRotatef((float)(this.rotateAngleZ * 57.295776f), (float)0.0f, (float)0.0f, (float)1.0f);
            }
            if (this.rotateAngleY != 0.0f) {
                GL11.glRotatef((float)(this.rotateAngleY * 57.295776f), (float)0.0f, (float)1.0f, (float)0.0f);
            }
            if (this.rotateAngleX != 0.0f) {
                GL11.glRotatef((float)(this.rotateAngleX * 57.295776f), (float)1.0f, (float)0.0f, (float)0.0f);
            }
            GL11.glScalef((float)this.scaleX, (float)this.scaleY, (float)this.scaleZ);
        }
    }

    public ModelRendererBends setPosition(float argX, float argY, float argZ) {
        this.rotationPointX = argX;
        this.rotationPointY = argY;
        this.rotationPointZ = argZ;
        return this;
    }

    public ModelRendererBends setOffset(float argX, float argY, float argZ) {
        this.offsetX = argX;
        this.offsetY = argY;
        this.offsetZ = argZ;
        return this;
    }

    public ModelRendererBends setScale(float argX, float argY, float argZ) {
        this.scaleX = argX;
        this.scaleY = argY;
        this.scaleZ = argZ;
        return this;
    }

    public ModelRendererBends resetScale() {
        this.scaleZ = 1.0f;
        this.scaleY = 1.0f;
        this.scaleX = 1.0f;
        return this;
    }

    public void sync(ModelRendererBends argBox) {
        if (argBox != null) {
            this.rotateAngleX = argBox.rotateAngleX;
            this.rotateAngleY = argBox.rotateAngleY;
            this.rotateAngleZ = argBox.rotateAngleZ;
            this.rotation.vOld.set((ReadableVector3f)argBox.rotation.vOld);
            this.rotation.completion.set((ReadableVector3f)argBox.rotation.completion);
            this.rotation.vFinal.set((ReadableVector3f)argBox.rotation.vFinal);
            this.rotation.vSmooth.set((ReadableVector3f)argBox.rotation.vSmooth);
            this.rotation.smoothness.set((ReadableVector3f)argBox.rotation.smoothness);
            this.pre_rotation.vOld.set((ReadableVector3f)argBox.pre_rotation.vOld);
            this.pre_rotation.completion.set((ReadableVector3f)argBox.pre_rotation.completion);
            this.pre_rotation.vFinal.set((ReadableVector3f)argBox.pre_rotation.vFinal);
            this.pre_rotation.vSmooth.set((ReadableVector3f)argBox.pre_rotation.vSmooth);
            this.pre_rotation.smoothness.set((ReadableVector3f)argBox.pre_rotation.smoothness);
            this.scaleX = argBox.scaleX;
            this.scaleY = argBox.scaleY;
            this.scaleZ = argBox.scaleZ;
        }
    }

    @Override
    public void addBox(float p_78790_1_, float p_78790_2_, float p_78790_3_, int p_78790_4_, int p_78790_5_, int p_78790_6_, float p_78790_7_) {
        this.cubeList.add(new ModelBoxBends((ModelRenderer)this, this.txOffsetX, this.txOffsetY, p_78790_1_, p_78790_2_, p_78790_3_, p_78790_4_, p_78790_5_, p_78790_6_, p_78790_7_));
    }

    public ModelBoxBends getBox() {
        return (ModelBoxBends)this.cubeList.get(0);
    }

    public ModelRendererBends offsetBox(float argX, float argY, float argZ) {
        this.getBox().offset(argX, argY, argZ);
        return this;
    }

    public ModelRendererBends offsetBox_Add(float argX, float argY, float argZ) {
        this.getBox().offset_add(argX, argY, argZ);
        return this;
    }

    public ModelRendererBends resizeBox(float argX, float argY, float argZ) {
        this.getBox().resize(argX, argY, argZ);
        return this;
    }

    public ModelRendererBends updateVertices() {
        this.getBox().updateVertexPositions(this);
        this.compiled = false;
        return this;
    }
}

