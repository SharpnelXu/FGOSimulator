package yome.fgo.simulator.models.effects.buffs;

import lombok.experimental.SuperBuilder;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.utils.RoundUtils;
import yome.fgo.simulator.utils.TargetUtils;

@SuperBuilder
public class BuffSpecificAttackBuff extends SpecificAttackBuff {
    private static final int MAX_BUFF_COUNT = 10;

    private final Class<?> targetBuff;
    private final Target target;

    @Override
    public double getValue(final Simulation simulation) {
        int buffCount = 0;
        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            for (final Buff buff : combatant.getBuffs()) {
                if (targetBuff.isInstance(buff)) {
                    buffCount++;
                    if (MAX_BUFF_COUNT == buffCount) {
                        return RoundUtils.roundNearest(value * MAX_BUFF_COUNT);
                    }
                }
            }
        }

        return RoundUtils.roundNearest(value * buffCount);
    }
}
