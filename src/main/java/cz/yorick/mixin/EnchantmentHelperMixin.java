package cz.yorick.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import cz.yorick.LastRites;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
    @WrapMethod(method = "onUserDamaged")
    private static void ignoreDamagedEnchantments(LivingEntity user, Entity attacker, Operation<Void> original) {
        if(!user.hasStatusEffect(LastRites.ANTIMAGIC_EFFECT)) {
            original.call(user, attacker);
        }
    }

    @WrapMethod(method = "onTargetDamaged")
    private static void ignoreDamageEnchantments(LivingEntity user, Entity target, Operation<Void> original) {
        if(!user.hasStatusEffect(LastRites.ANTIMAGIC_EFFECT)) {
            original.call(user, target);
        }
    }

    @WrapMethod(method = "getEquipmentLevel")
    private static int ignoreEquipmentLevel(Enchantment enchantment, LivingEntity entity, Operation<Integer> original) {
        if(!entity.hasStatusEffect(LastRites.ANTIMAGIC_EFFECT)) {
            original.call(enchantment, entity);
        }

        return 0;
    }
}
