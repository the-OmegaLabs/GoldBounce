/*
 * WTFPL LICENSED CODE
 */
package net.ccbluex.liquidbounce.features.command.commands;

import net.ccbluex.liquidbounce.features.command.Command;
import net.ccbluex.liquidbounce.script.api.global.Chat;
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
        // Remove ".title "
        try{
            Display.setTitle(shabi.toString().substring(args[0].length()+1));
        } catch (StringIndexOutOfBoundsException e){
            Chat.print("java.lang.StringIndexOutOfBoundsException: String index out of range");
        }
    }
}
