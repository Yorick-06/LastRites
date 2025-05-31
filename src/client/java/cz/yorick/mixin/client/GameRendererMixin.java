package cz.yorick.mixin.client;

import cz.yorick.LastRites;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    PostEffectProcessor postProcessor;

    @Inject(method = "onCameraEntitySet", at = @At("TAIL"))
    public void reEnableGriefPostProcessor(Entity entity, CallbackInfo info) {
        //load the post processor any time the camera entity or the player has the effect
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if((player != null && player.hasStatusEffect(LastRites.GRIEF_EFFECT)) || (entity instanceof LivingEntity livingEntity && livingEntity.hasStatusEffect(LastRites.GRIEF_EFFECT))) {
            loadPostProcessor(new Identifier("shaders/post/desaturate.json"));
        }
    }

    @Shadow
    abstract void loadPostProcessor(Identifier id);
}
