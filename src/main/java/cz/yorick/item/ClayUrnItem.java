package cz.yorick.item;

import cz.yorick.LastRites;
import cz.yorick.block.SoulAshBlock;
import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.*;
import net.minecraft.potion.PotionUtil;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ClayUrnItem extends BlockItem {
    public ClayUrnItem(Block block, Item.Settings settings) {
        super(block, settings);
    }

    //just recycle the thrown potion since it renders the texture of the item
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if(isFilled(itemStack)) {
            if (!world.isClient) {
                PotionEntity potionEntity = new PotionEntity(world, user);
                PotionUtil.setPotion(itemStack, LastRites.GRIEF_POTION);
                potionEntity.setItem(itemStack);
                potionEntity.setVelocity(user, user.getPitch(), user.getYaw(), -20.0F, 0.5F, 1.0F);
                world.spawnEntity(potionEntity);
            }

            user.incrementStat(Stats.USED.getOrCreateStat(this));
            if (!user.getAbilities().creativeMode) {
                itemStack.decrement(1);
            }

            return TypedActionResult.success(itemStack, world.isClient());
        }

        return TypedActionResult.pass(itemStack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if(!isFilled(stack)) {
            tooltip.add(Text.literal("Empty"));
        }
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if(context.getWorld().getBlockState(context.getBlockPos()).isOf(LastRites.SOUL_ASH) && !isFilled(context.getStack())) {
            SoulAshBlock.consumeLayer(context.getWorld(), context.getBlockPos());
            fill(context.getStack(), context.getPlayer());
            context.getPlayer().incrementStat(Stats.USED.getOrCreateStat(this));
            return ActionResult.SUCCESS;
        }

        return super.useOnBlock(context);
    }

    public void fill(ItemStack stack, PlayerEntity player) {
        ItemStack newStack = new ItemStack(this);
        newStack.getOrCreateNbt().putBoolean("filled", true);
        ItemUsage.exchangeStack(stack, player, newStack);
    }

    public static boolean isFilled(ItemStack stack) {
        return stack.hasNbt() && stack.getNbt().getBoolean("filled");
    }
}
