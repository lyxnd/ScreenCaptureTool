package net.jackchuan.screencapturetool;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.jackchuan.screencapturetool.controller.CaptureDisplayController;
import net.jackchuan.screencapturetool.util.ScreenCaptureUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/11/20 16:26
 */
public class ScreenCaptureToolApp extends Application {
    private boolean shiftPressed=false;
    private boolean shotting=false;
    private Stage overlayStage;
    private Stage displayStage;
    private CaptureDisplayController controller;
    private double startX, startY, endX, endY;
    @Override
    public void start(Stage stage) throws Exception {
        // 隐藏主窗口
        Platform.setImplicitExit(false);

        // 注册全局热键监听
        registerGlobalKeyListener();
    }

    private void registerGlobalKeyListener() {
        try {
            // 禁用 JNativeHook 的日志输出
            Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
            logger.setLevel(Level.OFF);

            // 注册全局键盘监听
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
                @Override
                public void nativeKeyPressed(NativeKeyEvent e) {
                    if (e.getKeyCode() == NativeKeyEvent.VC_SHIFT) {
                        shiftPressed = true; // 记录Shift键按下
                    }
                    // 捕捉热键（例如 F12）
                    if (shiftPressed && e.getKeyCode() == NativeKeyEvent.VC_F12) {
                        shotting=true;
                        Platform.runLater(ScreenCaptureToolApp.this::showOverlayStage);
                    }
                    if (e.getKeyCode() == NativeKeyEvent.VC_ESCAPE && shotting) {
                        shotting=false;
                        Platform.runLater(ScreenCaptureToolApp.this::closeOverlayStage);
                    }
                }

                @Override
                public void nativeKeyReleased(NativeKeyEvent e) {
                    if (e.getKeyCode() == NativeKeyEvent.VC_SHIFT) {
                        shiftPressed = false; // 当Shift键释放时更新状态
                    }
                }

                @Override
                public void nativeKeyTyped(NativeKeyEvent e) { }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void closeOverlayStage() {
        if(overlayStage!=null){
            overlayStage.close();
        }
    }

    private void showOverlayStage() {
        overlayStage = new Stage();
        overlayStage.setAlwaysOnTop(true);
//        overlayStage.setOpacity(0.1);
        Rectangle screenBounds = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        javafx.scene.canvas.Canvas canvas = new Canvas(screenBounds.getWidth(), screenBounds.getHeight());
        GraphicsContext gc = canvas.getGraphicsContext2D();
        canvas.setCursor(Cursor.CROSSHAIR);
        // 初始化画布背景为透明
        gc.setFill(Color.rgb(236,236,236,0.1));
        gc.fillRect(0,0,canvas.getWidth(),canvas.getHeight());
        gc.setStroke(javafx.scene.paint.Color.RED);
        gc.setLineWidth(2);

        // 监听鼠标拖动事件
        canvas.setOnMousePressed(e -> {
            startX = e.getScreenX();
            startY = e.getScreenY();
        });

        canvas.setOnMouseDragged(e -> {
            endX = e.getScreenX();
            endY = e.getScreenY();

            // 清除之前的矩形，绘制新的
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.strokeRect(Math.min(startX, endX), Math.min(startY, endY),
                    Math.abs(endX - startX), Math.abs(endY - startY));
        });

        canvas.setOnMouseReleased(e -> {
            overlayStage.close();
            captureAndShowScreenshot((int) Math.min(startX, endX), (int) Math.min(startY, endY),
                    (int) Math.abs(endX - startX), (int) Math.abs(endY - startY));
        });

        // 设置透明的根布局
        StackPane root = new StackPane(canvas);
        root.setBackground(null);

        // 设置透明的场景
        Scene scene = new Scene(root, Color.TRANSPARENT);
        scene.setFill(null);
        overlayStage.setScene(scene);

        // 全屏显示，去除系统边框
        overlayStage.setMaximized(true);
        overlayStage.initStyle(StageStyle.TRANSPARENT);

        overlayStage.show();
    }

    private void captureAndShowScreenshot(int x, int y, int width, int height) {
        try {
            // JNA截取屏幕
            BufferedImage screenshot = ScreenCaptureUtil.captureScreenRegion(x,y,width,height);
            // 转换为 JavaFX Image
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(screenshot, "png", baos);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            javafx.scene.image.Image fxImage = new javafx.scene.image.Image(bais);
            // 显示截图弹窗
            showScreenshotPopup(fxImage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void showScreenshotPopup(javafx.scene.image.Image image) {
        displayStage = new Stage();
        displayStage.setTitle("Screenshot editor");
        // 加载 FXML
        FXMLLoader loader = new FXMLLoader(ScreenCaptureToolApp.class.getResource("capture.fxml"));
        Scene scene = null;
        try {
            scene = new Scene(loader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        scene.getStylesheets().add(ScreenCaptureToolApp.class.getResource("assets/css/style.css").toExternalForm());
        // 创建 ImageView 来显示图片
        javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(image);
        imageView.setPreserveRatio(true);  // 保持宽高比
        imageView.setFitWidth(800);  // 可以设置一个最大宽度
        // 获取图片的实际宽高
        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();

        // 动态设置 displayStage 的大小
        displayStage.setWidth(imageWidth);
        displayStage.setHeight(imageHeight+100);

        // 设置控制器
        controller = loader.getController();
        controller.setCapture(imageView);

        // 设置场景并显示窗口
        displayStage.setScene(scene);
        displayStage.show();
    }


    @Override
    public void stop() throws Exception {
        // 退出时注销全局监听
        GlobalScreen.unregisterNativeHook();
        super.stop();
    }
}
