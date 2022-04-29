package yome.fgo.simulator.models.effects.buffs;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;

@SuperBuilder
@Getter
public class NpCardTypeChange extends Buff {
    private final CommandCardType commandCardType;

    @Override
    public boolean commonBuffCondition() {
        return true;
    }

    @Override
    public boolean commonDebuffCondition() {
        return false;
    }

    @Override
    protected boolean commonStackableCondition() {
        return false;
    }
}
