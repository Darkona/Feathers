package com.elenai.feathers.capability;

import com.elenai.feathers.api.FeathersConstants;
import com.elenai.feathers.api.IFeathers;
import com.elenai.feathers.api.IModifier;
import com.elenai.feathers.attributes.FeathersAttributes;
import com.elenai.feathers.config.FeathersCommonConfig;
import com.elenai.feathers.util.Calculations;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class PlayerFeathers implements IFeathers {

    public static final IModifier DEFAULT_USAGE = new IModifier() {
        @Override
        public void apply(Player player, PlayerFeathers playerFeathers, AtomicInteger usingFeathers) {
            //Do nothing
        }

        @Override
        public int getOrdinal() {
            return 0;
        }

        @Override
        public String getName() {
            return "default";
        }
    };

    /**
     * Basic modifier that applies the regeneration effect.
     * This modifier is used to regenerate the player's stamina.
     * The regeneration value is defined in the config.
     * This modifier is applied once per tick.
     */
    public static final IModifier REGENERATION = new IModifier() {
        @Override
        public void apply(Player player, PlayerFeathers playerFeathers, AtomicInteger staminaDelta) {

            var fps = player.getAttribute(FeathersAttributes.FEATHERS_PER_SECOND.get());
            if (fps == null) {
                staminaDelta.set(Calculations.calculateStaminaPerTick(FeathersCommonConfig.REGEN_FEATHERS_PER_SECOND.get()));
            } else {
                staminaDelta.set(staminaDelta.get() + Calculations.calculateStaminaPerTick(fps.getValue()));
            }
        }

        @Override
        public int getOrdinal() {
            return 0;
        }

        @Override
        public String getName() {
            return "regeneration";
        }

    };

    /**
     * This modifier is used to inverse the regeneration effect.
     * Available for modders as an example, but not used in this mod.
     */
    public static final IModifier INVERSE_REGENERATION = new IModifier() {

        @Override
        public void apply(Player player, PlayerFeathers playerFeathers, AtomicInteger staminaDelta) {
            var fps = player.getAttribute(FeathersAttributes.FEATHERS_PER_SECOND.get());
            if (fps == null) {
                staminaDelta.set(Calculations.calculateStaminaPerTick(FeathersCommonConfig.REGEN_FEATHERS_PER_SECOND.get()));
            } else {
                staminaDelta.set(staminaDelta.get() - Calculations.calculateStaminaPerTick(fps.getValue()));
            }
        }

        @Override
        public int getOrdinal() {
            return 0;
        }

        @Override
        public String getName() {
            return "inverse_regeneration";
        }
    };
    /**
     * This modifier is used to make the regeneration effect non-linear.
     * Regeneration is faster at the start and slower at the end.
     * Available for modders as an example, but not used in this mod.
     */
    public static final IModifier NON_LINEAR_REGENERATION = new IModifier() {

        @Override
        public void apply(Player player, PlayerFeathers playerFeathers, AtomicInteger staminaDelta) {
            var fps = player.getAttribute(FeathersAttributes.FEATHERS_PER_SECOND.get());
            if (fps == null) {
                staminaDelta.set(Calculations.calculateStaminaPerTick(FeathersCommonConfig.REGEN_FEATHERS_PER_SECOND.get()));
            } else {
                var staminaPerSecond = Calculations.calculateStaminaPerTick(FeathersCommonConfig.REGEN_FEATHERS_PER_SECOND.get()) * 20;
                var something = Math.max((int) (1 / Math.log(playerFeathers.getFeathers() / 40d + 1.4) - 3.5) * staminaPerSecond, 1);
                staminaDelta.set(something);
            }
        }

        @Override
        public int getOrdinal() {
            return 0;
        }

        @Override
        public String getName() {
            return "non_linear_regeneration";
        }
    };


    private static final int ZERO = 0;
    private int stamina;
    private int maxStamina;
    private int feathers = ZERO;
    private int cooldown = ZERO;
    private int enduranceFeathers = ZERO;
    private int strainFeathers = ZERO;
    private int maxStrained = FeathersCommonConfig.MAX_STRAIN.get();
    private int effectCheckCooldown;
    //Recalculation
    private int staminaDelta = ZERO;
    @Getter(AccessLevel.NONE)
    private boolean shouldRecalculate;
    //Effects
    private boolean cold = false;
    private boolean hot = false;
    private boolean energized = false;
    private int energizedStrength = ZERO;
    private boolean fatigued = false;
    @Getter(AccessLevel.NONE)
    private boolean momentum = false;
    private boolean strained;

    //Modifiers
    private Map<String, IModifier> staminaDeltaModifiers = new HashMap<>();
    private List<IModifier> staminaDeltaModifierList = new ArrayList<>();
    private Map<String, IModifier> staminaUsageModifiers = new HashMap<>();
    private List<IModifier> staminaUsageModifiersList = new ArrayList<>();

    public PlayerFeathers(List<IModifier> deltaModifiers, List<IModifier> usageModifiers) {

        deltaModifiers.forEach(modifier -> staminaDeltaModifiers.put(modifier.getName(), modifier));
        usageModifiers.forEach(modifier -> staminaUsageModifiers.put(modifier.getName(), modifier));
        maxStamina = (int) (FeathersCommonConfig.MAX_FEATHERS.get() * FeathersConstants.STAMINA_PER_FEATHER);
        stamina = maxStamina;
        shouldRecalculate = true;
    }

    public boolean hasMomentum() {
        return momentum;
    }

    @Override
    public int getMaxFeathers() {
        return maxStamina / FeathersConstants.STAMINA_PER_FEATHER;
    }

    public boolean shouldRecalculate() {
        return shouldRecalculate;
    }

    @Override
    public int getFeathers() {
        return stamina / FeathersConstants.STAMINA_PER_FEATHER;
    }

    @Override
    public void setFeathers(int feathers) {
        this.stamina = feathers * FeathersConstants.STAMINA_PER_FEATHER;
    }

    @Override
    public void setStamina(int stamina) {
        this.stamina = Math.min(Math.max(stamina, ZERO), maxStamina);
        synchronizeFeathers();
    }

    @Override
    public void addDeltaModifier(IModifier modifier) {

        var prev = staminaDeltaModifiers.put(modifier.getName(), modifier);
        if (prev != modifier) {
            shouldRecalculate = true;
        }
    }

    @Override
    public void removeDeltaModifier(IModifier modifier) {
        if (staminaDeltaModifiers.remove(modifier.getName()) != null) {
            shouldRecalculate = true;
        }
    }

    private void sortDeltaModifiers() {
        staminaDeltaModifierList = new ArrayList<>(staminaDeltaModifiers.values());
        staminaDeltaModifierList.sort(Comparator.comparingInt(IModifier::getOrdinal));
    }

    @Override
    public void recalculateStaminaDelta(Player player) {
        if (!shouldRecalculate) return;
        AtomicInteger delta = new AtomicInteger(ZERO);
        sortDeltaModifiers();
        for (IModifier modifier : staminaDeltaModifierList) {
            modifier.apply(player, this, delta);
        }
        staminaDelta = delta.get();
        shouldRecalculate = false;
    }

    private int applyDeltaToStrain() {


        if (strainFeathers > 0) {
            int prevDelta = staminaDelta;
            int prevStrain = strainFeathers;
            strainFeathers -= staminaDelta;
            if (strainFeathers <= 0) {
                strained = false;
                staminaDelta = prevDelta - prevStrain;
                return staminaDelta;
            }
        }
        return staminaDelta;
    }

    public void applyStaminaDelta() {
        if (stamina <= ZERO) staminaDelta = applyDeltaToStrain();

        stamina += staminaDelta;
        if (stamina > maxStamina) stamina = maxStamina;
        if (stamina < ZERO) stamina = ZERO;
        synchronizeFeathers();
    }

    @Override
    public void addUsageModifier(IModifier modifier) {
        var prev = staminaUsageModifiers.put(modifier.getName(), modifier);
        if (prev != modifier) {
            sortUsageModifiers();
            shouldRecalculate = true;
        }
    }

    @Override
    public void removeUsageModifier(IModifier modifier) {
        if (staminaUsageModifiers.remove(modifier.getName()) != null) {
            sortUsageModifiers();
            shouldRecalculate = true;
        }
    }

    private void sortUsageModifiers() {
        staminaUsageModifiersList = new ArrayList<>(staminaUsageModifiers.values());
        staminaUsageModifiersList.sort(Comparator.comparingInt(IModifier::getOrdinal));
    }

    private void synchronizeFeathers() {
        feathers = stamina / FeathersConstants.STAMINA_PER_FEATHER;
    }

    @Override
    public int gainFeathers(int feathers) {

        addStamina(feathers * FeathersConstants.STAMINA_PER_FEATHER);
        return feathers;
    }

    /**
     * Spend feathers from the player. Returns the amount of feathers spent.
     *
     * @param player
     * @param feathers
     * @return
     */
    @Override
    public int useFeathers(Player player, int feathers) {
        var prev = this.feathers;
        var staminaToRemove = new AtomicInteger(feathers * FeathersConstants.STAMINA_PER_FEATHER);
        for (IModifier modifier : staminaUsageModifiersList) {
            modifier.apply(player, this, staminaToRemove);
        }
        if (staminaToRemove.get() <= 0) return 0;
        subtractStamina(staminaToRemove.get());
        synchronizeFeathers();
        return prev - feathers;
    }

    private void addStamina(int stamina) {
        this.stamina = Math.min(this.stamina + stamina, maxStamina);
    }

    private void subtractStamina(int stamina) {
        this.stamina = Math.max(this.stamina - stamina, ZERO);
    }

    public void copyFrom(PlayerFeathers source) {
        this.maxStamina = source.maxStamina;
        this.staminaDelta = source.staminaDelta;
        this.stamina = source.stamina;
        this.enduranceFeathers = source.enduranceFeathers;
        this.cold = source.cold;
        this.hot = source.hot;
        this.energized = source.energized;
        this.shouldRecalculate = true;
        this.staminaUsageModifiers.putAll(source.staminaUsageModifiers);
        this.staminaDeltaModifiers.putAll(source.staminaDeltaModifiers);
    }

    public void saveNBTData(CompoundTag nbt) {

        nbt.putInt("stamina", this.stamina);
        nbt.putInt("max_stamina", this.maxStamina);
        nbt.putInt("stamina_delta", this.staminaDelta);
        nbt.putInt("endurance_feathers", this.enduranceFeathers);
        nbt.putInt("strain_feathers", this.strainFeathers);
        nbt.putBoolean("cold", this.cold);
        nbt.putBoolean("hot", this.hot);
        nbt.putBoolean("energized", this.energized);
        nbt.putBoolean("strained", this.strained);

    }

    public void loadNBTData(CompoundTag nbt) {

        this.stamina = nbt.getInt("stamina");
        this.maxStamina = nbt.getInt("max_stamina");
        this.staminaDelta = nbt.getInt("stamina_delta");
        this.enduranceFeathers = nbt.getInt("endurance_feathers");
        this.strainFeathers = nbt.getInt("strain_feathers");
        this.cold = nbt.getBoolean("cold");
        this.hot = nbt.getBoolean("hot");
        this.energized = nbt.getBoolean("energized");
        this.strained = nbt.getBoolean("strained");
        synchronizeFeathers();
    }


}
