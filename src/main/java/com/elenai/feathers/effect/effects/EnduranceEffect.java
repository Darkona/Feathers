package com.elenai.feathers.effect.effects;

import com.elenai.feathers.api.FeathersConstants;
import com.elenai.feathers.api.IModifier;
import com.elenai.feathers.api.StaminaAPI;
import com.elenai.feathers.capability.Capabilities;
import com.elenai.feathers.capability.PlayerFeathers;
import com.elenai.feathers.config.FeathersCommonConfig;
import com.elenai.feathers.effect.FeathersEffects;
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
        public void onAdd(PlayerFeathers iFeathers) {

        }

        @Override
        public void onRemove(PlayerFeathers iFeathers) {

        }

        @Override
        public void applyToDelta(Player player, PlayerFeathers iFeathers, AtomicInteger staminaToUse) {


        }

        @Override
        public void applyToUsage(Player player, PlayerFeathers iFeathers, AtomicInteger staminaToUse, AtomicBoolean result) {
            if (player.hasEffect(FeathersEffects.ENDURANCE.get())) {
                iFeathers.getCounter(ENDURANCE_COUNTER).ifPresent(enduranceFeathers -> {

                    int availableEnduranceStamina = enduranceFeathers * FeathersConstants.STAMINA_PER_FEATHER;

                    int remaining = availableEnduranceStamina - staminaToUse.get();

                    if (remaining <= 0) {
                        resetEndurance(player, iFeathers, staminaToUse, remaining);
                    } else {
                        updateEndurance(iFeathers, staminaToUse, remaining);
                    }
                });
            }
        }

        private void resetEndurance(Player player, PlayerFeathers iFeathers, AtomicInteger staminaToUse, int remainingStamina) {
            iFeathers.setCounter(ENDURANCE_COUNTER, 0);
            staminaToUse.addAndGet(-1 * remainingStamina);
            player.removeEffect(FeathersEffects.ENDURANCE.get());
        }

        private void updateEndurance(PlayerFeathers iFeathers, AtomicInteger staminaToUse, int remainingStamina) {
            iFeathers.setCounter(ENDURANCE_COUNTER, remainingStamina / FeathersConstants.STAMINA_PER_FEATHER);
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
        entity.getCapability(Capabilities.PLAYER_FEATHERS).ifPresent(f -> {
            f.setCounter(EnduranceEffect.ENDURANCE_COUNTER, (effect.getAmplifier() + 1) * 8);
            StaminaAPI.addStaminaUsageModifier((Player) entity, EnduranceEffect.ENDURANCE);
        });

    }

    public void removeEffect(LivingEntity entity, MobEffectInstance effectInstance) {
        entity.getCapability(Capabilities.PLAYER_FEATHERS).ifPresent(f -> {
            f.removeCounter(EnduranceEffect.ENDURANCE_COUNTER);
            StaminaAPI.removeStaminaUsageModifier((Player) entity, EnduranceEffect.ENDURANCE);
        });

    }
}
