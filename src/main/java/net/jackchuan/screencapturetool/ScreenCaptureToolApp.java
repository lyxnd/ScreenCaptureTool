package net.jackchuan.screencapturetool;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Pair;
import net.jackchuan.screencapturetool.controller.SettingController;
import net.jackchuan.screencapturetool.external.stage.OverlayStage;
import net.jackchuan.screencapturetool.util.LibraryLoader;
import net.jackchuan.screencapturetool.util.ScreenCaptureUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/11/20 16:26
 */
public class ScreenCaptureToolApp extends Application {
    private boolean shiftPressed = false;
    private boolean altPressed = false;
    private boolean ctrlPressed = false;
    private boolean shotting = false;
    private OverlayStage overlayStage;
    public static final Logger LOGGER = LoggerFactory.getLogger(ScreenCaptureToolApp.class);

    @Override
    public void start(Stage stage) throws IOException {
        // 隐藏主窗口
        Platform.setImplicitExit(false);
        LibraryLoader.loadOpenCVLibrary();
        // 注册全局热键监听
        registerGlobalKeyListener();
        try {
            CaptureProperties.checkFile();
        } catch (IOException e) {
            try (PrintWriter writer = new PrintWriter(new File(CaptureProperties.logPath))) {
                e.printStackTrace(writer);
            }
        }
        if (!CaptureProperties.loadProperties()) {
            CaptureProperties.updateAll(CaptureProperties.enableAll);
        }
        if(CaptureProperties.showSettings){
            openConfigWindow();
        }
    }

    private void registerGlobalKeyListener() {
        try {
            // 注册全局键盘监听
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
                @Override
                public void nativeKeyPressed(NativeKeyEvent e) {
                    if (e.getKeyCode() == NativeKeyEvent.VC_SHIFT) {
                        shiftPressed = true;
                    }
                    if (e.getKeyCode() == NativeKeyEvent.VC_ALT) {
                        altPressed = true;
                    }
                    if (e.getKeyCode() == NativeKeyEvent.CTRL_MASK) {
                        ctrlPressed = true;
                    }
                    if (e.getKeyCode() == NativeKeyEvent.VC_F8 && shiftPressed) {
                        Platform.runLater(() -> openConfigWindow());
                    }
                    // 捕捉热键（例如 F12）
                    if (e.getKeyCode() == CaptureProperties.CAPTURE_KEY) {
                        if (CaptureProperties.isShiftNeeded && shiftPressed) {
                            shotting = true;
                            Platform.runLater(ScreenCaptureToolApp.this::showOverlayStage);
                        } else if (CaptureProperties.isAltNeeded && altPressed) {
                            shotting = true;
                            Platform.runLater(ScreenCaptureToolApp.this::showOverlayStage);
                        } else if (CaptureProperties.isCtrlNeeded && ctrlPressed) {
                            shotting = true;
                            Platform.runLater(ScreenCaptureToolApp.this::showOverlayStage);
                        } else if (!CaptureProperties.isShiftNeeded && !CaptureProperties.isCtrlNeeded && !CaptureProperties.isAltNeeded) {
                            shotting = true;
                            Platform.runLater(ScreenCaptureToolApp.this::showOverlayStage);
                        }
                    }
                    if (e.getKeyCode() == NativeKeyEvent.VC_ESCAPE && shotting) {
                        shotting = false;
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
                public void nativeKeyTyped(NativeKeyEvent e) {
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void openConfigWindow() {
        Stage settingStage = new Stage();
        FXMLLoader loader = new FXMLLoader(ScreenCaptureToolApp.class.getResource("setting.fxml"));
        Scene scene = null;
        try {
            scene = new Scene(loader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 设置控制器
        SettingController settingController = loader.getController();
        scene.getStylesheets().add(ScreenCaptureToolApp.class.getResource("assets/css/setting.css").toExternalForm());
        // 设置场景并显示窗口
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        settingStage.setScene(scene);
        settingStage.setTitle("设置");
        settingStage.getIcons().add(new Image(ScreenCaptureToolApp
                .class.getResource("assets/icon/setting.png").toExternalForm()));
        settingStage.setX(size.getWidth() / 3);
        settingStage.setY(size.getHeight() / 3);
        settingStage.setWidth(size.getWidth() * 0.4);
        settingStage.setHeight(size.getHeight() * 0.4);
        settingStage.show();
    }

    private void closeOverlayStage() {
        if (overlayStage != null) {
            overlayStage.close();
        }
    }

    private void showOverlayStage() {
        if(!CaptureProperties.autoSelect){
            overlayStage = new OverlayStage();
        }else {
            Pair<Integer, Integer> size = ScreenCaptureUtil.getScreenSize(ScreenCaptureUtil.DEFAULT_SIZE);
            BufferedImage image;
            try {
                image = ScreenCaptureUtil.captureWithAWT(0, 0, size.getKey(), size.getValue());
            } catch (AWTException e) {
                throw new RuntimeException(e);
            }
//            overlayStage = new OverlayStage(SwingFXUtils.toFXImage(image,null));
            overlayStage = new OverlayStage();
        }
        overlayStage.show();
    }

    @Override
    public void stop() throws Exception {
        // 退出时注销全局监听
        GlobalScreen.unregisterNativeHook();
        super.stop();
    }
}
