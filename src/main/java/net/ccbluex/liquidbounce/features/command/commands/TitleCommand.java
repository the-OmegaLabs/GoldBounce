/*
 * WTFPL LICENSED CODE
 */
package net.ccbluex.liquidbounce.features.command.commands;

import net.ccbluex.liquidbounce.features.command.Command;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.Display;

public class TitleCommand extends Command {
    public TitleCommand() {
        super("title", "tt","clienttitle","ctt");
    }

    @Override
    public void execute(@NotNull String [] args) {
        StringBuilder shabi = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            shabi.append(args[i]);
            if (i < args.length - 1) {
                shabi.append(" ");
            }
        }
        Display.setTitle(shabi.toString());
    }
}
