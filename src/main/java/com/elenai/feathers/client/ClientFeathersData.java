package com.elenai.feathers.client;

import com.elenai.feathers.config.FeathersCommonConfig;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ClientFeathersData {
	public static int stamina = 200;
	public static int maxStamina = 200;
    public static int feathers = 20;
	public static int maxFeathers = 20;
	public static int staminaDelta = FeathersCommonConfig.REGENERATION.get();
    public static int previousFeathers = feathers;
    public static int enduranceFeathers = 0;
    public static int weight = 20;
    public static int animationCooldown = 0;
	public static int fadeCooldown = 0;
    public static boolean cold = false;
	public static boolean hot = false;
    public static boolean energized = false;
	public static boolean overflowing = false;

}
