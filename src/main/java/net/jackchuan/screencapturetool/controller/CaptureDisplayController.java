package net.jackchuan.screencapturetool.controller;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
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
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.jackchuan.screencapturetool.CaptureProperties;
import net.jackchuan.screencapturetool.ScreenCaptureToolApp;
import net.jackchuan.screencapturetool.entity.DrawRecords;
import net.jackchuan.screencapturetool.entity.ImageEditRecord;
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
import java.sql.Timestamp;
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
    private StackPane stackPane;

    public Label state;
    @FXML
    private ComboBox<String> processType;
    private ImageView capture;
    private WritableImage originalImage;
    @FXML
    private Slider strokeSlider;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private Canvas canvas, editArea, animator;
    @FXML
    private ToolBar tools;
    @FXML
    private Button drag, pencil, rubber, rect, filledRect, arrow, line, cos, oval, addImage, addText,filledOval;
    private Color forecolor = Color.RED;
    private int type = -2,oriType, stroke;
    private double initialX, initialY, startX, startY;
    private Stage parent, history, settingStage;
    private List<WritableImage> drawRecords;
    private Stack<WritableImage> drawRecordsStack;
    private List<String> editHistory;
    private Stack<DrawRecords> editStack;
    private Stack<DrawRecords> undoStack;
    private boolean isAltPressed, isShiftPressed, isCtrlPressed;
    private EditRecordController editController;
    private SettingController settingController;
    private ExternalImageHandler imageHandler;
    private DrawableImage selectedImage = null;
    private double dragStartX;
    private double dragStartY;
    private boolean isResizing = false;

    private ContextMenu popMenu;
    private MenuItem delete, add;

    @FXML
    public void initialize() {
        drawRecords = new ArrayList<>();
        drawRecordsStack = new Stack<>();
        editHistory = new ArrayList<>();
        editStack = new Stack<>();
        undoStack = new Stack<>();
        colorPicker.setValue(forecolor);
        // 初始化时设置 capture 和 canvas
        editArea = new Canvas();
        animator = new Canvas();
        imageHandler = new ExternalImageHandler(editArea,animator);
        stackPane.getChildren().addAll(editArea, animator);
        canvas.setVisible(true);
        editArea.setVisible(true);
        animator.setVisible(true);
        animator.setMouseTransparent(true);
//        canvas.toBack();
//        editArea.toFront();
        Platform.runLater(() -> {
            stroke= (int) strokeSlider.getValue();
            if (capture != null && capture.getImage() != null) {
                parent = (Stage) canvas.getScene().getWindow();
                canvas.setWidth(capture.getFitWidth());
                canvas.setHeight(capture.getFitHeight());
                editArea.setWidth(capture.getFitWidth());
                editArea.setHeight(capture.getFitHeight());
                animator.setWidth(capture.getFitWidth());
                animator.setHeight(capture.getFitHeight());
                drawImageToCanvas(capture.getImage());
                state.setText(getState());
                strokeSlider.valueProperty().addListener((newVal, oldVal, val) -> {
                    stroke = val.intValue();
                });
                popMenu = new ContextMenu();
                delete = new MenuItem("删除");
                add = new MenuItem("添加");
                popMenu.getItems().addAll(add, delete);
                delete.setOnAction(event -> {
                    if (selectedImage != null) {
                        imageHandler.removeExternalImage(selectedImage);
                        clearCanvas(animator);
                        editRecordImageRender(false,selectedImage);
                        repaintCanvas(editArea.getGraphicsContext2D(),true,0,true);
                        imageHandler.drawAllImages(editArea.getGraphicsContext2D());
                        selectedImage = null;
                    }
                });
                add.setOnAction(e -> addExternalImage(editArea.getWidth() * 0.4, editArea.getHeight() * 0.4));
                saveCurrentState("初始化图片(0)", true);
                editArea.getGraphicsContext2D()
                        .drawImage(ImageFormatHandler.getTransparentImage(editArea), 0, 0, editArea.getWidth(), editArea.getHeight());
                animator.getGraphicsContext2D()
                        .drawImage(ImageFormatHandler.getTransparentImage(animator), 0, 0, animator.getWidth(), animator.getHeight());
                tools.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode() == KeyCode.TAB) {
                        event.consume(); // 阻止默认的焦点切换行为
                        showEditHistory();
                    }
                });
                parent.getScene().setOnKeyPressed(e -> {
                    oriType=type;
                    if (e.getCode() == KeyCode.ESCAPE) {
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
                    type=oriType;
                    backToOriginMode();
                    if (e.getCode() == KeyCode.SHIFT) {
                        animator.setCursor(Cursor.DEFAULT);
                        isShiftPressed = false;
                    }
                    if (e.getCode() == KeyCode.ALT) {
                        animator.setCursor(Cursor.DEFAULT);
                        isAltPressed = false;
                    }
                    if (e.getCode() == KeyCode.CONTROL) {
                        isCtrlPressed = false;
                    }
                });
                // 鼠标拖动和缩放事件
                editArea.setOnScroll(event -> {
                    double zoomFactor = 1.1;
                    if (event.getDeltaY() < 0) {
                        zoomFactor = 1 / zoomFactor;
                    }
                    canvas.setScaleX(canvas.getScaleX() * zoomFactor);
                    canvas.setScaleY(canvas.getScaleY() * zoomFactor);
                    editArea.setScaleX(editArea.getScaleX() * zoomFactor);
                    editArea.setScaleY(editArea.getScaleY() * zoomFactor);
                    animator.setScaleX(animator.getScaleX() * zoomFactor);
                    animator.setScaleY(animator.getScaleY() * zoomFactor);
                    state.setText(getState());
                });

                editArea.setOnMouseClicked(e -> {
                    if (type == 8 && selectedImage==null && e.getClickCount() == 2) {
                        addExternalImage(e.getX(), e.getY());
                    } else if (type == 9 && e.getClickCount() == 2) {
//                        saveCurrentState("添加文字(" + drawRecords.size() + ")");
                    }
                });

                // 监听鼠标按下和拖动事件
                editArea.setOnMousePressed(event -> {
                    selectedImage=null;
                    if (event.getButton() == MouseButton.SECONDARY) { // 右键
                        popMenu.show(animator, event.getScreenX(), event.getScreenY());
                    }
                    if (event.getButton() != MouseButton.SECONDARY) {
                        popMenu.hide();
                    }
                    if (type == 0) {
                        initialX = event.getSceneX();
                        initialY = event.getSceneY();
                    } else if (type == 8) {
                        for (ExternalImageHandler.DrawableImage image : imageHandler.getImages()) {
                            if (image.isInside(event.getX(), event.getY())) {
                                selectedImage = image;
                                dragStartX = event.getX();
                                dragStartY = event.getY();
                                image.setOriX(image.getX());
                                image.setOriY(image.getY());
                                image.setOriWidth(image.getWidth());
                                image.setOriHeight(image.getHeight());
                                image.setShouldRender(false);
                                image.setRenderBorder(true);
                                isResizing = imageHandler.isNearBorder(image, event.getX(), event.getY());
                            }else{
                                selectedImage=null;
                                image.setRenderBorder(false);
                                image.setShouldRender(true);
                            }
                        }
                        if(selectedImage!=null){
                            editArea.getGraphicsContext2D().clearRect(selectedImage.getX(), selectedImage.getY(),
                                    selectedImage.getWidth(),selectedImage.getHeight());
                        }
                        clearCanvas(animator);
                        imageHandler.drawAllImages(editArea.getGraphicsContext2D());
                    } else {
                        startX = event.getX();
                        startY = event.getY();
                    }
                });
                editArea.setOnMouseExited(e->{
                    if(type==-1){
                        GraphicsContext editContext = animator.getGraphicsContext2D();
                        editContext.clearRect(0, 0, animator.getWidth(), animator.getHeight());
                    }
                });
                editArea.setOnMouseMoved(e -> {
                    if (type == -1 ) {
                        GraphicsContext editContext = animator.getGraphicsContext2D();
                        editContext.clearRect(0, 0, animator.getWidth(), animator.getHeight());
                        editContext.strokeRect(e.getX(), e.getY(), stroke * 10, stroke * 10);
                    }
                });

                editArea.setOnMouseDragged(event -> {
                    GraphicsContext editContext = animator.getGraphicsContext2D();
                    if (type == 0) {
                        double deltaX = event.getSceneX() - initialX;
                        double deltaY = event.getSceneY() - initialY;
                        // 更新 Canvas 的平移
                        canvas.setTranslateX(canvas.getTranslateX() + deltaX);
                        canvas.setTranslateY(canvas.getTranslateY() + deltaY);

                        editArea.setTranslateX(editArea.getTranslateX() + deltaX);
                        editArea.setTranslateY(editArea.getTranslateY() + deltaY);
                        animator.setTranslateX(animator.getTranslateX() + deltaX);
                        animator.setTranslateY(animator.getTranslateY() + deltaY);

                        initialX = event.getSceneX();
                        initialY = event.getSceneY();
                    } else if (type == 1) {
                        // 绘制普通线条
                        GraphicsContext editGc = editArea.getGraphicsContext2D();
                        editGc.setStroke(forecolor);
                        editGc.setLineWidth(strokeSlider.getValue());
                        editGc.strokeLine(startX, startY, event.getX(), event.getY());
                        startX = editArea.sceneToLocal(event.getSceneX(), event.getSceneY()).getX();
                        startY = editArea.sceneToLocal(event.getSceneX(), event.getSceneY()).getY();
                    } else {
                        editContext.setStroke(forecolor);
                        editContext.setLineWidth(strokeSlider.getValue());
                        editContext.clearRect(0, 0, animator.getWidth(), animator.getHeight());
                        double currentX = event.getX();
                        double currentY = event.getY();
                        switch (type) {
                            //橡皮擦
                            case -1 -> {
                                editContext.setStroke(Color.GREEN);
                                editContext.setLineWidth(2);
                                editContext.strokeRect(event.getX(), event.getY(), stroke * 10, stroke * 10);
                                GraphicsContext gc = editArea.getGraphicsContext2D();
                                gc.clearRect(event.getX(), event.getY(), stroke * 10, stroke * 10);
                            }
                            //绘制矩形框
                            case 2 -> {
                                DrawType.RECTANGLE.draw(editContext, startX, startY, currentX, currentY);
                            }
                            //绘制半透明填充的矩形框
                            case 3 -> {
                                DrawType.FILLED_RECTANGLE.draw(editContext, startX, startY, currentX, currentY, forecolor);
                            }
                            //绘制箭头
                            case 4 -> {
                                DrawType.ARROW.draw(editContext, startX, startY, currentX, currentY, forecolor);
                            }
                            //绘制直线
                            case 5 -> {
                                DrawType.LINE.draw(editContext, startX, startY, currentX, currentY);
                            }
                            //绘制波浪线
                            case 6 -> {
                                DrawType.WAVE.draw(editContext, startX, startY, currentX, currentY);
                            }
                            //绘制圆
                            case 7 -> {
                                DrawType.CIRCLE.draw(editContext, startX, startY, currentX, currentY);
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
                                        selectedImage.setX(selectedImage.getX() + offsetX);
                                        selectedImage.setY(selectedImage.getY() + offsetY);
                                        dragStartX = event.getX();
                                        dragStartY = event.getY();
                                    }
                                }
                                editContext.clearRect(0, 0, animator.getWidth(), animator.getHeight());
                                if(selectedImage!=null){
                                    imageHandler.updateImage(editArea.getGraphicsContext2D(),selectedImage);
                                }
                            }
                            case 9 -> {

                            }
                            case 10 ->{
                                //绘制半透明圆形
                                DrawType.FILLED_CIRCLE.draw(editContext,startX,startY,currentX,currentY,forecolor);
                            }

                        }
                    }
                });

                editArea.setOnMouseReleased(event -> {
                    String editType;
                    GraphicsContext clearGc = animator.getGraphicsContext2D();
                    clearGc.clearRect(0, 0, animator.getWidth(), animator.getHeight());

                    GraphicsContext editContext = editArea.getGraphicsContext2D();
                    editContext.setStroke(forecolor);
                    editContext.setLineWidth(strokeSlider.getValue());
                    double currentX = event.getX();
                    double currentY = event.getY();
                    switch (type) {
                        case 0 -> {
                            editType = "图片移动(" + drawRecords.size() + ")";
                            DrawRecords record = new DrawRecords();
                        }
                        case 1 -> {
                            editType = "普通绘画(" + drawRecords.size() + ")";
                            DrawRecords record = new DrawRecords();
                            record.setColor(forecolor);
                            record.setDrawType("common");
                            record.setImage(getSnapshot(editArea));
                            editStack.push(record);
                        }
                        case 2 -> {
                            editType = "绘制矩形框(" + drawRecords.size() + ")";
                            DrawType.RECTANGLE.draw(editContext, startX, startY, currentX, currentY);
                            editStack.push(new DrawRecords("rect",startX,startY,currentX,currentY,null,forecolor));
                        }
                        case 3 -> {
                            editType = "绘制透明填充的矩形框(" + drawRecords.size() + ")";
                            DrawType.FILLED_RECTANGLE.draw(editContext, startX, startY, currentX, currentY, forecolor);
                            editStack.push(new DrawRecords("fillRect",startX,startY,currentX,currentY,null,forecolor));
                        }
                        case 4 -> {
                            editType = "绘制箭头(" + drawRecords.size() + ")";
                            DrawType.ARROW.draw(editContext, startX, startY, currentX, currentY, forecolor);
                            editStack.push(new DrawRecords("arrow",startX,startY,currentX,currentY,null,forecolor));
                        }
                        case 5 -> {
                            editType = "绘制直线(" + drawRecords.size() + ")";
                            DrawType.LINE.draw(editContext, startX, startY, currentX, currentY);
                            editStack.push(new DrawRecords("line",startX,startY,currentX,currentY,null,forecolor));
                        }
                        case 6 -> {
                            editType = "绘制波浪线(" + drawRecords.size() + ")";
                            DrawType.WAVE.draw(editContext, startX, startY, currentX, currentY);
                            editStack.push(new DrawRecords("wave",startX,startY,currentX,currentY,null,forecolor));
                        }
                        case 7 -> {
                            editType = "绘制圆(" + drawRecords.size() + ")";
                            DrawType.CIRCLE.draw(editContext, startX, startY, currentX, currentY);
                            editStack.push(new DrawRecords("circle",startX,startY,currentX,currentY,null,forecolor));
                        }
                        case 8->{
                            //保存图片内容
                            editType="externalImg";
                            if(selectedImage!=null){
                                selectedImage.setShouldRender(true);
                                imageHandler.drawAllImages(editContext);
                                DrawRecords record=getSameImageRecord(selectedImage);
                                String type = isResizing ?"resize":"move";
                                if(record!=null){
                                    ImageEditRecord editRecord=new ImageEditRecord(type,selectedImage.getX(),
                                            selectedImage.getY(),selectedImage.getWidth(),selectedImage.getHeight());
                                    record.getExternalRecord().addRecord(editRecord);
                                }else{
                                    record=new DrawRecords("externalImg", selectedImage,type);
                                    editStack.push(record);
                                }
                                System.out.println(editStack.size());
                            }

                        }
                        case 9 ->{
                            //保存文字内容
                            editType="";
                        }
                        case 10 ->{
                            editType = "绘制透明填充的圆(" + drawRecords.size() + ")";
                            DrawType.FILLED_CIRCLE.draw(editContext, startX, startY, currentX, currentY,forecolor);
                            editStack.push(new DrawRecords("filledOval",startX,startY,currentX,currentY,null,forecolor));
                        }
                        default -> editType = "未知操作(" + drawRecords.size() + ")";
                    }
                    if (type != 8 && type != 9) {
                        isResizing = false;
                        saveCurrentState(editType, false);
                    }
                });
            }
            ControllerInstance.getInstance().setController(this);
        });
    }


    private void addExternalImage(double x, double y) {
        addImage();
        FileChooser chooser = new FileChooser();
        chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter(
                "图片", "png", "jpg"
        ));
        chooser.setInitialDirectory(new File(CaptureProperties.selectPath));
        File file = chooser.showOpenDialog(parent);
        if (file != null) {
            CaptureProperties.updateSelectPath(file.getParent());
            Image image = new Image("file:" + file.getAbsolutePath());
            //TODO : get scaled size
            IntegerPair size = ImageFormatHandler.getScaledSize(image, animator.getWidth(), animator.getHeight());
            Timestamp stamp=new Timestamp(System.currentTimeMillis());
            DrawableImage drawableImage = new DrawableImage(image, x, y, size.getW(), size.getH(), file.getName()+stamp.getTime());
            drawableImage.setRenderBorder(true);
            drawableImage.setOriY(x);
            drawableImage.setOriX(y);
            imageHandler.addExternalImage(drawableImage);
            DrawRecords record = new DrawRecords("externalImg", drawableImage,"init");
            editStack.push(record);
            selectedImage=drawableImage;
            imageHandler.drawAllImages(editArea.getGraphicsContext2D());
        }
    }

    @FXML
    private void addImage() {
        type = 8;
        editArea.setCursor(Cursor.HAND);
        enableOthers();
        addImage.setDisable(true);
    }

    @FXML
    private void addText() {
        type = 9;
        editArea.setCursor(Cursor.CROSSHAIR);
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
    public void saveCurrentState(String editType, boolean first) {
        //TODO 修改保存逻辑
        WritableImage snapshot;
        if (first) {
            snapshot = ImageFormatHandler.getTransparentImage(animator);
        } else {
            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setTransform(Transform.scale(CaptureProperties.scale, CaptureProperties.scale));  // 扩大图像
            snapshot = animator.snapshot(parameters, null);
        }
        drawRecords.add(snapshot);
        drawRecordsStack.push(snapshot);
        editHistory.add(editType);
        if (editController != null) {
            editController.addRecord(editType);
        }
    }

    private void repaintCanvas(GraphicsContext gc,boolean renderAll,int undoOrRedo,boolean shouldClear) {
        if(shouldClear){
            gc.clearRect(0, 0, editArea.getWidth(), editArea.getHeight());
        }
        for (int i = 0; i < editStack.size(); i++) {
            DrawRecords record = editStack.get(i);
            if(renderAll){
                record.draw(gc,animator.getGraphicsContext2D(),undoOrRedo);
            }else {
                if("externalImg".equals(record.getDrawType())){
                    record.updateExternalImage(gc,animator.getGraphicsContext2D(),undoOrRedo);
                }
            }

        }
    }


    public WritableImage getSnapshot(Canvas canvas) {
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setTransform(Transform.scale(CaptureProperties.scale, CaptureProperties.scale));  // 扩大图像
        return canvas.snapshot(parameters, null);
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
        fc.setInitialDirectory(new File(CaptureProperties.selectPath));
        File file = fc.showOpenDialog(null);
        CaptureProperties.updateSelectPath(file.getParent());
        capture.setImage(new Image(file.toURI().toString()));
        resizeStage();
        repaintUploadCanvas(capture.getImage());
        originalImage = new WritableImage(capture.getImage().getPixelReader(),
                (int) capture.getImage().getWidth(), (int) capture.getImage().getHeight());
    }

    @FXML
    public void saveCapture() {
        // 处理保存截图逻辑
        canvas.setScaleX(1);
        canvas.setScaleY(1);
        editArea.setScaleX(1);
        editArea.setScaleY(1);
        state.setText(getState());
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setTransform(Transform.scale(CaptureProperties.scale, CaptureProperties.scale));  // 扩大图像
        repaintCanvas(canvas.getGraphicsContext2D(),true,0,false);
        WritableImage writableImage = canvas.snapshot(parameters, null);
        // 转换为 BufferedImage
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);
        // 保存为文件
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image");
        fileChooser.setInitialFileName("capture.png");
        fileChooser.setInitialDirectory(new File(CaptureProperties.selectPath));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                boolean flag = ImageIO.write(bufferedImage, "png", file);
                AlertHelper.showAutoClosedPopup(flag?"保存成功！":"保存失败！！！",1,parent.getX()+100,parent.getY()+parent.getHeight()+20);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        drawImageToCanvas(originalImage);
    }

    @FXML
    public void copy() {
        canvas.setScaleX(1);
        canvas.setScaleY(1);
        editArea.setScaleX(1);
        editArea.setScaleY(1);
        state.setText(getState());
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setTransform(Transform.scale(CaptureProperties.scale, CaptureProperties.scale));  // 扩大图像
        repaintCanvas(canvas.getGraphicsContext2D(),true,0,false);
        WritableImage writableImage = canvas.snapshot(parameters,null);
        TransferableImage transferableImage = new TransferableImage(writableImage);
        // 复制到剪贴板
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferableImage, null);
        drawImageToCanvas(originalImage);
        AlertHelper.showAutoClosedPopup("复制成功！",1,parent.getX()+100,parent.getY()+parent.getHeight()+20);
    }

    @FXML
    public void pencilMode() {
        // 处理铅笔模式
        type = 1;
        Image cursorImage = new Image(ScreenCaptureToolApp.class.getResource("assets/icon/pencil.png").toExternalForm()); // 替换为你自己的图像路径
        ImageCursor customCursor = new ImageCursor(cursorImage, 0, 32); // (16,16) 是热点位置（图像的中心）
        editArea.setCursor(customCursor);
        enableOthers();
        pencil.setDisable(true);
        clearCanvas(animator);
    }

    @FXML
    public void rubberMode() {
        // 处理橡皮模式
        type = -1;
        Image cursorImage = new Image(ScreenCaptureToolApp.class.getResource("assets/icon/rubber.png").toExternalForm()); // 替换为你自己的图像路径
        ImageCursor customCursor = new ImageCursor(cursorImage, 0, 32); // (16,16) 是热点位置（图像的中心）
        editArea.setCursor(customCursor);
        enableOthers();
        rubber.setDisable(true);
        clearCanvas(animator);
        GraphicsContext editContext = animator.getGraphicsContext2D();
        editContext.setStroke(Color.GREEN);
        editContext.setLineWidth(2);
    }

    @FXML
    public void drawRect() {
        // 处理绘制矩形逻辑
        type = 2;
        editArea.setCursor(Cursor.CROSSHAIR);
        enableOthers();
        rect.setDisable(true);
        clearCanvas(animator);
    }

    @FXML
    public void drawFilledRect() {
        // 处理绘制填充矩形逻辑
        type = 3;
        editArea.setCursor(Cursor.CROSSHAIR);
        enableOthers();
        filledRect.setDisable(true);
        clearCanvas(animator);
    }

    @FXML
    public void drawArrow() {
        // 处理绘制箭头逻辑
        type = 4;
        editArea.setCursor(Cursor.CROSSHAIR);
        enableOthers();
        arrow.setDisable(true);
        clearCanvas(animator);
    }

    @FXML
    public void drawLine() {
        // 处理绘制直线逻辑
        type = 5;
        editArea.setCursor(Cursor.CROSSHAIR);
        enableOthers();
        line.setDisable(true);
        clearCanvas(animator);
    }

    @FXML
    private void drawCos() {
        this.type = 6;
        editArea.setCursor(Cursor.CROSSHAIR);
        enableOthers();
        cos.setDisable(true);
        clearCanvas(animator);
    }

    @FXML
    public void colorPicker() {
        forecolor = colorPicker.getValue();
        clearCanvas(animator);
    }

    @FXML
    public void undo() {
        //TODO
        if (!editStack.isEmpty()) {  // 至少保留一个初始图像
            DrawRecords undoRecord = editStack.pop();
            if("externalImg".equals(undoRecord.getDrawType())){
                selectedImage=undoRecord.getExternalImage();
                if(undoRecord.getExternalRecord().shouldPop()){
                    undoStack.push(undoRecord);
                }else {
                    editStack.push(undoRecord);
                }
            }else{
                undoStack.push(undoRecord);
            }
            clearCanvas(animator);
            repaintCanvas(editArea.getGraphicsContext2D(),true,1,true);
        }
    }

    // 重做操作
    @FXML
    public void redo() {
        if (!undoStack.isEmpty()) {
            DrawRecords undoRecord = undoStack.pop();
            if("externalImg".equals(undoRecord.getDrawType())){
                selectedImage=undoRecord.getExternalImage();
            }
            editStack.push(undoRecord);
            clearCanvas(animator);
            repaintCanvas(editArea.getGraphicsContext2D(),true,-1,true);
        }else{
            DrawRecords last = editStack.getLast();
            if("externalImg".equals(last.getDrawType())&&last.getExternalRecord().canRedo()){
                clearCanvas(animator);
                repaintCanvas(editArea.getGraphicsContext2D(),true,-1,true);
            }
        }
    }

    @FXML
    private void resetImage() {
        canvas.setScaleX(1);
        canvas.setScaleY(1);
        canvas.setTranslateX(0);
        canvas.setTranslateY(0);
        editArea.setScaleX(1);
        editArea.setScaleY(1);
        editArea.setTranslateX(0);
        editArea.setTranslateY(0);
        animator.setScaleX(1);
        animator.setScaleY(1);
        animator.setTranslateX(0);
        animator.setTranslateY(0);
        resizeStage();
        drawImageToCanvas(capture.getImage());
        clearCanvas(animator);
        clearCanvas(editArea);
        imageHandler.clearAll();
    }

    @FXML
    private void dragMode() {
        type = 0;
        editArea.setCursor(Cursor.HAND);
        clearCanvas(animator);
    }

    public void repaintUploadCanvas(Image image) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setImageSmoothing(false);
        // 在 canvas 上绘制图像
        gc.drawImage(image, 0, 0, canvas.getWidth(), canvas.getHeight());
        canvas.requestFocus();
        clearCanvas(animator);
    }

    private void resizeStage() {
        if (parent != null) {
            parent.setWidth(capture.getFitWidth());
            parent.setHeight(capture.getFitHeight() + 100);
            canvas.setWidth(capture.getFitWidth());
            canvas.setHeight(capture.getFitHeight());
            editArea.setWidth(capture.getFitWidth());
            editArea.setHeight(capture.getFitHeight());
            animator.setWidth(capture.getFitWidth());
            animator.setHeight(capture.getFitHeight());
        }
    }

    public void setCapture(ImageView capture) {
        this.capture = capture;
        originalImage = new WritableImage(capture.getImage().getPixelReader(),
                (int) capture.getImage().getWidth(), (int) capture.getImage().getHeight());
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
        addImage.setDisable(false);
        addText.setDisable(false);
        filledOval.setDisable(false);
    }

    @FXML
    private void processImage() throws IOException {
        if ("灰化".equals(processType.getValue())) {
            Mat src = ImageFormatHandler.toMat(canvas.snapshot(null, null));
            Mat gray = new Mat();
            Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
            WritableImage image = SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(gray), null);
            drawImageToCanvas(image);
            saveCurrentState("图像灰化处理", false);
        } else if ("锐化".equals(processType.getValue())) {
            Mat src = ImageFormatHandler.toMat(canvas.snapshot(null, null));
            Mat sharpenKernel = new Mat(3, 3, CvType.CV_32F);
            sharpenKernel.put(0, 0, 0, -1, 0, -1, 5, -1, 0, -1, 0);  // 常见的锐化卷积核
            // 通过卷积操作锐化图像
            Mat sharpenedImage = new Mat();
            Imgproc.filter2D(src, sharpenedImage, src.depth(), sharpenKernel);
            WritableImage image = SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(sharpenedImage), null);
            drawImageToCanvas(image);
            saveCurrentState("图像锐化处理", false);
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
            saveCurrentState("图像分割处理", false);
        } else if ("边缘提取".equals(processType.getValue())) {
            Mat src = ImageFormatHandler.toMat(canvas.snapshot(null, null));
            Mat edges = new Mat();
            Imgproc.Canny(src, edges, 100, 200);
            WritableImage image = SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(edges), null);
            drawImageToCanvas(image);
            saveCurrentState("图像边缘处理", false);
        } else if ("均值平滑".equals(processType.getValue())) {
            Mat src = ImageFormatHandler.toMat(canvas.snapshot(null, null));
            Mat meanBlurred = new Mat();
            Size kernelSize = new Size(5, 5);  // 5x5的卷积核
            Imgproc.blur(src, meanBlurred, kernelSize);
            WritableImage image = SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(meanBlurred), null);
            drawImageToCanvas(image);
            saveCurrentState("图像均值平滑处理", false);
        } else if ("高斯平滑".equals(processType.getValue())) {
            Mat src = ImageFormatHandler.toMat(canvas.snapshot(null, null));
            Mat meanBlurred = new Mat();
            Size kernelSize = new Size(5, 5);  // 5x5的卷积核
            Imgproc.GaussianBlur(src, meanBlurred, kernelSize, 0);
            WritableImage image = SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(meanBlurred), null);
            drawImageToCanvas(image);
            saveCurrentState("图像高斯平滑处理", false);
        } else if ("人脸识别".equals(processType.getValue())) {
            Mat src = ImageFormatHandler.toMat(canvas.snapshot(null, null));
            Mat meanBlurred = new Mat();
            Size kernelSize = new Size(5, 5);  // 5x5的卷积核
            Imgproc.blur(src, meanBlurred, kernelSize);
            WritableImage image = SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(meanBlurred), null);
            drawImageToCanvas(image);
            saveCurrentState("人脸识别", false);
        }
    }

    public void clearAllRecord() {
        clearCanvas(editArea);
        capture.setImage(originalImage);
        resizeStage();
        WritableImage image = new WritableImage(originalImage.getPixelReader(),
                (int) originalImage.getWidth(), (int) originalImage.getHeight());
        drawRecords.clear();
        drawRecordsStack.clear();
        drawRecords.add(image);
        drawRecordsStack.add(image);
        imageHandler.clearAll();
        editStack.clear();
        if (editController != null) {
            editController.clearAllRecords();
        }
        clearCanvas(animator);
        clearCanvas(editArea);
    }

    @FXML
    private void drawOval() {
        type = 7;
        enableOthers();
        editArea.setCursor(Cursor.CROSSHAIR);
        oval.setDisable(true);
    }
    @FXML
    private void drawFilledOval(){
        type = 10;
        enableOthers();
        editArea.setCursor(Cursor.CROSSHAIR);
        filledOval.setDisable(true);
    }

    public void jumpTo(int index) {
        //TODO 修改逻辑
        if (index >= 0 && index < drawRecords.size()) {
            System.out.println("jump to " + index);
            WritableImage image = drawRecords.get(index);
            drawRecords.add(image);
            drawRecordsStack.push(image);
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
        if (AlertHelper.showConfirmAlert()) {
            Platform.exit();
            System.exit(0);
        }
    }

    public void adjustScale() {
//        capture.setFitWidth(originalImage.getWidth());
//        capture.setFitHeight(originalImage.getHeight());
//        canvas.setWidth(capture.getFitWidth());
//        canvas.setHeight(capture.getFitHeight());
//        editArea.setWidth(capture.getFitWidth());
//        editArea.setHeight(capture.getFitHeight());
//        animator.setWidth(capture.getFitWidth());
//        animator.setHeight(capture.getFitHeight());
        System.out.println("edit Stack size = "+editStack.size());
        System.out.println("------  全部编辑记录 --------");
        for(DrawRecords r:editStack){
            System.out.println(r.toString());
        }
        System.out.println("-------  undo 记录 ---------");
        for(DrawRecords r:undoStack){
            System.out.println(r.toString());
        }
        System.out.println("-------------------");
    }

    private String getState() {
        String s = "画板 ：长宽" + canvas.getWidth() + "/" + canvas.getHeight() + "  缩放比" + canvas.getScaleX() + "/" + canvas.getScaleY() + "  ";
        String s1 = "图片 ：长宽" + originalImage.getWidth() + "/" + originalImage.getHeight() + "  ";
        String s2 = "imageView ：长宽" + capture.getFitWidth() + "/" + capture.getFitHeight() + "   ";
        return s + s1 + s2;
    }

    public void test1() throws IOException {
        for(DrawRecords record:editStack){
            if("externalImg".equals(record.getDrawType())){
                record.getExternalRecord().print();
            }
        }
    }

    private void clearCanvas(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }
    private void backToOriginMode() {
        switch (type){
            case -1->rubberMode();
            case 1->pencilMode();
            case 2->drawRect();
            case 3->drawFilledRect();
            case 4->drawArrow();
            case 5->drawLine();
            case 6->drawCos();
            case 7->drawOval();
            case 8->addImage();
            case 9->addText();
            case 10->drawFilledOval();
        }
    }
    private void editRecordImageRender(boolean render,DrawableImage image) {
        for (DrawRecords record:editStack){
            if("externalImg".equals(record.getDrawType())&&image.equals(record.getExternalImage())){
                record.setShouldRender(render);
            }
        }
    }

    @FXML
    private void createEmptyImage() {
        //TODO create new empty image, open a chooser dialog

//        File file = new File("E:/t1.png");
//        try {
//            ImageIO.write(SwingFXUtils.fromFXImage(image2, null), "png", file);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    private DrawRecords getSameImageRecord(DrawableImage img) {
        int n=0;
        for (int i = 0; i < editStack.size(); i++) {
            DrawRecords record = editStack.get(i);
            if("externalImg".equals(record.getDrawType())&&record.getExternalImage().equals(img)){
                return record;
            }
        }
        return null;
    }
}
