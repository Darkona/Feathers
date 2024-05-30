package com.elenai.feathers.networking.packet;

import com.elenai.feathers.capability.PlayerFeathers;
import com.elenai.feathers.client.ClientFeathersData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


public class FeatherSyncSTCPacket {

    private final int stamina;
    private final int maxStamina;
    private final int enduranceStamina;
    private final int staminaDelta;

    private final int weight;
    private final boolean cold;
    private final boolean hot;
    private final boolean energized;
    private final boolean fatigued;
    private final boolean momentum;



    public FeatherSyncSTCPacket(PlayerFeathers feathers) {
        stamina = feathers.getStamina();
        maxStamina = feathers.getMaxStamina();
        enduranceStamina = feathers.getEnduranceFeathers();
        weight = ClientFeathersData.weight;
        energized = ClientFeathersData.energized;
        fatigued = ClientFeathersData.fatigued;
        staminaDelta = feathers.getStaminaDelta();
        cold = feathers.isCold();
        hot = feathers.isHot();
        momentum = feathers.hasMomentum();
    }

    public FeatherSyncSTCPacket(FriendlyByteBuf buf) {
        stamina = buf.readInt();
        maxStamina = buf.readInt();
        enduranceStamina = buf.readInt();
        staminaDelta = buf.readInt();
        weight = buf.readInt();
        cold = buf.readBoolean();
        hot = buf.readBoolean();
        energized = buf.readBoolean();
        fatigued = buf.readBoolean();
        momentum = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(stamina);
        buf.writeInt(maxStamina);
        buf.writeInt(enduranceStamina);
        buf.writeInt(staminaDelta);
        buf.writeInt(weight);
        buf.writeBoolean(cold);
        buf.writeBoolean(hot);
        buf.writeBoolean(energized);
        buf.writeBoolean(fatigued);
        buf.writeBoolean(momentum);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientFeathersData.stamina = stamina;
            ClientFeathersData.maxStamina = maxStamina;
            ClientFeathersData.enduranceFeathers = enduranceStamina;
            ClientFeathersData.staminaDelta = staminaDelta;
            ClientFeathersData.energized = energized;
            ClientFeathersData.fatigued = fatigued;
            ClientFeathersData.hot = hot;
            ClientFeathersData.cold = cold;
            ClientFeathersData.momentum = momentum;
            ClientFeathersData.weight = weight;
        });
        return true;
    }
}
