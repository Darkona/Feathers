package com.elenai.feathers.effect;

import com.elenai.feathers.api.IModifier;
import com.elenai.feathers.capability.Capabilities;
import com.elenai.feathers.capability.PlayerFeathers;
import com.elenai.feathers.config.FeathersCommonConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class EnergizedEffect extends MobEffect {

    public static final IModifier ENERGIZED = new IModifier() {

        @Override
        public void apply(Player player, PlayerFeathers playerFeathers, AtomicInteger staminaDelta) {
            var effect = player.getEffect(FeathersEffects.ENERGIZED.get());
            if (effect != null) {
                int strength =  effect.getAmplifier();
                float multiplier = 1 + ((strength + 1) * 0.2F);
                staminaDelta.set((int) (FeathersCommonConfig.REGEN_FEATHERS_PER_SECOND.get() * multiplier));
            }
        }

        @Override
        public int getOrdinal() {
            return 100;
        }

        @Override
        public String getName() {
            return "hot";
        }
    };
    private static final Function<Integer, Integer> energize_one = (i) -> i * 2;

    public EnergizedEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public void addAttributeModifiers(@NotNull LivingEntity target, @NotNull AttributeMap map, int strength) {
        if (target instanceof ServerPlayer player) {
            player.getCapability(Capabilities.PLAYER_FEATHERS).ifPresent(f -> {
                if (!f.isEnergized()) {
                    f.setEnergized(true);
                    f.addDeltaModifier(ENERGIZED);
                    //FeathersMessages.sendToPlayer(new EffectChangeSTCPacket(Effect.ENERGIZED, true, strength), player);
                }
            });
        }
        super.addAttributeModifiers(target, map, strength);
    }

    @Override
    public void removeAttributeModifiers(@NotNull LivingEntity target, @NotNull AttributeMap map, int strength) {
        if (target instanceof ServerPlayer player) {
            player.getCapability(Capabilities.PLAYER_FEATHERS).ifPresent(f -> {
                if (f.isEnergized()) {
                    f.setEnergized(false);
                    f.removeDeltaModifier(ENERGIZED);
                    //FeathersMessages.sendToPlayer(new EffectChangeSTCPacket(Effect.ENERGIZED, false, strength), player);
                }
            });
        }
        super.removeAttributeModifiers(target, map, strength);
    }


}
