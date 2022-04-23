package yome.fgo.simulator.models.effects;

import lombok.experimental.SuperBuilder;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.utils.TargetUtils;

import java.util.List;

@SuperBuilder
public class NpChange extends Effect {
    private final Target target;
    private final List<Double> percentNpChanges;

    @Override
    public void apply(final Simulation simulation, final int level) {
        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            combatant.changeNp(percentNpChanges.get(level - 1));
        }
    }
}
