package yome.fgo.simulator.gui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import yome.fgo.data.proto.FgoStorageData.CommandCardOption;
import yome.fgo.data.proto.FgoStorageData.CommandCardType;

import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public class CommandCardOptionBox extends VBox {
    private final Slider strengthenSlider;
    private final TextField commandCodeText;

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
        commandCodeText = new TextField();
        HBox.setHgrow(commandCodeText, Priority.ALWAYS);
        final HBox commandCodeHBox = new HBox();
        commandCodeHBox.setSpacing(10);
        commandCodeHBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        commandCodeHBox.setAlignment(Pos.CENTER_LEFT);
        commandCodeHBox.getChildren().addAll(commandCodeLabel, commandCodeText);

        getChildren().addAll(strengthenHBox, commandCodeHBox);
    }

    public CommandCardOptionBox(final CommandCardOption source) {
        this();
        strengthenSlider.setValue(source.getStrengthen() / 20);
        commandCodeText.setText(source.getCommandCode());
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

        if (!commandCodeText.getText().trim().isEmpty()) {
            builder.setHasCommandCode(true)
                    .setCommandCode(commandCodeText.getText().trim());
        }

        return builder.build();
    }
}
