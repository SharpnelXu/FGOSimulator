package yome.fgo.simulator.models.effects;

import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.variations.Variation;

import java.text.NumberFormat;
import java.util.List;
import java.util.stream.Collectors;

import static yome.fgo.simulator.models.variations.NoVariation.NO_VARIATION;

@SuperBuilder
public abstract class ValuedEffect extends Effect {
    protected final boolean isValueOvercharged;
    @Builder.Default
    protected final List<Double> values = Lists.newArrayList(0.0);

    @Builder.Default
    protected final Variation variation = NO_VARIATION;
    protected final boolean isAdditionsOvercharged;
    @Builder.Default
    protected final List<Double> additions = Lists.newArrayList(0.0);

    public double getValue(final Simulation simulation, final int level) {
        final double baseValue = isValueOvercharged ? values.get(level - 1) : values.get(0);
        final double additionValue = isAdditionsOvercharged ? additions.get(level - 1) : additions.get(0);
        return variation.evaluate(simulation, baseValue, additionValue);
    }

    @Override
    public String toString() {
        String base;
        final NumberFormat numberFormat = NumberFormat.getPercentInstance();
        numberFormat.setMaximumFractionDigits(2);
        if (isValueOvercharged) {
            base = ": (OC) " + values.stream().map(numberFormat::format).collect(Collectors.toList());
        } else {
            base = ": " + numberFormat.format(values.get(0));
        }
        if (variation != NO_VARIATION) {
            base = base + " + ";
            if (isAdditionsOvercharged) {
                base = base + "(OC) " + additions.stream().map(numberFormat::format).collect(Collectors.toList());
            } else {
                base = base + numberFormat.format(additions.get(0));
            }
            base = base + " " + variation;
        }
        return super.toString() + base;
    }
}
