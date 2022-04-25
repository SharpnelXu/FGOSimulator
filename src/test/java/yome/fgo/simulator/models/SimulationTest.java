package yome.fgo.simulator.models;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import yome.fgo.data.proto.FgoStorageData;
import yome.fgo.data.proto.FgoStorageData.CraftEssenceOption;
import yome.fgo.data.proto.FgoStorageData.MysticCodeData;
import yome.fgo.data.proto.FgoStorageData.MysticCodeOption;
import yome.fgo.data.proto.FgoStorageData.ServantOption;
import yome.fgo.simulator.ResourceManager;
import yome.fgo.simulator.models.combatants.CombatAction;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.combatants.CommandCard;
import yome.fgo.simulator.models.combatants.Servant;
import yome.fgo.simulator.models.craftessences.CraftEssence;
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
    public void testSimpleKamaLoop() {
        final String id = "hq11_day5_90+_2";
        final String subPath = "events/hq11";

        final Level level = new Level(ResourceManager.getLevelData(subPath, id));

        final String kamaId = "servant321";
        final ServantOption kamaOption = ServantOption.newBuilder()
                .setServantLevel(120)
                .setNoblePhantasmRank(1)
                .setNoblePhantasmLevel(5)
                .setAttackStatusUp(2000)
                .setHealthStatusUp(2000)
                .addAllActiveSkillRanks(ImmutableList.of(1, 1, 1))
                .addAllActiveSkillLevels(ImmutableList.of(10, 10, 10))
                .addAllAppendSkillLevels(ImmutableList.of(10, 10, 10))
                .addCommandCardOptions(FgoStorageData.CommandCardOption.newBuilder())
                .addCommandCardOptions(FgoStorageData.CommandCardOption.newBuilder())
                .addCommandCardOptions(FgoStorageData.CommandCardOption.newBuilder())
                .addCommandCardOptions(FgoStorageData.CommandCardOption.newBuilder())
                .addCommandCardOptions(FgoStorageData.CommandCardOption.newBuilder())
                .setBond(15)
                .setAscension(1)
                .build();

        final Servant kama = new Servant(kamaId, ResourceManager.getServantData(kamaId), kamaOption);

        final CraftEssenceOption ceOption = CraftEssenceOption.newBuilder()
                .setCraftEssenceLevel(100)
                .build();
        final CraftEssence ce = new CraftEssence(ResourceManager.getCraftEssenceData("craftEssence1080"), ceOption);
        kama.equipCraftEssence(ce);

        final String altriaId = "servant284";
        final ServantOption altriaOption = ServantOption.newBuilder()
                .setServantLevel(90)
                .setNoblePhantasmRank(1)
                .setNoblePhantasmLevel(5)
                .setAttackStatusUp(1000)
                .setHealthStatusUp(1000)
                .addAllActiveSkillRanks(ImmutableList.of(1, 1, 1))
                .addAllActiveSkillLevels(ImmutableList.of(10, 10, 10))
                .addAllAppendSkillLevels(ImmutableList.of(10, 10, 10))
                .addCommandCardOptions(FgoStorageData.CommandCardOption.newBuilder())
                .addCommandCardOptions(FgoStorageData.CommandCardOption.newBuilder())
                .addCommandCardOptions(FgoStorageData.CommandCardOption.newBuilder())
                .addCommandCardOptions(FgoStorageData.CommandCardOption.newBuilder())
                .addCommandCardOptions(FgoStorageData.CommandCardOption.newBuilder())
                .setBond(10)
                .setAscension(1)
                .build();

        final Servant altria1 = new Servant(altriaId, ResourceManager.getServantData(altriaId), altriaOption);
        final Servant altria2 = new Servant(altriaId, ResourceManager.getServantData(altriaId), altriaOption);

        final Simulation simulation = new Simulation(
                level,
                ImmutableList.of(kama, altria1, altria2),
                new MysticCode(MysticCodeData.newBuilder().build(), MysticCodeOption.newBuilder().build())
        );
        simulation.initiate();
        simulation.setFixedRandom(0.9);
        simulation.setCurrentAllyTargetIndex(0);
        simulation.activateServantSkill(0, 0);
        simulation.activateServantSkill(1, 0);
        simulation.activateServantSkill(1, 1);
        simulation.activateServantSkill(1, 2);
        simulation.activateServantSkill(2, 0);
        simulation.activateServantSkill(2, 1);
        simulation.activateServantSkill(2, 2);

        assertEquals(1.20, kama.getCurrentNp());

        final CombatAction kamaNp = createNoblePhantasmAction(0);
        simulation.executeCombatActions(ImmutableList.of(kamaNp));

        assertEquals(1.0472, kama.getCurrentNp());

        simulation.executeCombatActions(ImmutableList.of(kamaNp));

        assertEquals(0.9842, kama.getCurrentNp());

        simulation.activateServantSkill(0, 1);
        simulation.activateServantSkill(0, 2);
        simulation.executeCombatActions(ImmutableList.of(kamaNp));

        assertEquals(0.8896, kama.getCurrentNp());

        assertTrue(simulation.isSimulationCompleted());
    }

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
