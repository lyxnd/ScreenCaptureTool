package net.jackchuan.screencapturetool.util.impl;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public interface DrawingAction {
    void draw(GraphicsContext gc, double startX, double startY, double currentX, double currentY);
    void draw(GraphicsContext gc, double startX, double startY, double endX, double endY, Color foreColor);
}
