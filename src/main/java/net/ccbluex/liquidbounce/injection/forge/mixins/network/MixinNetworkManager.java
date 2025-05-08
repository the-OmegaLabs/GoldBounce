/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.network;

import com.viaversion.viarewind.protocol.v1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.connection.UserConnectionImpl;
import com.viaversion.viaversion.protocol.ProtocolPipelineImpl;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ServerboundPackets1_9;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import de.florianmichael.vialoadingbase.netty.event.CompressionReorderEvent;
import de.florianmichael.viamcp.MCPVLBPipeline;
import de.florianmichael.viamcp.ViaMCP;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.ccbluex.liquidbounce.event.EventManager;
import net.ccbluex.liquidbounce.event.EventState;
import net.ccbluex.liquidbounce.event.PacketEvent;
import net.ccbluex.liquidbounce.utils.PPSCounter;
import net.ccbluex.liquidbounce.utils.packet.BlinkUtils;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.InetAddress;

import static net.minecraft.network.NetworkManager.CLIENT_EPOLL_EVENTLOOP;
import static net.minecraft.network.NetworkManager.CLIENT_NIO_EVENTLOOP;

@Mixin(NetworkManager.class)
public class MixinNetworkManager {
    @Shadow
    private Channel channel;
    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void send(Packet<?> packet, CallbackInfo callback) {
        final PacketEvent event = new PacketEvent(packet, EventState.SEND);
        EventManager.INSTANCE.callEvent(event);

        if (event.isCancelled()) {
            callback.cancel();
            return;
        }

        if (!BlinkUtils.onPacket(packet)) {
            callback.cancel();
            return;
        }

        PPSCounter.INSTANCE.registerType(PPSCounter.PacketType.SEND);

        if (packet instanceof C08PacketPlayerBlockPlacement) {
            C08PacketPlayerBlockPlacement cp = (C08PacketPlayerBlockPlacement) packet;
            if (cp.getPosition().getY() == -2) {
                UserConnection connection = Via.getManager().getConnectionManager().getConnections().stream().findFirst().orElse(null);
                PacketWrapper offHand = PacketWrapper.create(ServerboundPackets1_9.USE_ITEM, connection);
                offHand.write(Types.VAR_INT, 1);
                //Min.playerController.syncCurrentPlayItem();
                offHand.sendToServer(Protocol1_9To1_8.class);
                callback.cancel();
            }
        }

        if (packet instanceof C0APacketAnimation && ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_12_2)) {
            UserConnection connection = Via.getManager().getConnectionManager().getConnections().stream().findFirst().orElse(null);
            PacketWrapper offHand = PacketWrapper.create(ServerboundPackets1_9.SWING, connection);
            offHand.write(Types.VAR_INT, 1);
            offHand.sendToServer(Protocol1_9To1_8.class);
            callback.cancel();
        }
    }

    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    private void read(ChannelHandlerContext context, Packet<?> packet, CallbackInfo callback) {
        final PacketEvent event = new PacketEvent(packet, EventState.RECEIVE);
        EventManager.INSTANCE.callEvent(event);

        if (event.isCancelled()) {
            callback.cancel();
            return;
        }

        PPSCounter.INSTANCE.registerType(PPSCounter.PacketType.RECEIVED);
    }
    @Inject(method = "createNetworkManagerAndConnect", at = @At("HEAD"), cancellable = true)
    private static void createNetworkManagerAndConnect(InetAddress p_createNetworkManagerAndConnect_0_, int p_createNetworkManagerAndConnect_1_, boolean p_createNetworkManagerAndConnect_2_, CallbackInfoReturnable<NetworkManager> cir) {
        NetworkManager networkmanager = new NetworkManager(EnumPacketDirection.CLIENTBOUND);
        Class<? extends Channel> oclass;
        LazyLoadBase<?> lazyloadbase;

        if (Epoll.isAvailable() && p_createNetworkManagerAndConnect_2_) {
            oclass = EpollSocketChannel.class;
            lazyloadbase = CLIENT_EPOLL_EVENTLOOP;
        } else {
            oclass = NioSocketChannel.class;
            lazyloadbase = CLIENT_NIO_EVENTLOOP;
        }

        new Bootstrap()
                .group((EventLoopGroup)lazyloadbase.getValue())
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel p_initChannel_1_) throws Exception {
                        try {
                            p_initChannel_1_.config().setOption(ChannelOption.TCP_NODELAY, true);
                        } catch (ChannelException var3) { }
                        p_initChannel_1_.pipeline().addLast("timeout", new ReadTimeoutHandler(30))
                                .addLast("splitter", new MessageDeserializer2())
                                .addLast("decoder", new MessageDeserializer(EnumPacketDirection.CLIENTBOUND))
                                .addLast("prepender", new MessageSerializer2())
                                .addLast("encoder", new MessageSerializer(EnumPacketDirection.SERVERBOUND))
                                .addLast("packet_handler", networkmanager);
                        if (p_initChannel_1_ instanceof SocketChannel && ViaLoadingBase.getInstance().getTargetVersion().getVersion() != ViaMCP.NATIVE_VERSION) {
                            UserConnection user = new UserConnectionImpl(p_initChannel_1_, true);
                            new ProtocolPipelineImpl(user);
                            p_initChannel_1_.pipeline().addLast(new MCPVLBPipeline(user));
                        }
                    }
                })
                .channel(oclass)
                .connect(p_createNetworkManagerAndConnect_0_, p_createNetworkManagerAndConnect_1_)
                .syncUninterruptibly();

        cir.setReturnValue(networkmanager);
    }
    @Inject(method = "setCompressionTreshold", at = @At("TAIL"))
    private void fireCompression(int p_setCompressionTreshold_1_, CallbackInfo ci) {
        channel.pipeline().fireUserEventTriggered(new CompressionReorderEvent());
    }
}