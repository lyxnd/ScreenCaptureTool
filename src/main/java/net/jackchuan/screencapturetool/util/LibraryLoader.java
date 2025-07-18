package net.jackchuan.screencapturetool.util;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/11/26 15:15
 */
import net.jackchuan.screencapturetool.ScreenCaptureToolApp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class LibraryLoader {
    public static void loadOpenCVLibrary() {
        try {
            // 将资源复制到临时目录
            File tempLib = File.createTempFile("opencv_java490", ".dll");
            tempLib.deleteOnExit(); // 程序退出时删除临时文件
            // 从资源路径读取 DLL 文件并写入临时文件
            Files.copy(ScreenCaptureToolApp.class.getResourceAsStream("lib/opencv_java490.dll"),
                    tempLib.toPath(), StandardCopyOption.REPLACE_EXISTING);
            // 使用临时文件的路径加载 DLL
            System.load(tempLib.getAbsolutePath());
            System.out.println("OpenCV library loaded successfully.");

        } catch (IOException e) {
            e.printStackTrace();
            throw new UnsatisfiedLinkError("Failed to load OpenCV library.");
        }
    }
    public static void loadTess4jLibrary() {
        try {
            System.setProperty("jna.library.path",
                    "32".equals(System.getProperty("sun.arch.data.model")) ? "lib/win32-x86" : "lib/win32-x86-64");
            File tempLib1 = File.createTempFile("libtesseract541", ".dll");
            tempLib1.deleteOnExit(); // 程序退出时删除临时文件
            // 从资源路径读取 DLL 文件并写入临时文件
            Files.copy(ScreenCaptureToolApp.class.getResourceAsStream("lib/libtesseract541.dll"),
                    tempLib1.toPath(), StandardCopyOption.REPLACE_EXISTING);
            // 使用临时文件的路径加载 DLL

            System.out.println(tempLib1.exists());
            System.load(tempLib1.getAbsolutePath());
            System.out.println("Tess4j library loaded successfully.");

        } catch (IOException e) {
            e.printStackTrace();
            throw new UnsatisfiedLinkError("Failed to load Tess4j library.");
        }
    }

    public static void loadJNativeHook() {
        try {
            // 将资源复制到临时目录
            File tempLib = File.createTempFile("JNativeHook", ".dll");
            tempLib.deleteOnExit(); // 程序退出时删除临时文件
            // 从资源路径读取 DLL 文件并写入临时文件
            Files.copy(ScreenCaptureToolApp.class.getResourceAsStream("lib/JNativeHook.dll"),
                    tempLib.toPath(), StandardCopyOption.REPLACE_EXISTING);
            // 使用临时文件的路径加载 DLL
            System.load(tempLib.getAbsolutePath());
            System.out.println("JNativeHook library loaded successfully.");

        } catch (IOException e) {
            e.printStackTrace();
            throw new UnsatisfiedLinkError("Failed to load OpenCV library.");
        }
    }
}
