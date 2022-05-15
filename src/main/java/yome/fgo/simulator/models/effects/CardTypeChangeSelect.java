package yome.fgo.simulator.models.effects;

import lombok.experimental.SuperBuilder;
import yome.fgo.data.proto.FgoStorageData.BuffData;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.effects.buffs.Buff;
import yome.fgo.simulator.models.effects.buffs.BuffFactory;

@SuperBuilder
public class CardTypeChangeSelect extends GrantBuff {
    private CommandCardType selectedCardType;

    @Override
    protected Buff buildBuff(final int level) {
        final BuffData buffDataToUse = isBuffOvercharged ?
                buffData.get(level - 1) :
                buffData.get(0);
        return BuffFactory.buildBuff(buffDataToUse.toBuilder().setStringValue(selectedCardType.name()).build(), buffLevel);
    }

    @Override
    protected void internalApply(final Simulation simulation, final int level) {
        selectedCardType = simulation.selectCommandCardType();
        super.internalApply(simulation, level);
    }
}
