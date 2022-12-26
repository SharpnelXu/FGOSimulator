package yome.fgo.simulator.models.effects.buffs;

import lombok.experimental.SuperBuilder;
import yome.fgo.data.proto.FgoStorageData.Target;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.effects.ForceGrantBuff;
import yome.fgo.simulator.models.effects.ForceRemoveBuff;

import static yome.fgo.simulator.translation.TranslationManager.TARGET_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

@SuperBuilder
public class OnFieldEffect extends Buff {
    public static final String ON_FIELD_BUFF_MARK = "onFieldBuffMark";
    public enum OnFieldMode {
        ENTER_FIELD,
        LEAVE_FIELD,
        DEATH
    }

    private final Buff activatedBuffBase;
    private final Target target;
    private ForceGrantBuff forceGrantBuff;
    private ForceRemoveBuff forceRemoveBuff;

    public void activate(final Simulation simulation, final OnFieldMode onFieldMode) {
        switch (onFieldMode) {
            case ENTER_FIELD -> forceGrantBuff.apply(simulation);
            case LEAVE_FIELD, DEATH -> forceRemoveBuff.apply(simulation);
        }
        simulation.checkBuffStatus();
    }

    @Override
    public boolean commonBuffCondition() {
        return activatedBuffBase.commonBuffCondition();
    }

    @Override
    public boolean commonDebuffCondition() {
        return activatedBuffBase.commonDebuffCondition();
    }

    @Override
    protected boolean commonStackableCondition() {
        return true;
    }

    @Override
    public String toString() {
        String base = ": " + getTranslation(TARGET_SECTION, target.name()) + " : " + activatedBuffBase.toString();
        return super.toString() + base;
    }

    @Override
    public Buff makeCopy() {
        final OnFieldEffect copy = (OnFieldEffect) super.makeCopy();
        copy.forceGrantBuff = this.forceGrantBuff;
        copy.forceRemoveBuff = this.forceRemoveBuff;
        return copy;
    }
}
