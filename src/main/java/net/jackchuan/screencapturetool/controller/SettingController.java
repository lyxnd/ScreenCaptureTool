package net.jackchuan.screencapturetool.controller;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.jackchuan.screencapturetool.CaptureProperties;
import net.jackchuan.screencapturetool.ScreenCaptureToolApp;
import net.jackchuan.screencapturetool.external.stage.AlertHelper;
import net.jackchuan.screencapturetool.external.stage.ProgressStage;
import net.jackchuan.screencapturetool.network.HttpRequestHandler;
import net.jackchuan.screencapturetool.util.FileHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/11/23 19:17
 */
public class SettingController {
    public TitledPane keys;
    public TextField logPath;
    public Button changeUploadKeyBind;
    public RadioButton uploadIsShiftNeeded;
    public RadioButton uploadIsAltNeeded;
    public RadioButton uploadIsCtrlNeeded;
    public VBox root;
    public CheckBox undo;
    public CheckBox redo;

    @FXML
    private ToggleGroup toggleGroup;
    @FXML
    private ToggleGroup toggleGroup1;
    @FXML
    private ToggleGroup readMode;
    @FXML
    private RadioButton pasteOverwrite, pasteAsImage;
    @FXML
    private TextField savePath, exePath, ocrDataPath, captureSavePath, proxyUrl;
    @FXML
    private ComboBox<String> outputSizeMode;
    @FXML
    private TextField outputCustomWidth, outputCustomHeight;
    @FXML
    private Label customWidthLabel, customHeightLabel, customSizePxLabel;
    @FXML
    private ComboBox<String> uploadSizeMode;
    @FXML
    private TextField uploadCustomWidth, uploadCustomHeight;
    @FXML
    private Label uploadWidthLabel, uploadHeightLabel, uploadPxLabel;
    @FXML
    private TextField blankWidth, blankHeight;
    @FXML
    private Label state;
    @FXML
    private RadioButton isShiftNeeded, isAltNeeded, isCtrlNeeded;
    @FXML
    private Button changeKeyBind;
    @FXML
    private ComboBox<String> captureType;
    @FXML
    private VBox setBox;
    @FXML
    private CheckBox enableAll, export, copy, reset, clearHistory, scaleOnMouse, popSetting;
    @FXML
    private CheckBox drag, rubber;
    @FXML
    private CheckBox autoCopy, autoLaunch;
    @FXML
    private ComboBox<String> detectMode;
    private Stage parent;
    public static boolean changing = false;
    private int changedKey = 0;
    private boolean shouldRegister = false;

    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            changeKeyBind.setText(NativeKeyEvent.getKeyText(CaptureProperties.CAPTURE_KEY));
            changeUploadKeyBind.setText(NativeKeyEvent.getKeyText(CaptureProperties.UPLOAD_KEY));
            keys.setContentDisplay(ContentDisplay.CENTER);
            if (setBox.getScene() == null)
                return;
            parent = (Stage) setBox.getScene().getWindow();
            setBox.setPrefHeight(parent.getHeight());
            setBox.setPrefWidth(parent.getWidth());
            initSettings();
            parent.setOnCloseRequest(e -> {
                if (CaptureProperties.configPath != null
                        && !CaptureProperties.configPath.isBlank()
                        && !"configuration.txt".equals(CaptureProperties.configPath)) {
                    CaptureProperties.saveOnOriginalPath();
                }
            });
            captureType.valueProperty().addListener((obj, oldVal, newVal) -> {
                CaptureProperties.captureType = newVal;
            });
            scaleOnMouse.selectedProperty().addListener((obj, oldVal, newVal) -> {
                CaptureProperties.scaleOnMouse = newVal;
            });
            popSetting.selectedProperty().addListener((obj, oldVal, newVal) -> {
                CaptureProperties.showSettings = newVal;
            });
            detectMode.valueProperty().addListener((obj, oldVal, newVal) -> {
                CaptureProperties.detectMode = newVal;
            });
            autoCopy.selectedProperty().addListener((obs, oldVal, newVal) -> {
                CaptureProperties.autoCopy = newVal;
            });
            autoLaunch.selectedProperty().addListener((obs, oldVal, newVal) -> {
                CaptureProperties.autoLaunch = newVal;
            });
            proxyUrl.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (!isFocused) CaptureProperties.proxyUrl = proxyUrl.getText().strip();
            });
            outputSizeMode.valueProperty().addListener((obs, old, newVal) -> {
                if (newVal == null) return;
                CaptureProperties.outputSizeMode = newVal;
                setCustomSizeVisible("自定义".equals(newVal));
            });
            outputCustomWidth.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (!isFocused) {
                    try { CaptureProperties.outputCustomWidth = Integer.parseInt(outputCustomWidth.getText().strip()); }
                    catch (NumberFormatException ignored) {}
                }
            });
            outputCustomHeight.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (!isFocused) {
                    try { CaptureProperties.outputCustomHeight = Integer.parseInt(outputCustomHeight.getText().strip()); }
                    catch (NumberFormatException ignored) {}
                }
            });
            uploadSizeMode.valueProperty().addListener((obs, old, newVal) -> {
                if (newVal == null) return;
                CaptureProperties.uploadSizeMode = newVal;
                setUploadSizeVisible("自定义".equals(newVal));
            });
            uploadCustomWidth.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (!isFocused) {
                    try { CaptureProperties.uploadCustomWidth = Integer.parseInt(uploadCustomWidth.getText().strip()); }
                    catch (NumberFormatException ignored) {}
                }
            });
            uploadCustomHeight.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (!isFocused) {
                    try { CaptureProperties.uploadCustomHeight = Integer.parseInt(uploadCustomHeight.getText().strip()); }
                    catch (NumberFormatException ignored) {}
                }
            });
            blankWidth.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (!isFocused) {
                    try { CaptureProperties.blankImageWidth = Integer.parseInt(blankWidth.getText().strip()); }
                    catch (NumberFormatException ignored) {}
                }
            });
            blankHeight.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (!isFocused) {
                    try { CaptureProperties.blankImageHeight = Integer.parseInt(blankHeight.getText().strip()); }
                    catch (NumberFormatException ignored) {}
                }
            });
            GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
                @Override
                public void nativeKeyPressed(NativeKeyEvent e) {
                    if (!changing) return;
                    int keyCode = e.getKeyCode();
                    // 忽略修饰键自身的按下事件，等待主键
                    if (keyCode == NativeKeyEvent.VC_SHIFT || keyCode == NativeKeyEvent.VC_ALT
                            || keyCode == NativeKeyEvent.CTRL_MASK) return;
                    int modifiers = e.getModifiers();
                    Platform.runLater(() -> {
                        changing = false;
                        if (keyCode == NativeKeyEvent.VC_ESCAPE) {
                            // ESC 取消，恢复按钮原样
                            restoreButton(changedKey);
                            updateState("已取消快捷键设置");
                            return;
                        }
                        boolean shift = (modifiers & NativeKeyEvent.SHIFT_MASK) != 0;
                        boolean ctrl  = (modifiers & NativeKeyEvent.CTRL_MASK)  != 0;
                        boolean alt   = (modifiers & NativeKeyEvent.ALT_MASK)   != 0;
                        String keyText = NativeKeyEvent.getKeyText(keyCode);
                        if (changedKey == 1) {
                            CaptureProperties.CAPTURE_KEY = keyCode;
                            CaptureProperties.isShiftNeeded = shift;
                            CaptureProperties.isCtrlNeeded  = ctrl;
                            CaptureProperties.isAltNeeded   = alt;
                            isShiftNeeded.setSelected(shift);
                            isCtrlNeeded.setSelected(ctrl);
                            isAltNeeded.setSelected(alt);
                            changeKeyBind.setText(keyText);
                            changeKeyBind.setStyle("");
                        } else {
                            CaptureProperties.UPLOAD_KEY = keyCode;
                            CaptureProperties.uploadIsShiftNeeded = shift;
                            CaptureProperties.uploadIsCtrlNeeded  = ctrl;
                            CaptureProperties.uploadIsAltNeeded   = alt;
                            uploadIsShiftNeeded.setSelected(shift);
                            uploadIsCtrlNeeded.setSelected(ctrl);
                            uploadIsAltNeeded.setSelected(alt);
                            changeUploadKeyBind.setText(keyText);
                            changeUploadKeyBind.setStyle("");
                        }
                        String modifier = shift ? "Shift + " : ctrl ? "Ctrl + " : alt ? "Alt + " : "";
                        updateState("快捷键已更新：" + modifier + keyText);
                    });
                }

                @Override
                public void nativeKeyReleased(NativeKeyEvent e) {}

                @Override
                public void nativeKeyTyped(NativeKeyEvent e) {}
            });
        });
    }

    private void restoreButton(int keyType) {
        if (keyType == 1) {
            changeKeyBind.setText(NativeKeyEvent.getKeyText(CaptureProperties.CAPTURE_KEY));
            changeKeyBind.setStyle("");
        } else {
            changeUploadKeyBind.setText(NativeKeyEvent.getKeyText(CaptureProperties.UPLOAD_KEY));
            changeUploadKeyBind.setStyle("");
        }
    }

    private void initSettings() {
        enableAll.setSelected(CaptureProperties.enableAll);
        export.setSelected(CaptureProperties.export);
        copy.setSelected(CaptureProperties.copy);
        undo.setSelected(CaptureProperties.undo);
        redo.setSelected(CaptureProperties.redo);
        copy.setSelected(CaptureProperties.copy);
        reset.setSelected(CaptureProperties.reset);
        clearHistory.setSelected(CaptureProperties.clearHistory);
        drag.setSelected(CaptureProperties.drag);
        rubber.setSelected(CaptureProperties.rubber);
        isShiftNeeded.setSelected(CaptureProperties.isShiftNeeded);
        isAltNeeded.setSelected(CaptureProperties.isAltNeeded);
        isCtrlNeeded.setSelected(CaptureProperties.isCtrlNeeded);
        uploadIsShiftNeeded.setSelected(CaptureProperties.uploadIsShiftNeeded);
        uploadIsAltNeeded.setSelected(CaptureProperties.uploadIsAltNeeded);
        uploadIsCtrlNeeded.setSelected(CaptureProperties.uploadIsCtrlNeeded);
        autoLaunch.setSelected(CaptureProperties.autoLaunch);
        autoCopy.setSelected(CaptureProperties.autoCopy);
        detectMode.setValue(CaptureProperties.detectMode);
        captureType.setValue(CaptureProperties.captureType);
        popSetting.setSelected(CaptureProperties.showSettings);
        savePath.setText(CaptureProperties.configPath);
        changeKeyBind.setText(NativeKeyEvent.getKeyText(CaptureProperties.CAPTURE_KEY));
        exePath.setText(CaptureProperties.exePath);
        ocrDataPath.setText(CaptureProperties.ocrPath);
        logPath.setText(CaptureProperties.logPath);
        captureSavePath.setText(CaptureProperties.captureSavePath);
        proxyUrl.setText(CaptureProperties.proxyUrl);
        pasteOverwrite.setSelected(!CaptureProperties.pasteAsExternalImage);
        pasteAsImage.setSelected(CaptureProperties.pasteAsExternalImage);
        outputSizeMode.setValue(CaptureProperties.outputSizeMode);
        outputCustomWidth.setText(String.valueOf(CaptureProperties.outputCustomWidth));
        outputCustomHeight.setText(String.valueOf(CaptureProperties.outputCustomHeight));
        boolean isCustom = "自定义".equals(CaptureProperties.outputSizeMode);
        setCustomSizeVisible(isCustom);
        uploadSizeMode.setValue(CaptureProperties.uploadSizeMode);
        uploadCustomWidth.setText(String.valueOf(CaptureProperties.uploadCustomWidth));
        uploadCustomHeight.setText(String.valueOf(CaptureProperties.uploadCustomHeight));
        setUploadSizeVisible("自定义".equals(CaptureProperties.uploadSizeMode));
        blankWidth.setText(String.valueOf(CaptureProperties.blankImageWidth));
        blankHeight.setText(String.valueOf(CaptureProperties.blankImageHeight));
    }

    private void setCustomSizeVisible(boolean visible) {
        customWidthLabel.setVisible(visible);
        customWidthLabel.setManaged(visible);
        outputCustomWidth.setVisible(visible);
        outputCustomWidth.setManaged(visible);
        customHeightLabel.setVisible(visible);
        customHeightLabel.setManaged(visible);
        outputCustomHeight.setVisible(visible);
        outputCustomHeight.setManaged(visible);
        customSizePxLabel.setVisible(visible);
        customSizePxLabel.setManaged(visible);
    }

    private void setUploadSizeVisible(boolean visible) {
        uploadWidthLabel.setVisible(visible);
        uploadWidthLabel.setManaged(visible);
        uploadCustomWidth.setVisible(visible);
        uploadCustomWidth.setManaged(visible);
        uploadHeightLabel.setVisible(visible);
        uploadHeightLabel.setManaged(visible);
        uploadCustomHeight.setVisible(visible);
        uploadCustomHeight.setManaged(visible);
        uploadPxLabel.setVisible(visible);
        uploadPxLabel.setManaged(visible);
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
            case "undo" -> {
                CaptureProperties.undo = undo.isSelected();
            }
            case "redo" -> {
                CaptureProperties.redo = redo.isSelected();
            }
            case "rubber" -> {
                CaptureProperties.rubber = rubber.isSelected();
            }
            case "autoCopy" -> {
                CaptureProperties.autoCopy = autoCopy.isSelected();
            }
            case "autoLaunch" -> {
                CaptureProperties.autoLaunch = autoLaunch.isSelected();
            }
            case "isShiftNeeded" -> {
                CaptureProperties.isShiftNeeded = isShiftNeeded.isSelected();
            }
            case "isAltNeeded" -> {
                CaptureProperties.isAltNeeded = isAltNeeded.isSelected();
            }
            case "isCtrlNeeded" -> {
                CaptureProperties.isCtrlNeeded = isCtrlNeeded.isSelected();
            }
            case "uploadIsShiftNeeded" -> {
                CaptureProperties.uploadIsShiftNeeded = uploadIsShiftNeeded.isSelected();
            }
            case "uploadIsAltNeeded" -> {
                CaptureProperties.uploadIsAltNeeded = uploadIsAltNeeded.isSelected();
            }
            case "uploadIsCtrlNeeded" -> {
                CaptureProperties.uploadIsCtrlNeeded = uploadIsCtrlNeeded.isSelected();
            }
            case "scaleOnMouse" -> {
                CaptureProperties.scaleOnMouse = scaleOnMouse.isSelected();
            }
            case "showSettings" -> {
                CaptureProperties.showSettings = popSetting.isSelected();
            }
            case "pasteOverwrite" -> {
                CaptureProperties.pasteAsExternalImage = false;
            }
            case "pasteAsImage" -> {
                CaptureProperties.pasteAsExternalImage = true;
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
        rubber.setSelected(isSelected);
    }

    @FXML
    private void changeKeyBinding() {
        changedKey = 1;
        changing = true;
        changeKeyBind.setText("[ 按下快捷键... ]");
        changeKeyBind.setStyle("-fx-background-color: #ffe082;");
        state.setText("按下新快捷键（可同时按住 Shift / Ctrl / Alt 组合，ESC 取消）");
    }

    @FXML
    private void saveAsFile() {
        if (autoLaunch.isSelected() && !CaptureProperties.autoLaunchEnabled) {
            if (!CaptureProperties.exePath.isBlank() && new File(CaptureProperties.exePath).isFile()) {
                shouldRegister = true;
                CaptureProperties.autoLaunchEnabled = true;
            } else {
                FileChooser fc = new FileChooser();
                fc.setInitialDirectory(CaptureProperties.getSelectDirectory());
                fc.setTitle("选择安装目录下的CaptureTool.exe文件");
                File f = fc.showOpenDialog(parent);
                if (f != null) {
                    CaptureProperties.updateSelectPath(f.getAbsolutePath());
                    CaptureProperties.exePath = f.getAbsolutePath();
                    shouldRegister = true;
                    CaptureProperties.autoLaunchEnabled = true;
                }
            }
        }
        // save settings to file here
        FileChooser chooser = new FileChooser();
        chooser.setInitialDirectory(CaptureProperties.getSelectDirectory());
        chooser.setInitialFileName("config.txt");
        chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("文本文件", "txt"));
        chooser.setTitle("Save configurations as file");
        File file = chooser.showSaveDialog(parent);
        if (file != null) {
            CaptureProperties.selectPath = file.getAbsolutePath();
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
        Platform.runLater(() -> {
            if (shouldRegister) {
                registerReg(Paths.get(new File(CaptureProperties.exePath).getParent(), "runtime", "CaptureTool.vbs").toString(), new File(CaptureProperties.exePath + "/").getParent());
            } else {
                if (!autoLaunch.isSelected()) {
                    unRegisterReg();
                }
            }
            shouldRegister = false;
        });
    }

    @Override
    public String toString() {
        return "ScreenCaptureToolProperties :{" +
                "\n isShiftNeeded=" + isShiftNeeded.isSelected() +
                "\n isAltNeeded=" + isAltNeeded.isSelected() +
                "\n isCtrlNeeded=" + isCtrlNeeded.isSelected() +
                "\n uploadIsShiftNeeded=" + uploadIsShiftNeeded.isSelected() +
                "\n uploadIsAltNeeded=" + uploadIsAltNeeded.isSelected() +
                "\n uploadIsCtrlNeeded=" + uploadIsCtrlNeeded.isSelected() +
                "\n captureType=" + captureType.getValue() +
                "\n enableAll=" + enableAll.isSelected() +
                "\n export=" + export.isSelected() +
                "\n copy=" + copy.isSelected() +
                "\n undo=" + undo.isSelected() +
                "\n redo=" + redo.isSelected() +
                "\n reset=" + reset.isSelected() +
                "\n clearHistory=" + clearHistory.isSelected() +
                "\n drag=" + drag.isSelected() +
                "\n rubber=" + rubber.isSelected() +
                "\n captureKey=" + CaptureProperties.CAPTURE_KEY +
                "\n uploadKey=" + CaptureProperties.UPLOAD_KEY +
                "\n autoCopy=" + autoCopy.isSelected() +
                "\n detectMode=" + detectMode.getValue() +
                "\n selectPath=" + CaptureProperties.selectPath +
                "\n autoLaunch=" + autoLaunch.isSelected() +
                "\n ocrFileInstalled=" + CaptureProperties.ocrFileInstalled +
                "\n exePath=" + exePath.getText() +
                "\n ocrDataPath=" + ocrDataPath.getText() +
                "\n autoLaunchEnabled=" + CaptureProperties.autoLaunchEnabled +
                "\n logPath=" + CaptureProperties.logPath +
                "\n scaleOnMouse=" + CaptureProperties.scaleOnMouse +
                "\n showSettings=" + CaptureProperties.showSettings +
                "\n captureSavePath=" + captureSavePath.getText() +
                "\n lastSaveDir=" + CaptureProperties.lastSaveDir +
                "\n lastUploadDir=" + CaptureProperties.lastUploadDir +
                "\n proxyUrl=" + proxyUrl.getText().strip() +
                "\n pasteAsExternalImage=" + CaptureProperties.pasteAsExternalImage +
                "\n outputSizeMode=" + outputSizeMode.getValue() +
                "\n outputCustomWidth=" + outputCustomWidth.getText().strip() +
                "\n outputCustomHeight=" + outputCustomHeight.getText().strip() +
                "\n uploadSizeMode=" + uploadSizeMode.getValue() +
                "\n uploadCustomWidth=" + uploadCustomWidth.getText().strip() +
                "\n uploadCustomHeight=" + uploadCustomHeight.getText().strip() +
                "\n blankImageWidth=" + blankWidth.getText().strip() +
                "\n blankImageHeight=" + blankHeight.getText().strip() +
                "\n}";
    }

    public void saveOnOriginalPath() {
        if (autoLaunch.isSelected() && !CaptureProperties.autoLaunchEnabled) {
            if (!CaptureProperties.exePath.isBlank() && new File(CaptureProperties.exePath).isFile()) {
                CaptureProperties.autoLaunchEnabled = true;
                shouldRegister = true;
            } else {
                FileChooser fc = new FileChooser();
                fc.setInitialDirectory(CaptureProperties.getSelectDirectory());
                fc.setTitle("选择安装目录下的CaptureTool.exe文件");
                File f = fc.showOpenDialog(parent);
                if (f != null) {
                    CaptureProperties.exePath = f.getAbsolutePath();
                    CaptureProperties.autoLaunchEnabled = true;
                    shouldRegister = true;
                }
            }
        } else {
            if (!autoLaunch.isSelected() && CaptureProperties.autoLaunchEnabled) {
                CaptureProperties.autoLaunchEnabled = false;
                shouldRegister = false;
            }
        }
        File f = new File(savePath.getText());
        f.setWritable(true);
        try (FileWriter writer1 = new FileWriter(f)) {
            writer1.write(toString());
            updateState("configuration saved successfully!");
            f.setWritable(false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Platform.runLater(() -> {
            if (shouldRegister) {
                registerReg(Paths.get(new File(CaptureProperties.exePath).getParent(), "runtime", "CaptureTool.vbs").toString()
                        , new File(CaptureProperties.exePath).getParent());
            } else {
                if (!autoLaunch.isSelected()) {
                    unRegisterReg();
                }
            }
            shouldRegister = false;
        });
    }

    public void updateState(String value) {
        Platform.runLater(() -> {
            state.setText(value);
        });
    }


    /**
     * @param path : 开机启动执行的文件
     * @param temp : exe文件父级路径
     **/
    public void registerReg(String path, String temp) {
        //生成bat和vbs文件
        //bat文件直接添加到./runtime
        //vbs文件替换${batPath}后存储到./runtime
        Path batPath = Paths.get(temp, "runtime", "CaptureTool.bat");
        try (InputStream inputStream = ScreenCaptureToolApp.class.getResourceAsStream("CaptureTool.bat")) {
            if (inputStream != null) {
                Files.createDirectories(batPath.getParent()); // 确保目录存在
                Files.copy(inputStream, batPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            ScreenCaptureToolApp.LOGGER.error("write bat file failed,", e);
            throw new RuntimeException(e);
        }
        Path vbsPath = Paths.get(temp, "runtime", "CaptureTool.vbs");
        try (InputStream inputStream = ScreenCaptureToolApp.class.getResourceAsStream("CaptureTool.vbs")) {
            if (inputStream != null) {
                Files.createDirectories(batPath.getParent()); // 确保目录存在
                Files.copy(inputStream, vbsPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            ScreenCaptureToolApp.LOGGER.error("write vbs file failed,", e);
            throw new RuntimeException(e);
        }
        // wscript  "D:\\CaptureTool\\runtime\\CaptureTool.vbs"
        path = path.replace("\\", "\\\\");
        path = "wscript \\\"" + path + "\\\"";
        String regContent = String.format("""
                Windows Registry Editor Version 5.00
                [HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Run]
                "%s"="%s"
                """, "CaptureTool", path);
        temp += "/autoLaunch.reg";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(temp))) {
            writer.write(regContent);
        } catch (IOException e) {
            if (CaptureProperties.shouldLog && CaptureProperties.logPath != null) {
                try (PrintWriter pw = new PrintWriter(new FileWriter(CaptureProperties.logPath, true), true)) {
                    e.printStackTrace(pw);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            e.printStackTrace();
            return;
        }
        // 导入 .reg 文件
        try {
            String command = String.format(
                    "powershell -Command \"Start-Process regedit.exe -ArgumentList '/s', '%s' -Verb RunAs\"",
                    temp
            );
            // 执行命令
            Process process = Runtime.getRuntime().exec(command);
            // 等待命令完成
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Registry updated successfully.");
            } else {
                System.err.println("Failed to update registry. Exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            if (CaptureProperties.shouldLog && CaptureProperties.logPath != null) {
                try (PrintWriter pw = new PrintWriter(new FileWriter(CaptureProperties.logPath, true), true)) {
                    e.printStackTrace(pw);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            e.printStackTrace();
        }
    }

    public void unRegisterReg() {
        try {
            // 执行删除注册表键值的命令
            String command = "reg delete \"HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Run\" /v \"CaptureTool\" /f";
            Process process = Runtime.getRuntime().exec(command);

            // 等待命令执行完成
            process.waitFor();
            System.out.println("注册表键值已删除");
        } catch (IOException | InterruptedException e) {
            if (CaptureProperties.shouldLog && CaptureProperties.logPath != null) {
                try (PrintWriter pw = new PrintWriter(new FileWriter(CaptureProperties.logPath, true), true)) {
                    e.printStackTrace(pw);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            e.printStackTrace();
        }
    }

    public void locateExePath() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("选择EXE文件安装目录");
        chooser.setInitialDirectory(new File("D:/"));
        File file = chooser.showOpenDialog(null);
        if (file != null) {
            CaptureProperties.exePath = file.getAbsolutePath();
            exePath.setText(file.getAbsolutePath());
            String exeParent = file.getParent();
            CaptureProperties.logPath = exeParent + "/logs/CaptureTool.log";
            logPath.setText(CaptureProperties.logPath);
            CaptureProperties.captureSavePath = exeParent + "/captures";
            captureSavePath.setText(CaptureProperties.captureSavePath);
            state.setText("EXE文件目录更改成功，已自动配置日志目录与截图保存目录");
        }
    }

    public void locateCaptureSavePath() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("选择截图保存目录");
        chooser.setInitialDirectory(CaptureProperties.getSelectDirectory());
        File file = chooser.showDialog(null);
        if (file != null) {
            CaptureProperties.captureSavePath = file.getAbsolutePath();
            captureSavePath.setText(CaptureProperties.captureSavePath);
            state.setText("截图保存目录更改成功");
        }
    }

    public void locateOCRPath() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("选择tessdata目录");
        chooser.setInitialDirectory(CaptureProperties.getSelectDirectory());
        File file = chooser.showDialog(null);
        if (file != null) {
            File[] trainedData = file.listFiles(f -> f.getName().endsWith(".traineddata"));
            if (trainedData == null || trainedData.length == 0) {
                state.setText("所选目录下未找到 .traineddata 文件，请确认选择正确的 tessdata 目录");
                return;
            }
            CaptureProperties.ocrPath = file.getAbsolutePath();
            CaptureProperties.ocrFileInstalled = true;
            ocrDataPath.setText(file.getAbsolutePath());
            state.setText("OCR数据目录设置成功（找到 " + trainedData.length + " 个语言文件）");
        }
    }

    public void downloadOcrData() {
        String destRoot;
        if (!CaptureProperties.exePath.isBlank()) {
            destRoot = new File(CaptureProperties.exePath).getParentFile().getAbsolutePath();
        } else {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("选择 OCR 数据下载目录");
            chooser.setInitialDirectory(CaptureProperties.getSelectDirectory());
            File f = chooser.showDialog(parent);
            if (f == null) return;
            destRoot = f.getAbsolutePath();
        }
        String finalDestRoot = destRoot;
        String url = "https://github.com/lyxnd/temp/archive/refs/heads/main.zip";
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Path zipPath = Path.of(finalDestRoot, "tessdata.zip");
                HttpRequestHandler.downloadFile(url, zipPath);
                // unzip 内部会在 destRoot 后追加 /tessdata，提取后结构为 tessdata/temp-main/
                FileHandler.unzip(zipPath.toString(), finalDestRoot);
                Path tempMain = Path.of(finalDestRoot, "tessdata", "temp-main");
                Path tessdata = Path.of(finalDestRoot, "tessdata");
                if (Files.exists(tempMain)) {
                    FileHandler.moveToAnotherDirectory(tempMain, tessdata);
                    // 清理临时子目录
                    Files.walk(tempMain)
                            .sorted(Comparator.reverseOrder())
                            .forEach(p -> { try { Files.delete(p); } catch (IOException ignored) {} });
                }
                return null;
            }
        };
        String tessDataPath = Path.of(destRoot, "tessdata").toString();
        ProgressStage progressStage = new ProgressStage("下载/更新 OCR 数据", parent, task);
        progressStage.setOcrPath(tessDataPath);
        // ProgressStage 关闭后（成功或取消）同步更新设置界面 UI
        progressStage.setOnHidden(e -> Platform.runLater(() -> {
            ocrDataPath.setText(CaptureProperties.ocrPath != null ? CaptureProperties.ocrPath : "");
            if (CaptureProperties.ocrFileInstalled) {
                state.setText("OCR 数据下载/更新完成");
            }
        }));
        progressStage.show();
        progressStage.startTask();
    }

    public void openConfigFile() throws IOException {
        File f = new File(CaptureProperties.configPath);
        if (f.exists()) {
            Desktop.getDesktop().open(f);
        }
    }

    public void reloadConfig() throws IOException {
        CaptureProperties.loadProperties();
        state.setText("config reload successfully");
        initSettings();
    }

    public void changeLogPath() throws IOException {
        DirectoryChooser fc = new DirectoryChooser();
        fc.setInitialDirectory(CaptureProperties.getSelectDirectory());
        File file = fc.showDialog(null);
        if (file != null) {
            CaptureProperties.logPath = file.toPath().resolve("logs").resolve("CaptureTool.log").toString();
            logPath.setText(CaptureProperties.logPath);
            File logFile = CaptureProperties.getLogPath();
            LoggerContext context = (LoggerContext) LogManager.getContext(false);
            context.setConfigLocation(logFile.toURI());
        }
    }
    public void clearLogs() throws IOException {
        File file = new File(logPath.getText());
        if (file.exists()) {
            new FileOutputStream(file).close();
        }
    }
    public void exitProgram(){
        Platform.exit();
        System.exit(0);
    }

    public void restartProgram() {
        ScreenCaptureToolApp.restartProgram();
    }

    public void changeUploadKeyBinding() {
        changedKey = 2;
        changing = true;
        changeUploadKeyBind.setText("[ 按下快捷键... ]");
        changeUploadKeyBind.setStyle("-fx-background-color: #ffe082;");
        state.setText("按下新快捷键（可同时按住 Shift / Ctrl / Alt 组合，ESC 取消）");
    }
}
