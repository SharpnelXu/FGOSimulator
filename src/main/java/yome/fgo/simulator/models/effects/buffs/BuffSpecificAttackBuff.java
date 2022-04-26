package yome.fgo.simulator.models.effects.buffs;

import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.utils.RoundUtils;

@SuperBuilder
public class BuffSpecificAttackBuff extends SpecificAttackBuff {
    private static final int MAX_BUFF_COUNT = 10;

    private final Class<?> targetBuff;

    @Override
    public double getValue(final Simulation simulation) {
        int buffCount = 0;
        for (final Buff buff : simulation.getDefender().getBuffs()) {
            if (targetBuff.isInstance(buff)) {
                buffCount++;
                if (MAX_BUFF_COUNT == buffCount) {
                    return RoundUtils.roundNearest(value * MAX_BUFF_COUNT);
                }
            }
        }

        return RoundUtils.roundNearest(value * buffCount);
    }
}
