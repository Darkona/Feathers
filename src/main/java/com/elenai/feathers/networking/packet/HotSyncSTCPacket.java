package com.elenai.feathers.networking.packet;

import com.elenai.feathers.client.ClientFeathersData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class HotSyncSTCPacket {

    private final boolean hot;

    public HotSyncSTCPacket(boolean hot) {
        this.hot = hot;
    }

    public HotSyncSTCPacket(FriendlyByteBuf buf) {
        this.hot = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(hot);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientFeathersData.hot = (hot);
        });
        return true;
    }

}
