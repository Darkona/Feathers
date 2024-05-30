package com.elenai.feathers;

import com.elenai.feathers.attributes.FeathersAttributes;
import com.elenai.feathers.commands.CommandInit;
import com.elenai.feathers.compatibility.coldsweat.FeathersColdSweatConfig;
import com.elenai.feathers.compatibility.thirst.FeathersThirstConfig;
import com.elenai.feathers.config.FeathersClientConfig;
import com.elenai.feathers.config.FeathersCommonConfig;
import com.elenai.feathers.effect.FeathersEffects;
import com.elenai.feathers.enchantment.FeathersEnchantments;
import com.elenai.feathers.networking.FeathersMessages;
import com.elenai.feathers.potion.FeathersPotions;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
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
    public static final String MODID = "feathers";
    public static final Logger logger = LogManager.getLogger(MODID);
    public static final boolean OB_LOADED = ModList.get().isLoaded("overflowingbars");
    public static final boolean THIRST_LOADED = ModList.get().isLoaded("thirst");
    public static final boolean COLD_SWEAT_LOADED = ModList.get().isLoaded("cold_sweat");

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
        FeathersAttributes.register(modEventBus);
        FeathersEffects.register(modEventBus);
        FeathersPotions.register(modEventBus);
        FeathersEnchantments.register(modEventBus);
        CommandInit.ARGUMENTS.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(FeathersMessages::register);
        registerBrewingRecipes();
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

        // Energized
        PotionBrewing.addMix(Potions.AWKWARD, Items.RAW_COPPER, FeathersPotions.ENERGIZED_POTION.get());
        PotionBrewing.addMix(FeathersPotions.ENERGIZED_POTION.get(), Items.REDSTONE, FeathersPotions.LONG_ENERGIZED_POTION.get());
        PotionBrewing.addMix(FeathersPotions.ENERGIZED_POTION.get(), Items.GLOWSTONE_DUST, FeathersPotions.STRONG_ENERGIZED_POTION.get());
    }
}
