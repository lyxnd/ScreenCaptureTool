package net.jackchuan.screencapturetool.test;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/11/21 23:10
 */
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class HeartAnimation extends Application {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final double SCALE = 15.0;

    @Override
    public void start(Stage primaryStage) {
        // 创建画布
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // 设置背景
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        // 绘制心形
        drawHeart(gc);

        // 创建场景和窗口
        Pane root = new Pane(canvas);
        Scene scene = new Scene(root);
        primaryStage.setTitle("Heart Animation");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void drawHeart(GraphicsContext gc) {
        Timeline timeline = new Timeline();
        gc.setStroke(Color.RED);
        gc.setLineWidth(2);

        // 心形中心位置
        double centerX = WIDTH / 2f;
        double centerY = HEIGHT / 3f;

        // 参数 t 的最大值和步长
        double tMax = Math.PI * 2;
        double tStep = 0.01;

        // 绘制逐步心形
        for (double t = 0; t <= tMax; t += tStep) {
            // 计算心形上的点
            double x = 16 * Math.pow(Math.sin(t), 3);
            double y = 13 * Math.cos(t) - 5 * Math.cos(2 * t) - 2 * Math.cos(3 * t) - Math.cos(4 * t);

            double scaledX = centerX + x * SCALE;
            double scaledY = centerY - y * SCALE;

            KeyFrame keyFrame = new KeyFrame(Duration.millis(t * 500), e -> {
                gc.lineTo(scaledX, scaledY);
                gc.stroke();
            });

            timeline.getKeyFrames().add(keyFrame);
        }

        // 播放动画
        timeline.setCycleCount(1);
        timeline.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
