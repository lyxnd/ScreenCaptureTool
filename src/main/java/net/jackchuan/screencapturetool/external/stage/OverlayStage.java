package net.jackchuan.screencapturetool.external.stage;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;
import net.jackchuan.screencapturetool.CaptureProperties;
import net.jackchuan.screencapturetool.ScreenCaptureToolApp;
import net.jackchuan.screencapturetool.controller.CaptureDisplayController;
import net.jackchuan.screencapturetool.util.ImageDetector;
import net.jackchuan.screencapturetool.util.ImageFormatHandler;
import net.jackchuan.screencapturetool.util.ScreenCaptureUtil;
import net.jackchuan.screencapturetool.util.TransferableImage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;


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
    private Stage displayStage;
    private double startX, startY, endX, endY;
    private double preX=-100,preY=-100;
    private ContextMenu popMenu;
    private MenuItem fullCut,test,test1;
    private Pair<Integer, Integer> size;
    public OverlayStage(){
        size = ScreenCaptureUtil.getScreenSize(ScreenCaptureUtil.DEFAULT_SIZE);
        canvas=new Canvas(size.getKey(), size.getValue());
        gc=canvas.getGraphicsContext2D();
        stackPane=new StackPane(canvas);
        scene=new Scene(stackPane,size.getKey(),size.getValue());
        popMenu=new ContextMenu();
        fullCut=new javafx.scene.control.MenuItem("全屏选择");
        test=new javafx.scene.control.MenuItem("save shot");
        test1=new MenuItem("detect rect");
        stackPane.setBackground(Background.EMPTY);
        scene.setFill(null);
        canvas.setCursor(Cursor.CROSSHAIR);
        gc.setFill(Color.rgb(236, 236, 236, 0.1));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        popMenu.getItems().addAll(fullCut,test,test1);
        initialAction();
        this.setScene(scene);
        this.setMaximized(true);
        this.initStyle(StageStyle.TRANSPARENT);
        this.getIcons().add(new Image(ScreenCaptureToolApp
                .class.getResource("assets/icon/shortcut.png").toExternalForm()));
        this.setAlwaysOnTop(true);
    }
    public OverlayStage(Image image){
        this.image=image;
        size = ScreenCaptureUtil.getScreenSize(ScreenCaptureUtil.DEFAULT_SIZE);
        canvas=new Canvas(size.getKey(),size.getValue());
        gc=canvas.getGraphicsContext2D();
        stackPane=new StackPane(canvas);
        scene=new Scene(stackPane,size.getKey(),size.getValue());
        popMenu=new ContextMenu();
        fullCut=new javafx.scene.control.MenuItem("全屏选择");
        test=new javafx.scene.control.MenuItem("save shot");
        test1=new MenuItem("detect rect");
        stackPane.setBackground(Background.EMPTY);
        scene.setFill(null);
        canvas.setCursor(Cursor.CROSSHAIR);
        gc.drawImage(image,0,0,size.getKey(),size.getValue());
        popMenu.getItems().addAll(fullCut,test,test1);
        initialAction();
        this.setScene(scene);
        this.setMaximized(true);
        this.initStyle(StageStyle.TRANSPARENT);
        this.getIcons().add(new Image(ScreenCaptureToolApp
                .class.getResource("assets/icon/shortcut.png").toExternalForm()));
        this.setAlwaysOnTop(true);
    }

    private void initialAction() {
        canvas.setOnMouseClicked(e->{
            if(e.getClickCount()==2){
                closeOverlayStage();
                captureAndShowScreenshot(0,0,size.getKey(),size.getValue(),true);
            }
        });
        fullCut.setOnAction(e->{
            closeOverlayStage();
            captureAndShowScreenshot(0,0,size.getKey(),size.getValue(),true);
        });
        test.setOnAction(e->{
            closeOverlayStage();
            Image snapshot = captureAndShowScreenshot(0,0,size.getKey(),size.getValue(),false);
            BufferedImage image = ImageFormatHandler.toBufferedImage(snapshot);
            try {
                ImageIO.write(image,"png",new File("F:/java_practise/ScreenCaptureTool/temp/test.png"));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        test1.setOnAction(e->{
            closeOverlayStage();
            Image snapshot = captureAndShowScreenshot(0,0,size.getKey(),size.getValue(),false);
            ImageDetector.detectRect(snapshot);
        });
        // 监听鼠标拖动事件
        canvas.setOnMousePressed(e -> {
            if(e.getButton()== MouseButton.SECONDARY){
                popMenu.show(this, e.getScreenX(), e.getScreenY());
            }
            startX = e.getX();
            startY = e.getY();
        });
        canvas.setOnMouseMoved(e -> {
            //TODO
            if(CaptureProperties.autoSelect){
                if(preX==-100||preY==-100){
                    preX=e.getX();
                    preY=e.getY();
                }
                double deltaX=e.getX()-preX;
                double deltaY=e.getY()-preY;
                if(deltaX>=50||deltaY>=50){
                    handleSelectArea();
                }
            }
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
                closeOverlayStage();
                Image image = captureAndShowScreenshot((int) Math.min(startX, endX), (int) Math.min(startY, endY),
                        (int) Math.abs(endX - startX), (int) Math.abs(endY - startY),true);
                if(CaptureProperties.autoCopy){
                    TransferableImage transferableImage = new TransferableImage(image);
                    // 复制到剪贴板
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferableImage, null);
                }
            }

        });
    }

    private void handleSelectArea() {
        CompletableFuture.supplyAsync(() -> ImageDetector.detectRect(image)).thenAccept(result -> {
            // 在任务完成后处理结果
            gc.clearRect(0,0,canvas.getWidth(),canvas.getHeight());
            gc.drawImage(result,0,0,canvas.getWidth(),canvas.getHeight());
        });
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
            fxImage = ImageFormatHandler.toFXImage(screenshot);
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
        // 设置控制器
        CaptureDisplayController controller = loader.getController();
        controller.setCapture(image,ScreenCaptureUtil.shouldScale(image));
        controller.setOriginalImage(image);
        // 设置场景并显示窗口
        displayStage.setScene(scene);
        displayStage.show();
    }

    private void closeOverlayStage() {
        this.close();
        this.setOpacity(0);
    }
    private boolean isDoubleClick() {
        return Math.abs(startX-endX)<=20||Math.abs(startY-endY)<=20||endX==0||endY==0;
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
}
