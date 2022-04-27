package yome.fgo.simulator.models.conditions;

import lombok.AllArgsConstructor;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.utils.TargetUtils;

@AllArgsConstructor
public class TargetsReceivedInstantDeath implements Condition {
    private final Target target;

    @Override
    public boolean evaluate(final Simulation simulation) {
        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            if (combatant.isReceivedInstantDeath()) {
                return true;
            }
        }
        return false;
    }
}
