package net.jackchuan.screencapturetool.entity;

import lombok.Data;
import net.jackchuan.screencapturetool.controller.CaptureDisplayController;
import net.jackchuan.screencapturetool.controller.EditRecordController;
import net.jackchuan.screencapturetool.controller.SettingController;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/11/23 13:30
 */
@Data
public class ControllerInstance {
    private static ControllerInstance instance;
    private CaptureDisplayController controller;
    private EditRecordController editController;
    private SettingController settingController;
    private ControllerInstance() {}

    public static synchronized ControllerInstance getInstance() {
        if (instance == null) {
            instance = new ControllerInstance();
        }
        return instance;
    }

}
