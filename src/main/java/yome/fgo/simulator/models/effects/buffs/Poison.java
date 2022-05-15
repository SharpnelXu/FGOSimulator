package yome.fgo.simulator.models.effects.buffs;

import lombok.experimental.SuperBuilder;

import static yome.fgo.simulator.models.variations.NoVariation.NO_VARIATION;

@SuperBuilder
public class Poison extends ValuedBuff implements DamageOverTime {
    @Override
    public boolean commonBuffCondition() {
        return false;
    }

    @Override
    public boolean commonDebuffCondition() {
        return true;
    }

    public static Class<? extends ValuedBuff> getEffectivenessClass() {
        return PoisonEffectivenessUp.class;
    }

    @Override
    public String toString() {
        String base = ": " + (int) value;
        if (variation != NO_VARIATION) {
            base = base + " + " + (int) addition + " " + variation;
        }
        return super.toString() + base;
    }
}
