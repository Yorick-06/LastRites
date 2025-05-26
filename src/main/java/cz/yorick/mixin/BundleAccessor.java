package cz.yorick.mixin;

import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

@Mixin(BundleItem.class)
public interface BundleAccessor {
	@Invoker("addToBundle")
	static int addToBundle(ItemStack bundle, ItemStack stack) {
		throw new UnsupportedOperationException("Implemented via mixin!");
	}

	@Invoker("removeFirstStack")
	static Optional<ItemStack> removeFirstStack(ItemStack stack) {
		throw new UnsupportedOperationException("Implemented via mixin!");
	}
}