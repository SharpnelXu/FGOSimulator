package yome.fgo.simulator.models.combatants;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.conditions.BuffTypeEquals;
import yome.fgo.simulator.models.effects.buffs.Charm;
import yome.fgo.simulator.models.effects.buffs.CharmResistDown;
import yome.fgo.simulator.models.effects.buffs.CommandCardBuff;
import yome.fgo.simulator.models.effects.buffs.CommandCardResist;
import yome.fgo.simulator.models.effects.buffs.Confusion;
import yome.fgo.simulator.models.effects.buffs.DebuffResist;
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

        combatant.addBuff(DebuffResist.builder()
                                  .value(0.3)
                                  .build());
        combatant.addBuff(DebuffResist.builder()
                                  .value(0.5)
                                  .build());

        combatant.addBuff(CharmResistDown.builder()
                                  .condition(new BuffTypeEquals(Charm.class))
                                  .value(0.7)
                                  .build());
        combatant.addBuff(CharmResistDown.builder()
                                  .condition(new BuffTypeEquals(Charm.class))
                                  .value(0.9)
                                  .build());
        combatant.addBuff(CharmResistDown.builder()
                                  .condition(new BuffTypeEquals(Charm.class))
                                  .value(1.1)
                                  .build());

        simulation.setCurrentBuff(Confusion.builder().build());
        final double totalBuff = combatant.applyBuff(simulation, DebuffResist.class);
        assertEquals(0.8, totalBuff, 0.0000001);

        simulation.setCurrentBuff(Charm.builder().build());
        final double totalBuff2 = combatant.applyBuff(simulation, DebuffResist.class);
        assertEquals(-1.9, totalBuff2, 0.0000001);
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
