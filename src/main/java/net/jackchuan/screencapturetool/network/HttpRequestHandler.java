package net.jackchuan.screencapturetool.network;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/12/22 20:52
 */
public class HttpRequestHandler {
    public static void main(String[] args) {
        String fileURL = "https://example.com/file-to-download.txt";  // 替换为目标 URL
        String saveDir = "F:/downloads/";  // 保存目录
        try {
            downloadFile(fileURL, saveDir);
        } catch (IOException e) {
            System.out.println("下载失败: " + e.getMessage());
        }
    }

    public static void downloadFile(String fileURL, String saveDir) throws IOException {
        // 创建 URL 对象
        URL url = new URL(fileURL);
        // 打开连接
        URLConnection connection = url.openConnection();
        connection.connect();  // 可选：用于触发实际的连接请求
        // 获取文件名（从 URL 中提取）
        String fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1);
        // 输入流从 URL 中获取数据
        try (InputStream inputStream = connection.getInputStream()) {
            // 输出流用于将数据写入文件
            File fileToSave = new File(saveDir + fileName);
            try (OutputStream outputStream = new FileOutputStream(fileToSave)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                // 从输入流读取数据并写入到输出流（文件中）
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                System.out.println("文件下载成功: " + fileToSave.getAbsolutePath());
            }
        }
    }
}
