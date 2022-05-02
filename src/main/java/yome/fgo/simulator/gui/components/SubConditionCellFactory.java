package yome.fgo.simulator.gui.components;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import yome.fgo.data.proto.FgoStorageData.ConditionData;

import java.io.IOException;

import static yome.fgo.simulator.gui.creators.ConditionBuilder.createCondition;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.CONDITION_SECTION;
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
                    editButton.setPrefWidth(75);
                    AnchorPane.setTopAnchor(editButton, 0.0);
                    AnchorPane.setBottomAnchor(editButton, 0.0);
                    AnchorPane.setRightAnchor(editButton, 85.0);

                    editButton.setOnAction(event -> {
                        try {
                            final ConditionData.Builder builder = conditionData.toBuilder();
                            createCondition(getListView().getScene().getWindow(), builder);

                            if (!builder.getType().isEmpty()) {
                                final int index = getListView().getItems().indexOf(conditionData);
                                getListView().getItems().set(index, builder.build());
                            }

                        } catch (final IOException e) {
                            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot start new window!" + e));
                            errorLabel.setVisible(true);
                        }
                    });

                    final Button removeButton = new Button(getTranslation(APPLICATION_SECTION, "Remove"));
                    removeButton.setPrefWidth(75);
                    AnchorPane.setTopAnchor(removeButton, 0.0);
                    AnchorPane.setBottomAnchor(removeButton, 0.0);
                    AnchorPane.setRightAnchor(removeButton, 0.0);

                    removeButton.setOnAction(event -> getListView().getItems().remove(conditionData));

                    final Label label = new Label(getTranslation(CONDITION_SECTION, conditionData.getType()));
                    AnchorPane.setTopAnchor(label, 0.0);
                    AnchorPane.setBottomAnchor(label, 0.0);
                    AnchorPane.setLeftAnchor(label, 0.0);

                    final AnchorPane anchorPane = new AnchorPane();
                    anchorPane.getChildren().addAll(label, editButton, removeButton);

                    setGraphic(anchorPane);
                }
            }
        };
    }
}
