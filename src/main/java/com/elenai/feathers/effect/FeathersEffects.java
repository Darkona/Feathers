package com.elenai.feathers.effect;

import com.elenai.feathers.Feathers;
import com.elenai.feathers.effect.effects.*;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class FeathersEffects extends MobEffect {

    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Feathers.MODID);

    public static final RegistryObject<MobEffect> ENDURANCE = EFFECTS
            .register("endurance", () -> new EnduranceEffect(MobEffectCategory.BENEFICIAL, 16776960));
    public static final RegistryObject<MobEffect> COLD = EFFECTS
            .register("cold", () -> new ColdEffect(MobEffectCategory.HARMFUL, 11993087));

    public static final RegistryObject<MobEffect> ENERGIZED = EFFECTS
            .register("energized", () -> new EnergizedEffect(MobEffectCategory.BENEFICIAL, 7458303));

    public static final RegistryObject<MobEffect> HOT = EFFECTS
            .register("hot", () -> new HotEffect(MobEffectCategory.HARMFUL, 0x7e5d48));

    public static final RegistryObject<MobEffect> FATIGUE = EFFECTS
            .register("fatigued", () -> new FatiguedEffect(MobEffectCategory.HARMFUL, 0x7e0048));

    public static final RegistryObject<MobEffect> MOMENTUM = EFFECTS
            .register("momentum", () -> new MomentumEffect(MobEffectCategory.BENEFICIAL, 0x7e0048));

    public static final RegistryObject<MobEffect> STRAINED = EFFECTS
            .register("strain", () -> new StrainEffect(MobEffectCategory.HARMFUL, 0x7e0048));

    protected FeathersEffects(MobEffectCategory p_19451_, int p_19452_) {
        super(p_19451_, p_19452_);
    }

    public static void register(IEventBus eventBus) {

        EFFECTS.register(eventBus);
    }

}
