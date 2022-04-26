package yome.fgo.simulator.models.combatants;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.effects.buffs.BuffSpecificAttackBuff;
import yome.fgo.simulator.models.effects.buffs.BurningLove;
import yome.fgo.simulator.models.effects.buffs.Charm;
import yome.fgo.simulator.models.effects.buffs.CommandCardBuff;
import yome.fgo.simulator.models.effects.buffs.CommandCardResist;
import yome.fgo.simulator.models.effects.buffs.SpecificAttackBuff;
import yome.fgo.simulator.models.effects.buffs.Stun;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        combatant.addBuff(CommandCardBuff.builder()
                                .value(0.3)
                                .build());
        combatant.addBuff(CommandCardBuff.builder()
                                .value(0.5)
                                .build());

        combatant.addBuff(CommandCardResist.builder()
                                .value(0.7)
                                .build());
        combatant.addBuff(CommandCardResist.builder()
                                .value(0.9)
                                .build());
        combatant.addBuff(CommandCardResist.builder()
                                .value(1.1)
                                .build());

        final double totalBuff = combatant.applyBuff(simulation, CommandCardBuff.class);
        assertEquals(0.8, totalBuff, 0.0000001);
    }
    @Test
    public void testApplyBuff_subClassing() {
        final Simulation simulation = new Simulation();

        final Combatant combatant = new Combatant();

        combatant.addBuff(SpecificAttackBuff.builder()
                                  .value(0.3)
                                  .build());
        combatant.addBuff(SpecificAttackBuff.builder()
                                  .value(0.5)
                                  .build());

        combatant.addBuff(BuffSpecificAttackBuff.builder()
                                  .value(0.7)
                                  .targetBuff(BurningLove.class)
                                  .build());
        combatant.addBuff(BuffSpecificAttackBuff.builder()
                                  .value(0.9)
                                  .targetBuff(BurningLove.class)
                                  .build());
        combatant.addBuff(BuffSpecificAttackBuff.builder()
                                  .value(1.1)
                                  .targetBuff(Charm.class)
                                  .build());

        simulation.setDefender(combatant);
        final double totalBuff = combatant.applyBuff(simulation, SpecificAttackBuff.class);
        assertEquals(0.8, totalBuff, 0.0000001);

        combatant.addBuff(BurningLove.builder().build());
        combatant.addBuff(BurningLove.builder().build());
        final double totalBuff2 = combatant.applyBuff(simulation, SpecificAttackBuff.class);
        assertEquals(4, totalBuff2, 0.0000001);
    }

    @Test
    public void testIsImmobilized() {
        final Combatant combatant = new Combatant();
        assertFalse(combatant.isImmobilized());
        combatant.addBuff(Stun.builder().build());
        assertTrue(combatant.isImmobilized());

        combatant.getBuffs().clear();
        assertFalse(combatant.isImmobilized());
        combatant.addBuff(Charm.builder().build());
        assertTrue(combatant.isImmobilized());
    }
}
