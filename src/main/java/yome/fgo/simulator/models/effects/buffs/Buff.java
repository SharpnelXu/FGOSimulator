package yome.fgo.simulator.models.effects.buffs;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import yome.fgo.data.proto.FgoStorageData.ClassAdvantageChangeMode;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.data.proto.FgoStorageData.FateClass;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.conditions.Condition;
import yome.fgo.simulator.models.effects.Effect;
import yome.fgo.simulator.models.effects.ForceGrantBuff;
import yome.fgo.simulator.models.effects.ForceRemoveBuff;
import yome.fgo.simulator.models.variations.Variation;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static yome.fgo.data.proto.FgoStorageData.BuffTraits.NEGATIVE_BUFF;
import static yome.fgo.data.proto.FgoStorageData.BuffTraits.POSITIVE_BUFF;
import static yome.fgo.data.proto.FgoStorageData.ClassAdvantageChangeMode.CLASS_ADV_NO_CHANGE;
import static yome.fgo.data.proto.FgoStorageData.FateClass.ANY_CLASS;
import static yome.fgo.simulator.models.conditions.Always.ALWAYS;
import static yome.fgo.simulator.models.effects.buffs.BuffType.CHARM_RESIST_DOWN;
import static yome.fgo.simulator.models.effects.buffs.BuffType.DELAYED_EFFECT;
import static yome.fgo.simulator.models.variations.NoVariation.NO_VARIATION;
import static yome.fgo.simulator.translation.TranslationManager.BUFF_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.CLASS_ADV_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.CLASS_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.COMMAND_CARD_TYPE_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.TARGET_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.TRAIT_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

@Builder
@AllArgsConstructor
@Getter
public class Buff {
    /*
     * ============================================================
     * Common Fields
     * ============================================================
     */
    private BuffType buffType;
    private String iconName;
    @Builder.Default
    private Condition condition = ALWAYS;
    @Builder.Default
    private double probability = 1;
    protected boolean forceStackable;
    private boolean irremovable;
    private boolean isPassive; // used to correctly remove passive & append skills during ascension transition
    @Builder.Default
    protected List<String> buffTraits = Lists.newArrayList();

    /*
     * ============================================================
     * Common Methods
     * ============================================================
     */
    public boolean isBuff() {
        return buffTraits.contains(POSITIVE_BUFF.name());
    }

    public boolean isDebuff() {
        return buffTraits.contains(NEGATIVE_BUFF.name());
    }

    public boolean shouldApply(final Simulation simulation) {
        final boolean eval = condition.evaluate(simulation) && simulation.getProbabilityThreshold() <= probability;
        if (buffType == DELAYED_EFFECT) {
            return eval && activeTurns == 1;
        } else {
            return eval;
        }
    }

    /*
     * ============================================================
     * Execution Fields - counting turns & revert hash
     * ============================================================
     */
    // set to -1 to be active forever (infinite turns or infinite times or both)
    @Builder.Default
    protected int activeTurns = -1;
    @Builder.Default
    protected int activeTimes = -1;

    private boolean isApplied; // for correctly decreasing numTimesActive

    @Builder.Default
    protected int turnPassed = 0;
    private int activatorHash;

    /*
     * ============================================================
     * Execution Methods
     * ============================================================
     */
    public void setApplied() {
        isApplied = true;
    }

    public void decrementActiveTimes() {
        isApplied = false;
        if (activeTimes > 0) {
            activeTimes -= 1;
        }
    }

    public void decrementActiveTurns() {
        turnPassed += 1;
        if (activeTurns > 0) {
            activeTurns -= 1;
        }
    }

    public boolean isInactive() {
        return activeTimes == 0 || activeTurns == 0;
    }

    public boolean isPermanentEffect() {
        return isPermanentTurnEffect() && isPermanentTimeEffect();
    }

    public boolean isPermanentTurnEffect() {
        return this.activeTurns < 0;
    }

    public boolean isPermanentTimeEffect() {
        return this.activeTimes < 0;
    }

    /*
     * ============================================================
     * Valued Buff Fields & Methods
     * ============================================================
     */
    protected double value;
    @Builder.Default
    protected Variation variation = NO_VARIATION;
    protected double addition;
    @Builder.Default
    protected double effectiveness = 1;

    public double getValue(final Simulation simulation) {
        simulation.setCurrentBuff(this);
        final double result = effectiveness * variation.evaluate(simulation, value, addition);
        simulation.unsetCurrentBuff();

        if (buffType == CHARM_RESIST_DOWN) {
            return -1 * result;
        } else {
            return result;
        }
    }

    public void addEffectiveness(final double skillEffectiveness) {
        this.effectiveness += skillEffectiveness;
    }

    /*
     * ============================================================
     * CardType Fields
     * ============================================================
     */
    private CommandCardType commandCardType;

    /*
     * ============================================================
     * ClassAdvantage Fields & Methods
     * ============================================================
     */
    @Builder.Default
    private ClassAdvantageChangeMode attackMode = CLASS_ADV_NO_CHANGE;
    @Builder.Default
    private double attackAdvantage = 1;
    @Builder.Default
    private List<FateClass> attackModeAffectedClasses = ImmutableList.of(ANY_CLASS);

    @Builder.Default
    private ClassAdvantageChangeMode defenseMode = CLASS_ADV_NO_CHANGE;
    @Builder.Default
    private double defenseAdvantage = 1;
    @Builder.Default
    private List<FateClass> defenseModeAffectedClasses = ImmutableList.of(ANY_CLASS);

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

    /*
     * ============================================================
     * EffectActivatingBuff Fields & Methods
     * ============================================================
     */
    @Builder.Default
    private List<Effect> effects = new ArrayList<>();

    public void activate(final Simulation simulation) {
        for (final Effect effect : effects) {
            effect.apply(simulation);
        }

        simulation.checkBuffStatus();
    }

    /*
     * ============================================================
     * OnFieldEffect Fields & Methods
     * ============================================================
     */
    public static final String ON_FIELD_BUFF_MARK = "onFieldBuffMark";
    private Buff activatedBuffBase;
    private Target target;
    private ForceGrantBuff forceGrantBuff;
    private ForceRemoveBuff forceRemoveBuff;

    public void activateOnFieldEffect(final Simulation simulation, final boolean isOnFieldEnterField) {
        if (isOnFieldEnterField) {
            forceGrantBuff.apply(simulation);
        } else {
            forceRemoveBuff.apply(simulation);
        }
        simulation.checkBuffStatus();
    }

    /*
     * ============================================================
     * DamageReflect Fields
     * ============================================================
     */
    private int storedDamage;

    /*
     * ============================================================
     * DamageReflect Methods
     * ============================================================
     */
    public void storeDamage(final int damage) {
        this.storedDamage += damage;
    }

    public void resetStoredDamage() {
        storedDamage = 0;
    }

    /*
     * ============================================================
     * String Value Fields
     * ============================================================
     */
    private String stringValue;

    /*
     * ============================================================
     * Guts Fields
     * ============================================================
     */
    private boolean isPercentageGuts;

    /*
     * ============================================================
     * Buff Type Conversion Fields & Methods
     * ============================================================
     */
    private String convertIconPath;

    public void convertType(final BuffType ofType, final String convertIconPath) {
        this.buffType = ofType;
        this.iconName = convertIconPath;
    }

    /*
     * ============================================================
     * BuffType Methods
     * ============================================================
     */
    public boolean isPositiveBuffType() {
        switch (buffType) {
            case ATTACK_BUFF:
            case BUFF_CHANCE_BUFF:
            case BUFF_REMOVAL_RESIST:
            case COMMAND_CARD_BUFF:
            case COMMAND_CARD_RESIST:
            case CRITICAL_CHANCE_RESIST:
            case CRITICAL_DAMAGE_BUFF:
            case CRITICAL_RATE_BUFF:
            case CRITICAL_STAR_GENERATION_BUFF:
            case CRITICAL_STAR_WEIGHT_BUFF:
            case DAMAGE_ADDITION_BUFF:
            case DAMAGE_REDUCTION_BUFF:
            case DEATH_CHANCE_BUFF:
            case DEATH_RESIST:
            case DEBUFF_CHANCE_BUFF:
            case DEBUFF_RESIST:
            case DEFENSE_BUFF:
            case DEF_NP_GENERATION_BUFF:
            case HEAL_EFFECTIVENESS_BUFF:
            case HEAL_GRANT_EFF_BUFF:
            case MAX_HP_BUFF:
            case NP_DAMAGE_BUFF:
            case NP_DAMAGE_BUFF_EFFECTIVENESS_UP:
            case NP_GENERATION_BUFF:
            case OVERCHARGE_BUFF:
            case PERCENT_ATTACK_BUFF:
            case PERCENT_DEFENSE_BUFF:
            case RECEIVED_BUFF_CHANCE_BUFF:
            case SKILL_EFFECTIVENESS_UP:
            case SPECIFIC_ATTACK_BUFF:
            case SPECIFIC_DEFENSE_BUFF:
            case TAUNT:
                return value > 0 || (value == 0 && addition > 0);
            case BURN:
            case BURN_EFFECTIVENESS_UP:
            case CHARM_RESIST_DOWN:
            case CURSE:
            case CURSE_EFFECTIVENESS_UP:
            case POISON:
            case POISON_EFFECTIVENESS_UP:
                return value < 0 || (value == 0 && addition < 0);
            case ATTACK_BUFF_DURATION_EXTEND:
            case BURNING_LOVE:
            case CHARM:
            case CLASS_ADVANTAGE_CHANGE_BUFF:
            case CONFUSION:
            case DAMAGE_REFLECT:
            case DEATH_EFFECT:
            case DELAYED_EFFECT:
            case DO_NOT_SHUFFLE_IN:
            case END_OF_TURN_EFFECT:
            case ENTER_FIELD_EFFECT:
            case HP_BREAK_EFFECT:
            case LEAVE_FIELD_EFFECT:
            case NP_SEAL:
            case PERMANENT_SLEEP:
            case PIGIFY:
            case POST_ATTACK_EFFECT:
            case POST_DEFENSE_EFFECT:
            case PRE_ATTACK_EFFECT:
            case PRE_DEFENSE_EFFECT:
            case PREVENT_DEATH_AGAINST_DOT:
            case SKILL_SEAL:
            case SLEEP:
            case START_OF_TURN_EFFECT:
            case STUN:
            case TERROR:
            case TRIGGER_ON_GUTS_EFFECT:
                return false;
            case ON_FIELD_EFFECT:
                return activatedBuffBase.isPositiveBuffType();
            default:
                return true;
        }
    }

    public boolean isNegativeBuffType() {
        switch (buffType) {
            case ATTACK_BUFF:
            case BUFF_CHANCE_BUFF:
            case BUFF_REMOVAL_RESIST:
            case COMMAND_CARD_BUFF:
            case COMMAND_CARD_RESIST:
            case CRITICAL_CHANCE_RESIST:
            case CRITICAL_DAMAGE_BUFF:
            case CRITICAL_RATE_BUFF:
            case CRITICAL_STAR_GENERATION_BUFF:
            case CRITICAL_STAR_WEIGHT_BUFF:
            case DAMAGE_ADDITION_BUFF:
            case DAMAGE_REDUCTION_BUFF:
            case DEATH_CHANCE_BUFF:
            case DEATH_RESIST:
            case DEBUFF_CHANCE_BUFF:
            case DEBUFF_RESIST:
            case DEFENSE_BUFF:
            case DEF_NP_GENERATION_BUFF:
            case HEAL_EFFECTIVENESS_BUFF:
            case HEAL_GRANT_EFF_BUFF:
            case MAX_HP_BUFF:
            case NP_DAMAGE_BUFF:
            case NP_DAMAGE_BUFF_EFFECTIVENESS_UP:
            case NP_GENERATION_BUFF:
            case OVERCHARGE_BUFF:
            case PERCENT_ATTACK_BUFF:
            case PERCENT_DEFENSE_BUFF:
            case RECEIVED_BUFF_CHANCE_BUFF:
            case SKILL_EFFECTIVENESS_UP:
            case SPECIFIC_ATTACK_BUFF:
            case SPECIFIC_DEFENSE_BUFF:
            case TAUNT:
                return value < 0 || (value == 0 && addition < 0);
            case BURN:
            case BURN_EFFECTIVENESS_UP:
            case CHARM_RESIST_DOWN:
            case CURSE:
            case CURSE_EFFECTIVENESS_UP:
            case POISON:
            case POISON_EFFECTIVENESS_UP:
                return value > 0 || (value == 0 && addition > 0);
            case BURNING_LOVE:
            case CHARM:
            case CONFUSION:
            case NP_SEAL:
            case PERMANENT_SLEEP:
            case PIGIFY:
            case SKILL_SEAL:
            case SLEEP:
            case STUN:
            case TERROR:
                return true;
            case ON_FIELD_EFFECT:
                return activatedBuffBase.isNegativeBuffType();
            default:
                return false;
        }
    }

    public boolean isStackableBuffType() {
        switch (buffType) {
            case ATTACK_BUFF_DURATION_EXTEND:
            case CARD_TYPE_CHANGE:
            case CHARM:
            case DAMAGE_REFLECT:
            case DO_NOT_SHUFFLE_IN:
            case EVADE:
            case FACELESS_MOON:
            case GUTS:
            case HITS_DOUBLED_BUFF:
            case IGNORE_DEFENSE_BUFF:
            case IGNORE_INVINCIBLE:
            case INVINCIBLE:
            case NP_CARD_TYPE_CHANGE:
            case NP_DAMAGE_BUFF_EFFECTIVENESS_UP:
            case NP_SEAL:
            case PERMANENT_SLEEP:
            case PIGIFY:
            case SKILL_SEAL:
            case SLEEP:
            case SPECIAL_INVINCIBLE:
            case STUN:
            case SURE_HIT:
                return false;
            default:
                return true;
        }
    }

    public boolean isStackable() {
        return forceStackable || isStackableBuffType();
    }

    /*
     * ============================================================
     * toString
     * ============================================================
     */
    @Override
    public String toString() {
        final String miscString = miscString();
        final String baseString = getTranslation(BUFF_SECTION, buffType.getType()) + miscString;
        final NumberFormat numberFormat = NumberFormat.getPercentInstance();
        numberFormat.setMaximumFractionDigits(2);

        switch (buffType) {
            case ATTACK_BUFF:
            case BUFF_CHANCE_BUFF:
            case BUFF_REMOVAL_RESIST:
            case BURN_EFFECTIVENESS_UP:
            case CHARM_RESIST_DOWN:
            case COMMAND_CARD_BUFF:
            case COMMAND_CARD_RESIST:
            case CRITICAL_CHANCE_RESIST:
            case CRITICAL_DAMAGE_BUFF:
            case CRITICAL_RATE_BUFF:
            case CRITICAL_STAR_GENERATION_BUFF:
            case CRITICAL_STAR_WEIGHT_BUFF:
            case CURSE_EFFECTIVENESS_UP:
            case DEATH_CHANCE_BUFF:
            case DEATH_RESIST:
            case DEBUFF_CHANCE_BUFF:
            case DEBUFF_RESIST:
            case DEFENSE_BUFF:
            case DEF_NP_GENERATION_BUFF:
            case HEAL_EFFECTIVENESS_BUFF:
            case HEAL_GRANT_EFF_BUFF:
            case NP_DAMAGE_BUFF:
            case NP_DAMAGE_BUFF_EFFECTIVENESS_UP:
            case NP_GENERATION_BUFF:
            case PERCENT_ATTACK_BUFF:
            case PERCENT_DEFENSE_BUFF:
            case POISON_EFFECTIVENESS_UP:
            case RECEIVED_BUFF_CHANCE_BUFF:
            case SKILL_EFFECTIVENESS_UP:
            case SPECIFIC_ATTACK_BUFF:
            case SPECIFIC_DEFENSE_BUFF:
            case TAUNT:
                String valueString = ": " + numberFormat.format(value * effectiveness);
                if (variation != NO_VARIATION) {
                    valueString = valueString + " + " + numberFormat.format(addition * effectiveness) + " " + variation;
                }
                return baseString + valueString;
            case BURN:
            case CURSE:
            case DAMAGE_ADDITION_BUFF:
            case DAMAGE_REDUCTION_BUFF:
            case MAX_HP_BUFF:
            case OVERCHARGE_BUFF:
            case POISON:
                String intValueString = ": " + (int) (value * effectiveness);
                if (variation != NO_VARIATION) {
                    intValueString = intValueString + " + " + (int) (addition * effectiveness) + " " + variation;
                }
                return baseString + intValueString;
            case CARD_TYPE_CHANGE:
            case NP_CARD_TYPE_CHANGE:
                return baseString + ": " + getTranslation(COMMAND_CARD_TYPE_SECTION, commandCardType.name());
            case CLASS_ADVANTAGE_CHANGE_BUFF:
                String classAdvString = "";
                if (attackMode != CLASS_ADV_NO_CHANGE) {
                    classAdvString = classAdvString + " " +
                            getTranslation(BUFF_SECTION, "Attack Affinity") +
                            getTranslation(CLASS_ADV_SECTION, attackMode.name());
                    classAdvString = classAdvString + " " + attackAdvantage + " " + attackModeAffectedClasses.stream()
                            .map(fateClass -> getTranslation(CLASS_SECTION, fateClass.name()))
                            .collect(Collectors.joining(", "));
                }
                if (defenseMode != CLASS_ADV_NO_CHANGE) {
                    classAdvString = classAdvString + " " +
                            getTranslation(BUFF_SECTION, "Defense Affinity") +
                            getTranslation(CLASS_ADV_SECTION, defenseMode.name());
                    classAdvString = classAdvString + " " + defenseAdvantage + " " + defenseModeAffectedClasses.stream()
                            .map(fateClass -> getTranslation(CLASS_SECTION, fateClass.name()))
                            .collect(Collectors.joining(", "));
                }

                return classAdvString + miscString;
            case CONFUSION:
            case DEATH_EFFECT:
            case DELAYED_EFFECT:
            case END_OF_TURN_EFFECT:
            case ENTER_FIELD_EFFECT:
            case HP_BREAK_EFFECT:
            case LEAVE_FIELD_EFFECT:
            case POST_ATTACK_EFFECT:
            case POST_DEFENSE_EFFECT:
            case PRE_ATTACK_EFFECT:
            case PRE_DEFENSE_EFFECT:
            case START_OF_TURN_EFFECT:
            case TERROR:
            case TRIGGER_ON_GUTS_EFFECT:
                return baseString + ": " + effects.stream().map(Effect::toString).collect(Collectors.toList());
            case GRANT_STAGE_TRAIT:
            case GRANT_TRAIT:
            case PREVENT_DEATH_AGAINST_DOT:
            case REMOVE_STAGE_TRAIT:
            case REMOVE_TRAIT:
                return baseString + ": " + getTranslation(TRAIT_SECTION, stringValue);
            case BUFF_TYPE_CONVERSION:
                return baseString + ": " + getTranslation(BUFF_SECTION, stringValue);
            case GUTS:
                final String gutsString = isPercentageGuts ?
                    numberFormat.format(value * effectiveness) :
                    (int) (value * effectiveness) + "";
                return baseString + ": " + gutsString;
            case ON_FIELD_EFFECT:
                return baseString + ": " + getTranslation(TARGET_SECTION, target.name()) + " : " + activatedBuffBase.toString();
            default:
                return baseString;
        }
    }

    public String baseToString() {
        final String base = getTranslation(BUFF_SECTION, getClass().getSimpleName());
        return base + miscString();
    }

    public String durationString() {
        final List<String> duration = new ArrayList<>();
        if (!isPermanentTimeEffect()) {
            duration.add(activeTimes + getTranslation(BUFF_SECTION, "Times"));
        }
        if (!isPermanentTurnEffect()) {
            duration.add(activeTurns + getTranslation(BUFF_SECTION, "Turns"));
        }
        if (duration.isEmpty()) {
            duration.add(getTranslation(BUFF_SECTION, "Permanent"));
        }
        return "(" + String.join(" ", duration) + ")";
    }

    public String miscString() {
        String base = "";
        if (condition != ALWAYS) {
            base = base + " (" + condition + ")";
        }

        if (probability != 1) {
            final NumberFormat numberFormat = NumberFormat.getPercentInstance();
            numberFormat.setMaximumFractionDigits(2);
            base = base + " " + numberFormat.format(probability) + getTranslation(BUFF_SECTION, "Probability");
        }
        if (irremovable) {
            base = base + " " + getTranslation(BUFF_SECTION, "Irremovable");
        }
        if (forceStackable) {
            base = base + " " + getTranslation(BUFF_SECTION, "Force Stackable");
        }
        return base;
    }

    /*
     * ============================================================
     * MakeCopy
     * ============================================================
     */
    private Buff(final Buff other) {
        this.buffType = other.buffType;
        this.iconName = other.iconName;
        this.condition = other.condition;
        this.probability = other.probability;
        this.forceStackable = other.forceStackable;
        this.irremovable = other.irremovable;
        this.isPassive = other.isPassive;
        this.buffTraits = other.buffTraits;

        this.activeTurns = other.activeTurns;
        this.activeTimes = other.activeTimes;
        this.isApplied = other.isApplied;
        this.turnPassed = other.turnPassed;
        this.activatorHash = other.activatorHash;

        this.value = other.value;
        this.variation = other.variation;
        this.addition = other.addition;
        this.effectiveness = other.effectiveness;

        this.commandCardType = other.commandCardType;

        this.attackMode = other.attackMode;
        this.attackAdvantage = other.attackAdvantage;
        this.attackModeAffectedClasses = other.attackModeAffectedClasses;

        this.defenseMode = other.defenseMode;
        this.defenseAdvantage = other.defenseAdvantage;
        this.defenseModeAffectedClasses = other.defenseModeAffectedClasses;

        this.effects = other.effects;

        this.storedDamage = other.storedDamage;

        this.stringValue = other.stringValue;

        this.isPercentageGuts = other.isPercentageGuts;

        this.activatedBuffBase = other.activatedBuffBase;
        this.target = other.target;
        this.forceGrantBuff = other.forceGrantBuff;
        this.forceRemoveBuff = other.forceRemoveBuff;

        this.convertIconPath = other.convertIconPath;
    }

    public Buff makeCopy() {
        return new Buff(this);
    }
}
