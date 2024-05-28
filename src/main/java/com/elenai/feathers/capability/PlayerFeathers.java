package com.elenai.feathers.capability;

import com.elenai.feathers.api.FeathersConstants;
import com.elenai.feathers.config.FeathersCommonConfig;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Getter
@Setter
public class PlayerFeathers  {

	private int stamina = FeathersCommonConfig.MAX_STAMINA.get();
	private int maxStamina = FeathersCommonConfig.MAX_STAMINA.get();
	private final int ZERO = 0;

	private int enduranceStamina = 0;
	private int strainStamina = 0;
	private int maxStrain = 4000;

	private int staminaDelta = 0;



	private boolean cold = false;
	private boolean hot = false;
	private boolean energized = false;


	private boolean shouldRecalculate = false;

	private Map<String, Function<Integer,Integer>> staminaDeltaModifiers = new HashMap<>();
	private Map<String, Function<Integer,Integer>> staminaUsageModifiers = new HashMap<>();



	private static final Function <Integer, Integer> regeneration = (i) -> i + FeathersCommonConfig.REGENERATION.get();
	private static final Function <Integer, Integer> energy = (i) -> FeathersCommonConfig.REGENERATION.get() * 2;
	private static final Function <Integer, Integer> no_regen = (i) -> 0;


	public PlayerFeathers() {
		addDeltaModifier("regeneration", regeneration);
	}

	public void setStamina(int stamina) {
		this.stamina = Math.min(Math.max(stamina, ZERO), maxStamina);
	}

	public int getFeathers(){
		return stamina / FeathersConstants.STAMINA_PER_FEATHER;
	}

	public int getMaxFeathers(){
		return maxStamina / FeathersConstants.STAMINA_PER_FEATHER;
	}

	public void setFeathers(int feathers){
		this.stamina = feathers * FeathersConstants.STAMINA_PER_FEATHER;
	}


	public void addDeltaModifier(String key, @NonNull Function<Integer, Integer> modifier) {

		var prev = staminaDeltaModifiers.put(key, modifier);
		if(prev != modifier){
			shouldRecalculate = true;
		}
	}

	public void recalculateStaminaDelta(){
		if(!shouldRecalculate)return;
		staminaDelta = 0;

		staminaDeltaModifiers.forEach((key, modifier) -> {
			if(modifier != null)
				staminaDelta = modifier.apply(staminaDelta);
		});
		if(energized) staminaDelta = energy.apply(staminaDelta);
		shouldRecalculate = false;
	}

	public void removeDeltaModifier(String key) {

		if(staminaDeltaModifiers.remove(key) != null)
			shouldRecalculate = true;
	}
	public void applyStaminaDelta(){

		stamina = stamina + staminaDelta;

		if(stamina > maxStamina) stamina = maxStamina;

		if(stamina < ZERO) stamina = ZERO;
	}

	public void setMaxStamina(int maxStamina) {
		this.maxStamina = Math.min(maxStamina, FeathersConstants.STAMINA_CAP);
	}

	public int useFeathers(int feathers) {

		this.stamina = Math.max(this.stamina - feathers, ZERO);
		return this.stamina;
	}
	public void addFeathers(int feathers) {

		this.stamina = Math.min(this.stamina + feathers, maxStamina);
	}

	public void subFeathers(int feathers) {

		this.stamina = Math.max(this.stamina - feathers, ZERO);
	}

	public void copyFrom(PlayerFeathers source) {
		this.maxStamina = source.maxStamina;
		this.staminaDelta = source.staminaDelta;
		this.stamina = source.stamina;
		this.enduranceStamina = source.enduranceStamina;
		this.cold = source.cold;
		this.hot = source.hot;
		this.energized = source.energized;
		this.shouldRecalculate = true;
	}

	public void saveNBTData(CompoundTag nbt) {

		nbt.putInt("stamina", this.stamina);
		nbt.putInt("max_stamina", this.maxStamina);
		nbt.putInt("stamina_delta", this.staminaDelta);
		nbt.putInt("endurance_stamina", this.enduranceStamina);
		nbt.putInt("strain_stamina", this.strainStamina);
		nbt.putBoolean("cold", this.cold);
		nbt.putBoolean("hot", this.hot);
		nbt.putBoolean("energized", this.energized);
	}

	public void loadNBTData(CompoundTag nbt) {

		this.stamina = nbt.getInt("stamina");
		this.maxStamina = nbt.getInt("max_stamina");
		this.staminaDelta = nbt.getInt("stamina_delta");
		this.enduranceStamina = nbt.getInt("endurance_stamina");
		this.strainStamina = nbt.getInt("strain_stamina");
		this.cold = nbt.getBoolean("cold");
		this.hot = nbt.getBoolean("hot");
		this.energized = nbt.getBoolean("energized");
	}

	public void addEndurance(int enduranceStamina) {

		this.enduranceStamina = this.enduranceStamina + enduranceStamina;
	}

	public void subEndurance(int enduranceStamina) {

		this.enduranceStamina = Math.max(this.enduranceStamina - enduranceStamina, 0);
	}

}
