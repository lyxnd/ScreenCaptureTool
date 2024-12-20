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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;
import net.jackchuan.screencapturetool.controller.CaptureDisplayController;
import net.jackchuan.screencapturetool.controller.SettingController;
import net.jackchuan.screencapturetool.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.imageio.ImageIO;
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
    private Stage overlayStage;
    private Stage displayStage;
    private CaptureDisplayController controller;
    private double startX, startY, endX, endY;
    private ContextMenu popMenu;
    private MenuItem fullCut,test,test1;
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
        if(!CaptureProperties.loadProperties()){
            CaptureProperties.updateAll(CaptureProperties.enableAll);
        }
    }

    private void registerGlobalKeyListener() {
        try {

            // 注册全局键盘监听
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
                @Override
                public void nativeKeyPressed(NativeKeyEvent e) {
                    if(e.getKeyCode()==NativeKeyEvent.VC_SHIFT){
                        shiftPressed=true;
                    }
                    if(e.getKeyCode()==NativeKeyEvent.VC_ALT){
                        altPressed=true;
                    }
                    if(e.getKeyCode()==NativeKeyEvent.CTRL_MASK){
                        ctrlPressed=true;
                    }
                    if(e.getKeyCode()==NativeKeyEvent.VC_F8&&shiftPressed){
                        Platform.runLater(()->openConfigWindow());
                    }
                    // 捕捉热键（例如 F12）
                    if (e.getKeyCode() == CaptureProperties.CAPTURE_KEY) {
                        if(CaptureProperties.isShiftNeeded&&shiftPressed){
                            shotting = true;
                            Platform.runLater(ScreenCaptureToolApp.this::showOverlayStage);
                        }else if(CaptureProperties.isAltNeeded&&altPressed){
                            shotting = true;
                            Platform.runLater(ScreenCaptureToolApp.this::showOverlayStage);
                        }else if(CaptureProperties.isCtrlNeeded&&ctrlPressed){
                            shotting = true;
                            Platform.runLater(ScreenCaptureToolApp.this::showOverlayStage);
                        }else if(!CaptureProperties.isShiftNeeded&&!CaptureProperties.isCtrlNeeded&&!CaptureProperties.isAltNeeded){
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
        overlayStage = new Stage();
        overlayStage.setAlwaysOnTop(true);
        overlayStage.getIcons().add(new Image(ScreenCaptureToolApp
                .class.getResource("assets/icon/shortcut.png").toExternalForm()));


        Rectangle screenBounds = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        javafx.scene.canvas.Canvas canvas = new Canvas(screenBounds.getWidth(), screenBounds.getHeight());
        GraphicsContext gc = canvas.getGraphicsContext2D();
        canvas.setCursor(Cursor.CROSSHAIR);
        canvas.setOnMouseClicked(e->{
            if(e.getClickCount()==2){
                overlayStage.setOpacity(0);
                overlayStage.close();
                Pair<Integer, Integer> size = ScreenCaptureUtil.getScreenSize(ScreenCaptureUtil.DEFAULT_SIZE);
                captureAndShowScreenshot(0,0,size.getKey(),size.getValue(),true);
            }
        });
        popMenu=new ContextMenu();
        fullCut=new MenuItem("全屏选择");
        test=new MenuItem("save shot");
        test1=new MenuItem("detect rect");
        popMenu.getItems().addAll(fullCut,test,test1);
        fullCut.setOnAction(e->{
            overlayStage.setOpacity(0);
            overlayStage.close();
            Pair<Integer, Integer> size = ScreenCaptureUtil.getScreenSize(ScreenCaptureUtil.DEFAULT_SIZE);
            captureAndShowScreenshot(0,0,size.getKey(),size.getValue(),true);
        });
        test.setOnAction(e->{
            overlayStage.close();
            Pair<Integer, Integer> size = ScreenCaptureUtil.getScreenSize(ScreenCaptureUtil.DEFAULT_SIZE);
            Image snapshot = captureAndShowScreenshot(0,0,size.getKey(),size.getValue(),false);
            BufferedImage image = ImageFormatHandler.fxImageToBufferedImage(snapshot);
            try {
                ImageIO.write(image,"png",new File("F:/java_practise/ScreenCaptureTool/temp/test.png"));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        test1.setOnAction(e->{
            overlayStage.close();
            Pair<Integer, Integer> size = ScreenCaptureUtil.getScreenSize(ScreenCaptureUtil.DEFAULT_SIZE);
            Image snapshot = captureAndShowScreenshot(0,0,size.getKey(),size.getValue(),false);
            ImageDetector.detectRect(snapshot);
        });
        // 初始化画布背景为透明
        gc.setFill(Color.rgb(236, 236, 236, 0.1));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setStroke(javafx.scene.paint.Color.RED);
        gc.setLineWidth(2);

        // 监听鼠标拖动事件
        canvas.setOnMousePressed(e -> {
            if(e.getButton()==MouseButton.SECONDARY){
                popMenu.show(overlayStage, e.getScreenX(), e.getScreenY());
            }
            startX = e.getX();
            startY = e.getY();
        });
        canvas.setOnMouseMoved(e -> {

        });

        canvas.setOnMouseDragged(e -> {
            if(e.getButton()==MouseButton.PRIMARY){
                endX = e.getX();
                endY = e.getY();
                // 清除之前的矩形，绘制新的
                drawRectAndDotInfo(gc,startX,startY, endX, endY, canvas.getWidth(),canvas.getHeight());
            }
        });

        canvas.setOnMouseReleased(e -> {
            if(!isDoubleClick()&&e.getButton()==MouseButton.PRIMARY){
                overlayStage.setOpacity(0);
                overlayStage.close();
                Image image = captureAndShowScreenshot((int) Math.min(startX, endX), (int) Math.min(startY, endY),
                        (int) Math.abs(endX - startX), (int) Math.abs(endY - startY),true);
                if(CaptureProperties.autoCopy){
                    TransferableImage transferableImage = new TransferableImage(image);
                    // 复制到剪贴板
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferableImage, null);
                }
            }

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

    private void drawRectAndDotInfo(GraphicsContext gc,double startX, double startY, double endX, double endY,double w,double h) {
        gc.clearRect(0, 0, w, h);
        gc.setStroke(Color.GREEN);
        gc.strokeRect(Math.min(startX, endX), Math.min(startY, endY),
                Math.abs(endX - startX), Math.abs(endY - startY));
        gc.setFill(Color.GREEN);
        double centerX = (startX+endX)/2;
        double centerY = (startY+endY)/2;
        gc.fillRect(startX-3,startY-3,6,6);
        gc.fillRect(startX-3,endY-3,6,6);
        gc.fillRect(endX-3,endY-3,6,6);
        gc.fillRect(endX-3,startY-3,6,6);
        gc.fillRect(centerX-3,startY-3,6,6);
        gc.fillRect(centerX-3,endY-3,6,6);
        gc.fillRect(startX-3,centerY-3,6,6);
        gc.fillRect(endX-3, centerY-3, 3, 6);
        int width= (int) Math.abs(startX-endX);
        int height= (int) Math.abs(startY-endY);
        String info="w : "+width+", h : "+height;
        gc.setStroke(Color.GREEN);
        gc.setLineWidth(2);
        gc.setFont(new Font("楷体",12));
        gc.strokeText(info,Math.max(startX,endX)+10,Math.max(startY,endY)+10,100);
    }

    private boolean isDoubleClick() {
        return Math.abs(startX-endX)<=20||Math.abs(startY-endY)<=20||endX==0||endY==0;
    }

    private Image captureAndShowScreenshot(int x, int y, int width, int height,boolean show) {
        javafx.scene.image.Image fxImage;
        try {
            // JNA截取屏幕
            BufferedImage screenshot;
            if ("javafx Robot".equals(CaptureProperties.captureType)) {
                screenshot = ScreenCaptureUtil.captureWithFX(x, y, width, height);
            } else if ("java awt Robot".equals(CaptureProperties.captureType)) {
                screenshot = ScreenCaptureUtil.captureWithAWT(x, y, width, height);
            } else if("Python's pillow".equals(CaptureProperties.captureType)){
                screenshot = ScreenCaptureUtil.captureWithPython(x, y, width, height);
            }else{
                screenshot = ScreenCaptureUtil.captureWithJNA(x,y,width,height);
            }
            if(screenshot==null){
                //TODO
                //alert
                return null;
            }
            // 转换为 JavaFX Image
            fxImage = ImageFormatHandler.bufferedToFXImage(screenshot);
            // 显示截图弹窗
            if(show){
                showScreenshotPopup(fxImage);
            }
        } catch (IOException | AWTException e) {
            throw new RuntimeException(e);
        }
        return fxImage;
    }

    private void showScreenshotPopup(javafx.scene.image.Image image) {
        displayStage = new Stage();
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
        // 创建 ImageView 来显示图片
        javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(image);
        imageView.setPreserveRatio(true);  // 保持宽高比
        // 获取图片的实际宽高
        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();
        double scale = ScreenCaptureUtil.getScreenScale();
        //TODO
        //应该调整使得显示的窗口大小合适，且图像缩放也合适
        imageView.setFitWidth(imageWidth/scale);//scale*0.9f
        imageView.setFitHeight(imageHeight/scale);
        // 动态设置 displayStage 的大小
        displayStage.setWidth(imageView.getFitWidth());
        displayStage.setHeight(imageView.getFitHeight() + 100);

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
