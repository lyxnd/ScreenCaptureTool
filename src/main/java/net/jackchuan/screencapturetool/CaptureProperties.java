package net.jackchuan.screencapturetool;

import javafx.concurrent.Task;
import javafx.stage.DirectoryChooser;
import net.jackchuan.screencapturetool.entity.ControllerInstance;
import net.jackchuan.screencapturetool.external.stage.AlertHelper;
import net.jackchuan.screencapturetool.external.stage.ProgressStage;
import net.jackchuan.screencapturetool.network.HttpRequestHandler;
import net.jackchuan.screencapturetool.util.FileHandler;
import net.jackchuan.screencapturetool.util.ScreenCaptureUtil;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/11/23 23:43
 */
public class CaptureProperties {
    public static boolean enableAll;
    public static boolean export;
    public static boolean copy;
    public static boolean reset;
    public static boolean clearHistory;
    public static boolean drag;
    public static boolean rubber;
    public static boolean undo;
    public static boolean redo;
    public static String captureType="Python's pillow";
    public static int CAPTURE_KEY;
    public static boolean isShiftNeeded=true;
    public static boolean isAltNeeded=false;
    public static boolean isCtrlNeeded=false;
    public static boolean autoCopy;
    public static boolean autoSelect;
    public static boolean autoLaunch;
    public static boolean shouldLog=true;
    public static boolean autoLaunchEnabled;
    public static boolean scaleOnMouse=false;
    public static String configPath;
    public static String selectPath="D:/";
    public static String exePath="";
    public static String logPath="F:/captureToolLog.txt";
    public static double scale;
    public static double width;
    public static double height;
    public static boolean ocrFileInstalled=false;
    public static String ocrPath;
    public static boolean showSettings=true;

    static {
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        scale= ScreenCaptureUtil.getScreenScale();
        width=size.getWidth();
        height=size.getHeight();
    }

    public static void updateAll(boolean flag){
        enableAll = flag;
        export = flag;
        copy = flag;
        reset = flag;
        clearHistory = flag;
        drag = flag;
        rubber = flag;
        undo = flag;
        redo = flag;
    }

    public static boolean loadProperties() throws IOException {
        File file=new File(System.getProperty("user.home")+"/captureToolConfig.txt");
        if(file.exists()){
            try(BufferedReader reader=new BufferedReader(new FileReader(file))){
                configPath = reader.readLine();
                if("configuration.txt".equals(configPath)){
                    readFromJar();
                }else {
                    File configFile=new File(configPath);
                    List<String> list = Files.readAllLines(configFile.toPath());
                    for (String line :list){
                        if (line.contains("=")) {
                            String[] pair = line.split("=");
                            boolean blank = false;
                            for(String s:pair){
                                if(s.isBlank()){
                                    blank=true;
                                    break;
                                }
                            }
                            if(!blank&&pair.length==2){
                                updateSettings(pair[0].trim(),pair[1].trim());
                            }

                        }
                    }
                }
            } catch (IOException e) {
                readFromJar();
            }
            return true;
        }else {
            return false;
        }
    }

    private static void readFromJar() throws IOException {
        configPath="configuration.txt";
        InputStream inputStream = ScreenCaptureToolApp.class.getResourceAsStream(configPath);
        if (inputStream == null) {
            throw new FileNotFoundException("Resource not found: " + configPath);
        }
        // 使用 BufferedReader 和 Stream API 读取文件内容
        try (BufferedReader reader1 = new BufferedReader(new InputStreamReader(inputStream))) {
            List<String> lines = reader1.lines().toList();
            for (String line : lines) {
                if (line.contains("=")) {
                    String[] pair = line.split("=");
                    updateSettings(pair[0].trim(), pair[1].trim());
                }
            }
        }
    }
    private static void updateSettings(String key,String value) {
        switch (key) {
            case "enableAll" -> {
                enableAll = Boolean.parseBoolean(value);
                updateAll(enableAll);
            }
            case "export" -> {
                export = Boolean.parseBoolean(value);
            }
            case "copy" -> {
                copy = Boolean.parseBoolean(value);
            }
            case "reset" -> {
                reset = Boolean.parseBoolean(value);
            }
            case "clearHistory" -> {
                clearHistory = Boolean.parseBoolean(value);
            }
            case "drag" -> {
                drag = Boolean.parseBoolean(value);
            }
            case "rubber" -> {
                rubber = Boolean.parseBoolean(value);
            }
            case "undo" -> {
                undo = Boolean.parseBoolean(value);
            }
            case "redo" -> {
                redo = Boolean.parseBoolean(value);
            }
            case "captureType"->{
                captureType=value;
            }
            case "captureKey"->{
                CAPTURE_KEY=Integer.parseInt(value);
            }
            case "autoCopy"->{
                autoCopy= Boolean.parseBoolean(value);
            }
            case "autoSelect"->{
                autoSelect= Boolean.parseBoolean(value);
            }
            case "isShiftNeeded"->{
                isShiftNeeded= Boolean.parseBoolean(value);
            }
            case "isAltNeeded"->{
                isAltNeeded= Boolean.parseBoolean(value);
            }
            case "isCtrlNeeded"->{
                isCtrlNeeded= Boolean.parseBoolean(value);
            }
            case "selectPath"->{
                selectPath= value;
            }
            case "autoLaunch"->{
                autoLaunch= Boolean.parseBoolean(value);
            }
            case "exePath"->{
                exePath= value;
            }
            case "autoLaunchEnabled"->{
                autoLaunchEnabled= Boolean.parseBoolean(value);
            }
            case "logPath"->{
                logPath= value;
            }
            case "ocrDataPath"->{
                ocrPath= value;
            }
            case "ocrFileInstalled"->{
                ocrFileInstalled= Boolean.parseBoolean(value);
            }
            case "showSettings"->{
                showSettings= Boolean.parseBoolean(value);
            }
        }

    }

    public static void updateSelectPath(String path){
        selectPath=path;
        File f=new File(configPath);
        f.setWritable(true);
        try (FileWriter writer1 = new FileWriter(f)) {
            writer1.write(toConfigString());
            f.setWritable(false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static File getSelectDirectory(){
        File f=new File(selectPath);
        if(f.isDirectory())
            return f;
        else
            return new File(f.getParent());
    }

    public static void checkFile() throws IOException {
        File file=new File(System.getProperty("user.home")+"/captureToolConfig.txt");
        if(!file.exists()){
            file.createNewFile();
            try(FileWriter writer=new FileWriter(file)){
                writer.write("configuration.txt");
            }
        }
    }
    public static String toConfigString() {
        return "ScreenCaptureToolProperties :{" +
                "\n isShiftNeeded=" + isShiftNeeded +
                "\n isAltNeeded=" + isAltNeeded +
                "\n isCtrlNeeded=" + isCtrlNeeded +
                "\n captureType=" + captureType +
                "\n enableAll=" + enableAll +
                "\n export=" + export +
                "\n copy=" + copy +
                "\n reset=" + reset +
                "\n clearHistory=" + clearHistory +
                "\n drag=" + drag +
                "\n rubber=" + rubber +
                "\n undo=" + undo +
                "\n redo=" + redo +
                "\n captureKey=" + CaptureProperties.CAPTURE_KEY +
                "\n autoCopy=" + autoCopy +
                "\n autoSelect=" + autoSelect +
                "\n selectPath=" + selectPath +
                "\n autoLaunch=" + autoLaunch +
                "\n exePath=" + exePath +
                "\n autoLaunchEnabled=" + autoLaunchEnabled +
                "\n logPath=" + logPath +
                "\n scaleOnMouse=" + scaleOnMouse +
                "\n ocrFileInstalled=" + ocrFileInstalled +
                "\n ocrDataPath=" + ocrPath +
                "\n}";
    }
    public static void saveOnOriginalPath() {
        File f = new File(configPath);
        f.setWritable(true);
        try (FileWriter writer1 = new FileWriter(f)) {
            writer1.write(toConfigString());
            f.setWritable(false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static boolean checkOCR() throws IOException {
        //TODO install ocr
        if(ocrFileInstalled){
            return true;
        }else {
            if(!AlertHelper.showConfirmAlert("Library error","Could not find tessdata files",
                    "Click ok to install tessdata files")){
                return false;
            }
            if(!exePath.isBlank()){
                String url="https://github.com/lyxnd/temp/archive/refs/heads/main.zip";
                Task<Void> taskThread = new Task<>() {
                    @Override
                    protected Void call() throws IOException, NoSuchAlgorithmException, KeyManagementException {
                        String path = HttpRequestHandler.downloadFile(url, new File(exePath).getParent()+"/tessdata.zip");
                        FileHandler.unzip(path,new File(exePath).getParent());
                        Path path1=Path.of(new File(exePath).getParent(),"tessdata","temp-main");
                        Path path2=Path.of(new File(exePath).getParent(),"tessdata");
                        FileHandler.moveToAnotherDirectory(path1,path2);
                        return null;
                    }
                };
                ProgressStage progressStage=new ProgressStage("下载进度",
                        ControllerInstance.getInstance().getController().getParent(),taskThread);
                progressStage.setOcrPath(new File(exePath).getParent()+"/tessdata");
                progressStage.show();
                progressStage.startTask();
            }else{
                DirectoryChooser chooser=new DirectoryChooser();
                chooser.setTitle("选择下载保存路径");
                chooser.setInitialDirectory(new File("D:/"));
                File file = chooser.showDialog(null);
                if(file!=null){
                    String url="https://github.com/lyxnd/temp/archive/refs/heads/main.zip";
                    Task<Void> taskThread = new Task<>() {
                        @Override
                        protected Void call() throws IOException, NoSuchAlgorithmException, KeyManagementException {
                            String path = HttpRequestHandler.downloadFile(url, file.getAbsolutePath()+"/tessdata.zip");
                            FileHandler.unzip(path,file.getAbsolutePath()+"/tessdata");
                            Path path1= Path.of(file.getAbsolutePath(),"tessdata","temp-main");
                            Path path2=Path.of(file.getAbsolutePath(),"tessdata");
                            FileHandler.moveToAnotherDirectory(path1,path2);
                            return null;
                        }
                    };
                    ProgressStage progressStage=new ProgressStage("下载进度",
                            ControllerInstance.getInstance().getController().getParent(),taskThread);
                    progressStage.show();
                    progressStage.setOcrPath(file.getAbsolutePath()+"/tessdata");
                    progressStage.startTask();
                }
            }
        }
        return true;
    }
}
