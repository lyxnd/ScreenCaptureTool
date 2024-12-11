package net.jackchuan.screencapturetool.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.jackchuan.screencapturetool.util.ControllerInstance;

import java.util.List;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/11/22 17:23
 */
public class EditRecordController {

    @FXML
    private VBox records;
    private List<String> recordsList;
    private Stage self;
    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            self= (Stage) records.getScene().getWindow();
            records.setPrefWidth(self.getWidth());
            records.setPrefHeight(self.getHeight());
            records.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode()== KeyCode.TAB) {
                    event.consume(); // 阻止默认的焦点切换行为
                }
            });
            records.getScene().setOnKeyReleased(e->{
                if (e.getCode()== KeyCode.TAB) {
                    if(self!=null){
                        self.close();
                    }
                }
            });

            for (String str : recordsList) {
                Button btn = createButton(str);
                records.getChildren().add(btn);
            }
        });
    }

    private Button createButton(String str) {
        Button btn = new Button(str);
        btn.setPrefWidth(self.getWidth());
        btn.setPrefHeight(30);
        btn.setOnAction(e -> {
            String str1=e.getSource().toString().split("'")[1];
            int a = str1.indexOf("(");
            int index= Integer.parseInt(str1.substring(a+1,str1.length()-1));
            ControllerInstance.getInstance().getController().jumpTo(index);
            ControllerInstance.getInstance().getController().saveCurrentState("jump edit("+recordsList.size()+")");
        });
        return btn;
    }


    public void setRecordsList(List<String> recordsList) {
        this.recordsList = recordsList;
    }


    public void addRecord(String editType) {
        recordsList.add(editType);
        Button btn = createButton(editType);
        records.getChildren().add(btn);
    }
}
