package com.darkona.feathers.effect.effects;

import com.darkona.feathers.Feathers;
import com.darkona.feathers.api.Constants;
import com.darkona.feathers.api.IFeathers;
import com.darkona.feathers.api.IModifier;
import com.darkona.feathers.capability.FeathersCapabilities;
import com.darkona.feathers.capability.PlayerFeathers;
import com.darkona.feathers.config.FeathersCommonConfig;
import com.darkona.feathers.effect.FeathersEffects;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.darkona.feathers.attributes.FeathersAttributes.FEATHERS_PER_SECOND;
import static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.*;

public class StrainEffect extends FeathersEffects {
    public static final String STRAIN_COUNTER = "strain";
    /**
     * This modifier is used to over-spend feathers when no more feathers are available.
     * While strained, the player will enter a negative stamina state.
     * This modifier adds Strained Feathers up to Max_strain when stamina is 0.
     * While strained, regeneration is much, much slower.
     */
    public static final IModifier STRAIN_MODIFIER = new IModifier() {

        @Override
        public void onAdd(IFeathers f) {
            f.setCounter(STRAIN_COUNTER, 0);
        }

        @Override
        public void onRemove(IFeathers f) {
            f.removeCounter(STRAIN_COUNTER);
        }

        @Override
        public void applyToDelta(Player player, IFeathers f, AtomicInteger staminaDelta) {
            if(f.getCooldown() > 0) return;
            int currentStrain = (int) Math.ceil(f.getCounter(STRAIN_COUNTER));
            if (currentStrain > 0) {
                int recover = currentStrain - (staminaDelta.get());
                if (recover <= 0) {
                    f.setCounter(STRAIN_COUNTER, 0);
                    staminaDelta.set(-recover);
                } else {
                    f.setCounter(STRAIN_COUNTER, recover);
                    staminaDelta.set(0);
                }
            }
        }

        @Override
        public void applyToUsage(Player player, IFeathers f, AtomicInteger staminaToUse, AtomicBoolean approve) {
            if (approve.get()) return;
            int use = f.getAvailableStamina() - staminaToUse.get();
            int strain = (int) Math.ceil(f.getCounter(STRAIN_COUNTER));
            int maxStrainStamina = FeathersCommonConfig.MAX_STRAIN.get() * Constants.STAMINA_PER_FEATHER;

            if (use < 0 && (strain - use <= maxStrainStamina)) {
                f.setCounter(STRAIN_COUNTER, strain - use);
                approve.set(true);
                staminaToUse.set(f.getAvailableStamina());
                if (FeathersCommonConfig.DEBUG_MODE.get() && !player.level().isClientSide()) {
                    Feathers.logger.info("Used {} stamina, generated {} strain stamina.", staminaToUse.get(), -use);
                }
            }
        }

        @Override
        public int getUsageOrdinal() {
            return ordinals[5];
        }

        @Override
        public int getDeltaOrdinal() {
            return ordinals[15];
        }

        @Override
        public String getName() {
            return "strain";
        }
    };

    private static final String MODIFIER_UUID = "735f6a64-a3f9-4a0b-bee8-51a243097c07";
    private static final double BASE_STRENGTH = -0.75D;
    /*
        This is the craziness Minecraft does internally, so this results in a 0.1x multiplier.

        double additionValue = baseValue;
        for(AttributeModifier attributemodifier : this.getModifiersOrEmpty(AttributeModifier.Operation.ADDITION)) {
            additionValue += attributemodifier.getAmount();
        }

        double multiplyValue = additionValue;

        for(AttributeModifier attributemodifier1 : this.getModifiersOrEmpty(AttributeModifier.Operation.MULTIPLY_BASE)) {
            multiplyValue += additionValue * attributemodifier1.getAmount();
        }

        for(AttributeModifier attributemodifier2 : this.getModifiersOrEmpty(AttributeModifier.Operation.MULTIPLY_TOTAL)) {
            multiplyValue *= 1.0D + attributemodifier2.getAmount();
        }
     */

    public StrainEffect(MobEffectCategory p_19451_, int p_19452_) {
        super(p_19451_, p_19452_);
        addAttributeModifier(FEATHERS_PER_SECOND.get(), MODIFIER_UUID, BASE_STRENGTH, MULTIPLY_TOTAL);
    }

    public void applyEffect(LivingEntity entity, MobEffectInstance effect) {
       /* if (entity instanceof Player player) {
            var attr = Objects.requireNonNull(player.getAttribute(FEATHERS_PER_SECOND.get()));
            attr.setBaseValue(attr.getBaseValue() * BASE_STRENGTH);
        }*/
    }

    public void removeEffect(LivingEntity entity, MobEffectInstance effectInstance) {
        /*if (entity instanceof Player player) {
            Objects.requireNonNull(player.getAttribute(FEATHERS_PER_SECOND.get())).setBaseValue(BASE_STRENGTH);
        }*/
    }

    @Override
    public boolean canApply(Player player) {
        return super.canApply(player) && FeathersCommonConfig.ENABLE_STRAIN.get();
    }
}
