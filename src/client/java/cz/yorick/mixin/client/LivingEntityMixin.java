package cz.yorick.mixin.client;

import cz.yorick.LastRites;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//effect application/removal normally only notified on the server
@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "onStatusEffectApplied", at = @At("HEAD"))
    protected void applyGriefShader(StatusEffectInstance effect, @Nullable Entity source, CallbackInfo info) {
        if ((Object) this instanceof ClientPlayerEntity && effect.getEffectType() == LastRites.GRIEF_EFFECT) {
            MinecraftClient.getInstance().gameRenderer.onCameraEntitySet(MinecraftClient.getInstance().getCameraEntity());
        }
    }

    //normal on removed only called on server, this method is used on the client
    @Inject(method = "removeStatusEffectInternal", at = @At("HEAD"))
    protected void removeGriefShader(StatusEffect type, CallbackInfoReturnable<StatusEffectInstance> info) {
        if ((Object) this instanceof ClientPlayerEntity && type == LastRites.GRIEF_EFFECT) {
            MinecraftClient.getInstance().gameRenderer.disablePostProcessor();
        }
    }
}
