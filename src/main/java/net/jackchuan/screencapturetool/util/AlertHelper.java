package net.jackchuan.screencapturetool.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

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


}
