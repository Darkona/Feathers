package com.elenai.feathers.compatibility.thirst;

import com.elenai.feathers.Feathers;
import com.elenai.feathers.api.ICapabilityPlugin;
import com.elenai.feathers.api.IModifier;
import com.elenai.feathers.attributes.FeathersAttributes;
import com.elenai.feathers.capability.Capabilities;
import com.elenai.feathers.capability.PlayerFeathers;
import com.elenai.feathers.event.FeatherEvent;
import com.elenai.feathers.util.Calculations;
import dev.ghen.thirst.foundation.common.capability.IThirst;
import dev.ghen.thirst.foundation.common.capability.ModCapabilities;
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

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Mod.EventBusSubscriber(modid = Feathers.MODID)
public class ThirstManager implements ICapabilityPlugin {

    private static ICapabilityPlugin instance;

    public static ICapabilityPlugin getInstance() {
        if (instance == null) {
            instance = new ThirstManager();
        }
        return instance;
    }

    public static final IModifier THIRSTY = new IModifier() {

        private final UUID uuid = UUID.fromString("9f2141c4-33bc-4245-97bd-b64a3eceafda");
        @Override
        public void apply(Player player, PlayerFeathers playerFeathers, AtomicInteger staminaDelta) {

            player.getCapability(ModCapabilities.PLAYER_THIRST).ifPresent(iThirst -> {

                var maxThirst = 20;
                var thirstCalculationEvent = new ThirstEvent(player, playerFeathers, ModCapabilities.PLAYER_THIRST);
                var cancelled = MinecraftForge.EVENT_BUS.post(thirstCalculationEvent);

                if (!cancelled && thirstCalculationEvent.getResult() == Event.Result.DEFAULT) {

                    thirstCalculationEvent.calculationResult = (iThirst.getThirst() - maxThirst) * FeathersThirstConfig.THIRST_STAMINA_DRAIN.get();

                    var fps = Calculations.calculateFeathersPerSecond(thirstCalculationEvent.calculationResult) * -1;
                    var modifier = new AttributeModifier(Feathers.MODID + ":thirsty", fps, AttributeModifier.Operation.ADDITION);

                    var attr = player.getAttribute(FeathersAttributes.FEATHERS_PER_SECOND.get());
                    if (attr != null){
                        attr.removeModifier(uuid);
                        attr.addPermanentModifier(modifier);
                    }
                }
            });
        }

        @Override
        public int getOrdinal() {
            return 2;
        }

        @Override
        public String getName() {
            return "thirsty";
        }
    };


    public static final IModifier QUENCHED = new IModifier() {

        private final UUID uuid = UUID.fromString("08c665cf-0c4a-4f87-92da-c63972f19b73");
        @Override
        public void apply(Player player, PlayerFeathers playerFeathers, AtomicInteger staminaDelta) {

            player.getCapability(ModCapabilities.PLAYER_THIRST).ifPresent(iThirst -> {

                var calculation = new ThirstEvent(player, playerFeathers, ModCapabilities.PLAYER_THIRST);
                var cancelled = MinecraftForge.EVENT_BUS.post(new ThirstEvent(player, playerFeathers, ModCapabilities.PLAYER_THIRST));

                if (!cancelled && calculation.getResult() == Event.Result.DEFAULT) {

                    calculation.calculationResult = iThirst.getQuenched() * FeathersThirstConfig.THIRST_STAMINA_DRAIN.get();

                    var fps = Calculations.calculateFeathersPerSecond(calculation.calculationResult);

                    var modifier = new AttributeModifier(uuid, Feathers.MODID + ":quenched", fps, AttributeModifier.Operation.ADDITION);

                    var attr = player.getAttribute(FeathersAttributes.FEATHERS_PER_SECOND.get());
                    if (attr != null){
                        attr.removeModifier(uuid);
                        attr.addPermanentModifier(modifier);
                    }
                }
            });
        }

        @Override
        public int getOrdinal() {
            return 2;
        }

        @Override
        public String getName() {
            return "quenched";
        }
    };

    private static final String LAST_THIRST_LEVEL = "lastThirstLevel";
    private static final String LAST_QUENCH_LEVEL = "lastQuenchLevel";

    @SubscribeEvent
    public static void onAttachDefaultDeltaModifiers(FeatherEvent.AttachDefaultDeltaModifiers event) {

        if (Feathers.THIRST_LOADED && FeathersThirstConfig.THIRST_COMPATIBILITY.get()) {

            if (FeathersThirstConfig.THIRST_STAMINA_DRAIN.get() > 0)
                event.modifiers.add(THIRSTY);

            if (FeathersThirstConfig.QUENCH_REGEN_BONUS_MULTIPLIER.get() > 0)
                event.modifiers.add(QUENCHED);

        }
    }

    @Override
    public void onPlayerJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Player player) {
            player.getCapability(Capabilities.PLAYER_FEATHERS).ifPresent(f -> {
                if (!f.hasCounter(LAST_QUENCH_LEVEL))
                    f.addCounter(LAST_QUENCH_LEVEL, 0);

                if (!f.hasCounter(LAST_THIRST_LEVEL))
                    f.addCounter(LAST_THIRST_LEVEL, 0);

                player.getCapability(ModCapabilities.PLAYER_THIRST).ifPresent(iThirst -> {
                    f.setCounter(LAST_THIRST_LEVEL, iThirst.getThirst());
                    f.setCounter(LAST_QUENCH_LEVEL, iThirst.getQuenched());
                });
            });

        }
    }

    @Override
    public void onPlayerTickBefore(TickEvent.PlayerTickEvent event) {

        if (!FeathersThirstConfig.enableThirst()) return;
        if (!(event.player.tickCount % 20 == 0)) return;
        event.player.getCapability(Capabilities.PLAYER_FEATHERS).ifPresent(f -> {
            event.player.getCapability(ModCapabilities.PLAYER_THIRST).ifPresent(t -> {

                f.getCounter(LAST_THIRST_LEVEL).ifPresent(lastThirst -> {
                    if (lastThirst != t.getThirst()) {
                        f.setCounter(LAST_THIRST_LEVEL, t.getThirst());
                        f.setShouldRecalculate(true);
                    }
                });

                f.getCounter(LAST_QUENCH_LEVEL).ifPresent(lastQuench -> {
                    if (lastQuench != t.getQuenched()) {
                        f.setCounter(LAST_QUENCH_LEVEL, t.getQuenched());
                        f.setShouldRecalculate(true);
                    }
                });
            });
        });


    }

    @Override
    public void onPlayerTickAfter(TickEvent.PlayerTickEvent event) {

    }

    public static class ThirstEvent extends PlayerEvent {

        public int calculationResult;
        public PlayerFeathers playerFeathers;

        public Capability<IThirst> thirst;

        public ThirstEvent(Player player, PlayerFeathers playerFeathers, Capability<IThirst> thirst) {
            super(player);
            this.playerFeathers = playerFeathers;
            this.thirst = thirst;
        }

    }


}
