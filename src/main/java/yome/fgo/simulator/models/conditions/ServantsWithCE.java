package yome.fgo.simulator.models.conditions;

import lombok.Builder;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.combatants.Servant;

import java.util.List;

import static yome.fgo.simulator.translation.TranslationManager.CONDITION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.ENTITY_NAME_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.TARGET_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;
import static yome.fgo.simulator.utils.TargetUtils.getTargets;

@Builder
public class ServantsWithCE extends Condition {
    private final Target target;
    private final String ceId;

    @Override
    public boolean evaluate(final Simulation simulation) {
        final List<Combatant> combatants = getTargets(simulation, target);
        for (final Combatant combatant : combatants) {
            if (combatant instanceof Servant) {
                final Servant servant = (Servant) combatant;
                if (servant.getCraftEssence() != null && servant.getCraftEssence().getCeId().equalsIgnoreCase(ceId)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format(
                getTranslation(CONDITION_SECTION, "%s Have %s"),
                getTranslation(TARGET_SECTION, target.name()),
                getTranslation(ENTITY_NAME_SECTION, ceId)
        );
    }
}
