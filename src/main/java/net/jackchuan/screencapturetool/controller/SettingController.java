package net.jackchuan.screencapturetool.controller;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.jackchuan.screencapturetool.CaptureProperties;
import net.jackchuan.screencapturetool.ScreenCaptureToolApp;

import java.io.*;
import java.util.List;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/11/23 19:17
 */
public class SettingController {
    @FXML
    private TextField savePath;
    @FXML
    private Label state;
    @FXML
    private ToggleGroup toggleGroup;
    @FXML
    private RadioButton isShiftNeeded, isAltNeeded, isCtrlNeeded;
    @FXML
    private Button changeKeyBind;
    @FXML
    private ComboBox<String> captureType;
    @FXML
    private VBox setBox;
    @FXML
    private CheckBox enableAll, export, copy, reset, clearHistory;
    @FXML
    private CheckBox drag, pencil, rubber, rect, filledRect, oval, arrow, line, wave;
    @FXML
    private CheckBox color, strokeUp, strokeDown, undo, redo,autoCopy,autoSelect;
    private Stage parent;
    private boolean changing = false;

    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            parent = (Stage) setBox.getScene().getWindow();
            setBox.setPrefHeight(parent.getHeight());
            setBox.setPrefWidth(parent.getWidth());
            initSettings();
            captureType.valueProperty().addListener((obj, oldVal, newVal) -> {
                CaptureProperties.captureType = newVal;
            });

            GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
                @Override
                public void nativeKeyPressed(NativeKeyEvent e) {
                    if (changing && e.getKeyCode() != NativeKeyEvent.VC_SHIFT &&
                            e.getKeyCode() != NativeKeyEvent.VC_ALT && e.getKeyCode() != NativeKeyEvent.CTRL_MASK) {
                        CaptureProperties.CAPTURE_KEY = e.getKeyCode();
                        CaptureProperties.isShiftNeeded = isShiftNeeded.isSelected();
                        CaptureProperties.isAltNeeded = isAltNeeded.isSelected();
                        CaptureProperties.isCtrlNeeded = isCtrlNeeded.isSelected();
                        Platform.runLater(() -> {
                            changeKeyBind.setText(NativeKeyEvent.getKeyText(e.getKeyCode()));
                        });
                    }
                }

                @Override
                public void nativeKeyReleased(NativeKeyEvent e) {
                    if (parent.isFocused()) {
                        changing = false;
                        updateState("当前快捷键更改为 ：" + getKeyBind());
                    }
                }

                @Override
                public void nativeKeyTyped(NativeKeyEvent e) {
                    // 不需要实现此方法
                }
            });
        });
    }

    private String getKeyBind() {
        String keyBind = "";
        if (isShiftNeeded.isSelected()) {
            keyBind = "Shift + ";
        } else if (isCtrlNeeded.isSelected()) {
            keyBind = "Ctrl + ";
        } else if (isAltNeeded.isSelected()) {
            keyBind = "Alt + ";
        }
        return keyBind + changeKeyBind.getText();
    }

    private void initSettings() {
        enableAll.setSelected(CaptureProperties.enableAll);
        export.setSelected(CaptureProperties.export);
        copy.setSelected(CaptureProperties.copy);
        reset.setSelected(CaptureProperties.reset);
        clearHistory.setSelected(CaptureProperties.clearHistory);
        drag.setSelected(CaptureProperties.drag);
        pencil.setSelected(CaptureProperties.pencil);
        rubber.setSelected(CaptureProperties.rubber);
        rect.setSelected(CaptureProperties.rect);
        filledRect.setSelected(CaptureProperties.filledRect);
        oval.setSelected(CaptureProperties.oval);
        arrow.setSelected(CaptureProperties.arrow);
        line.setSelected(CaptureProperties.line);
        wave.setSelected(CaptureProperties.wave);
        color.setSelected(CaptureProperties.color);
        strokeUp.setSelected(CaptureProperties.strokeUp);
        strokeDown.setSelected(CaptureProperties.strokeDown);
        undo.setSelected(CaptureProperties.undo);
        redo.setSelected(CaptureProperties.redo);
        isShiftNeeded.setSelected(CaptureProperties.isShiftNeeded);
        isAltNeeded.setSelected(CaptureProperties.isAltNeeded);
        isCtrlNeeded.setSelected(CaptureProperties.isCtrlNeeded);
        autoCopy.setSelected(CaptureProperties.autoCopy);
        autoSelect.setSelected(CaptureProperties.autoSelect);
        captureType.setValue(CaptureProperties.captureType);
        savePath.setText(CaptureProperties.configPath);
        changeKeyBind.setText(NativeKeyEvent.getKeyText(CaptureProperties.CAPTURE_KEY));
    }

    @FXML
    private void updateSettings(ActionEvent e) {
        String key = handleSource(e.getSource().toString());
        switch (key) {
            case "enableAll" -> {
                CaptureProperties.enableAll = enableAll.isSelected();
                CaptureProperties.updateAll(enableAll.isSelected());
                setAll(enableAll.isSelected());
            }
            case "export" -> {
                CaptureProperties.export = export.isSelected();
            }
            case "copy" -> {
                CaptureProperties.copy = copy.isSelected();
            }
            case "reset" -> {
                CaptureProperties.reset = reset.isSelected();
            }
            case "clearHistory" -> {
                CaptureProperties.clearHistory = clearHistory.isSelected();
            }
            case "drag" -> {
                CaptureProperties.drag = drag.isSelected();
            }
            case "pencil" -> {
                CaptureProperties.pencil = pencil.isSelected();
            }
            case "rubber" -> {
                CaptureProperties.rubber = rubber.isSelected();
            }
            case "rect" -> {
                CaptureProperties.rect = rect.isSelected();
            }
            case "filledRect" -> {
                CaptureProperties.filledRect = filledRect.isSelected();
            }
            case "oval" -> {
                CaptureProperties.oval = oval.isSelected();
            }
            case "arrow" -> {
                CaptureProperties.arrow = arrow.isSelected();
            }
            case "line" -> {
                CaptureProperties.line = line.isSelected();
            }
            case "wave" -> {
                CaptureProperties.wave = wave.isSelected();
            }
            case "color" -> {
                CaptureProperties.color = color.isSelected();
            }
            case "strokeUp" -> {
                CaptureProperties.strokeUp = strokeUp.isSelected();
            }
            case "strokeDown" -> {
                CaptureProperties.strokeDown = strokeDown.isSelected();
            }
            case "undo" -> {
                CaptureProperties.undo = undo.isSelected();
            }
            case "redo" -> {
                CaptureProperties.redo = redo.isSelected();
            }
            case "autoCopy" -> {
                CaptureProperties.autoCopy = autoCopy.isSelected();
            }
            case "autoSelect" -> {
                CaptureProperties.autoSelect = autoSelect.isSelected();
            }

        }

    }

    private String handleSource(String str) {
        return str.split(",")[0].substring(str.indexOf("=") + 1);
    }

    public void setAll(boolean isSelected) {
        enableAll.setSelected(isSelected);
        export.setSelected(isSelected);
        copy.setSelected(isSelected);
        reset.setSelected(isSelected);
        clearHistory.setSelected(isSelected);
        drag.setSelected(isSelected);
        pencil.setSelected(isSelected);
        rubber.setSelected(isSelected);
        rect.setSelected(isSelected);
        filledRect.setSelected(isSelected);
        oval.setSelected(isSelected);
        arrow.setSelected(isSelected);
        line.setSelected(isSelected);
        wave.setSelected(isSelected);
        color.setSelected(isSelected);
        strokeUp.setSelected(isSelected);
        strokeDown.setSelected(isSelected);
        undo.setSelected(isSelected);
        redo.setSelected(isSelected);
        autoCopy.setSelected(isSelected);
    }

    @FXML
    private void changeKeyBinding() {
        changing = true;
        state.setText("请输入快捷键(仅支持最多双按键，勾选三个辅助按键之一的一个，然后输入一个快捷键即可)");
    }

    @FXML
    private void saveAsFile() {
        // save settings to file here
        FileChooser chooser = new FileChooser();
        chooser.setInitialDirectory(new File("D:/"));
        chooser.setInitialFileName("config.txt");
        chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("文本文件", ".txt"));
        chooser.setTitle("Save configurations as file");
        File file = chooser.showSaveDialog(parent);
        if (file != null) {
            Platform.runLater(() -> {
                savePath.setText(file.getAbsolutePath());
            });
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(toString());
                file.setWritable(false);
                File configFile = new File(System.getProperty("user.home") + "/captureToolConfig.txt");
                configFile.setWritable(true);
                try (FileWriter writer1 = new FileWriter(configFile)) {
                    writer1.write(file.getAbsolutePath());
                    CaptureProperties.configPath = file.getAbsolutePath();
                    configFile.setWritable(false);
                }
               updateState("configuration saved successfully!");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String toString() {
        return "ScreenCaptureToolProperties :{" +
                "\n isShiftNeeded=" + isShiftNeeded.isSelected() +
                "\n isAltNeeded=" + isAltNeeded.isSelected() +
                "\n isCtrlNeeded=" + isCtrlNeeded.isSelected() +
                "\n captureType=" + captureType.getValue() +
                "\n enableAll=" + enableAll.isSelected() +
                "\n export=" + export.isSelected() +
                "\n copy=" + copy.isSelected() +
                "\n reset=" + reset.isSelected() +
                "\n clearHistory=" + clearHistory.isSelected() +
                "\n drag=" + drag.isSelected() +
                "\n pencil=" + pencil.isSelected() +
                "\n rubber=" + rubber.isSelected() +
                "\n rect=" + rect.isSelected() +
                "\n filledRect=" + filledRect.isSelected() +
                "\n oval=" + oval.isSelected() +
                "\n arrow=" + arrow.isSelected() +
                "\n line=" + line.isSelected() +
                "\n wave=" + wave.isSelected() +
                "\n color=" + color.isSelected() +
                "\n strokeUp=" + strokeUp.isSelected() +
                "\n strokeDown=" + strokeDown.isSelected() +
                "\n undo=" + undo.isSelected() +
                "\n redo=" + redo.isSelected() +
                "\n captureKey=" + CaptureProperties.CAPTURE_KEY +
                "\n autoCopy=" + autoCopy.isSelected() +
                "\n autoSelect=" + autoSelect.isSelected() +
                "\n}";
    }

    public void saveOnOriginalPath() {
        File f=new File(savePath.getText());
        f.setWritable(true);
        try (FileWriter writer1 = new FileWriter(f)) {
            writer1.write(toString());
            updateState("configuration saved successfully!");
            f.setWritable(false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void updateState(String value){
        Platform.runLater(() -> {
            state.setText(value);
        });
    }

}
