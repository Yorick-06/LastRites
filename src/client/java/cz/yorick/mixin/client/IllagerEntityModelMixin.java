package cz.yorick.mixin.client;

import cz.yorick.entity.DamnedOneEntity;
import net.minecraft.client.render.entity.model.IllagerEntityModel;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.entity.mob.IllagerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//requires separate mixin since the method parameter (T) changes
@Mixin(IllagerEntityModel.class)
public abstract class IllagerEntityModelMixin<T extends IllagerEntity> extends SinglePartEntityModel<T> {
    @Inject(method = "setAngles", at = @At("HEAD"))
    public void ignoreSittingWhileGrabbed(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo info) {
        if(livingEntity.getVehicle() instanceof DamnedOneEntity) {
            this.riding = false;
        }
    }
}
