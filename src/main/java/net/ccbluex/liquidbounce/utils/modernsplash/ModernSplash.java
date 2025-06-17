package net.ccbluex.liquidbounce.utils.modernsplash;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.lang.management.ManagementFactory;

public class ModernSplash {
    
    public static Logger LOGGER = LogManager.getLogger("ModernSplash");

    public static long doneTime = 0;

    boolean triggered = false;
    boolean trueFullscreen;

    long startupTime;
    boolean hasBeenMainMenu = false;
    boolean hasLeftMainMenu = false;
    public ModernSplash() {
        trueFullscreen = Minecraft.getMinecraft().gameSettings.fullScreen;
        Minecraft.getMinecraft().gameSettings.fullScreen = false;}



}
