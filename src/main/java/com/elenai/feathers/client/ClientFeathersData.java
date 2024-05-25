package com.elenai.feathers.client;

public class ClientFeathersData {
    private static int feathers = 20;
	private static int maxFeathers = 20;
	private static int regenRate = 1;
    private static int previousFeathers = feathers;
    private static int enduranceFeathers = 0;
    private static int weight = 20;
    private static int animationCooldown = 0;
	private static int fadeCooldown = 0;
    private static boolean cold = false;
	private static boolean hot = false;
    private static boolean energized = false;
	private static boolean overflowing = false;
	private static int maxCooldown;

	public static void setFeathers(int feathers) {
        ClientFeathersData.feathers = feathers;
    }

    public static int getFeathers() {
        return ClientFeathersData.feathers;
    }

	public static int getRegenRate() {return ClientFeathersData.regenRate; }

	public static void setRegenRate(int ticks) {ClientFeathersData.regenRate = ticks; }

	public static void setMaxFeathers(int feathers) { ClientFeathersData.maxFeathers = feathers; }

	public static int getMaxFeathers() { return ClientFeathersData.maxFeathers; }

	public static void setWeight(int weight) {
		ClientFeathersData.weight = weight;
	}

	public static int getWeight() {
		return ClientFeathersData.weight;
	}

	public static int getAnimationCooldown() { return animationCooldown; }

	public static void setAnimationCooldown(int i) {
		ClientFeathersData.animationCooldown = i;
	}

	public static int getFadeCooldown() { return fadeCooldown; }

	public static void setFadeCooldown(int i) { ClientFeathersData.fadeCooldown = i; }

	public static boolean isCold() {
		return cold;
	}

	public static void setCold(boolean cold) {
		ClientFeathersData.cold = cold;
	}

	public static boolean isHot() {
		return hot;
	}

	public static void setHot(boolean hot) {
		ClientFeathersData.hot = hot;
	}

	public static boolean isOverflowing() {
		return overflowing;
	}

	public static void setOverflowing(boolean overflowing) {
		ClientFeathersData.overflowing = overflowing;
	}

	public static int getEnduranceFeathers() {
		return enduranceFeathers;
	}

	public static void setEnduranceFeathers(int enduranceFeathers) {
		ClientFeathersData.enduranceFeathers = enduranceFeathers;
	}

	public static boolean isEnergized() {
		return energized;
	}

	public static void setEnergized(boolean energized) {
		ClientFeathersData.energized = energized;
	}

	public static int getPreviousFeathers() {
		return previousFeathers;
	}

	public static void setPreviousFeathers(int previousFeathers) {
		ClientFeathersData.previousFeathers = previousFeathers;
	}

	public static void setMaxCooldown(int maxCooldown) {
		ClientFeathersData.maxCooldown = maxCooldown;
	}

	public static int getMaxCooldown() {
		return maxCooldown;
	}
}
