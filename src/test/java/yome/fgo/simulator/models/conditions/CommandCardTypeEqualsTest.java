package yome.fgo.simulator.models.conditions;

import org.junit.jupiter.api.Test;
import yome.fgo.data.proto.FgoStorageData.CommandCardData;
import yome.fgo.data.proto.FgoStorageData.ConditionData;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.CommandCard;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.ANY;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.BUSTER;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.QUICK;
import static yome.fgo.simulator.models.conditions.ConditionFactory.buildCondition;

public class CommandCardTypeEqualsTest {
    @Test
    public void testCommandCardTypeEquals_specificType() {
        final ConditionData conditionData = ConditionData.newBuilder()
                .setType(CommandCardTypeEquals.class.getSimpleName())
                .setValue(BUSTER.name())
                .build();

        final Condition condition = buildCondition(conditionData);
        final Simulation simulation = new Simulation();

        simulation.setCurrentCommandCard(new CommandCard(CommandCardData.newBuilder().setCommandCardType(BUSTER).build()));
        assertTrue(condition.evaluate(simulation));

        simulation.setCurrentCommandCard(new CommandCard(CommandCardData.newBuilder().setCommandCardType(QUICK).build()));
        assertFalse(condition.evaluate(simulation));
    }
    @Test
    public void testCommandCardTypeEquals_any() {
        final ConditionData conditionData = ConditionData.newBuilder()
                .setType(CommandCardTypeEquals.class.getSimpleName())
                .setValue(ANY.name())
                .build();

        final Condition condition = buildCondition(conditionData);
        final Simulation simulation = new Simulation();

        simulation.setCurrentCommandCard(new CommandCard(CommandCardData.newBuilder().setCommandCardType(BUSTER).build()));
        assertTrue(condition.evaluate(simulation));

        simulation.setCurrentCommandCard(new CommandCard(CommandCardData.newBuilder().setCommandCardType(QUICK).build()));
        assertTrue(condition.evaluate(simulation));
    }
}
