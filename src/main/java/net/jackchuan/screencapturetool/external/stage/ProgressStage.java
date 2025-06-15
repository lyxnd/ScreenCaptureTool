package net.jackchuan.screencapturetool.external.stage;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.jackchuan.screencapturetool.CaptureProperties;
import net.jackchuan.screencapturetool.controller.CaptureDisplayController;
import net.jackchuan.screencapturetool.entity.ControllerInstance;
import net.jackchuan.screencapturetool.entity.StageInstance;
import net.jackchuan.screencapturetool.util.FileHandler;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/12/23 14:35
 */
public class ProgressStage extends Stage {
    private ProgressBar progressBar;
    private Label tip;
    private StackPane stackPane;
    private VBox vBox;
    private HBox hBox;
    private Scene scene;
    private double max=100;
    private double step=1;
    private Task<Void> taskThread;
    private String ocrPath;
    private Label url;
    private Button download;
    private Button btn;
    public ProgressStage(String title,Stage parent,Task<Void> task) {
        this.setTitle(title);
        progressBar=new ProgressBar();
        btn=new Button("重新下载");
        progressBar.setPrefWidth(250);
        tip=new Label("progress");
        vBox =new VBox();
        hBox=new HBox();
        download=new Button("打开浏览器下载");
        url=new Label("https://github.com/lyxnd/temp/archive/refs/heads/main.zip");
        vBox.setAlignment(Pos.CENTER);
        vBox.getChildren().addAll(progressBar,tip);
        stackPane=new StackPane(vBox);
        scene=new Scene(stackPane,400,100);
        this.setScene(scene);
//        tip.setStyle("-fx-padding: -10 0 0 0;");
        if(parent!=null){
            this.setX(parent.getX()+parent.getWidth()/3);
            this.setY(parent.getY()+parent.getHeight()/2);
        }
        StageInstance.getInstance().setProgressStage(this);
        this.taskThread=task;
        taskThread.setOnFailed(event -> {
            Throwable exception = taskThread.getException();
            exception.printStackTrace(); // 打印异常信息
            // 提示用户任务失败
            Platform.runLater(() -> {
                setProgress(0);
                setTitle("下载或解压失败");
                CaptureDisplayController controller = ControllerInstance.getInstance().getController();
                controller.setType(-2);
                controller.setCursorShape(Cursor.DEFAULT);
                tip.setText("");
                hBox.getChildren().addAll(download,url);
                vBox.getChildren().add(btn);
                vBox.getChildren().add(hBox);
                AlertHelper.showErrorDialog("Download failed",
                        "Some network error occurred,Please try again later",exception.toString());
            });
        });
        taskThread.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                setTitle("下载和解压完成");
                System.out.println("下载和解压完成");
                CaptureProperties.ocrFileInstalled=true;
                CaptureProperties.ocrPath=ocrPath;
                CaptureProperties.saveOnOriginalPath();
                close();
            });
        });

        btn.setOnAction(e-> {
            startTask();
            vBox.getChildren().remove(btn);
            vBox.getChildren().add(tip);
        });
        download.setOnAction(e->{
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                try {
                    Desktop.getDesktop().browse(new URI(url.getText()));
                    tip.setText("下载完成后请在设置中进行配置");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                } catch (URISyntaxException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                System.out.println("当前平台不支持打开浏览器功能");
            }
        });

        this.setOnCloseRequest(e->{
            stopTask();
            FileHandler.deleteDownloadedFile(CaptureProperties.exePath);
        });
    }

    public void setMax(double max){
        this.max=max;
    }

    public void setStep(double step) {
        this.step = step/max;
    }

    public void setProgress(double progress){
        Platform.runLater(()->{
            progressBar.setProgress(progress);
        });
    }
    public void increaseProgress(){
        Platform.runLater(()->{
            if(progressBar.getProgress()+step<1.0){
                progressBar.setProgress(progressBar.getProgress()+step);
            }
        });
    }

    public void decreaseProgress(){
        Platform.runLater(()->{
            if(progressBar.getProgress()-step>0){
                progressBar.setProgress(progressBar.getProgress()-step);
            }
        });
    }

    public void startTask(){
        new Thread(taskThread).start();
    }
    public void stopTask(){
        taskThread.cancel();
    }

    public String getOcrPath() {
        return ocrPath;
    }

    public void setOcrPath(String ocrPath) {
        this.ocrPath = ocrPath;
    }

    public void setTip(String tip) {
        Platform.runLater(()->{
            this.tip.setText(tip);
        });
    }
}
