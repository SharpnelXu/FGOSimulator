package yome.fgo.simulator.models.conditions;

import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Combatant;

public class BackupServantsExist implements Condition {
    public static final BackupServantsExist BACKUP_SERVANTS_EXIST = new BackupServantsExist();

    private BackupServantsExist() {}

    @Override
    public boolean evaluate(final Simulation simulation) {
        for (final Combatant combatant : simulation.backupServants) {
            if (combatant.isSelectable()) {
                return true;
            }
        }

        return false;
    }
}
