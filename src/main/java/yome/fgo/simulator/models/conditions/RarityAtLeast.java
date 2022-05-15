package yome.fgo.simulator.models.conditions;

import lombok.AllArgsConstructor;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.utils.TargetUtils;

import static yome.fgo.simulator.translation.TranslationManager.CONDITION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.TARGET_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

@AllArgsConstructor
public class RarityAtLeast extends Condition {
    private final int rarity;
    private final Target target;

    @Override
    public boolean evaluate(final Simulation simulation) {
        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            if (combatant.getRarity() >= rarity) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format(
                getTranslation(CONDITION_SECTION, "%s Rarity %d"),
                getTranslation(TARGET_SECTION, target.name()),
                rarity
        );
    }
}
