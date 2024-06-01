package com.elenai.feathers.capability;

import com.elenai.feathers.api.FeathersAPI;
import com.elenai.feathers.api.FeathersConstants;
import com.elenai.feathers.api.IFeathers;
import com.elenai.feathers.api.IModifier;
import com.elenai.feathers.client.ClientFeathersData;
import com.elenai.feathers.config.FeathersCommonConfig;
import com.elenai.feathers.effect.FeathersEffects;
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
import net.minecraft.world.effect.MobEffectInstance;
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

    //Stamina delta calculation
    private int staminaDelta;
    @Getter(AccessLevel.NONE)
    private boolean shouldRecalculate;

    //Checks
    private Map<String, Integer> counters = new HashMap<>();

    //Modifiers
    private Map<String, IModifier> staminaDeltaModifiers = new HashMap<>();
    private List<IModifier> staminaDeltaModifierList = new ArrayList<>();

    private Map<String, IModifier> staminaUsageModifiers = new HashMap<>();
    private List<IModifier> staminaUsageModifiersList = new ArrayList<>();


    public PlayerFeathers() {
        maxStamina = (int) (FeathersCommonConfig.MAX_FEATHERS.get() * FeathersConstants.STAMINA_PER_FEATHER);
        stamina = maxStamina;

        cooldown = ZERO;
        strainFeathers = ZERO;
        maxStrained = FeathersCommonConfig.MAX_STRAIN.get();

        attachDefaultDeltaModifiers();

        attachDefaultUsageModifiers();

        synchronizeFeathers();

        shouldRecalculate = true;
    }

    public void addCounter(String name, int value) {
        counters.put(name, value);
    }

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

    private void attachDefaultDeltaModifiers() {

        var modifiers = new ArrayList<IModifier>();
        var event = new FeatherEvent.AttachDefaultDeltaModifiers(modifiers);
        modifiers.add(IModifier.REGENERATION);
        if (MinecraftForge.EVENT_BUS.post(event)) return;

        if (event.getResult() == Event.Result.DEFAULT) {
            event.modifiers.forEach(this::addDeltaModifier);
        }

    }

    private void attachDefaultUsageModifiers() {

        var event = new FeatherEvent.AttachDefaultUsageModifiers(new ArrayList<>());

        if (!MinecraftForge.EVENT_BUS.post(event)) return;

        if (event.getResult() == Event.Result.DEFAULT) {
            event.modifiers.add(IModifier.DEFAULT_USAGE);
            event.modifiers.forEach(m -> staminaUsageModifiers.put(m.getName(), m));
        }

    }

    @Override
    public int getMaxFeathers() {
        return maxStamina / FeathersConstants.STAMINA_PER_FEATHER;
    }

    public boolean shouldRecalculate() {
        return shouldRecalculate;
    }

    public void setShouldRecalculate() {
        shouldRecalculate = true;
    }

    @Override
    public int getFeathers() {
        return stamina / FeathersConstants.STAMINA_PER_FEATHER;
    }

    @Override
    public void setFeathers(int feathers) {
        this.stamina = feathers * FeathersConstants.STAMINA_PER_FEATHER;
    }

    @Override
    public void setStamina(int stamina) {
        this.stamina = Math.min(Math.max(stamina, ZERO), maxStamina);
        synchronizeFeathers();
    }

    @Override
    public void addDeltaModifier(IModifier modifier) {

        var prev = staminaDeltaModifiers.put(modifier.getName(), modifier);
        if (prev != modifier) {
            shouldRecalculate = true;
        }
    }

    @Override
    public void removeDeltaModifier(IModifier modifier) {
        if (staminaDeltaModifiers.remove(modifier.getName()) != null) {
            shouldRecalculate = true;
        }
    }

    private void sortDeltaModifiers() {
        staminaDeltaModifierList = new ArrayList<>(staminaDeltaModifiers.values());
        staminaDeltaModifierList.sort(Comparator.comparingInt(IModifier::getOrdinal));
    }


    @Override
    public void recalculateStaminaDelta(Player player) {
        if (!shouldRecalculate) return;

        final AtomicInteger delta = new AtomicInteger(ZERO);

        sortDeltaModifiers();

        staminaDeltaModifierList.forEach(m -> m.apply(player, this, delta));

        staminaDelta = delta.get();

        shouldRecalculate = false;
    }


    public void applyStaminaDelta() {

        stamina += staminaDelta;

        if (stamina > maxStamina) stamina = maxStamina;

        if (stamina < ZERO) stamina = ZERO;

        synchronizeFeathers();
    }

    @Override
    public void addUsageModifier(IModifier modifier) {
        staminaUsageModifiers.put(modifier.getName(), modifier);
        sortUsageModifiers();

    }

    @Override
    public void removeUsageModifier(IModifier modifier) {
        staminaUsageModifiers.remove(modifier.getName());
        sortUsageModifiers();

    }

    public void sortUsageModifiers() {
        staminaUsageModifiersList = new ArrayList<>(staminaUsageModifiers.values());
        staminaUsageModifiersList.sort(Comparator.comparingInt(IModifier::getOrdinal));
    }

    private void synchronizeFeathers() {
        feathers = stamina / FeathersConstants.STAMINA_PER_FEATHER;

    }

    @Override
    public boolean gainFeathers(int feathers) {
        var prev = this.feathers;
        addStamina(feathers * FeathersConstants.STAMINA_PER_FEATHER);
        return feathers != prev;
    }

    @Override
    public boolean useFeathers(Player player, int feathers) {
        var prev = this.feathers;
        var multiplier = FeathersAPI.getPlayerStaminaUsageMultiplier(player);
        var staminaToRemove = new AtomicInteger((int) (feathers * FeathersConstants.STAMINA_PER_FEATHER * multiplier));
        var approve = new AtomicBoolean(false);
        staminaUsageModifiersList.forEach(m -> m.apply(player, this, staminaToRemove, approve));

        if (staminaToRemove.get() <= 0) return true;
        subtractStamina(staminaToRemove.get());
        synchronizeFeathers();
        return approve.get();
    }

    private void addStamina(int stamina) {
        this.stamina = Math.min(this.stamina + stamina, maxStamina);
    }

    private void subtractStamina(int stamina) {
        this.stamina = Math.max(this.stamina - stamina, ZERO);
    }

    public void copyFrom(IFeathers source) {
        this.maxStamina = source.getMaxStamina();
        this.staminaDelta = source.getStaminaDelta();
        this.stamina = source.getStamina();
        this.shouldRecalculate = true;
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

    private boolean canDoStaminaChange() {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        return true;
    }

    private void doStaminaChange(Player player) {

        recalculateStaminaDelta(player);

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

        if (stamina < 0) {

            if (FeathersCommonConfig.DEBUG_MODE.get()) {
                player.sendSystemMessage(MutableComponent.create(new LiteralContents("You are out of stamina!")));
            }

            if (FeathersCommonConfig.ENABLE_STRAIN.get()) {
                player.addEffect(new MobEffectInstance(FeathersEffects.STRAINED.get(), -1, 0, false, true));
            }

        }

    }

    public void tick(Player player) {

        if (shouldRecalculate)
            recalculateStaminaDelta(player);

        if (canDoStaminaChange()) {
            doStaminaChange(player);
        }

        if (player.tickCount % 2 == 0 && player.level().isClientSide) ClientFeathersData.getInstance().update(this);
    }

}
