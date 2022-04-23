package yome.fgo.simulator.models.conditions;

import lombok.Builder;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;

import java.util.List;

import static yome.fgo.simulator.utils.TargetUtils.getTargets;

@Builder
public class TargetsHaveTrait implements Condition {
    private final Target target;
    private final String trait;
    public boolean evaluate(final Simulation simulation) {
        final List<Combatant> combatants = getTargets(simulation, target);
        for (final Combatant combatant : combatants) {
            if (combatant.getAllTraits(simulation).stream().noneMatch(trait::equalsIgnoreCase)) {
                return false;
            }
        }
        return true;
    }
}
