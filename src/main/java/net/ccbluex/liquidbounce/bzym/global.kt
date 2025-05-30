package net.ccbluex.liquidbounce.bzym

import net.ccbluex.liquidbounce.utils.MinecraftInstance.Companion.mc
import net.ccbluex.liquidbounce.utils.ReflectionUtil
import net.ccbluex.liquidbounce.utils.RotationUtils.scale
import net.ccbluex.liquidbounce.utils.extensions.eyes
import net.minecraft.entity.Entity
import net.minecraft.util.AxisAlignedBB
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class global {
    var clientWalkStatus = false

    fun toggleWalk() {
        clientWalkStatus = !clientWalkStatus  // 切换状态

        // 设置前进键状态
        mc.gameSettings.keyBindForward.pressed = clientWalkStatus

    }
    /**
     * 检测玩家的视线是否指向指定实体的碰撞箱。
     *
     * @param player 要检测的玩家。
     * @param target 目标实体。
     * @param maxDistance 最大检测距离。
     * @return true 如果视线指向目标实体的碰撞箱；否则 false。
     */
    fun isPlayerLookingAtEntity(target: Entity, maxDistance: Double): Boolean {
        val eyePos = mc.thePlayer.eyes // 玩家眼睛位置
        val lookVec = mc.thePlayer.getLook(1.0f)

        val endPos = eyePos.add(lookVec.scale(maxDistance))

        val targetBoundingBox = ReflectionUtil.getFieldValue<AxisAlignedBB>(target,"boundingBox").expand(0.1, 0.1, 0.1)

        val hitResult = targetBoundingBox.calculateIntercept(eyePos, endPos)

        return hitResult != null
    }
    fun getWindowScreenWidthHeight(): Pair<Int, Int> {
        val width = mc.displayWidth
        val height = mc.displayHeight
        return Pair(width,height)
    }
    fun getIpLocation(): String {
        val apiUrl = "https://ip9.com.cn/get"
        try {
            val url = URL(apiUrl)
            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "GET"

                inputStream.bufferedReader().use {
                    val response = it.readText()
                    val jsonObject = JSONObject(response)

                    val sub = jsonObject.getJSONObject("data")
                    return "${sub.getString("prov")}人"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return "外星人"
        }
    }

    fun main() {
        println(getIpLocation())
    }

}
