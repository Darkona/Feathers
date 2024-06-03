package com.elenai.feathers.effect.effects;

import com.elenai.feathers.effect.FeathersEffects;
import net.minecraft.world.effect.MobEffectCategory;

import static com.elenai.feathers.attributes.FeathersAttributes.STAMINA_USAGE_MULTIPLIER;
import static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION;

public class MomentumEffect extends FeathersEffects {

    public static final double BASE_STRENGTH = -0.5d;

    private static final String MODIFIER_UUID = "d454fe2d-dbf1-4f6d-9a59-5c62f6dd430f";

    /**
     * Halves the amount of stamina used.
     */

    public MomentumEffect(MobEffectCategory p_19451_, int p_19452_) {
        super(p_19451_, p_19452_);
        addAttributeModifier(STAMINA_USAGE_MULTIPLIER.get(), MODIFIER_UUID, BASE_STRENGTH, ADDITION);
    }
}
