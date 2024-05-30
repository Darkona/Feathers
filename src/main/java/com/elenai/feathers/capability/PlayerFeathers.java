package com.elenai.feathers.capability;

import com.elenai.feathers.api.FeathersConstants;
import com.elenai.feathers.api.IModifier;
import com.elenai.feathers.config.FeathersCommonConfig;

import com.elenai.feathers.effect.ColdEffect;
import com.elenai.feathers.effect.HotEffect;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.*;

@Getter
@Setter
public class PlayerFeathers {

    private static final int ZERO = 0;

    private int stamina = FeathersCommonConfig.MAX_STAMINA.get();
    private int maxStamina = FeathersCommonConfig.MAX_STAMINA.get();

    private int feathers = ZERO;
    private int maxFeathers = FeathersCommonConfig.MAX_STAMINA.get() / FeathersConstants.STAMINA_PER_FEATHER;



    private int enduranceFeathers = ZERO;
    private int strainFeathers = ZERO;

    private int maxStrain = FeathersCommonConfig.MAX_STRAIN.get();

    //Recalculation
    private int staminaDelta = ZERO;
    private boolean shouldRecalculate = false;

    //Effects
    private boolean cold = false;
    private boolean hot = false;
    private boolean energized = false;
    private int energizedStrength = ZERO;
    private boolean fatigued = false;
    private boolean momentum = false;


    private Map<String, IModifier> staminaDeltaModifiers = new HashMap<>();
    private List<IModifier> staminaDeltaModifierList = new ArrayList<>();

    private Map<String, IModifier> staminaUsageModifiers = new HashMap<>();
    private List<IModifier> staminaUsageModifiersList = new ArrayList<>();
    private boolean strain;

    public PlayerFeathers(List<IModifier> deltaModifiers, List<IModifier> usageModifiers) {
        deltaModifiers.forEach(modifier -> staminaDeltaModifiers.put(modifier.getName(), modifier));
        usageModifiers.forEach(modifier -> staminaUsageModifiers.put(modifier.getName(), modifier));
        shouldRecalculate = true;
    }

    public void setStamina(int stamina) {
        this.stamina = Math.min(Math.max(stamina, ZERO), maxStamina);
        synchronizeFeathers();
    }

    public int getFeathers() {
        return stamina / FeathersConstants.STAMINA_PER_FEATHER;
    }

    public void setFeathers(int feathers) {
        this.stamina = feathers * FeathersConstants.STAMINA_PER_FEATHER;
    }

    public void addDeltaModifier(IModifier modifier) {

        var prev = staminaDeltaModifiers.put(modifier.getName(), modifier);
        if (prev != modifier) {
            sortDeltaModifiers();
            shouldRecalculate = true;
        }
    }

    public void removeDeltaModifier(IModifier modifier) {
        if (staminaDeltaModifiers.remove(modifier.getName()) != null) {
            sortDeltaModifiers();
            shouldRecalculate = true;
        }
    }

    private void sortDeltaModifiers() {
        staminaDeltaModifierList = new ArrayList<>(staminaDeltaModifiers.values());
        staminaDeltaModifierList.sort(Comparator.comparingInt(IModifier::getOrdinal));
    }

    public void recalculateStaminaDelta(Player player) {
        if (!shouldRecalculate) return;
        staminaDelta = 0;

        for (IModifier modifier : staminaDeltaModifierList) {
            staminaDelta = modifier.apply(player, this, staminaDelta);
        }
        shouldRecalculate = false;
    }

    public void applyStaminaDelta() {
        if (staminaDelta == ZERO) return;

        if (strainFeathers > ZERO) {

            strainFeathers = Math.max(strainFeathers - staminaDelta, ZERO);

        } else {
            stamina = stamina + staminaDelta;

            if (stamina > maxStamina) stamina = maxStamina;

            if (stamina < ZERO) stamina = ZERO;
        }

        synchronizeFeathers();
    }

    public void addUsageModifier(IModifier modifier) {
        var prev = staminaUsageModifiers.put(modifier.getName(), modifier);
        if (prev != modifier) {
            sortUsageModifiers();
            shouldRecalculate = true;
        }
    }

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

    public void setMaxStamina(int maxStamina) {
        this.maxStamina = Math.min(maxStamina, FeathersConstants.STAMINA_CAP);
    }

    public int gainFeathers(int feathers) {

        addStamina(feathers * FeathersConstants.STAMINA_PER_FEATHER);
        return feathers;
    }

    public int useFeathers(Player player, int feathers) {

        var staminaToRemove = feathers * FeathersConstants.STAMINA_PER_FEATHER;
        for (IModifier modifier : staminaUsageModifiersList) {
            staminaToRemove = modifier.apply(player, this, staminaToRemove);
        }

        subtractStamina(staminaToRemove);
        synchronizeFeathers();
        return this.getFeathers() - feathers;
    }

    private void addStamina(int stamina) {

        this.stamina = Math.min(this.stamina + stamina, maxStamina);
        synchronizeFeathers();
    }

    private void subtractStamina(int stamina) {

        this.stamina = Math.max(this.stamina - stamina, ZERO);
        synchronizeFeathers();
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
        nbt.putBoolean("strain", this.strain);

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
        this.strain = nbt.getBoolean("strain");
        synchronizeFeathers();
    }


}
