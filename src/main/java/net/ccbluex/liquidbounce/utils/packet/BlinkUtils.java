package net.ccbluex.liquidbounce.utils.packet;
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance;
import net.ccbluex.liquidbounce.utils.client.PacketUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S00PacketKeepAlive;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class BlinkUtils implements MinecraftInstance {
    public static final BlinkUtils INSTANCE = new BlinkUtils();
    public static boolean blinking = false;

    private static final List<Class<?>> blackList = new ArrayList<>();
    private static final Map<Class<?>, Predicate<Packet<?>>> cancelReturnPredicateMap = new HashMap<>();
    private static final Map<Class<?>, Predicate<Packet<?>>> releaseReturnPredicateMap = new HashMap<>();
    private static final Map<Class<?>, Consumer<Packet<?>>> cancelActionMap = new HashMap<>();
    private static final Map<Class<?>, Consumer<Packet<?>>> releaseActionMap = new HashMap<>();
    private static final List<Class<?>> whitList = new ArrayList<>();

    public static LinkedBlockingQueue<Packet<?>> packets = new LinkedBlockingQueue<>();
    public static boolean passEvent = false;

    public static boolean blink(Class<?>... fliterPackets){
        if(blinking)
            return false;

      /*  blackList.addAll(Arrays.asList(classes));*/

        Arrays.asList(fliterPackets).forEach(e -> {
            blackList.add(e);
            cancelReturnPredicateMap.put(e, f -> true);
        });

        blinking = true;
        return true;
    }

    /*
     Fliter Packets
   */

    public static void addWhiteList(Class<?>... classes){
        whitList.addAll(Arrays.asList(classes));
    }


    public static void removeBlackList(Class<?> packetClazz){
        blackList.remove(packetClazz);
    }

    public static void resetBlackList(){
        blackList.clear();
    }

    /*
      Action
    */
    public static void setCancelReturnPredicate(Class<?> clazz, Predicate<Packet<?>> predicate){
        boolean isIN = false;

        for (Class<?> classes : cancelReturnPredicateMap.keySet()){
            if (classes == clazz) {
                isIN = true;
                break;
            }
        }
        if(isIN) {
            cancelReturnPredicateMap.replace(clazz, predicate);
        } else {
            cancelReturnPredicateMap.put(clazz, predicate);
        }
    }

    public static void setReleaseReturnPredicateMap(Class<?> clazz, Predicate<Packet<?>> predicate){
        boolean isIN = false;

        for (Class<?> classes : releaseReturnPredicateMap.keySet()){
            if (classes == clazz) {
                isIN = true;
                break;
            }
        }
        if(isIN) {
            releaseReturnPredicateMap.replace(clazz, predicate);
        } else {
            releaseReturnPredicateMap.put(clazz, predicate);
        }
    }

    public static void setCancelAction(Class<?> clazz, Consumer<Packet<?>> packetConsumer){
        boolean isIN = false;

        for (Class<?> classes : cancelActionMap.keySet()){
            if (classes == clazz) {
                isIN = true;
                break;
            }
        }
        if(isIN) {
            cancelActionMap.replace(clazz, packetConsumer);
        } else {
            cancelActionMap.put(clazz, packetConsumer);
        }
    }
    public static void setReleaseAction(Class<?> clazz, Consumer<Packet<?>> packetConsumer){
        boolean isIN = false;

        for (Class<?> classes : releaseActionMap.keySet()){
            if (classes == clazz) {
                isIN = true;
                break;
            }
        }
        if(isIN) {
            releaseActionMap.replace(clazz, packetConsumer);
        } else {
            releaseActionMap.put(clazz, packetConsumer);
        }
    }

    /*
      release Packet
     */
    public static void releasePacket(boolean sendOneTick){
        releasePacket(packets.size(),sendOneTick);
    }

    public static void releasePacket(){
        releasePacket(packets.size());
    }

    public static void releasePacket(int sendPackets){
        releasePacket(sendPackets,false);
    }

    public static void releasePacket(int sendPackets,boolean sendOneTick){
        int sends = 0;
        try {
            here:
            while (!packets.isEmpty()){
                Packet<?> packet = packets.take();

                if(packet instanceof S00PacketKeepAlive){
                    if(sendOneTick) {
                        break;
                    }
                    continue;
                }

                for (Map.Entry<Class<?>, Predicate<Packet<?>>> entries : releaseReturnPredicateMap.entrySet()){
                    if(entries.getKey().isAssignableFrom(packet.getClass())){
                        if(entries.getValue().test(packet)){
                            continue here;
                        }
                    }
                }

                releaseActionMap.forEach((key,value) ->{
                    if(key.isAssignableFrom(packet.getClass())){
                        value.accept(packet);
                    }
                });

                sends++;
 /*               if(noEvent) {
                    passEvent = true;
                    PacketUtils.sendPacketNoEvent(packet);
                    passEvent = false;
                } else {
                    passEvent = true;
                    mc.getNetHandler().addToSendQueue(packet);
                    passEvent = false;
                }*/
                PacketUtils.sendPacket(packet,false);
      /*          System.out.println(packet.getClass().getSimpleName());*/
                if(sends >= sendPackets)
                    break;
            }
        } catch (Exception exception){
            exception.printStackTrace();
        }
    }

    public static void stopBlink(){
        blinking = false;
        passEvent = false;

        releasePacket();

        blackList.clear();
        cancelReturnPredicateMap.clear();
        cancelActionMap.clear();
        releaseActionMap.clear();
        whitList.clear();
    }

    public static boolean onPacket(Packet<?> packet) {
        if (blinking && !passEvent) {

            cancelActionMap.forEach((aClass, packetConsumer) -> {
                if(aClass.isAssignableFrom(packet.getClass())){
                    packetConsumer.accept(packet);
                }
            });

            for (Class<?> clazz : blackList){
                if(clazz.isAssignableFrom(packet.getClass()))
                {
                    return true;
                }
            }

            for (Map.Entry<Class<?>, Predicate<Packet<?>>> entries : cancelReturnPredicateMap.entrySet()){
                if(entries.getKey().isAssignableFrom(packet.getClass())){
                    if(entries.getValue().test(packet)){
                        return true;
                    }
                }
            }

            if(!whitList.isEmpty() && !whitList.contains(packet.getClass()))
                return true;

            packets.add(packet);
            return false;
        }
        return true;
    }


    public void onTick() {
        if(mc.getNetHandler() == null) {
            stopBlink();
        }
        if (blinking)
            packets.add(new S00PacketKeepAlive()); //这玩意就拿来做个标记。
    }

    @Override
    public @NotNull Minecraft getMc() {
        return Minecraft.getMinecraft();
    }
}
