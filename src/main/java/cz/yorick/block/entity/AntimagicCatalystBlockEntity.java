package cz.yorick.block.entity;

import cz.yorick.LastRites;
import cz.yorick.block.AntimagicCatalystBlock;
import cz.yorick.block.BrazierBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class AntimagicCatalystBlockEntity extends BlockEntity {
    private final int maxCooldown = 60 * 60 * 20;
    private int cooldown = 0;
    private final int maxProgress = 10 * 20;
    private int progress = 0;
    public AntimagicCatalystBlockEntity(BlockPos pos, BlockState state) {
        super(LastRites.ANTIMAGIC_CATALYST_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, AntimagicCatalystBlockEntity blockEntity) {
        if(world instanceof ServerWorld serverWorld) {
            if (blockEntity.cooldown > 0) {
                blockEntity.cooldown--;
                return;
            }

            if(!state.get(AntimagicCatalystBlock.ACTIVE)) {
                return;
            }

            if (blockEntity.progress < blockEntity.maxProgress) {
                blockEntity.progress++;
                return;
            }

            List<BlockPos> fires = locateFires(world, pos);
            if (fires.isEmpty()) {
                world.setBlockState(pos, state.with(AntimagicCatalystBlock.ACTIVE, false));
                blockEntity.cooldown = blockEntity.maxCooldown;
                return;
            }

            blockEntity.progress = 0;
            BlockPos extinguished = fires.get((int) ((fires.size() - 1) * world.random.nextDouble()));
            extinguish(world, extinguished);
            /*
            Vec3d origin = pos.toCenterPos();
            serverWorld.getPlayers().forEach(serverPlayer -> {
                if (origin.isInRange(serverPlayer.getPos(), 100)) {
                    serverPlayer.addStatusEffect(new StatusEffectInstance(LastRites.ANTIMAGIC_EFFECT, 12 * 20));
                }
            });*/

            serverWorld.playSound(null, pos, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.BLOCKS, 1.0F + world.getRandom().nextFloat(), world.getRandom().nextFloat() * 0.7F + 0.3F);
        }
    }

    public static List<BlockPos> locateFires(World world, BlockPos selfPos) {
        ArrayList<BlockPos> firePositions = new ArrayList<>();
        for (int yOffset = -2; yOffset < 3; yOffset++) {
            for (int xOffset = -2; xOffset < 3; xOffset++) {
                for(int zOffset = -2; zOffset < 3; zOffset++) {
                    BlockPos pos = selfPos.add(xOffset, yOffset, zOffset);
                    if(isValidFire(world.getBlockState(pos))) {
                        firePositions.add(pos);
                    }
                }
            }
        }

        return firePositions;
    }

    private static boolean isValidFire(BlockState state) {
        return state.isOf(Blocks.SOUL_FIRE) || (state.isOf(Blocks.SOUL_CAMPFIRE) && state.get(CampfireBlock.LIT)) || (state.isOf(LastRites.BRAZIER) && state.get(BrazierBlock.FIRE) == BrazierBlock.FireType.SOUL_FIRE);
    }

    private static void extinguish(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if(state.isOf(Blocks.SOUL_FIRE)) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
        }

        if(state.isOf(Blocks.SOUL_CAMPFIRE)) {
            world.setBlockState(pos, state.with(CampfireBlock.LIT, false));
        }

        if(state.isOf(LastRites.BRAZIER)) {
            world.setBlockState(pos, state.with(BrazierBlock.FIRE, BrazierBlock.FireType.NONE));
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        nbt.putInt("cooldown", this.cooldown);
        nbt.putInt("progress", this.progress);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        this.cooldown = nbt.getInt("cooldown");
        this.progress = nbt.getInt("progress");
    }

    public boolean canActivate() {
        return this.cooldown <= 0;
    }

    public void onPlaced(ItemStack stack) {
        if(stack.hasNbt()) {
            this.cooldown = stack.getNbt().getInt("cooldown");
        }
    }

    public ItemStack asStack() {
        ItemStack stack = new ItemStack(LastRites.ANTIMAGIC_CATALYST);
        stack.getOrCreateNbt().putInt("cooldown", this.cooldown);
        return stack;
    }
}
