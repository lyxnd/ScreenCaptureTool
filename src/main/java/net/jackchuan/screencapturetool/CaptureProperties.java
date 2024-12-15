package net.jackchuan.screencapturetool;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import net.jackchuan.screencapturetool.util.ScreenCaptureUtil;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
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
    public static boolean pencil;
    public static boolean rubber;
    public static boolean rect;
    public static boolean filledRect;
    public static boolean oval;
    public static boolean arrow;
    public static boolean line;
    public static boolean wave;
    public static boolean color;
    public static boolean strokeUp;
    public static boolean strokeDown;
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
    public static boolean autoLaunchEnabled;
    public static String configPath;
    public static String selectPath="D:/";
    public static String exePath;
    public static double scale;
    public static double width;
    public static double height;

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
        pencil = flag;
        rubber = flag;
        rect = flag;
        filledRect = flag;
        oval = flag;
        arrow = flag;
        line = flag;
        wave = flag;
        color = flag;
        strokeUp = flag;
        strokeDown = flag;
        undo = flag;
        redo = flag;
    }

    public static boolean loadProperties() throws IOException {
        File file=new File(System.getProperty("user.home")+"/captureToolConfig.txt");
        if(file.exists()){
            try(BufferedReader reader=new BufferedReader(new FileReader(file))){
                configPath = reader.readLine();
                System.out.println(configPath);
                if("configuration.txt".equals(configPath)){
                    readFromJar();
                }else {
                    File configFile=new File(configPath);
                    List<String> list = Files.readAllLines(configFile.toPath());
                    for (String line :list){
                        if (line.contains("=")) {
                            String[] pair = line.split("=");
                            updateSettings(pair[0].trim(),pair[1].trim());
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
            case "pencil" -> {
                pencil = Boolean.parseBoolean(value);
            }
            case "rubber" -> {
                rubber = Boolean.parseBoolean(value);
            }
            case "rect" -> {
                rect = Boolean.parseBoolean(value);
            }
            case "filledRect" -> {
                filledRect = Boolean.parseBoolean(value);
            }
            case "oval" -> {
                oval = Boolean.parseBoolean(value);
            }
            case "arrow" -> {
                arrow = Boolean.parseBoolean(value);
            }
            case "line" -> {
                line = Boolean.parseBoolean(value);
            }
            case "wave" -> {
                wave = Boolean.parseBoolean(value);
            }
            case "color" -> {
                color = Boolean.parseBoolean(value);
            }
            case "strokeUp" -> {
                strokeUp = Boolean.parseBoolean(value);
            }
            case "strokeDown" -> {
                strokeDown = Boolean.parseBoolean(value);
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
                "\n pencil=" + pencil +
                "\n rubber=" + rubber +
                "\n rect=" + rect +
                "\n filledRect=" + filledRect +
                "\n oval=" + oval +
                "\n arrow=" + arrow +
                "\n line=" + line +
                "\n wave=" + wave +
                "\n color=" + color +
                "\n strokeUp=" + strokeUp +
                "\n strokeDown=" + strokeDown +
                "\n undo=" + undo +
                "\n redo=" + redo +
                "\n captureKey=" + CaptureProperties.CAPTURE_KEY +
                "\n autoCopy=" + autoCopy +
                "\n autoSelect=" + autoSelect +
                "\n selectPath=" + selectPath +
                "\n autoLaunch=" + autoLaunch +
                "\n exePath=" + exePath +
                "\n autoLaunchEnabled=" + autoLaunchEnabled +
                "\n}";
    }
}
