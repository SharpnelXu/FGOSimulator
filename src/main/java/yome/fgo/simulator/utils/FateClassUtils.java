package yome.fgo.simulator.utils;

import yome.fgo.data.proto.FgoStorageData.FateClass;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.effects.buffs.Buff;
import yome.fgo.simulator.models.effects.buffs.ClassAdvantageChangeBuff;

import static yome.fgo.data.proto.FgoStorageData.FateClass.CASTER;

public class FateClassUtils {
    public static int getClassMaxNpGauge(final FateClass fateClass) {
        switch (fateClass) {
            case ARCHER:
            case ASSASSIN:
            case ALTEREGO:
            case MOONCANCER:
                return 3;
            case SABER:
            case LANCER:
            case RULER:
            case PRETENDER:
            case SHIELDER:
                return 4;
            default:
                return 5;
        }
    }

    public static double getClassAttackCorrection(final FateClass fateClass) {
        switch (fateClass) {
            case LANCER:
                return 1.05;
            case ARCHER:
                return 0.95;
            case BERSERKER:
            case RULER:
            case AVENGER:
                return 1.1;
            case CASTER:
            case ASSASSIN:
                return 0.9;
            default:
                return 1;
        }
    }
    public static double getClassNpCorrection(final FateClass fateClass) {
        switch (fateClass) {
            case CASTER:
            case MOONCANCER:
                return 1.2;
            case RIDER:
                return 1.1;
            case ASSASSIN:
                return 0.9;
            case BERSERKER:
                return 0.8;
            default:
                return 1;
        }
    }
    public static double getClassCritStarCorrection(final FateClass fateClass) {
        switch (fateClass) {
            case ARCHER:
            case ALTEREGO:
                return 0.05;
            case LANCER:
                return -0.05;
            case RIDER:
                return 0.1;
            case ASSASSIN:
            case AVENGER:
            case PRETENDER:
                return -0.1;
            case FOREIGNER:
                return 0.2;
            default:
                return 0;
        }
    }

    public static double getClassAdvantage(final Combatant attacker, final Combatant defender) {
        final FateClass attackerClass = attacker.getFateClass();
        final FateClass defenderClass = defender.getFateClass();

        double baseRate = getClassAdvantage(attackerClass, defenderClass);

        for (final Buff buff : attacker.getBuffs()) {
            if (buff instanceof ClassAdvantageChangeBuff) {
                baseRate = ((ClassAdvantageChangeBuff) buff).asAttacker(baseRate, defenderClass);
            }
        }

        for (final Buff buff : defender.getBuffs()) {
            if (buff instanceof ClassAdvantageChangeBuff) {
                baseRate = ((ClassAdvantageChangeBuff) buff).asDefender(baseRate, attackerClass);
            }
        }

        return baseRate;
    }

    public static double getClassAdvantage(final FateClass attackerClass, final FateClass defenderClass) {
        switch (attackerClass) {
            case SABER:
                switch (defenderClass) {
                    case ARCHER:
                    case RULER:
                        return 0.5;
                    case LANCER:
                    case BERSERKER:
                        return 2.0;
                    default:
                        return 1.0;
                }
            case ARCHER:
                switch (defenderClass) {
                    case LANCER:
                    case RULER:
                        return 0.5;
                    case SABER:
                    case BERSERKER:
                        return 2.0;
                    default:
                        return 1.0;
                }
            case LANCER:
                switch (defenderClass) {
                    case SABER:
                    case RULER:
                        return 0.5;
                    case ARCHER:
                    case BERSERKER:
                        return 2.0;
                    default:
                        return 1.0;
                }
            case RIDER:
                switch (defenderClass) {
                    case ASSASSIN:
                    case RULER:
                        return 0.5;
                    case CASTER:
                    case BERSERKER:
                    case BEAST_I:
                        return 2.0;
                    default:
                        return 1.0;
                }
            case CASTER:
                switch (defenderClass) {
                    case RIDER:
                    case RULER:
                        return 0.5;
                    case ASSASSIN:
                    case BERSERKER:
                    case BEAST_I:
                        return 2.0;
                    default:
                        return 1.0;
                }
            case ASSASSIN:
                switch (defenderClass) {
                    case CASTER:
                    case RULER:
                        return 0.5;
                    case RIDER:
                    case BERSERKER:
                    case BEAST_I:
                        return 2.0;
                    default:
                        return 1.0;
                }
            case BERSERKER:
                switch (defenderClass) {
                    case FOREIGNER:
                        return 0.5;
                    case SHIELDER:
                    case BEAST_II:
                    case BEAST_III_R:
                    case BEAST_III_L:
                    case BEAST_IV:
                        return 1.0;
                    default:
                        return 1.5;
                }
            case RULER:
                switch (defenderClass) {
                    case AVENGER:
                        return 0.5;
                    case BERSERKER:
                    case MOONCANCER:
                        return 2.0;
                    default:
                        return 1.0;
                }
            case AVENGER:
                switch (defenderClass) {
                    case MOONCANCER:
                        return 0.5;
                    case BERSERKER:
                    case RULER:
                        return 2.0;
                    default:
                        return 1.0;
                }
            case MOONCANCER:
                switch (defenderClass) {
                    case RULER:
                        return 0.5;
                    case BERSERKER:
                    case AVENGER:
                        return 2.0;
                    case BEAST_III_R:
                        return 1.2;
                    default:
                        return 1.0;
                }
            case ALTEREGO:
                switch (defenderClass) {
                    case SABER:
                    case ARCHER:
                    case LANCER:
                    case PRETENDER:
                        return 0.5;
                    case RIDER:
                    case CASTER:
                    case ASSASSIN:
                        return 1.5;
                    case BERSERKER:
                    case FOREIGNER:
                        return 2.0;
                    case BEAST_III_R:
                    case BEAST_III_L:
                        return 1.2;
                    default:
                        return 1.0;
                }
            case FOREIGNER:
                switch (defenderClass) {
                    case ALTEREGO:
                        return 0.5;
                    case BEAST_III_L:
                        return 1.2;
                    case BERSERKER:
                    case FOREIGNER:
                    case PRETENDER:
                        return 2.0;
                    default:
                        return 1.0;
                }
            case PRETENDER:
                switch (defenderClass) {
                    case RIDER:
                    case CASTER:
                    case ASSASSIN:
                    case FOREIGNER:
                        return 0.5;
                    case SABER:
                    case ARCHER:
                    case LANCER:
                        return 1.5;
                    case BERSERKER:
                    case ALTEREGO:
                        return 2.0;
                    default:
                        return 1.0;
                }
            case BEAST_I:
                switch (defenderClass) {
                    case AVENGER:
                        return 0.5;
                    case SABER:
                    case ARCHER:
                    case LANCER:
                    case BERSERKER:
                        return 2.0;
                    default:
                        return 1.0;
                }
            case BEAST_IV:
                if (defenderClass == CASTER) {
                    return 0.5;
                }
                return 1.0;
            default:
                return 1.0;
        }
    }
}
