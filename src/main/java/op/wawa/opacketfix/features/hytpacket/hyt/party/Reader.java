package op.wawa.opacketfix.features.hytpacket.hyt.party;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Reader {
    private String json;
    private String[] ele;

    public Reader(String json) {
        this.json = json;
        this.ele = json.split("<&>");
    }

    public String getInviter() {
        int index = this.containsString("\u9080\u8bf7\u4f60\u52a0\u5165\u961f\u4f0d");
        return index != -1 ? this.ele[index - 3].replace("\u00a76\uff1a<#>[txt]50", "").replace("\u00a76\u73a9\u5bb6 \u00a73\u00a7l", "") : null;
    }

    public String getJson() {
        return this.json;
    }

    private static String decode(byte[] data) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(data));
        byte[] array = new byte[256];

        int read;
        while((read = gzipInputStream.read(array)) >= 0) {
            byteArrayOutputStream.write(array, 0, read);
        }

        return byteArrayOutputStream.toString("UTF-8");
    }

    public int getButtonIndex(String btn) {
        for(int i = 0; i < this.ele.length; ++i) {
            String e = this.ele[i];
            if (e.endsWith("[but]" + btn)) {
                return i;
            }
        }

        return -1;
    }

    public boolean containsButton(String btn) {
        for(int i = 0; i < this.ele.length; ++i) {
            String e = this.ele[i];
            if (e.endsWith("[but]" + btn)) {
                return true;
            }
        }

        return false;
    }

    public int containsString(String s) {
        for(int i = 0; i < this.ele.length; ++i) {
            String e = this.ele[i];
            if (e.contains(s)) {
                return i;
            }
        }

        return -1;
    }

    public boolean containsButtons(String... btn) {
        String[] var2 = btn;
        int var3 = btn.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String e = var2[var4];
            if (!this.containsButton(e)) {
                return false;
            }
        }

        return true;
    }

    public String[] getEle() {
        return this.ele;
    }

    public String getButtonID(String btn) {
        int index = this.getButtonIndex(btn);
        return this.ele[index + 6];
    }

}
