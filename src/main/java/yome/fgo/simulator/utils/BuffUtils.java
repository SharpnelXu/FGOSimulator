package yome.fgo.simulator.utils;

import com.google.common.collect.ImmutableList;
import yome.fgo.data.proto.FgoStorageData.BuffTraits;
import yome.fgo.simulator.models.effects.buffs.Buff;
import yome.fgo.simulator.models.effects.buffs.BuffType;

import java.util.List;

import static yome.fgo.simulator.models.effects.buffs.BuffType.*;
import static yome.fgo.simulator.models.effects.buffs.BuffType.SKILL_SEAL;

public class BuffUtils {
    public static final List<BuffTraits> REGULAR_BUFF_TRAITS = ImmutableList.of(
            BuffTraits.ATTACKER_BUFF,
            BuffTraits.DEFENDER_BUFF,
            BuffTraits.POSITIVE_BUFF,
            BuffTraits.NEGATIVE_BUFF,
            BuffTraits.MENTAL_BUFF,
            BuffTraits.IMMOBILIZE_BUFF
    );

    public static boolean isImmobilizeOrSeal(final Buff buff) {
        final BuffType buffType = buff.getBuffType();
        return isImmobilizeDebuff(buffType) || buffType == SKILL_SEAL || buffType == NP_SEAL;
    }

    public static boolean shouldDecreaseNumTurnsActiveAtMyTurn(final Buff buff, final boolean isBuffExtended) {
        final BuffType buffType = buff.getBuffType();
        return (!isBuffExtended && buff.getBuffTraits().contains(BuffTraits.ATTACKER_BUFF.name())) ||
                buffType == END_OF_TURN_EFFECT ||
                isDamageOverTimeBuff(buffType);
    }

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

    public static boolean isImmobilizeDebuff(final BuffType buffType) {
        return switch (buffType) {
            case CHARM, PERMANENT_SLEEP, PIGIFY, SLEEP, STUN -> true;
            default -> false;
        };
    }

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
