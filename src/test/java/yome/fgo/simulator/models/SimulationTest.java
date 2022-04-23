package yome.fgo.simulator.models;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import yome.fgo.simulator.models.combatants.CombatAction;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.combatants.CommandCard;
import yome.fgo.simulator.models.combatants.Servant;
import yome.fgo.simulator.models.levels.Level;
import yome.fgo.simulator.models.levels.Stage;
import yome.fgo.simulator.models.mysticcodes.MysticCode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.ARTS;
import static yome.fgo.data.proto.FgoStorageData.CommandCardType.BUSTER;
import static yome.fgo.simulator.models.Simulation.getNextNonNullTargetIndex;
import static yome.fgo.simulator.models.Simulation.isBraveChain;
import static yome.fgo.simulator.models.Simulation.shouldRemoveDeadCombatants;
import static yome.fgo.simulator.models.combatants.CombatAction.createCommandCardAction;
import static yome.fgo.simulator.models.combatants.CombatAction.createNoblePhantasmAction;

public class SimulationTest {
    public static final String SERVANT_ID_1 = "servant1";
    public static final String SERVANT_ID_2 = "servant2";
    public static final String SERVANT_ID_3 = "servant3";
    public static final String SERVANT_ID_4 = "servant4";

    public static final Level SIMPLE_LEVEL = new Level(
            "SIMPLE_LEVEL",
            ImmutableList.of(new Stage(ImmutableList.of(new Combatant(
                    "SIMPLE_COMBATANT",
                    ImmutableList.of(10000)
            )), 3, ImmutableList.of())),
            ImmutableList.of()
    );
    public static final MysticCode SIMPLE_MYSTIC_CODE = new MysticCode();
    public static final List<CombatAction> COMMAND_CARD_0_1_0 = ImmutableList.of(
            createCommandCardAction(0, 0, false),
            createCommandCardAction(1, 0, false),
            createCommandCardAction(0, 0, false)
    );
    public static final List<CombatAction> COMMAND_CARD_0_0_0 = ImmutableList.of(
            createCommandCardAction(0, 0, false),
            createCommandCardAction(0, 0, false),
            createCommandCardAction(0, 0, false)
    );
    public static final List<CombatAction> COMMAND_CARD_0_0 = ImmutableList.of(
            createCommandCardAction(0, 0, false),
            createCommandCardAction(0, 0, false)
    );

    public Simulation simulation;

    @Test
    public void testIsTypeChain() {
        final Servant busterServant = new Servant("buster");
        busterServant.setCommandCards(ImmutableList.of(new CommandCard(BUSTER, ImmutableList.of(), 0.5, 39)));
        final Servant artsServant = new Servant("arts");
        artsServant.setCommandCards(ImmutableList.of(new CommandCard(ARTS, ImmutableList.of(), 0.5, 39)));
        final List<Servant> servants = ImmutableList.of(busterServant, artsServant);
        simulation = new Simulation();
        simulation.setCurrentServants(servants);

        assertFalse(simulation.isTypeChain(COMMAND_CARD_0_1_0));
        assertTrue(simulation.isTypeChain(COMMAND_CARD_0_0_0));
        assertFalse(simulation.isTypeChain(COMMAND_CARD_0_0));
    }

    @Test
    public void testIsBraveChain() {
        assertFalse(isBraveChain(COMMAND_CARD_0_1_0));
        assertTrue(isBraveChain(COMMAND_CARD_0_0_0));
        assertFalse(isBraveChain(COMMAND_CARD_0_0));
    }

    @Test
    public void testShouldRemoveDeadCombatants() {
        final List<CombatAction> noblePhantasm = ImmutableList.of(createNoblePhantasmAction(0));
        assertTrue(shouldRemoveDeadCombatants(noblePhantasm, 0));

        final List<CombatAction> nextCardNoblePhantasm = ImmutableList.of(
                createCommandCardAction(0, 0, false),
                createNoblePhantasmAction(0)
        );
        assertTrue(shouldRemoveDeadCombatants(nextCardNoblePhantasm, 0));

        final List<CombatAction> nextCardDifferentServant = ImmutableList.of(
                createCommandCardAction(0, 0, false),
                createCommandCardAction(1, 0, false)
        );
        assertTrue(shouldRemoveDeadCombatants(nextCardDifferentServant, 0));

        final List<CombatAction> nextCardSameServant = ImmutableList.of(
                createCommandCardAction(0, 0, false),
                createCommandCardAction(0, 0, false)
        );
        assertFalse(shouldRemoveDeadCombatants(nextCardSameServant, 0));

        // last card no brave chain
        assertTrue(shouldRemoveDeadCombatants(COMMAND_CARD_0_0, 1));

        // last card brave chain
        assertFalse(shouldRemoveDeadCombatants(COMMAND_CARD_0_0_0, 2));
    }

    @Test
    public void testGetNextNonNullTargetIndex() {
        final List<Combatant> combatants = Lists.newArrayList(
                null,
                new Combatant(),
                new Combatant(),
                null
        );
        assertEquals(1, getNextNonNullTargetIndex(combatants, 0));
        assertEquals(1, getNextNonNullTargetIndex(combatants, 1));
        assertEquals(2, getNextNonNullTargetIndex(combatants, 2));
        assertEquals(1, getNextNonNullTargetIndex(combatants, 3));

        final List<Combatant> nullList = Lists.newArrayList(null, null);
        assertEquals(0, getNextNonNullTargetIndex(nullList, 0));
        assertEquals(0, getNextNonNullTargetIndex(nullList, 1));
    }
}
