package yome.fgo.simulator.gui.components;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yome.fgo.data.proto.FgoStorageData.CraftEssenceData;

@NoArgsConstructor
@Getter
public class CraftEssenceDataWrapper extends AnchorPane {
    private CraftEssenceData craftEssenceData;
    private Image image;
    private ImageView imageView;

    public CraftEssenceDataWrapper(final CraftEssenceData craftEssenceData, final Image image) {
        super();
        setFrom(craftEssenceData, image);
    }

    public void setFrom(final CraftEssenceData craftEssenceData, final Image image) {
        setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        setStyle("-fx-border-color: rgba(161,161,161,0.8); -fx-border-style: solid; -fx-border-radius: 3; -fx-border-width: 2");
        this.craftEssenceData = craftEssenceData;
        this.image = image;
        this.imageView = new ImageView(image);
        this.imageView.setFitHeight(100);
        this.imageView.setFitWidth(100);
        AnchorPane.setBottomAnchor(imageView, 0.0);
        AnchorPane.setTopAnchor(imageView, 0.0);
        AnchorPane.setLeftAnchor(imageView, 0.0);
        AnchorPane.setRightAnchor(imageView, 0.0);
        getChildren().clear();
        getChildren().add(imageView);
    }
}
