package yome.fgo.data.writer.enemies;

import yome.fgo.data.proto.FgoStorageData.CombatantData;
import yome.fgo.simulator.translation.Traits;

import static yome.fgo.data.proto.FgoStorageData.Attribute.EARTH;
import static yome.fgo.data.proto.FgoStorageData.FateClass.BERSERKER;
import static yome.fgo.data.proto.FgoStorageData.Gender.MALE;
import static yome.fgo.data.writer.DataWriter.writeEnemy;
import static yome.fgo.simulator.translation.EnemyCategory.DECEASED;
import static yome.fgo.simulator.translation.EnemyCategory.EnemySubCategory.GHOUL;

public class Ghoul {
    public static void main(final String[] args) {
        final String id = "ghoul";
        final CombatantData combatantData = CombatantData.newBuilder()
                .setId(id)
                .setRarity(1)
                .setFateClass(BERSERKER)
                .setGender(MALE)
                .setAttribute(EARTH)
                .addTraits(Traits.DEMONIC)
                .addTraits(Traits.HUMAN)
                .addTraits(Traits.HUMANOID)
                .addTraits(Traits.UNDEAD)
                .addTraits(Traits.ONI)
                .setDeathRate(1.0)
                .build();

        writeEnemy(combatantData, DECEASED, GHOUL);
    }
}
