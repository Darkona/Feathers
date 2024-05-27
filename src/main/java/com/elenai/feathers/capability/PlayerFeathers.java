package com.elenai.feathers.capability;

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
public class PlayerFeathers implements Serializable {

	private int stamina = 200;
	private int maxStamina = 200;
	private final int ZERO = 0;
	private int enduranceStamina = 0;
	private int staminaDelta = 0;
	private int strain = 0;
	private int maxStrain = 200;

	private boolean cold = false;
	private boolean hot = true;
	private boolean shouldRecalculate = false;

	private Map<String, Function<Integer,Integer>> staminaDeltaModifiers = new HashMap<>();

	private static final Function <Integer, Integer> regeneration = (i) -> i + FeathersCommonConfig.REGENERATION.get();

	public PlayerFeathers() {
		addDeltaModifier("regeneration", regeneration);
	}


	public void addDeltaModifier(String key, @NonNull Function<Integer, Integer> modifier) {

		var prev = staminaDeltaModifiers.put(key, modifier);
		if(prev != modifier){
			shouldRecalculate = true;
		}
	}


	public void recalculateStaminaDelta(){
		if(!shouldRecalculate)return;

		staminaDeltaModifiers.forEach((key, modifier) -> {
			if(modifier != null)
				staminaDelta += modifier.apply(staminaDelta);
		});
		shouldRecalculate = false;
	}

	public void removeDeltaModifier(String key) {

		if(staminaDeltaModifiers.remove(key) != null)
			shouldRecalculate = true;
	}
	public void applyStaminaDelta(){

		stamina = Math.min(Math.max(stamina + staminaDelta, ZERO), maxStamina);
	}

	public void setMaxStamina(int feathers) {

		this.maxStamina = feathers;
		if (getStamina() > feathers) {
			setStamina(feathers);
		}
	}

	public void addFeathers(int feathers) {

		this.stamina = Math.min(this.stamina + feathers, maxStamina);
	}

	public void subFeathers(int feathers) {

		this.stamina = Math.max(this.stamina - feathers, ZERO);
	}

	public void copyFrom(PlayerFeathers source) {

		this.stamina = source.stamina;
		this.enduranceStamina = source.enduranceStamina;
		this.cold = source.cold;
	}

	public void saveNBTData(CompoundTag nbt) {

		nbt.putInt("feathers", this.stamina);
		nbt.putInt("max_feathers", this.maxStamina);
		nbt.putInt("stamina_delta", this.staminaDelta);
		nbt.putInt("endurance_feathers", this.enduranceStamina);
		nbt.putBoolean("cold", this.cold);
		nbt.putBoolean("hot", this.hot);
		nbt.putBoolean("should_recalculate", this.shouldRecalculate);
	}

	public void loadNBTData(CompoundTag nbt) {

		this.stamina = nbt.getInt("feathers");
		this.maxStamina = nbt.getInt("max_feathers");
		this.staminaDelta = nbt.getInt("stamina_delta");
		this.enduranceStamina = nbt.getInt("endurance_feathers");
		this.shouldRecalculate = nbt.getBoolean("should_recalculate");
		this.cold = nbt.getBoolean("cold");
		this.hot = nbt.getBoolean("hot");
	}

	public void addEndurance(int feathers) {

		this.enduranceStamina = this.enduranceStamina + feathers;
	}

	public void subEndurance(int feathers) {

		this.enduranceStamina = Math.max(this.enduranceStamina - feathers, 0);
	}

	public boolean isEnergized() {

		return enduranceStamina > 0;
	}
}
