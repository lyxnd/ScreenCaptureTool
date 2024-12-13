package net.jackchuan.screencapturetool.test;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/11/22 13:01
 */

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import net.jackchuan.screencapturetool.ScreenCaptureToolApp;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        // UI组件
        Image image=new Image(ScreenCaptureToolApp.class.getResource("assets/transparent.png").toExternalForm());
        Canvas canvas=new Canvas();
        Canvas canvas1=new Canvas();
        StackPane stackPane=new StackPane();
        canvas.setWidth(500);
        canvas.setHeight(500);
        canvas1.setWidth(500);
        canvas1.setHeight(500);
        canvas.setVisible(true);
        canvas1.setVisible(true);
        canvas.toFront();
        canvas1.toBack();
        GraphicsContext gc = canvas1.getGraphicsContext2D();
        Image img=new Image(ScreenCaptureToolApp.class.getResource("assets/icon/reload.png").toExternalForm());
        gc.drawImage(img,0,0,canvas1.getWidth(),canvas1.getHeight());
        stackPane.getChildren().addAll(canvas,canvas1);
        // 布局设置
        Scene scene = new Scene(stackPane, 500, 500);
        GraphicsContext g2 = canvas.getGraphicsContext2D();
        g2.setGlobalAlpha(0);
        g2.clearRect(0,0,canvas.getWidth(),canvas.getHeight());
        g2.setGlobalAlpha(1);

        g2.drawImage(image,0,0,canvas.getWidth(),canvas.getHeight());
        g2.strokeText("Test nihao",200,200);
        // 设置舞台
        primaryStage.setTitle("屏幕区域截图");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
