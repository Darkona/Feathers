package com.elenai.feathers.api;

import com.elenai.feathers.Feathers;
import com.elenai.feathers.client.ClientFeathersData;
import com.elenai.feathers.config.FeathersCommonConfig;
import com.elenai.feathers.enchantment.FeathersEnchantments;
import com.elenai.feathers.networking.FeathersMessages;
import com.elenai.feathers.networking.packet.ClientFeatherSpendPacket;
import com.elenai.feathers.util.ArmorHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Deprecated
public class FeathersHelper {

    @Deprecated
    public static boolean spendFeathers(int amount) {

        Player player = Minecraft.getInstance().player;
        assert player != null;
        if (player.isCreative() || player.isSpectator()) return true;

        if (ClientFeathersData.getInstance().getFeathers() >= amount) {
            FeathersMessages.sendToServer(new ClientFeatherSpendPacket(amount));
            return true;
        }
        return false;
    }


    public static int getArmorWeight(Item item, int lightweightLevel, int heavyLevel) {
        if (item instanceof ArmorItem armor) {
            return Math.max(ArmorHandler.getArmorWeight(armor) - lightweightLevel + (heavyLevel * ArmorHandler.getArmorWeight(armor)), 0);
        } else if (item == Items.AIR) {
            return 0;
        }
        return 0;
    }

    public static int getArmorWeightByStack(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ArmorItem armor) {
            return Math.max(ArmorHandler.getArmorWeight(armor) -
                    ArmorHandler.getItemEnchantmentLevel(FeathersEnchantments.LIGHTWEIGHT.get(), itemStack) +
                    (ArmorHandler.getItemEnchantmentLevel(FeathersEnchantments.HEAVY.get(), itemStack) * ArmorHandler.getArmorWeight(armor)), 0);
        } else if (itemStack.getItem() == Items.AIR) {
            return 0;
        }
        Feathers.logger.warn("Attempted to calculate weight of non armor item: " + itemStack.getDescriptionId());
        return 0;
    }

    public static int getPlayerWeight(ServerPlayer player) {
        if (!FeathersCommonConfig.ENABLE_ARMOR_WEIGHTS.get()) {
            return 0;
        }
        int weight = 0;
        for (ItemStack i : player.getArmorSlots()) {
            weight += getArmorWeightByStack(i);
        }
        return weight;
    }


}
