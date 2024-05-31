package com.elenai.feathers.effect;

import com.elenai.feathers.api.FeathersAPI;
import com.elenai.feathers.attributes.FeathersAttributes;
import com.elenai.feathers.capability.PlayerFeathersProvider;
import com.elenai.feathers.networking.FeathersMessages;
import com.elenai.feathers.networking.packet.Effect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class ColdEffect extends MobEffect {


    public static final String MODIFIER_UUID = "848704c2-d3b5-4b71-9e96-7ab5c42095e2";
    public static final double BASE_STRENGTH = -0.5d;

    public ColdEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
        addAttributeModifier(FeathersAttributes.FEATHERS_PER_SECOND.get(), MODIFIER_UUID, BASE_STRENGTH, AttributeModifier.Operation.MULTIPLY_BASE);
    }

    @Override
    public void addAttributeModifiers(@NotNull LivingEntity target, @NotNull AttributeMap map, int strength) {
        if (target instanceof Player player) {
           FeathersAPI.markForRecalculation(player);
        }
        super.addAttributeModifiers(target, map, strength);
    }

    @Override
    public void removeAttributeModifiers(@NotNull LivingEntity target, @NotNull AttributeMap map, int strength) {
        if (target instanceof Player player) {
            FeathersAPI.markForRecalculation(player);
        }
        super.removeAttributeModifiers(target, map, strength);
    }
}
