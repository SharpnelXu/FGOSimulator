package yome.fgo.simulator.utils;

import org.junit.jupiter.api.Test;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.ARTS;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.BUSTER;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.EXTRA;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.QUICK;
import static yome.fgo.simulator.utils.CommandCardTypeUtils.busterChainDamageAddition;
import static yome.fgo.simulator.utils.CommandCardTypeUtils.extraCardBuff;
import static yome.fgo.simulator.utils.CommandCardTypeUtils.getCommandCardCritStarCorrection;
import static yome.fgo.simulator.utils.CommandCardTypeUtils.getCommandCardDamageCorrection;
import static yome.fgo.simulator.utils.CommandCardTypeUtils.getCommandCardNpCorrection;
import static yome.fgo.simulator.utils.CommandCardTypeUtils.isBusterChain;
import static yome.fgo.simulator.utils.CommandCardTypeUtils.isValidCardPos;
import static yome.fgo.simulator.utils.CommandCardTypeUtils.modifierCap;

public class CommandCardTypeUtilsTest {
    public static final CommandCardType[] COMMAND_CARD_TYPES = {BUSTER, ARTS, QUICK};
    public static final double[][] EXPECTED_DAMAGE_CORRECTION = {
            {1.5, 1.8, 2.1},
            {1.0, 1.2, 1.4},
            {0.8, 0.96, 1.12}
    };
    public static final double[][] EXPECTED_NP_CORRECTION = {
            {0, 0, 0},
            {3.0, 4.5, 6.0},
            {1.0, 1.5, 2.0}
    };
    public static final double[][] EXPECTED_CRIT_STAR_CORRECTION = {
            {0.1, 0.15, 0.2},
            {0, 0, 0},
            {0.8, 1.3, 1.8}
    };

    @Test
    public void testIsValidCardPos() {
        assertTrue(isValidCardPos(BUSTER, 0));
        assertTrue(isValidCardPos(ARTS, 1));
        assertTrue(isValidCardPos(QUICK, 2));
        assertTrue(isValidCardPos(EXTRA, 3));

        assertFalse(isValidCardPos(BUSTER, -1));
        assertFalse(isValidCardPos(EXTRA, 1));
        assertFalse(isValidCardPos(BUSTER, 3));
        assertFalse(isValidCardPos(ARTS, 4));
        assertFalse(isValidCardPos(EXTRA, 5));
    }

    @Test
    public void testGetCommandCardDamageCorrection() {
        assertThrows(IllegalArgumentException.class, () -> getCommandCardDamageCorrection(BUSTER, -1));

        for (int i = 0; i < COMMAND_CARD_TYPES.length; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(EXPECTED_DAMAGE_CORRECTION[i][j], getCommandCardDamageCorrection(COMMAND_CARD_TYPES[i], j));
            }
        }
        assertEquals(1, getCommandCardDamageCorrection(EXTRA, 3));
    }

    @Test
    public void testGetCommandCardNpCorrection() {
        assertThrows(IllegalArgumentException.class, () -> getCommandCardNpCorrection(BUSTER, -1));

        for (int i = 0; i < COMMAND_CARD_TYPES.length; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(EXPECTED_NP_CORRECTION[i][j], getCommandCardNpCorrection(COMMAND_CARD_TYPES[i], j));
            }
        }
        assertEquals(1, getCommandCardNpCorrection(EXTRA, 3));
    }

    @Test
    public void testGetCommandCardCritStarCorrection() {
        assertThrows(IllegalArgumentException.class, () -> getCommandCardCritStarCorrection(BUSTER, -1));

        for (int i = 0; i < COMMAND_CARD_TYPES.length; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(EXPECTED_CRIT_STAR_CORRECTION[i][j], getCommandCardCritStarCorrection(COMMAND_CARD_TYPES[i], j));
            }
        }
        assertEquals(1, getCommandCardCritStarCorrection(EXTRA, 3));
    }

    @Test
    public void testModifierCap() {
        assertThrows(IllegalArgumentException.class, () -> modifierCap(100, 0, 200));

        assertEquals(0.5, modifierCap(0.5, 1.0, 0.0));
        assertEquals(1.0, modifierCap(3, 1.0, -1.0));
        assertEquals(-1.0, modifierCap(-4, 1.0, -1.0));
    }

    @Test
    public void testIsBusterChain() {
        assertTrue(isBusterChain(true, BUSTER));
        assertFalse(isBusterChain(true, QUICK));
    }

    @Test
    public void testBusterChainDamageAddition() {
        assertEquals(2000, busterChainDamageAddition(10000, BUSTER, true, BUSTER), 0.000001);
        assertEquals(0, busterChainDamageAddition(10000, EXTRA, true, BUSTER));
    }

    @Test
    public void testExtraCardBuff() {
        assertEquals(3.5, extraCardBuff(EXTRA, true, BUSTER));
        assertEquals(2.0, extraCardBuff(EXTRA, true, ARTS));
        assertEquals(2.0, extraCardBuff(EXTRA, true, QUICK));
        assertEquals(1.0, extraCardBuff(BUSTER, true, BUSTER));
    }
}
