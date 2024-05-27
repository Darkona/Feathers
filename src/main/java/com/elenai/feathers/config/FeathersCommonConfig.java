package com.elenai.feathers.config;

import java.util.ArrayList;
import java.util.List;

import com.elenai.feathers.Feathers;
import com.google.common.collect.Lists;

import net.minecraft.world.item.ArmorItem;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

public class FeathersCommonConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Integer> REGENERATION;

    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_ARMOR_WEIGHTS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ARMOR_WEIGHTS;

    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_COLD_EFFECTS;
    public static final ForgeConfigSpec.ConfigValue<Integer> COLD_EFFECT_COOLDOWN_MULTIPLIER;

    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_HOT_EFFECTS;
    public static final ForgeConfigSpec.ConfigValue<Integer> HOT_FEATHER_REDUCTION;

    public static final ForgeConfigSpec.ConfigValue<Boolean> SLEEPING_ALWAYS_RESTORES_FEATHERS;

    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_LIGHTWEIGHT_ENCHANTMENT;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_ENDURANCE;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENDURANCE_ENCHANTMENT_REGEN;

    public static final ForgeConfigSpec.ConfigValue<Integer> COLD_LINGER;





    //Configs for Cold Sweat

    public static final ForgeConfigSpec.ConfigValue<Boolean> COLD_SWEAT_COMPATIBILITY;


    //Configs for Thirst Was Taken
    public static final ForgeConfigSpec.ConfigValue<Boolean> THIRST_COMPATIBILITY;
    public static final ForgeConfigSpec.ConfigValue<Integer> THIRST_REGEN_REDUCTION_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Integer> QUENCH_REGEN_BONUS_MULTIPLIER;


    public static List<String> armorWeightBuilder = new ArrayList<>();

    static {

        boolean isThirstLoaded = ModList.get().isLoaded("thirst");
        boolean isColdSweatLoaded = ModList.get().isLoaded("cold_sweat");
        BUILDER.push("Feathers' Config");

        REGENERATION = BUILDER
                .comment("How many stamina regenerates per tick. 10 stamina = 1 feather.")
                          .define("Base Stamina Regeneration", 2);

        /*
         * Add all current armor types on config creation
         */
        ForgeRegistries.ITEMS.forEach(i -> {
            if (i.asItem() instanceof ArmorItem armor) {
                int def = armor.getDefense();
                FeathersCommonConfig.armorWeightBuilder.add(i.getDescriptionId() + ":" + def);
            }
        });

        ARMOR_WEIGHTS = BUILDER
                .comment("How many half feathers each item weighs.")
                .defineList("Armor Weights Override", Lists.newArrayList(armorWeightBuilder), o -> o instanceof String);

        ENABLE_ARMOR_WEIGHTS = BUILDER
                .comment("If enabled, armor types have weight, this reduces the amount of feathers you can use based on how heavy your armor is")
                .define("Enable Armor Weights", false);
        
        ENABLE_COLD_EFFECTS = BUILDER
                .comment("Whether the Cold Effect is enabled. When the effect is active, feathers regenerate slower.")
                .define("Enable Cold Effect", false);

        COLD_EFFECT_COOLDOWN_MULTIPLIER = BUILDER
                .comment("How muc does the cooldown multiply by when Cold Effect is applied. Values can range from 1 (which would have no effect) up to 20. " +
                        "Set to 1 to have no effect, set to 20 to have the feathers regenerate 20 times slower. Set to 21 to have feathers not regenerate at all.")
                .defineInRange("Cold Multiplier", 21, 1, 21);

        ENABLE_HOT_EFFECTS = BUILDER
                .comment("Whether the Hot Effect is enabled. When the effect is active, feathers are reduced. Fatigue is applied when the player is hot or burning")
                .define("Enable Hot Effect", false);

        HOT_FEATHER_REDUCTION = BUILDER.
                comment("Multiplier for the feather reduction when affected by heat. Values can range from 0 to 20." +
                        "The higher the value, the more feathers are reduced. The lower the value, the less feathers are reduced." +
                        "A value of 0 means no feathers are reduced. A value of 20 means all feathers are reduced.")
                .defineInRange("Fatigue Feather Reduction Multiplier", 6, 0, 20);

        ENABLE_LIGHTWEIGHT_ENCHANTMENT = BUILDER
                .comment("Whether the Lightweight enchantment can be applied in an enchantment table, or if it is treasure only.")
                .define("Enable Lightweight Enchantment in Table", false);

        ENABLE_ENDURANCE = BUILDER
                .comment("Whether the Endurance effect is enabled and the potions registered.")
                .define("Enable Endurance effect", false);

        ENDURANCE_ENCHANTMENT_REGEN = BUILDER
                .comment("Whether the Endurance effect also regenerates the extra feathers while active. " +
                        "If false, the effect only adds temporal extra feathers.")
                .define("Endurance Enchantment Regeneration", false);

        COLD_LINGER = BUILDER
                .comment("How long does the Cold Effect linger after stopping being cold")
                .define("Cold Lingering time in ticks", 60);

        SLEEPING_ALWAYS_RESTORES_FEATHERS = BUILDER
                .comment("Whether sleeping always restores feathers to the maximum amount.")
                .define("Sleeping Always Restores Feathers", true);



        BUILDER.pop();

        if(Feathers.COLD_SWEAT_LOADED){
            BUILDER.push("Cold Sweat compatibility settings");
            COLD_SWEAT_COMPATIBILITY = BUILDER
                    .comment("Enable compatibility with mod \"Cold Sweat\". " +
                            "If enabled, Cold Sweat will determine if the player gets the Hot or the Cold effect depending on body temperature.")
                    .define("Cold Sweat Compatibility", true);
            BUILDER.pop();
        }else{
            COLD_SWEAT_COMPATIBILITY = BUILDER.define("Cold Sweat Compatibility", false);
        }

        BUILDER.push("Thirst Was Taken compatibility settings");

        THIRST_COMPATIBILITY = BUILDER
                .comment("Enable compatibility with mod \"Thirst Was Taken\".")
                .define("Determine Cold With Thirst", true);

        THIRST_REGEN_REDUCTION_MULTIPLIER = BUILDER
                .comment("How many ticks of half-feather regeneration be increased by level missing of Thirst. " +
                        "Maximum Thirst is 20 and minimum is 0." )
                .define("Thirst Reduces Regen", 5);

        QUENCH_REGEN_BONUS_MULTIPLIER = BUILDER
                .comment("How many ticks of half-feather regeneration be decreased by level of Quench. " +
                        "Maximum Quench is whatever your current Thirst is and minimum is 0." )
                .define("Thirst Increases Regen", 2);

        BUILDER.pop();


        SPEC = BUILDER.build();
    }
}
