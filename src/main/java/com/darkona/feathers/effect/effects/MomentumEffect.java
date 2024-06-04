package com.darkona.feathers.effect.effects;

import com.darkona.feathers.config.FeathersCommonConfig;
import com.darkona.feathers.effect.FeathersEffects;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import static com.darkona.feathers.attributes.FeathersAttributes.STAMINA_USAGE_MULTIPLIER;
import static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION;

public class MomentumEffect extends FeathersEffects {

    public static final String MODIFIER_UUID = "d454fe2d-dbf1-4f6d-9a59-5c62f6dd430f";
    public static final double BASE_STRENGTH = -0.5d;

    /**
     * Halves the amount of stamina used.
     */
    public MomentumEffect(MobEffectCategory p_19451_, int p_19452_) {
        super(p_19451_, p_19452_);
        addAttributeModifier(STAMINA_USAGE_MULTIPLIER.get(), MODIFIER_UUID, BASE_STRENGTH, ADDITION);
    }

    @Override
    public void addAttributeModifiers(@NotNull LivingEntity target, @NotNull AttributeMap map, int strength) {
        super.addAttributeModifiers(target, map, strength);
    }

    @Override
    public void removeAttributeModifiers(@NotNull LivingEntity target, @NotNull AttributeMap map, int strength) {
        super.removeAttributeModifiers(target, map, strength);
    }

    @Override
    public boolean canApply(Player player){
        return super.canApply(player) && FeathersCommonConfig.ENABLE_MOMENTUM.get();
    }

}
