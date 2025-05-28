package cz.yorick.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import cz.yorick.LastRites;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @WrapOperation(method = "modifyAppliedDamage", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/enchantment/EnchantmentHelper;getProtectionAmount(Ljava/lang/Iterable;Lnet/minecraft/entity/damage/DamageSource;)I"
    ))
    private int ignoreProtectionEnchantments(Iterable<ItemStack> equipment, DamageSource damageSource, Operation<Integer> original) {
        if(!((LivingEntity)(Object)this).hasStatusEffect(LastRites.ANTIMAGIC_EFFECT)) {
            return original.call(equipment, damageSource);
        }

        return 0;
    }
}
