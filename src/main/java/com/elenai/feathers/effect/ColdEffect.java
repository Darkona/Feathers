package com.elenai.feathers.effect;

import com.elenai.feathers.api.IModifier;
import com.elenai.feathers.capability.Modifiers;
import com.elenai.feathers.capability.PlayerFeathers;
import com.elenai.feathers.capability.PlayerFeathersProvider;
import com.elenai.feathers.config.FeathersCommonConfig;
import com.elenai.feathers.networking.FeathersMessages;
import com.elenai.feathers.networking.packet.Effect;
import com.elenai.feathers.networking.packet.EffectChangeSTCPacket;
import com.elenai.feathers.potion.FeathersPotions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.extensions.IForgeMobEffect;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ColdEffect extends MobEffect {

    public ColdEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    public static final IModifier COLD = new IModifier() {
        @Override
        public int apply(Player player, PlayerFeathers playerFeathers, int staminaDelta) {
            return staminaDelta - FeathersCommonConfig.COLD_EFFECT_STRENGTH.get();
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
