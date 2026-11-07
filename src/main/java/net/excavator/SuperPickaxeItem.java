package net.excavator;

import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.tags.BlockTags;

public class SuperPickaxeItem extends DiggerItem {
    public SuperPickaxeItem(Tier tier, int attackDamageIn, float attackSpeedIn, Properties builder) {
        super((float) attackDamageIn, attackSpeedIn, tier, BlockTags.MINEABLE_WITH_PICKAXE, builder);
    }

    // Этот метод указывает игре, какой предмет может чинить нашу кирку на наковальне
    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repairMaterial) {
        // Разрешаем починку только незеритовым слитком (Netherite Ingot)
        return repairMaterial.is(Items.NETHERITE_INGOT) || super.isValidRepairItem(toRepair, repairMaterial);
    }
}