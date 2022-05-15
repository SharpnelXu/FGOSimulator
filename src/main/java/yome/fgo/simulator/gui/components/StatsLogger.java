package yome.fgo.simulator.gui.components;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import lombok.Getter;
import yome.fgo.simulator.models.effects.Effect;
import yome.fgo.simulator.models.effects.buffs.Buff;

import java.util.ArrayList;
import java.util.List;

import static yome.fgo.simulator.gui.components.StatsLogger.LogLevel.ACTION;
import static yome.fgo.simulator.gui.components.StatsLogger.LogLevel.DEBUG;
import static yome.fgo.simulator.gui.components.StatsLogger.LogLevel.EFFECT;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.BUFF_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.EFFECT_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.ENTITY_NAME_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public class StatsLogger extends VBox {

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

    public void logEffect(final String id, final Class<? extends Effect> aClass) {
        final String message = String.format(
                getTranslation(APPLICATION_SECTION, "%s activates %s"),
                getTranslation(ENTITY_NAME_SECTION, id),
                getTranslation(EFFECT_SECTION, aClass.getSimpleName())
        );

        appendLogEntry(EFFECT, message);
    }

    public void logTurnStart(final int currentTurn) {
        final String message = String.format(getTranslation(APPLICATION_SECTION, "Turn %d Start"), currentTurn);
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
