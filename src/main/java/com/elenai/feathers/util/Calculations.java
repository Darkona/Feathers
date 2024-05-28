package com.elenai.feathers.util;

import com.elenai.feathers.api.FeathersConstants;

public class Calculations {

    public static int calculateStaminaRegenPerTick(double feathersPerSecond) {
        double staminaPerSecond = feathersPerSecond * FeathersConstants.STAMINA_PER_FEATHER;
        return (int)staminaPerSecond / 20;
    }

    public static int calculateStaminaRegenPerSecondFromTicksPerFeather(int ticksPerFeather) {
        return FeathersConstants.STAMINA_PER_FEATHER / ticksPerFeather;
    }
}
