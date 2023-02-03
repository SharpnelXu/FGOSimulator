package yome.fgo.simulator.models.variations;

import yome.fgo.data.proto.FgoStorageData.VariationData;
import yome.fgo.simulator.models.conditions.ConditionFactory;
import yome.fgo.simulator.models.variations.Variation.VariationType;

import static yome.fgo.simulator.models.variations.NoVariation.NO_VARIATION;
import static yome.fgo.simulator.models.variations.TurnPassVariation.TURN_PASS_VARIATION;

public class VariationFactory {
    public static Variation buildVariation(final VariationData variationData) {
        return switch (VariationType.ofType(variationData.getType())) {
            case NO_VARIATION -> NO_VARIATION;
            case BUFF_COUNT_VARIATION -> new BuffCountVariation(
                    variationData.getMaxCount(),
                    ConditionFactory.buildCondition(variationData.getConditionData()),
                    variationData.getTarget()
            );
            case TRAIT_COUNT_VARIATION -> new TraitCountVariation(
                    variationData.getMaxCount(),
                    variationData.getTrait(),
                    variationData.getTarget()
            );
            case HP_ABSORPTION_VARIATION -> new HpAbsorptionVariation(variationData.getTarget());
            case NP_ABSORPTION_VARIATION -> new NpAbsorptionVariation(variationData.getTarget());
            case TARGET_COUNT_VARIATION -> new TargetCountVariation(variationData.getTarget());
            case TURN_PASS_VARIATION -> TURN_PASS_VARIATION;
            case HP_VARIATION -> new HpVariation(
                    variationData.getMaxHp(),
                    variationData.getMinHp(),
                    variationData.getTarget()
            );
        };
    }
}
