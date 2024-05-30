package com.elenai.feathers.effect;

import com.elenai.feathers.api.IModifier;
import com.elenai.feathers.capability.Modifiers;
import com.elenai.feathers.capability.PlayerFeathers;
import com.elenai.feathers.capability.PlayerFeathersProvider;
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

public class HotEffect extends MobEffect {
    public HotEffect(MobEffectCategory mobEffectCategory, int color) {super(mobEffectCategory, color);}


    /**
     * Doubles the amount of feathers used.
     */
    public static final IModifier HOT = new IModifier() {

        @Override
        public int apply(Player player, PlayerFeathers playerFeathers, int feathersToUse) {
            return feathersToUse * 2;
        }

        @Override
        public int getOrdinal() {
            return 10;
        }

        @Override
        public String getName() {
            return "hot";
        }
    };
    @Override
    public void addAttributeModifiers(@NotNull LivingEntity target, @NotNull AttributeMap map, int strength) {
        if (target instanceof ServerPlayer player) {
            player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).ifPresent(f -> {
                if (!f.isHot()) {
                    f.setHot(true);
                    f.addUsageModifier(HOT);
                    FeathersMessages.sendToPlayer(new EffectChangeSTCPacket(Effect.HOT, true, strength), player);
                }
            });
        }
        super.addAttributeModifiers(target, map, strength);
    }

    @Override
    public void removeAttributeModifiers(@NotNull LivingEntity target, @NotNull AttributeMap map, int strength) {
        if (target instanceof ServerPlayer player) {
            player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).ifPresent(f -> {
                if (f.isHot()) {
                    f.setHot(false);
                    f.removeUsageModifier(HOT);
                    FeathersMessages.sendToPlayer(new EffectChangeSTCPacket(Effect.HOT, false, strength), player);
                }
            });
        }
        super.removeAttributeModifiers(target, map, strength);
    }
}
