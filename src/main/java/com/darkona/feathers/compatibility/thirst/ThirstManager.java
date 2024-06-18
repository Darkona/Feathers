package com.darkona.feathers.compatibility.thirst;

import com.darkona.feathers.Feathers;
import com.darkona.feathers.api.Constants;
import com.darkona.feathers.api.ICapabilityPlugin;
import com.darkona.feathers.api.IFeathers;
import com.darkona.feathers.attributes.FeathersAttributes;
import com.darkona.feathers.capability.FeathersCapabilities;
import com.darkona.feathers.event.StaminaChangeEvent;
import dev.ghen.thirst.foundation.common.capability.IThirst;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

import static com.darkona.feathers.compatibility.thirst.FeathersThirstConfig.THIRST_FEATHER_REGEN_REDUCTION;
import static dev.ghen.thirst.foundation.common.capability.ModCapabilities.PLAYER_THIRST;
import static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION;

@Mod.EventBusSubscriber(modid = Feathers.MODID)
public class ThirstManager implements ICapabilityPlugin {


    private static final UUID QUENCH_UUID = UUID.fromString("08c665cf-0c4a-4f87-92da-c63972f19b73");
    private static final UUID THIRST_UUID = UUID.fromString("9f2141c4-33bc-4245-97bd-b64a3eceafda");
    private static final String LAST_THIRST_LEVEL = "lastThirst";
    private static final String LAST_QUENCH_LEVEL = "lastQuench";
    private static final String THIRST_FEATHER_COUNTER = "thirstFeather";
    private static final String THIRST_ACCUMULATOR = "thirstAcc";
    private static ICapabilityPlugin instance;

    public static ICapabilityPlugin getInstance() {
        if (instance == null) {
            instance = new ThirstManager();
        }
        return instance;
    }

    @SubscribeEvent
    public static void onStaminaChanged(StaminaChangeEvent.Post event) {
        var player = event.getEntity();
        var f = event.feathers;
        if (f.getStamina() > f.getPrevStamina()) {

            f.incrementCounterBy(THIRST_FEATHER_COUNTER, f.getStaminaDelta());

            if (f.getCounter(THIRST_FEATHER_COUNTER) >= Constants.STAMINA_PER_FEATHER) {
                f.incrementCounterBy(THIRST_FEATHER_COUNTER, -Constants.STAMINA_PER_FEATHER);
                f.incrementCounterBy(THIRST_ACCUMULATOR, FeathersThirstConfig.THIRST_CONSUMPTION_BY_FEATHER.get());
            }

            if (f.getCounter(THIRST_ACCUMULATOR) >= 1) {
                f.incrementCounterBy(THIRST_ACCUMULATOR, -1);
                player.getCapability(PLAYER_THIRST).ifPresent(thirst -> thirst.setThirst(thirst.getThirst() - 1));
            }
        }
    }

    @Override
    public void onPlayerJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Player player) {
            player.getCapability(FeathersCapabilities.PLAYER_FEATHERS).ifPresent(f -> {
                f.setCounter(LAST_THIRST_LEVEL, 0);
                f.setCounter(LAST_QUENCH_LEVEL, 0);
                f.setCounter(THIRST_FEATHER_COUNTER, 0);
                f.setCounter(THIRST_ACCUMULATOR, 0);
            });
        }
    }

    private void recalculateThirst(Player player, IFeathers playerFeathers) {
        player.getCapability(PLAYER_THIRST).ifPresent(iThirst -> {

            var maxThirst = 20;
            var event = new ThirstEvent(player, playerFeathers, PLAYER_THIRST);
            var cancelled = MinecraftForge.EVENT_BUS.post(event);

            if (!cancelled && event.getResult() == Event.Result.DEFAULT) {
                event.calculationResult = (iThirst.getThirst() - maxThirst) * THIRST_FEATHER_REGEN_REDUCTION.get();
            }

            var attr = player.getAttribute(FeathersAttributes.FEATHERS_PER_SECOND.get());
            if (attr != null) {
                attr.removeModifier(THIRST_UUID);
                attr.addPermanentModifier(new AttributeModifier(Feathers.MODID + ":thirsty", event.calculationResult, ADDITION));
            }
        });
    }

    private void recalculateQuench(Player player, IFeathers playerFeathers) {
        player.getCapability(PLAYER_THIRST).ifPresent(iThirst -> {

            var calculation = new ThirstEvent(player, playerFeathers, PLAYER_THIRST);
            var cancelled = MinecraftForge.EVENT_BUS.post(new ThirstEvent(player, playerFeathers, PLAYER_THIRST));

            if (!cancelled && calculation.getResult() == Event.Result.DEFAULT) {
                calculation.calculationResult = iThirst.getQuenched() * THIRST_FEATHER_REGEN_REDUCTION.get();
            }

            var attr = player.getAttribute(FeathersAttributes.FEATHERS_PER_SECOND.get());
            if (attr != null) {
                attr.removeModifier(QUENCH_UUID);
                attr.addPermanentModifier(new AttributeModifier(QUENCH_UUID, Feathers.MODID + ":quenched", calculation.calculationResult, ADDITION));
            }
        });
    }

    @Override
    public void onPlayerTickBefore(TickEvent.PlayerTickEvent event) {

        if (!FeathersThirstConfig.isThirstOn()) return;
        if (!(event.player.tickCount % 20 == 0)) return;
        event.player.getCapability(FeathersCapabilities.PLAYER_FEATHERS).ifPresent(f -> event.player.getCapability(PLAYER_THIRST).ifPresent(t -> {

            var lastThirst = f.getCounter(LAST_THIRST_LEVEL);
            var lastQuench = f.getCounter(LAST_QUENCH_LEVEL);
            if (lastThirst != t.getThirst()) {
                f.setCounter(LAST_THIRST_LEVEL, t.getThirst());
                recalculateThirst(event.player, f);
            }

            if (lastQuench != t.getQuenched()) {
                f.setCounter(LAST_QUENCH_LEVEL, t.getQuenched());
                recalculateQuench(event.player, f);
            }

        }));

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

    public static class ThirstEvent extends PlayerEvent {

        public double calculationResult;
        public IFeathers playerFeathers;

        public Capability<IThirst> thirst;

        public ThirstEvent(Player player, IFeathers playerFeathers, Capability<IThirst> thirst) {
            super(player);
            this.playerFeathers = playerFeathers;
            this.thirst = thirst;
        }

    }


}
