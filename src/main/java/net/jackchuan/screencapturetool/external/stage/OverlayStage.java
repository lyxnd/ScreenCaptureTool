package net.jackchuan.screencapturetool.external.stage;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/12/20 16:10
 */
public class OverlayStage extends Stage {
    private Scene scene;
    private StackPane stackPane;
    private Image image;
    private Canvas canvas;
    private GraphicsContext gc;
    public OverlayStage(Image image){
        this.image = image;
        canvas=new Canvas();
        gc=canvas.getGraphicsContext2D();
        stackPane=new StackPane(canvas);
        scene=new Scene(stackPane,image.getWidth(),image.getHeight());
        this.setScene(scene);
        this.initStyle(StageStyle.UNDECORATED);
        initialAction();
    }

    private void initialAction() {

    }
}
