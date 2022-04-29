package yome.fgo.simulator.models.effects.buffs;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import yome.fgo.data.proto.FgoStorageData.BuffData;
import yome.fgo.data.proto.FgoStorageData.CombatantData;
import yome.fgo.data.proto.FgoStorageData.ConditionData;
import yome.fgo.data.proto.FgoStorageData.VariationData;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.combatants.Servant;
import yome.fgo.simulator.models.conditions.BuffTypeEquals;
import yome.fgo.simulator.models.conditions.TargetsHaveTrait;
import yome.fgo.simulator.models.variations.BuffCountVariation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static yome.fgo.data.proto.FgoStorageData.Target.DEFENDER;
import static yome.fgo.data.proto.FgoStorageData.Traits.DEMONIC;
import static yome.fgo.simulator.models.conditions.Always.ALWAYS;
import static yome.fgo.simulator.models.effects.buffs.BuffFactory.buildBuff;

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
                                .setValue(DEMONIC.name())
                )
                .addAllValues(ImmutableList.of(10.0, 15.0, 20.0))
                .build();

        final Buff buff = buildBuff(buffData, 2);

        final Simulation simulation = new Simulation();

        assertEquals(-1, buff.getNumTimesActive());
        assertEquals(3, buff.getNumTurnsActive());
        assertEquals(15.0, ((ValuedBuff) buff).getValue(simulation));

        final CombatantData demonic = CombatantData.newBuilder()
                .addTraits(DEMONIC.name())
                .build();
        simulation.setDefender(new Servant("", demonic));
        assertTrue(buff.shouldApply(simulation));
    }

    @Test
    public void testBuffFactory_buffSpecificAttack() {
        final BuffData buffData = BuffData.newBuilder()
                .setType(SpecificAttackBuff.class.getSimpleName())
                .addValues(0)
                .setNumTurnsActive(3)
                .setApplyCondition(
                        ConditionData.newBuilder()
                                .setType(TargetsHaveTrait.class.getSimpleName())
                                .setTarget(DEFENDER)
                                .setValue(DEMONIC.name())
                )
                .setVariationData(
                        VariationData.newBuilder()
                                .setType(BuffCountVariation.class.getSimpleName())
                                .setTarget(DEFENDER)
                                .setMaxCount(10)
                                .setConditionData(
                                        ConditionData.newBuilder()
                                                .setType(BuffTypeEquals.class.getSimpleName())
                                                .setValue(BurningLove.class.getSimpleName())
                                )
                )
                .addAdditions(10)
                .addAdditions(15)
                .addAdditions(17)
                .build();

        final Buff buff = buildBuff(buffData, 2);

        final Simulation simulation = new Simulation();

        assertEquals(-1, buff.getNumTimesActive());
        assertEquals(3, buff.getNumTurnsActive());

        final CombatantData demonic = CombatantData.newBuilder()
                .addTraits(DEMONIC.name())
                .build();
        final Combatant defender = new Combatant("", demonic);
        simulation.setDefender(defender);
        assertTrue(buff.shouldApply(simulation));
        assertEquals(0, ((ValuedBuff) buff).getValue(simulation));

        defender.addBuff(BurningLove.builder().build());
        assertEquals(15.0, ((ValuedBuff) buff).getValue(simulation));

        defender.addBuff(BurningLove.builder().build());
        assertEquals(30.0, ((ValuedBuff) buff).getValue(simulation));
    }

    @Test
    public void testBuffFactory_grantTrait() {
        final BuffData buffData = BuffData.newBuilder()
                .setType(GrantTrait.class.getSimpleName())
                .setNumTurnsActive(3)
                .setStringValue(DEMONIC.name())
                .build();

        final Buff buff = buildBuff(buffData, 2);
        assertEquals(-1, buff.getNumTimesActive());
        assertEquals(3, buff.getNumTurnsActive());
        assertEquals(ALWAYS, buff.getCondition());
    }
}
