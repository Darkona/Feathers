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

import java.util.concurrent.atomic.AtomicInteger;

public class ColdEffect extends MobEffect {

    public static final IModifier COLD = new IModifier() {
        @Override
        public void apply(Player player, PlayerFeathers playerFeathers, AtomicInteger staminaDelta) {
            staminaDelta.set(staminaDelta.get() - FeathersCommonConfig.COLD_EFFECT_STRENGTH.get());
        }

        @Override
        public int getOrdinal() {
            return 1;
        }

        @Override
        public String getName() {
            return "cold";
        }
    };

    public ColdEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public void addAttributeModifiers(@NotNull LivingEntity target, @NotNull AttributeMap map, int strength) {
        if (target instanceof ServerPlayer player) {

            player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).ifPresent(f -> {
                if (!f.isCold()) {
                    f.setCold(true);
                    f.addDeltaModifier(COLD);
                    FeathersMessages.sendToPlayer(new EffectChangeSTCPacket(Effect.COLD, f.isCold(), strength), player);
                }
            });
        }
        super.addAttributeModifiers(target, map, strength);
    }

    @Override
    public void removeAttributeModifiers(@NotNull LivingEntity target, @NotNull AttributeMap map, int strength) {
        if (target instanceof ServerPlayer player) {
            player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).ifPresent(f -> {
                if (f.isCold()) {
                    f.setCold(false);
                    f.removeDeltaModifier(COLD);
                    FeathersMessages.sendToPlayer(new EffectChangeSTCPacket(Effect.COLD, f.isCold(), strength), player);
                }
            });
        }
        super.removeAttributeModifiers(target, map, strength);
    }
}
