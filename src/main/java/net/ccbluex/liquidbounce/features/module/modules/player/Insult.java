/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */

package net.ccbluex.liquidbounce.features.module.modules.player;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import kotlin.ranges.IntRange;
import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.EntityKilledEvent;
import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.Category;
import net.ccbluex.liquidbounce.file.FileManager;
import net.ccbluex.liquidbounce.utils.FileUtils;
import net.ccbluex.liquidbounce.utils.misc.RandomUtils;
import net.ccbluex.liquidbounce.value.BoolValue;
import net.ccbluex.liquidbounce.value.IntegerValue;
import net.ccbluex.liquidbounce.value.ListValue;
import net.minecraft.entity.player.EntityPlayer;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Insult extends Module {

    public static final ListValue modeValue = new ListValue("Mode", new String[]{"Clear", "WithWords", "RawWords"}, "RawWords",false,()->true);
    private static final BoolValue waterMarkValue = new BoolValue("WaterMark", true,false,()->true);
    private static final IntegerValue delayValue = new IntegerValue("DelaySeconds", 3, new IntRange(20,500000), false,()->true); // 0 to 10 seconds

    private final File insultFile = new File(LiquidBounce.INSTANCE.getFileManager().getDir(), "insult.json");
    private final List<String> insultWords = new ArrayList<>();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public Insult() {
        super("Insult", Category.WORLD,0,false,true,"LLL kid","Insult",false,false,false);
        loadFile();
    }

    public void loadFile() {
        try {
            if (!insultFile.exists()) {
                FileUtils.unpackFile(insultFile, "assets/minecraft/liquidbounce/insult.json");
            }

            String content = new String(java.nio.file.Files.readAllBytes(insultFile.toPath()), Charset.forName("UTF-8"));
            JsonElement json = new JsonParser().parse(content);

            insultWords.clear();

            if (json.isJsonArray()) {
                for (JsonElement element : json.getAsJsonArray()) {
                    insultWords.add(element.getAsString());
                }
            } else {
                convertToJson(insultFile, content);
            }
        } catch (Exception e) {
            e.printStackTrace();
            convertToJson(insultFile, "");
        }
    }

    private void convertToJson(File file, String oldContent) {
        try {
            insultWords.clear();
            for (String line : java.nio.file.Files.readAllLines(file.toPath(), Charset.forName("UTF-8"))) {
                if (!line.trim().isEmpty()) {
                    insultWords.add(line);
                }
            }

            JsonArray jsonArray = new JsonArray();
            for (String word : insultWords) {
                jsonArray.add(new JsonPrimitive(word));
            }

            // Write FileManager.INSTANCE.getPRETTY_GSON().toJson(jsonArray) to file file
            FileWriter fileWriter = new FileWriter(file);
            FileManager.INSTANCE.getPRETTY_GSON().toJson(jsonArray, fileWriter);
            fileWriter.flush();
            fileWriter.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getRandomOne() {
        int index = RandomUtils.nextInt(0, insultWords.size() - 1);
        return insultWords.get(index);
    }

    @EventTarget
    public void onKilled(EntityKilledEvent event) {
        Object targetEntity = event.getTargetEntity();
        if (!(targetEntity instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer target = (EntityPlayer) targetEntity;
        String name = target.getName();
        String message = "";

        switch (modeValue.get().toLowerCase()) {
            case "clear":
                message = "L " + name;
                break;
            case "withwords":
                message = "L " + name + " " + getRandomOne();
                break;
            case "rawwords":
                message = getRandomOne();
                break;
        }

        int delay = delayValue.get(); // Get delay in seconds
        scheduleMessage(message, name, delay);
    }

    private void scheduleMessage(String msg, String name, int delaySeconds) {
        if (delaySeconds <= 0) {
            sendInsultWords(msg, name);
            return;
        }

        scheduler.schedule(() -> sendInsultWords(msg, name), delaySeconds, TimeUnit.SECONDS);
    }

    private void sendInsultWords(String msg, String name) {
        String message = msg.replace("%name%", name);
        if (waterMarkValue.get()) {
            message = "g0Ldb0ncE > " + message;
        }

        if (mc.thePlayer != null) {
            mc.thePlayer.sendChatMessage(message);
        }
    }

    @Override
    public String getTag() {
        return modeValue.get();
    }

    @Override
    public void onDisable() {
        scheduler.shutdownNow(); // Clean up executor when module is disabled
        super.onDisable();
    }
}