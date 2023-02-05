package yome.fgo.simulator.models.effects;

import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;

import static yome.fgo.simulator.models.effects.buffs.BuffType.SKILL_EFFECTIVENESS_UP;

@SuperBuilder
public class CriticalStarChange extends IntValuedEffect {
    @Override
    protected void internalApply(final Simulation simulation, final int level) {
        if (!simulation.getActivator().isAlly() || !shouldApply(simulation)) {
            return;
        }

        final double skillEffectiveness = simulation.getActivator().applyValuedBuff(simulation, SKILL_EFFECTIVENESS_UP);
        final int starValue = (int) ((1 + skillEffectiveness) * getValue(simulation, level));

        simulation.gainStar(starValue);
    }
}
