package yome.fgo.simulator.models.combatants;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yome.fgo.data.proto.FgoStorageData.Attribute;
import yome.fgo.data.proto.FgoStorageData.CombatantData;
import yome.fgo.data.proto.FgoStorageData.EnemyData;
import yome.fgo.data.proto.FgoStorageData.FateClass;
import yome.fgo.simulator.models.Simulation;
import yome.fgo.simulator.models.effects.buffs.Buff;
import yome.fgo.simulator.models.effects.buffs.EffectActivatingBuff;
import yome.fgo.simulator.models.effects.buffs.EndOfTurnEffect;
import yome.fgo.simulator.models.effects.buffs.GrantTrait;
import yome.fgo.simulator.models.effects.buffs.ValuedBuff;
import yome.fgo.simulator.utils.RoundUtils;

import java.util.ArrayList;
import java.util.List;

import static yome.fgo.simulator.utils.FateClassUtils.getClassMaxNpGauge;

@NoArgsConstructor
@Getter
public class Combatant {
    // enemy only data
    private int maxNpGauge;
    private int currentNpGauge;
    private int currentHpBarIndex;
    private int cumulativeTurnDamage;

    protected CombatantData combatantData;

    protected String id;
    protected int currentHp;
    protected List<Integer> hpBars;
    protected List<Buff> buffs = new ArrayList<>();

    // for testing
    public Combatant(final String id) {
        this.id = id;
    }

    // for testing
    public Combatant(final String id, final List<Integer> hpBars) {
        if (hpBars.isEmpty()) {
            throw new IllegalArgumentException("Empty hpBars");
        }
        for (final int hpBar : hpBars) {
            if (hpBar <= 0) {
                throw new IllegalArgumentException("Invalid hpBar: " + hpBar + " in " + hpBars);
            }
        }

        this.id = id;
        this.hpBars = new ArrayList<>(hpBars);
        this.currentHp = this.hpBars.get(this.currentHpBarIndex);
    }

    // for servant
    public Combatant(final String id, final CombatantData combatantData) {
        this.id = id;
        this.combatantData = combatantData;
    }

    public Combatant(final CombatantData combatantData, final EnemyData enemyData) {
        this(enemyData.getEnemyBaseId(), enemyData.getHpBarsList());

        if (enemyData.hasCombatantDataOverride()) {
            final CombatantData override = enemyData.getCombatantDataOverride();
            final CombatantData.Builder builder = combatantData.toBuilder();
            builder.mergeFrom(override);
            builder.clearAlignments();
            builder.addAllAlignments(override.getAlignmentsCount() == 0
                                             ? combatantData.getAlignmentsList()
                                             : override.getAlignmentsList());
            builder.clearTraits();
            builder.addAllTraits(override.getTraitsCount() == 0
                                         ? combatantData.getTraitsList()
                                         : override.getTraitsList());
            this.combatantData = builder.build();
        } else {
            this.combatantData = combatantData;
        }
        if (enemyData.getHasCustomMaxNpGauge()) {
            this.maxNpGauge = enemyData.getCustomMaxNpGauge();
        } else {
            this.maxNpGauge = getClassMaxNpGauge(this.combatantData.getFateClass());
        }
    }

    public void initiate(final Simulation simulation) {
    }

    public int getAttack() {
        // enemy don't have attack
        return 0;
    }

    public FateClass getFateClass() {
        return combatantData.getFateClass();
    }

    public Attribute getAttribute() {
        return combatantData.getAttribute();
    }

    public List<String> getAllTraits(final Simulation simulation) {
        final ImmutableList.Builder<String> allTraits = ImmutableList.builder();
        allTraits.addAll(combatantData.getTraitsList());
        for (final Buff buff : buffs) {
            if (buff instanceof GrantTrait && buff.shouldApply(simulation)) {
                allTraits.add(((GrantTrait) buff).getTrait());
                buff.setApplied();
            }
        }

        return allTraits.build();
    }

    public boolean getUndeadNpCorrection() {
        return combatantData.getUndeadNpCorrection();
    }

    public int getMaxHp() {
        return hpBars.get(currentHpBarIndex);
    }

    public boolean hasNextHpBar() {
        return currentHpBarIndex < hpBars.size() - 1;
    }

    public void addBuff(final Buff buff) {
        buffs.add(buff);
    }

    public double applyBuff(final Simulation simulation, final Class<? extends ValuedBuff> buffClass) {
        double totalValue = 0;
        for (final Buff buff : buffs) {
            if (buffClass.isInstance(buff) && buff.shouldApply(simulation)) {
                totalValue += buffClass.cast(buff).getValue(simulation);
                buff.setApplied();
            }
        }
        return RoundUtils.roundNearest(totalValue);
    }

    public boolean consumeBuffIfExist(final Simulation simulation, final Class<? extends Buff> buffClass) {
        for (final Buff buff : buffs) {
            if (buffClass.isInstance(buff) && buff.shouldApply(simulation)) {
                buff.setApplied();
                return true;
            }
        }
        return false;
    }

    public boolean isAlreadyDead() {
        return currentHp <= 0;
    }

    public boolean isBuggedOverkill() {
        return getCumulativeTurnDamage() > getCurrentHp() && !hasNextHpBar();
    }

    public void addCumulativeTurnDamage(final int damage) {
        cumulativeTurnDamage += damage;
    }

    public void clearCumulativeTurnDamage() {
        cumulativeTurnDamage = 0;
    }

    public void enterField(final Simulation simulation) {
        // TODO: implement
    }

    public void leaveField(final Simulation simulation) {
        // TODO: implement
    }

    public void receiveDamage(final int damage) {
        currentHp -= damage;
    }

    public void hpBarBreak() {
        currentHpBarIndex++;
        currentHp = hpBars.get(currentHpBarIndex);
    }

    public void endOfTurn(final Simulation simulation) {
        currentNpGauge++;
        if (currentNpGauge > maxNpGauge) {
            currentNpGauge = maxNpGauge;
        }

        activateEffectActivatingBuff(simulation, EndOfTurnEffect.class);

        for (final Buff buff : buffs) {
            buff.decreaseNumTurnsActive();
        }

        clearInactiveBuff();
    }

    public void clearInactiveBuff() {
        for (int j = buffs.size() - 1; j >= 0; j--) {
            if (buffs.get(j).isInactive()) {
                buffs.remove(j);
            }
        }
    }

    public void activateEffectActivatingBuff(
            final Simulation simulation,
            final Class<? extends EffectActivatingBuff> buffClass
    ) {
        for (final Buff buff : buffs) {
            if (buffClass.isInstance(buff) && buff.shouldApply(simulation)) {
                simulation.setActivator(this);
                ((EffectActivatingBuff) buff).activate(simulation);
                buff.setApplied();
                simulation.setActivator(null);
            }
        }
    }

    public boolean activateGuts(final Simulation simulation) {
        // TODO: implement
        return false;
    }

    public void changeNp(final double percentNpChange) {
//        final int gaugeChange = (int) Math.ceil(Math.abs(percentNpChange) / 50);
//        final int changeSign = (int) Math.signum(percentNpChange);
//        currentNpGauge += gaugeChange * changeSign;
    }
}
