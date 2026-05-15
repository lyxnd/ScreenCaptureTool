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
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;

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
    public static String captureType="java awt Robot";
    public static int CAPTURE_KEY=88;
    public static int UPLOAD_KEY=59;
    public static boolean isShiftNeeded=false;
    public static boolean isAltNeeded=false;
    public static boolean isCtrlNeeded=false;
    public static boolean uploadIsShiftNeeded=false;
    public static boolean uploadIsAltNeeded=false;
    public static boolean uploadIsCtrlNeeded=false;
    public static boolean autoCopy;
    public static boolean autoLaunch;
    public static String detectMode = "窗口检测";
    public static boolean shouldLog=true;
    public static boolean autoLaunchEnabled;
    public static boolean scaleOnMouse=false;
    public static String configPath;
    public static String selectPath="D:/";
    public static String exePath="";
    public static String logPath="D:/captureToolLog.log";
    public static boolean ocrFileInstalled=false;
    public static String ocrPath;
    public static boolean showSettings=true;
    public static String captureSavePath="";
    public static String lastSaveDir="";
    public static String lastUploadDir="";
    // 代理地址，格式：http://host:port 或 socks://host:port，留空则使用系统代理
    public static String proxyUrl="";
    // false=覆盖原图，true=作为外部图片插入
    public static boolean pasteAsExternalImage = false;
    public static String dataUrl="https://pan.baidu.com/s/1l5wH3_-MT9ivvIth3Pzevg?pwd=1234";
    // 输出尺寸模式：原图尺寸 / 50% / 75% / 150% / 200% / 自定义
    public static String outputSizeMode = "原图尺寸";
    public static int outputCustomWidth = 1920;
    public static int outputCustomHeight = 1080;
    // 空白图像大小
    public static int blankImageWidth = 800;
    public static int blankImageHeight = 600;
    // 上传图片尺寸：原图尺寸 / 50% / 75% / 150% / 200% / 自定义
    public static String uploadSizeMode = "原图尺寸";
    public static int uploadCustomWidth = 1920;
    public static int uploadCustomHeight = 1080;

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
                if("configuration.txt".equals(configPath) ||configPath==null|| configPath.isEmpty()){
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
//                    ScreenCaptureToolApp.LOGGER.info("config line {}",line);
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
            case "uploadKey"->{
                UPLOAD_KEY=Integer.parseInt(value);
            }
            case "autoCopy"->{
                autoCopy= Boolean.parseBoolean(value);
            }
            case "detectMode"->{
                detectMode= value;
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
            case "uploadIsShiftNeeded"->{
                uploadIsShiftNeeded= Boolean.parseBoolean(value);
            }
            case "uploadIsAltNeeded"->{
                uploadIsAltNeeded= Boolean.parseBoolean(value);
            }
            case "uploadIsCtrlNeeded"->{
                uploadIsCtrlNeeded= Boolean.parseBoolean(value);
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
            case "scaleOnMouse"->{
                scaleOnMouse= Boolean.parseBoolean(value);
            }
            case "captureSavePath"->{
                captureSavePath= value;
            }
            case "lastSaveDir"->{
                lastSaveDir= value;
            }
            case "lastUploadDir"->{
                lastUploadDir= value;
            }
            case "proxyUrl"->{
                proxyUrl= value;
            }
            case "pasteAsExternalImage"->{
                pasteAsExternalImage= Boolean.parseBoolean(value);
            }
            case "outputSizeMode" -> outputSizeMode = value;
            case "outputCustomWidth" -> {
                try { outputCustomWidth = Integer.parseInt(value); } catch (NumberFormatException ignored) {}
            }
            case "outputCustomHeight" -> {
                try { outputCustomHeight = Integer.parseInt(value); } catch (NumberFormatException ignored) {}
            }
            case "blankImageWidth" -> {
                try { blankImageWidth = Integer.parseInt(value); } catch (NumberFormatException ignored) {}
            }
            case "blankImageHeight" -> {
                try { blankImageHeight = Integer.parseInt(value); } catch (NumberFormatException ignored) {}
            }
            case "uploadSizeMode" -> uploadSizeMode = value;
            case "uploadCustomWidth" -> {
                try { uploadCustomWidth = Integer.parseInt(value); } catch (NumberFormatException ignored) {}
            }
            case "uploadCustomHeight" -> {
                try { uploadCustomHeight = Integer.parseInt(value); } catch (NumberFormatException ignored) {}
            }
        }

    }

    public static void updateSelectPath(String path){
        selectPath=path;
        saveConfig();
    }
    public static File getSelectDirectory(){
        File f=new File(selectPath);
        if(f.isDirectory())
            return f;
        else
            return new File(f.getParent());
    }

    public static File getSaveDirectory(){
        String path = lastSaveDir.isBlank() ? selectPath : lastSaveDir;
        File f = new File(path);
        return f.isDirectory() ? f : new File(f.getParent());
    }

    public static File getUploadDirectory(){
        String path = lastUploadDir.isBlank() ? selectPath : lastUploadDir;
        File f = new File(path);
        return f.isDirectory() ? f : new File(f.getParent());
    }

    public static void updateLastSaveDir(String path){
        lastSaveDir = path;
        saveConfig();
    }

    public static void updateLastUploadDir(String path){
        lastUploadDir = path;
        saveConfig();
    }

    private static void saveConfig(){
        File f = new File(configPath);
        f.setWritable(true);
        try (FileWriter writer1 = new FileWriter(f)) {
            writer1.write(toConfigString());
            f.setWritable(false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static File getLogPath() throws IOException {
        File f=new File(logPath);
        if(f.exists()) {
            return checkAndCreateConfig(f);
        }
        else{
            boolean b=false;
            if(f.getParentFile().isDirectory()){
                 b= f.createNewFile();
                 return checkAndCreateConfig(f);
            }
            return b?f:null;
        }
    }

    private static File checkAndCreateConfig(File f) throws IOException {
        Path path;
        for (String s : Objects.requireNonNull(f.getParentFile().list())) {
            if(s.contains("log4j2.xml")){

                path=Paths.get(s);
                return path.toFile();
            }
        }
        //prepare log4j2.xml file at that folder
        InputStream in = ScreenCaptureToolApp.class.getResourceAsStream("log4j2.xml");
        if(in!=null){
            String s = new String(in.readAllBytes());
            s= s.replace("logPath",logPath);
            path=f.getParentFile().toPath().resolve("log4j2.xml");
            Files.createFile(path);
            Files.write(path,s.getBytes());
            return path.toFile();
        }
        return null;
    }


    public static void checkFile() throws IOException {
        File file=new File(System.getProperty("user.home")+"/captureToolConfig.txt");
        if(!file.exists()){
            file.createNewFile();
//            try(FileWriter writer=new FileWriter(file)){
//                writer.write("configuration.txt");
//            }
        }
    }
    public static String toConfigString() {
        return "ScreenCaptureToolProperties :{" +
                "\n isShiftNeeded=" + isShiftNeeded +
                "\n isAltNeeded=" + isAltNeeded +
                "\n isCtrlNeeded=" + isCtrlNeeded +
                "\n uploadIsShiftNeeded=" + uploadIsShiftNeeded +
                "\n uploadIsAltNeeded=" + uploadIsAltNeeded +
                "\n uploadIsCtrlNeeded=" + uploadIsCtrlNeeded +
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
                "\n detectMode=" + detectMode +
                "\n selectPath=" + selectPath +
                "\n autoLaunch=" + autoLaunch +
                "\n exePath=" + exePath +
                "\n autoLaunchEnabled=" + autoLaunchEnabled +
                "\n logPath=" + logPath +
                "\n scaleOnMouse=" + scaleOnMouse +
                "\n ocrFileInstalled=" + ocrFileInstalled +
                "\n ocrDataPath=" + ocrPath +
                "\n showSettings=" + showSettings +
                "\n captureSavePath=" + captureSavePath +
                "\n lastSaveDir=" + lastSaveDir +
                "\n lastUploadDir=" + lastUploadDir +
                "\n proxyUrl=" + proxyUrl +
                "\n pasteAsExternalImage=" + pasteAsExternalImage +
                "\n outputSizeMode=" + outputSizeMode +
                "\n outputCustomWidth=" + outputCustomWidth +
                "\n outputCustomHeight=" + outputCustomHeight +
                "\n blankImageWidth=" + blankImageWidth +
                "\n blankImageHeight=" + blankImageHeight +
                "\n uploadSizeMode=" + uploadSizeMode +
                "\n uploadCustomWidth=" + uploadCustomWidth +
                "\n uploadCustomHeight=" + uploadCustomHeight +
                "\n}";
    }
    public static void saveOnOriginalPath() {
        saveConfig();
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
                        Path path = Paths.get(new File(exePath).getParent() , "tessdata.zip");
                        HttpRequestHandler.downloadFile(url,path);
                        FileHandler.unzip(path.toString(),new File(exePath).getParent());
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
                            Path path = Paths.get(new File(exePath).getParent() , "tessdata.zip");
                            HttpRequestHandler.downloadFile(url,path);
                            FileHandler.unzip(path.toString(),file.getAbsolutePath()+"/tessdata");
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
