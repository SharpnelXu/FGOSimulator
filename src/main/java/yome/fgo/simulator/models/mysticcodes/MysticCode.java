package yome.fgo.simulator.models.mysticcodes;

import yome.fgo.data.proto.FgoStorageData.Gender;
import yome.fgo.data.proto.FgoStorageData.MysticCodeData;
import yome.fgo.data.proto.FgoStorageData.MysticCodeOption;
import yome.fgo.data.proto.FgoStorageData.SpecialActivationParams;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.ActiveSkill;

import java.util.List;
import java.util.stream.Collectors;

public class MysticCode {
    public final List<ActiveSkill> activeSkills;
    public final MysticCodeData mysticCodeData;
    public final Gender gender;

    public MysticCode(final MysticCodeData mysticCodeData, final MysticCodeOption mysticCodeOption) {
        this.activeSkills = mysticCodeData.getActiveSkillDataList()
                .stream()
                .map(activeSkillData -> new ActiveSkill(activeSkillData, mysticCodeOption.getMysticCodeLevel()))
                .collect(Collectors.toList());
        this.mysticCodeData = mysticCodeData;
        this.gender = mysticCodeOption.getGender();
    }

    public String getSkillIconName(final int index) {
        return activeSkills.get(index).getIconPath(1);
    }

    public int getCurrentCoolDown(final int index) {
        return activeSkills.get(index).getCurrentCoolDown();
    }

    public boolean canActivateSkill(final Simulation simulation, final int index) {
        return activeSkills.get(index).canActivate(simulation, 1);
    }

    public SpecialActivationParams getActiveSkillSpecialTarget(final int index) {
        return activeSkills.get(index).getSpecialActivationParams(1);
    }

    public String getId() {
        return mysticCodeData.getId();
    }

    public void activateSkill(final Simulation simulation, final int skillIndex) {
        activeSkills.get(skillIndex).activate(simulation, 1);
    }
}
