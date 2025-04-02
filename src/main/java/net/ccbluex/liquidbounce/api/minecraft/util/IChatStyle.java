package net.ccbluex.liquidbounce.api.minecraft.util;

import kotlin.Metadata;
import net.ccbluex.liquidbounce.api.minecraft.event.IClickEvent;
import org.jetbrains.annotations.Nullable;

/* compiled from: IChatStyle.kt */
@Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"��\"\n\u0002\u0018\u0002\n\u0002\u0010��\n��\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0005\bf\u0018��2\u00020\u0001R\u001a\u0010\u0002\u001a\u0004\u0018\u00010\u0003X¦\u000e¢\u0006\f\u001a\u0004\b\u0004\u0010\u0005\"\u0004\b\u0006\u0010\u0007R\u001a\u0010\b\u001a\u0004\u0018\u00010\tX¦\u000e¢\u0006\f\u001a\u0004\b\n\u0010\u000b\"\u0004\b\f\u0010\rR\u0018\u0010\u000e\u001a\u00020\u000fX¦\u000e¢\u0006\f\u001a\u0004\b\u0010\u0010\u0011\"\u0004\b\u0012\u0010\u0013¨\u0006\u0014"}, d2 = {"Lnet/ccbluex/liquidbounce/api/minecraft/util/IChatStyle;", "", "chatClickEvent", "Lnet/ccbluex/liquidbounce/api/minecraft/event/IClickEvent;", "getChatClickEvent", "()Lnet/ccbluex/liquidbounce/api/minecraft/event/IClickEvent;", "setChatClickEvent", "(Lnet/ccbluex/liquidbounce/api/minecraft/event/IClickEvent;)V", "color", "Lnet/ccbluex/liquidbounce/api/minecraft/util/WEnumChatFormatting;", "getColor", "()Lnet/ccbluex/liquidbounce/api/minecraft/util/WEnumChatFormatting;", "setColor", "(Lnet/ccbluex/liquidbounce/api/minecraft/util/WEnumChatFormatting;)V", "underlined", "", "getUnderlined", "()Z", "setUnderlined", "(Z)V", "Pride"})
/* loaded from: Ordinary２－９ｃｒｋ.jar:net/ccbluex/liquidbounce/api/minecraft/util/IChatStyle.class */
public interface IChatStyle {
    @Nullable
    IClickEvent getChatClickEvent();

    void setChatClickEvent(@Nullable IClickEvent iClickEvent);

    boolean getUnderlined();

    void setUnderlined(boolean z);

    @Nullable
    WEnumChatFormatting getColor();

    void setColor(@Nullable WEnumChatFormatting wEnumChatFormatting);
}
