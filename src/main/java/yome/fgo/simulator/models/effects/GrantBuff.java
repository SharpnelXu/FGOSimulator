package yome.fgo.simulator.models.effects;

import lombok.Builder;
import lombok.experimental.SuperBuilder;
import yome.fgo.data.proto.FgoStorageData.BuffData;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.effects.buffs.BuffFactory;
import yome.fgo.simulator.utils.TargetUtils;

import java.util.List;

@SuperBuilder
public class GrantBuff extends Effect {
    private final List<BuffData> buffData;
    private final Target target;
    private final int buffLevel;

    @Builder.Default
    private final double probability = 1;

    @Override
    public void apply(Simulation simulation, int level) {
        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            simulation.setEffectTarget(combatant);
            if (shouldApply(simulation)) {

                if (probability > simulation.getProbabilityThreshold()) {
                    combatant.addBuff(BuffFactory.buildBuff(buffData.get(level - 1), buffLevel));
                }
            }
            simulation.setEffectTarget(null);
        }
    }
}
