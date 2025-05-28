package cz.yorick.block.entity;

import cz.yorick.LastRites;
import cz.yorick.block.BrazierBlock;
import cz.yorick.item.EldritchUrnItem;
import cz.yorick.mixin.BundleAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

public class BrazierBlockEntity extends BlockEntity {
    private ItemStack urn = ItemStack.EMPTY;
    private int progress = 0;
    private final int maxProgress = 20 * 60;
    public BrazierBlockEntity(BlockPos pos, BlockState state) {
        super(LastRites.BRAZIER_BLOCK_ENTITY, pos, state);
    }

    public void placeUrn(ItemStack urn) {
        this.urn = urn;
    }

    public ItemStack takeUrn() {
        ItemStack urn = this.urn;
        this.urn = ItemStack.EMPTY;
        return urn;
    }

    public static void tick(World world, BlockPos pos, BlockState state, BrazierBlockEntity blockEntity) {
        if(world instanceof ServerWorld && !blockEntity.urn.isEmpty() && state.get(BrazierBlock.FIRE) != BrazierBlock.FireType.NONE && state.get(BrazierBlock.URN) == BrazierBlock.UrnType.URN) {
            blockEntity.progress++;
            if(blockEntity.progress >= blockEntity.maxProgress) {
                int ashAmount = 0;
                Optional<ItemStack> maybeRemoved = BundleAccessor.removeFirstStack(blockEntity.urn);
                while(maybeRemoved.isPresent()) {
                    ItemStack removed = maybeRemoved.get();
                    //add all present ash
                    if(removed.isOf(LastRites.SOUL_ASH.asItem())) {
                       ashAmount += removed.getCount();
                    }

                    float chance = EldritchUrnItem.getAshChance(removed);
                    //roll for each item
                    for (int i = 0; i < removed.getCount(); i++) {
                        if(world.getRandom().nextFloat() < chance) {
                            ashAmount++;
                        }
                    }

                    //remove next item
                    maybeRemoved = BundleAccessor.removeFirstStack(blockEntity.urn);
                }

                BundleAccessor.addToBundle(blockEntity.urn, new ItemStack(LastRites.SOUL_ASH, ashAmount));
                world.setBlockState(pos, state.with(BrazierBlock.URN, BrazierBlock.UrnType.FINISHED));
                blockEntity.progress = 0;
            }
        } else {
            blockEntity.progress = 0;
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        if(!this.urn.isEmpty()) {
            NbtCompound urnNbt = new NbtCompound();
            this.urn.writeNbt(urnNbt);
            nbt.put("urn", urnNbt);
        }

        nbt.putInt("progress", this.progress);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        NbtCompound urnNbt = nbt.getCompound("urn");
        if(!urnNbt.isEmpty()) {
            this.urn = ItemStack.fromNbt(urnNbt);
        }

        this.progress = nbt.getInt("progress");
    }
}
