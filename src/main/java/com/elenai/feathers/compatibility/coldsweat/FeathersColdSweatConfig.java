package com.elenai.feathers.compatibility.coldsweat;

import com.elenai.feathers.Feathers;
import net.minecraftforge.common.ForgeConfigSpec;

public class FeathersColdSweatConfig {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec SPEC;


    //Configs for Cold Sweat

    public static boolean isColdSweatEnabled(){
        return Feathers.COLD_SWEAT_LOADED && COLD_SWEAT_COMPATIBILITY.get();
    }

    public static final ForgeConfigSpec.ConfigValue<Boolean> COLD_SWEAT_COMPATIBILITY;
    public static final ForgeConfigSpec.ConfigValue<Boolean> BEING_COLD_ADDS_COLD_EFFECT;
    public static final ForgeConfigSpec.ConfigValue<Boolean> BEING_HOT_ADDS_HOT_EFFECT;

    static {
        BUILDER.push("Cold Sweat Compatibility");

        COLD_SWEAT_COMPATIBILITY = BUILDER
                .comment("Enable compatibility with Cold Sweat")
                .define("cold_sweat_compatibility", true);

        BEING_COLD_ADDS_COLD_EFFECT = BUILDER
                .comment("Should being cold add the cold effect?")
                .define("being_cold_adds_cold_effect", true);

        BEING_HOT_ADDS_HOT_EFFECT = BUILDER
                .comment("Should being hot add the hot effect?")
                .define("being_hot_adds_hot_effect", true);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
