package yome.fgo.simulator.gui.components;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yome.fgo.data.proto.FgoStorageData.ServantData;

import java.util.List;

@NoArgsConstructor
@Getter
public class ServantDataWrapper extends AnchorPane {
    private ServantData servantData;
    private List<Image> ascensionImages;
    private ImageView imageView;

    public ServantDataWrapper(final ServantData servantData, final List<Image> ascensionImages) {
        super();
        setFrom(servantData, ascensionImages);
    }

    public void setFrom(final ServantData servantData, final List<Image> ascensionImages) {
        setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        setStyle("-fx-border-color: rgba(161,161,161,0.8); -fx-border-style: solid; -fx-border-radius: 3; -fx-border-width: 2");
        this.servantData = servantData;
        this.ascensionImages = ascensionImages;
        this.imageView = new ImageView(ascensionImages.get(0));
        this.imageView.setFitHeight(100);
        this.imageView.setFitWidth(100);
        AnchorPane.setBottomAnchor(imageView, 0.0);
        AnchorPane.setTopAnchor(imageView, 0.0);
        AnchorPane.setLeftAnchor(imageView, 0.0);
        AnchorPane.setRightAnchor(imageView, 0.0);
        getChildren().add(imageView);
    }
}
