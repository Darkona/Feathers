package com.elenai.feathers.api;

import com.darkona.feathers.api.FeathersAPI;
import com.darkona.feathers.networking.FeathersMessages;
import com.darkona.feathers.networking.packet.FeatherSpendCTSPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FeathersHelper {

    @Deprecated
    public static boolean spendFeathers(int amount) {


        FeathersMessages.sendToServer(new FeatherSpendCTSPacket(amount));

        return FeathersAPI.spendFeathers(Minecraft.getInstance().player, amount, 20);
    }

}
