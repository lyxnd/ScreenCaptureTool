package net.jackchuan.screencapturetool.external.stage;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Screen;
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

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;


/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/12/20 16:10
 */
public class OverlayStage extends Stage {

    /** 最小化的 Dwmapi 绑定，用于获取去除透明阴影后的真实窗口边界 */
    private interface DwmApi extends com.sun.jna.Library {
        DwmApi INSTANCE = Native.load("dwmapi", DwmApi.class, W32APIOptions.DEFAULT_OPTIONS);
        int DwmGetWindowAttribute(WinDef.HWND hwnd, int dwAttribute, Pointer pvAttribute, int cbAttribute);
    }

    private Scene scene;
    private StackPane stackPane;
    private Image image;
    private Canvas canvas;
    private GraphicsContext gc;
    private Stage displayStage;
    private double startX, startY, endX, endY;
    private double autoStartX, autoStartY, autoEndX, autoEndY;
    private boolean hasAutoRect = false;
    private volatile int[] hoveredWindowRect = null;
    private volatile List<int[]> cachedEdgeRects = null;
    private volatile boolean detectingEdge = false;
    private long lastDetectTime = 0;
    private ContextMenu popMenu;
    private MenuItem fullCut, test, test1, captureWindow;
    private Pair<Integer, Integer> size;

    public OverlayStage(Image image) {
        init(image);
    }

    private void init(Image image) {
        this.image = image;
        size = ScreenCaptureUtil.getScreenSize(ScreenCaptureUtil.DEFAULT_SIZE);
        canvas = new Canvas(size.getKey(), size.getValue());
        gc = canvas.getGraphicsContext2D();
        stackPane = new StackPane(canvas);
        scene = new Scene(stackPane, size.getKey(), size.getValue());
        popMenu = new ContextMenu();
        fullCut = new MenuItem("全屏选择");
        test = new MenuItem("save shot");
        test1 = new MenuItem("detect rect");
        stackPane.setBackground(Background.EMPTY);
        scene.setFill(null);
        canvas.setCursor(Cursor.CROSSHAIR);
        gc.setFill(Color.rgb(255, 255, 255, 0.1));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        captureWindow = new MenuItem("截取此窗口");
        popMenu.getItems().addAll(fullCut, captureWindow, test, test1);
        initialAction();
        this.setScene(scene);
        this.initStyle(StageStyle.TRANSPARENT);
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        this.setX(screenBounds.getMinX());
        this.setY(screenBounds.getMinY());
        this.setWidth(screenBounds.getWidth());
        this.setHeight(screenBounds.getHeight());
        this.getIcons().add(new Image(ScreenCaptureToolApp
                .class.getResource("assets/icon/shortcut.png").toExternalForm()));
        this.setAlwaysOnTop(true);
    }

    private void initialAction() {
        canvas.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                closeOverlayStage();
                captureAndShowScreenshot(0, 0, size.getKey(), size.getValue(), true);
            }
        });
        fullCut.setOnAction(e -> {
            closeOverlayStage();
            captureAndShowScreenshot(0, 0, size.getKey(), size.getValue(), true);
        });
        test.setOnAction(e -> {
            closeOverlayStage();
            Image snapshot = captureAndShowScreenshot(0, 0, size.getKey(), size.getValue(), false);
            BufferedImage image = ImageFormatHandler.toBufferedImage(snapshot);
            try {
                ImageIO.write(image, "png", new File("F:/java_practise/ScreenCaptureTool/temp/test.png"));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        test1.setOnAction(e -> {

        });
        captureWindow.setOnAction(e -> {
            if (hoveredWindowRect != null) {
                int[] r = hoveredWindowRect;
                closeOverlayStage();
                Image img = captureAndShowScreenshot(r[0], r[1], r[2], r[3], true);
                if (CaptureProperties.autoCopy && img != null) {
                    Toolkit.getDefaultToolkit().getSystemClipboard()
                            .setContents(new TransferableImage(img), null);
                }
            }
        });
        canvas.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                // 右键时刷新一次窗口检测，确保菜单项准确
                handleSelectArea(e.getX(), e.getY());
                popMenu.show(this, e.getScreenX(), e.getScreenY());
            }
            startX = e.getX();
            startY = e.getY();
        });
        canvas.setOnMouseMoved(e -> handleSelectArea(e.getX(), e.getY()));
        canvas.setOnMouseDragged(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                endX = e.getX();
                endY = e.getY();
                drawRectAndDotInfo(gc, startX, startY, endX, endY, canvas.getWidth(), canvas.getHeight());
            }
        });
        canvas.setOnMouseReleased(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                if (!isDoubleClick()) {
                    // 有效拖拽 → 手动选区截图
                    closeOverlayStage();
                    Image img = captureAndShowScreenshot(
                            (int) Math.min(startX, endX), (int) Math.min(startY, endY),
                            (int) Math.abs(endX - startX), (int) Math.abs(endY - startY), true);
                    if (CaptureProperties.autoCopy && img != null) {
                        Toolkit.getDefaultToolkit().getSystemClipboard()
                                .setContents(new TransferableImage(img), null);
                    }
                } else if (hasAutoRect && hoveredWindowRect != null) {
                    // 单击 → 截取高亮窗口
                    int[] r = hoveredWindowRect;
                    closeOverlayStage();
                    Image img = captureAndShowScreenshot(r[0], r[1], r[2], r[3], true);
                    if (CaptureProperties.autoCopy && img != null) {
                        Toolkit.getDefaultToolkit().getSystemClipboard()
                                .setContents(new TransferableImage(img), null);
                    }
                }
            }
        });
    }

    private void handleSelectArea(double mx, double my) {
        long now = System.currentTimeMillis();
        if (now - lastDetectTime < 80) return;
        lastDetectTime = now;
        if ("边缘检测".equals(CaptureProperties.detectMode)) {
            handleEdgeDetection(mx, my);
        } else {
            handleWindowDetection(mx, my);
        }
    }

    /** 窗口检测：查询鼠标下方的顶层非本进程窗口，在画布上高亮。*/
    private void handleWindowDetection(double mx, double my) {
        double scale = Screen.getPrimary().getOutputScaleX();
        javafx.geometry.Point2D screenPt = canvas.localToScreen(mx, my);
        javafx.geometry.Point2D origin   = canvas.localToScreen(0, 0);
        if (screenPt == null) return;
        int sx = (int) (screenPt.getX() * scale);
        int sy = (int) (screenPt.getY() * scale);
        double ox = origin != null ? origin.getX() : 0;
        double oy = origin != null ? origin.getY() : 0;

        CompletableFuture.supplyAsync(() -> detectWindowAt(sx, sy))
                .thenAccept(winRect -> Platform.runLater(() -> {
                    double cw = canvas.getWidth(), ch = canvas.getHeight();
                    restoreBackground(cw, ch);
                    if (winRect == null) { hasAutoRect = false; hoveredWindowRect = null; return; }
                    int lx = (int) (winRect[0] / scale);
                    int ly = (int) (winRect[1] / scale);
                    int lw = (int) (winRect[2] / scale);
                    int lh = (int) (winRect[3] / scale);
                    hoveredWindowRect = new int[]{lx, ly, lw, lh};
                    autoStartX = lx - ox;
                    autoStartY = ly - oy;
                    autoEndX   = autoStartX + lw;
                    autoEndY   = autoStartY + lh;
                    hasAutoRect = true;
                    drawHighlightRect(autoStartX, autoStartY, lw, lh, Color.rgb(0, 220, 80));
                }));
    }

    /** 边缘检测：首次调用时异步跑 OpenCV，之后用缓存结果；找鼠标位置处最小包含矩形并高亮。*/
    private void handleEdgeDetection(double mx, double my) {
        if (image == null) return;
        double cw = canvas.getWidth(), ch = canvas.getHeight();
        double scaleX = image.getWidth()  / cw;
        double scaleY = image.getHeight() / ch;
        if (cachedEdgeRects != null) {
            applyEdgeHighlight(mx, my, cachedEdgeRects, scaleX, scaleY);
            return;
        }
        if (detectingEdge) return;
        detectingEdge = true;
        Image imgSnap = this.image;
        final double fmx = mx, fmy = my;
        CompletableFuture.supplyAsync(() -> ImageDetector.detectRects(imgSnap))
                .thenAccept(rects -> Platform.runLater(() -> {
                    cachedEdgeRects = rects;
                    detectingEdge = false;
                    applyEdgeHighlight(fmx, fmy, rects, scaleX, scaleY);
                }));
    }

    /** 绘制全部检测矩形（暗色轮廓）+ 高亮最相关的一个（鼠标在内则最小，否则最近）。*/
    private void applyEdgeHighlight(double mx, double my, List<int[]> rects, double scaleX, double scaleY) {
        double cw = canvas.getWidth(), ch = canvas.getHeight();
        restoreBackground(cw, ch);
        double imgX = mx * scaleX, imgY = my * scaleY;

        // 先把所有矩形画出来作为参考（半透明蓝色细线）
        gc.setStroke(Color.rgb(80, 160, 255, 0.55));
        gc.setLineWidth(1);
        for (int[] r : rects) {
            gc.strokeRect(r[0] / scaleX, r[1] / scaleY, r[2] / scaleX, r[3] / scaleY);
        }

        // 第一优先：找鼠标在内的最小矩形
        int[] best = null;
        double bestArea = Double.MAX_VALUE;
        for (int[] r : rects) {
            if (imgX >= r[0] && imgX <= r[0] + r[2] && imgY >= r[1] && imgY <= r[1] + r[3]) {
                double area = (double) r[2] * r[3];
                if (area < bestArea) { bestArea = area; best = r; }
            }
        }
        // 第二优先（回退）：找中心距离最近的矩形
        if (best == null && !rects.isEmpty()) {
            double minDist = Double.MAX_VALUE;
            for (int[] r : rects) {
                double cx = r[0] + r[2] / 2.0, cy = r[1] + r[3] / 2.0;
                double dist = Math.hypot(imgX - cx, imgY - cy);
                if (dist < minDist) { minDist = dist; best = r; }
            }
        }

        if (best == null) { hasAutoRect = false; hoveredWindowRect = null; return; }
        double rx = best[0] / scaleX, ry = best[1] / scaleY;
        double rw = best[2] / scaleX, rh = best[3] / scaleY;
        javafx.geometry.Point2D origin = canvas.localToScreen(0, 0);
        double ox = origin != null ? origin.getX() : 0;
        double oy = origin != null ? origin.getY() : 0;
        hoveredWindowRect = new int[]{(int)(rx + ox), (int)(ry + oy), (int) rw, (int) rh};
        autoStartX = rx; autoStartY = ry; autoEndX = rx + rw; autoEndY = ry + rh;
        hasAutoRect = true;
        drawHighlightRect(rx, ry, rw, rh, Color.rgb(0, 180, 255));
    }

    /** 恢复画布背景：如有预截图则先绘图，再叠半透明遮罩。*/
    private void restoreBackground(double cw, double ch) {
        gc.clearRect(0, 0, cw, ch);
        gc.setFill(Color.rgb(255, 255, 255, 0.1));
        gc.fillRect(0, 0, cw, ch);
    }

    /** 绘制带角点的高亮矩形及尺寸标注。*/
    private void drawHighlightRect(double rx, double ry, double rw, double rh, Color color) {
        gc.setStroke(color); gc.setLineWidth(2);
        gc.strokeRect(rx, ry, rw, rh);
        gc.setFill(color);
        double cx = rx + rw / 2, cy = ry + rh / 2;
        for (double[] dot : new double[][]{{rx, ry}, {rx, ry+rh}, {rx+rw, ry}, {rx+rw, ry+rh},
                {cx, ry}, {cx, ry+rh}, {rx, cy}, {rx+rw, cy}}) {
            gc.fillRect(dot[0]-3, dot[1]-3, 6, 6);
        }
        gc.setFont(javafx.scene.text.Font.font("楷体", 12));
        gc.strokeText((int) rw + " × " + (int) rh, rx+rw+6, ry+rh+14, 120);
    }

    /**
     * 在后台线程中调用。返回屏幕坐标 [x, y, w, h]，找不到返回 null。
     * 使用 GetTopWindow + GetWindow(GW_HWNDNEXT) 按真实 Z-order 从顶层向下遍历，
     * 保证找到的是最顶层的目标窗口（EnumWindows 不保证 Z-order）。
     * 使用 DwmGetWindowAttribute(DWMWA_EXTENDED_FRAME_BOUNDS) 获取去除透明阴影后的实际可见边界。
     */
    private int[] detectWindowAt(int sx, int sy) {
        long myPid = ProcessHandle.current().pid();
        // GetDesktopWindow + GetWindow(GW_CHILD=5) 等价于 GetTopWindow(null)，JNA 标准接口已含这两个方法
        // 后接 GetWindow(GW_HWNDNEXT=2) 按真实 Z-order 从顶往下遍历，保证找到最顶层目标窗口
        WinDef.HWND desktop = User32.INSTANCE.GetDesktopWindow();
        WinDef.HWND hwnd = User32.INSTANCE.GetWindow(desktop, new WinDef.DWORD(5)); // GW_CHILD
        while (hwnd != null) {
            if (User32.INSTANCE.IsWindowVisible(hwnd)) {
                IntByReference pidRef = new IntByReference();
                User32.INSTANCE.GetWindowThreadProcessId(hwnd, pidRef);
                if ((long) pidRef.getValue() != myPid) {
                    WinDef.RECT rect = getVisibleWindowRect(hwnd);
                    int w = rect.right - rect.left;
                    int h = rect.bottom - rect.top;
                    if (w > 10 && h > 10
                            && sx >= rect.left && sx <= rect.right
                            && sy >= rect.top  && sy <= rect.bottom) {
                        return new int[]{rect.left, rect.top, w, h};
                    }
                }
            }
            hwnd = User32.INSTANCE.GetWindow(hwnd, new WinDef.DWORD(2)); // GW_HWNDNEXT
        }
        return null;
    }

    /** 优先用 DwmGetWindowAttribute(DWMWA_EXTENDED_FRAME_BOUNDS=9) 获取去除透明阴影的实际边界 */
    private WinDef.RECT getVisibleWindowRect(WinDef.HWND hwnd) {
        WinDef.RECT rect = new WinDef.RECT();
        try {
            int hr = DwmApi.INSTANCE.DwmGetWindowAttribute(hwnd, 9, rect.getPointer(), rect.size());
            if (hr == 0) { rect.read(); return rect; }
        } catch (Throwable ignored) {}
        User32.INSTANCE.GetWindowRect(hwnd, rect);
        return rect;
    }

    private Image captureAndShowScreenshot(int x, int y, int width, int height, boolean show) {
        javafx.scene.image.Image fxImage;
        try {
            // JNA截取屏幕
            BufferedImage screenshot;
            if ("javafx Robot".equals(CaptureProperties.captureType)) {
                screenshot = ScreenCaptureUtil.captureWithFX(x, y, width, height);
            } else if ("java awt Robot".equals(CaptureProperties.captureType)) {
                screenshot = ScreenCaptureUtil.captureWithAWT(x, y, width, height);
            } else if ("Python's pillow".equals(CaptureProperties.captureType)) {
                screenshot = ScreenCaptureUtil.captureWithPython(x, y, width, height);
            } else if ("JNA".equals(CaptureProperties.captureType)) {
                ScreenCaptureToolApp.LOGGER.info("jna capture , width {} , height {}", width, height);
                screenshot = ScreenCaptureUtil.captureWithJNA(x, y, width, height);
            } else {
                screenshot = ScreenCaptureUtil.captureWithAWT(x, y, width, height);
            }
            if (screenshot == null) {
                //TODO
                //alert
                return null;
            }
            // 转换为 JavaFX Image
            fxImage = ImageFormatHandler.toFXImage(screenshot);
            // 显示截图弹窗
            if (show) {
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
//        controller.setCapture(image, ScreenCaptureUtil.shouldScale(image));
        controller.setCapture(image, false);
        controller.setOriginalImage(image);
        displayStage.setScene(scene);
        displayStage.setOnShown(e -> Platform.runLater(()->{
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            double x = bounds.getMinX() + (bounds.getWidth() - displayStage.getWidth()) / 2;
            double y = bounds.getMinY() + (bounds.getHeight() - displayStage.getHeight()) / 2;
            displayStage.setX(Math.max(bounds.getMinX(), x));
            displayStage.setY(Math.max(bounds.getMinY(), y));
        }));
        displayStage.show();
    }

    private void closeOverlayStage() {
        this.close();
    }

    private boolean isDoubleClick() {
        return Math.abs(startX - endX) <= 20 || Math.abs(startY - endY) <= 20 || endX == 0 || endY == 0;
    }

    private void drawRectAndDotInfo(GraphicsContext gc, double startX, double startY, double endX, double endY, double w, double h) {
        restoreBackground(w, h);
        gc.setStroke(Color.GREEN);
        gc.strokeRect(Math.min(startX, endX), Math.min(startY, endY),
                Math.abs(endX - startX), Math.abs(endY - startY));
        gc.setFill(Color.GREEN);
        double centerX = (startX + endX) / 2;
        double centerY = (startY + endY) / 2;
        gc.fillRect(startX - 3, startY - 3, 6, 6);
        gc.fillRect(startX - 3, endY - 3, 6, 6);
        gc.fillRect(endX - 3, endY - 3, 6, 6);
        gc.fillRect(endX - 3, startY - 3, 6, 6);
        gc.fillRect(centerX - 3, startY - 3, 6, 6);
        gc.fillRect(centerX - 3, endY - 3, 6, 6);
        gc.fillRect(startX - 3, centerY - 3, 6, 6);
        gc.fillRect(endX - 3, centerY - 3, 3, 6);
        int width = (int) Math.abs(startX - endX);
        int height = (int) Math.abs(startY - endY);
        String info = "w : " + width + ", h : " + height;
        gc.setStroke(Color.GREEN);
        gc.setLineWidth(2);
        gc.setFont(new Font("楷体", 12));
        gc.strokeText(info, Math.max(startX, endX) + 10, Math.max(startY, endY) + 10, 100);
    }
}
