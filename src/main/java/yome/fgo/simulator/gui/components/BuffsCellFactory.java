package yome.fgo.simulator.gui.components;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import lombok.AllArgsConstructor;
import yome.fgo.data.proto.FgoStorageData.BuffData;

import java.io.IOException;

import static yome.fgo.simulator.gui.components.DataPrinter.printBuffData;
import static yome.fgo.simulator.gui.creators.BuffBuilder.createBuff;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

@AllArgsConstructor
public class BuffsCellFactory implements Callback<ListView<BuffData>, ListCell<BuffData>> {
    private final Label errorLabel;

    @Override
    public ListCell<BuffData> call(final ListView<BuffData> param) {
        return new ListCell<>() {
            @Override
            public void updateItem(final BuffData buffData, final boolean empty) {
                super.updateItem(buffData, empty);

                if (empty || buffData == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(null);
                    final Button editButton = new Button(getTranslation(APPLICATION_SECTION, "Edit"));
                    editButton.setPrefWidth(55);

                    editButton.setOnAction(event -> {
                        try {
                            final BuffData.Builder builder = buffData.toBuilder();
                            createBuff(getListView().getScene().getWindow(), builder);

                            if (!builder.getType().isEmpty()) {
                                final int index = getListView().getItems().indexOf(buffData);
                                getListView().getItems().set(index, builder.build());
                            }

                        } catch (final IOException e) {
                            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot start new window!") + e);
                            errorLabel.setVisible(true);
                        }
                    });

                    final Button removeButton = new Button(getTranslation(APPLICATION_SECTION, "Remove"));
                    removeButton.setPrefWidth(55);

                    removeButton.setOnAction(event -> getListView().getItems().remove(buffData));

                    final Label label = new Label(printBuffData(buffData));
                    label.setWrapText(true);
                    label.setMaxWidth(Math.max(param.getWidth() - 140, 500));

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
