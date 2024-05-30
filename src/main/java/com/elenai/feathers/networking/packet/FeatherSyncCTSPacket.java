package com.elenai.feathers.networking.packet;

import com.elenai.feathers.capability.PlayerFeathersProvider;
import com.elenai.feathers.networking.FeathersMessages;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FeatherSyncCTSPacket {

    private final int feathers;
    private final int endurance;

    public FeatherSyncCTSPacket(int feathers, int endurance) {
        this.feathers = feathers;
        this.endurance = endurance;
    }

    public FeatherSyncCTSPacket(FriendlyByteBuf buf) {
        this.feathers = buf.readInt();
        this.endurance = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(feathers);
        buf.writeInt(endurance);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            context.getSender().getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).ifPresent(f -> {
                f.setFeathers(feathers);
                f.setEnduranceFeathers(endurance);
                FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), context.getSender());
            });

        });
        return true;
    }
}
