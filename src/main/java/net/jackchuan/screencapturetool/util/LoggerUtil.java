package net.jackchuan.screencapturetool.util;

import net.jackchuan.screencapturetool.CaptureProperties;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/12/17 18:09
 */
public class LoggerUtil {
    public static void logInfo(String msg){
        try(PrintWriter pw=new PrintWriter(new FileWriter(CaptureProperties.logPath,true),true)){
            pw.println(msg);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
