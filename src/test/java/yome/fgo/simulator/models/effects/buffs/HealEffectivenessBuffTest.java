package yome.fgo.simulator.models.effects.buffs;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.effects.HpChange;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static yome.fgo.data.proto.FgoStorageData.Target.SELF;

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

        final HealEffectivenessBuff buff = HealEffectivenessBuff.builder().value(1.5).build();
        combatant.addBuff(buff);
        hpChange.apply(simulation);

        assertEquals(85, combatant.getCurrentHp());

        final HealEffectivenessBuff buff2 = HealEffectivenessBuff.builder().value(-2.5).build();
        combatant.addBuff(buff2);
        hpChange.apply(simulation);

        assertEquals(85, combatant.getCurrentHp());
    }
}
