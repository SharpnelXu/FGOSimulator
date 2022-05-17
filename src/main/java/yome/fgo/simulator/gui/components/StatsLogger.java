package yome.fgo.simulator.gui.components;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import lombok.Getter;
import yome.fgo.simulator.models.combatants.CommandCard;
import yome.fgo.simulator.models.combatants.NoblePhantasm;
import yome.fgo.simulator.models.effects.buffs.Buff;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static yome.fgo.simulator.gui.components.StatsLogger.LogLevel.ACTION;
import static yome.fgo.simulator.gui.components.StatsLogger.LogLevel.DEBUG;
import static yome.fgo.simulator.gui.components.StatsLogger.LogLevel.EFFECT;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.BUFF_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.COMMAND_CARD_TYPE_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.ENTITY_NAME_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public class StatsLogger extends VBox {

    public void logDamageParameter(final String message) {
        appendLogEntry(DEBUG, message);
    }

    public void logProbability(final String id, final double activationProbability, final double probabilityThreshold) {
        final NumberFormat numberFormat = NumberFormat.getPercentInstance();
        numberFormat.setMaximumFractionDigits(2);
        final String message = String.format(
                getTranslation(APPLICATION_SECTION, "Target: %s, effect probability: %s, probability threshold: %s"),
                getTranslation(ENTITY_NAME_SECTION, id),
                numberFormat.format(activationProbability),
                numberFormat.format(probabilityThreshold)
        );
        appendLogEntry(DEBUG, message);
    }

    public void logDoT(final String id, final int poisonDamage, final int burnDamage, final int curseDamage) {
        if (poisonDamage == 0 && burnDamage == 0 && curseDamage == 0) {
            return;
        }

        final String message = String.format(
                getTranslation(APPLICATION_SECTION, "%s receives DoT Damage: Burn %d, Curse %d, Poison %d"),
                getTranslation(ENTITY_NAME_SECTION, id),
                burnDamage,
                curseDamage,
                poisonDamage
        );
        appendLogEntry(EFFECT, message);
    }

    public void logDeath(final String id) {
        final String message = String.format(getTranslation(APPLICATION_SECTION, "%s Exit Field"), getTranslation(ENTITY_NAME_SECTION, id));

        appendLogEntry(DEBUG, message);
    }

    public enum LogLevel {
        DEBUG(0),
        EFFECT(1),
        ACTION(2);

        private final int logLevel;

        LogLevel(final int i) {
            this.logLevel = i;
        }

        public int getLogLevelNum() {
            return logLevel;
        }
    }

    @Getter
    public static class LogEntry {
        private final LogLevel logLevel;
        private final Label label;

        public LogEntry(final LogLevel logLevel, final String message) {
            this.logLevel = logLevel;
            this.label = new Label(message);
            this.label.setWrapText(true);
        }
    }

    private final List<LogEntry> logEntries = new ArrayList<>();
    private LogLevel curLogLevel;

    public StatsLogger() {
        super();
        setSpacing(5);
        setStyle("-fx-background-color: white; -fx-border-color: grey; -fx-border-width: 3; -fx-border-radius: 3");
        setPadding(new Insets(10, 10, 10, 10));
        curLogLevel = DEBUG;
    }

    public void logEnterField(final String id) {
        final String message = String.format(getTranslation(APPLICATION_SECTION, "%s Enter Field"), getTranslation(ENTITY_NAME_SECTION, id));

        appendLogEntry(DEBUG, message);
    }

    public void logLeaveField(final String id) {
        final String message = String.format(getTranslation(APPLICATION_SECTION, "%s Leave Field"), getTranslation(ENTITY_NAME_SECTION, id));

        appendLogEntry(DEBUG, message);
    }

    public void logEffectActivatingBuff(final String id, final Class<? extends Buff> buffClass) {
        final String message = String.format(
                getTranslation(APPLICATION_SECTION, "%s activates %s"),
                getTranslation(ENTITY_NAME_SECTION, id),
                getTranslation(BUFF_SECTION, buffClass.getSimpleName())
        );

        appendLogEntry(EFFECT, message);
    }

    public void logEffect(final String effect) {
        appendLogEntry(EFFECT, effect);
    }

    public void logActivatePassiveSkill(final String id) {
        final String message = String.format(
                getTranslation(APPLICATION_SECTION, "%s activates passive skill"),
                getTranslation(ENTITY_NAME_SECTION, id)
        );

        appendLogEntry(EFFECT, message);
    }

    public void logActivateActiveSkill(final String id, final int skillIndex) {
        final String message = String.format(
                getTranslation(APPLICATION_SECTION, "%s activates Skill %d"),
                getTranslation(ENTITY_NAME_SECTION, id),
                skillIndex + 1
        );

        appendLogEntry(ACTION, message);
    }

    public void logTurnStart(final int currentTurn) {
        final String message = String.format(getTranslation(APPLICATION_SECTION, "Turn %d Start"), currentTurn);
        appendLogEntry(ACTION, message);
    }

    public void logCommandCardAction(
            final String attackerId,
            final String defenderId,
            final CommandCard currentCard,
            final int totalDamage,
            final double totalNp,
            final double totalCritStar,
            final int overkillCount,
            final int hits
    ) {
        final NumberFormat percentFormat = NumberFormat.getPercentInstance();
        final NumberFormat numberFormat = NumberFormat.getNumberInstance();
        percentFormat.setMaximumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);
        String commandCardString = getTranslation(COMMAND_CARD_TYPE_SECTION, currentCard.getCommandCardType().name());
        if (currentCard instanceof NoblePhantasm) {
            commandCardString += getTranslation(APPLICATION_SECTION, "NP Card");
        } else {
            commandCardString += getTranslation(APPLICATION_SECTION, "Command Card");
        }
        final List<String> commandCardAdditionString = new ArrayList<>();
        if (currentCard.getCommandCardStrengthen() > 0) {
            commandCardAdditionString.add("+" + currentCard.getCommandCardStrengthen());
        }
        if (currentCard.getCommandCodeData() != null) {
            commandCardAdditionString.add(getTranslation(ENTITY_NAME_SECTION, currentCard.getCommandCodeData().getId()));
        }
        if (!commandCardAdditionString.isEmpty()) {
            commandCardString += "(" + String.join(", ", commandCardAdditionString) + ")";
        }

        final String message = String.format(
                getTranslation(APPLICATION_SECTION, "%s executes %s on %s, total damage: %d, total NP: %s, total critical star: %s, overkill: %d/%d"),
                getTranslation(ENTITY_NAME_SECTION, attackerId),
                commandCardString,
                getTranslation(ENTITY_NAME_SECTION, defenderId),
                totalDamage,
                percentFormat.format(totalNp),
                numberFormat.format(totalCritStar),
                overkillCount,
                hits
        );
        appendLogEntry(ACTION, message);
    }

    private void appendLogEntry(final LogLevel logLevel, final String message) {
        final LogEntry logEntry = new LogEntry(logLevel, message);
        logEntries.add(logEntry);
        getChildren().add(logEntry.getLabel());
        if (curLogLevel.getLogLevelNum() > logLevel.getLogLevelNum()) {
            logEntry.getLabel().setManaged(false);
            logEntry.getLabel().setVisible(false);
        }
    }

    public void setLogLevel(final LogLevel logLevel) {
        curLogLevel = logLevel;
        for (final LogEntry logEntry : logEntries) {
            final boolean shouldShow = curLogLevel.getLogLevelNum() <= logEntry.getLogLevel().getLogLevelNum();

            logEntry.getLabel().setManaged(shouldShow);
            logEntry.getLabel().setVisible(shouldShow);
        }
    }
}
