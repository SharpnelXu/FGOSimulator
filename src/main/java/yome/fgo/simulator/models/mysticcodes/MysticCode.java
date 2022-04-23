package yome.fgo.simulator.models.mysticcodes;

import yome.fgo.data.proto.FgoStorageData.MysticCodeData;
import yome.fgo.data.proto.FgoStorageData.MysticCodeOption;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.combatants.ActiveSkill;

import java.util.List;
import java.util.stream.Collectors;

public class MysticCode {
    public final List<ActiveSkill> activeSkills;

    public MysticCode(final MysticCodeData mysticCodeData, final MysticCodeOption mysticCodeOption) {
        this.activeSkills = mysticCodeData.getActiveSkillDataList()
                .stream()
                .map(activeSkillData -> new ActiveSkill(activeSkillData, mysticCodeOption.getMysticCodeLevel()))
                .collect(Collectors.toList());
    }


    public void activateSkill(final Simulation simulation, final int skillIndex) {
        activeSkills.get(skillIndex).activate(simulation);
    }
}
