package com.darkona.feathers.capability;

import com.darkona.feathers.Feathers;
import com.darkona.feathers.FeathersManager;
import com.darkona.feathers.api.*;
import com.darkona.feathers.config.FeathersCommonConfig;
import com.darkona.feathers.effect.effects.StrainEffect;
import com.darkona.feathers.event.FeatherAmountEvent;
import com.darkona.feathers.event.FeatherEvent;
import com.darkona.feathers.event.StaminaChangeEvent;
import com.darkona.feathers.networking.FeathersMessages;
import com.darkona.feathers.networking.packet.FeatherSTCSyncPacket;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.network.NetworkEvent;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@Getter
@Setter
public class PlayerFeathers implements IFeathers {

    private static final int ZERO = 0;
    private int maxStamina;
    private int stamina;
    private int prevStamina;
    private int feathers;
    private int prevFeathers;
    private int cooldown;
    private int strainFeathers;
    private int maxStrained;
    private int weight;
    private boolean shouldCooldown = true;
    private int staminaDelta;

    @Setter(AccessLevel.NONE)
    private boolean dirty;


    private Map<String, Double> counters = new HashMap<>();

    private Map<String, IModifier> staminaDeltaModifiers = new HashMap<>();
    private List<IModifier> staminaDeltaModifierList = new ArrayList<>();

    private Map<String, IModifier> staminaUsageModifiers = new HashMap<>();
    private List<IModifier> featherUsageModifiersList = new ArrayList<>();

    /* Initialization */

    public PlayerFeathers() {
        maxStamina = FeathersCommonConfig.MAX_FEATHERS.get() * Constants.STAMINA_PER_FEATHER;
        stamina = maxStamina;

        cooldown = ZERO;
        strainFeathers = ZERO;
        maxStrained = FeathersCommonConfig.MAX_STRAIN.get();

        attachDefaultDeltaModifiers();

        attachDefaultUsageModifiers();

        FeathersManager.getPlugins().forEach(ICapabilityPlugin::attachDeltaModifiers);
        FeathersManager.getPlugins().forEach(ICapabilityPlugin::attackUsageModifiers);

        synchronizeFeathers();

        dirty = true;
    }

    private void attachDefaultDeltaModifiers() {

        var modifiers = new ArrayList<IModifier>();
        var event = new FeatherEvent.AttachDefaultDeltaModifiers(modifiers);

        if (MinecraftForge.EVENT_BUS.post(event) || event.getResult() == Event.Result.DENY) return;

        if (event.getResult() == Event.Result.DEFAULT) {
            modifiers.add(IModifier.REGENERATION);
            if (FeathersCommonConfig.ENABLE_STRAIN.get()) {
                modifiers.add(StrainEffect.STRAIN_MODIFIER);
            }
        }

        event.modifiers.forEach(m -> {
            staminaDeltaModifiers.put(m.getName(), m);
            m.onAdd(this);
        });

        sortDeltaModifiers();
    }

    private void attachDefaultUsageModifiers() {

        var event = new FeatherEvent.AttachDefaultUsageModifiers(new ArrayList<>());

        if (MinecraftForge.EVENT_BUS.post(event) || event.getResult() == Event.Result.DENY) return;

        if (event.getResult() == Event.Result.DEFAULT) {
            event.modifiers.add(IModifier.DEFAULT_USAGE);

            if (FeathersCommonConfig.ENABLE_STRAIN.get()) {
                event.modifiers.add(StrainEffect.STRAIN_MODIFIER);
            }
        }

        event.modifiers.forEach(m -> {
            staminaUsageModifiers.put(m.getName(), m);
            m.onAdd(this);
        });
        sortUsageModifiers();
    }

    /* Counters*/
    public void removeCounter(String name) {
        counters.remove(name);
    }

    public boolean hasCounter(String name) {
        return counters.containsKey(name);
    }

    public Double getCounter(String name) {
        return Optional.ofNullable(counters.get(name)).orElse(0D);
    }

    public void setCounter(String name, double value) {
        if (name.length() > 16) name = name.substring(0, 16);
        counters.put(name, value);
    }

    public void incrementCounterBy(String name, double amount) {
        counters.computeIfPresent(name, (k, v) -> v + amount);
    }

    public void multiplyCounterBy(String name, double amount) {
        counters.computeIfPresent(name, (k, v) -> v * amount);
    }

    @Override
    public void updateInClient(FeatherSTCSyncPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        assert Minecraft.getInstance().level != null;
        if (Minecraft.getInstance().level.isClientSide) {
            this.stamina = message.stamina;
            this.maxStamina = message.maxStamina;
            this.feathers = message.feathers;
            this.staminaDelta = message.staminaDelta;
            this.cooldown = message.cooldown;
            this.weight = message.weight;
            message.counters.forEach(this::setCounter);
        }
    }

    /* Markers */
    @Override
    public int getMaxFeathers() {
        return maxStamina / Constants.STAMINA_PER_FEATHER;
    }

    public void markDirty() {
        dirty = true;
    }


    /* Normal getters and setters */
    @Override
    public int getFeathers() {

        return feathers;
    }

    @Override
    public void setFeathers(int feathers) {
        setStamina(feathers * Constants.STAMINA_PER_FEATHER);
    }

    @Override
    public int getAvailableFeathers() {
        if (FeathersCommonConfig.ENABLE_ARMOR_WEIGHTS.get()) {
            return Math.max(feathers - weight, ZERO);
        }
        return feathers;
    }

    public int getAvailableStamina() {
        if (FeathersCommonConfig.ENABLE_ARMOR_WEIGHTS.get()) {
            return Math.max(stamina - (weight * Constants.STAMINA_PER_FEATHER), ZERO);
        }
        return stamina;
    }

    @Override
    public void setStamina(int stamina) {
        this.stamina = Math.min(Math.max(stamina, ZERO), maxStamina);
        synchronizeFeathers();
    }

    private void synchronizeFeathers() {
        feathers = stamina / Constants.STAMINA_PER_FEATHER;
    }

    /* Delta and usage modifiers*/
    @Override
    public void addDeltaModifier(IModifier modifier) {
        staminaDeltaModifiers.put(modifier.getName(), modifier);
        modifier.onAdd(this);
        sortDeltaModifiers();
    }

    @Override
    public void removeDeltaModifier(IModifier modifier) {
        if (staminaDeltaModifiers.remove(modifier.getName()) != null) {
            modifier.onRemove(this);
            sortDeltaModifiers();
        }
    }


    private void sortDeltaModifiers() {
        staminaDeltaModifierList = new ArrayList<>(staminaDeltaModifiers.values());
        staminaDeltaModifierList.sort(Comparator.comparingInt(IModifier::getDeltaOrdinal));
    }

    @Override
    public void addUsageModifier(IModifier modifier) {
        staminaUsageModifiers.put(modifier.getName(), modifier);
        modifier.onAdd(this);
        sortUsageModifiers();
    }

    @Override
    public void removeUsageModifier(IModifier modifier) {
        if (staminaDeltaModifiers.remove(modifier.getName()) != null) {
            modifier.onRemove(this);
            sortUsageModifiers();
        }

    }

    private void sortUsageModifiers() {
        featherUsageModifiersList = new ArrayList<>(staminaUsageModifiers.values());
        featherUsageModifiersList.sort(Comparator.comparingInt(IModifier::getUsageOrdinal));
    }


    /*Stamina delta calculations*/

    public void applyStaminaDelta() {

        stamina += staminaDelta;

        if (stamina > maxStamina) stamina = maxStamina;

        if (stamina < ZERO) stamina = ZERO;

        synchronizeFeathers();
    }


    /* Usage */
    @Override
    public boolean gainFeathers(int feathers) {
        var prev = this.feathers;
        addStamina(feathers * Constants.STAMINA_PER_FEATHER);
        return feathers != prev;
    }

    @Override
    public boolean useFeathers(Player player, int feathers, int cooldown) {

        if (FeathersCommonConfig.DEBUG_MODE.get() && !player.level().isClientSide) {
            Feathers.logger.info("Requested to use {} feathers.", feathers);
        }

        var multiplier = FeathersAPI.getPlayerStaminaUsageMultiplier(player);
        var staminaToUse = new AtomicInteger((int) (feathers * multiplier * Constants.STAMINA_PER_FEATHER));
        var approve = new AtomicBoolean(false);

        for (IModifier m : featherUsageModifiersList) {
            m.applyToUsage(player, this, staminaToUse, approve);
        }

        if (approve.get()) {
            if (FeathersCommonConfig.DEBUG_MODE.get() && !player.level().isClientSide) {
                Feathers.logger.info("Using {} stamina.", staminaToUse);
            }
            subtractStamina(staminaToUse.get());
            this.cooldown = Math.min(cooldown + this.cooldown, FeathersCommonConfig.MAX_COOLDOWN.get() * 20);
            synchronizeFeathers();
        }

        return approve.get();
    }

    private void addStamina(int stamina) {
        this.stamina = Math.min(this.stamina + stamina, maxStamina);
    }

    private void subtractStamina(int subtraction) {
        if (FeathersCommonConfig.DEBUG_MODE.get()) {
            Feathers.logger.info("Before subtracting: Stamina: {}, Weight: {}, AvailableStamina: {}, StaminaToRemove: {}", stamina, weight, getAvailableStamina(), subtraction);
        }
        stamina = Math.max(stamina - subtraction, ZERO);

        if (FeathersCommonConfig.DEBUG_MODE.get()) {
            Feathers.logger.info("Remaining stamina: {}", stamina);
        }
    }

    /* Tick */
    public void tick(Player player) {

        calculateStaminaDelta(player);
        if (shouldCooldown) --cooldown;

        if (cooldown <= 0) {
            cooldown = 0;
            doStaminaChange(player);
        } else if (FeathersCommonConfig.EXTENDED_LOGGING.get()) {
            Feathers.logger.info("{} cooldown: {}", player.level().isClientSide ? "Clientside:" : "Serverside:", cooldown);
        }

    }

    private void calculateStaminaDelta(Player player) {
        final AtomicInteger delta = new AtomicInteger(ZERO);
        staminaDeltaModifierList.forEach(m -> m.applyToDelta(player, this, delta));
        staminaDelta = delta.get();
    }

    private void doStaminaChange(Player player) {

        var preChangeEvent = new StaminaChangeEvent.Pre(player, staminaDelta, stamina);

        if (MinecraftForge.EVENT_BUS.post(preChangeEvent)) return;

        if (preChangeEvent.getResult() == Event.Result.DEFAULT) {

            staminaDelta = preChangeEvent.staminaDelta;
            stamina = preChangeEvent.stamina;
        }

        if (staminaDelta == ZERO) return;


        if ((staminaDelta > ZERO && stamina < maxStamina) || (staminaDelta < ZERO && stamina > ZERO)) applyStaminaDelta();


        if (stamina != prevStamina) postStaminaChange(player);

    }

    private void postStaminaChange(Player player) {
        MinecraftForge.EVENT_BUS.post(new StaminaChangeEvent.Post(player, this));

        if (stamina <= ZERO) {
            MinecraftForge.EVENT_BUS.post(new FeatherAmountEvent.Empty(player, prevStamina));
        } else if (stamina == maxStamina) {
            MinecraftForge.EVENT_BUS.post(new FeatherAmountEvent.Full(player));
        }

        if (prevFeathers != feathers) {
            MinecraftForge.EVENT_BUS.post(new FeatherEvent.Changed(player, this));
            FeathersMessages.sendToPlayer(new FeatherSTCSyncPacket(this), player);

            if (FeathersCommonConfig.DEBUG_MODE.get())
                Feathers.logger.info("{} Feathers: {}", player.level().isClientSide ? "Clientside: " : "ServerSide: ", feathers);

        }

        prevFeathers = feathers;
        prevStamina = stamina;
    }

    /* Save and load */
    public void copyFrom(IFeathers source) {
        this.maxStamina = source.getMaxStamina();
        this.staminaDelta = source.getStaminaDelta();
        this.stamina = source.getStamina();
        this.cooldown = source.getCooldown();
        this.counters.putAll(source.getCounters());
        this.staminaUsageModifiers.putAll(source.getStaminaUsageModifiers());
        this.staminaDeltaModifiers.putAll(source.getStaminaDeltaModifiers());
    }

    public CompoundTag saveNBTData() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("stamina", this.stamina);
        nbt.putInt("max_stamina", this.maxStamina);
        nbt.putInt("stamina_delta", this.staminaDelta);
        nbt.putInt("cooldown", this.cooldown);
        var countersTag = new CompoundTag();
        this.counters.forEach(countersTag::putDouble);
        nbt.put("counters", countersTag);

        return nbt;
    }

    public void loadNBTData(CompoundTag nbt) {

        this.stamina = nbt.getInt("stamina");
        this.maxStamina = nbt.getInt("max_stamina");
        this.staminaDelta = nbt.getInt("stamina_delta");
        this.cooldown = nbt.getInt("cooldown");
        CompoundTag tagCounters = nbt.getCompound("counters");
        tagCounters.getAllKeys().forEach(key -> counters.put(key, tagCounters.getDouble(key)));

        synchronizeFeathers();
    }
}
