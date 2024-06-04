package com.darkona.feathers.networking.packet;

import com.darkona.feathers.api.FeathersAPI;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FeatherGainCTSPacket {

    private final int feathersToGain;

    public FeatherGainCTSPacket(int feathersToGain) {
        this.feathersToGain = feathersToGain;
    }

    public FeatherGainCTSPacket(FriendlyByteBuf buf) {
        this.feathersToGain = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(feathersToGain);
    }

    public static void handle(FeatherGainCTSPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isServer()) {
            context.enqueueWork(() -> {
                Player player = context.getSender();
                if (player != null) {
                    Level level = player.level();
                    FeathersAPI.gainFeathers(player, message.feathersToGain);
                }
            });
        }
        context.setPacketHandled(true);
    }
}
