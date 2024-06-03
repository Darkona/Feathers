package com.elenai.feathers.compatibility.thirst;

import com.elenai.feathers.Feathers;
import net.minecraftforge.common.ForgeConfigSpec;

public class FeathersThirstConfig {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();


    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.ConfigValue<Boolean> THIRST_COMPATIBILITY;
    public static final ForgeConfigSpec.ConfigValue<Integer> THIRST_STAMINA_DRAIN;
    public static final ForgeConfigSpec.ConfigValue<Integer> QUENCH_REGEN_BONUS_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> THIRST_CONSUMPTION_BY_FEATHER;

    static {
        BUILDER.push("Thirst Compatibility");

        THIRST_COMPATIBILITY = BUILDER
                .comment("Enable compatibility with Thirst Was Taken")
                .define("thirst_compatibility", true);

        THIRST_STAMINA_DRAIN = BUILDER
                .comment("How much thirst reduces the regeneration bonus effect?" +
                        "This value is subtracted per thirst level (missing half-drop) from the stamina regeneration per tick."
                        + "The default value of 1 means that the stamina regeneration is reduced by 2 per missing drop, to a maximum of.")
                .defineInRange("thirst_regen_reduction_multiplier", 1, 1, Integer.MAX_VALUE);

        QUENCH_REGEN_BONUS_MULTIPLIER = BUILDER
                .comment("How much being quenched increases the regeneration bonus effect?" +
                        "This value is added to the stamina regeneration per tick.")
                .defineInRange("quench_regen_bonus_multiplier", 1, 1, Integer.MAX_VALUE);


        THIRST_CONSUMPTION_BY_FEATHER = BUILDER
                .comment("How much thirst/quench is consumed to regenerate one feather?")
                .define("thirst_consumption_by_feather", 0.2D);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static boolean isThirstOn() {
        return Feathers.THIRST_LOADED && THIRST_COMPATIBILITY.get();
    }
}
