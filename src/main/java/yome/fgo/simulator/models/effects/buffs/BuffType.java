package yome.fgo.simulator.models.effects.buffs;


import com.google.common.collect.ImmutableSet;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static yome.fgo.simulator.models.effects.buffs.BuffFields.BUFF_FIELD_BUFF_TYPE;
import static yome.fgo.simulator.models.effects.buffs.BuffFields.BUFF_FIELD_CARD_TYPE;
import static yome.fgo.simulator.models.effects.buffs.BuffFields.BUFF_FIELD_CLASS_ADV;
import static yome.fgo.simulator.models.effects.buffs.BuffFields.BUFF_FIELD_DOUBLE_VALUE;
import static yome.fgo.simulator.models.effects.buffs.BuffFields.BUFF_FIELD_EFFECTS;
import static yome.fgo.simulator.models.effects.buffs.BuffFields.BUFF_FIELD_INT_VALUE;
import static yome.fgo.simulator.models.effects.buffs.BuffFields.BUFF_FIELD_NO_VARIATION;
import static yome.fgo.simulator.models.effects.buffs.BuffFields.BUFF_FIELD_ON_FIELD;
import static yome.fgo.simulator.models.effects.buffs.BuffFields.BUFF_FIELD_PERCENT_OPTION;
import static yome.fgo.simulator.models.effects.buffs.BuffFields.BUFF_FIELD_STRING_VALUE;

/**
 * Display order of GUI is the same as Enum order.
 */
public enum BuffType {
    ATTACK_BUFF("AttackBuff", ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE)),
    DEFENSE_BUFF("DefenseBuff", ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE)),
    COMMAND_CARD_BUFF("CommandCardBuff", ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE)),
    COMMAND_CARD_RESIST("CommandCardResist", ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE)),
    DAMAGE_ADDITION_BUFF("DamageAdditionBuff", ImmutableSet.of(BUFF_FIELD_INT_VALUE)),
    DAMAGE_REDUCTION_BUFF("DamageReductionBuff", ImmutableSet.of(BUFF_FIELD_INT_VALUE)),
    CRITICAL_DAMAGE_BUFF("CriticalDamageBuff", ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE)),
    NP_DAMAGE_BUFF("NpDamageBuff", ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE)),
    NP_DAMAGE_BUFF_EFFECTIVENESS_UP("NpDamageBuffEffectivenessUp", ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE)),
    PERCENT_ATTACK_BUFF("PercentAttackBuff", ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE)),
    PERCENT_DEFENSE_BUFF("PercentDefenseBuff", ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE)),
    SPECIFIC_ATTACK_BUFF("SpecificAttackBuff", ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE)),
    SPECIFIC_DEFENSE_BUFF("SpecificDefenseBuff", ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE)),

    IGNORE_DEFENSE_BUFF("IgnoreDefenseBuff", ImmutableSet.of()),
    EVADE("Evade", ImmutableSet.of()),
    SURE_HIT("SureHit", ImmutableSet.of()),
    INVINCIBLE("Invincible", ImmutableSet.of()),
    IGNORE_INVINCIBLE("IgnoreInvincible", ImmutableSet.of()),
    SPECIAL_INVINCIBLE("SpecialInvincible", ImmutableSet.of()),
    HITS_DOUBLED_BUFF("HitsDoubledBuff", ImmutableSet.of()),

    CRITICAL_CHANCE_RESIST("CriticalChanceResist", ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE)),
    CRITICAL_RATE_BUFF("CriticalRateBuff", ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE)),
    CRITICAL_STAR_GENERATION_BUFF("CriticalStarGenerationBuff", ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE)),
    CRITICAL_STAR_WEIGHT_BUFF("CriticalStarWeightBuff", ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE)),
    NP_GENERATION_BUFF("NpGenerationBuff", ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE)),
    DEF_NP_GENERATION_BUFF("DefNpGenerationBuff", ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE)),
    OVERCHARGE_BUFF("OverchargeBuff", ImmutableSet.of(BUFF_FIELD_INT_VALUE, BUFF_FIELD_NO_VARIATION)),
    TAUNT("Taunt", ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE)),

    BUFF_CHANCE_BUFF("BuffChanceBuff", ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE)),
    RECEIVED_BUFF_CHANCE_BUFF("ReceivedBuffChanceBuff", ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE)),
    DEBUFF_CHANCE_BUFF("DebuffChanceBuff", ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE)),
    DEBUFF_RESIST("DebuffResist", ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE)),
    CHARM_RESIST_DOWN("CharmResistDown", ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE)), // Converted to DEBUFF_RESIST
    BUFF_REMOVAL_RESIST("BuffRemovalResist", ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE)),
    DEATH_CHANCE_BUFF("DeathChanceBuff", ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE)),
    DEATH_RESIST("DeathResist", ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE)),

    GUTS("Guts", ImmutableSet.of(BUFF_FIELD_PERCENT_OPTION, BUFF_FIELD_NO_VARIATION)),
    TRIGGER_ON_GUTS_EFFECT("TriggerOnGutsEffect", ImmutableSet.of(BUFF_FIELD_EFFECTS)),
    HP_BREAK_EFFECT("HPBreakEffect", ImmutableSet.of(BUFF_FIELD_EFFECTS)),

    DELAYED_EFFECT("DelayedEffect", ImmutableSet.of(BUFF_FIELD_EFFECTS)),
    START_OF_TURN_EFFECT("StartOfTurnEffect", ImmutableSet.of(BUFF_FIELD_EFFECTS)),
    END_OF_TURN_EFFECT("EndOfTurnEffect", ImmutableSet.of(BUFF_FIELD_EFFECTS)),
    ENTER_FIELD_EFFECT("EnterFieldEffect", ImmutableSet.of(BUFF_FIELD_EFFECTS)),
    LEAVE_FIELD_EFFECT("LeaveFieldEffect", ImmutableSet.of(BUFF_FIELD_EFFECTS)),
    DEATH_EFFECT("DeathEffect", ImmutableSet.of(BUFF_FIELD_EFFECTS)),
    ON_FIELD_EFFECT("OnFieldEffect", ImmutableSet.of(BUFF_FIELD_ON_FIELD)),
    PRE_ATTACK_EFFECT("PreAttackEffect", ImmutableSet.of(BUFF_FIELD_EFFECTS)),
    PRE_DEFENSE_EFFECT("PreDefenseEffect", ImmutableSet.of(BUFF_FIELD_EFFECTS)),
    POST_ATTACK_EFFECT("PostAttackEffect", ImmutableSet.of(BUFF_FIELD_EFFECTS)),
    POST_DEFENSE_EFFECT("PostDefenseEffect", ImmutableSet.of(BUFF_FIELD_EFFECTS)),

    GRANT_STAGE_TRAIT("GrantStageTrait", ImmutableSet.of(BUFF_FIELD_STRING_VALUE)),
    REMOVE_STAGE_TRAIT("RemoveStageTrait", ImmutableSet.of(BUFF_FIELD_STRING_VALUE)),
    GRANT_TRAIT("GrantTrait", ImmutableSet.of(BUFF_FIELD_STRING_VALUE)),
    REMOVE_TRAIT("RemoveTrait", ImmutableSet.of(BUFF_FIELD_STRING_VALUE)),
    BLESSED_BY_KUR("BlessedByKur", ImmutableSet.of()),
    BURNING_LOVE("BurningLove", ImmutableSet.of()),
    VENGEANCE("Vengeance", ImmutableSet.of()),

    MAX_HP_BUFF("MaxHpBuff", ImmutableSet.of(BUFF_FIELD_INT_VALUE, BUFF_FIELD_NO_VARIATION)),
    HEAL_EFFECTIVENESS_BUFF("HealEffectivenessBuff", ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE)),
    HEAL_GRANT_EFF_BUFF("HealGrantEffBuff", ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE)),
    BURN("Burn", ImmutableSet.of(BUFF_FIELD_INT_VALUE)),
    BURN_EFFECTIVENESS_UP("BurnEffectivenessUp", ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE)),
    CURSE("Curse", ImmutableSet.of(BUFF_FIELD_INT_VALUE)),
    CURSE_EFFECTIVENESS_UP("CurseEffectivenessUp", ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE)),
    POISON("Poison", ImmutableSet.of(BUFF_FIELD_INT_VALUE)),
    POISON_EFFECTIVENESS_UP("PoisonEffectivenessUp", ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE)),
    PREVENT_DEATH_AGAINST_DOT("PreventDeathAgainstDoT", ImmutableSet.of(BUFF_FIELD_STRING_VALUE)),

    CLASS_ADVANTAGE_CHANGE_BUFF("ClassAdvantageChangeBuff", ImmutableSet.of(BUFF_FIELD_CLASS_ADV)),
    CARD_TYPE_CHANGE("CardTypeChange", ImmutableSet.of(BUFF_FIELD_CARD_TYPE)),
    NP_CARD_TYPE_CHANGE("NpCardTypeChange", ImmutableSet.of(BUFF_FIELD_CARD_TYPE)),

    STUN("Stun", ImmutableSet.of()),
    PIGIFY("Pigify", ImmutableSet.of()),
    CHARM("Charm", ImmutableSet.of()),
    CONFUSION("Confusion", ImmutableSet.of()), // Converted to END_OF_TURN_EFFECT
    TERROR("Terror", ImmutableSet.of()), // Converted to END_OF_TURN_EFFECT
    SLEEP("Sleep", ImmutableSet.of()),
    PERMANENT_SLEEP("PermanentSleep", ImmutableSet.of()),
    NP_SEAL("NpSeal", ImmutableSet.of()),
    SKILL_SEAL("SkillSeal", ImmutableSet.of()),

    ATTACK_BUFF_DURATION_EXTEND("AttackBuffDurationExtend", ImmutableSet.of()),
    DAMAGE_REFLECT("DamageReflect", ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE)),
    DO_NOT_SHUFFLE_IN("DoNotShuffleIn", ImmutableSet.of()),
    FACELESS_MOON("FacelessMoon", ImmutableSet.of()),
    SKILL_RANK_UP("SkillRankUp", ImmutableSet.of()),
    SKILL_EFFECTIVENESS_UP("SkillEffectivenessUp", ImmutableSet.of(BUFF_FIELD_DOUBLE_VALUE)),
    BUFF_TYPE_CONVERSION("BuffTypeConversion", ImmutableSet.of(BUFF_FIELD_BUFF_TYPE));


    private final String type;
    private final Set<BuffFields> requiredFields;

    BuffType(final String type, final Set<BuffFields> requiredFields) {
        this.type = type;
        this.requiredFields = requiredFields;
    }

    private static final Map<String, BuffType> BY_TYPE_STRING = new LinkedHashMap<>();

    static {
        for (final BuffType buffType : values()) {
            BY_TYPE_STRING.put(buffType.type, buffType);
        }
    }

    public String getType() {
        return type;
    }

    public Set<BuffFields> getRequiredFields() {
        return requiredFields;
    }

    public static BuffType ofType(final String type) {
        if (!BY_TYPE_STRING.containsKey(type)) {
            throw new IllegalArgumentException("Unknown BuffType: " + type);
        }

        return BY_TYPE_STRING.get(type);
    }

    public static Set<String> getOrder() {
        return BY_TYPE_STRING.keySet();
    }
}
