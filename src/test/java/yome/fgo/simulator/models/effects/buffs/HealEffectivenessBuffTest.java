package yome.fgo.simulator.models.effects.buffs;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.effects.HpChange;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static yome.fgo.data.proto.FgoStorageData.Target.SELF;
import static yome.fgo.simulator.models.effects.buffs.BuffType.HEAL_EFFECTIVENESS_BUFF;

public class HealEffectivenessBuffTest {
    @Test
    public void testHealEffectivenessBuff() {
        final Simulation simulation = new Simulation();
        final Combatant combatant = new Combatant("test", ImmutableList.of(100));
        assertEquals(100, combatant.getCurrentHp());
        combatant.receiveDamage(50);
        assertEquals(50, combatant.getCurrentHp());
        final HpChange hpChange = HpChange.builder().values(ImmutableList.of(10)).target(SELF).build();

        simulation.setActivator(combatant);
        hpChange.apply(simulation);

        assertEquals(60, combatant.getCurrentHp());

        final Buff buff = Buff.builder().buffType(HEAL_EFFECTIVENESS_BUFF).value(1.5).build();
        combatant.addBuff(buff);
        hpChange.apply(simulation);

        assertEquals(85, combatant.getCurrentHp());

        final Buff buff2 = Buff.builder().buffType(HEAL_EFFECTIVENESS_BUFF).value(-2.5).build();
        combatant.addBuff(buff2);
        hpChange.apply(simulation);

        assertEquals(85, combatant.getCurrentHp());
    }
}
