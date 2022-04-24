package yome.fgo.data.writer.enemies;

import yome.fgo.data.proto.FgoStorageData.CombatantData;
import yome.fgo.simulator.translation.Traits;

import static yome.fgo.data.proto.FgoStorageData.Attribute.SKY;
import static yome.fgo.data.proto.FgoStorageData.FateClass.BERSERKER;
import static yome.fgo.data.writer.DataWriterMain.writeEnemy;
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
                .addTraits(Traits.DEMONIC)
                .setDeathRate(0.5)
                .build();

        writeEnemy(combatantData, OTHER, MINION);
    }
}
