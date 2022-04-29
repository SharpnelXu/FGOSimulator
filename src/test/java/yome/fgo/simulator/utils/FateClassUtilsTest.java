package yome.fgo.simulator.utils;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import yome.fgo.data.proto.FgoStorageData.CombatantData;
import yome.fgo.data.proto.FgoStorageData.FateClass;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.effects.buffs.ClassAdvantageChangeBuff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static yome.fgo.data.proto.FgoStorageData.ClassAdvantageChangeMode.CLASS_ADV_REMOVE_DISADV;
import static yome.fgo.data.proto.FgoStorageData.FateClass.ALTEREGO;
import static yome.fgo.data.proto.FgoStorageData.FateClass.ARCHER;
import static yome.fgo.data.proto.FgoStorageData.FateClass.ASSASSIN;
import static yome.fgo.data.proto.FgoStorageData.FateClass.AVENGER;
import static yome.fgo.data.proto.FgoStorageData.FateClass.BEAST_I;
import static yome.fgo.data.proto.FgoStorageData.FateClass.BEAST_II;
import static yome.fgo.data.proto.FgoStorageData.FateClass.BEAST_III_L;
import static yome.fgo.data.proto.FgoStorageData.FateClass.BEAST_III_R;
import static yome.fgo.data.proto.FgoStorageData.FateClass.BEAST_IV;
import static yome.fgo.data.proto.FgoStorageData.FateClass.BERSERKER;
import static yome.fgo.data.proto.FgoStorageData.FateClass.CASTER;
import static yome.fgo.data.proto.FgoStorageData.FateClass.FOREIGNER;
import static yome.fgo.data.proto.FgoStorageData.FateClass.LANCER;
import static yome.fgo.data.proto.FgoStorageData.FateClass.MOONCANCER;
import static yome.fgo.data.proto.FgoStorageData.FateClass.PRETENDER;
import static yome.fgo.data.proto.FgoStorageData.FateClass.RIDER;
import static yome.fgo.data.proto.FgoStorageData.FateClass.RULER;
import static yome.fgo.data.proto.FgoStorageData.FateClass.SABER;
import static yome.fgo.data.proto.FgoStorageData.FateClass.SHIELDER;
import static yome.fgo.simulator.utils.FateClassUtils.getClassAdvantage;
import static yome.fgo.simulator.utils.FateClassUtils.getClassAttackCorrection;
import static yome.fgo.simulator.utils.FateClassUtils.getClassCritStarCorrection;
import static yome.fgo.simulator.utils.FateClassUtils.getClassNpCorrection;

public class FateClassUtilsTest {
    public static final FateClass[] ALL_SERVANT_CLASSES = {SABER, ARCHER, LANCER, RIDER, CASTER, ASSASSIN, BERSERKER, RULER, AVENGER, ALTEREGO, MOONCANCER, FOREIGNER, PRETENDER, SHIELDER};
    public static final FateClass[] ALL_CLASSES = {SABER, ARCHER, LANCER, RIDER, CASTER, ASSASSIN, BERSERKER, RULER, AVENGER, ALTEREGO, MOONCANCER, FOREIGNER, PRETENDER, SHIELDER, BEAST_I, BEAST_II, BEAST_III_R, BEAST_III_L, BEAST_IV};
    public static final double[] CLASS_ATTACK_CORRECTION = {1.0, 0.95, 1.05, 1.0, 0.9, 0.9, 1.1, 1.1, 1.1, 1.0, 1.0, 1.0, 1.0, 1.0};
    public static final double[] CLASS_NP_CORRECTION = {1.0, 1.0, 1.0, 1.1, 1.2, 0.9, 0.8, 1.0, 1.0, 1.0, 1.2, 1.0, 1.0, 1.0};
    public static final double[] CLASS_CRIT_CORRECTION = {0, 0.05, -0.05, 0.1, 0, -0.1, 0, 0, -0.1, 0.05, 0, 0.2, -0.1, 0};
    public static final double[][] CLASS_ADVANTAGES = {
            // S,   A,   L,   R,   C,   A,   B,  Ru,  Av,  Al,  Mc,  Fo,   P,   S,  BI, BII, B3R, B3L, BIV
            {1.0, 0.5, 2.0, 1.0, 1.0, 1.0, 2.0, 0.5, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0}, // Saber
            {2.0, 1.0, 0.5, 1.0, 1.0, 1.0, 2.0, 0.5, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0}, // Archer
            {0.5, 2.0, 1.0, 1.0, 1.0, 1.0, 2.0, 0.5, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0}, // Lancer
            {1.0, 1.0, 1.0, 1.0, 2.0, 0.5, 2.0, 0.5, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 2.0, 1.0, 1.0, 1.0, 1.0}, // Rider
            {1.0, 1.0, 1.0, 0.5, 1.0, 2.0, 2.0, 0.5, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 2.0, 1.0, 1.0, 1.0, 1.0}, // Caster
            {1.0, 1.0, 1.0, 2.0, 0.5, 1.0, 2.0, 0.5, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 2.0, 1.0, 1.0, 1.0, 1.0}, // Assassin
            {1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 0.5, 1.5, 1.0, 1.5, 1.0, 1.0, 1.0, 1.0}, // Berserker
            {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 2.0, 1.0, 0.5, 1.0, 2.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0}, // Ruler
            {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 2.0, 2.0, 1.0, 1.0, 0.5, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0}, // Avenger
            {0.5, 0.5, 0.5, 1.5, 1.5, 1.5, 2.0, 1.0, 1.0, 1.0, 1.0, 2.0, 0.5, 1.0, 1.0, 1.0, 1.2, 1.2, 1.0}, // Alterego
            {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 2.0, 0.5, 2.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.2, 1.0, 1.0}, // Mooncancer
            {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 2.0, 1.0, 1.0, 0.5, 1.0, 2.0, 2.0, 1.0, 1.0, 1.0, 1.0, 1.2, 1.0}, // Foreigner
            {1.5, 1.5, 1.5, 0.5, 0.5, 0.5, 2.0, 1.0, 1.0, 2.0, 1.0, 0.5, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0}, // Pretender
            {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0}, // Shielder
            {2.0, 2.0, 2.0, 1.0, 1.0, 1.0, 2.0, 1.0, 0.5, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0}, // Beast I
            {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0}, // Beast II
            {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0}, // Beast III/R
            {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0}, // Beast III/L
            {1.0, 1.0, 1.0, 1.0, 0.5, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0}  // Beast IV
    };

    @Test
    public void testGetClassAttackCorrection() {
        for (int i = 0; i < ALL_SERVANT_CLASSES.length; i++) {
            assertEquals(CLASS_ATTACK_CORRECTION[i], getClassAttackCorrection(ALL_SERVANT_CLASSES[i]));
        }
    }

    @Test
    public void testGetClassNpCorrection() {
        for (int i = 0; i < ALL_SERVANT_CLASSES.length; i++) {
            assertEquals(CLASS_NP_CORRECTION[i], getClassNpCorrection(ALL_SERVANT_CLASSES[i]));
        }
    }

    @Test
    public void testGetClassCritStarCorrection() {
        for (int i = 0; i < ALL_SERVANT_CLASSES.length; i++) {
            assertEquals(CLASS_CRIT_CORRECTION[i], getClassCritStarCorrection(ALL_SERVANT_CLASSES[i]));
        }
    }

    @Test
    public void testGetClassAdvantages() {
        for (int i = 0; i < ALL_CLASSES.length; i++) {
            for (int j = 0; j < ALL_CLASSES.length; j++) {
                assertEquals(CLASS_ADVANTAGES[i][j], getClassAdvantage(ALL_CLASSES[i], ALL_CLASSES[j]));
            }
        }
    }

    @Test
    public void testGetClassAdvantage_classAdvantageChangeBuff() {
        final Combatant attacker = new Combatant("test", CombatantData.newBuilder().setFateClass(BERSERKER).build());
        final Combatant defender = new Combatant("test", CombatantData.newBuilder().setFateClass(FOREIGNER).build());

        final ClassAdvantageChangeBuff buff = ClassAdvantageChangeBuff.builder()
                .attackMode(CLASS_ADV_REMOVE_DISADV)
                .defenseMode(CLASS_ADV_REMOVE_DISADV)
                .defenseModeAffectedClasses(ImmutableList.of(BERSERKER))
                .build();

        attacker.addBuff(buff);

        assertEquals(1.0, getClassAdvantage(attacker, defender));
        assertEquals(2.0, getClassAdvantage(defender, attacker));
        assertEquals(1.0, getClassAdvantage(attacker, attacker));
    }
}
