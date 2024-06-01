package com.elenai.feathers.networking;

import com.elenai.feathers.Feathers;
import com.elenai.feathers.networking.packet.ClientFeatherSpendPacket;
import com.elenai.feathers.networking.packet.FeatherSyncSTCPacket;
import com.elenai.feathers.networking.packet.ReplyWithWeightSTCPacket;
import com.elenai.feathers.networking.packet.RequestWeightCTSPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class FeathersMessages {
    private static SimpleChannel INSTANCE;

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel network =
                NetworkRegistry.ChannelBuilder.named(new ResourceLocation(Feathers.MODID, "messages"))
                                              .networkProtocolVersion(() -> "1.0").clientAcceptedVersions(s -> true)
                                              .serverAcceptedVersions(s -> true)
                                              .simpleChannel();
        INSTANCE = network;

        network.messageBuilder(ClientFeatherSpendPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
               .decoder(ClientFeatherSpendPacket::new)
               .encoder(ClientFeatherSpendPacket::toBytes)
               .consumerMainThread(ClientFeatherSpendPacket::handle)
               .add();

        network.messageBuilder(RequestWeightCTSPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
               .decoder(RequestWeightCTSPacket::new)
               .encoder(RequestWeightCTSPacket::toBytes)
               .consumerMainThread(RequestWeightCTSPacket::handle)
               .add();

        network.messageBuilder(ReplyWithWeightSTCPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
               .decoder(ReplyWithWeightSTCPacket::new)
               .encoder(ReplyWithWeightSTCPacket::toBytes)
               .consumerMainThread(ReplyWithWeightSTCPacket::handle)
               .add();

        network.messageBuilder(FeatherSyncSTCPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
               .decoder(FeatherSyncSTCPacket::new)
               .encoder(FeatherSyncSTCPacket::toBytes)
               .consumerMainThread(FeatherSyncSTCPacket::handle)
               .add();

        /*network.messageBuilder(EffectChangeSTCPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
               .decoder(EffectChangeSTCPacket::new)
               .encoder(EffectChangeSTCPacket::toBytes)
               .consumerMainThread(EffectChangeSTCPacket::handle)
               .add();*/

    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, Player player) {
        if (player instanceof ServerPlayer p)
            INSTANCE.send(PacketDistributor.PLAYER.with(() -> p), message);
    }
}
