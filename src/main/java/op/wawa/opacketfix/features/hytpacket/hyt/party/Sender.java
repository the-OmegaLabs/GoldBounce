package op.wawa.opacketfix.features.hytpacket.hyt.party;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

public class Sender {
    private static void sendJson(JSONObject json) {
        byte[] data = encode(json.toString());
        ByteBuf buf = Unpooled.wrappedBuffer(data);
        Minecraft.getMinecraft().getNetHandler().addToSendQueue(new C17PacketCustomPayload("VexView", new PacketBuffer(buf)));
    }
    public static void sendJson(String json) throws IOException {
        byte[] data = encode(json);
        ByteBuf buf = Unpooled.wrappedBuffer(data);
        FMLProxyPacket packet = new FMLProxyPacket(new PacketBuffer(buf), "VexView");
        Minecraft.getMinecraft().getNetHandler().addToSendQueue(packet);
    }


    private static byte[] encode(String json) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))) {
            try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
                GZIPOutputStream out = new GZIPOutputStream(bout);
                byte[] array = new byte[256];
                int read;
                while ((read = in.read(array)) >= 0) {
                    out.write(array, 0, read);
                }
                out.finish();
                return bout.toByteArray();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    public static void openGui() {
        sendJson(new JSONObject()
                .put("packet_sub_type", "null")
                .put("packet_data", "null")
                .put("packet_type", "opengui"));
    }

    public static void closeGui() {
        sendJson(new JSONObject()
                .put("packet_sub_type", "null")
                .put("packet_data", "null")
                .put("packet_type", "gui_close"));
    }

    public static void clickButton(String id) {
        openGui();
        sendJson(new JSONObject()
                .put("packet_sub_type", id)
                .put("packet_data", "null")
                .put("packet_type", "button"));
        closeGui();
    }
    public static String getButtionClickJson(String id) {
        return "{\"packet_sub_type\":\"" + id + "\",\"packet_data\":\"null\",\"packet_type\":\"button\"}";
    }


    public static void joinParty(String text, String fieldId, String id) {
        openGui();
        sendJson(new JSONObject()
                .put("packet_sub_type", fieldId)
                .put("packet_data", text)
                .put("packet_type", "fieldtext"));
        clickButton(id);
        closeGui();
    }
}
