package net.jackchuan.screencapturetool.external.stage;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.datatransfer.StringSelection;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/12/19 23:14
 */
public class TextRecognitionStage extends Stage {
    private TextArea textArea;
    private ScrollPane scrollPane;
    private Scene scene;
    private VBox vBox;
    private FlowPane flowPane;
    private Button copy;
    private RadioButton cancelTop;
    public TextRecognitionStage(String text,Stage parent){
        copy=new Button("Copy");
        cancelTop=new RadioButton("always on top");
        flowPane=new FlowPane();
        flowPane.setAlignment(Pos.CENTER_RIGHT);
        flowPane.getChildren().addAll(cancelTop,copy);
        vBox=new VBox();
        textArea=new TextArea(text);
        scrollPane=new ScrollPane(textArea);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        vBox.getChildren().addAll(flowPane,scrollPane);
        scene=new Scene(vBox,400,400);

        cancelTop.selectedProperty().addListener((obj,old,newVal)->{
            setAlwaysOnTop(newVal);
        });
        copy.setOnAction(e->copy());
        this.setScene(scene);
        this.setX(parent.getX()+parent.getWidth()/2);
        this.setY(parent.getY());
        this.setTitle("-识别结果-");
        cancelTop.setSelected(true);
        this.setAlwaysOnTop(true);

    }
    public void setText(String text){
        textArea.setText(text);
    }

    public void copy(){
        // 将文本封装到 StringSelection 中
        StringSelection stringSelection = new StringSelection(textArea.getText());
        // 获取系统剪贴板并设置内容
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
    }

}
