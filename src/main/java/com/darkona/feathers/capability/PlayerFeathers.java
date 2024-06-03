package com.darkona.feathers.capability;

import com.darkona.feathers.Feathers;
import com.darkona.feathers.api.FeathersAPI;
import com.darkona.feathers.api.FeathersConstants;
import com.darkona.feathers.api.IFeathers;
import com.darkona.feathers.api.IModifier;
import com.darkona.feathers.client.ClientFeathersData;
import com.darkona.feathers.config.FeathersCommonConfig;
import com.darkona.feathers.effect.effects.StrainEffect;
import com.darkona.feathers.event.FeatherAmountEvent;
import com.darkona.feathers.event.FeatherEvent;
import com.darkona.feathers.event.StaminaChangeEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class PlayerFeathers implements IFeathers {

    private static final int ZERO = 0;
    private int stamina;
    private int maxStamina;
    private int feathers;
    private int cooldown;
    private int strainFeathers;
    private int maxStrained;
    private boolean desynced;
    private int weight;

    private int staminaDelta;

    @Setter(AccessLevel.NONE)
    private boolean dirty;


    private Map<String, Integer> counters = new HashMap<>();


    private Map<String, IModifier> staminaDeltaModifiers = new HashMap<>();
    private List<IModifier> staminaDeltaModifierList = new ArrayList<>();

    private Map<String, IModifier> staminaUsageModifiers = new HashMap<>();
    private List<IModifier> featherUsageModifiersList = new ArrayList<>();

    /* Initialization */

    public PlayerFeathers() {
        maxStamina = (int) (FeathersCommonConfig.MAX_FEATHERS.get() * FeathersConstants.STAMINA_PER_FEATHER);
        stamina = maxStamina;

        cooldown = ZERO;
        strainFeathers = ZERO;
        maxStrained = FeathersCommonConfig.MAX_STRAIN.get();

        attachDefaultDeltaModifiers();

        attachDefaultUsageModifiers();

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

    public Optional<Integer> getCounter(String name) {
        return Optional.ofNullable(counters.get(name));
    }

    public void setCounter(String name, int value) {
        counters.put(name, value);
    }


    /* Markers */
    @Override
    public int getMaxFeathers() {
        return maxStamina / FeathersConstants.STAMINA_PER_FEATHER;
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
        setStamina(feathers * FeathersConstants.STAMINA_PER_FEATHER);
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
            return Math.max(stamina - (weight * FeathersConstants.STAMINA_PER_FEATHER), ZERO);
        }
        return stamina;
    }

    @Override
    public void setStamina(int stamina) {
        this.stamina = Math.min(Math.max(stamina, ZERO), maxStamina);
        synchronizeFeathers();
    }

    private void synchronizeFeathers() {
        feathers = stamina / FeathersConstants.STAMINA_PER_FEATHER;
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
        addStamina(feathers * FeathersConstants.STAMINA_PER_FEATHER);
        return feathers != prev;
    }

    @Override
    public boolean useFeathers(Player player, int feathers, int cooldown) {

        if (staminaUsageModifiers.isEmpty()) attachDefaultUsageModifiers();

        if (FeathersCommonConfig.DEBUG_MODE.get() && !player.level().isClientSide) {
            Feathers.logger.info("Requested to use {} feathers.", feathers);
        }

        var multiplier = FeathersAPI.getPlayerStaminaUsageMultiplier(player);
        var staminaToUse = new AtomicInteger((int) (feathers * multiplier * FeathersConstants.STAMINA_PER_FEATHER));
        var approve = new AtomicBoolean(false);

        for (IModifier m : featherUsageModifiersList) {
            m.applyToUsage(player, this, staminaToUse, approve);
        }

        if (approve.get()) {
            if (FeathersCommonConfig.DEBUG_MODE.get() && !player.level().isClientSide) {
                Feathers.logger.info("After modifiers will use {} stamina.", staminaToUse);
            }
            subtractStamina(staminaToUse.get());
            this.cooldown += cooldown;
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

        if (staminaDeltaModifiers.isEmpty()) attachDefaultDeltaModifiers();

        if (--cooldown <= 0) {
            cooldown = 0;
            doStaminaChange(player);
        }

        if (player.tickCount % 2 == 0) ClientFeathersData.getInstance().update(player, this);
    }

    private void calculateStaminaDelta(Player player) {
        final AtomicInteger delta = new AtomicInteger(ZERO);
        staminaDeltaModifierList.forEach(m -> m.applyToDelta(player, this, delta));
        staminaDelta = delta.get();
    }

    private void doStaminaChange(Player player) {

        calculateStaminaDelta(player);

        int prevStamina = stamina;
        int prevFeathers = feathers;
        var preChangeEvent = new StaminaChangeEvent.Pre(player, staminaDelta, stamina);
        if (MinecraftForge.EVENT_BUS.post(preChangeEvent)) return;

        if (preChangeEvent.getResult() != Event.Result.DEFAULT) return;

        staminaDelta = preChangeEvent.staminaDelta;
        stamina = preChangeEvent.stamina;


        if (staminaDelta == ZERO) return;


        if ((staminaDelta > ZERO && stamina < maxStamina) || (staminaDelta < ZERO && stamina > ZERO)) applyStaminaDelta();


        if (stamina != prevStamina) postStaminaChange(player, prevStamina, prevFeathers);

    }

    private void postStaminaChange(Player player, int prevStamina, int prevFeather) {


        MinecraftForge.EVENT_BUS.post(new StaminaChangeEvent.Post(player, prevStamina, stamina));
        if (stamina <= ZERO) {
            MinecraftForge.EVENT_BUS.post(new FeatherAmountEvent.Empty(player, prevStamina));
        } else if (stamina == maxStamina) {
            MinecraftForge.EVENT_BUS.post(new FeatherAmountEvent.Full(player));
        }

        if (prevFeather != feathers) {
            if (FeathersCommonConfig.DEBUG_MODE.get() && player.level().isClientSide) {
                Feathers.logger.info("Feathers: {}", feathers);
            }
        }


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
        this.counters.forEach(countersTag::putInt);
        nbt.put("counters", countersTag);

        return nbt;
    }

    public void loadNBTData(CompoundTag nbt) {

        this.stamina = nbt.getInt("stamina");
        this.maxStamina = nbt.getInt("max_stamina");
        this.staminaDelta = nbt.getInt("stamina_delta");
        this.cooldown = nbt.getInt("cooldown");
        CompoundTag tagCounters = nbt.getCompound("counters");
        tagCounters.getAllKeys().forEach(key -> counters.put(key, tagCounters.getInt(key)));

        synchronizeFeathers();
    }
}
