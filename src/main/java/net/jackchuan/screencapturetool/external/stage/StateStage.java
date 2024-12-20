package net.jackchuan.screencapturetool.external.stage;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/12/19 22:16
 */
public class StateStage extends Stage {
    private Image image;
    private StackPane stackPane;
    private Scene scene;
    private ImageView imageView;
    public StateStage(BufferedImage bfImg) {
        this.image = SwingFXUtils.toFXImage(bfImg,null);
        imageView=new ImageView(image);
        imageView.setFitHeight(bfImg.getHeight());
        imageView.setFitWidth(bfImg.getWidth());
        stackPane=new StackPane(imageView);
        scene=new Scene(stackPane,bfImg.getWidth(),bfImg.getHeight());
        this.setScene(scene);
        this.setX(300);
        this.setY(300);
        this.setTitle("image displayer  (" + image.getWidth() + "," + image.getHeight()+")");
    }
}
