package net.ccbluex.liquidbounce.bzym;

import com.google.gson.stream.MalformedJsonException;
import kotlin.Pair;
import kotlin.io.TextStreamsKt;
import kotlin.jvm.internal.Intrinsics;
import lombok.Setter;
import net.ccbluex.liquidbounce.features.module.modules.player.CollideFix;
import net.ccbluex.liquidbounce.utils.extensions.PlayerExtensionKt;
import net.ccbluex.liquidbounce.utils.reflection.ReflectionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Setter
public final class GlobalFeatures {
	private boolean clientWalkStatus;

	public boolean getClientWalkStatus () {
		return clientWalkStatus;
	}

	public void toggleWalk () {
		clientWalkStatus = !clientWalkStatus;
		Minecraft.getMinecraft().gameSettings.keyBindForward.pressed = clientWalkStatus;
	}
	public static boolean 逼() {
		if (CollideFix.INSTANCE.handleEvents()) {
			return true;
		} else {
			return false;
		}
	}
	/**
	 * 检测玩家的视线是否指向指定实体的碰撞箱。
	 *
	 * @param player      要检测的玩家。
	 * @param target      目标实体。
	 * @param maxDistance 最大检测距离。
	 * @return true 如果视线指向目标实体的碰撞箱；否则 false。
	 */
	public boolean isPlayerLookingAtEntity (@NotNull Entity target, double maxDistance) throws
			IllegalAccessException {
		Intrinsics.checkNotNullParameter(target, "target");

		EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
		Vec3 eyePos = PlayerExtensionKt.getEyes(player);
		Vec3 lookVec = player.getLook(1.0F);

		Vec3 endPos = eyePos.addVector(
				lookVec.xCoord * maxDistance, lookVec.yCoord * maxDistance,
				lookVec.zCoord * maxDistance
		                              );

		AxisAlignedBB boundingBox = ReflectionUtil.INSTANCE.getFieldValue(
				target, "boundingBox");
		if (boundingBox == null) {
			return false;
		}

		MovingObjectPosition result = boundingBox.expand(0.1D, 0.1D, 0.1D)
				.calculateIntercept(eyePos, endPos);
		return result != null;
	}

	@NotNull public Pair<Integer, Integer> getWindowScreenWidthHeight () {
		return new Pair<>(
				Minecraft.getMinecraft().displayWidth,
				Minecraft.getMinecraft().displayHeight
		);
	}

	@NotNull public String getIpLocation () {
		String apiUrl = "https://ip9.com.cn/get";

		try {
			URL url = new URL(apiUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");

			try (
					InputStream inputStream = connection.getInputStream();
					Reader reader = new InputStreamReader(
							inputStream,
							StandardCharsets.UTF_8
					); BufferedReader bufferedReader = new BufferedReader(reader)
			) {

				String response = TextStreamsKt.readText(bufferedReader);
				JSONObject jsonObject = new JSONObject(response);
				JSONObject data = jsonObject.getJSONObject("data");

				return data.getString("prov") + '人';
			}
		} catch (Exception e) {
			// Handle network issues or invalid responses
			if(e instanceof MalformedJsonException){
				return "json解析失败导致不知道地区的人";
			}
			if(e instanceof UnknownHostException){
				return "朝鲜人";
			}
			return "外星人";
		}
	}
}