package com.elenai.feathers.effect;

import com.elenai.feathers.api.FeathersConstants;
import com.elenai.feathers.api.IModifier;
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

public class EnduranceEffect extends MobEffect {

    public EnduranceEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    /**
     * Uses Endurance Feathers before using regular feathers.
     * If the player has no Endurance Feathers, the player will use regular feathers.
     */
    public static final IModifier ENDURANCE = new IModifier() {
        @Override
        public int apply(Player player, PlayerFeathers playerFeathers, int staminaToUse) {
            if(!player.hasEffect(FeathersEffects.ENDURANCE.get())) return 0;

            int feathersToUse = staminaToUse / FeathersConstants.STAMINA_PER_FEATHER;
            if(playerFeathers.getEnduranceFeathers() > 0){
                int enduranceFeathers = playerFeathers.getEnduranceFeathers();
                if(enduranceFeathers >= feathersToUse){
                    playerFeathers.setEnduranceFeathers(enduranceFeathers - feathersToUse);
                    return 0;
                } else {
                    player.removeEffect(FeathersEffects.ENDURANCE.get());
                    return (feathersToUse - enduranceFeathers) * FeathersConstants.STAMINA_PER_FEATHER;
                }
            }

            return 0;
        }

        @Override
        public int getOrdinal() {
            return 0;
        }

        @Override
        public String getName() {
            return "endurance";
        }
    };

    @Override
    public void addAttributeModifiers(@NotNull LivingEntity target, @NotNull AttributeMap map, int strength) {
        if (target instanceof ServerPlayer player) {
            player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).ifPresent(f -> {
                f.setEnduranceFeathers((strength + 1) * 8);
                f.addUsageModifier(ENDURANCE);
                FeathersMessages.sendToPlayer(new EffectChangeSTCPacket(Effect.ENDURANCE, true, strength), player);
            });
        }
        super.addAttributeModifiers(target, map, strength);
    }

    @Override
    public void removeAttributeModifiers(@NotNull LivingEntity target, @NotNull AttributeMap map, int strength) {
        if (target instanceof ServerPlayer player) {
            player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).ifPresent(f -> {
                f.setEnduranceFeathers(0);
                f.removeUsageModifier(ENDURANCE);
                FeathersMessages.sendToPlayer(new EffectChangeSTCPacket(Effect.ENDURANCE, false, strength), player);
            });
        }
        super.removeAttributeModifiers(target, map, strength);
    }

}
