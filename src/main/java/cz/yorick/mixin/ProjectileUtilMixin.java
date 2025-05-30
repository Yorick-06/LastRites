package cz.yorick.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import cz.yorick.entity.DamnedOneEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Predicate;

@Mixin(ProjectileUtil.class)
public class ProjectileUtilMixin {
    //targets the second call of getRootVehicle, ignoring the vehicle if it is a damned one to allow the player to hit it while being grabbed
    @ModifyExpressionValue(method = "raycast", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;getRootVehicle()Lnet/minecraft/entity/Entity;",
            ordinal = 1
    ))
    private static Entity ignoreDamnedOneAsVehicle(Entity originalVehicle, Entity raycaster, Vec3d min, Vec3d max, Box box, Predicate<Entity> predicate, double d) {
        if(originalVehicle instanceof DamnedOneEntity) {
            return raycaster;
        }

        return originalVehicle;
    }
}
