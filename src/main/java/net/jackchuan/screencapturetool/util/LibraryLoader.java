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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class LibraryLoader {
    private static Path getNativeLibDir() throws IOException {
        // 提取到程序工作目录下的 native-libs 子目录，避免使用 temp（会被杀毒/系统清理）
        Path dir = Paths.get(System.getProperty("user.dir"), "native-libs");
        Files.createDirectories(dir);
        return dir;
    }

    private static Path extractLib(String resourceName, String fileName) throws IOException {
        Path libDir = getNativeLibDir();
        Path dest = libDir.resolve(fileName);
        // 只在文件不存在时才解压，避免重复 IO
        if (!Files.exists(dest)) {
            try (var in = ScreenCaptureToolApp.class.getResourceAsStream(resourceName)) {
                if (in == null) throw new IOException("Resource not found: " + resourceName);
                Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        return dest;
    }

    public static void loadOpenCVLibrary() {
        try {
            Path dll = extractLib("lib/opencv_java490.dll", "opencv_java490.dll");
            System.load(dll.toAbsolutePath().toString());
            System.out.println("OpenCV library loaded successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            throw new UnsatisfiedLinkError("Failed to load OpenCV library.");
        }
    }

    public static void loadTess4jLibrary() {
        try {
            Path libDir = getNativeLibDir();
            // JNA 使用绝对路径查找依赖库
            System.setProperty("jna.library.path", libDir.toAbsolutePath().toString());
            Path dll = extractLib("lib/libtesseract541.dll", "libtesseract541.dll");
            System.load(dll.toAbsolutePath().toString());
            System.out.println("Tess4j library loaded successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            throw new UnsatisfiedLinkError("Failed to load Tess4j library.");
        }
    }

    public static void loadJNativeHook() {
        try {
            Path dll = extractLib("lib/JNativeHook.dll", "JNativeHook.dll");
            System.load(dll.toAbsolutePath().toString());
            System.out.println("JNativeHook library loaded successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            throw new UnsatisfiedLinkError("Failed to load JNativeHook library.");
        }
    }
}
