package yome.fgo.simulator.gui.components;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import yome.fgo.data.proto.FgoStorageData.ConditionData;

import java.io.IOException;

import static yome.fgo.simulator.gui.components.DataPrinter.printConditionData;
import static yome.fgo.simulator.gui.creators.ConditionBuilder.createCondition;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public class SubConditionCellFactory implements Callback<ListView<ConditionData>, ListCell<ConditionData>> {
    private final Label errorLabel;

    public SubConditionCellFactory(final Label errorLabel) {
        this.errorLabel = errorLabel;
    }

    @Override
    public ListCell<ConditionData> call(final ListView<ConditionData> param) {
        return new ListCell<>() {
            @Override
            public void updateItem(final ConditionData conditionData, final boolean empty) {
                super.updateItem(conditionData, empty);

                if (empty || conditionData == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(null);
                    final Button editButton = new Button(getTranslation(APPLICATION_SECTION, "Edit"));
                    editButton.setPrefWidth(55);

                    editButton.setOnAction(event -> {
                        try {
                            final ConditionData.Builder builder = conditionData.toBuilder();
                            createCondition(getListView().getScene().getWindow(), builder);

                            if (!builder.getType().isEmpty()) {
                                final int index = getListView().getItems().indexOf(conditionData);
                                getListView().getItems().set(index, builder.build());
                            }

                        } catch (final IOException e) {
                            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot start new window!") + e);
                            errorLabel.setVisible(true);
                        }
                    });

                    final Button removeButton = new Button(getTranslation(APPLICATION_SECTION, "Remove"));
                    removeButton.setPrefWidth(55);

                    removeButton.setOnAction(event -> getListView().getItems().remove(conditionData));

                    final Label label = new Label(printConditionData(conditionData));
                    label.setWrapText(true);
                    label.setMaxWidth(500);

                    final HBox hBox = new HBox();
                    hBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
                    hBox.setAlignment(Pos.CENTER_LEFT);
                    hBox.setSpacing(10);
                    hBox.getChildren().addAll(removeButton, editButton, label);

                    setGraphic(hBox);
                }
            }
        };
    }
}
