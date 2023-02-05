package yome.fgo.simulator.models;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.effects.buffs.Buff;
import yome.fgo.simulator.models.variations.HpVariation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static yome.fgo.data.proto.FgoStorageData.Target.SELF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.BUFF_CHANCE_BUFF;

public class HpVariationTest {

    @Test
    public void testHpVariedBuffChanceBuff_default() {
        final Buff buff = Buff.builder()
                .buffType(BUFF_CHANCE_BUFF)
                .value(0)
                .addition(0.4)
                .variation(new HpVariation(1, 0, SELF))
                .build();

        final Simulation simulation = new Simulation();
        final Combatant combatant = new Combatant("test", ImmutableList.of(100));
        assertEquals(100, combatant.getCurrentHp());
        simulation.setActivator(combatant);
        combatant.addBuff(buff);
        final double valueAt100 = combatant.applyValuedBuff(simulation, BUFF_CHANCE_BUFF);
        assertEquals(0, valueAt100);

        combatant.receiveDamage(50);
        final double valueAt50 = combatant.applyValuedBuff(simulation, BUFF_CHANCE_BUFF);
        assertEquals(0.2, valueAt50);

        combatant.receiveDamage(50);
        final double valueAt0 = combatant.applyValuedBuff(simulation, BUFF_CHANCE_BUFF);
        assertEquals(0.4, valueAt0);

        combatant.receiveDamage(50);
        final double valueAtNegative = combatant.applyValuedBuff(simulation, BUFF_CHANCE_BUFF);
        assertEquals(0.4, valueAtNegative);
    }
    @Test
    public void testHpVariedBuffChanceBuff_nonDefaults() {
        final Buff buff = Buff.builder()
                .buffType(BUFF_CHANCE_BUFF)
                .value(0.2)
                .addition(0.4)
                .variation(new HpVariation(0.5, 0.3, SELF))
                .build();

        final Simulation simulation = new Simulation();
        final Combatant combatant = new Combatant("test", ImmutableList.of(100));
        assertEquals(100, combatant.getCurrentHp());
        simulation.setActivator(combatant);
        combatant.addBuff(buff);
        final double valueAt100 = combatant.applyValuedBuff(simulation, BUFF_CHANCE_BUFF);
        assertEquals(0, valueAt100);

        combatant.receiveDamage(25);
        final double valueAt75 = combatant.applyValuedBuff(simulation, BUFF_CHANCE_BUFF);
        assertEquals(0, valueAt75);

        combatant.receiveDamage(25);
        final double valueAt50 = combatant.applyValuedBuff(simulation, BUFF_CHANCE_BUFF);
        assertEquals(0.2, valueAt50);

        combatant.receiveDamage(15);
        final double valueAt35 = combatant.applyValuedBuff(simulation, BUFF_CHANCE_BUFF);
        assertEquals(0.5, valueAt35);

        combatant.receiveDamage(25);
        final double valueAt10 = combatant.applyValuedBuff(simulation, BUFF_CHANCE_BUFF);
        assertEquals(0.6, valueAt10);

        combatant.receiveDamage(50);
        final double valueAtNegative = combatant.applyValuedBuff(simulation, BUFF_CHANCE_BUFF);
        assertEquals(0.6, valueAtNegative);
    }
}
