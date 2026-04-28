package net.jackchuan.screencapturetool.network;

import javafx.stage.Stage;
import net.jackchuan.screencapturetool.entity.StageInstance;
import net.jackchuan.screencapturetool.external.stage.ProgressStage;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/12/22 20:52
 */
public class HttpRequestHandler {

    public static void downloadFile(String fileURL, Path savePath) throws IOException {
        URL url = new URL(fileURL);
        try (InputStream in = url.openStream()) {
            Files.copy(in, savePath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("文件下载完成：" + savePath.toAbsolutePath());
        }
    }

    public static void main(String[] args) {
        String fileURL = "https://github.com/lyxnd/temp/archive/refs/heads/main.zip";
        Path savePath = Paths.get("E:/downloaded_file.zip");

        try {
            downloadFile(fileURL, savePath);
        } catch (IOException e) {
            System.err.println("下载失败: " + e.getMessage());
        }
    }

}
