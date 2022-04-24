package yome.fgo.simulator.models.conditions;

import org.junit.jupiter.api.Test;
import yome.fgo.data.proto.FgoStorageData.CombatantData;
import yome.fgo.data.proto.FgoStorageData.ConditionData;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Servant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static yome.fgo.data.proto.FgoStorageData.Target.EFFECT_TARGET;
import static yome.fgo.simulator.models.conditions.ConditionFactory.buildCondition;
import static yome.fgo.simulator.translation.Traits.DEMONIC;
import static yome.fgo.simulator.translation.Traits.RIDING;

public class AndTest {
    @Test
    public void testAnd() {
        final ConditionData conditionData = ConditionData.newBuilder()
                .setType(And.class.getSimpleName())
                .addSubConditionData(
                        ConditionData.newBuilder()
                                .setType(TargetsHaveTrait.class.getSimpleName())
                                .setTarget(EFFECT_TARGET)
                                .setValue(RIDING)
                )
                .addSubConditionData(
                        ConditionData.newBuilder()
                                .setType(TargetsHaveTrait.class.getSimpleName())
                                .setTarget(EFFECT_TARGET)
                                .setValue(DEMONIC)
                )
                .build();

        final Condition condition = buildCondition(conditionData);

        final Simulation simulation = new Simulation();

        final CombatantData servant1 = CombatantData.newBuilder()
                .addTraits(RIDING)
                .addTraits(DEMONIC)
                .build();
        simulation.setEffectTarget(new Servant("", servant1));
        assertTrue(condition.evaluate(simulation));

        final CombatantData servant2 = CombatantData.newBuilder()
                .addTraits(RIDING)
                .build();
        simulation.setEffectTarget(new Servant("", servant2));
        assertFalse(condition.evaluate(simulation));
    }
}
