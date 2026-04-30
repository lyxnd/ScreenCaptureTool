package net.jackchuan.screencapturetool;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.sun.management.OperatingSystemMXBean;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.geometry.Rectangle2D;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Pair;
import net.jackchuan.screencapturetool.controller.CaptureDisplayController;
import net.jackchuan.screencapturetool.controller.SettingController;
import net.jackchuan.screencapturetool.external.stage.OverlayStage;
import net.jackchuan.screencapturetool.util.ImageFormatHandler;
import net.jackchuan.screencapturetool.util.LibraryLoader;
import net.jackchuan.screencapturetool.util.ScreenCaptureUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/11/20 16:26
 */
public class ScreenCaptureToolApp extends Application {
    private static boolean shiftPressed = false;
    private static boolean altPressed = false;
    private static boolean ctrlPressed = false;
    private static boolean shotting = false;
    private static boolean showing = false;
    private static OverlayStage overlayStage;
    public static Logger LOGGER;
    static {
        try {
            // logs/ 目录固定在 EXE 同级目录（user.dir 由 EXE 启动器指向 EXE 所在目录）
            Path logDir = Paths.get(System.getProperty("user.dir"), "logs");
            Files.createDirectories(logDir);
            System.setProperty("app.log.dir", logDir.toAbsolutePath().toString());

            java.net.URL configUrl = ScreenCaptureToolApp.class.getResource("log4j2.xml");
            if (configUrl != null) {
                Configurator.initialize(null, configUrl.toString());
            }

            CaptureProperties.checkFile();
            if (!CaptureProperties.loadProperties()) {
                CaptureProperties.updateAll(CaptureProperties.enableAll);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LOGGER = LogManager.getLogger(ScreenCaptureToolApp.class);
    }

    public static void setLogger(Logger logger){
        LOGGER=logger;
    }

    @Override
    public void start(Stage stage) throws IOException {

        // 隐藏主窗口
        Platform.setImplicitExit(false);
        LibraryLoader.loadOpenCVLibrary();
//        CaptureProperties.captureType = ScreenCaptureUtil.benchmark();
        LOGGER.info("benchmark selected capture method: {}", CaptureProperties.captureType);
        // 注册全局热键监听
        registerGlobalKeyListener();
        if(CaptureProperties.showSettings){
            openConfigWindow();
        }

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while(true){
//                    try {
//                        Thread.sleep(1000*15);
//                        OperatingSystemMXBean osBean =
//                                (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
//                        Runtime runtime = Runtime.getRuntime();
//                        long total = runtime.totalMemory(); // 当前分配给 JVM 的总内存（字节）
//                        long free = runtime.freeMemory();   // JVM 内部空闲内存
//                        long used = total - free;           // 实际已用堆内存
//                        long max = runtime.maxMemory();     // JVM 可用的最大内存（字节）
//                        double rss = osBean.getProcessCpuLoad();
//                        LOGGER.info("memory used = {} Mb, Total memory = {} Mb, Max memory = {} Mb,  cpu used ratio = {}",
//                                used / 1024 / 1024,total / 1024 / 1024,max / 1024 / 1024,rss);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//            }
//        }).start();
    }


    public static void startCapturing() throws IOException {
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

    private static void registerGlobalKeyListener() {
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
                        Platform.runLater(ScreenCaptureToolApp::openConfigWindow);
                    }
                    // 捕捉热键（F1）
                    if(e.getKeyCode()==CaptureProperties.UPLOAD_KEY && !SettingController.changing){
                        if(CaptureProperties.uploadIsShiftNeeded&&shiftPressed){
                            showing = true;
                            uploadImage();
                        }else if(CaptureProperties.uploadIsAltNeeded&&altPressed){
                            showing = true;
                            uploadImage();
                        }else if(CaptureProperties.uploadIsCtrlNeeded&&ctrlPressed){
                            showing = true;
                            uploadImage();
                        }else {
                            uploadImage();
                        }
                    }
                    if (e.getKeyCode() == CaptureProperties.CAPTURE_KEY&&(overlayStage==null||!overlayStage.isShowing())
                            && !SettingController.changing && ! showing) {
                        if (CaptureProperties.isShiftNeeded && shiftPressed) {
                            shotting = true;
                            Platform.runLater(ScreenCaptureToolApp::showOverlayStage);
                        } else if (CaptureProperties.isAltNeeded && altPressed) {
                            shotting = true;
                            Platform.runLater(ScreenCaptureToolApp::showOverlayStage);
                        } else if (CaptureProperties.isCtrlNeeded && ctrlPressed) {
                            shotting = true;
                            Platform.runLater(ScreenCaptureToolApp::showOverlayStage);
                        } else if (!CaptureProperties.isShiftNeeded && !CaptureProperties.isCtrlNeeded && !CaptureProperties.isAltNeeded) {
                            shotting = true;
                            Platform.runLater(ScreenCaptureToolApp::showOverlayStage);
                        }
                    }
                    if (e.getKeyCode() == NativeKeyEvent.VC_ESCAPE && shotting) {
                        shotting = false;
                        Platform.runLater(ScreenCaptureToolApp::closeOverlayStage);
                    }
                    showing = false;
                }

                @Override
                public void nativeKeyReleased(NativeKeyEvent e) {
                    if (e.getKeyCode() == NativeKeyEvent.VC_SHIFT) {
                        shiftPressed = false;
                        altPressed = false;
                        ctrlPressed = false;
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

    private static void openConfigWindow() {
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
        settingStage.setHeight(size.getHeight() * 0.54);
        settingStage.show();
    }

    private static void closeOverlayStage() {
        if (overlayStage != null) {
            overlayStage.close();
        }
    }

    private static void showOverlayStage() {
        BufferedImage image=null;
        if ("边缘检测".equals(CaptureProperties.detectMode)){
            Pair<Integer, Integer> size = ScreenCaptureUtil.getScreenSize(ScreenCaptureUtil.DEFAULT_SIZE);
            try {
                image = ScreenCaptureUtil.captureWithAWT(0, 0, size.getKey(), size.getValue());
            } catch (AWTException e) {
                throw new RuntimeException(e);
            }
        }
        overlayStage = new OverlayStage(image == null ? null:ImageFormatHandler.toFXImage(image));
        overlayStage.getIcons().add(new Image(ScreenCaptureToolApp
                .class.getResource("assets/icon/editor.png").toExternalForm()));
        overlayStage.show();
    }

    @Override
    public void stop() throws Exception {
        // 退出时注销全局监听
        GlobalScreen.unregisterNativeHook();
        super.stop();
    }

    public static void showImage(Image image){
        Stage displayStage = new Stage();
        displayStage.setTitle("Screenshot editor");
        displayStage.getIcons().add(new Image(ScreenCaptureToolApp
                .class.getResource("assets/icon/editor.png").toExternalForm()));
        // 加载 FXML
        FXMLLoader loader = new FXMLLoader(ScreenCaptureToolApp.class.getResource("capture.fxml"));
        Scene scene = null;
        try {
            scene = new Scene(loader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        scene.getStylesheets().add(ScreenCaptureToolApp.class.getResource("assets/css/style.css").toExternalForm());
        // 设置控制器
        CaptureDisplayController controller = loader.getController();
        controller.setCapture(image,ScreenCaptureUtil.shouldScale(image));
        controller.setOriginalImage(image);
        displayStage.setScene(scene);
        displayStage.setOnShown(e -> Platform.runLater(() -> {
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            double x = bounds.getMinX() + (bounds.getWidth() - displayStage.getWidth()) / 2;
            double y = bounds.getMinY() + (bounds.getHeight() - displayStage.getHeight()) / 2;
            displayStage.setX(Math.max(bounds.getMinX(), x));
            displayStage.setY(Math.max(bounds.getMinY(), y));
        }));
        displayStage.show();
    }

    public static void uploadImage(){
        Platform.runLater(()->{
            if(!isOpen){
                isOpen=true;
                fc = new FileChooser();
                fc.setInitialDirectory(CaptureProperties.getSelectDirectory());
                fc.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("图片", "*.jpg", "*.png"));
                File file = fc.showOpenDialog(null);
                if(file!=null){
                    BufferedImage bmg = null;
                    try {
                        bmg = ImageIO.read(file);
                    } catch (IOException e) {
                        ScreenCaptureToolApp.LOGGER.error("上传图片失败,",e);
                        throw new RuntimeException(e);
                    }
                    showImage(ImageFormatHandler.toFXImage(bmg));
                }
                isOpen=false;
            }
        });
    }
    private static FileChooser fc;
    private static boolean isOpen=false;
}
