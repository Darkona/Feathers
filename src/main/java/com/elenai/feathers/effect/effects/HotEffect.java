package com.elenai.feathers.effect.effects;

import com.elenai.feathers.compatibility.coldsweat.ColdSweatManager;
import com.elenai.feathers.compatibility.coldsweat.FeathersColdSweatConfig;
import com.elenai.feathers.config.FeathersCommonConfig;
import com.elenai.feathers.effect.FeathersEffects;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

import static com.elenai.feathers.attributes.FeathersAttributes.STAMINA_USAGE_MULTIPLIER;
import static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION;

public class HotEffect extends FeathersEffects {

    public static final double BASE_STRENGTH = 1.0d;
    /**
     * Doubles the amount of feathers used.
     */

    private static final String MODIFIER_UUID = "2a513b45-8047-472b-b5f1-c833440c0134";


    public HotEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
        addAttributeModifier(STAMINA_USAGE_MULTIPLIER.get(), MODIFIER_UUID, BASE_STRENGTH, ADDITION);
    }

    @Override
    public boolean canApply(Player player) {
        if (FeathersCommonConfig.ENABLE_HOT_EFFECTS.get() && super.canApply(player)) {
            if (FeathersColdSweatConfig.isColdSweatEnabled()) {
                return ColdSweatManager.canApplyHotEffect(player);
            }
            return !(player.hasEffect(MobEffects.FIRE_RESISTANCE) || player.hasEffect(FeathersEffects.MOMENTUM.get()));
        }
        return false;
    }

}
