package net.jackchuan.screencapturetool.test;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/11/22 13:01
 */

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.robot.Robot;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        // UI组件
        Button captureButton = new Button("截取屏幕");
        ImageView imageView = new ImageView();

        // 设置按钮点击事件
        captureButton.setOnAction(event -> {
            // 定义截取区域（屏幕左上角 100x100 到 400x400 区域）
            Rectangle2D screenRegion = new Rectangle2D(0, 0, 1280, 800);
            primaryStage.setWidth(1200);
            primaryStage.setHeight(1000);
            // 使用 Robot 截取屏幕
            Robot robot = new Robot();
            Image screenshot = robot.getScreenCapture(null, screenRegion);

            // 将截图显示到 ImageView 中
            imageView.setImage(screenshot);
        });

        // 布局设置
        VBox root = new VBox(10, captureButton, imageView);
        Scene scene = new Scene(root, 500, 500);

        // 设置舞台
        primaryStage.setTitle("屏幕区域截图");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
