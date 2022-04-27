package yome.fgo.simulator.models.conditions;

import lombok.Builder;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;

import java.util.List;

import static yome.fgo.simulator.utils.TargetUtils.getTargets;

@Builder
public class TargetsContainsSpecificServant implements Condition {
    private final Target target;
    private final String servantId;

    @Override
    public boolean evaluate(final Simulation simulation) {
        final List<Combatant> combatants = getTargets(simulation, target);
        for (final Combatant combatant : combatants) {
            if (combatant.getId().equalsIgnoreCase(servantId)) {
                return true;
            }
        }
        return false;
    }
}
