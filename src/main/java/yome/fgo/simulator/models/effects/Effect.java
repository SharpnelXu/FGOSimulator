package yome.fgo.simulator.models.effects;

import lombok.Builder;
import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.conditions.Condition;

import static yome.fgo.simulator.models.conditions.Always.ALWAYS;

@SuperBuilder
public abstract class Effect {

    private final boolean isOverchargedEffect;
    @Builder.Default
    private final Condition applyCondition = ALWAYS;

    public boolean shouldApply(final Simulation simulation) {
        return applyCondition.evaluate(simulation);
    }

    public void apply(final Simulation simulation) {
        apply(simulation, 1);
    }

    // for overcharged
    public abstract void apply(final Simulation simulation, final int level);
}
