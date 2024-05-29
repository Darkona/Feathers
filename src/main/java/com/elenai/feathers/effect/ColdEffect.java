package com.elenai.feathers.effect;

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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ColdEffect extends MobEffect implements IFeatherEffect {

    public ColdEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
        ret.add(new ItemStack(Items.MILK_BUCKET));
        //TODO add hot potion
        return ret;
    }

    private static final Function<Integer, Integer> coldModifier = (i) -> i - FeathersCommonConfig.COLD_EFFECT_STRENGTH.get();
    @Override
    public void addAttributeModifiers(@NotNull LivingEntity target, @NotNull AttributeMap map, int strength) {
        if (target instanceof ServerPlayer player) {

            player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).ifPresent(f -> {
                if (!f.isCold()) {
                    f.setCold(true);
                    f.addDeltaModifier("cold", coldModifier);
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
                    f.removeDeltaModifier("cold");
                    FeathersMessages.sendToPlayer(new EffectChangeSTCPacket(Effect.COLD, f.isCold(), strength), player);
                }
            });
        }
        super.removeAttributeModifiers(target, map, strength);
    }
}
