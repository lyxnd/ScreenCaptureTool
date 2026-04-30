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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

/**
 * 功能：继承该类后可以在点击时弹出选择框，在子类中设置选项点击事件，参数为选项序号，(无需为子类设置onAction等)
 * 作者：jackchuan
 * 日期：2024/12/17 19:57
 */
public abstract class CustomPicker extends Button {
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
        this.focusedProperty().addListener((val,val1,val2) -> {
            if(!val2 && popup != null){
                popup.hide();
            }
        });
    }
    public CustomPicker(Image icon, ArrayList<String> imgPaths,CaptureDisplayController controller){
        this.controller=controller;
        initUI(icon,imgPaths);
        this.focusedProperty().addListener((val,val1,val2) -> {
            if(!val2){
                popup.hide();
            }
        });
    }
    public CustomPicker(Image icon, ArrayList<String> imgPaths,GraphicsContext gc){
        this.gc=gc;
        initUI(icon,imgPaths);
        this.focusedProperty().addListener((val,val1,val2) -> {
            if(!val2&& popup != null){
                popup.hide();
            }
        });
    }

    public CustomPicker(String folder,CaptureDisplayController controller) throws URISyntaxException {
        this.controller=controller;
        this.focusedProperty().addListener((val,val1,val2) -> {
            if(!val2 && popup != null){
                popup.hide();
            }
        });
        initUI(folder);
    }

    private File[] lazyFiles;
    private int loadedCount = 0;
    private boolean loading = false;
    private static final int SCROLL_BATCH = 150;
    private static final int SCROLL_COLS = 10;

    private void initUI(String folder) throws URISyntaxException {
        popup = new Popup();
        popup.setAutoHide(true);
        displayer = new GridPane();
        viewList = new ArrayList<>();
        URL resource = ScreenCaptureToolApp.class.getResource(folder);
        if (resource != null) {
            File dir = new File(resource.toURI());
            lazyFiles = dir.listFiles();
        }
        ScrollPane scrollPane = new ScrollPane(displayer);
        scrollPane.setPrefHeight(300);
        scrollPane.setPrefWidth(380);
        popup.setWidth(380);
        scrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() >= 0.85 && !loading
                    && lazyFiles != null && loadedCount < lazyFiles.length) {
                loadNextBatch(scrollPane);
            }
        });
        popup.getContent().add(scrollPane);
        this.setOnAction(e -> popup(this.getScene().getWindow()));
    }

    private void loadNextBatch(ScrollPane scrollPane) {
        if (loading || lazyFiles == null || loadedCount >= lazyFiles.length) return;
        loading = true;
        int start = loadedCount;
        int end = Math.min(start + SCROLL_BATCH, lazyFiles.length);
        for (int i = start; i < end; i++) {
            Button btn = new Button();
            ImageView imageView = new ImageView(lazyFiles[i].toURI().toString());
            imageView.setFitWidth(20);
            imageView.setFitHeight(20);
            btn.setGraphic(imageView);
            int finalI = i;
            btn.setOnAction(e -> { popup.hide(); onClicked(gc, finalI); });
            displayer.add(btn, i % SCROLL_COLS, i / SCROLL_COLS);
        }
        loadedCount = end;
        // 延迟重置，等布局完成后再允许下一次触发
        Platform.runLater(() -> loading = false);
    }

    public void initUI(){
        popup=new Popup();
        popup.setAutoHide(true);
        displayer=new GridPane();
        popup.getContent().add(displayer);
        this.setOnAction(e -> popup(this.getScene().getWindow()));
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
    }
    public void popup(Window window){
        if (lazyFiles != null && loadedCount == 0) {
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
