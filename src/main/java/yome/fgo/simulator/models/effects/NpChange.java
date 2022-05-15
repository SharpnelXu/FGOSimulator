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

        final double baseValue = isValueOvercharged ? values.get(level - 1) : values.get(0);
        final double additionValue = isAdditionsOvercharged ? additions.get(level - 1) : additions.get(0);
        final double value = variation.evaluate(simulation, baseValue, additionValue);

        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            simulation.setEffectTarget(combatant);
            if (shouldApply(simulation)) {
                combatant.changeNp(value);
            }
            simulation.unsetEffectTarget();
        }
    }

    @Override
    public String toString() {
        return getTranslation(TARGET_SECTION, target.name()) + super.toString();
    }
}
