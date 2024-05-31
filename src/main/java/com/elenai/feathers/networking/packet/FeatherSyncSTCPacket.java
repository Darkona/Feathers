package com.elenai.feathers.networking.packet;

import com.elenai.feathers.api.FeathersConstants;
import com.elenai.feathers.capability.PlayerFeathers;
import com.elenai.feathers.client.ClientFeathersData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


public class FeatherSyncSTCPacket {

    private final int stamina;
    private final int maxStamina;

    private final int feathers;

    private final int maxFeathers;
    private final int staminaDelta;


    public FeatherSyncSTCPacket(PlayerFeathers f) {
        stamina = f.getStamina();
        maxStamina = f.getMaxStamina();
        feathers = f.getFeathers();
        maxFeathers = f.getMaxFeathers();
        staminaDelta = f.getStaminaDelta();
    }

    public FeatherSyncSTCPacket(FriendlyByteBuf buf) {
        stamina = buf.readInt();
        maxStamina = buf.readInt();
        feathers = buf.readInt();
        maxFeathers = buf.readInt();
        staminaDelta = buf.readInt();

    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(stamina);
        buf.writeInt(maxStamina);
        buf.writeInt(feathers);
        buf.writeInt(maxFeathers);
        buf.writeInt(staminaDelta);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientFeathersData.feathers = this.feathers;
            ClientFeathersData.maxFeathers = this.maxFeathers;
            ClientFeathersData.stamina = this.stamina;
            ClientFeathersData.maxStamina = this.maxStamina;
            ClientFeathersData.staminaDelta = this.staminaDelta;
        });
        return true;
    }
}
