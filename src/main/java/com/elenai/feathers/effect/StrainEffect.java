package com.elenai.feathers.effect;

import com.elenai.feathers.api.IModifier;
import com.elenai.feathers.capability.PlayerFeathers;
import com.elenai.feathers.capability.PlayerFeathersProvider;
import com.elenai.feathers.config.FeathersCommonConfig;
import com.elenai.feathers.networking.FeathersMessages;
import com.elenai.feathers.networking.packet.Effect;
import com.elenai.feathers.networking.packet.EffectChangeSTCPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class StrainEffect extends MobEffect {
    /**
     * This modifier is used to over-spend feathers when no more feathers are available.
     * While strained, the player will enter a negative stamina state.
     * This modifier adds Strained Feathers up to Max_strain when stamina is 0.
     */
    public static final IModifier STRAIN_USAGE = new IModifier() {
        @Override
        public int apply(Player player, PlayerFeathers playerFeathers, int feathers) {
            if (playerFeathers.getFeathers() == 0) {
                int strain = playerFeathers.getStrainFeathers();
                if (strain + feathers <= playerFeathers.getMaxStrained()) {
                    playerFeathers.setStrainFeathers(strain + feathers);
                }
            }
            return 0;
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
        public int apply(Player player, PlayerFeathers playerFeathers, int staminaDelta) {
            if (playerFeathers.getStrainFeathers() > 0) {
                staminaDelta = (int) (FeathersCommonConfig.REGENERATION.get() * 0.4);
            }
            return staminaDelta;
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

    protected StrainEffect(MobEffectCategory p_19451_, int p_19452_) {
        super(p_19451_, p_19452_);
    }

    @Override
    public void addAttributeModifiers(@NotNull LivingEntity target, @NotNull AttributeMap map, int strength) {
        if (!FeathersCommonConfig.ENABLE_STRAIN.get()) return;
        if (target instanceof ServerPlayer player) {
            player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).ifPresent(f -> {
                if (!f.isStrained()) {
                    f.setStrained(true);
                    f.addUsageModifier(STRAIN_USAGE);
                    f.removeDeltaModifier(PlayerFeathers.REGENERATION);
                    f.addDeltaModifier(STRAIN_RECOVERY);
                    FeathersMessages.sendToPlayer(new EffectChangeSTCPacket(Effect.STRAIN, true, strength), player);
                }
            });
        }
        super.addAttributeModifiers(target, map, strength);
    }

    @Override
    public void removeAttributeModifiers(@NotNull LivingEntity target, @NotNull AttributeMap map, int strength) {
        if (!FeathersCommonConfig.ENABLE_STRAIN.get()) return;
        if (target instanceof ServerPlayer player) {
            player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).ifPresent(f -> {
                if (f.isStrained()) {
                    f.setStrained(false);
                    f.removeUsageModifier(STRAIN_USAGE);
                    f.removeDeltaModifier(STRAIN_RECOVERY);
                    f.addDeltaModifier(PlayerFeathers.REGENERATION);
                    FeathersMessages.sendToPlayer(new EffectChangeSTCPacket(Effect.STRAIN, false, strength), player);
                }
            });
        }
        super.removeAttributeModifiers(target, map, strength);
    }

}
