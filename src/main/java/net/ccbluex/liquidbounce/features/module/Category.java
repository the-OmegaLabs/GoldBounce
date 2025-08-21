package net.ccbluex.liquidbounce.features.module;

import org.jetbrains.annotations.NotNull;

public enum Category {

    COMBAT("Combat"),
    PLAYER("Player"),
    MOVEMENT("Movement"),
    RENDER("Render"),
    WORLD("World"),
    MISC("Misc"),
    EXPLOIT("Exploit"),
    FUN("Fun"),
    SETTINGS("Settings"),
    HUD("HUD");
    @NotNull
//    @Getter

    private final String displayName;
    private Category(@NotNull String displayName) {
        this.displayName = displayName;
    }

    //    Retarded kotlin doesn't fucking resugar lombok generated getter, so fuck this
    //    Or the fucking stupid kotlin have higher priority than lombok's annotation processor😅
    public @NotNull String getDisplayName() {
        return this.displayName;
    }
}
