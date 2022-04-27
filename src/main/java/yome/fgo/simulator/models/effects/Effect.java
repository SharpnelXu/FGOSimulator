package yome.fgo.simulator.models.effects;

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
    protected final List<Double> probabilities;

    protected boolean shouldApply(final Simulation simulation) {
        return applyCondition.evaluate(simulation);
    }

    public void apply(final Simulation simulation) {
        apply(simulation, 1);
    }

    public void apply(final Simulation simulation, final int level) {
        internalApply(simulation, level);

        simulation.checkBuffStatus();
    }

    // for overcharged
    protected abstract void internalApply(final Simulation simulation, final int level);
}
