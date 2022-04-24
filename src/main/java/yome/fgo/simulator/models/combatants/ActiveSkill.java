package yome.fgo.simulator.models.combatants;

import lombok.Getter;
import yome.fgo.data.proto.FgoStorageData.ActiveSkillData;
import yome.fgo.simulator.models.Simulation;

@Getter
public class ActiveSkill extends Skill {
    private final int maxCoolDown;
    private int currentCoolDown;

    public ActiveSkill(final ActiveSkillData activeSkillData, final int activeSkillLevel) {
        super(activeSkillData.getEffectsList(), activeSkillLevel);
        this.maxCoolDown = activeSkillData.getBaseCoolDown() - activeSkillLevel / 5;
    }

    public boolean canActivate() {
        return currentCoolDown == 0;
    }

    @Override
    public void activate(final Simulation simulation) {
        super.activate(simulation);
        currentCoolDown = maxCoolDown;
    }

    public void decreaseCoolDown(final int decrease) {
        currentCoolDown -= decrease;
        if (currentCoolDown < 0) {
            currentCoolDown = 0;
        }
    }
}
