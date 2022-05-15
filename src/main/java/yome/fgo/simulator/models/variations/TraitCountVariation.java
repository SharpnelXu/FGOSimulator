package yome.fgo.simulator.models.variations;

import lombok.AllArgsConstructor;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.utils.RoundUtils;
import yome.fgo.simulator.utils.TargetUtils;

import static yome.fgo.simulator.translation.TranslationManager.TARGET_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.TRAIT_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.VARIATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

@AllArgsConstructor
public class TraitCountVariation extends Variation {
    private final int maxCount;
    private final String targetTrait;
    private final Target target;

    @Override
    public double evaluate(final Simulation simulation, final double baseValue, final double additionValue) {
        final int count = countTrait(simulation, target, targetTrait, maxCount);
        return  RoundUtils.roundNearest(baseValue + additionValue * count);
    }

    public static int countTrait(
            final Simulation simulation,
            final Target target,
            final String targetTrait,
            final int maxCount
    ) {
        int count = 0;
        for (final Combatant combatant : TargetUtils.getTargets(simulation, target)) {
            for (final String trait : combatant.getAllTraits(simulation)) {
                if (targetTrait.equalsIgnoreCase(trait)) {
                    count += 1;
                    if (maxCount > 0 && maxCount == count) {
                        return maxCount;
                    }
                }
            }
        }
        return count;
    }

    @Override
    public String toString() {
        final String base = String.format(
                getTranslation(VARIATION_SECTION, "%s Have %s"),
                getTranslation(TARGET_SECTION, target.name()),
                getTranslation(TRAIT_SECTION, targetTrait)
        );
        if (maxCount > 0) {
            return base + " " + String.format(getTranslation(VARIATION_SECTION, "(max %d)"), maxCount);
        }
        return base;
    }
}
