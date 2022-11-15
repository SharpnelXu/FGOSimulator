package yome.fgo.simulator.gui.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import yome.fgo.data.reader.AtlasDataConverter;
import yome.fgo.simulator.SimulatorMain;
import yome.fgo.simulator.gui.components.TranslationConverter;

import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public class AtlasDataConverterVBox extends VBox {
    public AtlasDataConverterVBox() {
        super(10);
        setPrefSize(800, 600);
        setPadding(new Insets(10));

        final Label dataTypeLabel = new Label(getTranslation(APPLICATION_SECTION, "Atlas Data Type"));
        final ChoiceBox<String> dataTypeChoices = new ChoiceBox<>();
        dataTypeChoices.setConverter(new TranslationConverter(APPLICATION_SECTION));
        dataTypeChoices.setItems(FXCollections.observableArrayList("Level"));
        dataTypeChoices.getSelectionModel().selectFirst();

        final Label resultFormatLabel = new Label(getTranslation(APPLICATION_SECTION, "Result Format"));
        final ChoiceBox<String> resultFormatChoices = new ChoiceBox<>();
        resultFormatChoices.setConverter(new TranslationConverter(APPLICATION_SECTION));
        resultFormatChoices.setItems(FXCollections.observableArrayList("Mooncell"));
        resultFormatChoices.getSelectionModel().selectFirst();

        final Button resetTranslationButton = new Button(getTranslation(APPLICATION_SECTION, "Reset Translation"));

        final HBox typeHBox = new HBox(10);
        typeHBox.setAlignment(Pos.CENTER_LEFT);
        typeHBox.getChildren().addAll(dataTypeLabel, dataTypeChoices, resultFormatLabel, resultFormatChoices, resetTranslationButton);

        final Label inputLabel = new Label(getTranslation(APPLICATION_SECTION, "Input"));
        final TextArea inputTextArea = new TextArea();

        final Label resultLabel = new Label(getTranslation(APPLICATION_SECTION, "Result"));
        final TextArea resultTextArea = new TextArea();
        VBox.setVgrow(resultTextArea, Priority.ALWAYS);

        final Label errorLabel = new Label();

        final Button convertButton = new Button(getTranslation(APPLICATION_SECTION, "Convert"));
        convertButton.setOnAction(e -> {
            try {
                if (!inputTextArea.getText().isBlank()) {
                    resultTextArea.clear();
                    resultTextArea.setText(AtlasDataConverter.convertAtlasData(inputTextArea.getText()));
                }
            } catch (final JsonProcessingException ex) {
                errorLabel.setText(getTranslation(APPLICATION_SECTION, "Error parsing input"));
            }
        });

        final Button clearButton = new Button(getTranslation(APPLICATION_SECTION, "Clear"));
        clearButton.setOnAction(e -> {
            inputTextArea.clear();
            resultTextArea.clear();
        });

        resetTranslationButton.setOnAction(e -> {
            final String temp1 = inputTextArea.getText();
            inputTextArea.clear();
            clearButton.setDisable(true);
            convertButton.setDisable(true);
            SimulatorMain.loadOptions();
            clearButton.setDisable(false);
            convertButton.setDisable(false);
            // Need this to refresh cache I think
            inputTextArea.setText(temp1);
        });


        final HBox buttonHBox = new HBox(10);
        buttonHBox.setAlignment(Pos.CENTER_RIGHT);
        buttonHBox.getChildren().addAll(errorLabel, convertButton, clearButton);

        getChildren().addAll(typeHBox, inputLabel, inputTextArea, resultLabel, resultTextArea, buttonHBox);
    }
}
