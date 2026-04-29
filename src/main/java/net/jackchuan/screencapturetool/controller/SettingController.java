package net.jackchuan.screencapturetool.controller;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javafx.application.Platform;
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
    public CheckBox paste;
    @FXML
    private ToggleGroup toggleGroup;
    @FXML
    private ToggleGroup toggleGroup1;
    @FXML
    private TextField savePath, exePath, ocrDataPath, captureSavePath;
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
    private CheckBox autoCopy, autoSelect, autoLaunch;
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
//                saveOnOriginalPath();
            });
            captureType.valueProperty().addListener((obj, oldVal, newVal) -> {
                CaptureProperties.captureType = newVal;
            });
            scaleOnMouse.selectedProperty().addListener((obj, oldVal, newVal) -> {
                CaptureProperties.scaleOnMouse = newVal;
            });
            autoSelect.selectedProperty().addListener((obj, oldVal, newVal) -> {
                CaptureProperties.autoSelect = newVal;
            });
            popSetting.selectedProperty().addListener((obj, oldVal, newVal) -> {
                CaptureProperties.showSettings = newVal;
            });
            GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
                @Override
                public void nativeKeyPressed(NativeKeyEvent e) {
                    if (changing && e.getKeyCode() != NativeKeyEvent.VC_SHIFT &&
                            e.getKeyCode() != NativeKeyEvent.VC_ALT && e.getKeyCode() != NativeKeyEvent.CTRL_MASK) {

                        Platform.runLater(() -> {
                            if (changedKey == 1) {
                                CaptureProperties.CAPTURE_KEY = e.getKeyCode();
                                CaptureProperties.isShiftNeeded = isShiftNeeded.isSelected();
                                CaptureProperties.isAltNeeded = isAltNeeded.isSelected();
                                CaptureProperties.isCtrlNeeded = isCtrlNeeded.isSelected();
                                changeKeyBind.setText(NativeKeyEvent.getKeyText(CaptureProperties.CAPTURE_KEY));
                            } else if(changedKey==2){
                                CaptureProperties.UPLOAD_KEY = e.getKeyCode();
                                CaptureProperties.uploadIsShiftNeeded = uploadIsShiftNeeded.isSelected();
                                CaptureProperties.uploadIsAltNeeded = uploadIsAltNeeded.isSelected();
                                CaptureProperties.uploadIsCtrlNeeded = uploadIsCtrlNeeded.isSelected();
                                changeUploadKeyBind.setText(NativeKeyEvent.getKeyText(CaptureProperties.UPLOAD_KEY));
                            }
                            changing = false;
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
        autoSelect.setSelected(CaptureProperties.autoSelect);
        captureType.setValue(CaptureProperties.captureType);
        popSetting.setSelected(CaptureProperties.showSettings);
        savePath.setText(CaptureProperties.configPath);
        changeKeyBind.setText(NativeKeyEvent.getKeyText(CaptureProperties.CAPTURE_KEY));
        exePath.setText(CaptureProperties.exePath);
        ocrDataPath.setText(CaptureProperties.ocrPath);
        logPath.setText(CaptureProperties.logPath);
        captureSavePath.setText(CaptureProperties.captureSavePath);
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
            case "paste" -> {
                CaptureProperties.paste = paste.isSelected();
            }
            case "rubber" -> {
                CaptureProperties.rubber = rubber.isSelected();
            }
            case "autoCopy" -> {
                CaptureProperties.autoCopy = autoCopy.isSelected();
            }
            case "autoSelect" -> {
                CaptureProperties.autoSelect = autoSelect.isSelected();
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
        changing = true;
        //截图快捷键
        changedKey = 1;
        state.setText("请输入截图快捷键(仅支持最多双按键，勾选三个辅助按键之一的一个，然后输入一个快捷键即可)");
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
                "\n paste=" + paste.isSelected() +
                "\n undo=" + undo.isSelected() +
                "\n redo=" + redo.isSelected() +
                "\n reset=" + reset.isSelected() +
                "\n clearHistory=" + clearHistory.isSelected() +
                "\n drag=" + drag.isSelected() +
                "\n rubber=" + rubber.isSelected() +
                "\n captureKey=" + CaptureProperties.CAPTURE_KEY +
                "\n uploadKey=" + CaptureProperties.UPLOAD_KEY +
                "\n autoCopy=" + autoCopy.isSelected() +
                "\n autoSelect=" + autoSelect.isSelected() +
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
        path = "wscript \"" + path + "\"";
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
        chooser.setTitle("选择OCR数据安装目录");
        chooser.setInitialDirectory(new File("D:/"));
        File file = chooser.showDialog(null);
        if (file != null) {
            ocrDataPath.setText(file.getAbsolutePath());
            state.setText("OCR数据安装目录更改成功");
            CaptureProperties.ocrPath = file.getAbsolutePath();
        }
    }

    public void unzipFromLocal() throws IOException {
        FileChooser fc = new FileChooser();
        fc.setTitle("选择数据zip文件");
        fc.setInitialFileName("tessdata.zip");
        fc.setInitialDirectory(CaptureProperties.getSelectDirectory());
        File file = fc.showOpenDialog(null);
        if (file != null) {
            if (file.getName().endsWith(".zip")) {
                //需要解压
                if (!CaptureProperties.exePath.isBlank()) {
                    File f = new File(CaptureProperties.exePath);
                    FileHandler.unzip(file.getPath(), f.getParentFile().getAbsolutePath());
                    Path path1 = Path.of(f.getParent(), "tessdata", "temp-main");
                    Path path2 = Path.of(f.getParent(), "tessdata");
                    FileHandler.moveToAnotherDirectory(path1, path2);
                    CaptureProperties.ocrPath = new File(CaptureProperties.exePath).getParentFile().getAbsolutePath() + "/tessdata";
                } else {
                    DirectoryChooser chooser = new DirectoryChooser();
                    chooser.setTitle("选择解压目录");
                    chooser.setInitialDirectory(CaptureProperties.getSelectDirectory());
                    File f = chooser.showDialog(null);
                    if (f != null) {
                        FileHandler.unzip(file.getAbsolutePath(), f.getAbsolutePath());
                        Path path1 = Path.of(f.getAbsolutePath(), "tessdata", "temp-main");
                        Path path2 = Path.of(f.getAbsolutePath(), "tessdata");
                        FileHandler.moveToAnotherDirectory(path1, path2);
                        CaptureProperties.ocrPath = f.getAbsolutePath() + "/tessdata";
                    }
                }
            } else {
                //直接设置目录
                CaptureProperties.ocrPath = file.getParent() + "/tessdata";
            }
            ocrDataPath.setText(CaptureProperties.ocrPath);
            CaptureProperties.ocrFileInstalled = true;
            state.setText("解压成功，OCR数据路径设置完成");
            saveOnOriginalPath();
        }
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

    public void changeUploadKeyBinding() {
        changing = true;
        //截图快捷键
        changedKey = 2;
        state.setText("请输入上传图片的快捷键(仅支持最多双按键，勾选三个辅助按键之一的一个，然后输入一个快捷键即可)");
    }
}
