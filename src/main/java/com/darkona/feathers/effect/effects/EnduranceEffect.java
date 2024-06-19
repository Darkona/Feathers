package com.darkona.feathers.effect.effects;

import com.darkona.feathers.api.Constants;
import com.darkona.feathers.api.IFeathers;
import com.darkona.feathers.api.IModifier;
import com.darkona.feathers.api.StaminaAPI;
import com.darkona.feathers.capability.FeathersCapabilities;
import com.darkona.feathers.capability.PlayerFeathers;
import com.darkona.feathers.config.FeathersCommonConfig;
import com.darkona.feathers.effect.FeathersEffects;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class EnduranceEffect extends FeathersEffects {

    public static final String ENDURANCE_COUNTER = "endurance";


    /**
     * Uses Endurance Feathers before using regular feathers.
     * If the player has no Endurance Feathers, the player will use regular feathers.
     */
    public static final IModifier ENDURANCE = new IModifier() {
        @Override
        public void onAdd(IFeathers iFeathers) {

        }

        @Override
        public void onRemove(IFeathers iFeathers) {

        }

        @Override
        public void applyToDelta(Player player, IFeathers iFeathers, AtomicInteger staminaToUse) {}

        @Override
        public void applyToUsage(Player player, IFeathers f, AtomicInteger staminaToUse, AtomicBoolean result) {
            if (player.hasEffect(FeathersEffects.ENDURANCE.get())) {
                var enduranceFeathers = f.getCounter(ENDURANCE_COUNTER);

                int availableEnduranceStamina = (int) Math.ceil(enduranceFeathers * Constants.STAMINA_PER_FEATHER);
                int remaining = availableEnduranceStamina - staminaToUse.get();
                if (remaining <= 0) {
                    resetEndurance(player, f, staminaToUse, remaining);
                } else {
                    updateEndurance(f, staminaToUse, remaining);
                }

            }
        }

        private void resetEndurance(Player player, IFeathers f, AtomicInteger staminaToUse, int remainingStamina) {
            f.setCounter(ENDURANCE_COUNTER, 0);
            staminaToUse.addAndGet(-remainingStamina);
            player.removeEffect(FeathersEffects.ENDURANCE.get());
        }

        private void updateEndurance(IFeathers f, AtomicInteger staminaToUse, int remainingStamina) {
            f.setCounter(ENDURANCE_COUNTER, (double) remainingStamina / Constants.STAMINA_PER_FEATHER);
            staminaToUse.set(0);
        }

        @Override
        public int getUsageOrdinal() {
            return 10;
        }

        @Override
        public int getDeltaOrdinal() {
            return 0;
        }

        @Override
        public String getName() {
            return "endurance";
        }
    };

    public EnduranceEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public void addAttributeModifiers(@NotNull LivingEntity target, @NotNull AttributeMap map, int strength) {
        if (!FeathersCommonConfig.ENABLE_ENDURANCE.get()) return;
        super.addAttributeModifiers(target, map, strength);
    }

    @Override
    public void removeAttributeModifiers(@NotNull LivingEntity target, @NotNull AttributeMap map, int strength) {
        if (!FeathersCommonConfig.ENABLE_ENDURANCE.get()) return;
        super.addAttributeModifiers(target, map, strength);
    }

    public void applyEffect(LivingEntity entity, MobEffectInstance effect) {
        entity.getCapability(FeathersCapabilities.PLAYER_FEATHERS).ifPresent(f -> {
            f.setCounter(EnduranceEffect.ENDURANCE_COUNTER, (effect.getAmplifier() + 1) * 8);
            StaminaAPI.addStaminaUsageModifier((Player) entity, EnduranceEffect.ENDURANCE);
        });

    }

    public void removeEffect(LivingEntity entity, MobEffectInstance effectInstance) {
        entity.getCapability(FeathersCapabilities.PLAYER_FEATHERS).ifPresent(f -> {
            f.removeCounter(EnduranceEffect.ENDURANCE_COUNTER);
            StaminaAPI.removeStaminaUsageModifier((Player) entity, EnduranceEffect.ENDURANCE);
        });

    }
}
