package net.jackchuan.screencapturetool.util;

import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import net.jackchuan.screencapturetool.entity.StageInstance;
import net.jackchuan.screencapturetool.external.stage.ProgressStage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/12/23 12:19
 */
public class FileHandler {

    public static boolean saveImage(BufferedImage img, String path) throws IOException {
        return ImageIO.write(img, "png", new File(path));
    }

    /**
     * 解压 ZIP 文件
     *
     * @param zipFilePath ZIP 文件的路径
     * @param destDir     解压的目标目录
     * @throws IOException
     */
    public static void unzip(String zipFilePath, String destDir) throws IOException {
        // 初始化目标目录
        if (destDir.isBlank()) {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("选择安装目录");
            chooser.setInitialDirectory(new File("D:/"));
            File file = chooser.showDialog(null);
            destDir = file.getAbsolutePath();
        } else {
            destDir = new File(destDir).getAbsolutePath();
        }
        destDir += "/tessdata";

        // 创建目标目录
        File destDirFile = new File(destDir);
        if (!destDirFile.exists()) {
            destDirFile.mkdirs();
        }

        // 获取 ZIP 文件的总条目数量
        int totalEntries = 0;
        try (ZipFile zipFile = new ZipFile(zipFilePath)) {
            totalEntries = zipFile.size();
        }
        ProgressStage progressStage = StageInstance.getInstance().getProgressStage();
        // 创建 ZipInputStream 输入流，读取 ZIP 文件
        int processedEntries = 0; // 已处理的条目数量
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                // 更新进度
                processedEntries++;
                double progress = (double) processedEntries / totalEntries;
                if(progressStage!=null){
                    progressStage.setProgress(progress/2+0.5);
                    progressStage.setTip("解压中 : "+String.format("%.2f",progress/2+0.5)+"%");
                }
                // 创建条目的文件路径
                File entryFile = new File(destDir, entry.getName());
                // 如果条目是一个目录，创建目录
                if (entry.isDirectory()) {
                    entryFile.mkdirs();
                } else {
                    // 如果是文件，创建父目录
                    entryFile.getParentFile().mkdirs();
                    // 写入解压文件
                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(entryFile))) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = zis.read(buffer)) != -1) {
                            bos.write(buffer, 0, bytesRead);
                        }
                    }
                }
                zis.closeEntry(); // 关闭当前条目
            }
        }
        // 删除 ZIP 文件
        System.out.println(zipFilePath);
        new File(zipFilePath).delete();
    }

    public static void moveToAnotherDirectory(Path sourceDir,Path targetDir) throws IOException {
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        // 遍历源目录及其子目录
        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                // 构建目标文件路径
                Path targetFile = targetDir.resolve(sourceDir.relativize(file));

                // 创建目标文件的父目录（如果不存在的话）
                Files.createDirectories(targetFile.getParent());
                // 复制文件
                Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                // 创建目标目录（对于每个目录，都会调用）
                Path targetSubDir = targetDir.resolve(sourceDir.relativize(dir));
                Files.createDirectories(targetSubDir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void deleteDownloadedFile(String path) {
        File file = new File(path);
        if(file.exists()){
            File[] files = file.getParentFile().listFiles();
            if(files==null){
                return;
            }
            for(File f:files){
                if(f.getName().contains("tessdata")){
                    f.delete();
                }
            }
        }

    }
}
