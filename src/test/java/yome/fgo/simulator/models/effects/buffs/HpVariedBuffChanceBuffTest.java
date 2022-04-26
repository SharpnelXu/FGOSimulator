package yome.fgo.simulator.models.effects.buffs;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HpVariedBuffChanceBuffTest {
    @Test
    public void testHpVariedBuffChanceBuff_default() {
        final HpVariedBuffChanceBuff buff = HpVariedBuffChanceBuff.builder()
                .value(0.4)
                .build();

        final Simulation simulation = new Simulation();
        final Combatant combatant = new Combatant("test", ImmutableList.of(100));
        assertEquals(100, combatant.getCurrentHp());
        simulation.setActivator(combatant);
        combatant.addBuff(buff);
        final double valueAt100 = combatant.applyBuff(simulation, BuffChanceBuff.class);
        assertEquals(0, valueAt100);

        combatant.receiveDamage(50);
        final double valueAt50 = combatant.applyBuff(simulation, BuffChanceBuff.class);
        assertEquals(0.2, valueAt50);

        combatant.receiveDamage(50);
        final double valueAt0 = combatant.applyBuff(simulation, BuffChanceBuff.class);
        assertEquals(0.4, valueAt0);

        combatant.receiveDamage(50);
        final double valueAtNegative = combatant.applyBuff(simulation, BuffChanceBuff.class);
        assertEquals(0.4, valueAtNegative);
    }
    @Test
    public void testHpVariedBuffChanceBuff_nonDefaults() {
        final HpVariedBuffChanceBuff buff = HpVariedBuffChanceBuff.builder()
                .value(0.4)
                .minHpPercent(0.3)
                .maxHpPercent(0.5)
                .baseValue(0.2)
                .build();

        final Simulation simulation = new Simulation();
        final Combatant combatant = new Combatant("test", ImmutableList.of(100));
        assertEquals(100, combatant.getCurrentHp());
        simulation.setActivator(combatant);
        combatant.addBuff(buff);
        final double valueAt100 = combatant.applyBuff(simulation, BuffChanceBuff.class);
        assertEquals(0, valueAt100);

        combatant.receiveDamage(25);
        final double valueAt75 = combatant.applyBuff(simulation, BuffChanceBuff.class);
        assertEquals(0, valueAt75);

        combatant.receiveDamage(25);
        final double valueAt50 = combatant.applyBuff(simulation, BuffChanceBuff.class);
        assertEquals(0.2, valueAt50);

        combatant.receiveDamage(15);
        final double valueAt35 = combatant.applyBuff(simulation, BuffChanceBuff.class);
        assertEquals(0.5, valueAt35);

        combatant.receiveDamage(25);
        final double valueAt10 = combatant.applyBuff(simulation, BuffChanceBuff.class);
        assertEquals(0.6, valueAt10);

        combatant.receiveDamage(50);
        final double valueAtNegative = combatant.applyBuff(simulation, BuffChanceBuff.class);
        assertEquals(0.6, valueAtNegative);
    }
}
