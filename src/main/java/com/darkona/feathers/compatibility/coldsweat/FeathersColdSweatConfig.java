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

    static {
        BUILDER.push("Green Feathers - Cond Sweat Compatibility Settings");

        COLD_SWEAT_COMPATIBILITY = BUILDER
                .comment("Enable compatibility with Cold Sweat")
                .define("cold_sweat_compatibility", true);

        BEING_COLD_ADDS_COLD_EFFECT = BUILDER
                .comment("Allow Cold Sweat to decide if the body temperature is low enough to auto apply the Cold Effect?")
                .define("being_cold_adds_cold_effect", true);

        BEING_HOT_ADDS_HOT_EFFECT = BUILDER
                .comment("Allow Cold Sweat to decide if the body temperature is high enough to auto apply the Hot Effect?")
                .define("being_hot_adds_hot_effect", true);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static boolean isColdSweatEnabled() {
        return Feathers.COLD_SWEAT_LOADED && COLD_SWEAT_COMPATIBILITY.get();
    }
}
