package net.dunestriders;

import java.util.Map;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("null")
public class DuneStriders implements ModInitializer {
    public static final String MOD_ID = "dunestriders";
public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

// Equipment Asset Key
public static final ResourceKey<EquipmentAsset> DUNE_STRIDERS_ASSET_KEY = ResourceKey.create(
    EquipmentAssets.ROOT_ID,
    Identifier.fromNamespaceAndPath(MOD_ID, "dune_striders")
);

// Armor Material
public static final ArmorMaterial DUNE_STRIDERS_MATERIAL = new ArmorMaterial(
    15, // enchantability
    Map.of(
        ArmorType.BOOTS, 2,
        ArmorType.LEGGINGS, 4,
        ArmorType.CHESTPLATE, 5,
        ArmorType.HELMET, 2,
        ArmorType.BODY, 3),
    9, // enchantment value
    SoundEvents.ARMOR_EQUIP_LEATHER,
    0.0F, // toughness
    0.0F, // knockback resistance
    ItemTags.REPAIRS_LEATHER_ARMOR, // repair tag (we'll use leather for now, scutes don't have a tag)
    DUNE_STRIDERS_ASSET_KEY // custom equipment asset key
);

    // Item
    public static final ResourceKey<Item> DUNE_STRIDERS_KEY = ResourceKey.create(Registries.ITEM,
            Identifier.fromNamespaceAndPath(MOD_ID, "dune_striders"));
    public static final Item DUNE_STRIDERS = new Item(
            new Item.Properties().humanoidArmor(DUNE_STRIDERS_MATERIAL, ArmorType.BOOTS).setId(DUNE_STRIDERS_KEY));

    // Enchantment
    public static final ResourceKey<Enchantment> DUNE_SPEED_KEY = ResourceKey.create(Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath(MOD_ID, "dune_speed"));

    @Override
    public void onInitialize() {
        // Register the item
        Registry.register(BuiltInRegistries.ITEM, DUNE_STRIDERS_KEY, DUNE_STRIDERS);

        // Add to Combat Creative Tab after Netherite Boots
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(content -> {
            content.addAfter(Items.NETHERITE_BOOTS, DUNE_STRIDERS);
        });

        // Tick event for speed effect on sand
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getPlayerList().getPlayers().forEach(player -> {
                var feetItem = player.getItemBySlot(EquipmentSlot.FEET);
                if (feetItem.is(DUNE_STRIDERS)) {
                    if (player.getBlockStateOn().is(BlockTags.SAND)) {
                        int level = EnchantmentHelper.getItemEnchantmentLevel(
                                server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                                        .getOrThrow(DUNE_SPEED_KEY),
                                feetItem);
                        // Level 0 = Speed I (amp 0)
                        // Level 1 = Speed II (amp 1)
                        // Level 2 = Speed III (amp 2)
                        // Level 3 = Speed IV (amp 3)
                        player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, level, false, false, true));
                    }
                }
            });
        });

        LOGGER.info("Dune Striders initialized!");
    }
}
