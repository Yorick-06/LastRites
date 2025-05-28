package cz.yorick.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FallingBlockEntity.class)
public interface FallingBlockAccessor {
    @Invoker("<init>")
    static FallingBlockEntity init(World world, double x, double y, double z, BlockState block) {
        throw new UnsupportedOperationException("Implemented via mixin!");
    }
}
