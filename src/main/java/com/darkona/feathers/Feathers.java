package com.darkona.feathers;

import com.darkona.feathers.attributes.FeathersAttributes;
import com.darkona.feathers.commands.CommandInit;
import com.darkona.feathers.compatibility.coldsweat.ColdSweatManager;
import com.darkona.feathers.compatibility.coldsweat.FeathersColdSweatConfig;
import com.darkona.feathers.compatibility.thirst.FeathersThirstConfig;
import com.darkona.feathers.compatibility.thirst.ThirstManager;
import com.darkona.feathers.config.FeathersClientConfig;
import com.darkona.feathers.config.FeathersCommonConfig;
import com.darkona.feathers.effect.EffectsHandler;
import com.darkona.feathers.effect.FeathersEffects;
import com.darkona.feathers.enchantment.FeathersEnchantments;
import com.darkona.feathers.networking.FeathersMessages;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Feathers.MODID)
public class Feathers {
    public static final String MODID = "green_feathers";
    public static final Logger logger = LogManager.getLogger(MODID);
    public static final boolean OB_LOADED = ModList.get().isLoaded("overflowingbars");
    public static final boolean THIRST_LOADED = ModList.get().isLoaded("thirst");
    public static final boolean COLD_SWEAT_LOADED = ModList.get().isLoaded("cold_sweat");
    public static final boolean SERENE_SEASONS_LOADED = ModList.get().isLoaded("sereneseasons");

    public Feathers() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, FeathersClientConfig.SPEC, "feathers//Feathers-Client.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, FeathersCommonConfig.SPEC, "feathers//Feathers-Common.toml");

        if (THIRST_LOADED) {
            ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, FeathersThirstConfig.SPEC, "feathers//Feathers-Thirst.toml");
        }
        if (COLD_SWEAT_LOADED) {
            ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, FeathersColdSweatConfig.SPEC, "feathers//Feathers-ColdSweat.toml");
        }
        if (SERENE_SEASONS_LOADED) {
            ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, FeathersColdSweatConfig.SPEC, "feathers//Feathers-SereneSeasons.toml");
        }

        FeathersAttributes.register(modEventBus);
        FeathersEffects.register(modEventBus);
        FeathersPotions.register(modEventBus);
        FeathersEnchantments.register(modEventBus);
        CommandInit.ARGUMENTS.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);

    }


    @SubscribeEvent
    public void commonSetup(FMLCommonSetupEvent event) {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        event.enqueueWork(FeathersMessages::register);
        registerBrewingRecipes();
        FeathersManager.registerPlugin(EffectsHandler.getInstance());
        if (THIRST_LOADED) {
            FeathersManager.registerPlugin(ThirstManager.getInstance());
        }
        if (COLD_SWEAT_LOADED) {
            FeathersManager.registerPlugin(ColdSweatManager.getInstance());
        }
    }

    private void registerBrewingRecipes() {

        // Cold
        PotionBrewing.addMix(Potions.AWKWARD, Items.SNOWBALL, FeathersPotions.COLD_POTION.get());

        //Hot
        PotionBrewing.addMix(Potions.AWKWARD, Items.MAGMA_BLOCK, FeathersPotions.HOT_POTION.get());

        // Endurance
        PotionBrewing.addMix(Potions.AWKWARD, Items.FEATHER, FeathersPotions.ENDURANCE_POTION.get());
        PotionBrewing.addMix(FeathersPotions.ENDURANCE_POTION.get(), Items.REDSTONE, FeathersPotions.LONG_ENDURANCE_POTION.get());
        PotionBrewing.addMix(FeathersPotions.ENDURANCE_POTION.get(), Items.GLOWSTONE_DUST, FeathersPotions.STRONG_ENDURANCE_POTION.get());

        // Momentum
        PotionBrewing.addMix(Potions.AWKWARD, Items.BASALT, FeathersPotions.MOMENTUM_POTION.get());
        PotionBrewing.addMix(FeathersPotions.MOMENTUM_POTION.get(), Items.REDSTONE, FeathersPotions.LONG_MOMENTUM_POTION.get());
        PotionBrewing.addMix(FeathersPotions.MOMENTUM_POTION.get(), Items.GLOWSTONE_DUST, FeathersPotions.STRONG_MOMENTUM_POTION.get());

        // Energized
        PotionBrewing.addMix(Potions.AWKWARD, Items.RAW_COPPER, FeathersPotions.ENERGIZED_POTION.get());
        PotionBrewing.addMix(FeathersPotions.ENERGIZED_POTION.get(), Items.REDSTONE, FeathersPotions.LONG_ENERGIZED_POTION.get());
        PotionBrewing.addMix(FeathersPotions.ENERGIZED_POTION.get(), Items.GLOWSTONE_DUST, FeathersPotions.STRONG_ENERGIZED_POTION.get());
    }

}
