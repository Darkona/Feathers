package com.elenai.feathers.effect.effects;

import com.elenai.feathers.api.FeathersAPI;
import com.elenai.feathers.api.IModifier;
import com.elenai.feathers.capability.PlayerFeathers;
import com.elenai.feathers.effect.FeathersEffects;
import net.minecraft.world.effect.MobEffectCategory;
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
        public void apply(Player player, PlayerFeathers iFeathers, AtomicInteger feathersToUse) {
            feathersToUse.set(feathersToUse.get() * 2);
        }

        @Override
        public void apply(Player player, PlayerFeathers iFeathers, AtomicInteger staminaDelta, AtomicBoolean result) {

        }

        @Override
        public int getOrdinal() {
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
        if (target instanceof Player player) {
            FeathersAPI.markForDeltaRecalculation(player);
        }
        super.addAttributeModifiers(target, map, strength);
    }

    @Override
    public void removeAttributeModifiers(@NotNull LivingEntity target, @NotNull AttributeMap map, int strength) {
        if (target instanceof Player player) {
            FeathersAPI.markForDeltaRecalculation(player);
        }
        super.removeAttributeModifiers(target, map, strength);
    }
}
