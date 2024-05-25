package com.elenai.feathers.event;

import com.elenai.feathers.Feathers;
import com.elenai.feathers.api.FeathersHelper;
import com.elenai.feathers.attributes.FeathersAttributes;
import com.elenai.feathers.capability.PlayerFeathers;
import com.elenai.feathers.capability.PlayerFeathersProvider;
import com.elenai.feathers.config.FeathersCommonConfig;
import com.elenai.feathers.effect.FeathersEffects;
import com.elenai.feathers.effect.PlayerSituationProvider;
import com.elenai.feathers.networking.FeathersMessages;
import com.elenai.feathers.networking.packet.ColdSyncSTCPacket;
import com.elenai.feathers.networking.packet.EnergizedSyncSTCPacket;
import com.elenai.feathers.networking.packet.FeatherSyncSTCPacket;
import com.elenai.feathers.networking.packet.HotSyncSTCPacket;
import dev.ghen.thirst.foundation.common.capability.ModCapabilities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import org.checkerframework.checker.units.qual.A;

import java.util.concurrent.atomic.AtomicInteger;

@Mod.EventBusSubscriber(modid = Feathers.MODID)
public class CommonEvents {

    @SubscribeEvent
    public static void onPlayerJoinWorld(EntityJoinLevelEvent event) {
        Level level = event.getLevel();
        if (!level.isClientSide && (event.getEntity() instanceof ServerPlayer player)) {
            player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).ifPresent(f -> {
                FeathersMessages.sendToPlayer(
                        new FeatherSyncSTCPacket(f.getFeathers(), f.getMaxFeathers(), f.getRegen(), FeathersHelper.getPlayerWeight(player), f.getEnduranceFeathers(), f.getMaxCooldown()), player);
                FeathersMessages.sendToPlayer(new ColdSyncSTCPacket(f.isCold()), player);
                FeathersMessages.sendToPlayer(new HotSyncSTCPacket(f.isHot()), player);
                FeathersMessages.sendToPlayer(new EnergizedSyncSTCPacket(player.hasEffect(FeathersEffects.ENERGIZED.get())), player);
            });
        }
    }


    /**
     * Handle the beta cold mechanic here
     */
    private static void handleColdEffect(PlayerTickEvent event) {
        ServerPlayer player = (ServerPlayer) event.player;
        if (PlayerSituationProvider.isInColdSituation(player)) {

            if (!player.hasEffect(FeathersEffects.COLD.get()) ||
                    player.getActiveEffectsMap().get(FeathersEffects.COLD.get()).getDuration() <= 201) {

                if (player.hasEffect(FeathersEffects.HOT.get()))
                    player.removeEffect(FeathersEffects.HOT.get());

                player.addEffect(new MobEffectInstance(FeathersEffects.COLD.get(), 1200, 0, false, true));
            }

        } else if (player.hasEffect(FeathersEffects.COLD.get()) &&
                player.getActiveEffectsMap().get(FeathersEffects.COLD.get()).getDuration() > FeathersCommonConfig.COLD_LINGER.get()) {

            player.removeEffect(FeathersEffects.COLD.get());
            player.addEffect(new MobEffectInstance(FeathersEffects.COLD.get(),
                    FeathersCommonConfig.COLD_LINGER.get(), 0, false, true));
        }
        if (player.isCreative() && player.hasEffect(FeathersEffects.COLD.get())) {
            player.removeEffect(FeathersEffects.COLD.get());
        }
    }

    /**
     * Handle the Fatigue mechanic here
     *
     * @param event
     */
    private static void handleHotEffect(PlayerTickEvent event) {
        ServerPlayer player = (ServerPlayer) event.player;
        if (PlayerSituationProvider.isInHotSituation(player)) {

            if (!player.hasEffect(FeathersEffects.HOT.get()) ||
                    player.getActiveEffectsMap().get(FeathersEffects.HOT.get()).getDuration() <= 201) {

                if (player.hasEffect(FeathersEffects.COLD.get()))
                    player.removeEffect(FeathersEffects.COLD.get());

                player.addEffect(new MobEffectInstance(FeathersEffects.HOT.get(), 1200, 0, false, true));
            }
        } else if (player.hasEffect(FeathersEffects.HOT.get())) {
            player.removeEffect(FeathersEffects.HOT.get());
        }

        if (player.isCreative() && player.hasEffect(FeathersEffects.HOT.get())) {
            player.removeEffect(FeathersEffects.HOT.get());
        }
    }

    /**
     * Handle the Endurance mechanic here, where the potion leaves if the player has no endurance feathers left
     */
    private static void handleEnduranceEffect(PlayerTickEvent event) {
        if (FeathersCommonConfig.ENABLE_ENDURANCE.get()) {
            if (event.player.hasEffect(FeathersEffects.ENDURANCE.get()) && FeathersHelper.getEndurance((ServerPlayer) event.player) == 0) {
                event.player.removeEffect(FeathersEffects.ENDURANCE.get());
            }
        }
    }

    /**
     * Regenerate the player's feathers, taking the energized potion and the cold effect into account
     *
     * @param event Player Tick Event
     */
    private static void regenerateFeathers(PlayerTickEvent event) {

        Player player = event.player;

        player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).ifPresent(f -> {

            int maxCooldown = FeathersCommonConfig.COOLDOWN.get();

            if (Feathers.THIRST_LOADED && FeathersCommonConfig.THIRST_COMPATIBILITY.get()) {
                AtomicInteger thirstReduction = new AtomicInteger(0);
                AtomicInteger quenchBonus = new AtomicInteger(0);

                player.getCapability(ModCapabilities.PLAYER_THIRST).ifPresent(iThirst -> {
                    thirstReduction.set((20-iThirst.getThirst()) * FeathersCommonConfig.THIRST_REGEN_REDUCTION_MULTIPLIER.get());
                    quenchBonus.set(iThirst.getQuenched() * FeathersCommonConfig.QUENCH_REGEN_BONUS_MULTIPLIER.get());
                });
                maxCooldown += thirstReduction.get() - quenchBonus.get();

            }

            syncFeatherAttributes((ServerPlayer) player);

            if (f.getFeathers() < f.getMaxFeathers()) {

                int regen = f.getRegen();

                if (player.hasEffect(FeathersEffects.ENERGIZED.get())) {
                    regen += player.getActiveEffectsMap().get(FeathersEffects.ENERGIZED.get()).getAmplifier() + 1;
                }

                f.addCooldown(regen);
            }

            if (player.hasEffect(FeathersEffects.COLD.get())) {
                maxCooldown *= FeathersCommonConfig.COLD_EFFECT_COOLDOWN_MULTIPLIER.get();
            }


            if (f.getCooldown() >= maxCooldown) {
                FeathersHelper.addFeathers((ServerPlayer) player, 1);
            }

            f.setMaxCooldown(maxCooldown);
        });
    }

    private static void syncFeatherAttributes(ServerPlayer player) {
        int maxFeathers = (int) player.getAttributeValue(FeathersAttributes.MAX_FEATHERS.get());
        int regen = (int) player.getAttributeValue(FeathersAttributes.FEATHER_REGEN.get());
        FeathersHelper.setMaxFeathers(player, maxFeathers);
        FeathersHelper.setFeatherRegen(player, regen);
    }

    @SubscribeEvent
    public static void playerTickEvent(PlayerTickEvent event) {
        if (event.side == LogicalSide.SERVER && event.phase == TickEvent.Phase.START) {
            regenerateFeathers(event);

            if (FeathersCommonConfig.ENABLE_HOT_EFFECTS.get())
                handleHotEffect(event);

            if (FeathersCommonConfig.ENABLE_COLD_EFFECTS.get())
                handleColdEffect(event);

            handleEnduranceEffect(event);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeArmor(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getSlot().getType() == EquipmentSlot.Type.ARMOR) {
            player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).ifPresent(f -> {
                FeathersMessages.sendToPlayer(
                        new FeatherSyncSTCPacket(f.getFeathers(), f.getMaxFeathers(), f.getRegen(), FeathersHelper.getPlayerWeight(player), f.getEnduranceFeathers(), f.getMaxCooldown()), player);
            });
        }
    }

    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).isPresent()) {
                event.addCapability(new ResourceLocation(Feathers.MODID, "properties"), new PlayerFeathersProvider());
            }
        }
    }

    //TODO: Repair this
    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) {
            event.getOriginal().getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).ifPresent(oldStore -> {
                event.getOriginal().getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).ifPresent(newStore -> {
                    newStore.copyFrom(oldStore);
                });
            });
        }
    }

    @SubscribeEvent
    public static void canApplyEffect(MobEffectEvent.Applicable event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (event.getEffectInstance().getEffect() == FeathersEffects.HOT.get()) {
                event.setResult(PlayerSituationProvider.canBeHot(player) ? Event.Result.ALLOW : Event.Result.DENY);
            }

            if (event.getEffectInstance().getEffect() == FeathersEffects.COLD.get()) {
                event.setResult(PlayerSituationProvider.canBeCold(player) ? Event.Result.ALLOW : Event.Result.DENY);
            }
        }
    }

    @SubscribeEvent
    public static void onEffectApplied(MobEffectEvent.Added event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (FeathersCommonConfig.ENABLE_HOT_EFFECTS.get() &&
                    event.getEffectInstance().getEffect() == FeathersEffects.HOT.get()) {
                player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).ifPresent(f -> {
                    FeathersHelper.setHot(player, true);
                    int maxFeathers = FeathersHelper.getMaxFeathers(player);
                    if (maxFeathers >= (int) Math.round(FeathersAttributes.MAX_FEATHERS.get().getDefaultValue())) {
                        FeathersHelper.setMaxFeathers(player, maxFeathers - FeathersCommonConfig.HOT_FEATHER_REDUCTION.get());
                    }

                });
            }

            if (FeathersCommonConfig.ENABLE_COLD_EFFECTS.get() &&
                    event.getEffectInstance().getEffect() == FeathersEffects.COLD.get()) {
                FeathersHelper.setCold(player, true);

            }
        }

    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (event.getEffectInstance().getEffect() == FeathersEffects.HOT.get()) {

                FeathersHelper.setHot(player, false);
                FeathersHelper.setMaxFeathers(player, (int) Math.round(FeathersAttributes.MAX_FEATHERS.get().getDefaultValue()));
            }
            if (event.getEffectInstance().getEffect() == FeathersEffects.COLD.get()) {
                player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).ifPresent(f -> {
                    FeathersHelper.setCold(player, false);
                });
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(PlayerFeathers.class);
    }

}