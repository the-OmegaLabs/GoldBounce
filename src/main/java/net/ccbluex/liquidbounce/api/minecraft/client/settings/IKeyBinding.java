package net.ccbluex.liquidbounce.api.minecraft.client.settings;

import kotlin.Metadata;

/* compiled from: IKeyBinding.kt */
@Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"�� \n\u0002\u0018\u0002\n\u0002\u0010��\n��\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0007\n\u0002\u0010\u0002\n��\bf\u0018��2\u00020\u0001J\u0010\u0010\r\u001a\u00020\u000e2\u0006\u0010\u0005\u001a\u00020\u0006H&R\u0012\u0010\u0002\u001a\u00020\u0003X¦\u0004¢\u0006\u0006\u001a\u0004\b\u0002\u0010\u0004R\u0012\u0010\u0005\u001a\u00020\u0006X¦\u0004¢\u0006\u0006\u001a\u0004\b\u0007\u0010\bR\u0018\u0010\t\u001a\u00020\u0003X¦\u000e¢\u0006\f\u001a\u0004\b\n\u0010\u0004\"\u0004\b\u000b\u0010\f¨\u0006\u000f"}, d2 = {"Lnet/ccbluex/liquidbounce/api/minecraft/client/settings/IKeyBinding;", "", "isKeyDown", "", "()Z", "keyCode", "", "getKeyCode", "()I", "pressed", "getPressed", "setPressed", "(Z)V", "onTick", "", "Pride"})
/* loaded from: Ordinary２－９ｃｒｋ.jar:net/ccbluex/liquidbounce/api/minecraft/client/settings/IKeyBinding.class */
public interface IKeyBinding {
    int getKeyCode();

    boolean getPressed();

    void setPressed(boolean z);

    boolean isKeyDown();

    void onTick(int i);
}
