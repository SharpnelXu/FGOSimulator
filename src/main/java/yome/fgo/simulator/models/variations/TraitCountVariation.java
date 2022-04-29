package yome.fgo.simulator.models.variations;

import lombok.AllArgsConstructor;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.utils.RoundUtils;
import yome.fgo.simulator.utils.SpecificDamageUtils;

@AllArgsConstructor
public class TraitCountVariation implements Variation {
    private final int maxCount;
    private final String targetTrait;
    private final Target target;

    @Override
    public double evaluate(final Simulation simulation, final double baseValue, final double additionValue) {
        final int count = SpecificDamageUtils.countTrait(simulation, target, targetTrait, maxCount);
        return  RoundUtils.roundNearest(baseValue + additionValue * count);
    }
}
