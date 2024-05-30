package com.elenai.feathers.handler;

import com.elenai.feathers.Feathers;
import com.elenai.feathers.api.FeathersConstants;
import com.elenai.feathers.capability.PlayerFeathers;
import com.elenai.feathers.capability.PlayerFeathersProvider;
import com.elenai.feathers.config.FeathersCommonConfig;
import com.elenai.feathers.config.FeathersThirstConfig;
import com.elenai.feathers.effect.FeathersEffects;
import com.elenai.feathers.event.FeatherAmountEvent;
import com.elenai.feathers.event.StaminaChangeEvent;
import com.elenai.feathers.networking.FeathersMessages;
import com.elenai.feathers.networking.packet.FeatherSyncSTCPacket;
import com.momosoftworks.coldsweat.api.util.Temperature;
import dev.ghen.thirst.foundation.common.capability.ModCapabilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicInteger;

@Mod.EventBusSubscriber(modid = Feathers.MODID)
public class StaminaDeltaTickHandler {

    static Logger log = LogManager.getLogger(Feathers.MODID);

    /**
     * Apply stamina delta to the player's stamina value. Handles regeneration, effects, etc.
     *
     * @param event Player Tick Event
     */
    public static void applyStaminaChanges(TickEvent.PlayerTickEvent event) {

        Player player = event.player;
        if (!player.isAlive() || player.isCreative() || player.isSpectator()) return;

        player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).ifPresent(f -> {

                    //If there is cooldown to start regenerating, it will go down before attempting to regenerate again.
                    if(f.getCooldown() > 0){
                        f.setCooldown(f.getCooldown() - 1);
                        return;
                    }

                    //If there was any change in the delta modifiers, recalculate.
                    //Internal logic will only run if the flag is set to true
                    f.recalculateStaminaDelta(player);

                    //Event to see if something modifies the stamina delta before applying it and after existing modifiers have been applied.

                    int prevStamina = f.getStamina();

                    var preChangeEvent = new StaminaChangeEvent.Pre(player, f.getStaminaDelta(), f.getStamina());
                    var cancelled = MinecraftForge.EVENT_BUS.post(preChangeEvent);

                    if  (cancelled) return;
                    if(preChangeEvent.getResult() == Event.Result.DENY) return;

                    f.setStaminaDelta(preChangeEvent.prevStaminaDelta);
                    f.setStamina(preChangeEvent.prevStamina);

                    f.applyStaminaDelta();

                    //If the stamina delta is not zero, then the player's stamina will change
                    if (f.getStaminaDelta() != 0) {

                        if (f.getStaminaDelta() > 0) {
                            //If the stamina delta is positive, only apply if we are not at max stamina. Avoid pointless operations.
                            if (f.getStamina() < f.getMaxStamina()) f.applyStaminaDelta();
                        } else {
                            //If the stamina delta is negative, only apply if we are not at zero stamina. Avoid pointless operations.
                            if (f.getStamina() > 0) f.applyStaminaDelta();
                        }
                    }

                    //If there was any change in stamina
                    if (prevStamina != f.getStamina()) {
                        MinecraftForge.EVENT_BUS.post(new StaminaChangeEvent.Post(player, prevStamina, f.getStamina()));
                        if (f.getStamina() <= 0) {

                            MinecraftForge.EVENT_BUS.post(new FeatherAmountEvent.Empty(player, prevStamina));
                            FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), (ServerPlayer) player);

                        } else if (f.getStamina() == f.getMaxStamina()) {

                            MinecraftForge.EVENT_BUS.post(new FeatherAmountEvent.Full(player));
                            FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), (ServerPlayer) player);

                        } else if (f.getStamina() % FeathersConstants.STAMINA_PER_FEATHER == 0) {

                            FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), (ServerPlayer) player);
                        }
                        if(f.getStamina() < 0){
                            player.addEffect(new MobEffectInstance(FeathersEffects.STRAIN.get(),-1, 0, false, true));

                        }
                    }

                }
        );
    }

    @SubscribeEvent
    public static void applyStrain(FeatherAmountEvent.Empty event) {
        event.getEntity().getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).ifPresent(f -> {
            if (FeathersCommonConfig.ENABLE_STRAIN.get()) {
                if (event.prevStamina > 0) {
                    //event.getEntity().addEffect(FeathersEffects.STRAIN);
                } else {
                    //event.getEntity().removeEffect(StrainEffect.INSTANCE.get());
                }
            }
        });
    }

    private static void logStuff(Player player, PlayerFeathers f) {
        log.info("Stamina: " + f.getStamina() + " Max Stamina: " + f.getMaxStamina() + " Delta: " + f.getStaminaDelta());
        if (player.isAlive()) {
            var core = Temperature.get(player, Temperature.Trait.CORE);
            var body = Temperature.get(player, Temperature.Trait.BODY);
            var base = Temperature.get(player, Temperature.Trait.BASE);
            log.info("Temperature Core = " + core + " Body = " + body + " Base = " + base);
            log.info(Temperature.get(player, Temperature.Trait.BURNING_POINT));
            log.info(Temperature.get(player, Temperature.Trait.FREEZING_POINT));
        }
    }

    private static int applyThirstModifiers(Player player, int maxCooldown) {
        if (Feathers.THIRST_LOADED && FeathersThirstConfig.THIRST_COMPATIBILITY.get()) {
            AtomicInteger thirstReduction = new AtomicInteger(0);
            AtomicInteger quenchBonus = new AtomicInteger(0);

            player.getCapability(ModCapabilities.PLAYER_THIRST).ifPresent(iThirst -> {

                quenchBonus.set(iThirst.getQuenched() * FeathersThirstConfig.QUENCH_REGEN_BONUS_MULTIPLIER.get());
            });
            maxCooldown += thirstReduction.get() - quenchBonus.get();

        }
        return maxCooldown;
    }

}
