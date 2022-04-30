package yome.fgo.simulator.models.variations;

import lombok.AllArgsConstructor;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.conditions.Condition;
import yome.fgo.simulator.models.effects.buffs.Buff;
import yome.fgo.simulator.utils.RoundUtils;
import yome.fgo.simulator.utils.TargetUtils;

@AllArgsConstructor
public class BuffCountVariation implements Variation {
    private final int maxCount;
    private final Condition matchCondition;
    private final Target target;

    @Override
    public double evaluate(final Simulation simulation, final double baseValue, final double additionValue) {
        final int count = countBuff(simulation, target, matchCondition, maxCount);
        return  RoundUtils.roundNearest(baseValue + additionValue * count);
    }


    public static int countBuff(
            final Simulation simulation,
            final Target target,
            final Condition matchCondition,
            final int maxCount
    ) {
        int count = 0;
        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            for (final Buff buff : combatant.getBuffs()) {
                simulation.setCurrentBuff(buff);

                if (matchCondition.evaluate(simulation)) {
                    count++;
                    if (maxCount > 0 && maxCount == count) {
                        return maxCount;
                    }
                }

                simulation.unsetCurrentBuff();
            }
        }
        return count;
    }
}
