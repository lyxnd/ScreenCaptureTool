package net.jackchuan.screencapturetool.external.picker;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Popup;
import javafx.stage.Window;
import net.jackchuan.screencapturetool.ScreenCaptureToolApp;
import net.jackchuan.screencapturetool.controller.CaptureDisplayController;

import java.util.ArrayList;

/**
 * 功能：
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
            if(!val2){
                popup.hide();
            }
        });
    }
    public CustomPicker(String text, ArrayList<String> imgPaths,GraphicsContext gc){
        this.gc=gc;
        initUI(text,imgPaths);
        this.focusedProperty().addListener((val,val1,val2) -> {
            if(!val2){
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
            if(!val2){
                popup.hide();
            }
        });
    }

    public CustomPicker(Image icon, Image[] imgs,GraphicsContext gc){
        this.gc=gc;
        initUI(icon,imgs);
        this.focusedProperty().addListener((val,val1,val2) -> {
            if(!val2){
                popup.hide();
            }
        });
    }
    public void initUI(){
        popup=new Popup();
        displayer=new GridPane();
        popup.getContent().add(displayer);
        this.setOnAction(e -> popup(this.getScene().getWindow()));
    }

    private void initUI(Image icon, Image[] imgs) {
        popup=new Popup();
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

    private void initUI(String text, ArrayList<String> imgPaths) {
        popup=new Popup();
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
        popup.show(window,this.localToScreen(this.getBoundsInLocal()).getMinX(),
                this.localToScreen(this.getBoundsInLocal()).getMaxY());
    }
    public Button getTrigger() {
        return this;
    }

    protected abstract void onClicked(GraphicsContext gc,int index);
}
