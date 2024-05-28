package com.elenai.feathers.client;

import com.elenai.feathers.api.FeathersConstants;
import com.elenai.feathers.config.FeathersCommonConfig;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ClientFeathersData {
	public static int stamina = 2000;
	public static int maxStamina = 2000;
	public static int staminaDelta = FeathersCommonConfig.REGENERATION.get();
    public static int previousFeathers = 0;
    public static int enduranceFeathers = 0;
    public static int weight = 0;
    public static int animationCooldown = 0;
	public static int fadeCooldown = 0;
    public static boolean cold = false;
	public static boolean hot = false;
    public static boolean energized = false;
	public static boolean overflowing = false;
    public static boolean fatigue = false;
    public static boolean momentum = false;
    public static boolean endurance = false;

    public static int getFeathers(){
        return stamina / FeathersConstants.STAMINA_PER_FEATHER;
    }

    public static int getMaxFeathers(){
        return maxStamina / FeathersConstants.STAMINA_PER_FEATHER;
    }

    public static void setFeathers(int i) {
        stamina = i * FeathersConstants.STAMINA_PER_FEATHER;
    }
}
