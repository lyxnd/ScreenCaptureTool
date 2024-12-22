package net.jackchuan.screencapturetool.external.pane;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import net.jackchuan.screencapturetool.controller.CaptureDisplayController;
import net.jackchuan.screencapturetool.external.ExternalTextHandler;
import net.jackchuan.screencapturetool.external.SelectorMenu;


/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/12/20 19:14
 */
public class TextFieldPane extends Pane {
    private TextField textField;
    private CaptureDisplayController controller;
    private ContextMenu fontSelector;
    private Menu font;
    private Menu size;
    private MenuItem arial;
    private MenuItem Helvetica;
    private MenuItem Merryweather;
    private MenuItem Consolas;
    private MenuItem songTi;
    private MenuItem xinSongTi;
    private MenuItem fangSong;
    private MenuItem weiRuanYaHei;
    private MenuItem kaiTi;
    private MenuItem huaWenSongTi;
    private MenuItem xingShu;//行书
    private MenuItem caoShu;//草书
    private MenuItem[] sizes;
    public TextFieldPane(CaptureDisplayController controller, double width, double height){
        this.controller=controller;
        this.textField=new TextField();
        initMenus();
        textField.setMaxWidth(150);
        textField.setBackground(Background.fill(Color.rgb(236, 236, 236, 0.5)));
        textField.setContextMenu(fontSelector);
        this.setPickOnBounds(false);
        this.setPrefSize(width,height);
        this.setBackground(Background.EMPTY);
        this.getChildren().add(textField);
        textField.setOnMousePressed(e -> {
            controller.setMoving(true);
            textField.setFocusTraversable(false);
        });
        textField.setOnMouseDragged(e -> {
            if (controller.isAltPressed() && controller.isMoving()) {
                textField.setLayoutX(e.getSceneX());
                textField.setLayoutY(e.getSceneY() - controller.getToolBarHeight());
            }
        });
        textField.setOnMouseReleased(e -> {
            controller.setMoving(false);
            textField.setFocusTraversable(false);
        });
        textField.setOnAction(e -> {
            ExternalTextHandler.DrawableText text=new ExternalTextHandler.DrawableText(textField.getText(),
                    textField.getLayoutX(),textField.getLayoutY()+textField.getHeight()+30,
                    controller.getFont(),controller.getStrokeColor());
            controller.getTextHandler().addExternalText(text);
            controller.getTextHandler().drawAllTexts(controller.getGraphicsContext());
            controller.addExternalText(text);
        });
        textField.textProperty().addListener((obj,oldVal,newVal)->{
//            drawText();
        });
        this.focusedProperty().addListener((obj,oldVal,newVal)->{
            if(!newVal){
                removeTextField();
            }
        });
    }

    private void initMenus() {
        fontSelector=new ContextMenu();
        font=new Menu("字体");
        size=new Menu("大小");
        arial = new SelectorMenu("Arial", 0, new Font("Arial", 14), textField);
        Helvetica = new SelectorMenu("Helvetica", 0, new Font("Helvetica", 14), textField);
        Merryweather = new SelectorMenu("Merryweather", 0, new Font("Merryweather", 14), textField);
        Consolas = new SelectorMenu("Consolas", 0, new Font("Consolas", 14), textField);
        songTi = new SelectorMenu("宋体", 0, new Font("SimSun", 14), textField);
        xinSongTi = new SelectorMenu("新宋体", 0, new Font("NSimSun", 14), textField);
        fangSong = new SelectorMenu("仿宋", 0, new Font("FangSong", 14), textField);
        weiRuanYaHei = new SelectorMenu("微软雅黑", 0, new Font("Microsoft YaHei", 14), textField);
        kaiTi = new SelectorMenu("楷体", 0, new Font("KaiTi", 14), textField);
        huaWenSongTi = new SelectorMenu("华文仿宋", 0, new Font("STFangsong", 14), textField);
        xingShu = new SelectorMenu("行书", 0, new Font("Brush Script MT", 14), textField); // 行书
        caoShu = new SelectorMenu("草书", 0, new Font("Comic Sans MS", 14), textField); // 草书

        sizes=new SelectorMenu[40];
        for(int i=0;i<sizes.length;i++){
            sizes[i]=new SelectorMenu(String.valueOf(i+6),1,new Font("Arial",14),textField);
            size.getItems().add(sizes[i]);
        }
        font.getItems().addAll(arial,Helvetica,Merryweather,Consolas,
                songTi,xinSongTi,fangSong,weiRuanYaHei,kaiTi,huaWenSongTi,xingShu,caoShu);
        fontSelector.getItems().addAll(font,size);
    }

    public void removeTextField() {
        this.getChildren().remove(textField);
    }

    public boolean isWriting() {
        return this.textField.isFocused();
    }
    public void addTextField(String text,Font font) {
        if(!this.getChildren().contains(textField)){
            this.getChildren().add(textField);
            textField.setText(text);
            textField.setFont(font);
        }
    }


    public boolean shouldAddTextField() {
        return !this.getChildren().contains(textField);
    }

    public boolean isInField(MouseEvent event) {
        return textField.getLayoutX()<=event.getX()&&textField.getLayoutY()<=event.getY()
                &&(textField.getWidth()+textField.getLayoutX())>=event.getX()
                &&(textField.getHeight()+textField.getLayoutY())>=event.getY();
    }

    public void setTextFieldPos(double x, double y) {
        textField.setLayoutX(x);
        textField.setLayoutY(y - controller.getToolBarHeight());
    }
}
