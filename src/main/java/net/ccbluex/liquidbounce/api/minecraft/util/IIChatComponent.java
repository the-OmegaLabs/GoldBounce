package net.ccbluex.liquidbounce.api.minecraft.util;

import kotlin.Metadata;
import org.jetbrains.annotations.NotNull;

/* compiled from: IIChatComponent.kt */
@Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"��\"\n\u0002\u0018\u0002\n\u0002\u0010��\n��\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0004\bf\u0018��2\u00020\u0001J\u0010\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020��H&J\u0010\u0010\u000f\u001a\u00020\r2\u0006\u0010\u0010\u001a\u00020\u0007H&R\u0012\u0010\u0002\u001a\u00020\u0003X¦\u0004¢\u0006\u0006\u001a\u0004\b\u0004\u0010\u0005R\u0012\u0010\u0006\u001a\u00020\u0007X¦\u0004¢\u0006\u0006\u001a\u0004\b\b\u0010\tR\u0012\u0010\n\u001a\u00020\u0007X¦\u0004¢\u0006\u0006\u001a\u0004\b\u000b\u0010\t¨\u0006\u0011"}, d2 = {"Lnet/ccbluex/liquidbounce/api/minecraft/util/IIChatComponent;", "", "chatStyle", "Lnet/ccbluex/liquidbounce/api/minecraft/util/IChatStyle;", "getChatStyle", "()Lnet/ccbluex/liquidbounce/api/minecraft/util/IChatStyle;", "formattedText", "", "getFormattedText", "()Ljava/lang/String;", "unformattedText", "getUnformattedText", "appendSibling", "", "component", "appendText", "text", "Pride"})
/* loaded from: Ordinary２－９ｃｒｋ.jar:net/ccbluex/liquidbounce/api/minecraft/util/IIChatComponent.class */
public interface IIChatComponent {
    @NotNull
    String getUnformattedText();

    @NotNull
    IChatStyle getChatStyle();

    @NotNull
    String getFormattedText();

    void appendText(@NotNull String str);

    void appendSibling(@NotNull IIChatComponent iIChatComponent);
}
