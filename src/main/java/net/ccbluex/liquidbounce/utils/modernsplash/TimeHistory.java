package net.ccbluex.liquidbounce.utils.modernsplash;

import net.minecraft.client.Minecraft;
import net.minecraft.launchwrapper.Launch;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;

public class TimeHistory {
    private static Properties config;
    private static File configFile = new File(Launch.minecraftHome, "config/time.history");;
    private static final Queue<Long> queue = new LinkedList<>();
    public static long getEstimateTime() {

        File parent = configFile.getParentFile();
        if (!parent.exists())
            parent.mkdirs();

        config = new Properties();
        try (Reader r = new InputStreamReader(Files.newInputStream(configFile.toPath()), StandardCharsets.UTF_8))
        {
            config.load(r);
        }
        catch(IOException e)
        {
            ModernSplash.LOGGER.debug("Could not load time.history, will create a default one");
        }

        long sum = 0;
        long temp;
        int count = 0;
        for(int i = 0; i < 5; i++) {
            temp = getLong(String.valueOf(i));
            if(temp > 0) count++;
            queue.add(temp);
            sum += temp;
        }


        return sum == 0 ? 0 : sum / count;


    }

    public static void saveHistory(long time) {
        queue.add(time);
        queue.remove();
        //if(queue.size() != 5) return;
        int i = 0;
        for(long l : queue) {
            config.setProperty(String.valueOf(i++), Long.toString(l));
        }

        try (Writer w = new OutputStreamWriter(Files.newOutputStream(configFile.toPath()), StandardCharsets.UTF_8))
        {
            config.store(w, "Launch time history");
        }
        catch(IOException e)
        {
            ModernSplash.LOGGER.debug("Could not save the time.history file", e);
        }

    }

    private static long getLong(String name)
    {
        String value = config.getProperty(name, Long.toString(0));
        config.setProperty(name, value);
        return Long.decode(value);
    }
}