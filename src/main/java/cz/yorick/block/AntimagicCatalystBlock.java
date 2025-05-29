package cz.yorick.block;

import cz.yorick.LastRites;
import cz.yorick.block.entity.AntimagicCatalystBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class AntimagicCatalystBlock extends BlockWithEntity {
    public static final BooleanProperty ACTIVE = BooleanProperty.of("active");
    public AntimagicCatalystBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(ACTIVE, false));
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AntimagicCatalystBlockEntity(pos, state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, LastRites.ANTIMAGIC_CATALYST_BLOCK_ENTITY, AntimagicCatalystBlockEntity::tick);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack handStack = player.getStackInHand(hand);
        if(world instanceof ServerWorld serverWorld && handStack.isOf(LastRites.SOUL_ASH.asItem()) && ((AntimagicCatalystBlockEntity)world.getBlockEntity(pos)).canActivate() && AntimagicCatalystBlockEntity.locateFires(world, pos).size() >= 12) {
            world.setBlockState(pos, state.with(ACTIVE, true));
            serverWorld.playSound(null, pos, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.BLOCKS, 1.0F + world.getRandom().nextFloat(), world.getRandom().nextFloat() * 0.7F + 0.3F);
            if(!player.isCreative()) {
                handStack.decrement(1);
            }
            return ActionResult.SUCCESS;
        }

        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if(world instanceof ServerWorld serverWorld && serverWorld.getBlockEntity(pos) instanceof AntimagicCatalystBlockEntity antimagicCatalystBlockEntity) {
            antimagicCatalystBlockEntity.onPlaced(itemStack);
        }
    }

    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        if(world.getBlockEntity(pos) instanceof AntimagicCatalystBlockEntity antimagicCatalystBlockEntity) {
            Vec3d origin = pos.toCenterPos();
            ItemScatterer.spawn(world, origin.getX(), origin.getY(), origin.getZ(), antimagicCatalystBlockEntity.asStack());
        }
        super.afterBreak(world, player, pos, state, blockEntity, tool);
    }

    private static final VoxelShape SHAPE = Block.createCuboidShape(1, 0, 1, 15, 24, 15);

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        return SHAPE;
    }
}
