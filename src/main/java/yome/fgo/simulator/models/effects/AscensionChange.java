package yome.fgo.simulator.models.effects;

import lombok.experimental.SuperBuilder;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.combatants.Servant;
import yome.fgo.simulator.utils.TargetUtils;

import java.util.List;

@SuperBuilder
public class AscensionChange extends IntValuedEffect {
    private final Target target;
    private final List<Integer> ascensionLevels;

    @Override
    protected void internalApply(final  Simulation simulation, final int level) {
        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            if (!(combatant instanceof Servant)) {
                continue;
            }

            simulation.setEffectTarget(combatant);
            final Servant servant = (Servant) combatant;
            servant.changeAscension(simulation, ascensionLevels.get(level - 1));
            simulation.unsetEffectTarget();
        }
    }
}
