package net.jackchuan.screencapturetool.external.picker;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Popup;
import javafx.stage.Window;
import net.jackchuan.screencapturetool.ScreenCaptureToolApp;
import net.jackchuan.screencapturetool.controller.CaptureDisplayController;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * 功能：继承该类后可以在点击时弹出选择框，在子类中设置选项点击事件，参数为选项序号，(无需为子类设置onAction等)
 * 作者：jackchuan
 * 日期：2024/12/17 19:57
 */
public abstract class CustomPicker extends Button {
    private static final List<CustomPicker> REGISTRY = new ArrayList<>();

    private GridPane displayer;
    protected Button[] choices;
    protected Popup popup;
    protected int col,row;
    protected boolean changed=false;
    protected ArrayList<ImageView> viewList;
    protected ArrayList<String> imgPaths;
    protected GraphicsContext gc;
    protected CaptureDisplayController controller;

    public CustomPicker(){
    }
    public CustomPicker(Image icon, ArrayList<String> imgPaths,CaptureDisplayController controller){
        this.controller=controller;
        initUI(icon,imgPaths);
    }
    public CustomPicker(Image icon, ArrayList<String> imgPaths,GraphicsContext gc){
        this.gc=gc;
        initUI(icon,imgPaths);
    }

    public CustomPicker(String folder,CaptureDisplayController controller) throws URISyntaxException {
        this.controller=controller;
        initUI(folder);
    }

    private static void hideAllPopups() {
        REGISTRY.forEach(p -> { if (p.popup != null) p.popup.hide(); });
    }

    public static void hideAll() {
        hideAllPopups();
    }

    private void register() {
        REGISTRY.add(this);
    }

    private String[] lazyPaths; // 资源路径，JAR 和文件系统均适用
    private int loadedCount = 0;
    private boolean loading = false;
    private static final int SCROLL_BATCH = 150;
    private static final int SCROLL_COLS = 10;

    private void initUI(String folder) {
        popup = new Popup();
        popup.setAutoHide(true);
        displayer = new GridPane();
        viewList = new ArrayList<>();
        lazyPaths = listResources(folder);
        ScrollPane scrollPane = new ScrollPane(displayer);
        scrollPane.setPrefHeight(300);
        scrollPane.setPrefWidth(380);
        popup.setWidth(380);
        scrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() >= 0.85 && !loading
                    && lazyPaths != null && loadedCount < lazyPaths.length) {
                loadNextBatch(scrollPane);
            }
        });
        popup.getContent().add(scrollPane);
        this.setOnAction(e -> popup(this.getScene().getWindow()));
        register();
    }

    /**
     * 枚举 folder 下的所有文件资源路径，兼容三种场景：
     *   file: — IDE 开发运行（resources 在文件系统）
     *   jar:  — 非模块化 fat-jar 或 jpackage 独立 JAR
     *   jrt:  — 模块化应用经 jlink 链接进 runtime image
     *
     * 注意：jlink 模块化打包时 getResource("directory") 对目录返回 null，
     * 需要直接走 JRT 文件系统（/modules/{module}/{packagePath}/{folder}）枚举。
     */
    private static String[] listResources(String folder) {
        List<String> result = new ArrayList<>();
        URL url = ScreenCaptureToolApp.class.getResource(folder);
        if (url != null) {
            try {
                URI uri = url.toURI();
                String scheme = uri.getScheme();
                if ("file".equals(scheme)) {
                    File[] files = new File(uri).listFiles();
                    if (files != null) {
                        for (File f : files) {
                            if (f.isFile()) result.add(folder + "/" + f.getName());
                        }
                    }
                    return result.toArray(new String[0]);
                } else if ("jar".equals(scheme)) {
                    String ssp = uri.getRawSchemeSpecificPart();
                    String[] parts = ssp.split("!", 2);
                    File jarFile = new File(new URI(parts[0]));
                    String innerFolder = parts[1].substring(1);
                    if (!innerFolder.endsWith("/")) innerFolder += "/";
                    try (JarFile jf = new JarFile(jarFile)) {
                        Enumeration<JarEntry> entries = jf.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            if (name.startsWith(innerFolder) && !entry.isDirectory()) {
                                result.add(folder + "/" + name.substring(name.lastIndexOf('/') + 1));
                            }
                        }
                    }
                    return result.toArray(new String[0]);
                } else if ("jrt".equals(scheme)) {
                    Path folderPath = Path.of(uri);
                    try (Stream<Path> stream = Files.list(folderPath)) {
                        stream.filter(p -> !Files.isDirectory(p))
                              .map(p -> folder + "/" + p.getFileName().toString())
                              .forEach(result::add);
                    }
                    return result.toArray(new String[0]);
                }
            } catch (Exception ignored) {}
        }

        // jlink 模块化打包：getResource() 对目录返回 null，通过 JRT 文件系统直接枚举
        String moduleName = ScreenCaptureToolApp.class.getModule().getName();
        ScreenCaptureToolApp.LOGGER.info("listResources fallback jrt, module={}", moduleName);
        if (moduleName != null) {
            try {
                String packagePath = ScreenCaptureToolApp.class.getPackageName().replace('.', '/');
                FileSystem jrtFs = FileSystems.getFileSystem(URI.create("jrt:/"));
                // JRT 路径格式：/modules/{moduleName}/{packagePath}/{folder}
                String jrtPath = "/modules/" + moduleName + "/" + packagePath + "/" + folder;
                Path folderPath = jrtFs.getPath(jrtPath);
                try (Stream<Path> stream = Files.list(folderPath)) {
                    stream.filter(p -> !Files.isDirectory(p))
                          .map(p -> folder + "/" + p.getFileName().toString())
                          .forEach(result::add);
                }
            } catch (Exception e) {
                ScreenCaptureToolApp.LOGGER.error("listResources jrt fallback failed", e);
            }
        }
        return result.toArray(new String[0]);
    }

    private void loadNextBatch(ScrollPane scrollPane) {
        if (loading || lazyPaths == null || loadedCount >= lazyPaths.length) return;
        loading = true;
        int start = loadedCount;
        int end = Math.min(start + SCROLL_BATCH, lazyPaths.length);
        for (int i = start; i < end; i++) {
            // 用 getResourceAsStream 加载，兼容 jrt:/、jar:、file: 全部协议
            InputStream stream = ScreenCaptureToolApp.class.getResourceAsStream(lazyPaths[i]);
            if (stream == null) continue;
            Button btn = new Button();
            ImageView imageView = new ImageView(new Image(stream));
            imageView.setFitWidth(20);
            imageView.setFitHeight(20);
            btn.setGraphic(imageView);
            int finalI = i;
            btn.setOnAction(e -> { popup.hide(); onClicked(gc, finalI); });
            displayer.add(btn, i % SCROLL_COLS, i / SCROLL_COLS);
        }
        loadedCount = end;
        Platform.runLater(() -> loading = false);
    }

    protected String getResourcePath(int index) {
        if (lazyPaths != null && index >= 0 && index < lazyPaths.length) return lazyPaths[index];
        return null;
    }

    public void initUI(){
        popup=new Popup();
        popup.setAutoHide(true);
        displayer=new GridPane();
        popup.getContent().add(displayer);
        this.setOnAction(e -> popup(this.getScene().getWindow()));
        register();
    }

    private void initUI(Image icon, Image[] imgs) {
        popup=new Popup();
        popup.setAutoHide(true);
        ImageView iconView =new ImageView(icon);
        iconView.setFitWidth(20);  // 设置图标的宽度
        iconView.setFitHeight(20); // 设置图标的高度
        this.setGraphic(iconView);
        displayer=new GridPane();
        viewList=new ArrayList<>();
        int n = (int) Math.ceil(Math.sqrt(imgs.length));
        choices=new Button[imgs.length];
        for (int i = 0; i < imgs.length; i++) {
            choices[i] = new Button();
            // 创建图像视图
            ImageView imageView = new ImageView(imgs[i]);
            imageView.setFitWidth(20);  // 设置图标的宽度
            imageView.setFitHeight(20); // 设置图标的高度
            choices[i].setGraphic(imageView);
            int r = i / n;  // 计算行
            int c = i % n;  // 计算列
            imageView.setUserData(new int[]{r,c});
            displayer.add(choices[i], r, c);
        }
        this.setOnAction(e->{
            popup(this.getScene().getWindow());
        });
        popup.getContent().add(displayer);
        register();
    }
    private void initUI(Image icon, ArrayList<String> imgPaths) {
        popup=new Popup();
        popup.setAutoHide(true);
        ImageView iconView =new ImageView(icon);
        iconView.setFitWidth(20);  // 设置图标的宽度
        iconView.setFitHeight(20); // 设置图标的高度
        this.setGraphic(iconView);
        displayer=new GridPane();
        viewList=new ArrayList<>();
        int n = (int) Math.ceil(Math.sqrt(imgPaths.size()));
        choices=new Button[imgPaths.size()];
        for (int i = 0; i < imgPaths.size(); i++) {
            choices[i] = new Button();
            // 创建图像视图
            String path = ScreenCaptureToolApp.class.getResource(imgPaths.get(i)).toExternalForm();
            if(path==null||path.isBlank()){
                continue;
            }
            ImageView imageView = new ImageView(path);
            imageView.setFitWidth(20);  // 设置图标的宽度
            imageView.setFitHeight(20); // 设置图标的高度
            choices[i].setGraphic(imageView);
            int r = i / n;  // 计算行
            int c = i % n;  // 计算列
            imageView.setUserData(imgPaths.get(i));
            int finalI = i;
            choices[i].setOnAction(e->{
                popup.hide();
                onClicked(gc,finalI);
            });
            displayer.add(choices[i], r, c);
        }
        this.setOnAction(e->{
            popup(this.getScene().getWindow());
        });
        popup.getContent().add(displayer);
        register();
    }

    private void initUI(String text, ArrayList<String> imgPaths) {
        popup=new Popup();
        popup.setAutoHide(true);
        this.setText(text);
        displayer=new GridPane();
        viewList=new ArrayList<>();
        int n = (int) Math.ceil(Math.sqrt(imgPaths.size()));
        choices=new Button[imgPaths.size()];
        for (int i = 0; i < imgPaths.size(); i++) {
            choices[i] = new Button();
            // 创建图像视图
            ImageView imageView = new ImageView(ScreenCaptureToolApp.class.getResource(imgPaths.get(i)).toExternalForm());
            imageView.setFitWidth(20);  // 设置图标的宽度
            imageView.setFitHeight(20); // 设置图标的高度
            choices[i].setGraphic(imageView);
            int r = i / n;  // 计算行
            int c = i % n;  // 计算列
            imageView.setUserData(imgPaths.get(i));
            int finalI = i;
            choices[i].setOnAction(e->{
                popup.hide();
                onClicked(gc,finalI);
            });
            displayer.add(choices[i], r, c);
        }
        this.setOnAction(e->{
            popup(this.getScene().getWindow());
        });
        popup.getContent().add(displayer);
        register();
    }
    public void popup(Window window){
        boolean wasShowing = popup.isShowing();
        hideAllPopups(); // 先关所有 picker（包括自己），绕开 autoHide 的 click-absorption 问题
        if (wasShowing) return; // 点同一个按钮时收起（toggle）
        if (lazyPaths != null && loadedCount == 0) {
            ScrollPane sp = (ScrollPane) popup.getContent().get(0);
            loadNextBatch(sp);
        }
        popup.show(window,this.localToScreen(this.getBoundsInLocal()).getMinX()+40,
                this.localToScreen(this.getBoundsInLocal()).getMaxY()-40);
    }
    public Button getTrigger() {
        return this;
    }

    protected abstract void onClicked(GraphicsContext gc,int index);
}
