package yome.fgo.simulator.gui.components;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yome.fgo.data.proto.FgoStorageData.Gender;
import yome.fgo.data.proto.FgoStorageData.MysticCodeData;

import java.util.List;

@NoArgsConstructor
@Getter
public class MysticCodeDataWrapper extends AnchorPane {
    private MysticCodeData mysticCodeData;
    private List<Image> images;
    private ImageView imageView;
    private Gender gender;

    public MysticCodeDataWrapper(final MysticCodeData mysticCodeData, final List<Image> images) {
        super();
        setFrom(mysticCodeData, images, Gender.MALE);
    }

    public void setFrom(final MysticCodeData mysticCodeData, final List<Image> images, final Gender gender) {
        setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        this.mysticCodeData = mysticCodeData;
        this.images = images;
        this.imageView = new ImageView(images.get(0));
        this.imageView.setFitHeight(100);
        this.imageView.setFitWidth(100);
        AnchorPane.setBottomAnchor(imageView, 0.0);
        AnchorPane.setTopAnchor(imageView, 0.0);
        AnchorPane.setLeftAnchor(imageView, 0.0);
        AnchorPane.setRightAnchor(imageView, 0.0);
        getChildren().clear();
        getChildren().add(imageView);
        this.gender = gender;
        setFromGender(gender);
    }

    public void setFromGender(final Gender gender) {
        this.gender = gender;
        final Image imageToSet = gender == Gender.MALE ?
                images.get(0) :
                images.get(1);
        imageView.setImage(imageToSet);
    }
}
