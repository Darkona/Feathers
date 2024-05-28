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
    private final boolean cold;
    private final boolean hot;

    public FeatherSyncSTCPacket(PlayerFeathers feathers) {
        stamina = feathers.getStamina();
        maxStamina = feathers.getMaxStamina();
        enduranceStamina = feathers.getEnduranceStamina();
        staminaDelta = feathers.getStaminaDelta();
        cold = feathers.isCold();
        hot = feathers.isHot();
    }

    public FeatherSyncSTCPacket(FriendlyByteBuf buf) {
        stamina = buf.readInt();
        maxStamina = buf.readInt();
        enduranceStamina = buf.readInt();
        staminaDelta = buf.readInt();
        cold = buf.readBoolean();
        hot = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(stamina);
        buf.writeInt(maxStamina);
        buf.writeInt(enduranceStamina);
        buf.writeInt(staminaDelta);

        buf.writeBoolean(cold);
        buf.writeBoolean(hot);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientFeathersData.stamina = stamina;
            ClientFeathersData.maxStamina = maxStamina;
            ClientFeathersData.staminaDelta = staminaDelta;
            ClientFeathersData.enduranceFeathers = enduranceStamina;
            ClientFeathersData.hot = hot;
            ClientFeathersData.cold = cold;
        });
        return true;
    }
}
