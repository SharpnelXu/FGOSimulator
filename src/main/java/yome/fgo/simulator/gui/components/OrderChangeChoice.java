package yome.fgo.simulator.gui.components;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import yome.fgo.simulator.models.combatants.Servant;

public class OrderChangeChoice extends VBox {
    public final int index;
    private final RadioButton radioButton;

    public OrderChangeChoice(
            final ToggleGroup toggleGroup,
            final int index,
            final Servant servant,
            final SimulationWindow simulationWindow
    ) {
        super();
        setAlignment(Pos.TOP_CENTER);
        setSpacing(10);
        setFillWidth(false);

        this.index = index;

        radioButton = new RadioButton();
        radioButton.setToggleGroup(toggleGroup);
        final ImageView servantThumbnail = new ImageView();
        servantThumbnail.setImage(simulationWindow.getServantImage(servant.getId(), servant.getAscension()));
        servantThumbnail.setFitHeight(100);
        servantThumbnail.setFitWidth(100);
        final AnchorPane imgAnchor = new AnchorPane();
        imgAnchor.setStyle("-fx-border-color: rgba(161,161,161,0.8); -fx-border-style: solid; -fx-border-radius: 3; -fx-border-width: 2");
        AnchorPane.setBottomAnchor(servantThumbnail, 0.0);
        AnchorPane.setTopAnchor(servantThumbnail, 0.0);
        AnchorPane.setLeftAnchor(servantThumbnail, 0.0);
        AnchorPane.setRightAnchor(servantThumbnail, 0.0);
        imgAnchor.getChildren().add(servantThumbnail);

        final Button servantButton = new Button();
        servantButton.setGraphic(imgAnchor);
        servantButton.setOnAction(e -> radioButton.fire());

        getChildren().addAll(radioButton, servantButton);
    }

    public boolean isSelected() {
        return radioButton.isSelected();
    }

    public int getIndex() {
        return index;
    }
}
