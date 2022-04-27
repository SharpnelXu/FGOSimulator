package yome.fgo.data.writer.enemies;

import yome.fgo.data.proto.FgoStorageData.CombatantData;

import static yome.fgo.data.proto.FgoStorageData.Attribute.SKY;
import static yome.fgo.data.proto.FgoStorageData.FateClass.BERSERKER;
import static yome.fgo.data.writer.DataWriter.writeEnemy;
import static yome.fgo.simulator.translation.EnemyCategory.EnemySubCategory.MINION;
import static yome.fgo.simulator.translation.EnemyCategory.OTHER;

public class MagicalStick {
    public static void main(final String[] args) {
        final String id = "magicalStick";
        final CombatantData combatantData = CombatantData.newBuilder()
                .setId(id)
                .setRarity(3)
                .setFateClass(BERSERKER)
                .setAttribute(SKY)
                .setDeathRate(0.5)
                .build();

        writeEnemy(combatantData, OTHER, MINION);
    }
}
