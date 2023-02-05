package yome.fgo.simulator.models.combatants;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.conditions.BuffTypeEquals;
import yome.fgo.simulator.models.effects.buffs.Buff;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static yome.fgo.data.proto.FgoStorageData.BuffTraits.IMMOBILIZE_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.CHARM;
import static yome.fgo.simulator.models.effects.buffs.BuffType.COMMAND_CARD_BUFF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.COMMAND_CARD_RESIST;
import static yome.fgo.simulator.models.effects.buffs.BuffType.DEBUFF_RESIST;
import static yome.fgo.simulator.models.effects.buffs.BuffType.STUN;

public class CombatantTest {
    @Test
    public void testCombatant_constructor() {
        assertThrows(IllegalArgumentException.class, () -> new Combatant("", ImmutableList.of()));
        assertThrows(IllegalArgumentException.class, () -> new Combatant("", ImmutableList.of(50, 0)));
        assertThrows(IllegalArgumentException.class, () -> new Combatant("", ImmutableList.of(50, -100)));

        final Combatant test = new Combatant("test", ImmutableList.of(50, 100, 1000));
        assertEquals(50, test.getCurrentHp());
    }

    @Test
    public void testApplyBuff() {
        final Simulation simulation = new Simulation();

        final Combatant combatant = new Combatant();

        combatant.addBuff(Buff.builder()
                                  .buffType(COMMAND_CARD_BUFF)
                                  .value(0.3)
                                  .build());
        combatant.addBuff(Buff.builder()
                                  .buffType(COMMAND_CARD_BUFF)
                                  .value(0.5)
                                  .build());

        combatant.addBuff(Buff.builder()
                                  .buffType(COMMAND_CARD_RESIST)
                                  .value(0.7)
                                  .build());
        combatant.addBuff(Buff.builder()
                                  .buffType(COMMAND_CARD_RESIST)
                                  .value(0.9)
                                  .build());
        combatant.addBuff(Buff.builder()
                                  .buffType(COMMAND_CARD_RESIST)
                                  .value(1.1)
                                  .build());

        final double totalBuff = combatant.applyValuedBuff(simulation, COMMAND_CARD_BUFF);
        assertEquals(0.8, totalBuff, 0.0000001);
    }

    @Test
    public void testApplyBuff_subClassing() {
        final Simulation simulation = new Simulation();

        final Combatant combatant = new Combatant();

        combatant.addBuff(Buff.builder().buffType(DEBUFF_RESIST).value(0.3).build());
        combatant.addBuff(Buff.builder().buffType(DEBUFF_RESIST).value(0.5).build());

        combatant.addBuff(Buff.builder()
                                  .buffType(DEBUFF_RESIST)
                                  .condition(new BuffTypeEquals(CHARM))
                                  .value(-0.7)
                                  .build());
        combatant.addBuff(Buff.builder()
                                  .buffType(DEBUFF_RESIST)
                                  .condition(new BuffTypeEquals(CHARM))
                                  .value(-0.9)
                                  .build());
        combatant.addBuff(Buff.builder()
                                  .buffType(DEBUFF_RESIST)
                                  .condition(new BuffTypeEquals(CHARM))
                                  .value(-1.1)
                                  .build());

        simulation.setCurrentBuff(Buff.builder().buffType(STUN).build());
        final double totalBuff = combatant.applyValuedBuff(simulation, DEBUFF_RESIST);
        assertEquals(0.8, totalBuff, 0.0000001);

        simulation.setCurrentBuff(Buff.builder().buffType(CHARM).build());
        final double totalBuff2 = combatant.applyValuedBuff(simulation, DEBUFF_RESIST);
        assertEquals(-1.9, totalBuff2, 0.0000001);
    }

    @Test
    public void testIsImmobilized() {
        final Combatant combatant = new Combatant();
        assertFalse(combatant.isImmobilized());
        combatant.addBuff(Buff.builder().buffType(STUN).buffTraits(Collections.singletonList(IMMOBILIZE_BUFF.name())).build());
        assertTrue(combatant.isImmobilized());

        combatant.getBuffs().clear();
        assertFalse(combatant.isImmobilized());
        combatant.addBuff(Buff.builder().buffType(CHARM).buffTraits(Collections.singletonList(IMMOBILIZE_BUFF.name())).build());
        assertTrue(combatant.isImmobilized());
    }
}
