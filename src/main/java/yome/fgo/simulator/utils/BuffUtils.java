package yome.fgo.simulator.utils;

import yome.fgo.simulator.models.effects.buffs.AttackerBuff;
import yome.fgo.simulator.models.effects.buffs.Buff;
import yome.fgo.simulator.models.effects.buffs.EndOfTurnDamage;
import yome.fgo.simulator.models.effects.buffs.EndOfTurnEffect;
import yome.fgo.simulator.models.effects.buffs.ImmobilizeDebuff;
import yome.fgo.simulator.models.effects.buffs.NpSeal;
import yome.fgo.simulator.models.effects.buffs.SkillSeal;

public class BuffUtils {
    public static boolean isImmobilizeOrSeal(final Buff buff) {
        return buff instanceof ImmobilizeDebuff || buff instanceof SkillSeal || buff instanceof NpSeal;
    }

    public static boolean shouldDecreaseNumTurnsActiveAtMyTurn(final Buff buff) {
        return buff instanceof AttackerBuff || buff instanceof EndOfTurnEffect || buff instanceof EndOfTurnDamage;
    }
}
