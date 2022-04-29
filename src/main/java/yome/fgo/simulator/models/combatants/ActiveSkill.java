package yome.fgo.simulator.models.combatants;

import lombok.Getter;
import yome.fgo.data.proto.FgoStorageData.ActiveSkillData;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.conditions.Condition;
import yome.fgo.simulator.models.conditions.ConditionFactory;

import static yome.fgo.simulator.models.conditions.Always.ALWAYS;

@Getter
public class ActiveSkill extends Skill {
    private final int maxCoolDown;
    private int currentCoolDown;
    private final Condition activationCondition;

    public ActiveSkill(final ActiveSkillData activeSkillData, final int activeSkillLevel) {
        super(activeSkillData.getEffectsList(), activeSkillLevel);
        this.maxCoolDown = activeSkillData.getBaseCoolDown() - activeSkillLevel / 5;

        if (activeSkillData.hasActivationCondition()) {
            this.activationCondition = ConditionFactory.buildCondition(activeSkillData.getActivationCondition());
        } else {
            this.activationCondition = ALWAYS;
        }
    }

    public boolean canActivate(final Simulation simulation) {
        return currentCoolDown == 0 && activationCondition.evaluate(simulation);
    }

    @Override
    public void activate(final Simulation simulation) {
        super.activate(simulation);
        currentCoolDown = maxCoolDown;
    }

    public void setCurrentCoolDown(final int coolDown) {
        currentCoolDown = coolDown;
    }

    public void decreaseCoolDown(final int decrease) {
        currentCoolDown -= decrease;
        if (currentCoolDown < 0) {
            currentCoolDown = 0;
        }
    }
}
