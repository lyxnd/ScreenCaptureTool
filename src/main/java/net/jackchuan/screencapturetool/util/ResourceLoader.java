package net.jackchuan.screencapturetool.util;

import net.jackchuan.screencapturetool.ScreenCaptureToolApp;

import java.io.*;
import java.util.ArrayList;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/12/17 21:54
 */
public class ResourceLoader {
    public static ArrayList<String> getAsLines(String path) throws IOException {
        System.out.println(path);
        InputStream inputStream = ScreenCaptureToolApp.class.getResourceAsStream(path);
        if(inputStream==null){
            return null;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        ArrayList<String> lines = new ArrayList<>();
        String line = reader.readLine();
        while(line!=null){
            lines.add(line);
            line=reader.readLine();
        }
        return lines;
    }
}
