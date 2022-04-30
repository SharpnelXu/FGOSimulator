package yome.fgo.simulator.models.conditions;

import lombok.Builder;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.effects.buffs.Buff;
import yome.fgo.simulator.utils.TargetUtils;

@Builder
public class TargetsHaveBuff implements Condition {
    private final Target target;
    private final Condition buffMatchCondition;

    @Override
    public boolean evaluate(final Simulation simulation) {
        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            for (final Buff buff : combatant.getBuffs()) {
                simulation.setCurrentBuff(buff);
                final boolean match = buffMatchCondition.evaluate(simulation);
                simulation.unsetCurrentBuff();

                if (match) {
                    return true;
                }
            }
        }
        return false;
    }
}
