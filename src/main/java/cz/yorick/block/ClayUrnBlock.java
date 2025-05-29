package cz.yorick.block;

import cz.yorick.LastRites;
import cz.yorick.item.ClayUrnItem;
import cz.yorick.effect.GriefStatusEffect;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ClayUrnBlock extends Block {
    public static final BooleanProperty FILLED = BooleanProperty.of("filled");
    public ClayUrnBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FILLED, false));
    }

    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        if(ClayUrnItem.isFilled(ctx.getStack())) {
            return getDefaultState().with(FILLED, true);
        }

        return getDefaultState();
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FILLED);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if(state.get(FILLED)) {
            if(world instanceof ServerWorld serverWorld) {
                Vec3d origin = pos.toCenterPos();
                serverWorld.getPlayers().forEach(serverPlayer -> {
                    if (origin.isInRange(serverPlayer.getPos(), 4)) {
                        serverPlayer.addStatusEffect(new StatusEffectInstance(LastRites.GRIEF_EFFECT, 600));
                    }
                });
            }

            //2002 == non-instant potion break
            world.syncWorldEvent(2002, pos, GriefStatusEffect.COLOR);
        }

        super.onBreak(world, pos, state, player);
    }

    private static final VoxelShape SHAPE = VoxelShapes.union(
            //bottom
            Block.createCuboidShape(4, 0, 4, 12, 10, 12),
            //middle part
            Block.createCuboidShape(5.5, 10, 5.5, 10.5, 11, 10.5),
            //top part
            Block.createCuboidShape(4.5, 11, 4.5, 11.5, 13, 11.5)
    );

    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        return SHAPE;
    }
}
