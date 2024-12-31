/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.skid.fdpx

import net.minecraft.block.Block
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.math.*


fun Float.toRadians() = this * 0.017453292f
fun Float.toRadiansD() = toRadians().toDouble()
fun Float.toDegrees() = this * 57.29578f
fun Float.toDegreesD() = toDegrees().toDouble()

fun Double.toRadians() = this * 0.017453292
fun Double.toRadiansF() = toRadians().toFloat()
fun Double.toDegrees() = this * 57.295779513
fun Double.toDegreesF() = toDegrees().toFloat()

fun Vec3.toFloatTriple() = Triple(xCoord.toFloat(), yCoord.toFloat(), zCoord.toFloat())
val RenderManager.renderPos
    get() = Vec3(renderPosX, renderPosY, renderPosZ)
operator fun Vec3.plus(vec: Vec3): Vec3 = add(vec)
operator fun Vec3.minus(vec: Vec3): Vec3 = subtract(vec)
operator fun Vec3.times(number: Double) = Vec3(xCoord * number, yCoord * number, zCoord * number)
operator fun Vec3.div(number: Double) = times(1 / number)

fun ClosedFloatingPointRange<Float>.lerpWith(t: Float) = start + (endInclusive - start) * t

object MathUtils {

    const val DEGREES_TO_RADIANS = 0.017453292519943295

    const val RADIANS_TO_DEGREES = 57.29577951308232
    fun radians(degrees: Double): Double {
        return degrees * Math.PI / 180
    }

    @JvmStatic
    fun randomizeDouble(min: Double, max: Double): Double {
        return Math.random() * (max - min) + min
    }

    fun getDistance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        return sqrt((x1 - x2).pow((2).toDouble()) + (y1 - y2).pow((2).toDouble()))
    }
    // TODO: Solve the coordinates of the intersection of two circles (there will be time at the end of the month)（TargetStrafe）
    // 2 + 2 is 4 - 1  thats 3 quick maffs

    fun lerp(a: Array<Double>, b: Array<Double>, t: Double) = arrayOf(a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t)

    fun lerp(pct: Double, start: Double, end: Double): Double {
        return start + pct * (end - start)
    }

    fun normalize(value: Float, min: Float, max: Float): Float {
        return (value - min) / (max - min)
    }

    fun interporate(p_219803_0_: Double, p_219803_2_: Double, p_219803_4_: Double): Double {
        return p_219803_2_ + p_219803_0_ * (p_219803_4_ - p_219803_2_)
    }

    @JvmStatic
    fun lerp(min: Float, max: Float, delta: Float): Float {
        return min + (max - min) * delta
    }

    fun distanceSq(a: Array<Double>, b: Array<Double>): Double = (a[0] - b[0]).pow(2) + (a[1] - b[1]).pow(2)

    fun distanceToSegmentSq(p: Array<Double>, v: Array<Double>, w: Array<Double>): Double {
        val l2 = distanceSq(v, w)
        if (l2 == 0.0) {
            return distanceSq(p, v)
        }
        return distanceSq(p, lerp(v, w, (((p[0] - v[0]) * (w[0] - v[0]) + (p[1] - v[1]) * (w[1] - v[1])) / l2).coerceAtMost(1.0).coerceAtLeast(0.0)))
    }

    val AxisAlignedBB.center
        get() = lerpWith(0.5)

    fun Block.lerpWith(x: Double, y: Double, z: Double) = Vec3(
        blockBoundsMinX + (blockBoundsMaxX - blockBoundsMinX) * x,
        blockBoundsMinY + (blockBoundsMaxY - blockBoundsMinY) * y,
        blockBoundsMinZ + (blockBoundsMaxZ - blockBoundsMinZ) * z
    )

    fun AxisAlignedBB.lerpWith(x: Double, y: Double, z: Double) =
        Vec3(minX + (maxX - minX) * x, minY + (maxY - minY) * y, minZ + (maxZ - minZ) * z)

    fun AxisAlignedBB.lerpWith(point: Vec3) = lerpWith(point.xCoord, point.yCoord, point.zCoord)
    fun AxisAlignedBB.lerpWith(value: Double) = lerpWith(value, value, value)

    fun Double.inRange(base: Double, range: Double): Boolean {
        return this in base - range..base + range
    }

    @JvmStatic
    fun calcCurvePoint(points: Array<Array<Double>>, t: Double): Array<Double> {
        val cpoints = mutableListOf<Array<Double>>()
        for (i in 0 until (points.size - 1)) {
            cpoints.add(lerp(points[i], points[i + 1], t))
        }
        return if (cpoints.size == 1) {
            cpoints[0]
        } else {
            calcCurvePoint(cpoints.toTypedArray(), t)
        }
    }

    @JvmStatic
    fun getPointsOnCurve(points: Array<Array<Double>>, num: Int): Array<Array<Double>> {
        val cpoints = mutableListOf<Array<Double>>()
        for (i in 0 until num) {
            val t = i / (num - 1.0)
            cpoints.add(calcCurvePoint(points, t))
        }
        return cpoints.toTypedArray()
    }

    /**
     * Converts double to radians
     */
    fun Double.toRadians() = this * DEGREES_TO_RADIANS

    fun gaussian(x: Int, sigma: Float): Float {
        val s = sigma * sigma * 2

        return (1f / (sqrt(PI.toFloat() * s))) * exp(-(x * x) / s)
    }

    @JvmOverloads
    @JvmStatic
    fun simplifyPoints(
        points: Array<Array<Double>>,
        epsilon: Double,
        start: Int = 0,
        end: Int = points.size,
        outPoints: MutableList<Array<Double>> = mutableListOf()
    ): Array<Array<Double>> {
        val s = points[start]
        val e = points[end - 1]
        var maxDistSq = 0.0
        var maxNdx = 1
        for (i in (start + 1) until (end - 1)) {
            val distSq = distanceToSegmentSq(points[i], s, e)
            if (distSq > maxDistSq) {
                maxDistSq = distSq
                maxNdx = i
            }
        }

        // if that point is too far
        if (sqrt(maxDistSq) > epsilon) {
            // split
            simplifyPoints(points, epsilon, start, maxNdx + 1, outPoints)
            simplifyPoints(points, epsilon, maxNdx, end, outPoints)
        } else {
            // add the 2 end points
            outPoints.add(s)
            outPoints.add(e)
        }

        return outPoints.toTypedArray()
    }

    fun round(value: Double, inc: Double): Double {
        return if (inc == 0.0) value else if (inc == 1.0) Math.round(value).toDouble() else {
            val halfOfInc = inc / 2.0
            val floored = floor(value / inc) * inc
            if (value >= floored + halfOfInc) BigDecimal(ceil(value / inc) * inc)
                .toDouble() else BigDecimal(floored)
                .toDouble()
        }
    }

    fun calculateGaussianDistribution(x: Float, sigma: Float): Double {
        val random = Random()
        return sqrt(sigma.toDouble()) * random.nextGaussian() + x
    }

    fun roundToHalf(d: Double): Double {
        return Math.round(d * 2.0) / 2.0
    }

    fun interpolate(oldValue: Double, newValue: Double, interpolationValue: Double): Double {
        return oldValue + (newValue - oldValue) * interpolationValue
    }

    @JvmStatic
    fun interpolateFloat(oldValue: Float, newValue: Float, interpolationValue: Double): Float {
        return interpolate(oldValue.toDouble(), newValue.toDouble(), interpolationValue.toFloat().toDouble()).toFloat()
    }

    @JvmStatic
    fun interpolateInt(oldValue: Int, newValue: Int, interpolationValue: Double): Int {
        return interpolate(oldValue.toDouble(), newValue.toDouble(), interpolationValue.toFloat().toDouble()).toInt()
    }

    @JvmStatic
    fun calculateGaussianValue(x: Float, sigma: Float): Float {
        val PI = 3.141592653
        val output = 1.0 / sqrt(2.0 * PI * (sigma * sigma))
        return (output * exp(-(x * x) / (2.0 * (sigma * sigma)))).toFloat()
    }

    fun incValue(`val`: Double, inc: Double): Double {
        val one = 1.0 / inc
        return Math.round(`val` * one) / one
    }

    fun round(value: Double, places: Int): Double {
        require(places >= 0)
        var bd = BigDecimal(value)
        bd = bd.setScale(places, RoundingMode.HALF_UP)
        return bd.toDouble()
    }

}
