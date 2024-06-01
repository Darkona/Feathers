package com.elenai.feathers;

import com.elenai.feathers.api.FeathersAPI;
import com.elenai.feathers.api.ICapabilityPlugin;
import com.elenai.feathers.api.IFeathers;
import com.elenai.feathers.api.StaminaAPI;
import com.elenai.feathers.attributes.FeathersAttributes;
import com.elenai.feathers.capability.Capabilities;
import com.elenai.feathers.capability.PlayerFeathers;
import com.elenai.feathers.config.FeathersCommonConfig;
import com.elenai.feathers.effect.FeathersEffects;
import com.elenai.feathers.effect.effects.EnduranceEffect;
import com.elenai.feathers.networking.FeathersMessages;
import com.elenai.feathers.networking.packet.FeatherSyncSTCPacket;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Feathers.MODID)
public class FeathersManager {

    private static final String THIRST_COUNTER = "thirst";

    private static final List<ICapabilityPlugin> plugins = new ArrayList<>();

    private FeathersManager() {}

    public static void registerPlugin(ICapabilityPlugin plugin) {
        plugins.add(plugin);
    }

    public static void deRegisterPlugin(ICapabilityPlugin plugin) {
        plugins.remove(plugin);
    }

    @SubscribeEvent
    public static void attachCapabilityToEntity(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            if (!event.getObject().getCapability(Capabilities.PLAYER_FEATHERS).isPresent()) {
                event.addCapability(new ResourceLocation(Feathers.MODID, "properties"), getProvider());
            }
        }
    }

    @NotNull
    private static ICapabilityProvider getProvider() {
        final Capability<IFeathers> capability = Capabilities.PLAYER_FEATHERS;
        return new ICapabilitySerializable<CompoundTag>() {

            final IFeathers feathersCapability = new PlayerFeathers();
            final LazyOptional<IFeathers> capOptional = LazyOptional.of(() -> feathersCapability);

            @Nonnull
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction direction) {
                return cap == capability ? capOptional.cast() : LazyOptional.empty();
            }

            public CompoundTag serializeNBT() {
                return feathersCapability.saveNBTData();
            }

            public void deserializeNBT(CompoundTag nbt) {
                feathersCapability.loadNBTData(nbt);
            }
        };
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if (!event.isWasDeath() && !event.getEntity().level().isClientSide) {
            Player oldPlayer = event.getOriginal();
            oldPlayer.reviveCaps();

            event.getOriginal().getCapability(Capabilities.PLAYER_FEATHERS).ifPresent(oldStore -> {
                event.getOriginal().getCapability(Capabilities.PLAYER_FEATHERS).ifPresent(newStore -> {
                    newStore.copyFrom(oldStore);
                });
            });

            oldPlayer.invalidateCaps();
        }
    }

    @SubscribeEvent
    public static void onPlayerJoinWorld(EntityJoinLevelEvent event) {
        Level level = event.getLevel();

        if (event.getEntity() instanceof Player player) {

            assignFeathersAttributes(player);

            player.getCapability(Capabilities.PLAYER_FEATHERS).ifPresent(f -> {
                f.setShouldRecalculate();
                FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), player);
            });

            plugins.forEach(p -> p.onPlayerJoin(event));
        }


    }

    @SubscribeEvent
    public static void onPlayerSleep(PlayerWakeUpEvent event) {
        if (!FeathersCommonConfig.SLEEPING_ALWAYS_RESTORES_FEATHERS.get()) return;

        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(Capabilities.PLAYER_FEATHERS).ifPresent(f -> {

                f.setStamina(f.getMaxStamina());
                f.setStrainFeathers(0);
                FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), player);
            });
        }

    }

    @SubscribeEvent
    public static void onPlayerChangeArmor(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getSlot().getType() == EquipmentSlot.Type.ARMOR) {
            player.getCapability(Capabilities.PLAYER_FEATHERS).ifPresent(f -> {

                FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), player);
            });
        }
    }

    @SubscribeEvent
    public static void onConfig(ModConfigEvent event) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        server.getPlayerList().getPlayers().forEach(player -> {

            assignFeathersAttributes(player);

            player.getCapability(Capabilities.PLAYER_FEATHERS).ifPresent(f -> {
                f.setShouldRecalculate();
                f.recalculateStaminaDelta(player);
            });
        });
    }

    public static void assignFeathersAttributes(Player player) {
        var attr = player.getAttribute(FeathersAttributes.MAX_FEATHERS.get());

        if (attr != null && attr.getBaseValue() != FeathersCommonConfig.MAX_FEATHERS.get()) {
            attr.setBaseValue((FeathersCommonConfig.MAX_FEATHERS.get()));
        }

        if (attr != null && attr.getBaseValue() != FeathersCommonConfig.REGEN_FEATHERS_PER_SECOND.get()) {
            attr.setBaseValue((FeathersCommonConfig.REGEN_FEATHERS_PER_SECOND.get()));
        }

    }

    @SubscribeEvent
    public static void playerTickEvent(TickEvent.PlayerTickEvent event) {
        Player player = event.player;

        if (!player.isAlive() || player.isCreative() || player.isSpectator()) return;

        plugins.forEach(p -> p.onPlayerTickBefore(event));

        player.getCapability(Capabilities.PLAYER_FEATHERS).ifPresent(f -> f.tick(player));

        plugins.forEach(p -> p.onPlayerTickAfter(event));

        checkEffects(player);
    }

    private static void checkEffects(Player player) {
        player.getCapability(Capabilities.PLAYER_FEATHERS).ifPresent(f -> {
            if (FeathersAPI.isEnduring(player) && f.getCounter(EnduranceEffect.ENDURANCE_COUNTER).orElse(0) == 0) {

                player.removeEffect(FeathersEffects.ENDURANCE.get());
            }
        });

    }

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        if (event.getEntity() instanceof Player player && event.getEffectInstance().getEffect() instanceof FeathersEffects effect) {
            player.getCapability(Capabilities.PLAYER_FEATHERS).ifPresent(f -> {
                f.addCounter(EnduranceEffect.ENDURANCE_COUNTER, (event.getEffectInstance().getAmplifier() + 1) * 4);
                StaminaAPI.addStaminaUsageModifier(player, EnduranceEffect.ENDURANCE);
            });
        }
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEntity() instanceof Player player && player.hasEffect(FeathersEffects.ENDURANCE.get())) {
            player.getCapability(Capabilities.PLAYER_FEATHERS).ifPresent(f -> {
                f.removeCounter(EnduranceEffect.ENDURANCE_COUNTER);
                StaminaAPI.removeStaminaUsageModifier(player, EnduranceEffect.ENDURANCE);
            });
        }
    }
}
