package com.elenai.feathers.effect.effects;

import com.elenai.feathers.api.FeathersAPI;
import com.elenai.feathers.api.IFeathers;
import com.elenai.feathers.capability.Capabilities;
import com.elenai.feathers.effect.FeathersEffects;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import static com.elenai.feathers.attributes.FeathersAttributes.FEATHERS_PER_SECOND;
import static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_BASE;
import static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_TOTAL;

public class ColdEffect extends FeathersEffects {


    public static final String MODIFIER_UUID = "848704c2-d3b5-4b71-9e96-7ab5c42095e2";
    public static final double BASE_STRENGTH = -0.5d;

    public ColdEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
        addAttributeModifier(FEATHERS_PER_SECOND.get(), MODIFIER_UUID, BASE_STRENGTH, MULTIPLY_TOTAL);
    }

    @Override
    public void addAttributeModifiers(@NotNull LivingEntity target, @NotNull AttributeMap map, int strength) {
        super.addAttributeModifiers(target, map, strength);
        target.getCapability(Capabilities.PLAYER_FEATHERS).ifPresent(IFeathers::shouldRecalculate);
    }

    @Override
    public void removeAttributeModifiers(@NotNull LivingEntity target, @NotNull AttributeMap map, int strength) {
        super.removeAttributeModifiers(target, map, strength);
        target.getCapability(Capabilities.PLAYER_FEATHERS).ifPresent(IFeathers::shouldRecalculate);
    }
}
