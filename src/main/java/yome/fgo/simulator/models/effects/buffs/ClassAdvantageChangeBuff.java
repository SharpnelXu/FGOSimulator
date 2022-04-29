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

        switch (attackMode) {
            case CLASS_ADV_REMOVE_ADV:
                return baseRate > 1 ? attackAdvantage : baseRate;
            case CLASS_ADV_REMOVE_DISADV:
                return baseRate < 1 ? attackAdvantage : baseRate;
            case CLASS_ADV_FIXED_RATE:
                return attackAdvantage;
            default:
                return baseRate;
        }
    }

    public double asDefender(final double baseRate, final FateClass attackerClass) {
        if (defenseMode == CLASS_ADV_NO_CHANGE) {
            return baseRate;
        }

        if (!defenseModeAffectedClasses.contains(attackerClass) && !defenseModeAffectedClasses.contains(ANY_CLASS)) {
            return baseRate;
        }

        switch (defenseMode) {
            case CLASS_ADV_REMOVE_ADV:
                return baseRate < 1 ? defenseAdvantage : baseRate;
            case CLASS_ADV_REMOVE_DISADV:
                return baseRate > 1 ? defenseAdvantage : baseRate;
            case CLASS_ADV_FIXED_RATE:
                return attackAdvantage;
            default:
                return baseRate;
        }
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

}
