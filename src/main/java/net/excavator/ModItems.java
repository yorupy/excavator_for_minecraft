package net.excavator;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ExcavatorMod.MODID);

    // Оригинальные инструменты (Алмазный тир, Золотой цвет)
    public static final RegistryObject<Item> SUPER_PICKAXE = ITEMS.register("super_pickaxe",
            () -> new SuperPickaxeItem(Tiers.DIAMOND, 1, -2.8F, new Item.Properties().rarity(Rarity.RARE)));

    public static final RegistryObject<Item> SUPER_HOE = ITEMS.register("super_hoe",
            () -> new HoeItem(Tiers.DIAMOND, -3, 0.0F, new Item.Properties().rarity(Rarity.RARE)));

    // Серия Pickme (Железный тир, Эпический РОЗОВЫЙ цвет)
    public static final RegistryObject<Item> PICKME_PICKAXE = ITEMS.register("pickme_pickaxe",
            () -> new SuperPickaxeItem(Tiers.IRON, 1, -2.8F, new Item.Properties().rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> PICKME_HOE = ITEMS.register("pickme_hoe",
            () -> new HoeItem(Tiers.IRON, -2, 0.0F, new Item.Properties().rarity(Rarity.EPIC)));
}