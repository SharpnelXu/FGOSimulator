package yome.fgo.simulator.models.effects;

import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.variations.Variation;

import java.util.List;

import static yome.fgo.simulator.models.variations.NoVariation.NO_VARIATION;

@SuperBuilder
public abstract class IntValuedEffect extends Effect {
    protected final boolean isValueOvercharged;
    @Builder.Default
    protected final List<Integer> values = Lists.newArrayList(0);

    @Builder.Default
    protected final Variation variation = NO_VARIATION;
    protected final boolean isAdditionsOvercharged;
    @Builder.Default
    protected final List<Integer> additions = Lists.newArrayList(0);

    public int getValue(final Simulation simulation, final int level) {
        final double baseValue = isValueOvercharged ? values.get(level - 1) : values.get(0);
        final double additionValue = isAdditionsOvercharged ? additions.get(level - 1) : additions.get(0);
        return (int) variation.evaluate(simulation, baseValue, additionValue);
    }

    @Override
    public String toString() {
        String base;
        if (isValueOvercharged) {
            base = ": (OC) " + values;
        } else {
            base = ": " + values.get(0);
        }
        if (variation != NO_VARIATION) {
            base = base + " + ";
            if (isAdditionsOvercharged) {
                base = base + "(OC) " + additions;
            } else {
                base = base + additions.get(0);
            }
            base = base + " " + variation;
        }
        return super.toString() + base;
    }
}
