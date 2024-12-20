package net.jackchuan.screencapturetool.external;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/12/18 19:55
 */
public class ToolBarManager {
    public ToolBar toolBar;

    public ArrayList<Node> comps;
    public ToolBarManager(ToolBar toolBar){
        this.toolBar=toolBar;
        comps=new ArrayList<>();
    }
    public void addAll(Node... nodes){
        comps.addAll(Arrays.asList(nodes));
    }
    public void add(Node node){
        comps.add(node);
    }
    public void addToToolBar(){
        int n=0;
        for(Node node : comps){
            toolBar.getItems().add(node);
        }
    }

    public void enableOthers() {
        for(Node node : comps){
           node.setDisable(false);
        }
    }

    public void disableSelf(String text) {
        for(Node node : comps){
            if(node instanceof Button btn&&btn.getText().equals(text)){
                node.setDisable(true);
                break;
            }
        }
    }
}
