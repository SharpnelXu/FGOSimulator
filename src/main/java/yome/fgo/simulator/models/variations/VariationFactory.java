package yome.fgo.simulator.models.variations;

import yome.fgo.data.proto.FgoStorageData.VariationData;
import yome.fgo.simulator.models.conditions.ConditionFactory;

import static yome.fgo.simulator.models.variations.NoVariation.NO_VARIATION;
import static yome.fgo.simulator.models.variations.TurnPassVariation.TURN_PASS_VARIATION;

public class VariationFactory {
    public static Variation buildVariation(final VariationData variationData) {
        final String type = variationData.getType();

        if (type.equalsIgnoreCase(BuffCountVariation.class.getSimpleName())) {
            return new BuffCountVariation(
                    variationData.getMaxCount(),
                    ConditionFactory.buildCondition(variationData.getConditionData()),
                    variationData.getTarget()
            );

        } else if (type.equalsIgnoreCase(HpVariation.class.getSimpleName())) {
            return new HpVariation(variationData.getMaxHp(), variationData.getMinHp(), variationData.getTarget());

        } else if (type.equalsIgnoreCase(NoVariation.class.getSimpleName())) {
            return NO_VARIATION;

        } else if (type.equalsIgnoreCase(NpAbsorptionVariation.class.getSimpleName())) {
            return new NpAbsorptionVariation(variationData.getTarget());

        } else if (type.equalsIgnoreCase(TraitCountVariation.class.getSimpleName())) {
            return new TraitCountVariation(variationData.getMaxCount(), variationData.getTrait(), variationData.getTarget());

        } else if (type.equalsIgnoreCase(TurnPassVariation.class.getSimpleName())) {
            return TURN_PASS_VARIATION;
        }


        throw new IllegalArgumentException("Unrecognized Variation type: " + type);
    }
}
