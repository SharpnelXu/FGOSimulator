package yome.fgo.simulator.models.conditions;

import lombok.Builder;
import yome.fgo.data.proto.FgoStorageData.FateClass;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.utils.TargetUtils;

import static yome.fgo.simulator.translation.TranslationManager.CLASS_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.CONDITION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.TARGET_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

@Builder
public class TargetsHaveClass extends Condition {
    private final Target target;
    private final FateClass targetClass;

    @Override
    public boolean evaluate(final Simulation simulation) {
        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            if (combatant.getFateClass() == targetClass) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format(
                getTranslation(CONDITION_SECTION, "%s Have %s"),
                getTranslation(TARGET_SECTION, target.name()),
                getTranslation(CLASS_SECTION, targetClass.name())
        );
    }
}
