package cz.yorick.block;

import cz.yorick.LastRites;
import net.minecraft.block.*;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SoulAshBlock extends FallingBlock {
    public static final IntProperty LAYERS = IntProperty.of("layers", 1, 8);
    private static final VoxelShape[] LAYERS_TO_SHAPE = new VoxelShape[] {
            VoxelShapes.empty(),
            Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
            Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
            Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 6.0, 16.0),
            Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 8.0, 16.0),
            Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 10.0, 16.0),
            Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 12.0, 16.0),
            Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 14.0, 16.0),
            Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 16.0),
    };

    public SoulAshBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(LAYERS, 1));
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        if (Objects.requireNonNull(type) == NavigationType.LAND) {
            return state.get(LAYERS) < 5;
        }
        return false;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return LAYERS_TO_SHAPE[state.get(LAYERS)];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return LAYERS_TO_SHAPE[state.get(LAYERS) - 1];
    }

    @Override
    public VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
        return LAYERS_TO_SHAPE[state.get(LAYERS)];
    }

    @Override
    public VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return LAYERS_TO_SHAPE[state.get(LAYERS)];
    }

    @Override
    public boolean hasSidedTransparency(BlockState state) {
        return true;
    }

    @Override
    public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        return state.get(LAYERS) == 8 ? 0.2F : 1.0F;
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos.down());
        if (blockState.isIn(BlockTags.SNOW_LAYER_CANNOT_SURVIVE_ON)) {
            return false;
        } else if (blockState.isIn(BlockTags.SNOW_LAYER_CAN_SURVIVE_ON)) {
            return true;
        } else {
            return Block.isFaceFullSquare(blockState.getCollisionShape(world, pos.down()), Direction.UP) || blockState.isOf(this) && blockState.get(LAYERS) == 8;
        }
    }

    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext context) {
        if (context.getStack().isOf(this.asItem()) && state.get(LAYERS) < 8) {
            if (context.canReplaceExisting()) {
                return context.getSide() == Direction.UP;
            } else {
                return true;
            }
        }

        return false;
    }

    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState blockState = ctx.getWorld().getBlockState(ctx.getBlockPos());
        if (blockState.isOf(this)) {
            return blockState.with(LAYERS, Math.min(8, blockState.get(LAYERS) + 1));
        } else {
            return super.getPlacementState(ctx);
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LAYERS);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if(player.getStackInHand(hand).isOf(Items.FLINT_AND_STEEL) && state.get(LAYERS) > 3) {
            if(player instanceof ServerPlayerEntity serverPlayer && world instanceof ServerWorld serverWorld) {
                world.playSound(player, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.4F + 0.8F);
                player.getStackInHand(hand).damage(1, serverPlayer, p -> p.sendToolBreakStatus(hand));
                animate(serverWorld, pos, state, player);
            }

            return ActionResult.SUCCESS;
        }

        return super.onUse(state, world, pos, player, hand, hit);
    }

    public static void animate(ServerWorld world, BlockPos pos, BlockState state, PlayerEntity owner) {
        int layers = state.get(LAYERS);
        while (layers > 1) {
            LastRites.DAMNED_ONE_ENTITY_TYPE.spawn(world, null, spawned -> {
                spawned.setOwner(owner);
            }, pos, SpawnReason.TRIGGERED, false, false);
            layers -= 4;
        }

        if(layers <= 0) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
        } else {
            world.setBlockState(pos, state.with(LAYERS, layers));
        }
    }

    public static void consumeLayer(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        int layers = state.get(SoulAshBlock.LAYERS);
        if(layers > 1) {
            world.setBlockState(pos, state.with(SoulAshBlock.LAYERS, layers - 1));
        } else {
            world.removeBlock(pos, false);
        }

        Vec3d vec3d = pos.toCenterPos();
        world.playSound(vec3d.getX(), vec3d.getY(), vec3d.getZ(), state.getBlock().getSoundGroup(state).getBreakSound(), SoundCategory.BLOCKS, 1.0F + world.getRandom().nextFloat(), world.getRandom().nextFloat() * 0.7F + 0.3F, false);
    }
}
