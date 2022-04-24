package yome.fgo.simulator.models.effects.buffs;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import yome.fgo.data.proto.FgoStorageData.BuffData;
import yome.fgo.data.proto.FgoStorageData.CombatantData;
import yome.fgo.data.proto.FgoStorageData.ConditionData;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Servant;
import yome.fgo.simulator.models.conditions.TargetsHaveTrait;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static yome.fgo.data.proto.FgoStorageData.Target.DEFENDER;
import static yome.fgo.simulator.models.conditions.Always.ALWAYS;
import static yome.fgo.simulator.models.effects.buffs.BuffFactory.buildBuff;
import static yome.fgo.simulator.translation.Traits.DEMONIC;

public class BuffFactoryTest {

    @Test
    public void testBuffFactory_unsupported() {
        final BuffData buffData = BuffData.newBuilder()
                .setType("Custom")
                .build();

        assertThrows(UnsupportedOperationException.class, () -> buildBuff(buffData, 0));
    }

    @Test
    public void testBuffFactory_specificAttack() {
        final BuffData buffData = BuffData.newBuilder()
                .setType(SpecificAttackBuff.class.getSimpleName())
                .setNumTurnsActive(3)
                .setApplyCondition(
                        ConditionData.newBuilder()
                                .setType(TargetsHaveTrait.class.getSimpleName())
                                .setTarget(DEFENDER)
                                .setValue(DEMONIC)
                )
                .addAllValues(ImmutableList.of(10.0, 15.0, 20.0))
                .build();

        final Buff buff = buildBuff(buffData, 2);
        assertEquals(-1, buff.getNumTimesActive());
        assertEquals(3, buff.getNumTurnsActive());
        assertEquals(15.0, buff.getValue());

        final Simulation simulation = new Simulation();

        final CombatantData nonRiding = CombatantData.newBuilder()
                .addTraits(DEMONIC)
                .build();
        simulation.setDefender(new Servant("", nonRiding));
        assertTrue(buff.shouldApply(simulation));
    }

    @Test
    public void testBuffFactory_grantTrait() {
        final BuffData buffData = BuffData.newBuilder()
                .setType(GrantTrait.class.getSimpleName())
                .setNumTurnsActive(3)
                .setStringValue(DEMONIC)
                .build();

        final Buff buff = buildBuff(buffData, 2);
        assertEquals(-1, buff.getNumTimesActive());
        assertEquals(3, buff.getNumTurnsActive());
        assertEquals(ALWAYS, buff.getCondition());
    }
}
