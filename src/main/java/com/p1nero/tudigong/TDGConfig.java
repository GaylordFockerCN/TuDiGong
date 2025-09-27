package com.p1nero.tudigong;

import net.minecraftforge.common.ForgeConfigSpec;

public class TDGConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue GUIDE_MODE = BUILDER.comment("Tell player how to summon TuDi when first meet TuDiTemple. This config will be set to false if the tutorial has finished.").define("guide_mode", true);
    public static final ForgeConfigSpec.BooleanValue SPAWN_GUIDER = BUILDER.comment("Whether to spawn the cloud guider").define("spawn_guider", true);
    public static final ForgeConfigSpec.BooleanValue GENERATE_TEMPLE = BUILDER.comment("Whether to generate TuDiTemple in villages").define("generate_temple", true);
    public static final ForgeConfigSpec.BooleanValue MARK_LOCATION = BUILDER.comment("Whether to show the target coordinate.").define("mark_location", true);

    static { BUILDER.push("Summoning"); }
    public static final ForgeConfigSpec.IntValue SPELL_COOLDOWN = BUILDER.comment("Cooldown for the TuDi Command Spell in ticks (20 ticks = 1 second).").defineInRange("spell_cooldown", 600, 0, Integer.MAX_VALUE);
    public static final ForgeConfigSpec.IntValue TEMPLE_SUMMON_SHIFT_COUNT = BUILDER.comment("How many times the player needs to sneak near a TuDi Temple to summon TuDiGong.").defineInRange("temple_summon_shift_count", 3, 1, 10);
    public static final ForgeConfigSpec.IntValue TEMPLE_SUMMON_COOLDOWN_TICKS = BUILDER.comment("Cooldown in ticks after a successful summon from a TuDi Temple before it can be used again.").defineInRange("temple_summon_cooldown_ticks", 40, 0, Integer.MAX_VALUE);
    public static final ForgeConfigSpec.IntValue TEMPLE_SUMMON_RESET_TICKS = BUILDER.comment("Time in ticks before the sneak counter resets if the summoning is not completed.").defineInRange("temple_summon_reset_ticks", 60, 20, Integer.MAX_VALUE);
    static { BUILDER.pop(); }

    static { BUILDER.push("Searching"); }
    public static final ForgeConfigSpec.IntValue STRUCTURE_SEARCH_RADIUS_CHUNKS = BUILDER.comment("The search radius in chunks for structures.").defineInRange("structure_search_radius_chunks", 100, 10, 500);
    public static final ForgeConfigSpec.IntValue BIOME_SEARCH_RADIUS_BLOCKS = BUILDER.comment("The search radius in blocks for biomes.").defineInRange("biome_search_radius_blocks", 6400, 100, 20000);
    public static final ForgeConfigSpec.IntValue HIGHLIGHT_DISTANCE_BLOCKS = BUILDER.comment("Distance in blocks from the destination to trigger the glowing highlight effect.").defineInRange("highlight_distance_blocks", 144, 16, 512);
    public static final ForgeConfigSpec.IntValue HIGHLIGHT_DURATION_TICKS = BUILDER.comment("Duration in ticks for the glowing highlight effect (20 ticks = 1 second).").defineInRange("highlight_duration_ticks", 600, 100, 72000);
    static { BUILDER.pop(); }

    static { BUILDER.push("Entity Behavior"); }
    public static final ForgeConfigSpec.IntValue TUDIGONG_LIFETIME_TICKS = BUILDER.comment("Time in ticks before TuDiGong despawns if not interacted with.").defineInRange("tudigong_lifetime_ticks", 1200, 100, 72000);
    public static final ForgeConfigSpec.IntValue XIANQI_DISCARD_DISTANCE_BLOCKS = BUILDER.comment("Distance in blocks from the destination at which the XianQi guider will disappear.").defineInRange("xianqi_discard_distance_blocks", 20, 1, 64);
    static { BUILDER.pop(); }

    static final ForgeConfigSpec SPEC = BUILDER.build();
}