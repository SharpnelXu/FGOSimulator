package yome.fgo.simulator.models.effects;

import lombok.Builder;
import lombok.experimental.SuperBuilder;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.utils.RoundUtils;
import yome.fgo.simulator.utils.SpecificDamageUtils;

import static yome.fgo.data.proto.FgoStorageData.Target.DEFENDER;

@SuperBuilder
public class TraitSpecificNpDamage extends NoblePhantasmDamage {
    private final String targetTrait;
    @Builder.Default
    private final Target specificTarget = DEFENDER;

    @Override
    protected double getNpSpecificDamageRate(final Simulation simulation, final int level) {
        if (npSpecificDamageCondition.evaluate(simulation)) {
            final double baseSpecificDamageRate = isNpSpecificDamageOverchargedEffect ?
                    npSpecificDamageRates.get(level - 1) :
                    npSpecificDamageRates.get(0);


            final int buffCount = SpecificDamageUtils.countTrait(simulation, specificTarget, targetTrait);
            return RoundUtils.roundNearest(baseSpecificDamageRate * buffCount);
        } else {
            return 1.0;
        }
    }
}
