package net.jackchuan.screencapturetool.network;

import net.jackchuan.screencapturetool.CaptureProperties;
import net.jackchuan.screencapturetool.ScreenCaptureToolApp;
import net.jackchuan.screencapturetool.entity.StageInstance;
import net.jackchuan.screencapturetool.external.stage.ProgressStage;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/12/22 20:52
 */
public class HttpRequestHandler {

    private static final int CONNECT_TIMEOUT = 15_000;
    private static final int READ_TIMEOUT     = 60_000;
    private static final int BUFFER_SIZE      = 32_768;

    public static void downloadFile(String fileURL, Path savePath) throws IOException {
        ProgressStage ps = StageInstance.getInstance().getProgressStage();
        if (ps != null) {
            ps.setProgress(-1);
            ps.setTip("正在连接...");
        }
        // 优先使用配置的代理，否则尝试系统代理
        Proxy proxy = resolveProxy();
        HttpURLConnection connection = openConnection(fileURL, proxy);

        // 处理重定向（GitHub 会跳转到 CDN）
        int code = connection.getResponseCode();
        for (int i = 0; i < 5 && isRedirect(code); i++) {
            String location = connection.getHeaderField("Location");
            connection.disconnect();
            connection = openConnection(location, proxy);
            code = connection.getResponseCode();
        }

        if (code != HttpURLConnection.HTTP_OK) {
            connection.disconnect();
            throw new IOException("HTTP " + code + " : " + fileURL);
        }

        long total = connection.getContentLengthLong();
        ProgressStage progressStage = StageInstance.getInstance().getProgressStage();

        // 连接已建立，开始接收数据前先给用户反馈
        if (progressStage != null) {
            progressStage.setProgress(-1); // indeterminate
            progressStage.setTip(total > 0
                    ? String.format("准备下载：%.1f MB", total / 1_048_576.0)
                    : "连接中，等待数据...");
        }

        try (InputStream  in  = new BufferedInputStream(connection.getInputStream(), BUFFER_SIZE);
             OutputStream out = new BufferedOutputStream(
                     Files.newOutputStream(savePath, StandardOpenOption.CREATE,
                             StandardOpenOption.TRUNCATE_EXISTING), BUFFER_SIZE)) {
            byte[] buf = new byte[BUFFER_SIZE];
            long downloaded = 0;
            long lastUpdate = System.currentTimeMillis();
            long lastBytes  = 0;
            int read;
            while ((read = in.read(buf)) != -1) {
                out.write(buf, 0, read);
                downloaded += read;
                long now = System.currentTimeMillis();
                if (progressStage != null && now - lastUpdate >= 300) {
                    long elapsed = Math.max(now - lastUpdate, 1);
                    double speed = (downloaded - lastBytes) * 1000.0 / elapsed / 1024; // KB/s
                    // total > 0：显示确定进度（占前 50%）；否则保持 indeterminate 动画
                    double progress = total > 0 ? (double) downloaded / total * 0.5 : -1;
                    progressStage.setProgress(progress);
                    String tip = total > 0
                            ? String.format("下载中：%.1f / %.1f MB  %.0f KB/s",
                                    downloaded / 1_048_576.0, total / 1_048_576.0, speed)
                            : String.format("已下载：%.1f MB  %.0f KB/s",
                                    downloaded / 1_048_576.0, speed);
                    progressStage.setTip(tip);
                    lastUpdate = now;
                    lastBytes  = downloaded;
                }
            }
        } finally {
            connection.disconnect();
        }
    }

    // ── 内部工具 ──────────────────────────────────────────────

    private static HttpURLConnection openConnection(String urlStr, Proxy proxy) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) (proxy != null
                ? url.openConnection(proxy)
                : url.openConnection());
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        conn.setInstanceFollowRedirects(false); // 手动处理，避免 http→https 重定向失败
        return conn;
    }

    private static boolean isRedirect(int code) {
        return code == HttpURLConnection.HTTP_MOVED_PERM
                || code == HttpURLConnection.HTTP_MOVED_TEMP
                || code == 307 || code == 308;
    }

    private static Proxy resolveProxy() {
        String proxyUrl = CaptureProperties.proxyUrl;
        if (proxyUrl != null && !proxyUrl.isBlank()) {
            try {
                if (!proxyUrl.contains("://")) proxyUrl = "http://" + proxyUrl;
                URI uri = new URI(proxyUrl);
                String scheme = uri.getScheme().toLowerCase();
                Proxy.Type type = scheme.startsWith("socks") ? Proxy.Type.SOCKS : Proxy.Type.HTTP;
                int port = uri.getPort() > 0 ? uri.getPort() : (type == Proxy.Type.SOCKS ? 1080 : 8080);
                return new Proxy(type, new InetSocketAddress(uri.getHost(), port));
            } catch (URISyntaxException e) {
                System.err.println("代理地址解析失败，将使用系统代理：" + e.getMessage());
            }
        }
        // 使用系统代理
        System.setProperty("java.net.useSystemProxies", "true");
        return null; // null 让 openConnection 用系统代理
    }
}