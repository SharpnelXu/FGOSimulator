package yome.fgo.simulator.models.effects;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.conditions.Condition;

import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static yome.fgo.simulator.models.conditions.Always.ALWAYS;
import static yome.fgo.simulator.models.effects.Effect.EffectFields.EFFECT_FIELD_CARD_TYPE_SELECT;
import static yome.fgo.simulator.models.effects.Effect.EffectFields.EFFECT_FIELD_DOUBLE_VALUE;
import static yome.fgo.simulator.models.effects.Effect.EffectFields.EFFECT_FIELD_GRANT_BUFF;
import static yome.fgo.simulator.models.effects.Effect.EffectFields.EFFECT_FIELD_HP_CHANGE;
import static yome.fgo.simulator.models.effects.Effect.EffectFields.EFFECT_FIELD_INT_VALUE;
import static yome.fgo.simulator.models.effects.Effect.EffectFields.EFFECT_FIELD_NP_DAMAGE;
import static yome.fgo.simulator.models.effects.Effect.EffectFields.EFFECT_FIELD_RANDOM_EFFECT;
import static yome.fgo.simulator.models.effects.Effect.EffectFields.EFFECT_FIELD_REMOVE_BUFF;
import static yome.fgo.simulator.models.effects.Effect.EffectFields.EFFECT_FIELD_TARGET;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.EFFECT_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.ENTITY_NAME_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

@SuperBuilder
@Getter
public abstract class Effect {
    public enum EffectFields {
        EFFECT_FIELD_TARGET,
        EFFECT_FIELD_INT_VALUE,
        EFFECT_FIELD_DOUBLE_VALUE,
        EFFECT_FIELD_NP_DAMAGE,
        EFFECT_FIELD_GRANT_BUFF,
        EFFECT_FIELD_HP_CHANGE,
        EFFECT_FIELD_REMOVE_BUFF,
        EFFECT_FIELD_RANDOM_EFFECT,
        EFFECT_FIELD_CARD_TYPE_SELECT
    }

    /**
     * Display order of GUI is the same as Enum order.
     */
    public enum EffectType {
        GRANT_BUFF(GrantBuff.class.getSimpleName(), ImmutableSet.of(EFFECT_FIELD_GRANT_BUFF, EFFECT_FIELD_TARGET)),
        FORCE_GRANT_BUFF(ForceGrantBuff.class.getSimpleName(), ImmutableSet.of(EFFECT_FIELD_GRANT_BUFF, EFFECT_FIELD_TARGET)),
        REMOVE_BUFF(RemoveBuff.class.getSimpleName(), ImmutableSet.of(EFFECT_FIELD_TARGET, EFFECT_FIELD_INT_VALUE, EFFECT_FIELD_REMOVE_BUFF)),
        FORCE_REMOVE_BUFF(ForceRemoveBuff.class.getSimpleName(), ImmutableSet.of(EFFECT_FIELD_TARGET, EFFECT_FIELD_INT_VALUE, EFFECT_FIELD_REMOVE_BUFF)),
        CRITICAL_STAR_CHANGE(CriticalStarChange.class.getSimpleName(), ImmutableSet.of(EFFECT_FIELD_INT_VALUE)),
        NP_CHANGE(NpChange.class.getSimpleName(), ImmutableSet.of(EFFECT_FIELD_DOUBLE_VALUE, EFFECT_FIELD_TARGET)),
        NP_GAUGE_CHANGE(NpGaugeChange.class.getSimpleName(), ImmutableSet.of(EFFECT_FIELD_INT_VALUE, EFFECT_FIELD_TARGET)),
        HP_CHANGE(HpChange.class.getSimpleName(), ImmutableSet.of(EFFECT_FIELD_TARGET, EFFECT_FIELD_HP_CHANGE)),
        NOBLE_PHANTASM_DAMAGE(NoblePhantasmDamage.class.getSimpleName(), ImmutableSet.of(EFFECT_FIELD_DOUBLE_VALUE, EFFECT_FIELD_NP_DAMAGE, EFFECT_FIELD_TARGET)),

        DECREASE_ACTIVE_SKILL_COOL_DOWN(DecreaseActiveSkillCoolDown.class.getSimpleName(), ImmutableSet.of(EFFECT_FIELD_INT_VALUE, EFFECT_FIELD_TARGET)),
        INSTANT_DEATH(InstantDeath.class.getSimpleName(), ImmutableSet.of(EFFECT_FIELD_TARGET)),
        FORCE_INSTANT_DEATH(ForceInstantDeath.class.getSimpleName(), ImmutableSet.of(EFFECT_FIELD_TARGET)),
        ASCENSION_CHANGE(AscensionChange.class.getSimpleName(), ImmutableSet.of(EFFECT_FIELD_INT_VALUE, EFFECT_FIELD_TARGET)),
        BUFF_ABSORPTION(BuffAbsorption.class.getSimpleName(), ImmutableSet.of(EFFECT_FIELD_TARGET)),
        CARD_TYPE_CHANGE_SELECT(CardTypeChangeSelect.class.getSimpleName(), ImmutableSet.of(EFFECT_FIELD_GRANT_BUFF, EFFECT_FIELD_TARGET, EFFECT_FIELD_CARD_TYPE_SELECT)),
        ORDER_CHANGE(OrderChange.class.getSimpleName(), ImmutableSet.of()),
        MOVE_TO_LAST_BACKUP(MoveToLastBackup.class.getSimpleName(), ImmutableSet.of()),
        SHUFFLE_CARDS(ShuffleCards.class.getSimpleName(), ImmutableSet.of()),
        RANDOM_EFFECTS(RandomEffects.class.getSimpleName(), ImmutableSet.of(EFFECT_FIELD_RANDOM_EFFECT));

        private final String type;
        private final Set<EffectFields> requiredFields;

        EffectType(final String type, final Set<EffectFields> requiredFields) {
            this.type = type;
            this.requiredFields = requiredFields;
        }

        private static final Map<String, EffectType> BY_TYPE_STRING = new LinkedHashMap<>();

        static {
            for (final EffectType effectType : values()) {
                BY_TYPE_STRING.put(effectType.type, effectType);
            }
        }

        public Set<EffectFields> getRequiredFields() {
            return requiredFields;
        }

        public static EffectType ofType(final String type) {
            if (!BY_TYPE_STRING.containsKey(type)) {
                throw new IllegalArgumentException("Unknown EffectType: " + type);
            }

            return BY_TYPE_STRING.get(type);
        }

        public static Set<String> getOrder() {
            return BY_TYPE_STRING.keySet();
        }
    }
    
    protected final boolean isOverchargedEffect;
    @Builder.Default
    protected final Condition applyCondition = ALWAYS;

    protected final boolean isProbabilityOvercharged;

    @Builder.Default
    protected final List<Double> probabilities = Lists.newArrayList(1.0);

    protected double getProbability(final int level) {
        if (isProbabilityOvercharged) {
            return probabilities.get(level - 1);
        } else {
            return probabilities.get(0);
        }
    }

    protected boolean shouldApply(final Simulation simulation) {
        return applyCondition.evaluate(simulation);
    }

    public void apply(final Simulation simulation) {
        apply(simulation, 1);
    }

    public void apply(final Simulation simulation, final int level) {
        if (simulation.getStatsLogger() != null) {
            simulation.getStatsLogger().logEffect(
                    String.format(
                            getTranslation(APPLICATION_SECTION, "%s activates %s"),
                            getTranslation(ENTITY_NAME_SECTION, simulation.getActivator().getId()),
                            this
                    )
            );
        }

        internalApply(simulation, level);
    }

    // for overcharged
    protected abstract void internalApply(final Simulation simulation, final int level);

    @Override
    public String toString() {
        final String base = getTranslation(EFFECT_SECTION, getClass().getSimpleName());
        return base + miscString();
    }

    public String miscString() {
        String base = "";
        if (applyCondition != ALWAYS) {
            base = base + " (" + applyCondition + ")";
        }

        final NumberFormat numberFormat = NumberFormat.getPercentInstance();
        numberFormat.setMaximumFractionDigits(2);
        if (isProbabilityOvercharged()) {
            base = base + " (OC) " + probabilities.stream().map(numberFormat::format).collect(Collectors.toList()) +
                    getTranslation(EFFECT_SECTION, "Probability");
        } else if (probabilities.get(0) != 1.0) {
            base = base + " " + numberFormat.format(probabilities.get(0)) +
                    getTranslation(EFFECT_SECTION, "Probability");
        }
        return base;
    }
}
