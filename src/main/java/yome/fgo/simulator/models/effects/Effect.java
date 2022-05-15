package yome.fgo.simulator.models.effects;

import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.conditions.Condition;

import java.util.List;

import static yome.fgo.simulator.models.conditions.Always.ALWAYS;

@SuperBuilder
@Getter
public abstract class Effect {
    protected final boolean isOverchargedEffect;
    @Builder.Default
    protected final Condition applyCondition = ALWAYS;

    protected final boolean isProbabilityOvercharged;

    @Builder.Default
    protected final List<Double> probabilities = Lists.newArrayList(1.0);

    protected double getProbability(final int level) {
        if (isProbabilityOvercharged) {
            return probabilities.get(level - 1);
        } else {
            return probabilities.get(0);
        }
    }

    protected boolean shouldApply(final Simulation simulation) {
        return applyCondition.evaluate(simulation);
    }

    public void apply(final Simulation simulation) {
        apply(simulation, 1);
    }

    public void apply(final Simulation simulation, final int level) {
        internalApply(simulation, level);
    }

    // for overcharged
    protected abstract void internalApply(final Simulation simulation, final int level);
}
