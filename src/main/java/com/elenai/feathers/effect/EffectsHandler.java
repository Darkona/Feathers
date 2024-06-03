package com.elenai.feathers.effect;

import com.elenai.feathers.Feathers;
import com.elenai.feathers.api.FeathersAPI;
import com.elenai.feathers.api.ICapabilityPlugin;
import com.elenai.feathers.api.IFeathers;
import com.elenai.feathers.capability.Capabilities;
import com.elenai.feathers.compatibility.coldsweat.ColdSweatManager;
import com.elenai.feathers.compatibility.coldsweat.FeathersColdSweatConfig;
import com.elenai.feathers.config.FeathersCommonConfig;
import com.elenai.feathers.effect.effects.StrainEffect;
import com.elenai.feathers.event.FeatherAmountEvent;
import com.elenai.feathers.event.FeatherEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static net.minecraftforge.eventbus.api.Event.Result.ALLOW;
import static net.minecraftforge.eventbus.api.Event.Result.DENY;

@Mod.EventBusSubscriber(modid = Feathers.MODID)
public class EffectsHandler implements ICapabilityPlugin {

    private static ICapabilityPlugin instance;

    public static ICapabilityPlugin getInstance() {
        if (instance == null) {
            instance = new EffectsHandler();
        }
        return instance;
    }

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        if (event.getEntity() instanceof Player player &&
                event.getEffectInstance().getEffect() instanceof FeathersEffects effect &&
                effect.canApply(player)) {
            effect.applyEffect(player, event.getEffectInstance());
        }
    }

    public static void onFeathersUsed(FeatherEvent.Use event) {

    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {

    }

    @SubscribeEvent
    public static void onFeathersEmpty(FeatherAmountEvent.Empty event) {

    }

    /**
     * Handle the cold mechanic here.
     */
    public static void autoApplyColdEffect(Player player) {
        if (!FeathersCommonConfig.ENABLE_COLD_EFFECTS.get()) return;

        var hasCold = FeathersAPI.isCold(player) && player.getActiveEffectsMap().get(FeathersEffects.COLD.get()).getDuration() == -1;

        if (player.isCreative() && player.hasEffect(FeathersEffects.COLD.get())) {
            player.removeEffect(FeathersEffects.COLD.get());
            return;
        }

        if (isInColdSituation(player)) {
            player.removeEffect(FeathersEffects.HOT.get());
            if (!hasCold) {
                player.addEffect(new MobEffectInstance(FeathersEffects.COLD.get(), -1, 0, false, true));
            }
        } else if (hasCold) {
            player.removeEffect(FeathersEffects.COLD.get());
            player.addEffect(new MobEffectInstance(FeathersEffects.COLD.get(), FeathersCommonConfig.COLD_LINGER.get(), 0, false, true));
        }
    }
    public static boolean isInColdSituation(Player player) {

        if (FeathersColdSweatConfig.isColdSweatEnabled() && FeathersColdSweatConfig.BEING_COLD_ADDS_COLD_EFFECT.get()) {
            return ColdSweatManager.isFreezing(player);
        }

        boolean isExposedToWeather = player.level().isRaining() && player.level().isRainingAt(player.blockPosition()) &&
                player.level().canSeeSky(player.blockPosition());

        boolean isInColdBiome = player.level().getBiome(player.blockPosition()).get().coldEnoughToSnow(player.blockPosition());

        return (isInColdBiome && isExposedToWeather) || player.isFreezing();
    }

    /**
     * Handles the hot mechanic here.
     */
    public static void autoApplyHotEffect(Player player) {
        if (!FeathersCommonConfig.ENABLE_HOT_EFFECTS.get()) return;
        var hasHot = FeathersAPI.isHot(player) && player.getActiveEffectsMap().get(FeathersEffects.HOT.get()).getDuration() == -1;

        if (isInHotSituation(player)) {
            player.removeEffect(FeathersEffects.COLD.get());
            if (!hasHot) {
                player.addEffect(new MobEffectInstance(FeathersEffects.HOT.get(), -1, 0, false, true));
            }

        } else if (hasHot) {

            player.removeEffect(FeathersEffects.HOT.get());
            player.addEffect(new MobEffectInstance(FeathersEffects.HOT.get(), FeathersCommonConfig.COLD_LINGER.get(), 0, false, true));
        }
    }
    public static boolean isInHotSituation(Player player) {

        boolean isBurning = player.wasOnFire || player.isOnFire() || player.isInLava();

        if (FeathersColdSweatConfig.isColdSweatEnabled() && FeathersColdSweatConfig.BEING_HOT_ADDS_HOT_EFFECT.get()) {
            return ColdSweatManager.isOverheating(player) || isBurning;
        }

        boolean isInHotBiome = player.level().getBiome(player.blockPosition()).get().getModifiedClimateSettings().temperature() > 0.45f ||
                player.level().dimension().equals(Level.NETHER);

        return isBurning || isInHotBiome;
    }


    @Override
    public void onPlayerTickBefore(TickEvent.PlayerTickEvent event) {
        if (event.player.level().isClientSide()) return;

        if (event.player.tickCount % 40 == 0) {
            autoApplyColdEffect(event.player);
            autoApplyHotEffect(event.player);
        }

    }

    @Override
    public void onPlayerTickAfter(TickEvent.PlayerTickEvent event) {
        if(event.player.level().isClientSide()) return;
        event.player.getCapability(Capabilities.PLAYER_FEATHERS).ifPresent(f -> {
            checkStrain(event.player, f);
        });
    }

    @Override
    public void onPlayerJoin(EntityJoinLevelEvent event) {

    }

    private void checkStrain(Player player, IFeathers feathers) {
        feathers.getCounter(StrainEffect.STRAIN_COUNTER).ifPresent(strain -> {
            if (strain <= 0) {
                player.removeEffect(FeathersEffects.STRAINED.get());
                feathers.markDirty();
            }else{
                player.addEffect(new MobEffectInstance(FeathersEffects.STRAINED.get(), -1, 0, false, false, false));
                feathers.markDirty();
            }
        });
    }
}
