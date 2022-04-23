package yome.fgo.simulator.models.combatants;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.effects.buffs.CommandCardBuff;
import yome.fgo.simulator.models.effects.buffs.CommandCardResist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
