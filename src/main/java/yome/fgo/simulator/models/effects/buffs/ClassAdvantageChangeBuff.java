package yome.fgo.simulator.models.effects.buffs;

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import yome.fgo.data.proto.FgoStorageData.ClassAdvantageChangeMode;
import yome.fgo.data.proto.FgoStorageData.FateClass;

import java.util.List;

import static yome.fgo.data.proto.FgoStorageData.ClassAdvantageChangeMode.CLASS_ADV_NO_CHANGE;
import static yome.fgo.data.proto.FgoStorageData.FateClass.ANY_CLASS;
import static yome.fgo.simulator.translation.TranslationManager.BUFF_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.CLASS_ADV_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

@SuperBuilder
@Getter
public class ClassAdvantageChangeBuff extends Buff {
    @Builder.Default
    private final ClassAdvantageChangeMode attackMode = CLASS_ADV_NO_CHANGE;
    @Builder.Default
    private final double attackAdvantage = 1;
    @Builder.Default
    private final List<FateClass> attackModeAffectedClasses = ImmutableList.of(ANY_CLASS);

    @Builder.Default
    private final ClassAdvantageChangeMode defenseMode = CLASS_ADV_NO_CHANGE;
    @Builder.Default
    private final double defenseAdvantage = 1;
    @Builder.Default
    private final List<FateClass> defenseModeAffectedClasses = ImmutableList.of(ANY_CLASS);

    public double asAttacker(final double baseRate, final FateClass defenderClass) {
        if (attackMode == CLASS_ADV_NO_CHANGE) {
            return baseRate;
        }

        if (!attackModeAffectedClasses.contains(defenderClass) && !attackModeAffectedClasses.contains(ANY_CLASS)) {
            return baseRate;
        }

        return switch (attackMode) {
            case CLASS_ADV_REMOVE_ADV -> baseRate > 1 ? attackAdvantage : baseRate;
            case CLASS_ADV_REMOVE_DISADV -> baseRate < 1 ? attackAdvantage : baseRate;
            case CLASS_ADV_FIXED_RATE -> attackAdvantage;
            default -> baseRate;
        };
    }

    public double asDefender(final double baseRate, final FateClass attackerClass) {
        if (defenseMode == CLASS_ADV_NO_CHANGE) {
            return baseRate;
        }

        if (!defenseModeAffectedClasses.contains(attackerClass) && !defenseModeAffectedClasses.contains(ANY_CLASS)) {
            return baseRate;
        }

        return switch (defenseMode) {
            case CLASS_ADV_REMOVE_ADV -> baseRate < 1 ? defenseAdvantage : baseRate;
            case CLASS_ADV_REMOVE_DISADV -> baseRate > 1 ? defenseAdvantage : baseRate;
            case CLASS_ADV_FIXED_RATE -> defenseAdvantage;
            default -> baseRate;
        };
    }

    @Override
    public boolean commonBuffCondition() {
        return true;
    }

    @Override
    public boolean commonDebuffCondition() {
        return false;
    }

    @Override
    protected boolean commonStackableCondition() {
        return true;
    }

    @Override
    public String toString() {
        String base = "";
        if (attackMode != CLASS_ADV_NO_CHANGE) {
            base = base + " " + getTranslation(BUFF_SECTION, "Attack Affinity") + getTranslation(CLASS_ADV_SECTION, attackMode.name());
            if (attackAdvantage != 1) {
                base = base + " " + attackAdvantage;
            }
        }
        if (defenseMode != CLASS_ADV_NO_CHANGE) {
            base = base + " " + getTranslation(BUFF_SECTION, "Defense Affinity") + getTranslation(CLASS_ADV_SECTION, defenseMode.name());
            if (defenseAdvantage != 1) {
                base = base + " " + defenseAdvantage;
            }
        }

        return base;
    }

}
