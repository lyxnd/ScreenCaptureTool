package net.jackchuan.screencapturetool.network;

import javafx.stage.Stage;
import net.jackchuan.screencapturetool.entity.StageInstance;
import net.jackchuan.screencapturetool.external.stage.ProgressStage;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/12/22 20:52
 */
public class HttpRequestHandler {
    public static String downloadFile(String fileURL, String saveDir) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        File tempFile = new File(saveDir);
        // 创建 URL 对象
        // 设置 SSL 配置
        SSLSocketFactory sslSocketFactory = setSSLConfig().getSocketFactory();
        // 创建 URL 对象
        URL url = new URL(fileURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // 确保它是 HttpsURLConnection
        if (connection instanceof HttpsURLConnection httpsConnection) {
            // 设置 SSLSocketFactory
            httpsConnection.setSSLSocketFactory(sslSocketFactory);
            // 其他可选的配置，如设置协议
            httpsConnection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            // 进行连接并处理响应
            httpsConnection.connect();
            // 获取响应码
            System.out.println("Response Code: " + httpsConnection.getResponseCode());
//            connection.setRequestProperty("User-Agent",
//                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

            if (!tempFile.exists()) {
                tempFile.createNewFile();
            }
            ProgressStage progressStage = StageInstance.getInstance().getProgressStage();
            double totalBytes = 1.3651756E7;
            // 输入流从 URL 中获取数据
            try (InputStream inputStream = httpsConnection.getInputStream()) {
                // 输出流用于将数据写入文件
                try (OutputStream outputStream = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    double n = 0;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        n += bytesRead;
                        if (progressStage != null) {
                            double progress = n / totalBytes / 2f;
                            progressStage.setProgress(progress);
                            progressStage.setTip("下载中 : " + String.format("%.2f", progress) + "%");
                        }
                        outputStream.write(buffer, 0, bytesRead);
                        outputStream.flush();
                    }
                }
            }
        }
            // 打开连接
//            URLConnection connection = url.openConnection();
            return tempFile.getAbsolutePath();
        }

        // 设置 SSL 配置方法
        private static SSLContext setSSLConfig () throws NoSuchAlgorithmException, KeyManagementException {
            SSLContext sslContext = SSLContext.getInstance("TLSv1.3");

            // 配置 TrustManager (信任所有证书)
            TrustManager[] trustManagers = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            sslContext.init(null, trustManagers, new java.security.SecureRandom());
            return sslContext;
        }
    }
