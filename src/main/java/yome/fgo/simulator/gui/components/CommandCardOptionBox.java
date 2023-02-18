package yome.fgo.simulator.gui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import yome.fgo.data.proto.FgoStorageData.CommandCardOption;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;
import yome.fgo.simulator.gui.creators.EntitySelector;

import java.io.IOException;

import static yome.fgo.simulator.gui.helpers.ComponentUtils.createInfoImageView;
import static yome.fgo.simulator.gui.helpers.ComponentUtils.createTooltip;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.ENTITY_NAME_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public class CommandCardOptionBox extends VBox {
    private final Slider strengthenSlider;
    private final Label commandCodeNameLabel;

    private String selectedCommandCode;

    public CommandCardOptionBox() {
        super();
        setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        setSpacing(10);
        setPadding(new Insets(5, 5, 5, 5));

        final Label strengthenLabel = new Label(getTranslation(APPLICATION_SECTION, "Command Card Status Up"));
        final Label strengthenValueLabel = new Label("0");
        strengthenValueLabel.setMinWidth(50);
        strengthenSlider = new Slider();
        strengthenSlider.setMin(0);
        strengthenSlider.setMax(25);
        strengthenSlider.setBlockIncrement(1);
        strengthenSlider.setMajorTickUnit(5);
        strengthenSlider.setMinorTickCount(0);
        strengthenSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    final int intValue = newValue.intValue();
                    strengthenSlider.setValue(intValue);
                    strengthenValueLabel.setText(Integer.toString(intValue * 20));
                }
        );
        strengthenSlider.setShowTickMarks(true);
        HBox.setHgrow(strengthenSlider, Priority.ALWAYS);
        final HBox strengthenHBox = new HBox();
        strengthenHBox.setSpacing(10);
        strengthenHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        strengthenHBox.setAlignment(Pos.CENTER_LEFT);
        strengthenHBox.getChildren().addAll(strengthenLabel, strengthenValueLabel, strengthenSlider);

        final Label commandCodeLabel = new Label(getTranslation(APPLICATION_SECTION, "Command Code"));

        final Button editCCButton = new Button();
        editCCButton.setGraphic(createInfoImageView("edit"));
        editCCButton.setTooltip(createTooltip(getTranslation(APPLICATION_SECTION, "SelectCommandCode")));

        final Button removeCCButton = new Button();
        removeCCButton.setGraphic(createInfoImageView("remove"));
        removeCCButton.setTooltip(createTooltip(getTranslation(APPLICATION_SECTION, "RemoveCommandCode")));

        this.commandCodeNameLabel = new Label(getTranslation(APPLICATION_SECTION, "Empty"));
        HBox.setHgrow(this.commandCodeNameLabel, Priority.ALWAYS);

        editCCButton.setOnAction(e -> {
            try {
                final CommandCodeDataAnchorPane selection = EntitySelector.selectCommandCode(this.getScene().getWindow());
                if (selection != null) {
                    selectedCommandCode = selection.getCommandCodeData().getId();
                    commandCodeNameLabel.setText(getTranslation(ENTITY_NAME_SECTION, selectedCommandCode));
                }
            } catch (final IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        removeCCButton.setOnAction(e -> {
            selectedCommandCode = null;
            commandCodeNameLabel.setText(getTranslation(APPLICATION_SECTION, "Empty"));
        });

        final HBox commandCodeHBox = new HBox();
        commandCodeHBox.setSpacing(10);
        commandCodeHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        commandCodeHBox.setAlignment(Pos.CENTER_LEFT);
        commandCodeHBox.getChildren().addAll(commandCodeLabel, editCCButton, removeCCButton, this.commandCodeNameLabel);

        getChildren().addAll(strengthenHBox, commandCodeHBox);
    }

    public CommandCardOptionBox(final CommandCardOption source) {
        this();
        strengthenSlider.setValue(source.getStrengthen() / 20);
        if (source.getHasCommandCode()) {
            selectedCommandCode = source.getCommandCode();
            commandCodeNameLabel.setText(getTranslation(ENTITY_NAME_SECTION, source.getCommandCode()));
        }
    }

    public void setType(final CommandCardType commandCardType) {
        switch (commandCardType) {
            case BUSTER ->
                    setStyle("-fx-border-color:red; -fx-background-color: rgba(255,0,0,0.3); -fx-border-width: 2");
            case ARTS ->
                    setStyle("-fx-border-color:blue; -fx-background-color: rgba(0,0,255,0.3); -fx-border-width: 2");
            case QUICK ->
                    setStyle("-fx-border-color:green; -fx-background-color: rgba(0,255,0,0.3); -fx-border-width: 2");
            default -> setStyle(
                    "-fx-border-color:#bdbdbd; -fx-background-color: rgba(255,255,255,0.94); -fx-border-width: 2");
        }
    }

    public CommandCardOption build() {
        final CommandCardOption.Builder builder = CommandCardOption.newBuilder()
                .setStrengthen((int) strengthenSlider.getValue() * 20);

        if (selectedCommandCode != null) {
            builder.setHasCommandCode(true).setCommandCode(selectedCommandCode);
        }

        return builder.build();
    }
}
