package cz.yorick.item;

import cz.yorick.LastRites;
import cz.yorick.block.SoulAshBlock;
import cz.yorick.entity.DamnedOneEntity;
import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ClayUrnItem extends BlockItem {
    public static String EMPTY_TRANSLATION_KEY = "tooltip." + LastRites.MOD_ID + ".empty";
    public static String SOUL_ASH_TRANSLATION_KEY = "tooltip." + LastRites.MOD_ID + ".soul_ash";
    public static String DAMNED_ONE_TRANSLATION_KEY = "tooltip." + LastRites.MOD_ID + ".damned_one";
    public ClayUrnItem(Block block, Item.Settings settings) {
        super(block, settings);
    }

    //just recycle the thrown potion since it renders the texture of the item
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        Type urnContent = getFillType(itemStack);
        if(urnContent == Type.ASH) {
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

        if(urnContent == Type.ENTITY) {
            if (world instanceof ServerWorld serverWorld) {
                LastRites.DAMNED_ONE_ENTITY_TYPE.spawn(serverWorld, null, spawned -> {
                    spawned.readCustomDataFromNbt(itemStack.getNbt().getCompound("filled"));
                    spawned.setOwner(user);
                }, user.getBlockPos(), SpawnReason.TRIGGERED, false, false);

                serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(), Registries.SOUND_EVENT.getEntry(SoundEvents.ENTITY_SPLASH_POTION_BREAK), SoundCategory.PLAYERS, 1.0F, user.getRandom().nextFloat() * 0.1F + 0.9F, user.getRandom().nextLong());
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
        String translationKey = switch (getFillType(stack)) {
            case EMPTY -> EMPTY_TRANSLATION_KEY;
            case ASH -> SOUL_ASH_TRANSLATION_KEY;
            case ENTITY -> DAMNED_ONE_TRANSLATION_KEY;
        };

        tooltip.add(Text.translatable(translationKey).formatted(Formatting.GRAY));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if(context.getWorld().getBlockState(context.getBlockPos()).isOf(LastRites.SOUL_ASH) && !isFilled(context.getStack())) {
            SoulAshBlock.consumeLayer(context.getWorld(), context.getBlockPos());

            ItemStack output = context.getStack().copy();
            output.setCount(1);
            output.getOrCreateNbt().putBoolean("filled", true);
            context.getStack().decrement(1);
            context.getPlayer().getInventory().offerOrDrop(output);

            context.getPlayer().incrementStat(Stats.USED.getOrCreateStat(this));
            return ActionResult.SUCCESS;
        }

        //only place when empty or ash + sneaking
        Type fillType = getFillType(context.getStack());
        if(fillType == Type.EMPTY || (context.getPlayer().isSneaking() && fillType == Type.ASH)) {
            return super.useOnBlock(context);
        }

        return ActionResult.PASS;
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if(user.getWorld() instanceof ServerWorld serverWorld && !isFilled(stack) && entity instanceof DamnedOneEntity damnedOneEntity && user == damnedOneEntity.getOwner()) {
            NbtCompound entityNbt = new NbtCompound();
            damnedOneEntity.writeCustomDataToNbt(entityNbt);
            serverWorld.playSound(null, entity.getX(), entity.getY(), entity.getZ(), Registries.SOUND_EVENT.getEntry(SoundEvents.ENTITY_VEX_DEATH), SoundCategory.PLAYERS, 1.0F, user.getRandom().nextFloat() * 0.1F + 0.9F, user.getRandom().nextLong());
            damnedOneEntity.remove(Entity.RemovalReason.DISCARDED);

            ItemStack output = stack.copy();
            output.setCount(1);
            output.getOrCreateNbt().put("filled", entityNbt);
            stack.decrement(1);
            user.getInventory().offerOrDrop(output);

            user.incrementStat(Stats.USED.getOrCreateStat(this));
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    public static boolean isFilled(ItemStack stack) {
        return getFillType(stack) != Type.EMPTY;
    }

    public static Type getFillType(ItemStack stack) {
        if(stack.hasNbt()) {
            NbtCompound nbt = stack.getNbt();
            if(nbt.contains("filled", NbtElement.BYTE_TYPE) && nbt.getBoolean("filled")) {
                return Type.ASH;
            }

            if(nbt.contains("filled", NbtElement.COMPOUND_TYPE)) {
                return Type.ENTITY;
            }
        }
        return Type.EMPTY;
    }

    private enum Type {
        EMPTY,
        ASH,
        ENTITY;
    }
}
