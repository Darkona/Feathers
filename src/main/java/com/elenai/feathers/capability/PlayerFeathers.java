package com.elenai.feathers.capability;

import com.elenai.feathers.api.FeathersAPI;
import com.elenai.feathers.api.FeathersConstants;
import com.elenai.feathers.api.IFeathers;
import com.elenai.feathers.api.IModifier;
import com.elenai.feathers.client.ClientFeathersData;
import com.elenai.feathers.config.FeathersCommonConfig;
import com.elenai.feathers.effect.effects.StrainEffect;
import com.elenai.feathers.event.FeatherAmountEvent;
import com.elenai.feathers.event.FeatherEvent;
import com.elenai.feathers.event.StaminaChangeEvent;
import com.elenai.feathers.networking.FeathersMessages;
import com.elenai.feathers.networking.packet.FeatherSyncSTCPacket;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
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


    private int staminaDelta;

    @Setter(AccessLevel.NONE)
    private boolean dirty;


    private Map<String, Integer> counters = new HashMap<>();


    private Map<String, IModifier> staminaDeltaModifiers = new HashMap<>();
    private List<IModifier> staminaDeltaModifierList = new ArrayList<>();

    private Map<String, IModifier> staminaUsageModifiers = new HashMap<>();
    private List<IModifier> staminaUsageModifiersList = new ArrayList<>();

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
        dirty = true;
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
        staminaUsageModifiersList = new ArrayList<>(staminaUsageModifiers.values());
        staminaUsageModifiersList.sort(Comparator.comparingInt(IModifier::getUsageOrdinal));
    }


    /*Stamina delta calculations*/
    @Override
    public void recalculateStaminaDelta(Player player) {

    }

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
    public boolean useFeathers(Player player, int feathers) {
        if (staminaUsageModifiers.isEmpty()) attachDefaultUsageModifiers();
        var prev = this.feathers;
        var multiplier = FeathersAPI.getPlayerStaminaUsageMultiplier(player);
        var staminaToUse = new AtomicInteger((int) (feathers * FeathersConstants.STAMINA_PER_FEATHER * multiplier));
        var approve = new AtomicBoolean(false);

        for (IModifier m : staminaUsageModifiersList) {
            m.applyToUsage(player, this, staminaToUse, approve);
        }

        if (approve.get()) {
            subtractStamina(staminaToUse.get());
            cooldown += 40;
            synchronizeFeathers();
        }
        ;
        return approve.get();
    }

    private void addStamina(int stamina) {
        this.stamina = Math.min(this.stamina + stamina, maxStamina);
    }

    private void subtractStamina(int subtraction) {
        stamina = Math.max(stamina - subtraction, ZERO);
    }

    /* Tick */
    public void tick(Player player) {


        if (canDoStaminaChange()) {
            doStaminaChange(player);
        }

        if (player.tickCount % 2 == 0 && player.level().isClientSide) ClientFeathersData.getInstance().update(player, this);
    }

    private boolean canDoStaminaChange() {
        if (staminaDeltaModifiers.isEmpty()) attachDefaultDeltaModifiers();
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        return true;
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


        if ((staminaDelta > ZERO && stamina < maxStamina) || (staminaDelta < ZERO && stamina > ZERO)) {
            applyStaminaDelta();
        }

        if (stamina != prevStamina) postStaminaChange(player, prevStamina, prevFeathers);
    }

    private void postStaminaChange(Player player, int prevStamina, int prevFeather) {


        MinecraftForge.EVENT_BUS.post(new StaminaChangeEvent.Post(player, prevStamina, stamina));
        if (stamina <= ZERO) {

            MinecraftForge.EVENT_BUS.post(new FeatherAmountEvent.Empty(player, prevStamina));
            FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(this), player);


        } else if (stamina == maxStamina) {

            MinecraftForge.EVENT_BUS.post(new FeatherAmountEvent.Full(player));
            FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(this), player);

        }

        if (prevFeather != feathers) {

            if (FeathersCommonConfig.DEBUG_MODE.get()) {
                player.sendSystemMessage(MutableComponent.create(new LiteralContents("Previous feathers : " + prevFeather + ", now: " + feathers)));
            }

            FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(this), player);
        }

    }


    /* Save and load */
    public void copyFrom(com.elenai.feathers.api.IFeathers source) {
        this.maxStamina = source.getMaxStamina();
        this.staminaDelta = source.getStaminaDelta();
        this.stamina = source.getStamina();
        this.dirty = true;
        this.counters.putAll(source.getCounters());
        this.staminaUsageModifiers.putAll(source.getStaminaUsageModifiers());
        this.staminaDeltaModifiers.putAll(source.getStaminaDeltaModifiers());
    }

    public CompoundTag saveNBTData() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("stamina", this.stamina);
        nbt.putInt("max_stamina", this.maxStamina);
        nbt.putInt("stamina_delta", this.staminaDelta);
        var countersTag = new CompoundTag();
        this.counters.forEach(countersTag::putInt);
        nbt.put("counters", countersTag);

        return nbt;
    }

    public void loadNBTData(CompoundTag nbt) {

        this.stamina = nbt.getInt("stamina");
        this.maxStamina = nbt.getInt("max_stamina");
        this.staminaDelta = nbt.getInt("stamina_delta");
        CompoundTag tagCounters = nbt.getCompound("counters");
        tagCounters.getAllKeys().forEach(key -> counters.put(key, tagCounters.getInt(key)));

        synchronizeFeathers();
    }
}
