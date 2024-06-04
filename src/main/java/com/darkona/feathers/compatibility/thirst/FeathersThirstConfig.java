package com.darkona.feathers.compatibility.thirst;

import com.darkona.feathers.Feathers;
import net.minecraftforge.common.ForgeConfigSpec;

public class FeathersThirstConfig {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();


    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.ConfigValue<Boolean> THIRST_COMPATIBILITY;
    public static final ForgeConfigSpec.ConfigValue<Double> THIRST_FEATHER_REGEN_REDUCTION;
    public static final ForgeConfigSpec.ConfigValue<Double> QUENCH_REGEN_BONUS_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> THIRST_CONSUMPTION_BY_FEATHER;

    static {
        BUILDER.push("Thirst Compatibility");

        THIRST_COMPATIBILITY = BUILDER
                .comment("Enable compatibility with Thirst Was Taken")
                .define("thirst_compatibility", true);

        THIRST_FEATHER_REGEN_REDUCTION = BUILDER
                .comment("How much does being dehydrated reduces feather regeneration?" +
                        "Each point of dehydration is a missing half droplet, just like feathers." +
                        "This value means how many feathers per second are reduced per each point of dehydration" +
                        "Value type: Double")
                .defineInRange("thirst_regen_reduction", 0.02, 0.01, 20.0);

        QUENCH_REGEN_BONUS_MULTIPLIER = BUILDER
                .comment("How much being quenched increases the regeneration bonus effect?" +
                        "Quench is a mechanic just like hunger saturation, so it's invisible" +
                        "Each point of \"quenchness\" adds this many feathers per second to regeneration." +
                        "Value type: Double")
                .defineInRange("quench_regen_bonus", 0.02, 0.01, 20.0);


        THIRST_CONSUMPTION_BY_FEATHER = BUILDER
                .comment("Regenerating one feather consumes this much hydration." +
                        "Set to zero to disable consuming hydration to regenerate feathers." +
                        "Value type: Double")
                .define("thirst_consumption_by_feather", 0.0D);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static boolean isThirstOn() {
        return Feathers.THIRST_LOADED && THIRST_COMPATIBILITY.get();
    }
}
