package com.elenai.feathers.effect.effects;

import com.elenai.feathers.api.FeathersAPI;
import com.elenai.feathers.api.FeathersConstants;
import com.elenai.feathers.api.IModifier;
import com.elenai.feathers.api.StaminaAPI;
import com.elenai.feathers.capability.Capabilities;
import com.elenai.feathers.capability.PlayerFeathers;
import com.elenai.feathers.client.ClientFeathersData;
import com.elenai.feathers.config.FeathersCommonConfig;
import com.elenai.feathers.effect.FeathersEffects;
import net.minecraft.world.effect.MobEffectCategory;
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
        public void apply(Player player, PlayerFeathers playerFeathers, AtomicInteger staminaToUse) {


        }

        @Override
        public void apply(Player player, PlayerFeathers playerFeathers, AtomicInteger staminaToUse, AtomicBoolean result) {
            if (!player.hasEffect(FeathersEffects.ENDURANCE.get())) return;


            playerFeathers.getCounter(ENDURANCE_COUNTER).ifPresent(enduranceFeathers -> {

                int enduranceStamina = enduranceFeathers * FeathersConstants.STAMINA_PER_FEATHER;

                int enduranceLeft = enduranceStamina - staminaToUse.get();

                if (enduranceLeft <= 0) {
                    staminaToUse.addAndGet(-enduranceLeft);
                    enduranceLeft = 0;
                    player.removeEffect(FeathersEffects.ENDURANCE.get());
                } else {
                    staminaToUse.set(0);
                }
                playerFeathers.setCounter(ENDURANCE_COUNTER, enduranceLeft / FeathersConstants.STAMINA_PER_FEATHER);
            });
        }

        @Override
        public int getOrdinal() {
            return 10;
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

}
