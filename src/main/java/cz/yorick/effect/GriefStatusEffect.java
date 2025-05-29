package cz.yorick.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class GriefStatusEffect extends StatusEffect {
    public static final int COLOR = 0x101010;
    public GriefStatusEffect() {
        super(StatusEffectCategory.HARMFUL, COLOR);
    }
}
