package net.jackchuan.screencapturetool.controller;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.jackchuan.screencapturetool.CaptureProperties;
import net.jackchuan.screencapturetool.ScreenCaptureToolApp;
import net.jackchuan.screencapturetool.external.ExternalImageHandler;
import net.jackchuan.screencapturetool.external.ExternalImageHandler.DrawableImage;
import net.jackchuan.screencapturetool.util.*;
import net.jackchuan.screencapturetool.util.impl.DrawType;
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

    public Label state;
    @FXML
    private ComboBox<String> processType;
    private ImageView capture;
    private Image originalImage;
    @FXML
    private Slider strokeSlider;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private Canvas canvas,editArea;
    @FXML
    private ToolBar tools;
    @FXML
    private Button drag, pencil, rubber, rect, filledRect, arrow, line, cos, oval, addImage, addText;
    private Color forecolor = Color.RED;
    private int type = -2, stroke;
    private double initialX, initialY, startX, startY;
    private Stage parent, history, settingStage;
    private List<WritableImage> drawRecords;
    private Stack<WritableImage> drawRecordsStack;
    private List<String> editHistory;
    private boolean isAltPressed, isShiftPressed, isCtrlPressed;
    private EditRecordController editController;
    private SettingController settingController;
    private ExternalImageHandler imageHandler;
    private DrawableImage selectedImage = null;
    private double dragStartX;
    private double dragStartY;
    private boolean isResizing = false;

    private ContextMenu popMenu;
    private MenuItem delete,add;
    @FXML
    public void initialize() {
        drawRecords = new ArrayList<>();
        drawRecordsStack = new Stack<>();
        editHistory = new ArrayList<>();
        imageHandler=new ExternalImageHandler(canvas);
        colorPicker.setBackground(Background.fill(forecolor));
        // 初始化时设置 capture 和 canvas
        canvas.setVisible(true);
        editArea.setVisible(true);
        Platform.runLater(() -> {
            if (capture != null && capture.getImage() != null) {
                parent = (Stage) canvas.getScene().getWindow();
                canvas.setWidth(capture.getFitWidth());
                canvas.setHeight(capture.getFitHeight());
                drawImageToCanvas(capture.getImage());
                state.setText(getState());
                strokeSlider.valueProperty().addListener((newVal, oldVal, val) -> {
                    stroke = val.intValue();
                });
                popMenu=new ContextMenu();
                delete = new MenuItem("删除");
                add = new MenuItem("添加");
                popMenu.getItems().addAll(add,delete);
                delete.setOnAction(event -> {
                    if (selectedImage != null) {
                        imageHandler.removeExternalImage(selectedImage);
                        selectedImage = null;
                        canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                        repaintCanvas(drawRecords.getLast());
                        imageHandler.drawAllImages(canvas.getGraphicsContext2D(), selectedImage);
                    }
                });
                add.setOnAction(e->addExternalImage(canvas.getWidth()*0.4,canvas.getHeight()*0.4));
                // 保存初始图像到 drawRecords 中
                saveCurrentState("初始化图片(0)");
                tools.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode() == KeyCode.TAB) {
                        event.consume(); // 阻止默认的焦点切换行为
                        showEditHistory();
                    }
                });
                parent.getScene().setOnKeyPressed(e -> {
                    if(e.getCode()==KeyCode.ESCAPE){
                        copy();
                        parent.close();
                    }
                    if (e.getCode() == KeyCode.SHIFT) {
                        rubberMode();
                        isShiftPressed = true;
                    }
                    if (e.getCode() == KeyCode.ALT) {
                        dragMode();
                        isAltPressed = true;
                    }
                    if (e.getCode() == KeyCode.CONTROL) {
                        isCtrlPressed = true;
                    }
                    if (e.getCode() == KeyCode.E && CaptureProperties.rubber) {
                        rubberMode();
                    }
                    if (e.getCode() == KeyCode.P && CaptureProperties.pencil) {
                        pencilMode();
                    }
                    if (e.getCode() == KeyCode.R && CaptureProperties.rect) {
                        drawRect();
                    }
                    if (e.getCode() == KeyCode.A && CaptureProperties.arrow) {
                        drawArrow();
                    }
                    if (e.getCode() == KeyCode.L && CaptureProperties.line) {
                        drawLine();
                    }
                    if (e.getCode() == KeyCode.W && CaptureProperties.wave) {
                        drawCos();
                    }
                    if (e.getCode() == KeyCode.O && CaptureProperties.oval) {
                        drawOval();
                    }
                    if (e.getCode() == KeyCode.UP && CaptureProperties.strokeUp) {
                        strokeSlider.setValue(strokeSlider.getValue() + 0.5);
                    }
                    if (e.getCode() == KeyCode.DOWN && CaptureProperties.strokeDown) {
                        strokeSlider.setValue(strokeSlider.getValue() - 0.5);
                    }
                    if (e.getCode() == KeyCode.LEFT && CaptureProperties.undo) {
                        undo();
                    }
                    if (e.getCode() == KeyCode.RIGHT && CaptureProperties.redo) {
                        redo();
                    }
                    if (e.getCode() == KeyCode.C && CaptureProperties.color) {
                        colorPicker.setVisible(true);
                    }
                    if (e.getCode() == KeyCode.S && isCtrlPressed && CaptureProperties.export) {
                        saveCapture();
                    }
                    if (e.getCode() == KeyCode.C && isCtrlPressed && CaptureProperties.copy) {
                        copy();
                    }
                    if (e.getCode() == KeyCode.Z && isCtrlPressed && CaptureProperties.undo) {
                        undo();
                    }
                });

                parent.getScene().setOnKeyReleased(e -> {
                    if (e.getCode() == KeyCode.SHIFT) {
                        type = -2;
                        canvas.setCursor(Cursor.DEFAULT);
                        isShiftPressed = false;
                    }
                    if (e.getCode() == KeyCode.ALT) {
                        type = -2;
                        canvas.setCursor(Cursor.DEFAULT);
                        isAltPressed = false;
                    }
                    if (e.getCode() == KeyCode.CONTROL) {
                        isCtrlPressed = false;
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
                    state.setText(getState());
                });

                canvas.setOnMouseClicked(e->{
                    if (type == 8&&e.getClickCount()==2) {
                        addExternalImage(e.getX(),e.getY());
                    }else if(type==9&&e.getClickCount()==2){
//                        saveCurrentState("添加文字(" + drawRecords.size() + ")");
                    }
                });

                // 监听鼠标按下和拖动事件
                canvas.setOnMousePressed(event -> {
                    selectedImage = null; // 清除之前的选中状态
                    for (DrawableImage image : imageHandler.getImages()) {
                        if (image.isInside(event.getX(), event.getY())) {
                            selectedImage = image;
                            break;
                        }
                    }
                    if (event.getButton() == MouseButton.SECONDARY) { // 右键
                        popMenu.show(canvas, event.getScreenX(), event.getScreenY());
                    } else if (event.getButton() != MouseButton.PRIMARY) {
                        selectedImage = null;
                        popMenu.hide();
                        imageHandler.drawAllImages(canvas.getGraphicsContext2D(), selectedImage);
                    }
                    if(event.getButton()!=MouseButton.SECONDARY){
                        popMenu.hide();
                    }
                    if (type == 0) {
                        initialX = event.getSceneX();
                        initialY = event.getSceneY();
                    }else if(type==8){
                        for (ExternalImageHandler.DrawableImage image : imageHandler.getImages()) {
                            if (image.isInside(event.getX(), event.getY())) {
                                selectedImage = image;
                                dragStartX = event.getX();
                                dragStartY = event.getY();
                                isResizing = imageHandler.isNearBorder(image, event.getX(), event.getY());
                                break;
                            }
                        }
                    }else {
                        startX = event.getX();
                        startY = event.getY();
                    }
                    imageHandler.drawAllImages(canvas.getGraphicsContext2D(), selectedImage);
                });

                canvas.setOnMouseDragged(event -> {
                    GraphicsContext imgContext = canvas.getGraphicsContext2D();
                    GraphicsContext editContext = editArea.getGraphicsContext2D();
                    if (type == -1) {
                        //rubber mode
                        imgContext.clearRect(event.getX(), event.getY(), 10, 10);
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
                        imgContext.setStroke(forecolor);
                        imgContext.setLineWidth(strokeSlider.getValue());
                        imgContext.strokeLine(startX, startY, event.getX(), event.getY());
                        startX = canvas.sceneToLocal(event.getSceneX(), event.getSceneY()).getX();
                        startY = canvas.sceneToLocal(event.getSceneX(), event.getSceneY()).getY();
                    } else {
                        imgContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                        repaintCanvas(drawRecords.getLast());
                        imgContext.setStroke(forecolor);
                        imgContext.setLineWidth(strokeSlider.getValue());
                        double currentX = event.getX();
                        double currentY = event.getY();
                        switch (type) {
                            //绘制矩形框
                            case 2 -> {
                                DrawType.RECTANGLE.draw(imgContext, startX, startY, currentX, currentY);
                            }
                            //绘制半透明填充的矩形框
                            case 3 -> {
                                DrawType.FILLED_RECTANGLE.draw(imgContext, startX, startY, currentX, currentY, forecolor);
                            }
                            //绘制箭头
                            case 4 -> {
                                DrawType.ARROW.draw(imgContext, startX, startY, currentX, currentY, forecolor);
                            }
                            //绘制直线
                            case 5 -> {
                                DrawType.LINE.draw(imgContext, startX, startY, currentX, currentY);
                            }
                            //绘制波浪线
                            case 6 -> {
                                DrawType.WAVE.draw(imgContext, startX, startY, currentX, currentY);
                            }
                            //绘制圆
                            case 7 -> {
                                DrawType.CIRCLE.draw(imgContext, startX, startY, currentX, currentY);
                            }
                            //绘制图片拖动及缩放
                            case 8 -> {
                                if (selectedImage != null) {
                                    if (isResizing) {
//                                        canvas.setCursor(Cursor.E_RESIZE);
                                        double newWidth = selectedImage.getWidth() + (event.getX() - (selectedImage.getX() + selectedImage.getWidth()));
                                        double newHeight = selectedImage.getHeight() + (event.getY() - (selectedImage.getY() + selectedImage.getHeight()));
                                        if (newWidth > 50 && newHeight > 50) {
                                            selectedImage.setWidth(newWidth);
                                            selectedImage.setHeight(newHeight);
                                        }
                                    } else {
                                        double offsetX = event.getX() - dragStartX;
                                        double offsetY = event.getY() - dragStartY;
                                        selectedImage.setX(selectedImage.getX()+offsetX);
                                        selectedImage.setY(selectedImage.getY()+offsetY);
                                        dragStartX = event.getX();
                                        dragStartY = event.getY();
                                    }
                                }
                                imgContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                                repaintCanvas(drawRecords.getLast());
                                imageHandler.drawAllImages(imgContext, selectedImage);
                            }
                            case 9 -> {

                            }

                        }
                    }
                });

                canvas.setOnMouseReleased(event -> {
                    String editType;
                    switch (type) {
                        case 0 -> editType = "图片移动(" + drawRecords.size() + ")";
                        case 1 -> editType = "普通绘画(" + drawRecords.size() + ")";
                        case 2 -> editType = "绘制矩形框(" + drawRecords.size() + ")";
                        case 3 -> editType = "绘制透明填充的矩形框(" + drawRecords.size() + ")";
                        case 4 -> editType = "绘制箭头(" + drawRecords.size() + ")";
                        case 5 -> editType = "绘制直线(" + drawRecords.size() + ")";
                        case 6 -> editType = "绘制波浪线(" + drawRecords.size() + ")";
                        case 7 -> editType = "绘制圆(" + drawRecords.size() + ")";
                        default -> editType = "未知操作(" + drawRecords.size() + ")";
                    }
                    if(type!=8&&type!=9){
                        isResizing=false;
                        saveCurrentState(editType);
                    }
                });
            }
            ControllerInstance.getInstance().setController(this);
        });
    }

    private void addExternalImage(double x,double y) {
        FileChooser chooser = new FileChooser();
        chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter(
                "图片", "png", "jpg"
        ));
        File file = chooser.showOpenDialog(parent);
        if (file != null) {
            Image image = new Image("file:" + file.getAbsolutePath());
            //TODO : get scaled size
            IntegerPair size = ImageFormatHandler.getScaledSize(image,canvas.getWidth(),canvas.getHeight());
            DrawableImage drawableImage = new DrawableImage(image, x,y, size.getW(), size.getH(),file.getName());
            imageHandler.addExternalImage(drawableImage);
            imageHandler.drawAllImages(canvas.getGraphicsContext2D(),selectedImage);
//                            saveCurrentState("添加图片(" + drawRecords.size() + ")");
        }
    }

    @FXML
    private void addImage() {
        type = 8;
        canvas.setCursor(Cursor.HAND);
        enableOthers();
        addImage.setDisable(true);
    }

    @FXML
    private void addText() {
        type = 9;
        canvas.setCursor(Cursor.CROSSHAIR);
        enableOthers();
        addText.setDisable(true);
    }

    private void showEditHistory() {
        history = new Stage();
        FXMLLoader loader = new FXMLLoader(ScreenCaptureToolApp.class.getResource("edit_record.fxml"));
        Scene scene = null;
        try {
            scene = new Scene(loader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 设置控制器
        editController = loader.getController();
        editController.setRecordsList(editHistory);

        // 设置场景并显示窗口
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        history.setScene(scene);
        history.setTitle("历史编辑记录");
        history.setX(size.getWidth() / 3);
        history.setY(size.getHeight() / 3);
        history.setWidth(size.getWidth() * 0.4);
        history.setHeight(size.getHeight() * 0.4);
        history.show();
    }

    // 保存当前 Canvas 状态
    public void saveCurrentState(String editType) {
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setTransform(Transform.scale(CaptureProperties.scale, CaptureProperties.scale));  // 扩大图像
        WritableImage snapshot = canvas.snapshot(parameters, null);
        drawRecords.add(snapshot);
        drawRecordsStack.push(snapshot);
        editHistory.add(editType);
        if (editController != null) {
            editController.addRecord(editType);
        }
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
        canvas.setScaleX(1);
        canvas.setScaleY(1);
        state.setText(getState());
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setTransform(Transform.scale(CaptureProperties.scale, CaptureProperties.scale));  // 扩大图像
        WritableImage writableImage = canvas.snapshot(parameters, null);
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
        canvas.setScaleX(1);
        canvas.setScaleY(1);
        state.setText(getState());
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setTransform(Transform.scale(CaptureProperties.scale, CaptureProperties.scale));  // 扩大图像
        TransferableImage transferableImage = new TransferableImage(canvas.snapshot(parameters, null));
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
        if (drawRecordsStack.size() + 1 < drawRecords.size()) {
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
        imageHandler.clearAll();
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
            parent.setWidth(capture.getFitWidth());
            parent.setHeight(capture.getFitHeight() + 100);
            canvas.setWidth(capture.getFitWidth());
            canvas.setHeight(capture.getFitHeight());
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
        if ("灰化".equals(processType.getValue())) {
            Mat src = ImageFormatHandler.toMat(canvas.snapshot(null, null));
            Mat gray = new Mat();
            Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
            WritableImage image = SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(gray), null);
            drawImageToCanvas(image);
            saveCurrentState("图像灰化处理");
        } else if ("锐化".equals(processType.getValue())) {
            Mat src = ImageFormatHandler.toMat(canvas.snapshot(null, null));
            Mat sharpenKernel = new Mat(3, 3, CvType.CV_32F);
            sharpenKernel.put(0, 0, 0, -1, 0, -1, 5, -1, 0, -1, 0);  // 常见的锐化卷积核
            // 通过卷积操作锐化图像
            Mat sharpenedImage = new Mat();
            Imgproc.filter2D(src, sharpenedImage, src.depth(), sharpenKernel);
            WritableImage image = SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(sharpenedImage), null);
            drawImageToCanvas(image);
            saveCurrentState("图像锐化处理");
        } else if ("分割".equals(processType.getValue())) {
            Mat src = ImageFormatHandler.toMat(canvas.snapshot(null, null));
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
            WritableImage image = SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(fgModel), null);
            drawImageToCanvas(image);
            saveCurrentState("图像分割处理");
        } else if ("边缘提取".equals(processType.getValue())) {
            Mat src = ImageFormatHandler.toMat(canvas.snapshot(null, null));
            Mat edges = new Mat();
            Imgproc.Canny(src, edges, 100, 200);
            WritableImage image = SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(edges), null);
            drawImageToCanvas(image);
            saveCurrentState("图像边缘处理");
        } else if ("均值平滑".equals(processType.getValue())) {
            Mat src = ImageFormatHandler.toMat(canvas.snapshot(null, null));
            Mat meanBlurred = new Mat();
            Size kernelSize = new Size(5, 5);  // 5x5的卷积核
            Imgproc.blur(src, meanBlurred, kernelSize);
            WritableImage image = SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(meanBlurred), null);
            drawImageToCanvas(image);
            saveCurrentState("图像均值平滑处理");
        } else if ("高斯平滑".equals(processType.getValue())) {
            Mat src = ImageFormatHandler.toMat(canvas.snapshot(null, null));
            Mat meanBlurred = new Mat();
            Size kernelSize = new Size(5, 5);  // 5x5的卷积核
            Imgproc.GaussianBlur(src, meanBlurred, kernelSize, 0);
            WritableImage image = SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(meanBlurred), null);
            drawImageToCanvas(image);
            saveCurrentState("图像高斯平滑处理");
        } else if ("人脸识别".equals(processType.getValue())) {
            Mat src = ImageFormatHandler.toMat(canvas.snapshot(null, null));
            Mat meanBlurred = new Mat();
            Size kernelSize = new Size(5, 5);  // 5x5的卷积核
            Imgproc.blur(src, meanBlurred, kernelSize);
            WritableImage image = SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(meanBlurred), null);
            drawImageToCanvas(image);
            saveCurrentState("人脸识别");
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
        imageHandler.clearAll();
    }

    @FXML
    private void drawOval() {
        type = 7;
        enableOthers();
        oval.setDisable(true);
    }

    public void jumpTo(int index) {
        if (index >= 0 && index < drawRecords.size()) {
            WritableImage image = drawRecords.get(index);
            drawRecords.add(image);
            drawRecordsStack.push(image);
            repaintCanvas(image);
        }
    }

    @FXML
    private void openSettingStage() {
        settingStage = new Stage();
        settingStage.setTitle("设置");
        settingStage.getIcons().add(new Image(ScreenCaptureToolApp
                .class.getResource("assets/icon/setting.png").toExternalForm()));
        FXMLLoader loader = new FXMLLoader(ScreenCaptureToolApp.class.getResource("setting.fxml"));
        Scene scene = null;
        try {
            scene = new Scene(loader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 设置控制器
        settingController = loader.getController();
        scene.getStylesheets().add(ScreenCaptureToolApp.class.getResource("assets/css/setting.css").toExternalForm());
        // 设置场景并显示窗口
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        settingStage.setScene(scene);
        settingStage.setTitle("设置");
        settingStage.setX(size.getWidth() / 3);
        settingStage.setY(size.getHeight() / 3);
        settingStage.setWidth(size.getWidth() * 0.4);
        settingStage.setHeight(size.getHeight() * 0.4);
        settingStage.show();
    }

    public void completelyExit() {
        if(AlertHelper.showConfirmAlert()){
            Platform.exit();
            System.exit(0);
        }
    }

    public void adjustScale() {
        capture.setFitWidth(originalImage.getWidth());
        capture.setFitHeight(originalImage.getHeight());
        canvas.setWidth(capture.getFitWidth());
        canvas.setHeight(capture.getFitHeight());

    }

    private String getState(){
        String s="画板 ：长宽"+canvas.getWidth()+"/"+canvas.getHeight()+"  缩放比"+canvas.getScaleX()+"/"+canvas.getScaleY()+"  ";
        String s1="图片 ：长宽"+originalImage.getWidth()+"/"+originalImage.getHeight()+"  ";
        String s2="imageView ：长宽"+capture.getFitWidth()+"/"+capture.getFitHeight()+"   ";
        return s+s1+s2;
    }
}
