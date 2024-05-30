package com.elenai.feathers.capability;

import com.elenai.feathers.api.IModifier;
import com.elenai.feathers.compatibility.thirst.ThirstManager;
import com.elenai.feathers.config.FeathersCommonConfig;
import com.elenai.feathers.effect.StrainEffect;
import com.elenai.feathers.event.FeatherEvent;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PlayerFeathersProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<PlayerFeathers> PLAYER_FEATHERS = CapabilityManager.get(new CapabilityToken<>() {
    });

    private PlayerFeathers feathers = null;
    private final LazyOptional<PlayerFeathers> optional = LazyOptional.of(this::createPlayerFeathers);

    private PlayerFeathers createPlayerFeathers() {

        if (this.feathers == null) {
            this.feathers = new PlayerFeathers(attachDeltaModifiers(), attachUsageModifiers());
        }

        return this.feathers;
    }

    private List<IModifier> attachDeltaModifiers(){
        List<IModifier> deltaModifiers = new ArrayList<>();

        var attachInitialModifiersEvent = new FeatherEvent.AttachDeltaModifiers(deltaModifiers);
        if(MinecraftForge.EVENT_BUS.post(attachInitialModifiersEvent)){
            return deltaModifiers;
        } else if (attachInitialModifiersEvent.getResult() == Event.Result.DEFAULT) {
            attachInitialModifiersEvent.modifiers.add(PlayerFeathers.REGENERATION);
            if(FeathersCommonConfig.ENABLE_STRAIN.get()){
                attachInitialModifiersEvent.modifiers.add(StrainEffect.STRAIN_RECOVERY);
            }
        }

        return attachInitialModifiersEvent.modifiers;
    }

    private List<IModifier> attachUsageModifiers(){
        List<IModifier> usageModifiers = new ArrayList<>();

        var attachInitialModifiersEvent = new FeatherEvent.AttachUsageModifiers(usageModifiers);
        if(MinecraftForge.EVENT_BUS.post(attachInitialModifiersEvent)) {
            return usageModifiers;
        }else if(attachInitialModifiersEvent.getResult() == Event.Result.DEFAULT){
            if(FeathersCommonConfig.ENABLE_STRAIN.get()) {
                attachInitialModifiersEvent.modifiers.add(StrainEffect.STRAIN_USAGE);
            }
        }

        return attachInitialModifiersEvent.modifiers;
    }


    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == PLAYER_FEATHERS) {
            return optional.cast();
        }

        return LazyOptional.empty();
    }

    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createPlayerFeathers().saveNBTData(nbt);
        return nbt;
    }

    public void deserializeNBT(CompoundTag nbt) {
        createPlayerFeathers().loadNBTData(nbt);
    }
}
