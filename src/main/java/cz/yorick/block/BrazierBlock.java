package cz.yorick.block;

import cz.yorick.LastRites;
import cz.yorick.block.entity.BrazierBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BrazierBlock extends BlockWithEntity {
    public static final EnumProperty<UrnType> URN = EnumProperty.of("urn", UrnType.class);
    public static final EnumProperty<FireType> FIRE = EnumProperty.of("fire", FireType.class);
    public BrazierBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(URN, UrnType.NONE).with(FIRE, FireType.NONE));
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BrazierBlockEntity(pos, state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(URN, FIRE);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (state.get(FIRE) != FireType.NONE && entity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity)entity)) {
            entity.damage(world.getDamageSources().inFire(), 2);
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack handStack = player.getStackInHand(hand);
        //light the brazier
        if(handStack.isOf(Items.FLINT_AND_STEEL) && state.get(FIRE) != FireType.FIRE) {
            world.playSound(player, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.4F + 0.8F);
            if (player instanceof ServerPlayerEntity serverPlayer) {
                player.getStackInHand(hand).damage(1, serverPlayer, p -> p.sendToolBreakStatus(hand));
            }

            world.setBlockState(pos, state.with(FIRE, FireType.FIRE));
            return ActionResult.SUCCESS;
        }

        //right-click with soul sand/soil to turn it into a soul fire
        if(handStack.isOf(Items.SOUL_SAND) || handStack.isOf(Items.SOUL_SOIL) && state.get(FIRE) == FireType.FIRE) {
            world.playSound(player, pos, SoundEvents.BLOCK_SOUL_SAND_PLACE, SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.4F + 0.8F);
            world.setBlockState(pos, state.with(FIRE, FireType.SOUL_FIRE));
            return ActionResult.SUCCESS;
        }

        //extinguish the brazier
        if(state.get(FIRE) != FireType.NONE && handStack.isOf(Items.WATER_BUCKET)) {
            world.playSound(player, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.4F + 0.8F);
            world.setBlockState(pos, state.with(FIRE, FireType.NONE));
            player.setStackInHand(hand, BucketItem.getEmptiedStack(handStack, player));
            return ActionResult.SUCCESS;
        }

        //place the urn
        if(handStack.isOf(LastRites.ELDRITCH_URN) && !hasUrn(state)) {
            world.playSound(player, pos, SoundEvents.BLOCK_DECORATED_POT_PLACE, SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.4F + 0.8F);
            world.setBlockState(pos, state.with(URN, UrnType.URN));
            player.setStackInHand(hand, ItemStack.EMPTY);
            ((BrazierBlockEntity)world.getBlockEntity(pos)).placeUrn(handStack);
            return ActionResult.SUCCESS;
        }

        //take the urn
        if(handStack.isEmpty() && hasUrn(state)) {
            world.playSound(player, pos, SoundEvents.BLOCK_DECORATED_POT_BREAK, SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.4F + 0.8F);
            world.setBlockState(pos, state.with(URN, UrnType.NONE));
            player.setStackInHand(hand, ((BrazierBlockEntity)world.getBlockEntity(pos)).takeUrn());
            return ActionResult.SUCCESS;
        }

        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, LastRites.BRAZIER_BLOCK_ENTITY, BrazierBlockEntity::tick);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        ItemStack urn = ((BrazierBlockEntity)world.getBlockEntity(pos)).takeUrn();
        if(!urn.isEmpty()) {
            Vec3d vec3d = pos.toCenterPos();
            ItemScatterer.spawn(world, vec3d.getX(), vec3d.getY(), vec3d.getZ(), urn);
        }

        super.onBreak(world, pos, state, player);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if(state.get(FIRE) != FireType.NONE) {
            Vec3d vec3d = pos.toCenterPos();
            if (random.nextInt(24) == 0) {
                world.playSound(vec3d.getX(), vec3d.getY(), vec3d.getZ(), SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, 1.0F + random.nextFloat(), random.nextFloat() * 0.7F + 0.3F, false);
            }

            world.addParticle(ParticleTypes.LARGE_SMOKE, vec3d.getX(), vec3d.getY(), vec3d.getZ(), 0.0, 0.0, 0.0);
        }
    }

    private static final VoxelShape EMPTY_BRAZIER = VoxelShapes.union(
            //bottom
            Block.createCuboidShape(2.5, 0, 2.5, 13.5, 2, 13.5),
            //wider part
            Block.createCuboidShape(1.5, 2, 1.5, 14.5, 4, 14.5),
            //coal part
            Block.createCuboidShape(2, 4, 2, 14, 6, 14)
    );

    private static final VoxelShape BRAZIER_WITH_URN = VoxelShapes.union(
            EMPTY_BRAZIER,
            //large part
            Block.createCuboidShape(4.5, 6, 4.5, 11.5, 10, 11.5),
            //middle part
            Block.createCuboidShape(6.5, 10, 6.5, 9.5, 10, 9.5),
            //top part
            Block.createCuboidShape(5.5, 14, 5.5, 10.5, 16, 10.5)
    );

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return hasUrn(state) ? BRAZIER_WITH_URN : EMPTY_BRAZIER;
    }

    @Override
    public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        return hasUrn(state) ? BRAZIER_WITH_URN : EMPTY_BRAZIER;
    }

    public static boolean hasUrn(BlockState state) {
        return state.get(URN) != UrnType.NONE;
    }

    public enum FireType implements StringIdentifiable {
        NONE,
        FIRE,
        SOUL_FIRE;

        @Override
        public String asString() {
            return name().toLowerCase();
        }
    }

    public enum UrnType implements StringIdentifiable {
        NONE,
        URN,
        FINISHED;

        @Override
        public String asString() {
            return name().toLowerCase();
        }
    }
}
