package yome.fgo.simulator.models.effects;

import lombok.experimental.SuperBuilder;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.utils.TargetUtils;

import static yome.fgo.simulator.translation.TranslationManager.TARGET_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

@SuperBuilder
public class NpChange extends ValuedEffect {
    private final Target target;

    @Override
    protected void internalApply(final Simulation simulation, final int level) {
        final double probability = getProbability(level);
        if (probability < simulation.getProbabilityThreshold()) {
            return;
        }

        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            simulation.setEffectTarget(combatant);
            if (shouldApply(simulation)) {
                combatant.changeNp(getValue(simulation, level));
            }
            simulation.unsetEffectTarget();
        }
    }

    @Override
    public String toString() {
        return getTranslation(TARGET_SECTION, target.name()) + super.toString();
    }
}
