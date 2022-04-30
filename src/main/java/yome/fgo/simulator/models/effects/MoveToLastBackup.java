package yome.fgo.simulator.models.effects;

import lombok.experimental.SuperBuilder;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Servant;

@SuperBuilder
public class MoveToLastBackup extends Effect {
    public static final MoveToLastBackup MOVE_TO_LAST_BACKUP = MoveToLastBackup.builder().build();

    @Override
    protected void internalApply(final Simulation simulation, final int level) {
        if (simulation.getCurrentServants().size() == 1 || !(simulation.getActivator() instanceof Servant)) {
            return;
        }
        final Servant servant = (Servant) simulation.getActivator();
        final int servantIndex = simulation.getCurrentServants().indexOf(servant);
        if (servantIndex != -1) {
            simulation.getBackupServants().push((Servant) simulation.getActivator());
            simulation.getCurrentServants().set(servantIndex, null);
        }
    }
}
