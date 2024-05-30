package com.elenai.feathers.capability;

import com.elenai.feathers.api.IModifier;
import com.elenai.feathers.config.FeathersCommonConfig;
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
            List<IModifier> deltaModifiers = new ArrayList<>();
            List<IModifier> usageModifiers = new ArrayList<>();
            deltaModifiers.add(Modifiers.REGENERATION);

            var attachInitialModifiersEvent = new FeatherEvent.InitializeModifiers(deltaModifiers);
            var isCancelled = MinecraftForge.EVENT_BUS.post(attachInitialModifiersEvent);
            if(!isCancelled && attachInitialModifiersEvent.getResult() == Event.Result.DEFAULT){
                this.feathers = new PlayerFeathers(deltaModifiers, usageModifiers);
            }


        }

        return this.feathers;
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
