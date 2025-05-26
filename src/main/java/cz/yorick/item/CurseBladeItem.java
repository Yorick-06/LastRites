package cz.yorick.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import cz.yorick.LastRites;
import cz.yorick.block.SoulAshBlock;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class CurseBladeItem extends SwordItem {
    private static final int MAX_LAYERS = 16;
    private static final UUID STAGE_BONUS_UUID = UUID.fromString("44ee2e6e-ad5e-4af1-9cb8-cad57d20063f");
    public CurseBladeItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if(context.getWorld().getBlockState(context.getBlockPos()).isOf(LastRites.SOUL_ASH) && addLayer(context.getStack())) {
            SoulAshBlock.consumeLayer(context.getWorld(), context.getBlockPos());
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if(getLayers(stack) == MAX_LAYERS) {
            target.addStatusEffect(new StatusEffectInstance(LastRites.GRIEF_EFFECT, 200));
        }

        if(!(attacker instanceof ServerPlayerEntity serverPlayer) || !serverPlayer.isCreative()) {
            removeLayer(stack);
        }

        return super.postHit(stack, target, attacker);
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(ItemStack stack, EquipmentSlot slot) {
        if(slot == EquipmentSlot.MAINHAND) {
            return ImmutableMultimap.<EntityAttribute, EntityAttributeModifier>builder()
                    .putAll(super.getAttributeModifiers(stack, slot))
                    .put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(STAGE_BONUS_UUID, "Stage Bonus", (getStage(stack) - 1), EntityAttributeModifier.Operation.ADDITION))
                    .build();
        }

        return super.getAttributeModifiers(stack, slot);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("Ash: " + getLayers(stack)));
    }

    public static int getStage(ItemStack stack) {
        if(stack.hasNbt()) {
            int layers = getLayers(stack);
            return switch (layers) {
                case 0, 1, 2, 3 -> 1;
                case 4, 5, 6, 7 -> 2;
                case 8, 9, 10, 11, 12, 13, 14, 15 -> 3;
                case 16 -> 4;
                //invalid amount of layers can be added with commands
                default -> layers < 0 ? 1 : 4;
            };
        }

        return 0;
    }

    private static int getLayers(ItemStack stack) {
        if(stack.hasNbt()) {
            return stack.getNbt().getInt("layers");
        }

        return 0;
    }

    private static boolean addLayer(ItemStack stack) {
        int layers = stack.getOrCreateNbt().getInt("layers");
        if(layers < MAX_LAYERS) {
            stack.getOrCreateNbt().putInt("layers", layers + 1);
            return true;
        }

        return false;
    }

    private static void removeLayer(ItemStack stack) {
        if(stack.hasNbt()) {
            int layers = stack.getNbt().getInt("layers");
            if(layers > 0) {
                stack.getNbt().putInt("layers", layers - 1);
            }
        }
    }
}
