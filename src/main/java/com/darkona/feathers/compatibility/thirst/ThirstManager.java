package com.darkona.feathers.compatibility.thirst;

import com.darkona.feathers.Feathers;
import com.darkona.feathers.api.ICapabilityPlugin;
import com.darkona.feathers.api.IFeathers;
import com.darkona.feathers.attributes.FeathersAttributes;
import com.darkona.feathers.capability.Capabilities;
import com.darkona.feathers.util.Calculations;
import dev.ghen.thirst.foundation.common.capability.IThirst;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;

import java.util.UUID;

import static com.darkona.feathers.compatibility.thirst.FeathersThirstConfig.THIRST_STAMINA_DRAIN;
import static dev.ghen.thirst.foundation.common.capability.ModCapabilities.PLAYER_THIRST;
import static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION;

public class ThirstManager implements ICapabilityPlugin {

    private static final String LAST_THIRST_LEVEL = "lastThirst";
    private static final String LAST_QUENCH_LEVEL = "lastQuench";
    private static ICapabilityPlugin instance;
    private final UUID QUENCH_UUID = UUID.fromString("08c665cf-0c4a-4f87-92da-c63972f19b73");
    private final UUID THIRST_UUID = UUID.fromString("9f2141c4-33bc-4245-97bd-b64a3eceafda");

    public static ICapabilityPlugin getInstance() {
        if (instance == null) {
            instance = new ThirstManager();
        }
        return instance;
    }

    @Override
    public void onPlayerJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Player player) {
            player.getCapability(Capabilities.PLAYER_FEATHERS).ifPresent(f -> player.getCapability(PLAYER_THIRST).ifPresent(iThirst -> {
                f.setCounter(LAST_THIRST_LEVEL, iThirst.getThirst());
                f.setCounter(LAST_QUENCH_LEVEL, iThirst.getQuenched());
            }));
        }
    }

    private void recalculateQuench(Player player, IFeathers playerFeathers) {
        player.getCapability(PLAYER_THIRST).ifPresent(iThirst -> {

            var calculation = new ThirstEvent(player, playerFeathers, PLAYER_THIRST);
            var cancelled = MinecraftForge.EVENT_BUS.post(new ThirstEvent(player, playerFeathers, PLAYER_THIRST));

            if (!cancelled && calculation.getResult() == Event.Result.DEFAULT) {

                calculation.calculationResult = iThirst.getQuenched() * THIRST_STAMINA_DRAIN.get();

                var fps = Calculations.calculateFeathersPerSecond(calculation.calculationResult);

                var modifier = new AttributeModifier(QUENCH_UUID, Feathers.MODID + ":quenched", fps, ADDITION);

                var attr = player.getAttribute(FeathersAttributes.FEATHERS_PER_SECOND.get());
                if (attr != null) {
                    attr.removeModifier(QUENCH_UUID);
                    attr.addPermanentModifier(modifier);
                }
            }
        });
    }

    private void recalculateThirst(Player player, IFeathers playerFeathers) {
        player.getCapability(PLAYER_THIRST).ifPresent(iThirst -> {

            var maxThirst = 20;
            var thirstCalculationEvent = new ThirstEvent(player, playerFeathers, PLAYER_THIRST);
            var cancelled = MinecraftForge.EVENT_BUS.post(thirstCalculationEvent);

            if (!cancelled && thirstCalculationEvent.getResult() == Event.Result.DEFAULT) {

                thirstCalculationEvent.calculationResult = (iThirst.getThirst() - maxThirst) * THIRST_STAMINA_DRAIN.get();

                var fps = Calculations.calculateFeathersPerSecond(thirstCalculationEvent.calculationResult) * -1;
                var modifier = new AttributeModifier(Feathers.MODID + ":thirsty", fps, ADDITION);

                var attr = player.getAttribute(FeathersAttributes.FEATHERS_PER_SECOND.get());
                if (attr != null) {
                    attr.removeModifier(QUENCH_UUID);
                    attr.addPermanentModifier(modifier);
                }
            }
        });
    }

    @Override
    public void onPlayerTickBefore(TickEvent.PlayerTickEvent event) {

        if (!FeathersThirstConfig.isThirstOn()) return;
        if (!(event.player.tickCount % 20 == 0)) return;
        event.player.getCapability(Capabilities.PLAYER_FEATHERS).ifPresent(f -> event.player.getCapability(PLAYER_THIRST).ifPresent(t -> {

            f.getCounter(LAST_THIRST_LEVEL).ifPresent(lastThirst -> {
                if (lastThirst != t.getThirst()) {
                    f.setCounter(LAST_THIRST_LEVEL, t.getThirst());
                    recalculateThirst(event.player, f);
                }
            });

            f.getCounter(LAST_QUENCH_LEVEL).ifPresent(lastQuench -> {
                if (lastQuench != t.getQuenched()) {
                    f.setCounter(LAST_QUENCH_LEVEL, t.getQuenched());
                    recalculateQuench(event.player, f);
                }
            });
        }));


    }

    @Override
    public void onPlayerTickAfter(TickEvent.PlayerTickEvent event) {


    }

    public static class ThirstEvent extends PlayerEvent {

        public int calculationResult;
        public IFeathers playerFeathers;

        public Capability<IThirst> thirst;

        public ThirstEvent(Player player, IFeathers playerFeathers, Capability<IThirst> thirst) {
            super(player);
            this.playerFeathers = playerFeathers;
            this.thirst = thirst;
        }

    }


}
