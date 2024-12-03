/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/LiquidBounce/
 */
package net.ccbluex.liquidbounce.ui.font.fontmanager.api;

public interface FontFamily {

	FontRenderer ofSize(int size);

	FontType font();
}