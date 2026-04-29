package net.jackchuan.screencapturetool.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.scene.Group;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.transform.Transform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import net.jackchuan.screencapturetool.CaptureProperties;
import net.jackchuan.screencapturetool.ScreenCaptureToolApp;
import net.jackchuan.screencapturetool.entity.ControllerInstance;
import net.jackchuan.screencapturetool.entity.DrawRecords;
import net.jackchuan.screencapturetool.entity.DrawTypes;
import net.jackchuan.screencapturetool.external.ExternalImageHandler;
import net.jackchuan.screencapturetool.external.ExternalImageHandler.DrawableImage;
import net.jackchuan.screencapturetool.external.ExternalTextHandler;
import net.jackchuan.screencapturetool.external.ExternalTextHandler.DrawableText;
import net.jackchuan.screencapturetool.external.ToolBarManager;
import net.jackchuan.screencapturetool.external.pane.TextFieldPane;
import net.jackchuan.screencapturetool.external.picker.*;
import net.jackchuan.screencapturetool.external.stage.AlertHelper;
import net.jackchuan.screencapturetool.external.stage.TextRecognitionStage;
import net.jackchuan.screencapturetool.util.*;
import net.jackchuan.screencapturetool.util.impl.CornerType;
import net.jackchuan.screencapturetool.util.impl.DrawType;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/11/20 15:59
 */
public class CaptureDisplayController {
    public BorderPane captureRoot;
    @FXML
    private StackPane stackPane;
    public Label state;
    @FXML
    private ComboBox<String> processType;
    private ImageView capture;
    @Setter
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
    private VBox sideBar;
    @FXML
    private Button upload, createEmpty, save, copy, undo, redo, tailor, reset, clear, drag, pencil, rubber, addImage, addText, process, setting, exit;
    @FXML
    private CustomPicker arrow, rect, line, oval, ocr;
    private Color forecolor = Color.RED;
    @Setter
    private int type = -2, oriType, stroke;
    private double initialX, initialY, startX, startY;
    @Getter
    private Stage parent, history, settingStage;
    private List<String> editHistory;
    private Stack<DrawRecords> allRecordStack;
    private Stack<DrawRecords> repaintStack;
    private Stack<DrawRecords> undoStack;
    private EditRecordController editController;
    private ExternalImageHandler imageHandler;
    @Getter
    private ExternalTextHandler textHandler;
    private DrawableImage selectedImage = null;
    private DrawableText selectedText = null;
    private double dragStartX;
    private double dragStartY;
    @Setter
    @Getter
    private boolean isResizing = false, moving;
    private ContextMenu popMenu;
    private MenuItem delete, addImg, addStr;
    private CornerType cornerType;
    private TextFieldPane textPane;
    private ToolBarManager toolbarManager;
    private TextRecognitionStage textStage;
    private Font font;
    private ExecutorService executor;
    private Group canvasGroup;
    private List<Point2D> handPoints;
    @FXML
    public void initialize() throws IOException {
        editHistory = new ArrayList<>();
        allRecordStack = new Stack<>();
        repaintStack = new Stack<>();
        executor = Executors.newFixedThreadPool(4);
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
        textHandler = new ExternalTextHandler(editArea, animator);
        stackPane.getChildren().remove(canvas);
        canvasGroup = new Group(canvas, editArea, animator);
        stackPane.getChildren().add(canvasGroup);
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.widthProperty().bind(stackPane.widthProperty());
        clip.heightProperty().bind(stackPane.heightProperty());
        stackPane.setClip(clip);
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
                parent.widthProperty().addListener((observable, oldValue, newValue) -> {
                    if(newValue.doubleValue()<oldValue.doubleValue()){
//                        resizeStage(newValue.doubleValue(),1);
                    }
                });
                parent.heightProperty().addListener((observable, oldValue, newValue) -> {
                    if(newValue.doubleValue()<oldValue.doubleValue()){
//                        resizeStage(newValue.doubleValue(),0);
                    }
                });
                popMenu = new ContextMenu();
                delete = new MenuItem("删除");
                addImg = new MenuItem("添加图片");
                addStr = new MenuItem("添加文字");
                popMenu.getItems().addAll(addImg, addStr, delete);
                delete.setOnAction(this::deleteExternalContent);
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
                addDragEventToParent();
            }
            ControllerInstance.getInstance().setController(this);
            resizeStage();
        });

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void resizeStage(double v,int isWidth) {
        if(isWidth==1){
            //宽度
            capture.setFitWidth(v);
            resizeStage();
        }else {
            //高度
            double rate = capture.getFitWidth()/capture.getFitHeight();
            capture.setFitWidth(v*rate);
            resizeStage();
        }
    }

    private void addDragEventToParent() {
        captureRoot.setOnDragOver(e -> {
            if (e.getGestureSource() != captureRoot && (e.getDragboard().hasFiles()||e.getDragboard().hasImage())) {
                e.acceptTransferModes(TransferMode.COPY);
            }
            e.consume();
        });
        captureRoot.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            if(db.hasImage()){
                Image image = db.getImage();
                setCapture(image,false);
            }else if (db.hasFiles()) {
                for (File file : db.getFiles()) {
                    if(file.getName().endsWith(".png")||file.getName().endsWith(".jpg")) {
                        try {
                            BufferedImage read = ImageIO.read(file);
                            setCapture(ImageFormatHandler.toFXImage(read),false);
                            break;
                        } catch (IOException ex) {
                            ScreenCaptureToolApp.LOGGER.error("读取剪切板图片内容失败,",ex);
                            throw new RuntimeException(ex);
                        }
                    }
                }
                e.setDropCompleted(true);
            } else {
                e.setDropCompleted(false);
            }
            e.consume();
        });
    }

    private void changeToExternalMode(MouseEvent e) {
        if (type == 8 && selectedImage == null && e.getClickCount() == 2) {
            addExternalImage(e.getX(), e.getY());
        } else if (type == 9 && e.getClickCount() == 2) {
            showTextInputArea(e.getX(), e.getY());
        }
    }

    private void initDrawType(MouseEvent event) {
        selectedImage = null;
        selectedText = null;
        if (type == 0) {
            initialX = event.getSceneX();
            initialY = event.getSceneY();
        } else if (type == 1 || type == -1) {
            Point2D local = editArea.sceneToLocal(event.getSceneX(), event.getSceneY());
            initialX = local.getX();
            initialY = local.getY();
            startX = local.getX();
            startY = local.getY();
            handPoints = new ArrayList<>();
            handPoints.add(new Point2D(initialX, initialY));
        } else if (type == 8) {
            for (ExternalImageHandler.DrawableImage image : imageHandler.getImages()) {
                if (!image.isUndo() && image.isInside(event.getX(), event.getY())) {
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
            repaintCanvas(editArea.getGraphicsContext2D(), false, false, true);
            if(selectedImage!=null){
                imageHandler.updateImage(animator.getGraphicsContext2D(), selectedImage);
            }

        } else if (type == 9) {
            if (textPane != null && !textPane.isInField(event)) {
                textPane.removeTextField();
            }
            for (DrawableText text : textHandler.getTexts()) {
                if (!text.isUndo() && text.isInside(event.getX(), event.getY())) {
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
            if(selectedText!=null){
                ScreenCaptureToolApp.LOGGER.info("selectedText :" + selectedText.toString());
                textHandler.updateText(animator.getGraphicsContext2D(), selectedText);
            }
            repaintCanvas(editArea.getGraphicsContext2D(), false, false, true);
        } else {
            startX = event.getX();
            startY = event.getY();
        }
        if (event.getButton() == MouseButton.SECONDARY) { // 右键
            popMenu.show(animator, event.getScreenX(), event.getScreenY());
        }
        if (event.getButton() != MouseButton.SECONDARY) {
            popMenu.hide();
        }
    }

    private void showTextInputArea(double x, double y) {
        if (textPane == null) {
            textPane = new TextFieldPane(this, canvas.getWidth(), canvas.getHeight());
            textPane.setTextFieldPos(x, y);
            stackPane.getChildren().add(textPane);
            textPane.setVisible(true);
        } else {
            textPane.addTextField(selectedText != null ? selectedText.getValue() : "",
                    selectedText != null ? selectedText.getFont() : new Font("Arial", 16));
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
        pos += "Size : (" + (int) canvas.getWidth() + "," + (int) canvas.getHeight() + ")  ";
        setState(pos);
        if (type == -1) {
            GraphicsContext editContext = animator.getGraphicsContext2D();
            editContext.clearRect(0, 0, animator.getWidth(), animator.getHeight());
            editContext.strokeRect(e.getX(), e.getY(), stroke * 10, stroke * 10);
        } else if (type == 8) {
            for (ExternalImageHandler.DrawableImage image : imageHandler.getImages()) {
                if (!image.equals(selectedImage)) {
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
        } else if (type == 9) {
            for (DrawableText text : textHandler.getTexts()) {
                if (!text.equals(selectedText)) {
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
            canvasGroup.setTranslateX(canvasGroup.getTranslateX() + deltaX);
            canvasGroup.setTranslateY(canvasGroup.getTranslateY() + deltaY);
            initialX = event.getSceneX();
            initialY = event.getSceneY();
        } else if (type == 1) {
            // 绘制普通线条
            GraphicsContext editGc = editArea.getGraphicsContext2D();
            editGc.setStroke(forecolor);
            editGc.setLineWidth(strokeSlider.getValue());
            editGc.strokeLine(startX, startY, event.getX(), event.getY());
            Point2D local = editArea.sceneToLocal(event.getSceneX(), event.getSceneY());
            startX = local.getX();
            startY = local.getY();
            handPoints.add(new Point2D(startX, startY));
        } else {
            editContext.setStroke(forecolor);
            editContext.setLineWidth(strokeSlider.getValue());
            editContext.clearRect(0, 0, animator.getWidth(), animator.getHeight());
            double currentX = event.getX();
            double currentY = event.getY();
            switch (type) {
                //橡皮擦
                case -1 -> {
                    Point2D localPos = editArea.sceneToLocal(event.getSceneX(), event.getSceneY());
                    startX = localPos.getX();
                    startY = localPos.getY();
                    handPoints.add(new Point2D(startX, startY));
                    editContext.setStroke(Color.GREEN);
                    editContext.setLineWidth(2);
                    editContext.strokeRect(event.getX(), event.getY(), stroke * 10, stroke * 10);
                    GraphicsContext gc = editArea.getGraphicsContext2D();
                    gc.clearRect(startX, startY, stroke * 10, stroke * 10);
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
                case 27, 29 -> {
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
        double deltaX,deltaY;
        if(type==1||type==-1){
            Point2D local = editArea.sceneToLocal(event.getSceneX(), event.getSceneY());
            deltaX = Math.abs(local.getX()-initialX);
            deltaY = Math.abs(local.getY()-initialY);
        }else {
            deltaX=Math.abs(event.getX() - initialX);
            deltaY=Math.abs(event.getY() - initialY);
        }
        if ( deltaX<= 5 && deltaY <= 5) {
            if (selectedImage != null) {
                selectedImage.setShouldRender(true);
                imageHandler.drawAllImages(editContext);
            }
            if (selectedText != null) {
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
            case -1 -> {
                editType = "橡皮擦除(" + allRecordStack.size() + ")";
                allRecordStack.push(new DrawRecords(DrawTypes.ERASER,handPoints,Color.TRANSPARENT, tick,stroke));
            }
            case 0 -> {
                editType = "图片移动(" + allRecordStack.size() + ")";
            }
            case 1 -> {
                editType = "普通绘画(" + allRecordStack.size() + ")";
                //通过DrawRecords的image来还原
                allRecordStack.push(new DrawRecords(DrawTypes.COMMON,handPoints,forecolor, tick,stroke));
            }
            case 2 -> {
                editType = "绘制矩形框(" + allRecordStack.size() + ")";
                DrawType.RECTANGLE.draw(editContext, startX, startY, currentX, currentY);
                allRecordStack.push(new DrawRecords(DrawTypes.RECT, startX, startY, currentX, currentY, null, forecolor, tick,stroke));
            }
            case 3 -> {
                editType = "绘制透明填充的矩形框(" + allRecordStack.size() + ")";
                DrawType.FILLED_RECTANGLE.draw(editContext, startX, startY, currentX, currentY, forecolor);
                allRecordStack.push(new DrawRecords(DrawTypes.FILL_RECT, startX, startY, currentX, currentY, null, forecolor, tick,stroke));
            }
            case 4 -> {
                editType = "绘制箭头(" + allRecordStack.size() + ")";
                DrawType.ARROW.draw(editContext, startX, startY, currentX, currentY);
                allRecordStack.push(new DrawRecords(DrawTypes.ARROW, startX, startY, currentX, currentY, null, forecolor, tick,stroke));
            }
            case 5 -> {
                editType = "绘制直线(" + allRecordStack.size() + ")";
                DrawType.LINE.draw(editContext, startX, startY, currentX, currentY);
                allRecordStack.push(new DrawRecords(DrawTypes.LINE, startX, startY, currentX, currentY, null, forecolor, tick,stroke));
            }
            case 6 -> {
                editType = "绘制波浪线(" + allRecordStack.size() + ")";
                DrawType.WAVE.draw(editContext, startX, startY, currentX, currentY);
                allRecordStack.push(new DrawRecords(DrawTypes.WAVE, startX, startY, currentX, currentY, null, forecolor, tick,stroke));
            }
            case 7 -> {
                editType = "绘制圆(" + allRecordStack.size() + ")";
                DrawType.CIRCLE.draw(editContext, startX, startY, currentX, currentY);
                allRecordStack.push(new DrawRecords(DrawTypes.CIRCLE, startX, startY, currentX, currentY, null, forecolor, tick,stroke));
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
                    DrawRecords record = new DrawRecords(DrawTypes.EXTERNAL_IMAGE, selectedImage, type, tick);
                    allRecordStack.push(record);
                    editType = "图片-" + type + "(" + allRecordStack.size() + ")";
                    editHistory.add(editType);
                    repaintCanvas(editContext, true, true, true);
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
                    DrawRecords record = new DrawRecords(DrawTypes.EXTERNAL_TEXT, selectedText, type, tick);
                    allRecordStack.push(record);
                    editType = "文字-" + type + "(" + allRecordStack.size() + ")";
                    editHistory.add(editType);
                }
            }
            case 10 -> {
                editType = "绘制透明填充的圆(" + allRecordStack.size() + ")";
                DrawType.FILLED_CIRCLE.draw(editContext, startX, startY, currentX, currentY, forecolor);
                allRecordStack.push(new DrawRecords(DrawTypes.FILLED_OVAL, startX, startY, currentX, currentY, null, forecolor, tick,stroke));
            }
            case 11 -> {
                //绘制虚线
                editType = "绘制虚线(" + allRecordStack.size() + ")";
                DrawType.LINE_DASHED.draw(editContext, startX, startY, currentX, currentY);
                allRecordStack.push(new DrawRecords(DrawTypes.LINE_DASHED, startX, startY, currentX, currentY, null, forecolor, tick,stroke));
            }
            case 12 -> {
                //绘制双画线
                editType = "绘制双画线(" + allRecordStack.size() + ")";
                DrawType.LINE_DOUBLE.draw(editContext, startX, startY, currentX, currentY);
                allRecordStack.push(new DrawRecords(DrawTypes.LINE_DOUBLE, startX, startY, currentX, currentY, null, forecolor, tick,stroke));
            }
            case 13 -> {
                //绘制圆角矩形
                editType = "绘制圆角矩形(" + allRecordStack.size() + ")";
                DrawType.RECTANGLE_ROUND.draw(editContext, startX, startY, currentX, currentY);
                allRecordStack.push(new DrawRecords(DrawTypes.RECT_ROUND, startX, startY, currentX, currentY, null, forecolor, tick,stroke));
            }
            case 14 -> {
                //绘制半透明圆角矩形
                editType = "绘制半透明圆角矩形(" + allRecordStack.size() + ")";
                DrawType.RECTANGLE_ROUND_FILLED.draw(editContext, startX, startY, currentX, currentY, forecolor);
                allRecordStack.push(new DrawRecords(DrawTypes.RECT_ROUND_FILLED, startX, startY, currentX, currentY, null, forecolor, tick,stroke));
            }
            case 15 -> {
                //绘制虚线箭头
                editType = "绘制虚线箭头(" + allRecordStack.size() + ")";
                DrawType.ARROW_DASHED.draw(editContext, startX, startY, currentX, currentY);
                allRecordStack.push(new DrawRecords(DrawTypes.ARROW_DASHED, startX, startY, currentX, currentY, null, forecolor, tick,stroke));
            }
            case 16 -> {
                //绘制填充的箭头
                editType = "绘制填充的箭头(" + allRecordStack.size() + ")";
                DrawType.ARROW_FILLED.draw(editContext, startX, startY, currentX, currentY, forecolor);
                allRecordStack.push(new DrawRecords(DrawTypes.ARROW_FILLED, startX, startY, currentX, currentY, null, forecolor, tick,stroke));
            }
            case 17 -> {
                //绘制无填充的箭头
                editType = "绘制无填充的箭头(" + allRecordStack.size() + ")";
                DrawType.ARROW_EMPTY.draw(editContext, startX, startY, currentX, currentY);
                allRecordStack.push(new DrawRecords(DrawTypes.ARROW_EMPTY, startX, startY, currentX, currentY, null, forecolor, tick,stroke));
            }
            case 18 -> {
                //绘制双向箭头
                editType = "绘制双向箭头(" + allRecordStack.size() + ")";
                DrawType.ARROW_TWO_DIR.draw(editContext, startX, startY, currentX, currentY);
                allRecordStack.push(new DrawRecords(DrawTypes.ARROW_TWO_DIR, startX, startY, currentX, currentY, null, forecolor, tick,stroke));
            }
            case 19 -> {
                //绘制双线箭头
                editType = "绘制双线箭头(" + allRecordStack.size() + ")";
                DrawType.ARROW_DOUBLE.draw(editContext, startX, startY, currentX, currentY);
                allRecordStack.push(new DrawRecords(DrawTypes.ARROW_DOUBLE, startX, startY, currentX, currentY, null, forecolor, tick,stroke));
            }
            case 20 -> {
                //绘制虚线圆
                editType = "绘制虚线圆(" + allRecordStack.size() + ")";
                DrawType.OVAL_DASHED.draw(editContext, startX, startY, currentX, currentY);
                allRecordStack.push(new DrawRecords(DrawTypes.OVAL_DASHED, startX, startY, currentX, currentY, null, forecolor, tick,stroke));
            }
            case 21 -> {
                //绘制双虚线的箭头
                editType = "绘制双虚线的箭头(" + allRecordStack.size() + ")";
                DrawType.ARROW_DOUBLE_DASHED.draw(editContext, startX, startY, currentX, currentY);
                allRecordStack.push(new DrawRecords(DrawTypes.ARROW_DOUBLE_DASHED, startX, startY, currentX, currentY, null, forecolor, tick,stroke));
            }
            case 22 -> {
                //绘制双线双向的箭头
                editType = "绘制双线双向的箭头(" + allRecordStack.size() + ")";
                DrawType.ARROW_DOUBLE_TWO_DIR.draw(editContext, startX, startY, currentX, currentY);
                allRecordStack.push(new DrawRecords(DrawTypes.ARROW_DOUBLE_TWO_DIR, startX, startY, currentX, currentY, null, forecolor, tick,stroke));
            }
            case 23 -> {
                //绘制圆角虚线矩形
                editType = "绘制圆角虚线矩形(" + allRecordStack.size() + ")";
                DrawType.RECTANGLE_ROUND_DASHED.draw(editContext, startX, startY, currentX, currentY);
                allRecordStack.push(new DrawRecords(DrawTypes.RECT_ROUND_DASHED, startX, startY, currentX, currentY, null, forecolor, tick,stroke));
            }
            case 24 -> {
                //绘制圆角虚线半透明矩形
                editType = "绘制圆角虚线半透明矩形(" + allRecordStack.size() + ")";
                DrawType.RECTANGLE_ROUND_DASHED_FILLED.draw(editContext, startX, startY, currentX, currentY, forecolor);
                allRecordStack.push(new DrawRecords(DrawTypes.RECT_ROUND_DASHED_FILLED, startX, startY, currentX, currentY, null, forecolor, tick,stroke));
            }
            case 25 -> {
                //绘制虚线矩形
                editType = "绘制虚线矩形(" + allRecordStack.size() + ")";
                DrawType.RECTANGLE_DASHED.draw(editContext, startX, startY, currentX, currentY);
                allRecordStack.push(new DrawRecords(DrawTypes.RECT_DASHED, startX, startY, currentX, currentY, null, forecolor, tick,stroke));
            }
            case 26 -> {
                //绘制虚线半透明矩形
                editType = "绘制虚线半透明矩形(" + allRecordStack.size() + ")";
                DrawType.RECTANGLE_DASHED_FILLED.draw(editContext, startX, startY, currentX, currentY, forecolor);
                allRecordStack.push(new DrawRecords(DrawTypes.RECT_DASHED_FILLED, startX, startY, currentX, currentY, null, forecolor, tick,stroke));
            }
            case 27 -> {
                doOCR(ImageFormatHandler.cropImage(canvas,null ,startX, startY, currentX, currentY));
            }
            case 29 -> {
                //crop image
                //裁剪后分辨率减低，会有点模糊
                //TODO 需添加裁剪撤销等功能，即需要更改drawRecord逻辑
                //TODO 裁剪时应该将记录绘制在图片上后在裁剪 done
                BufferedImage croppedImage = ImageFormatHandler.cropImage(canvas,getImageWithRecords() ,startX, startY, currentX, currentY);
                WritableImage croppedFxImage = ImageFormatHandler.toFXImage(croppedImage);
                allRecordStack.push(new DrawRecords(DrawTypes.CROP, startX, startY, currentX, currentY,getImageWithRecords(),croppedFxImage , forecolor, tick));
                setCapture(croppedFxImage,ScreenCaptureUtil.shouldScale(croppedFxImage));
            }
            default -> editType = "未知操作(" + allRecordStack.size() + ")";
        }
        isResizing = false;
        if (!editType.isBlank()) {
            editHistory.add(editType);
            setState(editType.substring(0, editType.indexOf("(")));
        }
        initialX=-10;
        initialY=-10;
    }

    public void doOCR(BufferedImage image) {
        ScreenCaptureToolApp.LOGGER.info("OCR 开始，tessdata路径={}", CaptureProperties.ocrPath);
        CompletableFuture.supplyAsync(() -> {
            Tesseract localTess = new Tesseract();
            localTess.setLanguage("chi_sim+eng");
            localTess.setDatapath(CaptureProperties.ocrPath);
            try {
                String result = localTess.doOCR(image);
                ScreenCaptureToolApp.LOGGER.info("OCR 识别成功，结果={}", result);
                return result;
            } catch (TesseractException e) {
                ScreenCaptureToolApp.LOGGER.error("OCR 识别失败", e);
                return "OCR Error: " + e.getMessage();
            }
        }, executor).thenAccept(res -> {
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
        }).exceptionally(e -> {
            ScreenCaptureToolApp.LOGGER.error("OCR 任务异常", e);
            return null;
        });
    }

    private void initToolManager() {
        tools.getItems().clear();
        toolbarManager.addAll(upload, createEmpty, save, copy, undo, redo, reset, clear);
        toolbarManager.addAll(colorPicker, strokeSlider, processType, process, setting, exit);
        toolbarManager.addToToolBar();
    }

    private void initPickers() throws IOException {
        ArrowPicker newArrow = new ArrowPicker(new Image(ScreenCaptureToolApp.class.getResource("assets/icon/arrow.png").toExternalForm()),
                ResourceLoader.getAsLines("picker/arrow.txt"), this);
        RectanglePicker newRect = new RectanglePicker(new Image(ScreenCaptureToolApp.class.getResource("assets/icon/rect.png").toExternalForm()),
                ResourceLoader.getAsLines("picker/rect.txt"), this);
        OvalPicker newOval = new OvalPicker(new Image(ScreenCaptureToolApp.class.getResource("assets/icon/oval.png").toExternalForm()),
                ResourceLoader.getAsLines("picker/oval.txt"), this);
        LinePicker newLine = new LinePicker(new Image(ScreenCaptureToolApp.class.getResource("assets/icon/line.png").toExternalForm()),
                ResourceLoader.getAsLines("picker/line.txt"), this);
        OCRPicker newOcr = new OCRPicker(new Image(ScreenCaptureToolApp.class.getResource("assets/icon/ocr.png").toExternalForm()),
                ResourceLoader.getAsLines("picker/ocr.txt"), this);
        replaceSidebarNode(arrow, newArrow);   arrow = newArrow;
        replaceSidebarNode(rect, newRect);     rect = newRect;
        replaceSidebarNode(oval, newOval);     oval = newOval;
        replaceSidebarNode(line, newLine);     line = newLine;
        replaceSidebarNode(ocr, newOcr);       ocr = newOcr;
        initToolManager();
    }

    private void replaceSidebarNode(javafx.scene.Node oldNode, javafx.scene.Node newNode) {
        int index = sideBar.getChildren().indexOf(oldNode);
        if (index >= 0) {
            sideBar.getChildren().set(index, newNode);
        }
    }

    public void addExternalText(DrawableText text) {
        //TODO
        long tick = new Timestamp(System.currentTimeMillis()).getTime();
        DrawRecords record = new DrawRecords(DrawTypes.EXTERNAL_TEXT, text, "init", tick);
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
            DrawRecords record = new DrawRecords(DrawTypes.EXTERNAL_IMAGE, drawableImage, "init", tick);
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

    /**
     *
     * @param gc 显示图片本身的canvas
     * @param renderSelectedImg 是否渲染选中图片
     * @param renderSelectedText 是否渲染选中文本
     * @param shouldClear 是否先清除在绘制
     */
    private void repaintCanvas(GraphicsContext gc, boolean renderSelectedImg, boolean renderSelectedText, boolean shouldClear) {
        if (shouldClear) {
            gc.clearRect(0, 0, editArea.getWidth(), editArea.getHeight());
        }
        initRepaintStack(allRecordStack);
        for (int i = 0; i < repaintStack.size(); i++) {
            DrawRecords record = repaintStack.get(i);
            if (record.getType()==DrawTypes.EXTERNAL_IMAGE && selectedImage != null && selectedImage.getImage().equals(record.getImage())) {
                if (renderSelectedImg) {
                    record.draw(gc, animator.getGraphicsContext2D());
                }
            } else if (record.getType()==DrawTypes.EXTERNAL_TEXT && selectedText != null && selectedText.getValue().equals(record.getText())) {
                if (renderSelectedText) {
                    record.draw(gc, animator.getGraphicsContext2D());
                }
            } else {
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
            if (record.getType()==DrawTypes.EXTERNAL_IMAGE) {
                record.setShouldRepaint(true);
            }
            if (record.getType()==DrawTypes.EXTERNAL_TEXT) {
                record.setShouldRepaint(true);
            }
        }
    }

    private void onKeyReleased(KeyEvent e) {
        if (textPane != null && !textPane.shouldAddTextField()) {
            return;
        }
        type = oriType;
        backToOriginMode();
        clearCanvas(animator);
        if (e.getCode() == KeyCode.SHIFT) {
            animator.setCursor(Cursor.DEFAULT);
        }
        if (e.getCode() == KeyCode.ALT) {
            animator.setCursor(Cursor.DEFAULT);
        }
    }

    private void onKeyPressed(KeyEvent e) {
        oriType = type;
        if (e.getCode() == KeyCode.ESCAPE) {
            copy.fire();
            parent.close();
        }
        if (e.getCode() == KeyCode.SHIFT ) {//textPane == null ||    && textPane.shouldAddTextField()
            rubberMode();
        }
        if (e.getCode() == KeyCode.ALT) {//&&!moving
            dragMode();
            //what is this
            if (textPane != null && textPane.shouldAddTextField()) {
            }
        }
        if (e.getCode() == KeyCode.S && e.isControlDown() && CaptureProperties.export) {
            save.fire();
        }
        if (e.getCode() == KeyCode.C && e.isControlDown() && CaptureProperties.copy) {
            copy.fire();
        }
        if (e.getCode() == KeyCode.Z && e.isControlDown() && CaptureProperties.undo) {
            undo.fire();
        }
        if (e.getCode() == KeyCode.Y && e.isControlDown() && CaptureProperties.undo) {
            redo.fire();
        }
        if (e.getCode() == KeyCode.R && e.isControlDown() && CaptureProperties.reset) {
            reset.fire();
        }
        if (e.getCode() == KeyCode.V && e.isControlDown() && CaptureProperties.paste) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            Image image = clipboard.getImage();
            if(image != null) {
                setCapture(image,false);
            }else{
                for (File file : clipboard.getFiles()) {
                    if(file.getName().endsWith(".png")||file.getName().endsWith(".jpg")) {
                        try {
                            BufferedImage read = ImageIO.read(file);
                            setCapture(ImageFormatHandler.toFXImage(read),false);
                            break;
                        } catch (IOException ex) {
                            ScreenCaptureToolApp.LOGGER.error("读取剪切板图片内容失败,",ex);
                            throw new RuntimeException(ex);
                        }
                    }
                }

            }
        }
    }

    private void deleteExternalContent(ActionEvent e) {
        if (selectedImage != null) {
            imageHandler.removeExternalImage(selectedImage);
            clearCanvas(animator);
            //TODO check
            long tick = new Timestamp(System.currentTimeMillis()).getTime();
            allRecordStack.push(new DrawRecords(DrawTypes.EXTERNAL_IMAGE, selectedImage, "delete", tick));
            editHistory.add("删除图片(" + allRecordStack.size() + ")");
//                        editRecordImageRender(false,selectedImage);
            repaintCanvas(editArea.getGraphicsContext2D(), false, false, true);
            imageHandler.drawAllImages(editArea.getGraphicsContext2D());
            selectedImage = null;
        } else if (selectedText != null) {
            textHandler.removeExternalText(selectedText);
            clearCanvas(animator);
            //TODO check
            long tick = new Timestamp(System.currentTimeMillis()).getTime();
            allRecordStack.push(new DrawRecords(DrawTypes.EXTERNAL_TEXT, selectedText, "delete", tick));
            editHistory.add("删除图片(" + allRecordStack.size() + ")");
//                        editRecordImageRender(false,selectedImage);
            repaintCanvas(editArea.getGraphicsContext2D(), false, false, true);
            textHandler.drawAllTexts(editArea.getGraphicsContext2D());
            selectedText = null;
        }
    }

    private void adjustCanvasScale(ScrollEvent event) {
        double zoomFactor = event.getDeltaY() > 0 ? 1.1 : 1 / 1.1;
        if (CaptureProperties.scaleOnMouse) {
            Point2D mouseInParent = canvasGroup.localToParent(
                    canvasGroup.sceneToLocal(event.getSceneX(), event.getSceneY()));
            canvasGroup.setScaleX(canvasGroup.getScaleX() * zoomFactor);
            canvasGroup.setScaleY(canvasGroup.getScaleY() * zoomFactor);
            Point2D mouseInParentAfter = canvasGroup.localToParent(
                    canvasGroup.sceneToLocal(event.getSceneX(), event.getSceneY()));
            canvasGroup.setTranslateX(canvasGroup.getTranslateX() + mouseInParent.getX() - mouseInParentAfter.getX());
            canvasGroup.setTranslateY(canvasGroup.getTranslateY() + mouseInParent.getY() - mouseInParentAfter.getY());
        } else {
            canvasGroup.setScaleX(canvasGroup.getScaleX() * zoomFactor);
            canvasGroup.setScaleY(canvasGroup.getScaleY() * zoomFactor);
        }
    }

    private void initRepaintStack(Stack<DrawRecords> recordStack) {
        repaintStack.clear();
        for (DrawRecords record : recordStack) {
            if (record.getType()==DrawTypes.EXTERNAL_IMAGE) {
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
            } else if (record.getType()==DrawTypes.EXTERNAL_TEXT) {
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
        parameters.setTransform(Transform.scale(ScreenCaptureUtil.SCALE,ScreenCaptureUtil.SCALE));  // 扩大图像
        WritableImage image=new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        canvas.snapshot(parameters, image);
        return image;
    }

    // 将 ImageView 中的图片绘制到 Canvas 上
    private void drawImageToCanvas(Image image) {
        // 获取 canvas 的 GraphicsContext
        GraphicsContext gc = canvas.getGraphicsContext2D();
        clearCanvas(canvas);
        clearCanvas(editArea);
        gc.setImageSmoothing(true);
        // 在 canvas 上绘制图像
        gc.drawImage(image, 0, 0, capture.getFitWidth(), capture.getFitHeight());
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
        canvasGroup.setScaleX(1);
        canvasGroup.setScaleY(1);
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setTransform(Transform.scale(ScreenCaptureUtil.SCALE, ScreenCaptureUtil.SCALE));
        repaintCanvas(canvas.getGraphicsContext2D(), true, true, false);
        WritableImage writableImage = new WritableImage((int) (canvas.getWidth() * ScreenCaptureUtil.SCALE),
                (int) (canvas.getHeight() * ScreenCaptureUtil.SCALE));
        canvas.snapshot(parameters, writableImage);
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);

        if (!CaptureProperties.captureSavePath.isBlank()) {
            File dir = new File(CaptureProperties.captureSavePath);
            dir.mkdirs();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
            File file = new File(dir, "capture_" + timestamp + ".png");
            try {
                boolean flag = ImageIO.write(bufferedImage, "png", file);
                AlertHelper.showAutoClosedPopup(flag ? "保存成功！" : "保存失败！！！", 1, parent.getX() + 100, parent.getY() + parent.getHeight() + 20);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
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
                CaptureProperties.updateSelectPath(file.getParent());
            }
        }
        drawImageToCanvas(originalImage);
        state.setText("图片已保存");
    }

    @FXML
    public void copy(){
        canvasGroup.setScaleX(1);
        canvasGroup.setScaleY(1);
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setTransform(Transform.scale(ScreenCaptureUtil.SCALE,ScreenCaptureUtil.SCALE));  // 扩大图像
        repaintCanvas(canvas.getGraphicsContext2D(), true, true, false);
        WritableImage writableImage = new WritableImage((int) (canvas.getWidth()*ScreenCaptureUtil.SCALE),
                (int) (canvas.getHeight()*ScreenCaptureUtil.SCALE));
        canvas.snapshot(parameters, writableImage);
        // 复制到剪贴板
        FileHandler.copyImage(writableImage);
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
            if(popRecord.getType()==DrawTypes.CROP){
                Image image = popRecord.getCropImage().beforeCrop();
                setCapture(image,ScreenCaptureUtil.shouldScale(image));
            }
            if (popRecord.getType()==DrawTypes.EXTERNAL_IMAGE) {
                if ("init".equals(popRecord.getDetailInfo())) {
                    //如果是最后一个相关记录，则设置为伪移除
                    popRecord.getDrawableImage().setUndo(true);
                }
            } else if (popRecord.getType()==DrawTypes.EXTERNAL_TEXT) {
                if ("init".equals(popRecord.getDetailInfo())) {
                    //如果是最后一个相关记录，则设置为伪移除
                    popRecord.getDrawableText().setUndo(true);
                }
            }
            undoStack.push(popRecord);
            //如果不是最后一个，则修改为上一个的坐标
            if (!allRecordStack.isEmpty()) {
                DrawRecords lastRecord = allRecordStack.getLast();
                if (lastRecord.getType()==DrawTypes.EXTERNAL_IMAGE) {
                    selectedImage = lastRecord.getDrawableImage();
                    adjustSelectedImagePos(lastRecord);
                } else if (lastRecord.getType()==DrawTypes.EXTERNAL_TEXT) {
                    selectedText = lastRecord.getDrawableText();
                    adjustSelectedTextPos(lastRecord);
                } else {

                    selectedImage = null;
                    selectedText = null;
                }
            }

            clearCanvas(animator);
            repaintCanvas(editArea.getGraphicsContext2D(), true, true, true);
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
            if(popRecord.getType()==DrawTypes.CROP){
                try {
                    ImageIO.write(ImageFormatHandler.toBufferedImage(popRecord.getCropImage().beforeCrop()),
                            "png",new File("E:/before.png"));
                    ImageIO.write(ImageFormatHandler.toBufferedImage(popRecord.getCropImage().afterCrop()),
                            "png",new File("E:/aftr.png"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Image image = popRecord.getCropImage().afterCrop();
                setCapture(image,ScreenCaptureUtil.shouldScale(image));
            }
            if (popRecord.getType()==DrawTypes.EXTERNAL_IMAGE) {
                if ("init".equals(popRecord.getDetailInfo())) {
                    popRecord.getDrawableImage().setUndo(false);
                }
            } else if (popRecord.getType()==DrawTypes.EXTERNAL_TEXT) {
                if ("init".equals(popRecord.getDetailInfo())) {
                    popRecord.getDrawableText().setUndo(false);
                }
            }
            if (!undoStack.isEmpty()) {
                DrawRecords lastRecord = undoStack.getLast();
                if (popRecord.getType()==DrawTypes.EXTERNAL_IMAGE) {
                    selectedImage = lastRecord.getDrawableImage();
                    if ("init".equals(popRecord.getDetailInfo())) {
                        selectedImage.setUndo(false);
                    }
                    adjustSelectedImagePos(lastRecord);
                } else if (popRecord.getType()==DrawTypes.EXTERNAL_TEXT) {
                    selectedText = lastRecord.getDrawableText();
                    adjustSelectedTextPos(lastRecord);
                } else {
                    selectedImage = null;
                    selectedText = null;
                }
            }
            allRecordStack.push(popRecord);
            clearCanvas(animator);
            repaintCanvas(editArea.getGraphicsContext2D(), true, true, true);

        }
    }

    @FXML
    private void resetImage() {
        canvasGroup.setScaleX(1);
        canvasGroup.setScaleY(1);
        canvasGroup.setTranslateX(0);
        canvasGroup.setTranslateY(0);
        drawImageToCanvas(capture.getImage());
        clearCanvas(animator);
        clearCanvas(editArea);
        imageHandler.clearAll();
        textHandler.clearAll();
    }


    public void repaintUploadCanvas(Image image) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setImageSmoothing(true);
        // 在 canvas 上绘制图像
        clearCanvas(canvas);
        clearCanvas(editArea);
        gc.drawImage(image, 0, 0, canvas.getWidth(), canvas.getHeight());
        canvas.requestFocus();
        clearCanvas(animator);
    }

    public void relocateStage(){
        parent.centerOnScreen();
    }

    private void resizeStage() {
        if (parent != null) {
            tools.applyCss();
            tools.layout();
            double toolbarW = tools.prefWidth(-1);
            double sidebarW = sideBar != null ? sideBar.getPrefWidth() : 0;
            parent.setWidth(Math.max(capture.getFitWidth() + sidebarW + 30, toolbarW + 20));
            parent.setHeight(capture.getFitHeight() + 100);
            canvas.setWidth(capture.getFitWidth());
            canvas.setHeight(capture.getFitHeight());
            editArea.setWidth(capture.getFitWidth());
            editArea.setHeight(capture.getFitHeight());
            animator.setWidth(capture.getFitWidth());
            animator.setHeight(capture.getFitHeight());
        }
    }

    public void setCapture(Image image, boolean shouldScale) {
        this.capture = new ImageView(image);
        capture.setPreserveRatio(true);

        double fitWidth = image.getWidth() / ScreenCaptureUtil.SCALE;
        double fitHeight = image.getHeight() / ScreenCaptureUtil.SCALE;

        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        double maxW = screen.getWidth() * 0.85;
        double maxH = screen.getHeight() * 0.85 - 100;

        if (fitWidth > maxW || fitHeight > maxH) {
            double scale = Math.min(maxW / fitWidth, maxH / fitHeight);
            fitWidth *= scale;
            fitHeight *= scale;
        } else if (Math.max(fitWidth, fitHeight) < 400) {
            double scale = 400.0 / Math.max(fitWidth, fitHeight);
            fitWidth *= scale;
            fitHeight *= scale;
        }

        capture.setFitWidth(fitWidth);
        capture.setFitHeight(fitHeight);

        if (capture.getImage() != null) {
            drawImageToCanvas(capture.getImage());
        }
        resizeStage();
    }

    @FXML
    private void processImage() {
        WritableImage image = getImageWithRecords();
        FileHandler.getScheduledTask().execute(()->{
            try {
                WritableImage result;
                if ("灰化".equals(processType.getValue())) {
                    result=ImageProcessor.toGray(image);
                } else if ("拉普拉斯锐化".equals(processType.getValue())) {
                    result=ImageProcessor.laplacian(image);
                } else if ("边缘提取".equals(processType.getValue())) {
                    result=ImageProcessor.edgeDetect(image);
                } else if ("垂直/水平边缘提取".equals(processType.getValue())) {
                    result=ImageProcessor.vhDetect(image);
                } else if ("均值平滑".equals(processType.getValue())) {
                    result=ImageProcessor.averageSmooth(image);
                } else if ("高斯平滑".equals(processType.getValue())) {
                    result=ImageProcessor.gaussianSmooth(image);
                }else if ("中值平滑".equals(processType.getValue())) {
                    result=ImageProcessor.medianSmooth(image);
                } else if ("反转".equals(processType.getValue())) {
                    result=ImageProcessor.invertColor(image);
                } else if ("对比度增强".equals(processType.getValue())) {
                    result=ImageProcessor.enhanceContrast(image);
                } else if ("直方图均衡化".equals(processType.getValue())) {
                    result=ImageProcessor.grayContrast(image);
                }else if ("同态滤波".equals(processType.getValue())) {
                    result=ImageProcessor.applyHomomorphicFilter(image);
                }else if ("区域生长分割".equals(processType.getValue())) {
                    result=ImageProcessor.regionGrowing(image);
                }else if ("前景分割".equals(processType.getValue())) {
                    result=ImageProcessor.grabCut(image);
                } else {
                    result = null;
                }
                Platform.runLater(()->{
                    if(result!=null){
                        drawImageToCanvas(result);
                    }
                });
            }catch (Exception e){
                ScreenCaptureToolApp.LOGGER.error("errors" ,e);
            }
        });
    }

    public void clearAllRecord() {
        clearCanvas(editArea);
        capture.setImage(originalImage);
//        resizeStage();
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
            if (re.getType()==DrawTypes.EXTERNAL_IMAGE) {
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
        if (AlertHelper.showConfirmAlert("Exit confirm", "Are you sure you want to exit now?",
                "Click OK to exit, or Cancel to continue.")) {
            Platform.exit();
            System.exit(0);
        }
    }

    public void adjustScale() {
        Image image = capture.getImage();
        if (image == null) return;
        double fitWidth = image.getWidth() / ScreenCaptureUtil.SCALE;
        double fitHeight = image.getHeight() / ScreenCaptureUtil.SCALE;
        capture.setFitWidth(fitWidth);
        capture.setFitHeight(fitHeight);
        canvasGroup.setScaleX(1); canvasGroup.setScaleY(1);
        canvasGroup.setTranslateX(0); canvasGroup.setTranslateY(0);
        drawImageToCanvas(image);
        resizeStage();
        if (parent != null) {
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            double x = bounds.getMinX() + (bounds.getWidth() - parent.getWidth()) / 2;
            double y = bounds.getMinY() + (bounds.getHeight() - parent.getHeight()) / 2;
            parent.setX(Math.max(bounds.getMinX(), x));
            parent.setY(Math.max(bounds.getMinY(), y));
        }
    }

    public void test1() throws IOException {
//        ScreenCaptureToolApp.LOGGER.info("===========  全部编辑记录 ===========");
//        for (DrawRecords r : allRecordStack) {
//            ScreenCaptureToolApp.LOGGER.info(r.toString());
//        }
//        ScreenCaptureToolApp.LOGGER.info("-------  undo 记录 ---------");
//        for (DrawRecords r : undoStack) {
//            ScreenCaptureToolApp.LOGGER.info(r.toString());
//        }
//        ScreenCaptureToolApp.LOGGER.info("==========================");
        ScreenCaptureToolApp.LOGGER.info("stage width {}",parent.getWidth());
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
            case 29 -> tailorMode();
            default -> setCursorShape(Cursor.CROSSHAIR);
        }
    }

    @FXML
    private void createEmptyImage() {
        //TODO create new empty image, open a chooser dialog
        Image emptyImage = new Image(Objects.requireNonNull(ScreenCaptureToolApp.class.getResourceAsStream("assets/transparent.png")));
        setCapture(emptyImage,false);
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
        editArea.getGraphicsContext2D().setLineDashes(0);
    }

    @FXML
    private void tailorMode() {
        type = 29;
        setCursorShape(Cursor.CROSSHAIR);
        editArea.getGraphicsContext2D().setLineDashes(0);
    }

    @FXML
    public void rubberMode() {
        // 处理橡皮模式
        type = -1;
//        Image cursorImage = new Image(ScreenCaptureToolApp.class.getResource("assets/icon/rubber.png").toExternalForm()); // 替换为你自己的图像路径
//        ImageCursor customCursor = new ImageCursor(cursorImage, 0, 32); // (16,16) 是热点位置（图像的中心）
//        setCursorShape(customCursor);
        animator.getGraphicsContext2D().setStroke(Color.GREEN);
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

    public double getToolBarHeight() {
        return this.tools.getHeight();
    }



    public Color getStrokeColor() {
        return this.forecolor;
    }

    public GraphicsContext getGraphicsContext() {
        return editArea.getGraphicsContext2D();
    }

    public void setFont(Font font) {
        this.font = font;
        setGraphicsContextFont(font);
    }

    public Font getFont() {
        return this.font == null ? new Font("Arial", 16) : font;
    }

    private void setGraphicsContextFont() {
        editArea.getGraphicsContext2D().setFont(font);
        editArea.getGraphicsContext2D().setLineDashes(0);
        animator.getGraphicsContext2D().setFont(font);
        animator.getGraphicsContext2D().setLineDashes(0);
    }

    public void setGraphicsContextFont(Font font) {
        editArea.getGraphicsContext2D().setFont(font);
        animator.getGraphicsContext2D().setFont(font);
    }

    public BufferedImage getOriginalImage() {
        return SwingFXUtils.fromFXImage(originalImage, null);
    }

    public void initialExecutor() {
        if (this.executor != null && !this.executor.isShutdown()) {
            this.executor.shutdown();
        }
        this.executor = Executors.newFixedThreadPool(4);
    }

    public void setOriginalImage(Image originalImage) {
        this.originalImage = new WritableImage(originalImage.getPixelReader(),
                (int) originalImage.getWidth(), (int) originalImage.getHeight());
    }

    public WritableImage getImageWithRecords(){
        repaintCanvas(canvas.getGraphicsContext2D(), true, true, false);
        SnapshotParameters params = new SnapshotParameters();
        params.setTransform(Transform.scale(ScreenCaptureUtil.SCALE, ScreenCaptureUtil.SCALE));
        WritableImage snapshotImage = new WritableImage((int)(canvas.getWidth() * ScreenCaptureUtil.SCALE),
                (int)(canvas.getHeight() * ScreenCaptureUtil.SCALE));
        canvas.snapshot(params, snapshotImage);
        return snapshotImage;
    }
}
