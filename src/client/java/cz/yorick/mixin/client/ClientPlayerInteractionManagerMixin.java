package cz.yorick.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import cz.yorick.LastRites;
import cz.yorick.item.CurseBladeItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
    @WrapMethod(method = "getReachDistance")
    public float getReachDistance(Operation<Float> original) {
        ItemStack heldStack = MinecraftClient.getInstance().player.getMainHandStack();
        if(heldStack.isOf(LastRites.CURSEBLADE)) {
            return original.call() + CurseBladeItem.getBonusReach(heldStack);
        }

        return original.call();
    }
}
