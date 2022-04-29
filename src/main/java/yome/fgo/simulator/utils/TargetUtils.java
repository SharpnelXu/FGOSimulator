package yome.fgo.simulator.utils;

import com.google.common.collect.ImmutableList;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TargetUtils {
    public static List<Combatant> getTargets(final Simulation simulation, final Target target) {
        final ImmutableList.Builder<Combatant> targets = ImmutableList.builder();
        switch (target) {
            case ACTIVATOR:
            case SELF:
                targets.add(simulation.getActivator());
                break;
            case FIRST_ENEMY:
                targets.add(simulation.getFirstAliveEnemy());
                break;
            case FIRST_ALLY_EXCLUDING_SELF:
                simulation.getAliveAllies()
                        .stream()
                        .filter(ally -> ally != simulation.getActivator() && ally.isSelectable())
                        .findFirst()
                        .ifPresent(targets::add);
                break;
            case TARGETED_ALLY:
                targets.add(simulation.getTargetedAlly());
                break;
            case TARGETED_ENEMY:
                targets.add(simulation.getTargetedEnemy());
                break;
            case NON_TARGETED_ALLIES:
                targets.addAll(simulation.getAliveAllies()
                                       .stream()
                                       .filter(ally -> ally != simulation.getTargetedAlly())
                                       .collect(Collectors.toList()));
                break;
            case NON_TARGETED_ENEMIES:
                targets.addAll(simulation.getAliveEnemies()
                                       .stream()
                                       .filter(enemy -> enemy != simulation.getTargetedEnemy())
                                       .collect(Collectors.toList()));
                break;
            case ALL_ALLIES:
                targets.addAll(simulation.getAliveAllies());
                break;
            case ALL_ALLIES_EXCLUDING_SELF:
                targets.addAll(simulation.getAliveAllies()
                                       .stream()
                                       .filter(ally -> ally != simulation.getActivator())
                                       .collect(Collectors.toList()));
                break;
            case ALL_ALLIES_INCLUDING_BACKUP:
                targets.addAll(simulation.getAliveAllies());
                targets.addAll(simulation.getBackupServants());
                break;
            case ALL_ALLIES_EXCLUDING_SELF_INCLUDING_BACKUP:
                final List<Combatant> allAllies = new ArrayList<>();
                allAllies.addAll(simulation.getAliveAllies());
                allAllies.addAll(simulation.getBackupServants());
                allAllies.remove(simulation.getActivator());
                targets.addAll(allAllies);
                break;
            case ALL_ENEMIES:
                targets.addAll(simulation.getAliveEnemies());
                break;
            case ALL_ENEMIES_INCLUDING_BACKUP:
                targets.addAll(simulation.getAliveEnemies());
                targets.addAll(simulation.getBackupEnemies());
                break;
            case ALL_CHARACTERS:
                targets.addAll(simulation.getAliveEnemies());
                targets.addAll(simulation.getAliveAllies());
                break;
            case ALL_CHARACTERS_EXCLUDING_SELF:
                final List<Combatant> allCharacters = new ArrayList<>(simulation.getAliveAllies());
                allCharacters.remove(simulation.getActivator());
                targets.addAll(allCharacters);
                targets.addAll(simulation.getAliveEnemies());
                break;
            case ALL_CHARACTERS_INCLUDING_BACKUP:
                targets.addAll(simulation.getAliveEnemies());
                targets.addAll(simulation.getAliveAllies());
                targets.addAll(simulation.getBackupEnemies());
                targets.addAll(simulation.getBackupServants());
                break;
            case ALL_CHARACTERS_EXCLUDING_SELF_INCLUDING_BACKUP:
                final List<Combatant> allAllyServants = new ArrayList<>();
                allAllyServants.addAll(simulation.getBackupServants());
                allAllyServants.addAll(simulation.getAliveAllies());
                allAllyServants.remove(simulation.getActivator());

                targets.addAll(allAllyServants);
                targets.addAll(simulation.getAliveEnemies());
                targets.addAll(simulation.getBackupEnemies());
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
