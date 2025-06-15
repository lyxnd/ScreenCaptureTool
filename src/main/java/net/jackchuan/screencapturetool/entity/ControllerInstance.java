package net.jackchuan.screencapturetool.entity;

import net.jackchuan.screencapturetool.controller.CaptureDisplayController;
import net.jackchuan.screencapturetool.controller.EditRecordController;
import net.jackchuan.screencapturetool.controller.SettingController;

/**
 * 功能：
 * 作者：jackchuan
 * 日期：2024/11/23 13:30
 */
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

    public void setController(CaptureDisplayController controller) {
        this.controller = controller;
    }

    public CaptureDisplayController getController() {
        return controller;
    }

    public EditRecordController getEditController() {
        return editController;
    }

    public SettingController getSettingController() {
        return settingController;
    }

    public void setEditController(EditRecordController editController) {
        this.editController = editController;
    }

    public void setSettingController(SettingController settingController) {
        this.settingController = settingController;
    }
}
