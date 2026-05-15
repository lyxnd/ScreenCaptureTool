package net.jackchuan.screencapturetool.external.stage;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.jackchuan.screencapturetool.util.TranslateService;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.concurrent.CompletableFuture;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/12/19 23:14
 */
public class TextRecognitionStage extends Stage {
    private TextArea textArea;
    private TextArea translateArea;
    private ScrollPane scrollPane;
    private ScrollPane translateScrollPane;
    private Scene scene;
    private VBox vBox;
    private FlowPane flowPane;
    private FlowPane translatePane;
    private Button copy;
    private Button reduce;
    private Button translateBtn;
    private ComboBox<String> langBox;
    private RadioButton cancelTop;

    public TextRecognitionStage(String text, Stage parent) {
        copy = new Button("Copy");
        reduce = new Button("Reduce");
        reduce.setTooltip(new Tooltip("删除空格"));
        cancelTop = new RadioButton("always on top");

        langBox = new ComboBox<>();
        langBox.getItems().addAll("英文", "中文", "日语", "韩语", "法语", "德语", "西班牙语");
        langBox.setValue("英文");
        langBox.setPrefWidth(80);

        translateBtn = new Button("翻译");

        flowPane = new FlowPane(4, 4);
        flowPane.setAlignment(Pos.CENTER_RIGHT);
        flowPane.setPadding(new Insets(2, 4, 2, 4));
        flowPane.getChildren().addAll(cancelTop, copy, reduce, langBox, translateBtn);

        textArea = new TextArea(text);
        textArea.setWrapText(true);
        scrollPane = new ScrollPane(textArea);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(180);

        translateArea = new TextArea();
        translateArea.setWrapText(true);
        translateArea.setEditable(false);
        translateArea.setPromptText("翻译结果...");
        translateScrollPane = new ScrollPane(translateArea);
        translateScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        translateScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        translateScrollPane.setFitToWidth(true);
        translateScrollPane.setPrefHeight(180);

        vBox = new VBox(4);
        vBox.getChildren().addAll(flowPane, scrollPane, translateScrollPane);
        scene = new Scene(vBox, 400, 420);

        cancelTop.selectedProperty().addListener((obj, old, newVal) -> setAlwaysOnTop(newVal));
        copy.setOnAction(e -> copy());
        reduce.setOnAction(e -> textArea.setText(textArea.getText().replaceAll(" ", "")));
        translateBtn.setOnAction(e -> doTranslate());

        this.setScene(scene);
        this.setX(parent.getX() + parent.getWidth() / 2);
        this.setY(parent.getY());
        this.setTitle("-识别结果-");
        cancelTop.setSelected(true);
        this.setAlwaysOnTop(true);
    }

    private void doTranslate() {
        String src = textArea.getText().trim();
        if (src.isEmpty()) return;
        String targetLang = switch (langBox.getValue()) {
            case "中文"    -> "zh";
            case "日语"    -> "ja";
            case "韩语"    -> "ko";
            case "法语"    -> "fr";
            case "德语"    -> "de";
            case "西班牙语" -> "es";
            default        -> "en";
        };
        translateBtn.setDisable(true);
        translateArea.setText("翻译中...");
        CompletableFuture.supplyAsync(() -> {
            try {
                return TranslateService.translate(src, targetLang);
            } catch (Exception ex) {
                return "翻译失败: " + ex.getMessage();
            }
        }).thenAccept(result -> Platform.runLater(() -> {
            translateArea.setText(result);
            translateBtn.setDisable(false);
        }));
    }

    public void setText(String text) {
        textArea.setText(text);
        translateArea.clear();
    }

    public void copy() {
        StringSelection stringSelection = new StringSelection(textArea.getText());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
    }
}
