package net.jackchuan.screencapturetool.external;

import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import net.jackchuan.screencapturetool.controller.CaptureDisplayController;
import net.jackchuan.screencapturetool.entity.ControllerInstance;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/12/22 19:33
 */
public class SelectorMenu extends MenuItem {
    private String text;
    private int type;
    private Font defaultFont;
    private TextField textField;
    public SelectorMenu(String text, int type, Font defaultFont, TextField field) {
        this.text = text;
        this.type = type;
        this.textField=field;
        this.defaultFont = defaultFont;
        this.setText(text);
        this.setStyle("-fx-font-family: " + defaultFont.getName() + "; -fx-font-size: " + defaultFont.getSize() + "px;");
        this.setOnAction(e->{
            if(type==0){
                //字体
                CaptureDisplayController controller = ControllerInstance.getInstance().getController();
                Font font = controller.getFont();
                Font font1=new Font(defaultFont.getFamily(),font==null?12:font.getSize());
                controller.setFont(font1);
                textField.setFont(font1);
            }else if(type==1){
                //大小
                CaptureDisplayController controller = ControllerInstance.getInstance().getController();
                Font font = controller.getFont();
                Font font1=new Font(font==null?"Arial":font.getFamily(),Integer.parseInt(text));
                controller.setFont(font1);
                textField.setFont(font1);
            }
        });
    }

    public SelectorMenu(String text,int type) {
        this.text = text;
        this.type = type;
        this.setText(text);
        this.setOnAction(e->{
            System.out.println(toString());
            if(type==0){
                //字体
                CaptureDisplayController controller = ControllerInstance.getInstance().getController();
                Font font = controller.getFont();
                controller.setFont(new Font(text,font==null?12:font.getSize()));
            }else if(type==1){
                //大小
                CaptureDisplayController controller = ControllerInstance.getInstance().getController();
                Font font = controller.getFont();
                controller.setFont(new Font(font==null?"Arial":font.getName(),Integer.parseInt(text)));
            }
        });
    }

    @Override
    public String toString() {
        return "SelectorMenu{" +
                "text='" + text + '\'' +
                ", type=" + type +
                '}';
    }
}
