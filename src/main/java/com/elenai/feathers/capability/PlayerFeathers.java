package com.elenai.feathers.capability;

import com.elenai.feathers.api.FeathersConstants;
import com.elenai.feathers.api.IFeathers;
import com.elenai.feathers.api.IModifier;
import com.elenai.feathers.attributes.FeathersAttributes;
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
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class PlayerFeathers implements IFeathers {

    private static final int ZERO = 0;
    private int stamina;
    private int maxStamina;
    private int feathers;
    private int cooldown;
    private int enduranceFeathers;
    private int strainFeathers;
    private int maxStrained;
    //Recalculation
    private int staminaDelta;
    @Getter(AccessLevel.NONE)
    private boolean shouldRecalculate;
    //Effects
    private boolean cold;
    private boolean hot;
    private boolean energized;
    private int energizedStrength;
    private boolean fatigued;
    @Getter(AccessLevel.NONE)
    private boolean momentum;
    private boolean strained;


    //Modifiers
    private Map<String, IModifier> staminaDeltaModifiers = new HashMap<>();
    private List<IModifier> staminaDeltaModifierList = new ArrayList<>();

    private Map<String, IModifier> staminaUsageModifiers = new HashMap<>();
    private List<IModifier> staminaUsageModifiersList = new ArrayList<>();

    /**
     * Default constructor.
     */
    public PlayerFeathers(){
        maxStamina = (int) (FeathersCommonConfig.MAX_FEATHERS.get() * FeathersConstants.STAMINA_PER_FEATHER);
        stamina = maxStamina;
        synchronizeFeathers();
        attachDefaultDeltaModifiers();
        attachDefaultUsageModifiers();
        cooldown = ZERO;
        strainFeathers = ZERO;
        enduranceFeathers = ZERO;
        maxStrained = FeathersCommonConfig.MAX_STRAIN.get();

        shouldRecalculate = true;
    }
    public PlayerFeathers(Player player) {

        var attr = player.getAttributes();
        if(attr.hasAttribute(FeathersAttributes.MAX_FEATHERS.get())){
            maxStamina = (int) attr.getValue(FeathersAttributes.MAX_FEATHERS.get());
        } else {
            maxStamina = (int) (FeathersCommonConfig.MAX_FEATHERS.get() * FeathersConstants.STAMINA_PER_FEATHER);
        }
        stamina = maxStamina;
        synchronizeFeathers();
        attachDefaultDeltaModifiers();
        attachDefaultUsageModifiers();
        cooldown = ZERO;
        strainFeathers = ZERO;
        enduranceFeathers = ZERO;
        maxStrained = FeathersCommonConfig.MAX_STRAIN.get();

        shouldRecalculate = true;
    }

    private void attachDefaultDeltaModifiers() {

        var event = new FeatherEvent.AttachDefaultDeltaModifiers(new ArrayList<>());

        if (!MinecraftForge.EVENT_BUS.post(event)) return;

        if (event.getResult() == Event.Result.DEFAULT) {
            event.modifiers.add(StaminaDeltaModifiers.REGENERATION);
            event.modifiers.forEach(m -> staminaDeltaModifiers.put(m.getName(), m));
        }

    }

    private void attachDefaultUsageModifiers() {

        var event = new FeatherEvent.AttachDefaultUsageModifiers(new ArrayList<>());

        if (!MinecraftForge.EVENT_BUS.post(event)) return;

        if (event.getResult() == Event.Result.DEFAULT) {
            event.modifiers.add(StaminaUsageModifiers.DEFAULT_USAGE);
            event.modifiers.forEach(m -> staminaUsageModifiers.put(m.getName(), m));
        }

    }

    public boolean hasMomentum() {
        return momentum;
    }

    @Override
    public int getMaxFeathers() {
        return maxStamina / FeathersConstants.STAMINA_PER_FEATHER;
    }

    public boolean shouldRecalculate() {
        return shouldRecalculate;
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
        AtomicInteger delta = new AtomicInteger(ZERO);
        sortDeltaModifiers();
        for (IModifier modifier : staminaDeltaModifierList) {
            modifier.apply(player, this, delta);
        }
        staminaDelta = delta.get();
        shouldRecalculate = false;
    }

    private int applyDeltaToStrain() {

        if (strainFeathers > 0) {
            int prevDelta = staminaDelta;
            int prevStrain = strainFeathers;
            strainFeathers -= staminaDelta;
            if (strainFeathers <= 0) {
                strained = false;
                staminaDelta = prevDelta - prevStrain;
                return staminaDelta;
            }
        }
        return staminaDelta;
    }

    public void applyStaminaDelta() {
        if (stamina <= ZERO) staminaDelta = applyDeltaToStrain();

        stamina += staminaDelta;
        if (stamina > maxStamina) stamina = maxStamina;
        if (stamina < ZERO) stamina = ZERO;
        synchronizeFeathers();
    }

    @Override
    public void addUsageModifier(IModifier modifier) {
        var prev = staminaUsageModifiers.put(modifier.getName(), modifier);
        if (prev != modifier) {
            sortUsageModifiers();
            shouldRecalculate = true;
        }
    }

    @Override
    public void removeUsageModifier(IModifier modifier) {
        if (staminaUsageModifiers.remove(modifier.getName()) != null) {
            sortUsageModifiers();
            shouldRecalculate = true;
        }
    }

    private void sortUsageModifiers() {
        staminaUsageModifiersList = new ArrayList<>(staminaUsageModifiers.values());
        staminaUsageModifiersList.sort(Comparator.comparingInt(IModifier::getOrdinal));
    }

    private void synchronizeFeathers() {
        feathers = stamina / FeathersConstants.STAMINA_PER_FEATHER;
    }

    @Override
    public int gainFeathers(int feathers) {

        addStamina(feathers * FeathersConstants.STAMINA_PER_FEATHER);
        return feathers;
    }


    @Override
    public int useFeathers(Player player, int feathers) {
        var prev = this.feathers;
        var staminaToRemove = new AtomicInteger(feathers * FeathersConstants.STAMINA_PER_FEATHER);
        for (IModifier modifier : staminaUsageModifiersList) {
            modifier.apply(player, this, staminaToRemove);
        }
        if (staminaToRemove.get() <= 0) return 0;
        subtractStamina(staminaToRemove.get());
        synchronizeFeathers();
        return prev - feathers;
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
        this.enduranceFeathers = source.getEnduranceFeathers();
        this.cold = source.isCold();
        this.hot = source.isHot();
        this.energized = source.isEnergized();
        this.shouldRecalculate = true;
        this.staminaUsageModifiers.putAll(source.getStaminaUsageModifiers());
        this.staminaDeltaModifiers.putAll(source.getStaminaDeltaModifiers());
    }

    public CompoundTag saveNBTData() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("stamina", this.stamina);
        nbt.putInt("max_stamina", this.maxStamina);
        nbt.putInt("stamina_delta", this.staminaDelta);
        nbt.putInt("endurance_feathers", this.enduranceFeathers);
        nbt.putInt("strain_feathers", this.strainFeathers);
        nbt.putBoolean("cold", this.cold);
        nbt.putBoolean("hot", this.hot);
        nbt.putBoolean("energized", this.energized);
        nbt.putBoolean("strained", this.strained);
        return nbt;
    }

    public void loadNBTData(CompoundTag nbt) {

        this.stamina = nbt.getInt("stamina");
        this.maxStamina = nbt.getInt("max_stamina");
        this.staminaDelta = nbt.getInt("stamina_delta");
        this.enduranceFeathers = nbt.getInt("endurance_feathers");
        this.strainFeathers = nbt.getInt("strain_feathers");
        this.cold = nbt.getBoolean("cold");
        this.hot = nbt.getBoolean("hot");
        this.energized = nbt.getBoolean("energized");
        this.strained = nbt.getBoolean("strained");
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

        boolean staminaChanged = false;
        if (canDoStaminaChange()) {
            doStaminaChange(player);
        }

    }

}
