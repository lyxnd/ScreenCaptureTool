package net.jackchuan.screencapturetool.controller;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
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
import javafx.scene.input.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.transform.Transform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import net.jackchuan.screencapturetool.CaptureProperties;
import net.jackchuan.screencapturetool.ScreenCaptureToolApp;
import net.jackchuan.screencapturetool.entity.DrawRecords;

import net.jackchuan.screencapturetool.external.ExternalImageHandler;
import net.jackchuan.screencapturetool.external.ExternalImageHandler.DrawableImage;
import net.jackchuan.screencapturetool.external.ExternalTextHandler;
import net.jackchuan.screencapturetool.external.ExternalTextHandler.DrawableText;
import net.jackchuan.screencapturetool.external.ToolBarManager;
import net.jackchuan.screencapturetool.external.pane.TextFieldPane;
import net.jackchuan.screencapturetool.external.picker.*;
import net.jackchuan.screencapturetool.external.stage.TextRecognitionStage;
import net.jackchuan.screencapturetool.util.*;
import net.jackchuan.screencapturetool.util.impl.CornerType;
import net.jackchuan.screencapturetool.util.impl.DrawType;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
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
import java.util.concurrent.CompletableFuture;

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
    private Button upload, createEmpty, save, copy, undo, redo, reset, clear, drag, pencil, rubber, addImage, ocr, addText, process, setting, exit;
    @FXML
    private CustomPicker arrow, rect, line, oval;
    private Color forecolor = Color.RED;
    private int type = -2, oriType, stroke;
    private double initialX, initialY, startX, startY;
    private Stage parent, history, settingStage;
    private List<String> editHistory;
    private Stack<DrawRecords> allRecordStack;
    private Stack<DrawRecords> repaintStack;
    private Stack<DrawRecords> undoStack;
    private boolean isAltPressed, isShiftPressed, isCtrlPressed;
    private EditRecordController editController;
    private ExternalImageHandler imageHandler;
    private ExternalTextHandler textHandler;
    private DrawableImage selectedImage = null;
    private DrawableText selectedText = null;
    private double dragStartX;
    private double dragStartY;
    private boolean isResizing = false, moving;
    private ContextMenu popMenu;
    private MenuItem delete, addImg, addStr;
    private CornerType cornerType;
    private TextFieldPane textPane;
    private ToolBarManager toolbarManager;
    private TextRecognitionStage textStage;
    private ITesseract tess;
    private Font font;

    @FXML
    public void initialize() throws IOException {
        editHistory = new ArrayList<>();
        allRecordStack = new Stack<>();
        repaintStack = new Stack<>();
        undoStack = new Stack<>();
        toolbarManager = new ToolBarManager(tools);
        colorPicker.setValue(forecolor);
        // 初始化时设置 capture 和 canvas
        editArea = new Canvas();
        animator = new Canvas();
        canvas.setId("image");
        editArea.setId("editArea");
        animator.setId("animator");
        imageHandler = new ExternalImageHandler(editArea, animator);
        textHandler = new ExternalTextHandler(editArea,animator);
        stackPane.getChildren().addAll(editArea, animator);
        canvas.setVisible(true);
        editArea.setVisible(true);
        animator.setVisible(true);
        animator.setMouseTransparent(true);
        initPickers();
        Platform.runLater(() -> {
            stroke = (int) strokeSlider.getValue();
            if (capture != null && capture.getImage() != null) {
                parent = (Stage) canvas.getScene().getWindow();
                canvas.setWidth(capture.getFitWidth());
                canvas.setHeight(capture.getFitHeight());
                editArea.setWidth(capture.getFitWidth());
                editArea.setHeight(capture.getFitHeight());
                animator.setWidth(capture.getFitWidth());
                animator.setHeight(capture.getFitHeight());
                drawImageToCanvas(capture.getImage());
                strokeSlider.valueProperty().addListener((newVal, oldVal, val) -> {
                    stroke = val.intValue();
                });
                popMenu = new ContextMenu();
                delete = new MenuItem("删除");
                addImg = new MenuItem("添加图片");
                addStr = new MenuItem("添加文字");
                popMenu.getItems().addAll(addImg, addStr, delete);
                delete.setOnAction(this::deleteExternalImage);
                addStr.setOnAction(e -> {
                    addText();
                    showTextInputArea(editArea.getWidth() * 0.4, editArea.getHeight() * 0.4);
                });
                addImg.setOnAction(e -> addExternalImage(editArea.getWidth() * 0.4, editArea.getHeight() * 0.4));
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
                parent.getScene().setOnKeyPressed(this::onKeyPressed);

                parent.getScene().setOnKeyReleased(this::onKeyReleased);
                // 鼠标拖动和缩放事件
                editArea.setOnScroll(this::adjustCanvasScale);
                editArea.setOnMouseClicked(this::changeToExternalMode);
                editArea.setOnMousePressed(this::initDrawType);
                editArea.setOnMouseExited(this::clearPromotions);
                editArea.setOnMouseMoved(this::drawPromotions);
                editArea.setOnMouseDragged(this::drawDynamaticProgress);
                editArea.setOnMouseReleased(this::saveEditRecord);
            }
            ControllerInstance.getInstance().setController(this);
        });
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    @FXML
    private void textDetection() throws IOException {
        if(CaptureProperties.checkOCR()){
            type = 27;
            setCursorShape(Cursor.CROSSHAIR);
        }
    }

    private void changeToExternalMode(MouseEvent e) {
        if (type == 8 && selectedImage == null && e.getClickCount() == 2) {
            addExternalImage(e.getX(), e.getY());
        } else if (type == 9 && e.getClickCount() == 2) {
            showTextInputArea(e.getX(),e.getY());
        }
    }

    private void initDrawType(MouseEvent event) {
        selectedImage = null;
        selectedText=null;
        if (event.getButton() == MouseButton.SECONDARY) { // 右键
            popMenu.show(animator, event.getScreenX(), event.getScreenY());
        }
        if (event.getButton() != MouseButton.SECONDARY) {
            popMenu.hide();
        }
        if (type == 0) {
            initialX = event.getSceneX();
            initialY = event.getSceneY();
        } else if (type == 8&&event.getButton()==MouseButton.PRIMARY) {
            for (ExternalImageHandler.DrawableImage image : imageHandler.getImages()) {
                System.out.println("isUndo = "+image.isUndo());
                if (!image.isUndo()&&image.isInside(event.getX(), event.getY())) {
                    selectedImage = image;
                    dragStartX = event.getX();
                    dragStartY = event.getY();
                    startX = event.getX();
                    startY = event.getY();
                    image.setOriX(image.getX());
                    image.setOriY(image.getY());
                    image.setShouldRender(false);
                    image.setRenderBorder(true);
                    cornerType = imageHandler.isNearBorder(image, event.getX(), event.getY());
                    isResizing = cornerType != CornerType.EMPTY;
                } else {
                    image.setRenderBorder(false);
                    image.setShouldRender(true);
                }
            }
            clearCanvas(animator);
            if (selectedImage != null) {
                imageHandler.updateImage(animator.getGraphicsContext2D(), selectedImage);
                repaintCanvas(editArea.getGraphicsContext2D(), false,false,true);
            }
        } else if (type == 9&&event.getButton()==MouseButton.PRIMARY) {
            if(textPane!=null&&!textPane.isInField(event)){
                textPane.removeTextField();
            }
            for(DrawableText text: textHandler.getTexts()){
                if (!text.isUndo()&&text.isInside(event.getX(), event.getY())) {
                    selectedText = text;
                    dragStartX = event.getX();
                    dragStartY = event.getY();
                    startX = event.getX();
                    startY = event.getY();
                    text.setOriX(text.getX());
                    text.setOriY(text.getY());
                    text.setShouldRender(false);
                    text.setRenderBorder(true);
                    cornerType = textHandler.isNearBorder(text, event.getX(), event.getY());
                    isResizing = cornerType != CornerType.EMPTY;
                } else {
                    text.setRenderBorder(false);
                    text.setShouldRender(true);
                }
            }
            clearCanvas(animator);
            if (selectedText != null) {
                System.out.println("selectedText :"+selectedText.toString());
                textHandler.updateText(animator.getGraphicsContext2D(), selectedText);
                repaintCanvas(editArea.getGraphicsContext2D(), false,false, true);
            }
        } else {
            startX = event.getX();
            startY = event.getY();
        }
    }

    private void showTextInputArea(double x, double y) {
        if (textPane == null) {
            textPane = new TextFieldPane(this,canvas.getWidth(), canvas.getHeight());
            textPane.setTextFieldPos(x,y);
            stackPane.getChildren().add(textPane);
            textPane.setVisible(true);
        }else {
            textPane.addTextField(selectedText!=null?selectedText.getValue():"",
                    selectedText!=null?selectedText.getFont():new Font("Arial",16));
        }
    }

    private void clearPromotions(MouseEvent event) {
        if (type == -1) {
            GraphicsContext editContext = animator.getGraphicsContext2D();
            editContext.clearRect(0, 0, animator.getWidth(), animator.getHeight());
        }
    }

    private void drawPromotions(MouseEvent e) {
        String pos = "Mouse pos : (" + (int) e.getX() + "," + (int) e.getY() + ")  ";
        pos += "Size : (" + (int)canvas.getWidth() + "," + (int)canvas.getHeight() + ")  ";
        setState(pos);
        if (type == -1) {
            GraphicsContext editContext = animator.getGraphicsContext2D();
            editContext.clearRect(0, 0, animator.getWidth(), animator.getHeight());
            editContext.strokeRect(e.getX(), e.getY(), stroke * 10, stroke * 10);
        } else if (type == 8) {
            for (ExternalImageHandler.DrawableImage image : imageHandler.getImages()) {
                if(!image.equals(selectedImage)){
                    continue;
                }
                switch (imageHandler.isNearBorder(image, e.getX(), e.getY())) {
                    case EAST -> editArea.setCursor(Cursor.E_RESIZE);
                    case WEST -> editArea.setCursor(Cursor.W_RESIZE);
                    case SOUTH -> editArea.setCursor(Cursor.S_RESIZE);
                    case NORTH -> editArea.setCursor(Cursor.N_RESIZE);
                    case SOUTHEAST -> editArea.setCursor(Cursor.SE_RESIZE);
                    case SOUTHWEST -> editArea.setCursor(Cursor.SW_RESIZE);
                    case NORTHEAST -> editArea.setCursor(Cursor.NE_RESIZE);
                    case NORTHWEST -> editArea.setCursor(Cursor.NW_RESIZE);
                    case EMPTY -> editArea.setCursor(Cursor.HAND);
                }
            }
        }
        else if(type==9){
            for (DrawableText text : textHandler.getTexts()) {
                if(!text.equals(selectedText)){
                    continue;
                }
                switch (textHandler.isNearBorder(text, e.getX(), e.getY())) {
                    case EAST -> editArea.setCursor(Cursor.E_RESIZE);
                    case WEST -> editArea.setCursor(Cursor.W_RESIZE);
                    case SOUTH -> editArea.setCursor(Cursor.S_RESIZE);
                    case NORTH -> editArea.setCursor(Cursor.N_RESIZE);
                    case SOUTHEAST -> editArea.setCursor(Cursor.SE_RESIZE);
                    case SOUTHWEST -> editArea.setCursor(Cursor.SW_RESIZE);
                    case NORTHEAST -> editArea.setCursor(Cursor.NE_RESIZE);
                    case NORTHWEST -> editArea.setCursor(Cursor.NW_RESIZE);
                    case EMPTY -> editArea.setCursor(Cursor.HAND);
                }
            }
        }
    }

    private void drawDynamaticProgress(MouseEvent event) {
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
        }
        else if (type == 1) {
            // 绘制普通线条
            GraphicsContext editGc = editArea.getGraphicsContext2D();
            editGc.setStroke(forecolor);
            editGc.setLineWidth(strokeSlider.getValue());
            editGc.strokeLine(startX, startY, event.getX(), event.getY());
            startX = editArea.sceneToLocal(event.getSceneX(), event.getSceneY()).getX();
            startY = editArea.sceneToLocal(event.getSceneX(), event.getSceneY()).getY();
        }
        else {
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
                    DrawType.ARROW.draw(editContext, startX, startY, currentX, currentY);
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
                            RectanglePair pair = RectanglePair.calculateResizedImage(selectedImage, event.getX(), event.getY(), cornerType);
                            double newWidth = pair.getW();
                            double newHeight = pair.getH();
                            selectedImage.setWidth(newWidth);
                            selectedImage.setHeight(newHeight);
                            selectedImage.setX(pair.getX());
                            selectedImage.setY(pair.getY());
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
                    if (selectedImage != null) {
                        imageHandler.updateImage(editArea.getGraphicsContext2D(), selectedImage);
                    }
                }
                case 9 -> {
                    if (selectedText != null) {
//                        if (isResizing) {
//                        adjustScale();
//                        } else {
                            double offsetX = event.getX() - dragStartX;
                            double offsetY = event.getY() - dragStartY;
                            selectedText.setX(selectedText.getX() + offsetX);
                            selectedText.setY(selectedText.getY() + offsetY);
                            dragStartX = event.getX();
                            dragStartY = event.getY();
//                        }
                    }
                    editContext.clearRect(0, 0, animator.getWidth(), animator.getHeight());
                    if (selectedText != null) {
                        textHandler.updateText(editArea.getGraphicsContext2D(), selectedText);
                    }
                }
                case 10 -> {
                    //绘制半透明圆形
                    DrawType.FILLED_CIRCLE.draw(editContext, startX, startY, currentX, currentY, forecolor);
                }
                case 11 -> {
                    //绘制虚线
                    DrawType.LINE_DASHED.draw(editContext, startX, startY, currentX, currentY);
                }
                case 12 -> {
                    //绘制双画线
                    DrawType.LINE_DOUBLE.draw(editContext, startX, startY, currentX, currentY);
                }
                case 13 -> {
                    //绘制圆角矩形
                    DrawType.RECTANGLE_ROUND.draw(editContext, startX, startY, currentX, currentY);
                }
                case 14 -> {
                    //绘制半透明圆角矩形
                    DrawType.RECTANGLE_ROUND_FILLED.draw(editContext, startX, startY, currentX, currentY, forecolor);
                }
                case 15 -> {
                    //绘制虚线箭头
                    DrawType.ARROW_DASHED.draw(editContext, startX, startY, currentX, currentY);
                }
                case 16 -> {
                    //绘制填充的箭头
                    DrawType.ARROW_FILLED.draw(editContext, startX, startY, currentX, currentY, forecolor);
                }
                case 17 -> {
                    //绘制无填充的箭头
                    DrawType.ARROW_EMPTY.draw(editContext, startX, startY, currentX, currentY);
                }
                case 18 -> {
                    //绘制双向箭头
                    DrawType.ARROW_TWO_DIR.draw(editContext, startX, startY, currentX, currentY);
                }
                case 19 -> {
                    //绘制双线箭头
                    DrawType.ARROW_DOUBLE.draw(editContext, startX, startY, currentX, currentY);
                }
                case 20 -> {
                    //绘制虚线圆
                    DrawType.OVAL_DASHED.draw(editContext, startX, startY, currentX, currentY);
                }
                case 21 -> {
                    //绘制双虚线的箭头
                    DrawType.ARROW_DOUBLE_DASHED.draw(editContext, startX, startY, currentX, currentY);
                }
                case 22 -> {
                    //绘制的箭头
                    DrawType.ARROW_DOUBLE_TWO_DIR.draw(editContext, startX, startY, currentX, currentY);
                }
                case 23 -> {
                    //绘制圆角虚线矩形
                    DrawType.RECTANGLE_ROUND_DASHED.draw(editContext, startX, startY, currentX, currentY);
                }
                case 24 -> {
                    //绘制圆角虚线半透明矩形
                    DrawType.RECTANGLE_ROUND_DASHED_FILLED.draw(editContext, startX, startY, currentX, currentY, forecolor);
                }
                case 25 -> {
                    //绘制虚线矩形
                    DrawType.RECTANGLE_DASHED.draw(editContext, startX, startY, currentX, currentY);
                }
                case 26 -> {
                    //绘制虚线半透明矩形
                    DrawType.RECTANGLE_DASHED_FILLED.draw(editContext, startX, startY, currentX, currentY, forecolor);
                }
                case 27 -> {
                    //OCR
                    DrawType.RECTANGLE.draw(editContext, startX, startY, currentX, currentY);
                }
            }
        }
    }

    private void saveEditRecord(MouseEvent event) {
        GraphicsContext clearGc = animator.getGraphicsContext2D();
        clearGc.clearRect(0, 0, animator.getWidth(), animator.getHeight());
        GraphicsContext editContext = editArea.getGraphicsContext2D();
        if (event.getButton() != MouseButton.PRIMARY) {
            return;
        }
        if(Math.abs(event.getX()-startX)<=25&&Math.abs(event.getY()-startY)<=25){
            if(selectedImage!=null){
                selectedImage.setShouldRender(true);
                imageHandler.drawAllImages(editContext);
            }
            if(selectedText!=null){
                selectedText.setShouldRender(true);
                textHandler.drawAllTexts(editContext);
            }
            return;
        }
        String editType = "";
        editContext.setStroke(forecolor);
        editContext.setLineWidth(strokeSlider.getValue());
        double currentX = event.getX();
        double currentY = event.getY();
        long tick = new Timestamp(System.currentTimeMillis()).getTime();
        switch (type) {
            case 0 -> {
                editType = "图片移动(" + allRecordStack.size() + ")";
                DrawRecords record = new DrawRecords();
            }
            case 1 -> {
                editType = "普通绘画(" + allRecordStack.size() + ")";
                DrawRecords record = new DrawRecords();
                record.setColor(forecolor);
                record.setDrawType("common");
                record.setImage(getSnapshot(editArea));
                allRecordStack.push(record);
            }
            case 2 -> {
                editType = "绘制矩形框(" + allRecordStack.size() + ")";
                DrawType.RECTANGLE.draw(editContext, startX, startY, currentX, currentY);
                allRecordStack.push(new DrawRecords("rect", startX, startY, currentX, currentY, null, forecolor, tick));
            }
            case 3 -> {
                editType = "绘制透明填充的矩形框(" + allRecordStack.size() + ")";
                DrawType.FILLED_RECTANGLE.draw(editContext, startX, startY, currentX, currentY, forecolor);
                allRecordStack.push(new DrawRecords("fillRect", startX, startY, currentX, currentY, null, forecolor, tick));
            }
            case 4 -> {
                editType = "绘制箭头(" + allRecordStack.size() + ")";
                DrawType.ARROW.draw(editContext, startX, startY, currentX, currentY);
                allRecordStack.push(new DrawRecords("arrow", startX, startY, currentX, currentY, null, forecolor, tick));
            }
            case 5 -> {
                editType = "绘制直线(" + allRecordStack.size() + ")";
                DrawType.LINE.draw(editContext, startX, startY, currentX, currentY);
                allRecordStack.push(new DrawRecords("line", startX, startY, currentX, currentY, null, forecolor, tick));
            }
            case 6 -> {
                editType = "绘制波浪线(" + allRecordStack.size() + ")";
                DrawType.WAVE.draw(editContext, startX, startY, currentX, currentY);
                allRecordStack.push(new DrawRecords("wave", startX, startY, currentX, currentY, null, forecolor, tick));
            }
            case 7 -> {
                editType = "绘制圆(" + allRecordStack.size() + ")";
                DrawType.CIRCLE.draw(editContext, startX, startY, currentX, currentY);
                allRecordStack.push(new DrawRecords("circle", startX, startY, currentX, currentY, null, forecolor, tick));
            }
            case 8 -> {
                //保存图片内容
                if (selectedImage != null) {
                    String type = isResizing ? "resize" : "move";
                    selectedImage.setShouldRender(true);
//                    imageHandler.drawAllImages(editContext);
                    if (!isResizing && selectedImage.canKeep()) {
                        break;
                    }
                    DrawRecords record = new DrawRecords("externalImg", selectedImage, type, tick);
                    allRecordStack.push(record);
                    editType = "图片-" + type + "(" + allRecordStack.size() + ")";
                    editHistory.add(editType);
                    repaintCanvas(editContext,true,true,true);
                }
            }
            case 9 -> {
                //保存文字内容
                if (selectedText != null) {
                    String type = isResizing ? "resize" : "move";
                    selectedText.setShouldRender(true);
                    textHandler.drawAllTexts(editContext);
                    if (!isResizing && selectedText.canKeep()) {
                        break;
                    }
                    DrawRecords record = new DrawRecords("externalText", selectedText, type, tick);
                    allRecordStack.push(record);
                    editType = "文字-" + type + "(" + allRecordStack.size() + ")";
                    editHistory.add(editType);
                }
            }
            case 10 -> {
                editType = "绘制透明填充的圆(" + allRecordStack.size() + ")";
                DrawType.FILLED_CIRCLE.draw(editContext, startX, startY, currentX, currentY, forecolor);
                allRecordStack.push(new DrawRecords("filledOval", startX, startY, currentX, currentY, null, forecolor, tick));
            }
            case 11 -> {
                //绘制虚线
                editType = "绘制虚线(" + allRecordStack.size() + ")";
                DrawType.LINE_DASHED.draw(editContext, startX, startY, currentX, currentY);
                allRecordStack.push(new DrawRecords("lineDashed", startX, startY, currentX, currentY, null, forecolor, tick));
            }
            case 12 -> {
                //绘制双画线
                editType = "绘制双画线(" + allRecordStack.size() + ")";
                DrawType.LINE_DOUBLE.draw(editContext, startX, startY, currentX, currentY);
                allRecordStack.push(new DrawRecords("lineDouble", startX, startY, currentX, currentY, null, forecolor, tick));
            }
            case 13 -> {
                //绘制圆角矩形
                editType = "绘制圆角矩形(" + allRecordStack.size() + ")";
                DrawType.RECTANGLE_ROUND.draw(editContext, startX, startY, currentX, currentY);
                allRecordStack.push(new DrawRecords("rectRound", startX, startY, currentX, currentY, null, forecolor, tick));
            }
            case 14 -> {
                //绘制半透明圆角矩形
                editType = "绘制半透明圆角矩形(" + allRecordStack.size() + ")";
                DrawType.RECTANGLE_ROUND_FILLED.draw(editContext, startX, startY, currentX, currentY, forecolor);
                allRecordStack.push(new DrawRecords("rectRoundFilled", startX, startY, currentX, currentY, null, forecolor, tick));
            }
            case 15 -> {
                //绘制虚线箭头
                editType = "绘制虚线箭头(" + allRecordStack.size() + ")";
                DrawType.ARROW_DASHED.draw(editContext, startX, startY, currentX, currentY);
                allRecordStack.push(new DrawRecords("arrowDashed", startX, startY, currentX, currentY, null, forecolor, tick));
            }
            case 16 -> {
                //绘制填充的箭头
                editType = "绘制填充的箭头(" + allRecordStack.size() + ")";
                DrawType.ARROW_FILLED.draw(editContext, startX, startY, currentX, currentY, forecolor);
                allRecordStack.push(new DrawRecords("arrowFilled", startX, startY, currentX, currentY, null, forecolor, tick));
            }
            case 17 -> {
                //绘制无填充的箭头
                editType = "绘制无填充的箭头(" + allRecordStack.size() + ")";
                DrawType.ARROW_EMPTY.draw(editContext, startX, startY, currentX, currentY);
                allRecordStack.push(new DrawRecords("arrowEmpty", startX, startY, currentX, currentY, null, forecolor, tick));
            }
            case 18 -> {
                //绘制双向箭头
                editType = "绘制双向箭头(" + allRecordStack.size() + ")";
                DrawType.ARROW_TWO_DIR.draw(editContext, startX, startY, currentX, currentY);
                allRecordStack.push(new DrawRecords("arrowTwoDir", startX, startY, currentX, currentY, null, forecolor, tick));
            }
            case 19 -> {
                //绘制双线箭头
                editType = "绘制双线箭头(" + allRecordStack.size() + ")";
                DrawType.ARROW_DOUBLE.draw(editContext, startX, startY, currentX, currentY);
                allRecordStack.push(new DrawRecords("arrowDouble", startX, startY, currentX, currentY, null, forecolor, tick));
            }
            case 20 -> {
                //绘制虚线圆
                editType = "绘制虚线圆(" + allRecordStack.size() + ")";
                DrawType.OVAL_DASHED.draw(editContext, startX, startY, currentX, currentY);
                allRecordStack.push(new DrawRecords("ovalDashed", startX, startY, currentX, currentY, null, forecolor, tick));
            }
            case 21 -> {
                //绘制双虚线的箭头
                editType = "绘制双虚线的箭头(" + allRecordStack.size() + ")";
                DrawType.ARROW_DOUBLE_DASHED.draw(editContext, startX, startY, currentX, currentY);
                allRecordStack.push(new DrawRecords("arrowDoubleDashed", startX, startY, currentX, currentY, null, forecolor, tick));
            }
            case 22 -> {
                //绘制双线双向的箭头
                editType = "绘制双线双向的箭头(" + allRecordStack.size() + ")";
                DrawType.ARROW_DOUBLE_TWO_DIR.draw(editContext, startX, startY, currentX, currentY);
                allRecordStack.push(new DrawRecords("arrowDoubleTwo", startX, startY, currentX, currentY, null, forecolor, tick));
            }
            case 23 -> {
                //绘制圆角虚线矩形
                editType = "绘制圆角虚线矩形(" + allRecordStack.size() + ")";
                DrawType.RECTANGLE_ROUND_DASHED.draw(editContext, startX, startY, currentX, currentY);
                allRecordStack.push(new DrawRecords("rectRoundDashed", startX, startY, currentX, currentY, null, forecolor, tick));
            }
            case 24 -> {
                //绘制圆角虚线半透明矩形
                editType = "绘制圆角虚线半透明矩形(" + allRecordStack.size() + ")";
                DrawType.RECTANGLE_ROUND_DASHED_FILLED.draw(editContext, startX, startY, currentX, currentY, forecolor);
                allRecordStack.push(new DrawRecords("rectRoundDashedFilled", startX, startY, currentX, currentY, null, forecolor, tick));
            }
            case 25 -> {
                //绘制虚线矩形
                editType = "绘制虚线矩形(" + allRecordStack.size() + ")";
                DrawType.RECTANGLE_DASHED.draw(editContext, startX, startY, currentX, currentY);
                allRecordStack.push(new DrawRecords("rectDashed", startX, startY, currentX, currentY, null, forecolor, tick));
            }
            case 26 -> {
                //绘制虚线半透明矩形
                editType = "绘制虚线半透明矩形(" + allRecordStack.size() + ")";
                DrawType.RECTANGLE_DASHED_FILLED.draw(editContext, startX, startY, currentX, currentY, forecolor);
                allRecordStack.push(new DrawRecords("rectDashedFilled", startX, startY, currentX, currentY, null, forecolor, tick));
            }
            case 27 -> {
                double imageStartX = (startX - canvas.getTranslateX()) / canvas.getScaleX();
                double imageStartY = (startY - canvas.getTranslateY()) / canvas.getScaleY();
                double imageEndX = (currentX - canvas.getTranslateX()) / canvas.getScaleX();
                double imageEndY = (currentY - canvas.getTranslateY()) / canvas.getScaleY();
                // 计算矩形区域的宽度和高度
                int width = (int) Math.abs(imageEndX - imageStartX);
                int height = (int) Math.abs(imageEndY - imageStartY);
                // 确保起始点为左上角的坐标
                int x = (int) Math.min(imageStartX, imageEndX);
                int y = (int) Math.min(imageStartY, imageEndY);
                BufferedImage bf = SwingFXUtils.fromFXImage(canvas.snapshot(null, null), null);
                BufferedImage subImage = bf.getSubimage(x, y, width, height);
                if (tess == null) {
                    tess = new Tesseract();
                    tess.setLanguage("chi_sim+eng");
                    tess.setDatapath("D:/Tesseract-OCR/tessdata"); // 设置 tesseract 数据路径（训练数据）
                }
                CompletableFuture.supplyAsync(() -> {
                    try {
                        // 模拟繁琐任务
                        return tess.doOCR(subImage);
                    } catch (TesseractException e) {
                        throw new RuntimeException(e);
                    }
                }).thenAccept(res -> {
                    Platform.runLater(() -> {
                        if (textStage != null) {
                            textStage.setText(res);
                            if (!textStage.isShowing()) {
                                textStage.show();
                            }
                        } else {
                            textStage = new TextRecognitionStage(res, parent);
                            textStage.show();
                        }
                    });
                });
            }
            default -> editType = "未知操作(" + allRecordStack.size() + ")";
        }
        isResizing = false;
        if (!editType.isBlank()) {
            editHistory.add(editType);
            setState(editType.substring(0, editType.indexOf("(")));
        }

    }

    private void initToolManager() {
        tools.getItems().clear();
        toolbarManager.addAll(upload, createEmpty, save, copy, undo, redo, reset, clear, drag, pencil, rubber);
        toolbarManager.addAll(line, rect, arrow, oval);
        toolbarManager.addAll(colorPicker, strokeSlider, addImage, addText, ocr, processType, process, setting, exit);
        toolbarManager.addToToolBar();
    }

    private void initPickers() throws IOException {
        arrow = new ArrowPicker(new Image(ScreenCaptureToolApp.class.getResource("assets/icon/arrow.png").toExternalForm()),
                ResourceLoader.getAsLines("picker/arrow.txt"), this);
        rect = new RectanglePicker(new Image(ScreenCaptureToolApp.class.getResource("assets/icon/rect.png").toExternalForm()),
                ResourceLoader.getAsLines("picker/rect.txt"), this);
        oval = new OvalPicker(new Image(ScreenCaptureToolApp.class.getResource("assets/icon/oval.png").toExternalForm()),
                ResourceLoader.getAsLines("picker/oval.txt"), this);
        line = new LinePicker(new Image(ScreenCaptureToolApp.class.getResource("assets/icon/line.png").toExternalForm()),
                ResourceLoader.getAsLines("picker/line.txt"), this);
        initToolManager();
    }

    public void addExternalText(DrawableText text) {
        //TODO
        long tick = new Timestamp(System.currentTimeMillis()).getTime();
        DrawRecords record = new DrawRecords("externalText", text,"init", tick);
        allRecordStack.push(record);
        String editType = "文字-添加 " + "(" + allRecordStack.size() + ")";
        editHistory.add(editType);
    }


    private void addExternalImage(double x, double y) {
        addImage();
        FileChooser chooser = new FileChooser();
        chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter(
                "图片", "png", "jpg"
        ));
        chooser.setInitialDirectory(CaptureProperties.getSelectDirectory());
        File file = chooser.showOpenDialog(parent);
        if (file != null) {
            CaptureProperties.updateSelectPath(file.getParent());
            Image image = new Image("file:" + file.getAbsolutePath());
            //TODO : get scaled size
            RectanglePair size = ImageFormatHandler.getScaledSize(image, animator.getWidth(), animator.getHeight());
            Timestamp stamp = new Timestamp(System.currentTimeMillis());
            DrawableImage drawableImage = new DrawableImage(image, x, y, size.getW(), size.getH(), file.getName() + stamp.getTime());
            drawableImage.setRenderBorder(true);
            drawableImage.setOriX(x);
            drawableImage.setOriY(y);
            imageHandler.addExternalImage(drawableImage);
            long tick = new Timestamp(System.currentTimeMillis()).getTime();
            DrawRecords record = new DrawRecords("externalImg", drawableImage, "init", tick);
            allRecordStack.push(record);
            selectedImage = drawableImage;
            imageHandler.drawAllImages(editArea.getGraphicsContext2D());
            editHistory.add("添加图片(" + allRecordStack.size() + ")");
        }
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


    private void repaintCanvas(GraphicsContext gc, boolean renderSelectedImg,boolean renderSelectedText, boolean shouldClear) {
        if (shouldClear) {
            gc.clearRect(0, 0, editArea.getWidth(), editArea.getHeight());
        }
        initRepaintStack(allRecordStack);
        for (int i = 0; i < repaintStack.size(); i++) {
            DrawRecords record = repaintStack.get(i);
            if ("externalImg".equals(record.getDrawType()) &&selectedImage!=null &&selectedImage.getImage().equals(record.getImage())) {
                if (renderSelectedImg) {
                    record.draw(gc, animator.getGraphicsContext2D());
                }
            }else if("externalText".equals(record.getDrawType()) && selectedText!=null&& selectedText.getValue().equals(record.getText())){
                if (renderSelectedText) {
                    record.draw(gc, animator.getGraphicsContext2D());
                }
            }else {
                record.draw(gc, animator.getGraphicsContext2D());
            }
        }
        clearRepaintState();
    }

    private void jumpToCertainCanvas(int index) {
        clearCanvas(animator);
        GraphicsContext gc = clearCanvas(editArea);
        Stack<DrawRecords> allRecord = new Stack<>();
        allRecord.addAll(allRecordStack);
        allRecord.addAll(undoStack);
        initRepaintStack(allRecord);
        for (int i = 0; i <= index; i++) {
            if (i >= repaintStack.size()) {
                break;
            }
            DrawRecords record = repaintStack.get(i);
            record.draw(gc, animator.getGraphicsContext2D());
        }
        clearRepaintState();
    }

    //设置记录为图片的shouldRepaint为true
    private void clearRepaintState() {
        for (DrawRecords record : allRecordStack) {
            if ("externalImg".equals(record.getDrawType())) {
                record.setShouldRepaint(true);
            }
            if ("externalText".equals(record.getDrawType())) {
                record.setShouldRepaint(true);
            }
        }
    }

    private void onKeyReleased(KeyEvent e) {
        isAltPressed=false;
        isShiftPressed=false;
        isCtrlPressed=false;
        if(textPane!=null&&!textPane.shouldAddTextField()){
            return;
        }
        type = oriType;
        backToOriginMode();
        clearCanvas(animator);
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
    }

    private void onKeyPressed(KeyEvent e) {
        oriType = type;
        isAltPressed=false;
        isShiftPressed=false;
        isCtrlPressed=false;
        if (e.getCode() == KeyCode.ESCAPE) {
            copy();
            parent.close();
        }
        if (textPane==null||e.getCode() == KeyCode.SHIFT&&textPane.shouldAddTextField()) {
            rubberMode();
            isShiftPressed = true;
        }
        if (e.getCode() == KeyCode.ALT) {//&&!moving
            if(textPane!=null&&textPane.shouldAddTextField()){
                dragMode();
            }
            isAltPressed = true;
        }
        if (e.getCode() == KeyCode.CONTROL) {
            isCtrlPressed = true;
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
    }

    private void deleteExternalImage(ActionEvent e) {
        if (selectedImage != null) {
            imageHandler.removeExternalImage(selectedImage);
            clearCanvas(animator);
            //TODO check
            long tick = new Timestamp(System.currentTimeMillis()).getTime();
            allRecordStack.push(new DrawRecords("externalImg", selectedImage, "delete", tick));
            editHistory.add("删除图片(" + allRecordStack.size() + ")");
//                        editRecordImageRender(false,selectedImage);
            repaintCanvas(editArea.getGraphicsContext2D(), false, false,true);
            imageHandler.drawAllImages(editArea.getGraphicsContext2D());
            selectedImage = null;
        }
    }

    private void adjustCanvasScale(ScrollEvent event) {
        double zoomFactor = 1.1;
        if (event.getDeltaY() < 0) {
            zoomFactor = 1 / zoomFactor; // 如果是滚轮向下，缩小
        }
        // 缩放操作
        canvas.setScaleX(canvas.getScaleX() * zoomFactor);
        canvas.setScaleY(canvas.getScaleY() * zoomFactor);
        editArea.setScaleX(editArea.getScaleX() * zoomFactor);
        editArea.setScaleY(editArea.getScaleY() * zoomFactor);
        animator.setScaleX(animator.getScaleX() * zoomFactor);
        animator.setScaleY(animator.getScaleY() * zoomFactor);
        animator.setScaleY(animator.getScaleY() * zoomFactor);
        // 计算缩放后画布中心的偏移量
        // 获取鼠标在 canvas 上的坐标
        if (CaptureProperties.scaleOnMouse) {
            double mouseX = event.getX();
            double mouseY = event.getY();
            double offsetX = mouseX - (mouseX * zoomFactor);
            double offsetY = mouseY - (mouseY * zoomFactor);

            // 调整 canvas 的平移，使得缩放点对齐鼠标位置
            canvas.setTranslateX(canvas.getTranslateX() + offsetX);
            canvas.setTranslateY(canvas.getTranslateY() + offsetY);
        }
    }

    private void initRepaintStack(Stack<DrawRecords> recordStack) {
        repaintStack.clear();
        for (DrawRecords record : recordStack) {
            if ("externalImg".equals(record.getDrawType())) {
                DrawRecords lastRecord = null;
                for (DrawRecords record1 : recordStack) {
                    if (record.getImage().equals(record1.getImage())) {
                        if (record1.getEditTick() >= record.getEditTick()) {
                            lastRecord = record1;
                        } else {
                            record1.setShouldRepaint(false);
                        }
                    }
                }
                if (lastRecord != null) {
                    lastRecord.setShouldRepaint(true);
                    if (lastRecord.getDrawableImage().equals(selectedImage)) {
                        lastRecord.getDrawableImage().setRenderBorder(true);
                    }
                    repaintStack.push(lastRecord);
                }
            }else if ("externalText".equals(record.getDrawType())) {
                DrawRecords lastRecord = null;
                for (DrawRecords record1 : recordStack) {
                    //TODO
                    if (record.getText().equals(record1.getText())) {
                        if (record1.getEditTick() >= record.getEditTick()) {
                            lastRecord = record1;
                        } else {
                            record1.setShouldRepaint(false);
                        }
                    }
                }
                if (lastRecord != null) {
                    lastRecord.setShouldRepaint(true);
                    if (lastRecord.getDrawableText().equals(selectedText)) {
                        lastRecord.getDrawableText().setRenderBorder(true);
                    }
                    repaintStack.push(lastRecord);
                }
            } else {
                repaintStack.push(record);
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
        fc.setInitialDirectory(CaptureProperties.getSelectDirectory());
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
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setTransform(Transform.scale(CaptureProperties.scale, CaptureProperties.scale));  // 扩大图像
        repaintCanvas(canvas.getGraphicsContext2D(), true,true, false);
        WritableImage writableImage = canvas.snapshot(parameters, null);
        // 转换为 BufferedImage
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);
        // 保存为文件
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image");
        fileChooser.setInitialFileName("capture.png");
        fileChooser.setInitialDirectory(CaptureProperties.getSelectDirectory());
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                boolean flag = ImageIO.write(bufferedImage, "png", file);
                AlertHelper.showAutoClosedPopup(flag ? "保存成功！" : "保存失败！！！", 1, parent.getX() + 100, parent.getY() + parent.getHeight() + 20);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        drawImageToCanvas(originalImage);
        state.setText("图片已保存");
    }

    @FXML
    public void copy() {
        canvas.setScaleX(1);
        canvas.setScaleY(1);
        editArea.setScaleX(1);
        editArea.setScaleY(1);
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setTransform(Transform.scale(CaptureProperties.scale, CaptureProperties.scale));  // 扩大图像
        repaintCanvas(canvas.getGraphicsContext2D(), true, true,false);
        WritableImage writableImage = canvas.snapshot(parameters, null);
        TransferableImage transferableImage = new TransferableImage(writableImage);
        // 复制到剪贴板
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferableImage, null);
        drawImageToCanvas(originalImage);
        AlertHelper.showAutoClosedPopup("复制成功！", 1, parent.getX() + 100, parent.getY() + parent.getHeight() + 20);
        state.setText("图片已复制");
    }

    @FXML
    public void colorPicker() {
        forecolor = colorPicker.getValue();
        clearCanvas(animator);
    }

    @FXML
    public void undo() {
        //TODO
        if (!allRecordStack.isEmpty()) {  // 至少保留一个初始图像
            DrawRecords popRecord = allRecordStack.pop();
            if ("externalImg".equals(popRecord.getDrawType())){
                if("init".equals(popRecord.getDetailInfo())){
                    //如果是最后一个相关记录，则设置为伪移除
                    popRecord.getDrawableImage().setUndo(true);
                }
            }else if("externalText".equals(popRecord.getDrawType())){
                if("init".equals(popRecord.getDetailInfo())){
                    //如果是最后一个相关记录，则设置为伪移除
                    popRecord.getDrawableText().setUndo(true);
                }
            }
            undoStack.push(popRecord);
            //如果不是最后一个，则修改为上一个的坐标
            if (!allRecordStack.isEmpty()) {
                DrawRecords lastRecord = allRecordStack.getLast();
                if ("externalImg".equals(lastRecord.getDrawType())) {
                    selectedImage = lastRecord.getDrawableImage();
                    adjustSelectedImagePos(lastRecord);
                } else if("externalText".equals(lastRecord.getDrawType())){
                    selectedText = lastRecord.getDrawableText();
                    adjustSelectedTextPos(lastRecord);
                }else {
                    selectedImage = null;
                    selectedText = null;
                }
            }
            clearCanvas(animator);
            repaintCanvas(editArea.getGraphicsContext2D(), true, true,true);
        }
    }

    private void adjustSelectedTextPos(DrawRecords lastRecord) {
        selectedText.setX(lastRecord.getStartX());
        selectedText.setY(lastRecord.getStartY());
        selectedText.setWidth(lastRecord.getWidth());
        selectedText.setHeight(lastRecord.getHeight());
    }

    private void adjustSelectedImagePos(DrawRecords lastRecord) {
        selectedImage.setX(lastRecord.getStartX());
        selectedImage.setY(lastRecord.getStartY());
        selectedImage.setWidth(lastRecord.getWidth());
        selectedImage.setHeight(lastRecord.getHeight());
    }

    // 重做操作
    @FXML
    public void redo() {
        if (!undoStack.isEmpty()) {
            DrawRecords popRecord = undoStack.pop();
            if ("externalImg".equals(popRecord.getDrawType())) {
                if("init".equals(popRecord.getDetailInfo())){
                    popRecord.getDrawableImage().setUndo(false);
                }
            } else if("externalText".equals(popRecord.getDrawType())){
                if("init".equals(popRecord.getDetailInfo())){
                    popRecord.getDrawableText().setUndo(false);
                }
            }
            if (!undoStack.isEmpty()) {
                DrawRecords lastRecord = undoStack.getLast();
                if ("externalImg".equals(lastRecord.getDrawType())) {
                    selectedImage = lastRecord.getDrawableImage();
                    if("init".equals(popRecord.getDetailInfo())){
                        selectedImage.setUndo(false);
                    }
                    adjustSelectedImagePos(lastRecord);
                } else if("externalText".equals(lastRecord.getDrawType())){
                    selectedText = lastRecord.getDrawableText();
                    adjustSelectedTextPos(lastRecord);
                }else {
                    selectedImage = null;
                    selectedText = null;
                }
            }
            allRecordStack.push(popRecord);
            clearCanvas(animator);
            repaintCanvas(editArea.getGraphicsContext2D(), true, true,true);

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
        textHandler.clearAll();
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

    @FXML
    private void processImage() {
        if ("灰化".equals(processType.getValue())) {
            Mat src = ImageFormatHandler.toMat(canvas.snapshot(null, null));
            Mat gray = new Mat();
            Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
            WritableImage image = SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(gray), null);
            drawImageToCanvas(image);
        } else if ("锐化".equals(processType.getValue())) {
            Mat src = ImageFormatHandler.toMat(canvas.snapshot(null, null));
            Mat sharpenKernel = new Mat(3, 3, CvType.CV_32F);
            sharpenKernel.put(0, 0, 0, -1, 0, -1, 5, -1, 0, -1, 0);  // 常见的锐化卷积核
            // 通过卷积操作锐化图像
            Mat sharpenedImage = new Mat();
            Imgproc.filter2D(src, sharpenedImage, src.depth(), sharpenKernel);
            WritableImage image = SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(sharpenedImage), null);
            drawImageToCanvas(image);
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
        } else if ("边缘提取".equals(processType.getValue())) {
            Mat src = ImageFormatHandler.toMat(canvas.snapshot(null, null));
            Mat edges = new Mat();
            Imgproc.Canny(src, edges, 100, 200);
            WritableImage image = SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(edges), null);
            drawImageToCanvas(image);
        } else if ("均值平滑".equals(processType.getValue())) {
            Mat src = ImageFormatHandler.toMat(canvas.snapshot(null, null));
            Mat meanBlurred = new Mat();
            Size kernelSize = new Size(5, 5);  // 5x5的卷积核
            Imgproc.blur(src, meanBlurred, kernelSize);
            WritableImage image = SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(meanBlurred), null);
            drawImageToCanvas(image);
        } else if ("高斯平滑".equals(processType.getValue())) {
            Mat src = ImageFormatHandler.toMat(canvas.snapshot(null, null));
            Mat meanBlurred = new Mat();
            Size kernelSize = new Size(5, 5);  // 5x5的卷积核
            Imgproc.GaussianBlur(src, meanBlurred, kernelSize, 0);
            WritableImage image = SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(meanBlurred), null);
            drawImageToCanvas(image);
        } else if ("人脸识别".equals(processType.getValue())) {
            Mat src = ImageFormatHandler.toMat(canvas.snapshot(null, null));
            Mat meanBlurred = new Mat();
            Size kernelSize = new Size(5, 5);  // 5x5的卷积核
            Imgproc.blur(src, meanBlurred, kernelSize);
            WritableImage image = SwingFXUtils.toFXImage(ImageFormatHandler.toBufferedImage(meanBlurred), null);
            drawImageToCanvas(image);
        }
    }

    public void clearAllRecord() {
        clearCanvas(editArea);
        capture.setImage(originalImage);
        resizeStage();
        imageHandler.clearAll();
        textHandler.clearAll();
        allRecordStack.clear();
        editHistory.clear();
        undoStack.clear();
        if (editController != null) {
            editController.clearAllRecords();
        }
        clearCanvas(animator);
        clearCanvas(editArea);
    }


    public void jumpTo(int index) {
        //TODO 修改逻辑
        if (index >= 0 && index < editHistory.size()) {
            jumpToCertainCanvas(index);
            adjustUndoStack(index);
        }
    }

    private void adjustUndoStack(int index) {
        if (index >= allRecordStack.size()) {
            int n = index - allRecordStack.size();
            for (int i = 0; i <= n; i++) {
                if (!undoStack.isEmpty()) {
                    allRecordStack.push(undoStack.pop());
                }
            }
        } else {
            int n = allRecordStack.size() - index - 1;
            for (int i = 0; i < n; i++) {
                if (!allRecordStack.isEmpty()) {
                    undoStack.push(allRecordStack.pop());
                }
            }
        }
        for (DrawRecords re : allRecordStack) {
            if ("externalImg".equals(re.getDrawType())) {
                imageHandler.addExternalImage(re.getDrawableImage());
            }
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
       for(DrawableText text:textHandler.getTexts()){
           System.out.println(text.toString());
       }
    }

    public void test1() throws IOException {
        System.out.println("===========  全部编辑记录 ===========");
        for(DrawRecords r: allRecordStack){
            System.out.println(r.toString());
        }
        System.out.println("-------  undo 记录 ---------");
        for(DrawRecords r:undoStack){
            System.out.println(r.toString());
        }
        System.out.println("==========================");
    }

    private GraphicsContext clearCanvas(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        return gc;
    }

    private void backToOriginMode() {
        switch (type) {
            case -1 -> rubberMode();
            case 0 -> dragMode();
            case 1 -> pencilMode();
            case 8 -> addImage();
            case 9 -> addText();
            default -> setCursorShape(Cursor.CROSSHAIR);
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

    @FXML
    private void dragMode() {
        type = 0;
        setCursorShape(Cursor.HAND);
    }

    @FXML
    private void addImage() {
        type = 8;
        editArea.setCursor(Cursor.HAND);
    }

    @FXML
    private void addText() {
        type = 9;
        editArea.setCursor(Cursor.HAND);
        setGraphicsContextFont();
    }

    @FXML
    public void pencilMode() {
        // 处理铅笔模式
        type = 1;
        Image cursorImage = new Image(ScreenCaptureToolApp.class.getResource("assets/icon/pencil.png").toExternalForm()); // 替换为你自己的图像路径
        ImageCursor customCursor = new ImageCursor(cursorImage, 0, 32); // (16,16) 是热点位置（图像的中心）
        setCursorShape(customCursor);
    }

    @FXML
    public void rubberMode() {
        // 处理橡皮模式
        type = -1;
        Image cursorImage = new Image(ScreenCaptureToolApp.class.getResource("assets/icon/rubber.png").toExternalForm()); // 替换为你自己的图像路径
        ImageCursor customCursor = new ImageCursor(cursorImage, 0, 32); // (16,16) 是热点位置（图像的中心）
        setCursorShape(customCursor);
        animator.getGraphicsContext2D().setStroke(Color.GREEN);
    }

    public void setType(int index) {
        this.type = index;
    }

    public void setCursorShape(Cursor cursor) {
        editArea.setCursor(cursor);
        clearCanvas(animator);
    }

    public void setState(String text) {
        Platform.runLater(() -> {
            state.setText(text);
        });
    }
    public boolean isMoving(){
        return this.moving;
    }
    public void setMoving(boolean moving){
        this.moving=moving;
    }
    public double getToolBarHeight(){
        return this.tools.getHeight();
    }
    public boolean isAltPressed(){
        return this.isAltPressed;
    }
    public ExternalTextHandler getTextHandler(){
        return this.textHandler;
    }
    public Color getStrokeColor(){
        return this.forecolor;
    }
    public GraphicsContext getGraphicsContext(){
        return editArea.getGraphicsContext2D();
    }
    public void setFont(Font font){
        this.font=font;
        setGraphicsContextFont(font);
    }
    public Font getFont(){
        return this.font==null ? new Font("Arial",16) : font;
    }
    private void setGraphicsContextFont() {
        editArea.getGraphicsContext2D().setFont(font);
        animator.getGraphicsContext2D().setFont(font);
    }
    public void setGraphicsContextFont(Font font) {
        editArea.getGraphicsContext2D().setFont(font);
        animator.getGraphicsContext2D().setFont(font);
    }
}
