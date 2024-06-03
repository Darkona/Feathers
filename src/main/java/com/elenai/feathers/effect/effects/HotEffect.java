package com.elenai.feathers.effect.effects;

import com.elenai.feathers.api.IModifier;
import com.elenai.feathers.capability.PlayerFeathers;
import com.elenai.feathers.compatibility.coldsweat.ColdSweatManager;
import com.elenai.feathers.compatibility.coldsweat.FeathersColdSweatConfig;
import com.elenai.feathers.config.FeathersCommonConfig;
import com.elenai.feathers.effect.FeathersEffects;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.elenai.feathers.attributes.FeathersAttributes.STAMINA_USAGE_MULTIPLIER;
import static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION;

public class HotEffect extends FeathersEffects {

    public static final double BASE_STRENGTH = 2.0d;
    /**
     * Doubles the amount of feathers used.
     */
    public static final IModifier HOT = new IModifier() {

        @Override
        public void onAdd(PlayerFeathers iFeathers) {

        }

        @Override
        public void onRemove(PlayerFeathers iFeathers) {

        }

        @Override
        public void applyToDelta(Player player, PlayerFeathers iFeathers, AtomicInteger feathersToUse) {

        }

        @Override
        public void applyToUsage(Player player, PlayerFeathers iFeathers, AtomicInteger staminaToUse, AtomicBoolean result) {
            staminaToUse.set(staminaToUse.get() * 2);
            result.set(true);
        }

        @Override
        public int getUsageOrdinal() {
            return ordinals[0];
        }

        @Override
        public int getDeltaOrdinal() {
            return 0;
        }

        @Override
        public String getName() {
            return "hot";
        }
    };
    private static final String MODIFIER_UUID = "2a513b45-8047-472b-b5f1-c833440c0134";


    public HotEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
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
    public boolean canApply(Player player) {
        if (FeathersCommonConfig.ENABLE_HOT_EFFECTS.get() && super.canApply(player)) {
            if (FeathersColdSweatConfig.isColdSweatEnabled()) {
                return ColdSweatManager.canApplyHotEffect(player);
            }
            return !player.hasEffect(MobEffects.FIRE_RESISTANCE);
        }
        return false;
    }
}
