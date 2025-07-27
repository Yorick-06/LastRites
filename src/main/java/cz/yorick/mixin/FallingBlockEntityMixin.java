package cz.yorick.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import cz.yorick.LastRites;
import cz.yorick.block.SoulAshBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FallingBlockEntity.class)
public class FallingBlockEntityMixin {
    @Shadow
    private BlockState block;

    //we must trick the block into thinking it can replace the other block if both are soul ash,
    //replacement gets modified by the method below
    @WrapOperation(
            method = "tick",
            at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/block/BlockState;canReplace(Lnet/minecraft/item/ItemPlacementContext;)Z"
            )
    )
    private boolean canReplaceSoulAshBlock(BlockState originalState, ItemPlacementContext context, Operation<Boolean> original) {
        if(isSpecialCase(originalState, this.block)) {
            return true;
        }

        return original.call(originalState, context);
    }

    //normally falling blocks use setState to replace the block they fall onto
    //we need to modify the block instead of replacing it and possibly place the overflow above
    @WrapOperation(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"
            )
    )
    private boolean placeSoulAshBlock(World world, BlockPos toPlacePos, BlockState stateToPlace, int flags, Operation<Boolean> original) {
        BlockState currentState = world.getBlockState(toPlacePos);
        if(!isSpecialCase(currentState, stateToPlace)) {
            return original.call(world, toPlacePos, stateToPlace, flags);
        }

        int totalLayers = currentState.get(SoulAshBlock.LAYERS) + stateToPlace.get(SoulAshBlock.LAYERS);
        if(totalLayers <= SoulAshBlock.MAX_LAYERS) {
            //add layers to the current block
            return world.setBlockState(toPlacePos, currentState.with(SoulAshBlock.LAYERS, totalLayers), flags);
        }

        //add max layers to the current block and add a block above with the remaining layers
        return
            world.setBlockState(toPlacePos, currentState.with(SoulAshBlock.LAYERS, SoulAshBlock.MAX_LAYERS), flags)
            &&
            world.setBlockState(toPlacePos.up(), currentState.with(SoulAshBlock.LAYERS, totalLayers - SoulAshBlock.MAX_LAYERS), flags);
    }

    //checks if we are placing a soul ash block on a soul ash block
    @Unique
    private static boolean isSpecialCase(BlockState current, BlockState toPlace) {
        return current.isOf(LastRites.SOUL_ASH) && toPlace.isOf(LastRites.SOUL_ASH);
    }
}
