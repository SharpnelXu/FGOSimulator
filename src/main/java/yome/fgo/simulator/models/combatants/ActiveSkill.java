package yome.fgo.simulator.models.combatants;

import lombok.Getter;
import yome.fgo.data.proto.FgoStorageData.ActiveSkillData;
import yome.fgo.data.proto.FgoStorageData.ActiveSkillUpgrades;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.conditions.Condition;
import yome.fgo.simulator.models.conditions.ConditionFactory;
import yome.fgo.simulator.models.effects.Effect;

import java.util.ArrayList;
import java.util.List;

import static yome.fgo.simulator.models.conditions.Always.ALWAYS;

@Getter
public class ActiveSkill {

    @Getter
    public static class ActiveSkillWrapper extends Skill {
        private final int maxCoolDown;
        private final Condition activationCondition;
        private final String iconName;

        public ActiveSkillWrapper(final ActiveSkillData activeSkillData, final int activeSkillLevel) {
            super(activeSkillData.getEffectsList(), activeSkillLevel);
            this.maxCoolDown = activeSkillData.getBaseCoolDown() - activeSkillLevel / 5;
            this.iconName = activeSkillData.getIconName();

            if (activeSkillData.hasActivationCondition()) {
                this.activationCondition = ConditionFactory.buildCondition(activeSkillData.getActivationCondition());
            } else {
                this.activationCondition = ALWAYS;
            }
        }
    }

    private final List<ActiveSkillWrapper> activeSkillWrapperList;

    private int currentCoolDown;

    public ActiveSkill(final ActiveSkillUpgrades activeSkillUpgrades, final int activeSkillLevel) {
        this.activeSkillWrapperList = new ArrayList<>();

        for (final ActiveSkillData activeSkillData : activeSkillUpgrades.getActiveSkillDataList()) {
            activeSkillWrapperList.add(new ActiveSkillWrapper(activeSkillData, activeSkillLevel));
        }
    }
    public ActiveSkill(final ActiveSkillData activeSkillData, final int activeSkillLevel) {
        this.activeSkillWrapperList = new ArrayList<>();
        activeSkillWrapperList.add(new ActiveSkillWrapper(activeSkillData, activeSkillLevel));
    }

    public ActiveSkillWrapper getSkillAtRankOrHighest(final int rank) {
        if (activeSkillWrapperList.size() >= rank) {
            return activeSkillWrapperList.get(rank - 1);
        } else {
            return activeSkillWrapperList.get(activeSkillWrapperList.size() - 1);
        }
    }

    public boolean canActivate(final Simulation simulation, final int rank) {
        final ActiveSkillWrapper skillAtRank = getSkillAtRankOrHighest(rank);
        return currentCoolDown == 0 && skillAtRank.activationCondition.evaluate(simulation);
    }

    public String getIconPath(final int rank) {
        return getSkillAtRankOrHighest(rank).iconName;
    }

    public void activate(final Simulation simulation, final int rank) {
        final ActiveSkillWrapper skillAtRank = getSkillAtRankOrHighest(rank);
        currentCoolDown = skillAtRank.maxCoolDown;
        for (final Effect effect: skillAtRank.effects) {
            effect.apply(simulation);
        }

        simulation.checkBuffStatus();
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

    private ActiveSkill(final ActiveSkill other) {
        this.activeSkillWrapperList = new ArrayList<>();
        this.activeSkillWrapperList.addAll(other.activeSkillWrapperList);
        this.currentCoolDown = other.currentCoolDown;
    }

    public ActiveSkill makeCopy() {
        return new ActiveSkill(this);
    }
}
