package cz.yorick.item;

import com.google.common.collect.ImmutableMap;
import cz.yorick.LastRites;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.BundleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.ClickType;

import java.util.Map;

public class EldritchUrnItem extends BundleItem {
    public EldritchUrnItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
        if(isValid(slot.getStack())) {
            return super.onStackClicked(stack, slot, clickType, player);
        }

        return false;
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if(isValid(otherStack)) {
            return super.onClicked(stack, otherStack, slot, clickType, player, cursorStackReference);
        }

        return false;
    }

    private static boolean isValid(ItemStack stack) {
        return stack.isEmpty() || stack.isOf(LastRites.SOUL_ASH.asItem()) || ASH_CHANCES.containsKey(stack.getItem());
    }

    public static float getAshChance(ItemStack stack) {
        return ASH_CHANCES.getOrDefault(stack.getItem(), 0F);
    }

    private static final Map<Item, Float> ASH_CHANCES = ImmutableMap.<Item, Float>builder()
            .put(Items.ROTTEN_FLESH, 0.02F)
            .put(Items.SOUL_SAND, 0.03F)
            .put(Items.SOUL_SOIL, 0.03F)
            .put(Items.BONE, 0.02F)
            .put(Items.BEEF, 0.02F)
            .put(Items.PORKCHOP, 0.02F)
            .put(Items.MUTTON, 0.02F)
            .put(Items.CHICKEN, 0.02F)
            .put(Items.RABBIT, 0.02F)
            .put(Items.COD, 0.02F)
            .put(Items.SALMON, 0.02F)
            .build();
}
