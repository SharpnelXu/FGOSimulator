package yome.fgo.simulator.utils;

import com.google.common.collect.ImmutableList;
import yome.fgo.data.proto.FgoStorageData.BuffTraits;
import yome.fgo.simulator.models.effects.buffs.Buff;
import yome.fgo.simulator.models.effects.buffs.BuffType;

import java.util.List;

import static yome.fgo.data.proto.FgoStorageData.BuffTraits.ATTACKER_BUFF;
import static yome.fgo.data.proto.FgoStorageData.BuffTraits.DEFENDER_BUFF;
import static yome.fgo.data.proto.FgoStorageData.BuffTraits.IMMOBILIZE_BUFF;
import static yome.fgo.data.proto.FgoStorageData.BuffTraits.MENTAL_BUFF;
import static yome.fgo.data.proto.FgoStorageData.BuffTraits.NEGATIVE_BUFF;
import static yome.fgo.data.proto.FgoStorageData.BuffTraits.POSITIVE_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.END_OF_TURN_EFFECT;

public class BuffUtils {
    public static final List<BuffTraits> REGULAR_BUFF_TRAITS = ImmutableList.of(
            ATTACKER_BUFF,
            DEFENDER_BUFF,
            POSITIVE_BUFF,
            NEGATIVE_BUFF,
            MENTAL_BUFF,
            IMMOBILIZE_BUFF
    );
    public static final String SEAL_TRAIT = "SEAL";
    public static final String DAMAGE_OVER_TIME = "DAMAGE_OVER_TIME";

    public static boolean isImmobilizeOrSeal(final Buff buff) {
        return buff.getBuffTraits().stream()
                .anyMatch(trait -> SEAL_TRAIT.equals(trait) || IMMOBILIZE_BUFF.name().equals(trait));
    }

    public static boolean isImmobilizeDebuff(final Buff buff) {
        return buff.getBuffTraits().contains(IMMOBILIZE_BUFF.name());
    }

    public static boolean shouldDecreaseNumTurnsActiveAtMyTurn(final Buff buff, final boolean isBuffExtended) {
        final BuffType buffType = buff.getBuffType();
        return (!isBuffExtended && buff.getBuffTraits().contains(ATTACKER_BUFF.name())) ||
                buffType == END_OF_TURN_EFFECT ||
                buff.getBuffTraits().contains(DAMAGE_OVER_TIME);
    }

    /*
     * ===========================================================================================
     * The following methods should only be used by BuffFactory
     * ===========================================================================================
     */
    public static boolean isAttackerBuff(final BuffType buffType) {
        switch (buffType) {
            case ATTACK_BUFF:
            case COMMAND_CARD_BUFF:
            case CRITICAL_DAMAGE_BUFF:
            case CRITICAL_RATE_BUFF:
            case DAMAGE_ADDITION_BUFF:
            case HITS_DOUBLED_BUFF:
            case IGNORE_DEFENSE_BUFF:
            case IGNORE_INVINCIBLE:
            case NP_DAMAGE_BUFF:
            case NP_DAMAGE_BUFF_EFFECTIVENESS_UP:
            case PERCENT_ATTACK_BUFF:
            case POST_ATTACK_EFFECT:
            case PRE_ATTACK_EFFECT:
            case SPECIFIC_ATTACK_BUFF:
            case SURE_HIT:
                return true;
            default:
                return false;
        }
    }

    public static boolean isDefenderBuff(final BuffType buffType) {
        switch (buffType) {
            case COMMAND_CARD_RESIST:
            case CRITICAL_CHANCE_RESIST:
            case DAMAGE_REDUCTION_BUFF:
            case DEATH_RESIST:
            case DEFENSE_BUFF:
            case EVADE:
            case INVINCIBLE:
            case PERCENT_DEFENSE_BUFF:
            case POST_DEFENSE_EFFECT:
            case PRE_DEFENSE_EFFECT:
            case SPECIAL_INVINCIBLE:
            case SPECIFIC_DEFENSE_BUFF:
                return true;
            default:
                return false;
        }
    }

    public static boolean isSeal(final BuffType buffType) {
        return switch (buffType) {
            case NP_SEAL, SKILL_SEAL -> true;
            default -> false;
        };
    }

    public static boolean isImmobilizeDebuff(final BuffType buffType) {
        return switch (buffType) {
            case CHARM, PERMANENT_SLEEP, PIGIFY, SLEEP, STUN -> true;
            default -> false;
        };
    }

    /**
     * Warning: since CHARM_RESIST_DOWN is converted to DEBUFF_RESIST, checking if a buff is a mental buff during
     * simulation should be by checking trait.
     */
    public static boolean isMentalDebuff(final BuffType buffType) {
        return switch (buffType) {
            case CHARM, CHARM_RESIST_DOWN, CONFUSION, PERMANENT_SLEEP, SLEEP, TERROR -> true;
            default -> false;
        };
    }

    public static boolean isDamageOverTimeBuff(final BuffType buffType) {
        return switch (buffType) {
            case BURN, CURSE, POISON, BURN_EFFECTIVENESS_UP, CURSE_EFFECTIVENESS_UP, POISON_EFFECTIVENESS_UP -> true;
            default -> false;
        };
    }
}
