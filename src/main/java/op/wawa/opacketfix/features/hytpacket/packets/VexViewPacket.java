package op.wawa.opacketfix.features.hytpacket.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import op.wawa.opacketfix.features.hytpacket.CustomPacket;
import op.wawa.opacketfix.features.hytpacket.hyt.party.ButtonDecoder;
import op.wawa.opacketfix.features.hytpacket.hyt.party.Reader;
import op.wawa.opacketfix.features.hytpacket.hyt.party.Sender;
import op.wawa.opacketfix.features.hytpacket.ui.party.GuiInit;
import op.wawa.opacketfix.features.hytpacket.ui.party.GuiInput;
import op.wawa.opacketfix.features.hytpacket.ui.party.GuiPartyManage;
import op.wawa.opacketfix.utils.ClientUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;


public class VexViewPacket implements CustomPacket {
    @Override
    public String getChannel() {
        return "VexView";
    }

    @Override
    public void process(ByteBuf byteBuf) {
        //byte[] data = new byte[byteBuf.readableBytes()];
        //byteBuf.readBytes(data);
        try {
            check(byteBuf);
        } catch (IOException e) {}
/*        ClientUtils.displayChatMessage("QuickMarco >> VexView");
        ButtonDecoder buttonDecoder = new ButtonDecoder(byteBuf);
        if (buttonDecoder.invited) {
            System.out.println(buttonDecoder.inviter);
            ChatComponentText textComponents = new ChatComponentText("\247b[QuickMarco] 收到来自\247a" + buttonDecoder.inviter + "\247b的邀请消息。\247e\247n(点此查看)");
            textComponents.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("\247e点击查看!")));
            textComponents.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/min hyt party handle " + buttonDecoder.getButton("同意").getId() + " " + buttonDecoder.getButton("拒绝").getId()));
            printToChat(textComponents);
        } else if (buttonDecoder.containsButtons("创建队伍", "申请入队")) {
            mc.displayGuiScreen(new GuiInit(buttonDecoder.getButton("创建队伍"), buttonDecoder.getButton("申请入队")));
        } else if (buttonDecoder.containsButtons("申请列表", "踢出队员", "离开队伍", "解散队伍")) {
            if (buttonDecoder.containsButton("邀请玩家")) {
                mc.displayGuiScreen(new GuiPartyManage(buttonDecoder.getButton("离开队伍"), buttonDecoder.getButton("解散队伍"), buttonDecoder.getButton("邀请玩家")));
            } else {
                mc.displayGuiScreen(new GuiPartyManage(buttonDecoder.getButton("离开队伍"), buttonDecoder.getButton("解散队伍"), null));
            }
        } else if (buttonDecoder.containsButton("手动输入")) {
            Sender.clickButton(buttonDecoder.getButton("手动输入").getId());
        } else if (buttonDecoder.containsButton("提交")) { // 提交
            mc.displayGuiScreen(new GuiInput(buttonDecoder.getElement(buttonDecoder.getButtonIndex("提交") - 1), buttonDecoder.getButton("提交")));
        }*/
    }


    private void check(ByteBuf byteBuf) throws IOException {
        ButtonDecoder buttonDecoder = new ButtonDecoder(byteBuf);
        //Reader reader = new Reader(json);
        String inviter;
        EntityPlayerSP player;
        if (buttonDecoder.containsButtons("创建队伍", "申请入队")) { //reader.containsButtons(new String[]{"创建队伍", "申请入队"})
            try {
                Sender.sendJson("{\"packet_sub_type\":\"null\",\"packet_data\":\"null\",\"packet_type\":\"opengui\"}");
                inviter = buttonDecoder.getButton("创建队伍").getId();
                Sender.sendJson(Sender.getButtionClickJson(inviter));
                Sender.sendJson("{\"packet_sub_type\":\"null\",\"packet_data\":\"null\",\"packet_type\":\"gui_close\"}");
            } catch (IOException var5) {
                var5.printStackTrace();
            }

            player = Minecraft.getMinecraft().thePlayer;
            player.addChatComponentMessage(new ChatComponentText("§c§l检\u6d4b\u5230\u60a8\u8f93\u5165\u4e86/kh \u5df2\u7ecf\u4e3a\u60a8\u81ea\u52a8\u521b\u5efa\u4e86\u961f\u4f0d,\u79bb\u5f00\u961f\u4f0d\u9000\u51fa\u6e38\u620f"));
        } else if (buttonDecoder.containsButtons("申请列表", "踢出队员", "离开队伍", "解散队伍")) {
            //player = Minecraft.getMinecraft().thePlayer;
            //player.addChatComponentMessage(new ChatComponentText("\u00a7c\u00a7l\u62b1\u6b49\u65e0\u6cd5\u9080\u8bf7\u73a9\u5bb6,\u8bf7\u8fd4\u56de\u5927\u5385"));
            if (buttonDecoder.containsButton("邀请玩家")) {
                mc.displayGuiScreen(new GuiPartyManage(buttonDecoder.getButton("离开队伍"), buttonDecoder.getButton("解散队伍"), buttonDecoder.getButton("邀请玩家")));
            } else {
                mc.displayGuiScreen(new GuiPartyManage(buttonDecoder.getButton("离开队伍"), buttonDecoder.getButton("解散队伍"), null));
            }
        } else if (buttonDecoder.containsButtons("申请列表", "踢出队员", "离开队伍", "解散队伍", "邀请玩家")) {
            player = Minecraft.getMinecraft().thePlayer;
            Sender.sendJson("{\"packet_sub_type\":\"null\",\"packet_data\":\"null\",\"packet_type\":\"opengui\"}");
            player.addChatComponentMessage(new ChatComponentText("\u00a7c\u00a7l\u5df2\u7ecf\u4e3a\u60a8\u8df3\u8f6c\u5230\u9080\u8bf7\u754c\u9762"));
            Sender.sendJson(Sender.getButtionClickJson(buttonDecoder.getButton("邀请玩家").getId()));
            Sender.sendJson("{\"packet_sub_type\":\"null\",\"packet_data\":\"null\",\"packet_type\":\"gui_close\"}");
        } else if (buttonDecoder.containsButton("手动输入")) {
            Sender.sendJson("{\"packet_sub_type\":\"null\",\"packet_data\":\"null\",\"packet_type\":\"opengui\"}");
            Sender.sendJson(Sender.getButtionClickJson(buttonDecoder.getButton("手动输入").getId()));
            Sender.sendJson("{\"packet_sub_type\":\"null\",\"packet_data\":\"null\",\"packet_type\":\"gui_close\"}");
        } else if (buttonDecoder.containsButton("提交")) {
            Sender.sendJson("{\"packet_sub_type\":\"null\",\"packet_data\":\"null\",\"packet_type\":\"opengui\"}");
            mc.displayGuiScreen(new GuiInput(buttonDecoder.getElement(buttonDecoder.getButtonIndex("提交") - 1), buttonDecoder.getButton("提交")));
        } else if (buttonDecoder.invited) {
            Sender.sendJson("{\"packet_sub_type\":\"null\",\"packet_data\":\"null\",\"packet_type\":\"opengui\"}");
            ChatComponentText textComponents = new ChatComponentText("\247bQuickMarco >> VexView 收到来自\247a" + buttonDecoder.inviter + "\247b的邀请消息。\247e\247n(点此查看)");
            textComponents.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("\247e点击查看!")));
            textComponents.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/min hyt party handle " + buttonDecoder.getButton("同意").getId() + " " + buttonDecoder.getButton("拒绝").getId()));
        }
    }

}
