package yome.fgo.simulator.models.effects;

import lombok.Builder;
import lombok.experimental.SuperBuilder;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.utils.TargetUtils;

@SuperBuilder
public class MaxHpChange extends IntValuedEffect {
    private final Target target;
    @Builder.Default
    private final int numTurnsActive = -1;

    @Override
    protected void internalApply(final Simulation simulation, final int level) {
        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            simulation.setEffectTarget(combatant);
            if (shouldApply(simulation)) {
                combatant.changeMaxHp(values.get(level - 1), numTurnsActive);
            }
            simulation.setEffectTarget(null);
        }
    }
}
