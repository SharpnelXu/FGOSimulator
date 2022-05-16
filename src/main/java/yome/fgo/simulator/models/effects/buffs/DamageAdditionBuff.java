package yome.fgo.simulator.models.effects.buffs;

import lombok.experimental.SuperBuilder;

import static yome.fgo.simulator.models.variations.NoVariation.NO_VARIATION;

@SuperBuilder
public class DamageAdditionBuff extends ValuedBuff implements AttackerBuff {
    @Override
    public String toString() {
        String base = ": " + (int) value;
        if (variation != NO_VARIATION) {
            base = base + " + " + (int) addition + " " + variation;
        }
        return baseToString() + base;
    }
}
