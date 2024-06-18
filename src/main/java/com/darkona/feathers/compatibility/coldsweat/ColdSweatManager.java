package com.darkona.feathers.compatibility.coldsweat;

import com.darkona.feathers.api.ICapabilityPlugin;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.util.registries.ModEffects;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;

public class ColdSweatManager implements ICapabilityPlugin {

    private static ICapabilityPlugin instance;

    public static ICapabilityPlugin getInstance() {
        if (instance == null) {
            instance = new ColdSweatManager();
        }
        return instance;
    }

    public static boolean canApplyColdEffect(Player player) {
        return !(player.hasEffect(ModEffects.GRACE) || player.hasEffect(ModEffects.ICE_RESISTANCE));
    }

    public static boolean canApplyHotEffect(Player player) {
        return !(player.hasEffect(ModEffects.GRACE) || player.hasEffect(MobEffects.FIRE_RESISTANCE));
    }

    public static boolean isOverheating(Player player) {
        return Temperature.get(player, Temperature.Trait.BODY) >= FeathersColdSweatConfig.HOT_THRESHOLD.get();
    }

    public static boolean isFreezing(Player player) {
        return Temperature.get(player, Temperature.Trait.BODY) <= FeathersColdSweatConfig.COLD_THRESHOLD.get();
    }

    @Override
    public void onPlayerJoin(EntityJoinLevelEvent event) {

    }

    @Override
    public void onPlayerTickBefore(TickEvent.PlayerTickEvent event) {

    }

    @Override
    public void onPlayerTickAfter(TickEvent.PlayerTickEvent event) {

    }

    @Override
    public void attachDeltaModifiers() {

    }

    @Override
    public void attackUsageModifiers() {

    }

}
