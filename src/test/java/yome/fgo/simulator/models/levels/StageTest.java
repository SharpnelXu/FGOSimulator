package yome.fgo.simulator.models.levels;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import yome.fgo.data.proto.FgoStorageData.CombatantData;
import yome.fgo.data.proto.FgoStorageData.EnemyData;
import yome.fgo.data.proto.FgoStorageData.StageData;
import yome.fgo.simulator.models.combatants.Combatant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static yome.fgo.data.proto.FgoStorageData.FateClass.RIDER;
import static yome.fgo.simulator.translation.EnemyCategory.DECEASED;
import static yome.fgo.simulator.translation.EnemyCategory.EnemySubCategory.GHOUL;

public class StageTest {
    @Test
    public void testPollEnemies() {
        final Stage stage = new Stage(
                ImmutableList.of(new Combatant(), new Combatant()),
                3,
                ImmutableList.of()
        );

        assertTrue(stage.hasMoreEnemies());
        stage.getNextEnemy();
        stage.getNextEnemy();
        assertFalse(stage.hasMoreEnemies());
    }

    @Test
    public void testConstructor() {
        final StageData stageData = StageData.newBuilder()
                .addEnemyData(EnemyData.newBuilder()
                                      .setEnemyBaseId("ghoul")
                                      .setEnemyCategories(DECEASED + "/" + GHOUL)
                                      .addHpBars(35219)
                                      .setCombatantDataOverride(CombatantData.newBuilder()
                                                                        .setFateClass(RIDER)))
                .addEnemyData(EnemyData.newBuilder()
                                      .setEnemyBaseId("elderGhoul")
                                      .setEnemyCategories(DECEASED + "/" + GHOUL)
                                      .addHpBars(40150)
                                      .setCombatantDataOverride(CombatantData.newBuilder()
                                                                        .setFateClass(RIDER)))
                .addEnemyData(EnemyData.newBuilder()
                                      .setEnemyBaseId("ghoul")
                                      .setEnemyCategories(DECEASED + "/" + GHOUL)
                                      .addHpBars(35611)
                                      .setCombatantDataOverride(CombatantData.newBuilder()
                                                                        .setFateClass(RIDER)))
                .build();

        final Stage stage = new Stage(stageData);
        assertTrue(stage.hasMoreEnemies());

        final Combatant ghoul1 = stage.getNextEnemy();
        assertEquals("ghoul", ghoul1.getId());
        assertEquals(35219, ghoul1.getCurrentHp());
        assertEquals(RIDER, ghoul1.getFateClass());
        final Combatant ghoul2 = stage.getNextEnemy();
        assertEquals("elderGhoul", ghoul2.getId());
        assertEquals(40150, ghoul2.getCurrentHp());
        assertEquals(RIDER, ghoul2.getFateClass());
        final Combatant ghoul3 = stage.getNextEnemy();
        assertEquals("ghoul", ghoul3.getId());
        assertEquals(35611, ghoul3.getCurrentHp());
        assertEquals(RIDER, ghoul3.getFateClass());

        assertFalse(stage.hasMoreEnemies());
    }
}
