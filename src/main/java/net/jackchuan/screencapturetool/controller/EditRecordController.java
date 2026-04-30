package net.jackchuan.screencapturetool.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.jackchuan.screencapturetool.entity.ControllerInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/11/22 17:23
 */
public class EditRecordController {

    @FXML
    private VBox records;
    private List<String> recordsList=new ArrayList<>();
    private Stage self;
    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            self = (Stage) records.getScene().getWindow();
            records.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.TAB) {
                    event.consume();
                }
            });
            records.getScene().setOnKeyReleased(e -> {
                if (e.getCode() == KeyCode.TAB && self != null) {
                    self.close();
                }
            });
            for (String str : recordsList) {
                Button btn = createButton(str, 0);
                records.getChildren().add(btn);
            }
        });
    }

    private Button createButton(String str, double width) {
        int index = records.getChildren().size();
        Button btn = new Button(String.format("%02d  %s", index + 1, str));
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add(index % 2 == 0 ? "record-item" : "record-item-alt");
        btn.setOnAction(e -> {
            int i = records.getChildren().indexOf(e.getSource());
            ControllerInstance.getInstance().getController().jumpTo(i);
        });
        return btn;
    }


    public void setRecordsList(List<String> recordsList) {
        this.recordsList = recordsList;
    }


    public void addRecord(String editType) {
        recordsList.add(editType);
        Button btn = createButton(editType, 0);
        records.getChildren().add(btn);
    }

    public void clearAllRecords() {
        recordsList.clear();
        records.getChildren().clear();
    }
}
