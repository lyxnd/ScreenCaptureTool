package net.jackchuan.screencapturetool.util;

import javafx.animation.PauseTransition;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.Optional;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/12/10 14:15
 */
public class AlertHelper {

    public static boolean showConfirmAlert(){
        Alert alert=new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit confirm");
        alert.setHeaderText("Are you sure you want to exit now?");
        alert.setContentText("Click OK to exit, or Cancel to continue.");
        Optional<ButtonType> type = alert.showAndWait();
        if(type.isEmpty()){
            return false;
        }
        return type.get() == ButtonType.OK;
    }

    public static void showAutoClosedPopup(String message, int seconds,double x,double y) {
        // 创建弹出框 Stage
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL); // 模态窗口（阻止与其他窗口交互）
        popupStage.initStyle(StageStyle.UNDECORATED); // 无装饰窗口
        popupStage.setAlwaysOnTop(true);

        // 弹出框内容
        Label label = new Label(message);
        label.setStyle("-fx-font-size: 16px; -fx-padding: 20px; -fx-background-color: lightblue; -fx-border-color: gray; -fx-border-width: 2px;");
        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 150, 50);
        popupStage.setScene(scene);
        popupStage.setX(x);
        popupStage.setY(y);
        // 显示弹出框
        popupStage.show();

        // 延时关闭弹出框
        PauseTransition delay = new PauseTransition(Duration.seconds(seconds));
        delay.setOnFinished(event -> popupStage.close());
        delay.play();
    }


}
