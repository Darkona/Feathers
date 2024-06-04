package com.darkona.feathers.compatibility.coldsweat;

import com.darkona.feathers.Feathers;
import net.minecraftforge.common.ForgeConfigSpec;

public class FeathersColdSweatConfig {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec SPEC;


    //Configs for Cold Sweat
    public static final ForgeConfigSpec.ConfigValue<Boolean> COLD_SWEAT_COMPATIBILITY;
    public static final ForgeConfigSpec.ConfigValue<Boolean> BEING_COLD_ADDS_COLD_EFFECT;
    public static final ForgeConfigSpec.ConfigValue<Boolean> BEING_HOT_ADDS_HOT_EFFECT;
    public static final ForgeConfigSpec.ConfigValue<Integer> COLD_THRESHOLD;
    public static final ForgeConfigSpec.ConfigValue<Integer> HOT_THRESHOLD;

    static {
        BUILDER.push("Green Feathers - Cold Sweat Compatibility Settings");

        COLD_SWEAT_COMPATIBILITY = BUILDER
                .comment("Enable compatibility with Cold Sweat")
                .define("cold_sweat_compatibility", true);

        BEING_COLD_ADDS_COLD_EFFECT = BUILDER
                .comment("Allow Cold Sweat to decide if the body temperature is low enough to auto apply the Cold Effect?")
                .define("being_cold_adds_cold_effect", true);

        BEING_HOT_ADDS_HOT_EFFECT = BUILDER
                .comment("Allow Cold Sweat to decide if the body temperature is high enough to auto apply the Hot Effect?")
                .define("being_hot_adds_hot_effect", true);

        COLD_THRESHOLD = BUILDER
                .comment("The temperature threshold for Cold Sweat to apply the Cold Effect" +
                        "In Cold Sweat, a player's body temperature goes from -150 to 150 \"degrees\"." +
                        "At about -45 freezing effect starts.")
                .define("cold_threshold", -50);

        HOT_THRESHOLD = BUILDER
                .comment("The temperature threshold for Cold Sweat to apply the Hot Effect" +
                        "In Cold Sweat, a player's body temperature goes from -150 to 150 \"degrees\"." +
                        "At about 45 the player starts to overheat.")
                .define("hot_threshold", 50);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static boolean isColdSweatEnabled() {
        return Feathers.COLD_SWEAT_LOADED && COLD_SWEAT_COMPATIBILITY.get();
    }
}
