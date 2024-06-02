package com.elenai.feathers.effect.effects;

import com.elenai.feathers.api.IModifier;
import com.elenai.feathers.capability.Capabilities;
import com.elenai.feathers.capability.PlayerFeathers;
import com.elenai.feathers.config.FeathersCommonConfig;
import com.elenai.feathers.effect.FeathersEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class StrainEffect extends FeathersEffects {
    /**
     * This modifier is used to over-spend feathers when no more feathers are available.
     * While strained, the player will enter a negative stamina state.
     * This modifier adds Strained Feathers up to Max_strain when stamina is 0.
     * While strained, regeneration is much, much slower.
     */
    public static final IModifier STRAIN_USAGE = new IModifier() {
        @Override
        public void apply(Player player, PlayerFeathers iFeathers, AtomicInteger feathers) {
            if (iFeathers.getFeathers() == 0) {
                int strain = iFeathers.getStrainFeathers();
                if (strain + feathers.get() <= iFeathers.getMaxStrained()) {
                    iFeathers.setStrainFeathers(strain + feathers.get());
                }
            }
        }

        @Override
        public void apply(Player player, PlayerFeathers iFeathers, AtomicInteger staminaDelta, AtomicBoolean result) {

        }

        @Override
        public int getOrdinal() {
            return 20;
        }

        @Override
        public String getName() {
            return "strain";
        }
    };
    public static final IModifier STRAIN_RECOVERY = new IModifier() {
        @Override
        public void apply(Player player, PlayerFeathers iFeathers, AtomicInteger staminaDelta) {
            if (iFeathers.getStrainFeathers() > 0) {
                staminaDelta.set(staminaDelta.get() + (int) (FeathersCommonConfig.REGEN_FEATHERS_PER_SECOND.get() * 0.4));
            }
        }

        @Override
        public void apply(Player player, PlayerFeathers iFeathers, AtomicInteger staminaDelta, AtomicBoolean result) {

        }

        @Override
        public int getOrdinal() {
            return 0;
        }

        @Override
        public String getName() {
            return "strain_recovery";
        }
    };

    public StrainEffect(MobEffectCategory p_19451_, int p_19452_) {
        super(p_19451_, p_19452_);
    }

    @Override
    public void addAttributeModifiers(@NotNull LivingEntity target, @NotNull AttributeMap map, int strength) {
        if (!FeathersCommonConfig.ENABLE_STRAIN.get()) return;
        if (target instanceof ServerPlayer player) {
            player.getCapability(Capabilities.PLAYER_FEATHERS).ifPresent(f -> {


            });
        }
        super.addAttributeModifiers(target, map, strength);
    }

    @Override
    public void removeAttributeModifiers(@NotNull LivingEntity target, @NotNull AttributeMap map, int strength) {
        if (!FeathersCommonConfig.ENABLE_STRAIN.get()) return;
        if (target instanceof ServerPlayer player) {
            player.getCapability(Capabilities.PLAYER_FEATHERS).ifPresent(f -> {

            });
        }
        super.removeAttributeModifiers(target, map, strength);
    }

    public void applyEffect(LivingEntity entity) {
        entity.getCapability(Capabilities.PLAYER_FEATHERS).ifPresent(f -> {
            f.addCounter("Strain", 0);
        });
    }

}
