package cz.yorick.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import cz.yorick.LastRites;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin<T extends LivingEntity> {
	@WrapMethod(method = "shouldRender")
	private <E extends Entity> boolean applyGriefRestriction(E entity, Frustum frustum, double x, double y, double z, Operation<Boolean> original) {
		if(original.call(entity, frustum, x, y, z)) {
			ClientPlayerEntity player = MinecraftClient.getInstance().player;
			return !player.hasStatusEffect(LastRites.GRIEF_EFFECT) || player.distanceTo(entity) < 20;
		}

		return false;
	}
}