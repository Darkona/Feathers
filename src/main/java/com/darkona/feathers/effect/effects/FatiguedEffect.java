package com.darkona.feathers.effect.effects;

import com.darkona.feathers.config.FeathersCommonConfig;
import com.darkona.feathers.effect.FeathersEffects;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import static com.darkona.feathers.attributes.FeathersAttributes.MAX_FEATHERS;
import static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION;

public class FatiguedEffect extends FeathersEffects {

    public static final String MODIFIER_UUID = "85465978-90f7-4dcf-8374-63a1635a72e1";
    public static final double BASE_STRENGTH = -4;

    public FatiguedEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
        addAttributeModifier(MAX_FEATHERS.get(), MODIFIER_UUID, BASE_STRENGTH, ADDITION);
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
        return super.canApply(player) && FeathersCommonConfig.ENABLE_FATIGUE.get();
    }
}
