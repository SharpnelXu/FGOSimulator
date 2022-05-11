package yome.fgo.simulator.models.conditions;

import lombok.Builder;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.combatants.Servant;

import java.util.List;

import static yome.fgo.simulator.utils.TargetUtils.getTargets;

@Builder
public class ServantsWithCE implements Condition {
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
}
