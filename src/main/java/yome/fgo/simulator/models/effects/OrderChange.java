package yome.fgo.simulator.models.effects;

import lombok.experimental.SuperBuilder;
import yome.fgo.data.proto.FgoStorageData.SpecialActivationParams;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.Servant;

import java.util.List;

import static yome.fgo.data.proto.FgoStorageData.SpecialActivationTarget.ORDER_CHANGE;

@SuperBuilder
public class OrderChange extends Effect {

    @Override
    protected void internalApply(final Simulation simulation, final int level) {
        if (!shouldApply(simulation)) {
            return;
        }
        simulation.requestSpecialActivationTarget(
                SpecialActivationParams.newBuilder()
                        .setSpecialTarget(ORDER_CHANGE)
                        .build()
        );
        final List<Integer> targets = simulation.getOrderChangeTargets();
        final int onFieldIndex = targets.get(0);
        final int backupIndex = targets.get(1);
        final Servant servantOnField = simulation.getCurrentServants().get(onFieldIndex);
        final Servant servantInBackup = simulation.getBackupServants().get(backupIndex);

        simulation.getBackupServants().set(backupIndex, servantOnField);
        simulation.getCurrentServants().set(onFieldIndex, servantInBackup);

        servantInBackup.enterField(simulation);
        servantOnField.leaveField(simulation);
    }
}
