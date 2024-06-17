package com.darkona.feathers.networking;

import com.darkona.feathers.Feathers;
import com.darkona.feathers.config.FeathersCommonConfig;
import com.darkona.feathers.networking.packet.FeatherGainCTSPacket;
import com.darkona.feathers.networking.packet.FeatherSTCDebugPacket;
import com.darkona.feathers.networking.packet.FeatherSTCSyncPacket;
import com.darkona.feathers.networking.packet.FeatherSpendCTSPacket;
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
                                              .networkProtocolVersion(() -> "1.0")
                                              .clientAcceptedVersions(s -> true)
                                              .serverAcceptedVersions(s -> true)
                                              .simpleChannel();
        INSTANCE = network;

        network.messageBuilder(FeatherSpendCTSPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
               .decoder(FeatherSpendCTSPacket::new)
               .encoder(FeatherSpendCTSPacket::toBytes)
               .consumerMainThread(FeatherSpendCTSPacket::handle)
               .add();

        network.messageBuilder(FeatherGainCTSPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
               .decoder(FeatherGainCTSPacket::new)
               .encoder(FeatherGainCTSPacket::toBytes)
               .consumerMainThread(FeatherGainCTSPacket::handle)
               .add();

        network.messageBuilder(FeatherSTCSyncPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
               .decoder(FeatherSTCSyncPacket::new)
               .encoder(FeatherSTCSyncPacket::toBytes)
               .consumerMainThread(FeatherSTCSyncPacket::handle)
               .add();

        network.messageBuilder(FeatherSTCDebugPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
               .decoder(FeatherSTCDebugPacket::new)
               .encoder(FeatherSTCDebugPacket::toBytes)
               .consumerMainThread(FeatherSTCDebugPacket::handle)
               .add();

    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, Player player) {
        if (player instanceof ServerPlayer p && !player.level().isClientSide) {
            if (FeathersCommonConfig.DEBUG_MODE.get()) {
                Feathers.logger.info("Sending {} packet to client", message.getClass().getSimpleName());
                INSTANCE.send(PacketDistributor.PLAYER.with(() -> p), message);
            }
        }

    }
}
