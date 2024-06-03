package com.darkona.feathers.util;

import com.darkona.feathers.api.FeathersConstants;

public class Calculations {

    public static int calculateStaminaPerTick(double feathersPerSecond) {
        return (int) staminaPerSecond(feathersPerSecond) / 20;
    }

    public static double staminaPerSecond(double feathersPerSecond) {
        return feathersPerSecond * FeathersConstants.STAMINA_PER_FEATHER;
    }

    public static int staminaPerSecond(int staminaPerTick) {
        return staminaPerTick * 20;
    }

    public static double calculateFeathersPerSecond(int staminaPerTick) {
        return (double) staminaPerSecond(staminaPerTick) / FeathersConstants.STAMINA_PER_FEATHER;
    }

}
