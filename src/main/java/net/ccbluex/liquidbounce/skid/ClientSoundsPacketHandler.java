package net.ccbluex.liquidbounce.skid;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ChannelHandler.Sharable
public class ClientSoundsPacketHandler extends ChannelDuplexHandler {
    public static final ExecutorService executor = Executors.newFixedThreadPool(15);
    public static final ArrayList<String> blocks = new ArrayList<String>();
    private static final List<Integer> functionalRightClick = Arrays.asList(23, 25, 26, 36, 54, 61, 62, 63, 64, 68, 58, 69, 71, 77, 84, 85, 92, 93, 94, 96, 107, 113, 116, 117, 118, 122, 130, 137, 138, 140, 143, 145, 146, 149, 150, 151, 154, 167, 178, 183, 184, 185, 186, 187, 188, 189, 190, 191, 192, 193, 194, 195, 196);
    private static final List<Integer> replacedRightClick = Arrays.asList(6, 8, 9, 10, 11, 31, 32, 78, 106);
    public static long ping;

    public ClientSoundsPacketHandler() {
        super();
        ping = Minecraft.getMinecraft().getCurrentServerData().pingToServer + 500;
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object packet) throws Exception {
        // Ignore S29PacketSoundEffect of those blocks you placed
        try {
            if (packet instanceof S29PacketSoundEffect) {
                S29PacketSoundEffect blockPlacePacket = (S29PacketSoundEffect) packet;
                // ((S29PacketSoundEffect)packet).getPitch()==0.7936508f &&
                if (!ClientSoundsPacketHandler.blocks.contains(blockPlacePacket.getX() + " " + blockPlacePacket.getY() + " " + blockPlacePacket.getZ())) {
                    super.channelRead(context, packet);
                }
            } else {
                super.channelRead(context, packet);
            }
        } catch (ConcurrentModificationException e) {
            super.channelRead(context, packet);
        }
    }

    @Override
    public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) throws Exception {
        super.write(context, packet, channelPromise);
        // Use block placing packet instead of event manager because latter sucks
        if (packet instanceof C08PacketPlayerBlockPlacement && !Minecraft.getMinecraft().playerController.getCurrentGameType().isAdventure()) {
            ping = Minecraft.getMinecraft().getCurrentServerData().pingToServer + 500;
            executor.execute(() -> {
                C08PacketPlayerBlockPlacement blockpacket = (C08PacketPlayerBlockPlacement) packet;
                // Block direction=255 means nothing is placed
                if (blockpacket.getPlacedBlockDirection() != 255 && blockpacket.getStack() != null && blockpacket.getStack().getItem() instanceof ItemBlock) {
                    boolean notthisblock = true;
                    int blockID = Block.getIdFromBlock(Minecraft.getMinecraft().theWorld.getBlockState(blockpacket.getPosition()).getBlock());
                    if (functionalRightClick.contains(blockID) && !Minecraft.getMinecraft().thePlayer.isSneaking()) {
                        return;
                    }
                    if (replacedRightClick.contains(blockID)) {
                        notthisblock = false;
                    }
                    BlockPos blockpos = blockpacket.getPosition();
                    float x = (float) (blockpos.getX() + 0.5);
                    float y = (float) (blockpos.getY() + 0.5);
                    float z = (float) (blockpos.getZ() + 0.5);
                    String c = x + " " + y + " " + z;
                    blocks.add(c);
                    if (notthisblock) {
                        switch (blockpacket.getPlacedBlockDirection()) {
                            case 0:
                                y--;
                                break;
                            case 1:
                                y++;
                                break;
                            case 2:
                                z--;
                                break;
                            case 3:
                                z++;
                                break;
                            case 4:
                                x--;
                                break;
                            case 5:
                                x++;
                        }
                    }

                    float finalX = x;
                    float finalY = y;
                    float finalZ = z;
                    // Schedule the sound in main thread
                    Minecraft.getMinecraft().addScheduledTask(() -> Minecraft.getMinecraft().getSoundHandler().playSound(new PositionedSoundRecord(new ResourceLocation(((ItemBlock) blockpacket.getStack().getItem()).getBlock().stepSound.getPlaceSound()),
                            // The pitch of block placing is 0.7936508, don't question it
                            1f, 0.7936508f, finalX, finalY, finalZ)));
                    String d = x + " " + y + " " + z;
                    blocks.add(d);

                    // Delete the data after 600ms
                    try {
                        Thread.sleep(ping);
                        blocks.remove(c);
                        blocks.remove(d);
                        // Catching ConcurrentModificationException
                    } catch (Exception e) {
                    }
                }
            });
        }
    }

}
