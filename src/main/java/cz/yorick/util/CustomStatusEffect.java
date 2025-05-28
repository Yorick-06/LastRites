package cz.yorick.util;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class CustomStatusEffect extends StatusEffect {
    public static final int COLOR = 0x101010;
    public CustomStatusEffect() {
        super(StatusEffectCategory.HARMFUL, COLOR);
    }
}
