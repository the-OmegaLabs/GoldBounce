package net.ccbluex.liquidbounce.bzym

import com.google.common.collect.Queues
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import java.nio.FloatBuffer
import java.util.Deque

/**
 * PoseStack 低版本兼容层
 *
 * 在滑氯雷他定视觉时遇到问题 低版本没有blaze3d
 * 所以我制作了这个PoseStack 低版本实现
 * 代码 by ChatGPT o3-mini
 * 在 Minecraft 1.8.9（LWJGL2 + 固定管线）中模拟新版 Blaze3D 的 PoseStack API：
 * - 在 CPU 端维护一个 Matrix4f 的矩阵栈（pose）和 Matrix3f 的法线矩阵栈（normal）
 * - 在 GPU 端通过 GL11 固定管线调用同步变换
 */
class PoseStack {
    // 内部存放 Pose 对象的双端队列，始终至少包含一个元素（初始单位矩阵）
    private val poseStack: Deque<Pose> = Queues.newArrayDeque<Pose>().apply {
        add(Pose(Matrix4f(), Matrix3f()))
    }

    /**
     * 将当前顶层 pose 矩阵上传给固定管线（GL_MODELVIEW）
     */
    private fun uploadMatrix(matrix: Matrix4f) {
        // 将矩阵转为 FloatBuffer
        val buf: FloatBuffer = BufferUtils.createFloatBuffer(16)
        matrix.get(buf)
        buf.flip()
        // 相乘到当前 GL 矩阵堆栈
        GL11.glMultMatrix(buf)
    }

    /**
     * 平移（double 版），内部转为 float 调用
     */
    fun translate(x: Double, y: Double, z: Double) {
        translate(x.toFloat(), y.toFloat(), z.toFloat())
    }

    /**
     * 平移（float 版）
     * - 更新软件栈顶 pose 矩阵
     * - 调用 glTranslatef 同步 GPU
     */
    fun translate(x: Float, y: Float, z: Float) {
        val top = poseStack.last()
        top.pose.translate(x, y, z)
        GL11.glTranslatef(x, y, z)
    }

    /**
     * 缩放（float 版）
     * - 更新 pose 矩阵
     * - 计算并更新法线矩阵 normal
     * - 调用 glScalef 同步 GPU
     */
    fun scale(x: Float, y: Float, z: Float) {
        val top = poseStack.last()

        // 先在 pose 矩阵上执行缩放
        top.pose.scale(x, y, z)

        // 法线矩阵调整：处理各向同性和负缩放情况
        if (x == y && y == z) {
            if (x > 0f) {
                // 正数均匀缩放，无需修改 normal
                GL11.glScalef(x, y, z)
                return
            }
            // 负数均匀缩放，翻转法线方向
            top.normal.scale(-1f)
        }
        // 非均匀缩放时按逆转置原则更新 normal
        val invX = 1f / x
        val invY = 1f / y
        val invZ = 1f / z
        // 快速计算逆立方根
        val scaleNorm = fastInvCubeRoot(invX * invY * invZ)
        top.normal.scale(invX * scaleNorm, invY * scaleNorm, invZ * scaleNorm)

        // 最后在 GPU 端执行缩放
        GL11.glScalef(x, y, z)
    }

    /**
     * 快速近似计算逆立方根（1 / cbrt(x)）
     * 使用标准库函数实现基础版本
     */
    private fun fastInvCubeRoot(x: Float): Float {
        if (x == 0f) return 0f // 处理零值边界情况
        val absX = Math.abs(x.toDouble())
        val sign = if (x < 0) -1 else 1
        return (sign / Math.cbrt(absX)).toFloat()
    }

    /**
     * 乘以四元数旋转
     * - 更新 pose 和 normal
     * - 将新 pose 上传至 GPU
     */
    fun mulPose(q: Quaternionf) {
        val top = poseStack.last()
        top.pose.rotate(q)
        top.normal.rotate(q)
        uploadMatrix(top.pose)
    }

    /**
     * 围绕指定点旋转
     * - 平移到旋转中心 → 旋转 → 平移回去
     * - 同步更新 pose、normal 并上传到 GPU
     */
    fun rotateAround(q: Quaternionf, cx: Float, cy: Float, cz: Float) {
        val top = poseStack.last()

        // 移动到中心
        top.pose.translate(cx, cy, cz)
        GL11.glTranslatef(cx, cy, cz)

        // 执行旋转
        top.pose.rotate(q)
        top.normal.rotate(q)
        uploadMatrix(top.pose)

        // 移回原位
        top.pose.translate(-cx, -cy, -cz)
        GL11.glTranslatef(-cx, -cy, -cz)
    }

    /**
     * 入栈：复制当前顶层的 pose & normal，入软件栈
     */
    fun pushPose() {
        val src = poseStack.last()
        poseStack.addLast(Pose(Matrix4f(src.pose), Matrix3f(src.normal)))
    }

    /**
     * 出栈：移除栈顶（至少保留一个初始元素）
     */
    fun popPose() {
        if (poseStack.size <= 1) {
            throw IllegalStateException("PoseStack underflow!")
        }
        poseStack.removeLast()
    }

    /**
     * 获取当前顶层 Pose 对象
     */
    fun last(): Pose = poseStack.last()

    /**
     * 清空检测：只有初始元素时返回 true
     */
    fun clear(): Boolean = poseStack.size == 1

    /**
     * 重置当前顶层为单位矩阵，并同步 GPU 端恢复 glLoadIdentity
     */
    fun setIdentity() {
        val top = poseStack.last()
        top.pose.identity()
        top.normal.identity()
        GL11.glLoadIdentity()
    }

    /**
     * 将给定矩阵与当前 pose 相乘，并上传至 GPU
     */
    fun mulPoseMatrix(matrix: Matrix4f) {
        val top = poseStack.last()
        top.pose.mul(matrix)
        uploadMatrix(matrix)
    }

    /**
     * 内部数据类：保存一个 4×4 pose 矩阵和对应的 3×3 normal 矩阵
     */
    data class Pose(
        val pose: Matrix4f,
        val normal: Matrix3f
    )
}
