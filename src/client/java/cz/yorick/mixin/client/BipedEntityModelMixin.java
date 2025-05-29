package cz.yorick.mixin.client;

import cz.yorick.entity.DamnedOneEntity;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelMixin<T extends LivingEntity> extends AnimalModel<T> {
    @Inject(method = "setAngles", at = @At("HEAD"))
    public void ignoreSittingWhileGrabbed(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo info) {
        if(livingEntity.getVehicle() instanceof DamnedOneEntity) {
            this.riding = false;
        }
    }
}
