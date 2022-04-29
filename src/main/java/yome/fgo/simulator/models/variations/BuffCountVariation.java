package yome.fgo.simulator.models.variations;

import lombok.AllArgsConstructor;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.conditions.Condition;
import yome.fgo.simulator.utils.RoundUtils;
import yome.fgo.simulator.utils.SpecificDamageUtils;

@AllArgsConstructor
public class BuffCountVariation implements Variation {
    private final int maxCount;
    private final Condition matchCondition;
    private final Target target;

    @Override
    public double evaluate(final Simulation simulation, final double baseValue, final double additionValue) {
        final int count = SpecificDamageUtils.countBuff(simulation, target, matchCondition, maxCount);
        return  RoundUtils.roundNearest(baseValue + additionValue * count);
    }
}
