package cz.yorick.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import cz.yorick.LastRites;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity> {
	@WrapMethod(method = "isVisible")
	private boolean applyGriefRestriction(T entity, Operation<Boolean> original) {
		if(original.call(entity)) {
			ClientPlayerEntity player = MinecraftClient.getInstance().player;
			return !player.hasStatusEffect(LastRites.GRIEF_EFFECT) || player.distanceTo(entity) < 20;
		}

		return false;
	}
}