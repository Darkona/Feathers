package com.elenai.feathers.effect.effects;

import com.elenai.feathers.api.IFeathers;
import com.elenai.feathers.capability.Capabilities;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import org.jetbrains.annotations.NotNull;

import static com.elenai.feathers.attributes.FeathersAttributes.FEATHERS_PER_SECOND;
import static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_TOTAL;

public class EnergizedEffect extends MobEffect {

    public static final String MODIFIER_UUID = "848704c2-d3b5-4b71-9e96-7ab5c42095e2";
    public static final double BASE_STRENGTH = 1.5D;

    public EnergizedEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
        addAttributeModifier(FEATHERS_PER_SECOND.get(), MODIFIER_UUID, BASE_STRENGTH, MULTIPLY_TOTAL);
    }

    @Override
    public void addAttributeModifiers(@NotNull LivingEntity target, @NotNull AttributeMap map, int strength) {
        target.getCapability(Capabilities.PLAYER_FEATHERS).ifPresent(IFeathers::isDirty);
        super.addAttributeModifiers(target, map, strength);
    }

    @Override
    public void removeAttributeModifiers(@NotNull LivingEntity target, @NotNull AttributeMap map, int strength) {
        super.removeAttributeModifiers(target, map, strength);
        target.getCapability(Capabilities.PLAYER_FEATHERS).ifPresent(IFeathers::isDirty);

    }


}
