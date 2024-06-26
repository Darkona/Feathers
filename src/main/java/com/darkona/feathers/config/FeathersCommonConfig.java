package com.darkona.feathers.config;

import com.google.common.collect.Lists;
import net.minecraft.world.item.ArmorItem;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class FeathersCommonConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();


    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Boolean> DEBUG_MODE;
    public static final ForgeConfigSpec.ConfigValue<Boolean> EXTENDED_LOGGING;

    public static final ForgeConfigSpec.ConfigValue<Integer> MAX_FEATHERS;
    public static final ForgeConfigSpec.ConfigValue<Double> REGEN_FEATHERS_PER_SECOND;
    public static final ForgeConfigSpec.ConfigValue<Boolean> SLEEPING_ALWAYS_RESTORES_FEATHERS;
    public static final ForgeConfigSpec.ConfigValue<Integer> DEFAULT_USAGE_COOLDOWN;
    public static final ForgeConfigSpec.ConfigValue<Boolean> REGEN_USES_HUNGER;
    public static final ForgeConfigSpec.ConfigValue<Integer> MAX_COOLDOWN;

    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_COLD;
    public static final ForgeConfigSpec.ConfigValue<Double> COLD_TEMPERATURE;

    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_HEAT;
    public static final ForgeConfigSpec.ConfigValue<Double> HOT_TEMPERATURE;

    public static final ForgeConfigSpec.ConfigValue<Integer> EFFECT_LINGER;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_ENDURANCE;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_STRAIN;
    public static final ForgeConfigSpec.ConfigValue<Integer> MAX_STRAIN;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_MOMENTUM;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_FATIGUE;


    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_ARMOR_WEIGHTS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ARMOR_WEIGHTS;

    public static final ForgeConfigSpec.ConfigValue<Boolean> LIGHTWEIGHT_ON_TABLE;

    public static List<String> armorWeightBuilder = new ArrayList<>();

    static {

        BUILDER.push("General");

        MAX_FEATHERS = BUILDER.comment("Maximum Feathers the player can have. Every two feathers is a full icon, just like hearts." +
                                      "By default, 20 feathers means a full row of ten feathers." +
                                      "Value type: Integer (Numbers without decimals. You can't have a fractional amount of feathers!)")
                              .defineInRange("Max Feathers", 20, 0, 40);

        REGEN_FEATHERS_PER_SECOND = BUILDER
                .comment("How many feathers the player will regenerate every second. A value of 1 means one feather will be regenerated every second." +
                        "By default, this will be modified by the Cold Effect which lowers regeneration speed, and the Energized Effect which increases it." +
                        "Value type: Double (Numbers with decimals).")
                .defineInRange("base_feather_per_second_regen", 0.4, -40.0, 40.0);

        SLEEPING_ALWAYS_RESTORES_FEATHERS = BUILDER
                .comment("Whether sleeping always restores feathers to the maximum amount." +
                        "Value type: Boolean.")
                .define("sleeping_restores_all_feathers", true);
        DEFAULT_USAGE_COOLDOWN = BUILDER
                .comment("How many ticks need to pass after using feathers for them to start regenerating again." +
                        "Value type: Integer.")
                .define("default_usage_cooldown", 30);

        REGEN_USES_HUNGER = BUILDER
                .comment("Whether feather regeneration uses hunger. If enabled, the player will need to have a certain amount of hunger to regenerate feathers." +
                        "Value type: Boolean.")
                .define("regen_uses_hunger", false);

        MAX_COOLDOWN = BUILDER
                .comment("Maximum cooldown the player can have. This is the maximum amount of time in seconds the player has to wait before feathers start regenerating again." +
                        "Value type: Integer.")
                .define("max_cooldown", 5);

        BUILDER.pop();

        BUILDER.push("Effects");

        ENABLE_COLD = BUILDER
                .comment("Enable the Cold Effect. This effect halves the regeneration speed at Level I and completely negate it at Level II when its active." +
                        "While the player is in a too cold environment, or experiencing freezing, the effect will be applied." +
                        "Value type: Boolean (Valid values are true and false.)")
                .define("effect_cold_enabled", true);

        COLD_TEMPERATURE = BUILDER
                .comment("Temperature at which the Cold Effect is applied." +
                        "Cold biomes begin at temperature 0.05 and below, down to -0.7" +
                        "Value type: Double (Numbers with decimals.)")
                .define("cold_temperature", -0.3);

        ENABLE_HEAT = BUILDER
                .comment("Enable the Heat Effect. This effect doubles the feather usage when its active." +
                        "While the player is in a too hot environment, or burning, or in lava, the effect will be applied." +
                        "Fire resistance will negate this effect." +
                        "Value type: Boolean.")
                .define("effect_hot_enabled", true);

        HOT_TEMPERATURE = BUILDER
                .comment("Temperature at which the Heat Effect is applied." +
                        "The hottest biomes are at temperature 0.95 (Jungle) and above. up to 2.0" +
                        "Value type: Double (Numbers with decimals.)")
                .define("hot_temperature", 1.8);

        EFFECT_LINGER = BUILDER
                .comment("How long do the Cold and the Heat Effect linger after the player is no longer under the circumstances that provoke them." +
                        "This number is in ticks, one second equals 20 ticks. Set to 0 to disable lingering effects." +
                        "Value type: Integer.")
                .define("effect_cold_lingering_ticks", 60);

        ENABLE_ENDURANCE = BUILDER
                .comment("Enable the Endurance Effect. This effect gives temporal golden feathers that can be consumed on top of normal feathers." +
                        "Once all the extra feathers are consumed the effect ends." +
                        "Value type: Boolean.")
                .define("effect_endurance_enabled", true);

        ENABLE_STRAIN = BUILDER
                .comment("Enable the Strain Effect. This setting enables the player to overspend feathers beyond the normal amount. " +
                        "When that happens the player will start to accumulate Strained Feathers and the effect will be applied" +
                        "While strained, the feather regeneration is slowed greatly." +
                        "Value type: Boolean.")
                .define("effect_strain_enabled", true);

        MAX_STRAIN = BUILDER
                .comment("Maximum strained feathers the player can have." +
                        "Value type: Integer.")
                .defineInRange("max_strained_feathers", 6, 2, 20);

        ENABLE_MOMENTUM = BUILDER
                .comment("Enable the Momentum Effect. This effect halves the feather usage while it's active." +
                        "Value type: Boolean.")
                .define("effect_momentum_enabled", true);

        ENABLE_FATIGUE = BUILDER
                .comment("Enable the Fatigue Effect. This effect lowers the maximum feathers while it's active." +
                        "Value type: Boolean.")
                .define("effect_fatigue_enabled", true);
        BUILDER.pop();

        BUILDER.push("Armor weights");
        /*
         * Add all current armor types on config creation
         */


        ENABLE_ARMOR_WEIGHTS = BUILDER
                .comment("If enabled, armor items have weight, this reduces the amount of feathers you can use based on how heavy your armor is.")
                .define("Enable Armor Weights", false);

        LIGHTWEIGHT_ON_TABLE = BUILDER
                .comment("Whether the Lightweight enchantment can be applied in an enchantment table, or if it is treasure only.")
                .define("Enable Lightweight Enchantment in Table", true);

        ForgeRegistries.ITEMS.forEach(i -> {
            if (i.asItem() instanceof ArmorItem armor) {
                int def = armor.getDefense();
                FeathersCommonConfig.armorWeightBuilder.add(i.getDescriptionId() + ":" + def);
            }
        });

        ARMOR_WEIGHTS = BUILDER
                .comment("How many half feathers each item weighs.")
                .defineList("Armor Weights Override", Lists.newArrayList(armorWeightBuilder), o -> o instanceof String);

        BUILDER.pop();

        BUILDER.push("Debugging");


        DEBUG_MODE = BUILDER
                .comment("Whether debug mode is enabled. This will print debug messages to the console. Useful only for developers or to submit issues. " +
                        "Will vomit a log of spam to the logs so don't enable this unless you know what you're doing.")
                .define("Debug Mode", false);

        EXTENDED_LOGGING = BUILDER
                .comment("Whether extended logging is enabled. This will print more detailed logs to the console. Useful for debugging issues." +
                        "Value type: Boolean.")
                .define("Extended Logging", false);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
