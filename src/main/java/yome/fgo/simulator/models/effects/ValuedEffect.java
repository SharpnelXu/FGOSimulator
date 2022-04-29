package yome.fgo.simulator.models.effects;

import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.variations.Variation;

import java.util.List;

import static yome.fgo.simulator.models.variations.NoVariation.NO_VARIATION;

@SuperBuilder
public abstract class ValuedEffect extends Effect {
    protected boolean isValueOvercharged;
    @Builder.Default
    protected final List<Double> values = Lists.newArrayList(0.0);

    @Builder.Default
    protected final Variation variation = NO_VARIATION;
    protected boolean isAdditionsOvercharged;
    @Builder.Default
    protected final List<Double> additions = Lists.newArrayList(0.0);
}
