package yome.fgo.simulator.models.effects.buffs;

import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;

@SuperBuilder
public class DelayedEffect extends EffectActivatingBuff {
    @Override
    public boolean shouldApply(final Simulation simulation) {
        return numTurnsActive == 1 && super.shouldApply(simulation);
    }
}
