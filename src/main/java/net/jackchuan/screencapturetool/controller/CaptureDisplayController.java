package net.jackchuan.screencapturetool.controller;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.jackchuan.screencapturetool.ScreenCaptureToolApp;
import net.jackchuan.screencapturetool.util.ImageFormatHandler;
import net.jackchuan.screencapturetool.util.TransferableImage;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/11/20 15:59
 */
public class CaptureDisplayController {
    @FXML
    private ComboBox<String> processType;
    private ImageView capture;
    private Image originalImage;
    @FXML
    private Slider strokeSlider;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private Canvas canvas;
    @FXML
    private Button drag, pencil, rubber, rect, filledRect, arrow, line, cos,oval;
    private Color forecolor = Color.RED;
    private int type = -2, stroke;
    private double initialX, initialY, startX, startY;
    private Stage parent;
    private List<WritableImage> drawRecords;
    private Stack<WritableImage> drawRecordsStack;
    private boolean isAltPressed,isShiftPressed,isCtrlPressed;

    @FXML
    public void initialize() {
        drawRecords = new ArrayList<>();
        drawRecordsStack = new Stack<>();
        colorPicker.setBackground(Background.fill(forecolor));

        // 初始化时设置 capture 和 canvas
        Platform.runLater(() -> {
            if (capture != null && capture.getImage() != null) {
                parent = (Stage) canvas.getScene().getWindow();
                canvas.setWidth(capture.getImage().getWidth());
                canvas.setHeight(capture.getImage().getHeight());
                drawImageToCanvas(capture.getImage());
                strokeSlider.valueProperty().addListener((newVal, oldVal, val) -> {
                    stroke = val.intValue();
                });

                // 保存初始图像到 drawRecords 中
                saveCurrentState();

                parent.getScene().setOnKeyPressed(e->{
                    if(e.getCode()==KeyCode.SHIFT){
                        rubberMode();
                        isShiftPressed=true;
                    }
                    if(e.getCode()==KeyCode.ALT){
                        dragMode();
                        isAltPressed=true;
                    }
                    if(e.getCode()==KeyCode.CONTROL){
                        isCtrlPressed=true;
                    }
                    if(e.getCode()==KeyCode.E){
                        rubberMode();
                    }
                    if(e.getCode()== KeyCode.P){
                        pencilMode();
                    }
                    if(e.getCode()== KeyCode.R){
                        drawRect();
                    }
                    if(e.getCode()== KeyCode.A){
                        drawArrow();
                    }
                    if(e.getCode()== KeyCode.L){
                        drawLine();
                    }
                    if(e.getCode()== KeyCode.W){
                        drawCos();
                    }
                    if(e.getCode()== KeyCode.O){
                        drawOval();
                    }
                    if(e.getCode()== KeyCode.TAB){
                        showEditHistory();
                    }
                    if(e.getCode()== KeyCode.UP){
                        strokeSlider.setValue(strokeSlider.getValue()+0.5);
                    }
                    if(e.getCode()== KeyCode.DOWN){
                        strokeSlider.setValue(strokeSlider.getValue()-0.5);
                    }
                    if(e.getCode()== KeyCode.LEFT){
                        undo();
                    }
                    if(e.getCode()== KeyCode.RIGHT){
                        redo();
                    }
                    if(e.getCode()== KeyCode.C){
                        colorPicker.setVisible(true);
                    }
                    if(e.getCode()== KeyCode.S&&isCtrlPressed){
                        saveCapture();
                    }
                    if(e.getCode()== KeyCode.C&&isCtrlPressed){
                        copy();
                    }
                });

                parent.getScene().setOnKeyReleased(e->{
                    if(e.getCode()==KeyCode.SHIFT){
                        type=-2;
                        canvas.setCursor(Cursor.DEFAULT);
                        isShiftPressed=false;
                    }
                    if(e.getCode()==KeyCode.ALT){
                        type=-2;
                        canvas.setCursor(Cursor.DEFAULT);
                        isAltPressed=false;
                    }
                    if(e.getCode()==KeyCode.CONTROL){
                        isCtrlPressed=false;
                    }
                });
                // 鼠标拖动和缩放事件
                canvas.setOnScroll(event -> {
                    double zoomFactor = 1.1;
                    if (event.getDeltaY() < 0) {
                        zoomFactor = 1 / zoomFactor;
                    }
                    canvas.setScaleX(canvas.getScaleX() * zoomFactor);
                    canvas.setScaleY(canvas.getScaleY() * zoomFactor);
                });

                // 监听鼠标按下和拖动事件
                canvas.setOnMousePressed(event -> {
                    if (type == 0) {
                        initialX = event.getSceneX();
                        initialY = event.getSceneY();
                    } else {
                        startX = canvas.sceneToLocal(event.getSceneX(), event.getSceneY()).getX();
                        startY = canvas.sceneToLocal(event.getSceneX(), event.getSceneY()).getY();
                    }
                });

                canvas.setOnMouseDragged(event -> {
                    GraphicsContext g2 = canvas.getGraphicsContext2D();
                    if(type==-1){
                        //rubber mode

                    } else if (type == 0) {
                        double deltaX = event.getSceneX() - initialX;
                        double deltaY = event.getSceneY() - initialY;
                        // 更新 Canvas 的平移
                        canvas.setTranslateX(canvas.getTranslateX() + deltaX);
                        canvas.setTranslateY(canvas.getTranslateY() + deltaY);

                        initialX = event.getSceneX();
                        initialY = event.getSceneY();
                    } else if (type == 1) {
                        // 绘制普通线条
                        g2.setStroke(forecolor);
                        g2.setLineWidth(strokeSlider.getValue());
                        g2.strokeLine(startX, startY, event.getX(), event.getY());
                        startX = canvas.sceneToLocal(event.getSceneX(), event.getSceneY()).getX();
                        startY = canvas.sceneToLocal(event.getSceneX(), event.getSceneY()).getY();
                    } else if (type == 2) {
                        //绘制矩形框
                        g2.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                        repaintCanvas(drawRecords.getLast());
                        g2.setStroke(forecolor);
                        g2.setLineWidth(strokeSlider.getValue());
                        // 计算鼠标当前位置与起始位置之间的宽度和高度
                        double currentX = event.getX();
                        double currentY = event.getY();
                        // 绘制空心矩形框
                        double width = Math.abs(currentX - startX);
                        double height = Math.abs(currentY - startY);
                        if (startX < currentX && startY < currentY) {
                            g2.strokeRect(startX, startY, width, height);
                        } else if (startX < currentX && startY > currentY) {
                            g2.strokeRect(startX, currentY, width, height);
                        } else if (startX > currentX && startY < currentY) {
                            g2.strokeRect(currentX, startY, width, height);
                        } else if (startX > currentX && startY > currentY) {
                            g2.strokeRect(currentX, currentY, width, height);
                        }

                    } else if (type == 3) {
                        //绘制半透明填充的矩形框
                        g2.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                        repaintCanvas(drawRecords.getLast());
                        g2.setStroke(forecolor);
                        g2.setLineWidth(strokeSlider.getValue());
                        // 计算鼠标当前位置与起始位置之间的宽度和高度
                        double currentX = event.getX();
                        double currentY = event.getY();
                        // 绘制空心矩形框
                        double width = Math.abs(currentX - startX);
                        double height = Math.abs(currentY - startY);
                        if (startX < currentX && startY < currentY) {
                            g2.strokeRect(startX, startY, width, height);
                            g2.setFill(Color.rgb((int) (forecolor.getRed() * 255), ((int) forecolor.getGreen() * 255),
                                    (int) (forecolor.getBlue() * 255), 0.05));
                            g2.fillRect(startX, startY, width, height);
                        } else if (startX < currentX && startY > currentY) {
                            g2.strokeRect(startX, currentY, width, height);
                            g2.setFill(Color.rgb((int) (forecolor.getRed() * 255), ((int) forecolor.getGreen() * 255),
                                    (int) (forecolor.getBlue() * 255), 0.05));
                            g2.fillRect(startX, currentY, width, height);
                        } else if (startX > currentX && startY < currentY) {
                            g2.strokeRect(currentX, startY, width, height);
                            g2.setFill(Color.rgb((int) (forecolor.getRed() * 255), ((int) forecolor.getGreen() * 255),
                                    (int) (forecolor.getBlue() * 255), 0.05));
                            g2.fillRect(currentX, startY, width, height);
                        } else if (startX > currentX && startY > currentY) {
                            g2.strokeRect(currentX, currentY, width, height);
                            g2.setFill(Color.rgb((int) (forecolor.getRed() * 255), ((int) forecolor.getGreen() * 255),
                                    (int) (forecolor.getBlue() * 255), 0.05));
                            g2.fillRect(currentX, currentY, width, height);
                        }
                    } else if (type == 4) {
                        //绘制箭头
                        g2.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                        repaintCanvas(drawRecords.getLast());
                        g2.setLineWidth(strokeSlider.getValue());
                        drawArrow(g2, startX, startY, event.getX(), event.getY(), 10, 10);
                    } else if (type == 5) {
                        //绘制直线
                        g2.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                        repaintCanvas(drawRecords.getLast());
                        g2.setStroke(forecolor);
                        g2.setLineWidth(strokeSlider.getValue());
                        g2.strokeLine(startX, startY, event.getX(), event.getY());
                    } else if (type == 6) {
                        //绘制波浪线
                        g2.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                        repaintCanvas(drawRecords.getLast());
                        g2.setLineWidth(strokeSlider.getValue());
                        g2.setStroke(forecolor);
                        drawCos(g2, startX, startY, event.getX(), event.getY(), 5);
                    }
                });

                canvas.setOnMouseReleased(event -> {
                    saveCurrentState();
                });
            }
        });
    }

    private void showEditHistory() {

    }

    private void drawCos(GraphicsContext g2, double startX, double startY, double endX, double endY, int radius) {
        double dx = Math.abs(startX - endX);
        double dy = Math.abs(startY - endY);
        double t = Math.sqrt(dx * dx + dy * dy);
        double s = 0;
        while (s <= t) {
            if ((s / (2.5 * radius)) % 2 == 0) {
                // 上半弧
                g2.strokeArc(s + startX, startY, 2.5 * radius, 1.2 * radius, 0, 180, ArcType.OPEN);
            } else {
                // 下半弧
                g2.strokeArc(s + startX, startY, 2.5 * radius, 1.2 * radius, 0, -180, ArcType.OPEN);
            }
            s += 2.5 * radius; // 按每个弧的宽度（radius）移动
        }
    }

    public void drawArrow(GraphicsContext g2, double startX, double startY, double endX, double endY, double arrowLength, double arrowWidth) {
        // 计算箭头的角度
        double angle = Math.atan2(endY - startY, endX - startX);
        // 绘制箭头的主体线
        g2.setStroke(forecolor);
        g2.setLineWidth(2);
        g2.strokeLine(startX, startY, endX, endY);
        // 计算箭头头部的两个点
        double arrowAngle = Math.toRadians(30); // 箭头两侧的角度（可以根据需要调整）
        double x1 = endX - arrowLength * Math.cos(angle - arrowAngle);
        double y1 = endY - arrowLength * Math.sin(angle - arrowAngle);
        double x2 = endX - arrowLength * Math.cos(angle + arrowAngle);
        double y2 = endY - arrowLength * Math.sin(angle + arrowAngle);
        // 绘制箭头的两侧
        g2.setFill(forecolor);
        g2.fillPolygon(new double[]{endX, x1, x2}, new double[]{endY, y1, y2}, 3);
    }

    // 保存当前 Canvas 状态
    private void saveCurrentState() {
        WritableImage snapshot = canvas.snapshot(null, null);
        drawRecords.add(snapshot);
        drawRecordsStack.push(snapshot);
    }


    private void repaintCanvas(WritableImage image) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());  // 清空画布
        gc.drawImage(image, 0, 0, canvas.getWidth(), canvas.getHeight());
    }

    // 将 ImageView 中的图片绘制到 Canvas 上
    private void drawImageToCanvas(Image image) {
        // 获取 canvas 的 GraphicsContext
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setImageSmoothing(false);
        // 在 canvas 上绘制图像
        gc.drawImage(image, 0, 0, canvas.getWidth(), canvas.getHeight());
    }

    @FXML
    public void upload() {
        // 处理上传逻辑
        FileChooser fc = new FileChooser();
        fc.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("图片", ".png", ".jpg"));
        File file = fc.showOpenDialog(null);
        capture.setImage(new Image(file.toURI().toString()));
        resizeStage();
        repaintCanvas(capture.getImage());
        originalImage = capture.getImage();
    }

    @FXML
    public void saveCapture() {
        // 处理保存截图逻辑
        WritableImage writableImage = canvas.snapshot(null, null);
        // 转换为 BufferedImage
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);

        // 保存为文件
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image");
        fileChooser.setInitialFileName("capture.png");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                ImageIO.write(bufferedImage, "png", file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @FXML
    public void copy() {
        TransferableImage transferableImage = new TransferableImage(canvas.snapshot(null,null));
        // 复制到剪贴板
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferableImage, null);
    }

    @FXML
    public void pencilMode() {
        // 处理铅笔模式
        type = 1;
        Image cursorImage = new Image(ScreenCaptureToolApp.class.getResource("assets/icon/pencil.png").toExternalForm()); // 替换为你自己的图像路径
        ImageCursor customCursor = new ImageCursor(cursorImage, 0, 32); // (16,16) 是热点位置（图像的中心）
        canvas.setCursor(customCursor);
        enableOthers();
        pencil.setDisable(true);
    }

    @FXML
    public void rubberMode() {
        // 处理橡皮模式
        type = -1;
        Image cursorImage = new Image(ScreenCaptureToolApp.class.getResource("assets/icon/rubber.png").toExternalForm()); // 替换为你自己的图像路径
        ImageCursor customCursor = new ImageCursor(cursorImage, 0, 32); // (16,16) 是热点位置（图像的中心）
        canvas.setCursor(customCursor);
        enableOthers();
        rubber.setDisable(true);
    }

    @FXML
    public void drawRect() {
        // 处理绘制矩形逻辑
        type = 2;
        canvas.setCursor(Cursor.CROSSHAIR);
        enableOthers();
        rect.setDisable(true);
    }

    @FXML
    public void drawFilledRect() {
        // 处理绘制填充矩形逻辑
        type = 3;
        canvas.setCursor(Cursor.CROSSHAIR);
        enableOthers();
        filledRect.setDisable(true);
    }

    @FXML
    public void drawArrow() {
        // 处理绘制箭头逻辑
        type = 4;
        canvas.setCursor(Cursor.CROSSHAIR);
        enableOthers();
        arrow.setDisable(true);
    }

    @FXML
    public void drawLine() {
        // 处理绘制直线逻辑
        type = 5;
        canvas.setCursor(Cursor.CROSSHAIR);
        enableOthers();
        line.setDisable(true);
    }

    @FXML
    private void drawCos() {
        this.type = 6;
        canvas.setCursor(Cursor.CROSSHAIR);
        enableOthers();
        cos.setDisable(true);
    }

    @FXML
    public void colorPicker() {
        forecolor = colorPicker.getValue();
        colorPicker.setBackground(Background.fill(forecolor));
    }

    @FXML
    public void undo() {
        if (drawRecordsStack.size() > 1) {  // 至少保留一个初始图像
            drawRecordsStack.pop();  // 移除当前状态
            WritableImage previousState = drawRecordsStack.pop();
            repaintCanvas(previousState);
        }
    }

    // 重做操作
    @FXML
    public void redo() {
        // 实现重做功能时，需要维护一个 redo 记录列表。
        // 当前代码没有实现 redo，但可以在撤销时将状态保存到 redo 列表中
        // 并在需要时恢复状态。
        if(drawRecordsStack.size()+1<drawRecords.size()){
            WritableImage image = drawRecords.get(drawRecordsStack.size() + 1);
            repaintCanvas(image);
            drawRecordsStack.push(image);
        }
    }

    @FXML
    private void resetImage() {
        canvas.setScaleX(1);
        canvas.setScaleY(1);
        canvas.setTranslateX(0);
        canvas.setTranslateY(0);
        resizeStage();
        drawImageToCanvas(capture.getImage());
    }

    @FXML
    private void dragMode() {
        type = 0;
        canvas.setCursor(Cursor.HAND);
    }

    public void repaintCanvas(Image image) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setImageSmoothing(false);
        // 在 canvas 上绘制图像
        gc.drawImage(image, 0, 0, canvas.getWidth(), canvas.getHeight());
        canvas.requestFocus();
    }

    private void resizeStage() {
        if (parent != null) {
            parent.setWidth(capture.getImage().getWidth());
            parent.setHeight(capture.getImage().getHeight() + 100);
            canvas.setWidth(capture.getImage().getWidth());
            canvas.setHeight(capture.getImage().getHeight());
        }
    }

    public void setCapture(ImageView capture) {
        this.capture = capture;
        this.originalImage = capture.getImage();
        if (capture.getImage() != null) {
            drawImageToCanvas(capture.getImage());
        }
    }

    private void enableOthers() {
        drag.setDisable(false);
        pencil.setDisable(false);
        rubber.setDisable(false);
        rect.setDisable(false);
        filledRect.setDisable(false);
        arrow.setDisable(false);
        line.setDisable(false);
        cos.setDisable(false);
        oval.setDisable(false);
    }

    @FXML
    private void processImage() throws IOException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        if("灰化".equals(processType.getValue())){
            Mat src = ImageFormatHandler.toMat(canvas.snapshot(null,null));
            Mat gray = new Mat();
            Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
            WritableImage image=SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(gray),null);
            drawImageToCanvas(image);
            saveCurrentState();
        }else if("锐化".equals(processType.getValue())){
            Mat src = ImageFormatHandler.toMat(canvas.snapshot(null,null));
            Mat sharpenKernel = new Mat(3, 3, CvType.CV_32F);
            sharpenKernel.put(0, 0, 0, -1, 0, -1, 5, -1, 0, -1, 0);  // 常见的锐化卷积核
            // 通过卷积操作锐化图像
            Mat sharpenedImage = new Mat();
            Imgproc.filter2D(src, sharpenedImage, src.depth(), sharpenKernel);
            WritableImage image=SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(sharpenedImage),null);
            drawImageToCanvas(image);
            saveCurrentState();
        }else if("分割".equals(processType.getValue())){
            Mat src = ImageFormatHandler.toMat(canvas.snapshot(null,null));
            Mat mask = new Mat(src.size(), CvType.CV_8UC1, new Scalar(Imgproc.GC_PR_BGD));
            Mat bgModel = new Mat();
            Mat fgModel = new Mat();
            // 矩形框 (前景的初步估计)
            Rect rect = new Rect(50, 50, 200, 200);
            // 应用 GrabCut 算法
            Imgproc.grabCut(src, mask, rect, bgModel, fgModel, 5, Imgproc.GC_INIT_WITH_RECT);
            // 提取前景
            Mat resultMask = new Mat();
            Core.compare(mask, new Scalar(Imgproc.GC_PR_FGD), resultMask, Core.CMP_EQ);
            // 将前景从原图中提取
            Mat foreground = new Mat();
            src.copyTo(foreground, resultMask);
            WritableImage image=SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(fgModel),null);
            drawImageToCanvas(image);
            saveCurrentState();
        }else if("边缘提取".equals(processType.getValue())){
            Mat src = ImageFormatHandler.toMat(canvas.snapshot(null,null));
            Mat edges = new Mat();
            Imgproc.Canny(src, edges, 100, 200);
            WritableImage image=SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(edges),null);
            drawImageToCanvas(image);
            saveCurrentState();
        }else if("均值平滑".equals(processType.getValue())){
            Mat src = ImageFormatHandler.toMat(canvas.snapshot(null,null));
            Mat meanBlurred = new Mat();
            Size kernelSize = new Size(5, 5);  // 5x5的卷积核
            Imgproc.blur(src, meanBlurred, kernelSize);
            WritableImage image=SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(meanBlurred),null);
            drawImageToCanvas(image);
            saveCurrentState();
        }else if("高斯平滑".equals(processType.getValue())){
            Mat src = ImageFormatHandler.toMat(canvas.snapshot(null,null));
            Mat meanBlurred = new Mat();
            Size kernelSize = new Size(5, 5);  // 5x5的卷积核
            Imgproc.GaussianBlur(src, meanBlurred, kernelSize,0);
            WritableImage image=SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(meanBlurred),null);
            drawImageToCanvas(image);
            saveCurrentState();
        } else if("人脸识别".equals(processType.getValue())){
            Mat src = ImageFormatHandler.toMat(canvas.snapshot(null,null));
            Mat meanBlurred = new Mat();
            Size kernelSize = new Size(5, 5);  // 5x5的卷积核
            Imgproc.blur(src, meanBlurred, kernelSize);
            WritableImage image=SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(meanBlurred),null);
            drawImageToCanvas(image);
            saveCurrentState();
        }
    }

    public void clearAllRecord() {
        GraphicsContext g2 = canvas.getGraphicsContext2D();
        g2.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        capture.setImage(originalImage);
        resizeStage();
        g2.drawImage(originalImage, 0, 0, canvas.getWidth(), canvas.getHeight());
        WritableImage image = new WritableImage(originalImage.getPixelReader(),
                (int) originalImage.getWidth(), (int) originalImage.getHeight());
        drawRecords.add(image);
        drawRecordsStack.add(image);
    }

    @FXML
    private void drawOval() {
        type=7;
        enableOthers();
        oval.setDisable(true);
    }
}
