package com.elenai.feathers.effect.effects;

import com.elenai.feathers.api.FeathersConstants;
import com.elenai.feathers.api.IModifier;
import com.elenai.feathers.capability.PlayerFeathers;
import com.elenai.feathers.effect.FeathersEffects;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.player.Player;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MomentumEffect extends FeathersEffects {

    /**
     * Halves the amount of stamina used.
     */
    public static final IModifier MOMENTUM = new IModifier() {

        @Override
        public void apply(Player player, PlayerFeathers playerFeathers, AtomicInteger feathersToUse) {

        }

        @Override
        public void apply(Player player, PlayerFeathers playerFeathers, AtomicInteger feathersToUse, AtomicBoolean result) {
            feathersToUse.set((feathersToUse.get() * FeathersConstants.STAMINA_PER_FEATHER) / 2);
        }

        @Override
        public int getOrdinal() {
            return 11;
        }

        @Override
        public String getName() {
            return "cold";
        }
    };

    public MomentumEffect(MobEffectCategory p_19451_, int p_19452_) {
        super(p_19451_, p_19452_);
    }
}
