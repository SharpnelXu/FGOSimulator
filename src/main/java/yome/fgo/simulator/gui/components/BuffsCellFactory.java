package yome.fgo.simulator.gui.components;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
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
                    AnchorPane.setTopAnchor(editButton, 0.0);
                    AnchorPane.setBottomAnchor(editButton, 0.0);
                    AnchorPane.setLeftAnchor(editButton, 65.0);

                    editButton.setOnAction(event -> {
                        try {
                            final BuffData.Builder builder = buffData.toBuilder();
                            createBuff(getListView().getScene().getWindow(), builder);

                            if (!builder.getType().isEmpty()) {
                                final int index = getListView().getItems().indexOf(buffData);
                                getListView().getItems().set(index, builder.build());
                            }

                        } catch (final IOException e) {
                            errorLabel.setText(getTranslation(APPLICATION_SECTION, "Cannot start new window!" + e));
                            errorLabel.setVisible(true);
                        }
                    });

                    final Button removeButton = new Button(getTranslation(APPLICATION_SECTION, "Remove"));
                    removeButton.setPrefWidth(55);
                    AnchorPane.setTopAnchor(removeButton, 0.0);
                    AnchorPane.setBottomAnchor(removeButton, 0.0);
                    AnchorPane.setLeftAnchor(removeButton, 0.0);

                    removeButton.setOnAction(event -> getListView().getItems().remove(buffData));

                    final Label label = new Label(printBuffData(buffData));
                    AnchorPane.setTopAnchor(label, 0.0);
                    AnchorPane.setBottomAnchor(label, 0.0);
                    AnchorPane.setLeftAnchor(label, 130.0);

                    final AnchorPane anchorPane = new AnchorPane();
                    anchorPane.getChildren().addAll(label, editButton, removeButton);

                    setGraphic(anchorPane);
                }
            }
        };
    }
}
