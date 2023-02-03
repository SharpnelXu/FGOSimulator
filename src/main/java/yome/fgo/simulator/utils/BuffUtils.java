package yome.fgo.simulator.utils;

import com.google.common.collect.ImmutableList;
import yome.fgo.data.proto.FgoStorageData.BuffTraits;
import yome.fgo.simulator.models.effects.buffs.Buff;
import yome.fgo.simulator.models.effects.buffs.DamageOverTime;
import yome.fgo.simulator.models.effects.buffs.EndOfTurnEffect;
import yome.fgo.simulator.models.effects.buffs.ImmobilizeDebuff;
import yome.fgo.simulator.models.effects.buffs.NpSeal;
import yome.fgo.simulator.models.effects.buffs.SkillSeal;

import java.util.List;

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
        return buff instanceof ImmobilizeDebuff || buff instanceof SkillSeal || buff instanceof NpSeal;
    }

    public static boolean shouldDecreaseNumTurnsActiveAtMyTurn(final Buff buff, final boolean isBuffExtended) {
        return (!isBuffExtended && buff.getBuffTraits()
                .contains(BuffTraits.ATTACKER_BUFF.name())) || buff instanceof EndOfTurnEffect || buff instanceof DamageOverTime;
    }
    }
}
