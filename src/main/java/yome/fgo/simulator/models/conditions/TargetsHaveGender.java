package yome.fgo.simulator.models.conditions;

import lombok.experimental.SuperBuilder;
import yome.fgo.data.proto.FgoStorageData.Gender;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.utils.TargetUtils;

@SuperBuilder
public class TargetsHaveGender implements Condition {
    private final Target target;
    private final Gender targetGender;

    @Override
    public boolean evaluate(final Simulation simulation) {
        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            if (combatant.getGender() == targetGender) {
                return true;
            }
        }
        return false;
    }
}
