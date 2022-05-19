package yome.fgo.simulator.utils;

import com.google.common.collect.ImmutableList;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class TargetUtils {
    public static List<Combatant> getTargets(final Simulation simulation, final Target target) {
        final ImmutableList.Builder<Combatant> targets = ImmutableList.builder();
        final Combatant activator = simulation.hasActivator() ? simulation.getActivator() : null;
        final boolean isAlly = activator == null || activator.isAlly();
        final Collection<? extends Combatant> backupAllies = isAlly ? simulation.getBackupServants() : simulation.getBackupEnemies();
        final List<? extends Combatant> aliveAllies = isAlly ? simulation.getAliveAllies() : simulation.getAliveEnemies();
        final Combatant targetedAlly = isAlly ? simulation.getTargetedAlly() : simulation.getTargetedEnemy();

        final Collection<? extends Combatant> backupEnemies = isAlly ? simulation.getBackupEnemies() : simulation.getBackupServants();
        final List<? extends Combatant> aliveEnemies = isAlly ? simulation.getAliveEnemies() : simulation.getAliveAllies();
        final Combatant targetedEnemy = isAlly ? simulation.getTargetedEnemy() : simulation.getTargetedAlly();

        switch (target) {
            case ACTIVATOR:
            case SELF:
                targets.add(simulation.getActivator());
                break;
            case FIRST_ENEMY:
                if (!aliveEnemies.isEmpty()) {
                    targets.add(aliveEnemies.get(0));
                }
                break;
            case LAST_ENEMY:
                if (!aliveEnemies.isEmpty()) {
                    targets.add(aliveEnemies.get(aliveEnemies.size() - 1));
                }
                break;
            case FIRST_ALLY_EXCLUDING_SELF:
                final List<? extends Combatant> nonActivators = aliveAllies.stream()
                        .filter(ally -> ally != activator)
                        .collect(Collectors.toList());
                if (!nonActivators.isEmpty()) {
                    targets.add(nonActivators.get(0));
                }
                break;
            case LAST_ALLY_EXCLUDING_SELF:
                final List<? extends Combatant> nonActivators2 = aliveAllies.stream()
                        .filter(ally -> ally != activator)
                        .collect(Collectors.toList());
                if (!nonActivators2.isEmpty()) {
                    targets.add(nonActivators2.get(nonActivators2.size() - 1));
                }
                break;
            case TARGETED_ALLY:
                if (targetedAlly != null) {
                    targets.add(targetedAlly);
                }
                break;
            case TARGETED_ENEMY:
                if (targetedEnemy != null) {
                    targets.add(targetedEnemy);
                }
                break;
            case NON_TARGETED_ALLIES:
                targets.addAll(aliveAllies.stream().filter(ally -> ally != targetedAlly).collect(Collectors.toList()));
                break;
            case NON_TARGETED_ENEMIES:
                targets.addAll(aliveEnemies.stream().filter(enemy -> enemy != targetedEnemy).collect(Collectors.toList()));
                break;
            case ALL_ALLIES:
                targets.addAll(aliveAllies);
                break;
            case ALL_ALLIES_EXCLUDING_SELF:
                targets.addAll(aliveAllies.stream().filter(ally -> ally != activator).collect(Collectors.toList()));
                break;
            case ALL_ALLIES_INCLUDING_BACKUP:
                targets.addAll(aliveAllies);
                targets.addAll(backupAllies);
                break;
            case ALL_ALLIES_EXCLUDING_SELF_INCLUDING_BACKUP:
                final List<Combatant> allyList = new ArrayList<>();
                allyList.addAll(aliveAllies);
                allyList.addAll(backupAllies);
                allyList.remove(activator);
                targets.addAll(allyList);
                break;
            case ALL_ENEMIES:
                targets.addAll(aliveEnemies);
                break;
            case ALL_ENEMIES_INCLUDING_BACKUP:
                targets.addAll(aliveEnemies);
                targets.addAll(backupEnemies);
                break;
            case ALL_CHARACTERS:
                targets.addAll(aliveEnemies);
                targets.addAll(aliveAllies);
                break;
            case ALL_CHARACTERS_EXCLUDING_SELF:
                final List<Combatant> allCharacters = new ArrayList<>(aliveAllies);
                allCharacters.remove(activator);
                targets.addAll(allCharacters);
                targets.addAll(aliveEnemies);
                break;
            case ALL_CHARACTERS_INCLUDING_BACKUP:
                targets.addAll(aliveEnemies);
                targets.addAll(aliveAllies);
                targets.addAll(backupEnemies);
                targets.addAll(backupAllies);
                break;
            case ALL_CHARACTERS_EXCLUDING_SELF_INCLUDING_BACKUP:
                final List<Combatant> allAllyServants = new ArrayList<>();
                allAllyServants.addAll(backupAllies);
                allAllyServants.addAll(aliveAllies);
                allAllyServants.remove(activator);

                targets.addAll(allAllyServants);
                targets.addAll(aliveEnemies);
                targets.addAll(backupEnemies);
                break;
            case ATTACKER:
                targets.add(simulation.getAttacker());
                break;
            case DEFENDER:
                targets.add(simulation.getDefender());
                break;
            case EFFECT_TARGET:
                targets.add(simulation.getEffectTarget());
                break;
            case SERVANT_EXCHANGE:
                throw new UnsupportedOperationException("pending implementation");
            case UNRECOGNIZED:
                throw new IllegalArgumentException("Invalid effect target: " + target);
            case NONE:
            default:
                break;
        }
        return targets.build();
    }
}
