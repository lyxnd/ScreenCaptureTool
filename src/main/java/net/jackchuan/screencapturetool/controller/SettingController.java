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
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.jackchuan.screencapturetool.CaptureProperties;
import net.jackchuan.screencapturetool.util.FileHandler;

import java.awt.*;
import java.io.*;
import java.nio.file.Path;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/11/23 19:17
 */
public class SettingController {
    public TitledPane keys;
    @FXML
    private TextField savePath,exePath,ocrDataPath;
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
    private CheckBox enableAll, export, copy, reset, clearHistory,scaleOnMouse,popSetting;
    @FXML
    private CheckBox drag,rubber;
    @FXML
    private CheckBox autoCopy, autoSelect, autoLaunch;
    private Stage parent;
    private boolean changing = false;
    private boolean shouldRegister = false;

    @FXML
    private void initialize() {
        Platform.runLater(() -> {
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
                CaptureProperties.scaleOnMouse=newVal;
            });
            autoSelect.selectedProperty().addListener((obj, oldVal, newVal) -> {
                CaptureProperties.autoSelect=newVal;
            });
            popSetting.selectedProperty().addListener((obj, oldVal, newVal) -> {
                CaptureProperties.showSettings=newVal;
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
        rubber.setSelected(CaptureProperties.rubber);
        isShiftNeeded.setSelected(CaptureProperties.isShiftNeeded);
        isAltNeeded.setSelected(CaptureProperties.isAltNeeded);
        isCtrlNeeded.setSelected(CaptureProperties.isCtrlNeeded);
        autoLaunch.setSelected(CaptureProperties.autoLaunch);
        autoCopy.setSelected(CaptureProperties.autoCopy);
        autoSelect.setSelected(CaptureProperties.autoSelect);
        captureType.setValue(CaptureProperties.captureType);
        popSetting.setSelected(CaptureProperties.showSettings);
        savePath.setText(CaptureProperties.configPath);
        changeKeyBind.setText(NativeKeyEvent.getKeyText(CaptureProperties.CAPTURE_KEY));
        exePath.setText(CaptureProperties.exePath);
        ocrDataPath.setText(CaptureProperties.ocrPath);
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
            case "rubber" -> {
                CaptureProperties.rubber = rubber.isSelected();
            }
            case "autoCopy" -> {
                CaptureProperties.autoCopy = autoCopy.isSelected();
            }
            case "autoSelect" -> {
                CaptureProperties.autoSelect = autoSelect.isSelected();
            }
            case "isShiftNeeded"->{
                CaptureProperties.isShiftNeeded = isShiftNeeded.isSelected();
            }
            case "isAltNeeded"->{
                CaptureProperties.isAltNeeded = isAltNeeded.isSelected();
            }
            case "isCtrlNeeded"->{
                CaptureProperties.isCtrlNeeded = isCtrlNeeded.isSelected();
            }
            case "scaleOnMouse"->{
                CaptureProperties.scaleOnMouse = scaleOnMouse.isSelected();
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
        state.setText("请输入快捷键(仅支持最多双按键，勾选三个辅助按键之一的一个，然后输入一个快捷键即可)");
    }

    @FXML
    private void saveAsFile() {
        if (autoLaunch.isSelected() && !CaptureProperties.autoLaunchEnabled) {
            if (!CaptureProperties.exePath.isBlank()&&!new File(CaptureProperties.exePath).isFile()) {
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
            CaptureProperties.updateSelectPath(file.getAbsolutePath());
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
                registerReg(CaptureProperties.exePath, new File(CaptureProperties.exePath).getParent());
            } else {
                if(!autoLaunch.isSelected()){
                    unRegisterReg();
                }
            }
            shouldRegister=false;
        });
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
                "\n rubber=" + rubber.isSelected() +
                "\n captureKey=" + CaptureProperties.CAPTURE_KEY +
                "\n autoCopy=" + autoCopy.isSelected() +
                "\n autoSelect=" + autoSelect.isSelected() +
                "\n selectPath=" + CaptureProperties.selectPath +
                "\n autoLaunch=" + autoLaunch.isSelected() +
                "\n ocrFileInstalled=" + CaptureProperties.ocrFileInstalled +
                "\n exePath=" + exePath.getText()+
                "\n ocrDataPath=" + ocrDataPath.getText() +
                "\n autoLaunchEnabled=" + CaptureProperties.autoLaunchEnabled +
                "\n logPath=" + CaptureProperties.logPath +
                "\n scaleOnMouse=" + CaptureProperties.scaleOnMouse +
                "\n showSettings=" + CaptureProperties.showSettings +
                "\n}";
    }

    public void saveOnOriginalPath() {
        if (autoLaunch.isSelected() && !CaptureProperties.autoLaunchEnabled) {
            if (!CaptureProperties.exePath.isBlank()&&!new File(CaptureProperties.exePath).isFile()) {
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
                registerReg(CaptureProperties.exePath, new File(CaptureProperties.exePath).getParent());
            } else {
                if(!autoLaunch.isSelected()){
                    unRegisterReg();
                }
            }
            shouldRegister=false;
        });
    }

    public void updateState(String value) {
        Platform.runLater(() -> {
            state.setText(value);
        });
    }

    public void registerReg(String path, String temp) {
        path = path.replace("\\", "\\\\");
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
        FileChooser chooser=new FileChooser();
        chooser.setTitle("选择EXE文件安装目录");
        chooser.setInitialDirectory(new File("D:/"));
        File file = chooser.showOpenDialog(null);
        if (file!= null) {
            exePath.setText(file.getAbsolutePath());
            state.setText("EXE文件目录更改成功");
            CaptureProperties.exePath=file.getAbsolutePath();
        }
    }

    public void locateOCRPath() {
        DirectoryChooser chooser=new DirectoryChooser();
        chooser.setTitle("选择OCR数据安装目录");
        chooser.setInitialDirectory(new File("D:/"));
        File file = chooser.showDialog(null);
        if (file!= null) {
            ocrDataPath.setText(file.getAbsolutePath());
            state.setText("OCR数据安装目录更改成功");
            CaptureProperties.ocrPath=file.getAbsolutePath();
        }
    }

    public void unzipFromLocal() throws IOException {
        FileChooser fc=new FileChooser();
        fc.setTitle("选择数据zip文件");
        fc.setInitialFileName("tessdata.zip");
        fc.setInitialDirectory(CaptureProperties.getSelectDirectory());
        File file = fc.showOpenDialog(null);
        if(file!=null){
            if(file.getName().endsWith(".zip")){
                //需要解压
                if (!CaptureProperties.exePath.isBlank()) {
                    File f=new File(CaptureProperties.exePath);
                    FileHandler.unzip(file.getPath(), f.getParentFile().getAbsolutePath());
                    Path path1=Path.of(f.getParent(),"tessdata","temp-main");
                    Path path2=Path.of(f.getParent(),"tessdata");
                    FileHandler.moveToAnotherDirectory(path1,path2);
                    CaptureProperties.ocrPath=new File(CaptureProperties.exePath).getParentFile().getAbsolutePath()+"/tessdata";
                } else {
                    DirectoryChooser chooser = new DirectoryChooser();
                    chooser.setTitle("选择解压目录");
                    chooser.setInitialDirectory(CaptureProperties.getSelectDirectory());
                    File f = chooser.showDialog(null);
                    if (f != null) {
                        FileHandler.unzip(file.getAbsolutePath(), f.getAbsolutePath());
                        Path path1=Path.of(f.getAbsolutePath(),"tessdata","temp-main");
                        Path path2=Path.of(f.getAbsolutePath(),"tessdata");
                        FileHandler.moveToAnotherDirectory(path1,path2);
                        CaptureProperties.ocrPath=f.getAbsolutePath()+"/tessdata";
                    }
                }
            }else{
                //直接设置目录
                CaptureProperties.ocrPath=file.getParent()+"/tessdata";
            }
            ocrDataPath.setText(CaptureProperties.ocrPath);
            CaptureProperties.ocrFileInstalled=true;
            state.setText("解压成功，OCR数据路径设置完成");
            saveOnOriginalPath();
        }
    }

    public void openConfigFile() throws IOException {
        File f=new File(CaptureProperties.configPath);
        if(f.exists()){
            Desktop.getDesktop().open(f);
        }
    }

    public void reloadConfig() throws IOException {
        CaptureProperties.loadProperties();
        state.setText("config reload successfully");
        initSettings();
    }
}
