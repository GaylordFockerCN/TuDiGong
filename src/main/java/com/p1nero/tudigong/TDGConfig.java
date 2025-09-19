package com.p1nero.tudigong;

import net.minecraftforge.common.ForgeConfigSpec;

public class TDGConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.BooleanValue GUIDE_MODE = BUILDER.comment("Is first time meet Tu Di Gong").define("guide_mode", true);
    public static final ForgeConfigSpec.BooleanValue GENERATE_TEMPLE = BUILDER.comment("Whether to generate TuDiTemple in villages").define("generate_temple", true);
    public static final ForgeConfigSpec.BooleanValue MARK_LOCATION = BUILDER.comment("Whether to show the target coordinate.").define("mark_location", true);
    public static final ForgeConfigSpec.IntValue SPELL_COOLDOWN = BUILDER.comment("Spell cooldown.").defineInRange("spell_cooldown", 600, 0, Integer.MAX_VALUE);
    static final ForgeConfigSpec SPEC = BUILDER.build();
}
