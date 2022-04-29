package yome.fgo.simulator.models.effects.buffs;

import lombok.experimental.SuperBuilder;

import static yome.fgo.data.proto.FgoStorageData.Traits.BURNING_LOVE;

@SuperBuilder
public class BurningLove extends GrantTrait {
    @Override
    public String getTrait() {
        return BURNING_LOVE.name();
    }

    @Override
    public boolean commonBuffCondition() {
        return false;
    }

    @Override
    public boolean commonDebuffCondition() {
        return true;
    }

    @Override
    protected boolean commonStackableCondition() {
        return true;
    }
}
