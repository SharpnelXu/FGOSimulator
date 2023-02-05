package yome.fgo.simulator.models.conditions;

import com.google.common.collect.ImmutableSet;
import yome.fgo.simulator.models.Simulation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static yome.fgo.simulator.models.conditions.Condition.ConditionFields.CONDITION_FIELD_BUFF_TRAIT_VALUE;
import static yome.fgo.simulator.models.conditions.Condition.ConditionFields.CONDITION_FIELD_BUFF_TYPE;
import static yome.fgo.simulator.models.conditions.Condition.ConditionFields.CONDITION_FIELD_CARD_TYPE;
import static yome.fgo.simulator.models.conditions.Condition.ConditionFields.CONDITION_FIELD_CLASS_VALUE;
import static yome.fgo.simulator.models.conditions.Condition.ConditionFields.CONDITION_FIELD_DOUBLE_VALUE;
import static yome.fgo.simulator.models.conditions.Condition.ConditionFields.CONDITION_FIELD_INT_VALUE;
import static yome.fgo.simulator.models.conditions.Condition.ConditionFields.CONDITION_FIELD_LIMITED_SUB_CONDITION;
import static yome.fgo.simulator.models.conditions.Condition.ConditionFields.CONDITION_FIELD_NAMES;
import static yome.fgo.simulator.models.conditions.Condition.ConditionFields.CONDITION_FIELD_TARGET;
import static yome.fgo.simulator.models.conditions.Condition.ConditionFields.CONDITION_FIELD_TRAIT_VALUE;
import static yome.fgo.simulator.models.conditions.Condition.ConditionFields.CONDITION_FIELD_UNLIMITED_SUB_CONDITION;
import static yome.fgo.simulator.translation.TranslationManager.CONDITION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public abstract class Condition {
    public enum ConditionFields {
        CONDITION_FIELD_INT_VALUE,
        CONDITION_FIELD_DOUBLE_VALUE,
        CONDITION_FIELD_TRAIT_VALUE,
        CONDITION_FIELD_NAMES,
        CONDITION_FIELD_BUFF_TYPE,
        CONDITION_FIELD_CARD_TYPE,
        CONDITION_FIELD_CLASS_VALUE,
        CONDITION_FIELD_TARGET,
        CONDITION_FIELD_UNLIMITED_SUB_CONDITION,
        CONDITION_FIELD_LIMITED_SUB_CONDITION,
        CONDITION_FIELD_BUFF_TRAIT_VALUE
    }

    /**
     * Display order of GUI is the same as Enum order.
     */
    public enum ConditionType {
        ALWAYS(Always.class.getSimpleName(), ImmutableSet.of()),
        NEVER(Never.class.getSimpleName(), ImmutableSet.of()),
        AND(And.class.getSimpleName(), ImmutableSet.of(CONDITION_FIELD_UNLIMITED_SUB_CONDITION)),
        OR(Or.class.getSimpleName(), ImmutableSet.of(CONDITION_FIELD_UNLIMITED_SUB_CONDITION)),
        NOT(Not.class.getSimpleName(), ImmutableSet.of(CONDITION_FIELD_LIMITED_SUB_CONDITION)),

        CARD_TYPE_EQUALS(CardTypeEquals.class.getSimpleName(), ImmutableSet.of(CONDITION_FIELD_CARD_TYPE)),
        IS_CRITICAL_STRIKE(IsCriticalStrike.class.getSimpleName(), ImmutableSet.of()),
        NP_CARD(NpCard.class.getSimpleName(), ImmutableSet.of()),

        BUFF_TYPE_EQUALS(BuffTypeEquals.class.getSimpleName(), ImmutableSet.of(CONDITION_FIELD_BUFF_TYPE)),
        BUFF_HAS_TRAIT(BuffHasTrait.class.getSimpleName(), ImmutableSet.of(CONDITION_FIELD_BUFF_TRAIT_VALUE)),
        BUFF_REMOVABLE(BuffRemovable.class.getSimpleName(), ImmutableSet.of()),

        TARGETS_HAVE_CLASS(
                TargetsHaveClass.class.getSimpleName(),
                ImmutableSet.of(CONDITION_FIELD_TARGET, CONDITION_FIELD_CLASS_VALUE)
        ),
        TARGETS_HAVE_BUFF(
                TargetsHaveBuff.class.getSimpleName(),
                ImmutableSet.of(CONDITION_FIELD_TARGET, CONDITION_FIELD_LIMITED_SUB_CONDITION)
        ),
        TARGETS_HAVE_TRAIT(
                TargetsHaveTrait.class.getSimpleName(),
                ImmutableSet.of(CONDITION_FIELD_TARGET, CONDITION_FIELD_TRAIT_VALUE)
        ),
        TARGETS_CONTAINS_SPECIFIC_SERVANT(
                TargetsContainsSpecificServant.class.getSimpleName(),
                ImmutableSet.of(CONDITION_FIELD_TARGET, CONDITION_FIELD_NAMES)
        ),

        RARITY_AT_LEAST(
                RarityAtLeast.class.getSimpleName(),
                ImmutableSet.of(CONDITION_FIELD_INT_VALUE, CONDITION_FIELD_TARGET)
        ),
        SERVANTS_WITH_CE(
                ServantsWithCE.class.getSimpleName(),
                ImmutableSet.of(CONDITION_FIELD_TARGET, CONDITION_FIELD_NAMES)
        ),
        TARGETS_RECEIVED_INSTANT_DEATH(
                TargetsReceivedInstantDeath.class.getSimpleName(),
                ImmutableSet.of(CONDITION_FIELD_TARGET)
        ),
        STAGE_HAS_TRAIT(StageHasTrait.class.getSimpleName(), ImmutableSet.of(CONDITION_FIELD_TRAIT_VALUE)),

        CRIT_STAR_AT_LEAST(CritStarAtLeast.class.getSimpleName(), ImmutableSet.of(CONDITION_FIELD_INT_VALUE)),
        NP_AT_LEAST(NpAtLeast.class.getSimpleName(), ImmutableSet.of(CONDITION_FIELD_DOUBLE_VALUE)),
        NP_GAUGE_AT_LEAST(NpGaugeAtLeast.class.getSimpleName(), ImmutableSet.of(CONDITION_FIELD_INT_VALUE)),
        HP_AT_LEAST(HpAtLeast.class.getSimpleName(), ImmutableSet.of(CONDITION_FIELD_INT_VALUE)),
        HP_PERCENT_AT_MOST(HpPercentAtMost.class.getSimpleName(), ImmutableSet.of(CONDITION_FIELD_DOUBLE_VALUE)),

        SERVANTS_EXPLODABLE(ServantsExplodable.class.getSimpleName(), ImmutableSet.of()),
        BACKUP_SERVANTS_EXIST(BackupServantsExist.class.getSimpleName(), ImmutableSet.of());

        private final String type;
        private final Set<ConditionFields> requiredFields;

        ConditionType(final String type, final Set<ConditionFields> requiredFields) {
            this.type = type;
            this.requiredFields = requiredFields;
        }

        private static final Map<String, ConditionType> BY_TYPE_STRING = new LinkedHashMap<>();

        static {
            for (final ConditionType conditionType : values()) {
                BY_TYPE_STRING.put(conditionType.type, conditionType);
            }
        }

        public Set<ConditionFields> getRequiredFields() {
            return requiredFields;
        }

        public static ConditionType ofType(final String type) {
            if (!BY_TYPE_STRING.containsKey(type)) {
                throw new IllegalArgumentException("Unknown ConditionType: " + type);
            }

            return BY_TYPE_STRING.get(type);
        }

        public static Set<String> getOrder() {
            return BY_TYPE_STRING.keySet();
        }
    }

    public abstract boolean evaluate(final Simulation simulation);

    @Override
    public String toString() {
        return getTranslation(CONDITION_SECTION, this.getClass().getSimpleName());
    }
}
