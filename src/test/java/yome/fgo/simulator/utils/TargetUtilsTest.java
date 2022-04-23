package yome.fgo.simulator.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;
import yome.fgo.simulator.models.combatants.Servant;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static yome.fgo.data.proto.FgoStorageData.Target.ALL_ALLIES;
import static yome.fgo.data.proto.FgoStorageData.Target.ALL_ALLIES_EXCLUDING_SELF;
import static yome.fgo.data.proto.FgoStorageData.Target.ALL_ALLIES_EXCLUDING_SELF_INCLUDING_BACKUP;
import static yome.fgo.data.proto.FgoStorageData.Target.ALL_ALLIES_INCLUDING_BACKUP;
import static yome.fgo.data.proto.FgoStorageData.Target.ALL_CHARACTERS;
import static yome.fgo.data.proto.FgoStorageData.Target.ALL_CHARACTERS_EXCLUDING_SELF;
import static yome.fgo.data.proto.FgoStorageData.Target.ALL_CHARACTERS_EXCLUDING_SELF_INCLUDING_BACKUP;
import static yome.fgo.data.proto.FgoStorageData.Target.ALL_ENEMIES;
import static yome.fgo.data.proto.FgoStorageData.Target.ALL_ENEMIES_INCLUDING_BACKUP;
import static yome.fgo.data.proto.FgoStorageData.Target.FIRST_ALLY_EXCLUDING_SELF;
import static yome.fgo.data.proto.FgoStorageData.Target.FIRST_ENEMY;
import static yome.fgo.data.proto.FgoStorageData.Target.NON_TARGETED_ALLIES;
import static yome.fgo.data.proto.FgoStorageData.Target.NON_TARGETED_ENEMIES;
import static yome.fgo.simulator.utils.TargetUtils.getTargets;

public class TargetUtilsTest {
    public static final String SERVANT_1 = "servant1";
    public static final String SERVANT_2 = "servant2";
    public static final String SERVANT_3 = "servant3";
    public static final String SERVANT_4 = "servant4";
    public static final String SERVANT_5 = "servant5";
    public static final String SERVANT_6 = "servant6";

    private Simulation simulation;

    @BeforeEach
    public void init() {
        simulation = new Simulation();
    }

    @Test
    public void testGetTargets_fullAlly() {
        final List<Servant> currentServants = Lists.newArrayList(
                new Servant(SERVANT_1),
                new Servant(SERVANT_2),
                new Servant(SERVANT_3)
        );

        final LinkedList<Servant> backupServants = Lists.newLinkedList(ImmutableList.of(
                new Servant(SERVANT_4),
                new Servant(SERVANT_5),
                new Servant(SERVANT_6)
        ));

        simulation.setCurrentServants(currentServants);
        simulation.setBackupServants(backupServants);

        assertThat(getTargets(simulation, ALL_ALLIES)).containsExactlyInAnyOrderElementsOf(currentServants);

        final List<Servant> allAlliesIncludingBackup = new ArrayList<>();
        allAlliesIncludingBackup.addAll(currentServants);
        allAlliesIncludingBackup.addAll(backupServants);
        assertThat(getTargets(simulation, ALL_ALLIES_INCLUDING_BACKUP))
                .containsExactlyInAnyOrderElementsOf(allAlliesIncludingBackup);

        simulation.setActivator(currentServants.get(1));
        final List<Combatant> allAlliesExcludingSelf = getTargets(simulation, ALL_ALLIES_EXCLUDING_SELF);
        assertThat(allAlliesExcludingSelf.stream().map(Combatant::getId))
                .containsExactlyInAnyOrder(SERVANT_1, SERVANT_3);

        simulation.setActivator(currentServants.get(0));
        final List<Combatant> firstAllyExcludingSelf = getTargets(simulation, FIRST_ALLY_EXCLUDING_SELF);
        assertThat(firstAllyExcludingSelf.stream().map(Combatant::getId)).containsExactly(SERVANT_2);

        simulation.setActivator(backupServants.get(2));
        final List<Combatant> allAlliesExcludingSelfIncludingBackup =
                getTargets(simulation, ALL_ALLIES_EXCLUDING_SELF_INCLUDING_BACKUP);
        assertThat(allAlliesExcludingSelfIncludingBackup.stream().map(Combatant::getId))
                .containsExactlyInAnyOrder(SERVANT_1, SERVANT_2, SERVANT_3, SERVANT_4, SERVANT_5);

        simulation.setCurrentAllyTargetIndex(1);
        final List<Combatant> nonTargetedAllies = getTargets(simulation, NON_TARGETED_ALLIES);
        assertThat(nonTargetedAllies.stream().map(Combatant::getId))
                .containsExactlyInAnyOrder(SERVANT_1, SERVANT_3);
    }

    @Test
    public void testGetTargets_nullAlly() {
        final List<Servant> currentServants = Lists.newArrayList(
                null,
                new Servant(SERVANT_2),
                null
        );

        final LinkedList<Servant> backupServants = Lists.newLinkedList(ImmutableList.of(
                new Servant(SERVANT_4),
                new Servant(SERVANT_5),
                new Servant(SERVANT_6)
        ));

        simulation.setCurrentServants(currentServants);
        simulation.setBackupServants(backupServants);

        assertThat(getTargets(simulation, ALL_ALLIES)).containsExactly(currentServants.get(1));

        final List<Servant> allAlliesIncludingBackup = new ArrayList<>();
        allAlliesIncludingBackup.add(currentServants.get(1));
        allAlliesIncludingBackup.addAll(backupServants);
        assertThat(getTargets(simulation, ALL_ALLIES_INCLUDING_BACKUP))
                .containsExactlyInAnyOrderElementsOf(allAlliesIncludingBackup);

        simulation.setActivator(currentServants.get(1));
        assertThat(getTargets(simulation, ALL_ALLIES_EXCLUDING_SELF)).isEmpty();
        assertThat(getTargets(simulation, FIRST_ALLY_EXCLUDING_SELF)).isEmpty();

        simulation.setActivator(backupServants.get(2));
        final List<Combatant> allAlliesExcludingSelfIncludingBackup =
                getTargets(simulation, ALL_ALLIES_EXCLUDING_SELF_INCLUDING_BACKUP);
        assertThat(allAlliesExcludingSelfIncludingBackup.stream().map(Combatant::getId))
                .containsExactlyInAnyOrder(SERVANT_2, SERVANT_4, SERVANT_5);
    }

    @Test
    public void testGetTargets_nullEnemies() {
        final List<Combatant> currentEnemies = Lists.newArrayList(
                null,
                new Servant(SERVANT_2),
                null
        );

        final LinkedList<Combatant> backupEnemies = Lists.newLinkedList(ImmutableList.of(
                new Servant(SERVANT_4),
                new Servant(SERVANT_5),
                new Servant(SERVANT_6)
        ));

        simulation.setCurrentEnemies(currentEnemies);
        simulation.setBackupEnemies(backupEnemies);

        assertThat(getTargets(simulation, ALL_ENEMIES)).containsExactly(currentEnemies.get(1));

        final List<Combatant> allEnemiesIncludingBackup = new ArrayList<>();
        allEnemiesIncludingBackup.add(currentEnemies.get(1));
        allEnemiesIncludingBackup.addAll(backupEnemies);
        assertThat(getTargets(simulation, ALL_ENEMIES_INCLUDING_BACKUP))
                .containsExactlyInAnyOrderElementsOf(allEnemiesIncludingBackup);

        simulation.setCurrentEnemyTargetIndex(1);
        assertThat(getTargets(simulation, FIRST_ENEMY)).containsExactly(currentEnemies.get(1));
        assertThat(getTargets(simulation, NON_TARGETED_ENEMIES)).isEmpty();
    }

    @Test
    public void testGetTargets_nullCharacters() {
        final List<Servant> currentServants = Lists.newArrayList(
                null,
                new Servant(SERVANT_2),
                null
        );

        final LinkedList<Servant> backupServants = Lists.newLinkedList(ImmutableList.of(
                new Servant(SERVANT_4),
                new Servant(SERVANT_5),
                new Servant(SERVANT_6)
        ));

        simulation.setCurrentServants(currentServants);
        simulation.setBackupServants(backupServants);

        final List<Combatant> currentEnemies = Lists.newArrayList(
                null,
                new Servant(SERVANT_1),
                null
        );

        final LinkedList<Combatant> backupEnemies = Lists.newLinkedList(ImmutableList.of(new Servant(SERVANT_3)));

        simulation.setCurrentEnemies(currentEnemies);
        simulation.setBackupEnemies(backupEnemies);

        final List<Combatant> allCharacters = getTargets(simulation, ALL_CHARACTERS);
        assertThat(allCharacters.stream().map(Combatant::getId))
                .containsExactlyInAnyOrder(SERVANT_1, SERVANT_2);

        simulation.setActivator(currentServants.get(1));
        final List<Combatant> allCharactersExcludingSelf = getTargets(simulation, ALL_CHARACTERS_EXCLUDING_SELF);
        assertThat(allCharactersExcludingSelf.stream().map(Combatant::getId))
                .containsExactlyInAnyOrder(SERVANT_1);

        final List<Combatant> allCharactersExcludingSelfIncludingBackup = getTargets(
                simulation,
                ALL_CHARACTERS_EXCLUDING_SELF_INCLUDING_BACKUP
        );
        assertThat(allCharactersExcludingSelfIncludingBackup.stream().map(Combatant::getId))
                .containsExactlyInAnyOrder(SERVANT_1, SERVANT_3, SERVANT_4, SERVANT_5, SERVANT_6);
    }
}
