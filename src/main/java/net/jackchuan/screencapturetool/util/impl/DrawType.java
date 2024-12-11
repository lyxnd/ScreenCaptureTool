package net.jackchuan.screencapturetool.util.impl;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

public enum DrawType implements DrawingAction {
    LINE {
        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double currentX, double currentY) {
            gc.strokeLine(startX, startY, currentX, currentY);
        }

        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double endX, double endY, Color foreColor) {

        }
    },
    RECTANGLE {
        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double currentX, double currentY) {
            double width = Math.abs(currentX - startX);
            double height = Math.abs(currentY - startY);
            if (startX < currentX && startY < currentY) {
                gc.strokeRect(startX, startY, width, height);
            } else if (startX < currentX && startY > currentY) {
                gc.strokeRect(startX, currentY, width, height);
            } else if (startX > currentX && startY < currentY) {
                gc.strokeRect(currentX, startY, width, height);
            } else if (startX > currentX && startY > currentY) {
                gc.strokeRect(currentX, currentY, width, height);
            }
        }
        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double endX, double endY, Color foreColor) {

        }
    },
    FILLED_RECTANGLE{
        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double endX, double endY) {
        }
        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double currentX, double currentY,Color forecolor) {
            double width = Math.abs(currentX - startX);
            double height = Math.abs(currentY - startY);
            if (startX < currentX && startY < currentY) {
                gc.strokeRect(startX, startY, width, height);
                gc.setFill(Color.rgb((int) (forecolor.getRed() * 255), ((int) forecolor.getGreen() * 255),
                        (int) (forecolor.getBlue() * 255), 0.05));
                gc.fillRect(startX, startY, width, height);
            } else if (startX < currentX && startY > currentY) {
                gc.strokeRect(startX, currentY, width, height);
                gc.setFill(Color.rgb((int) (forecolor.getRed() * 255), ((int) forecolor.getGreen() * 255),
                        (int) (forecolor.getBlue() * 255), 0.05));
                gc.fillRect(startX, currentY, width, height);
            } else if (startX > currentX && startY < currentY) {
                gc.strokeRect(currentX, startY, width, height);
                gc.setFill(Color.rgb((int) (forecolor.getRed() * 255), ((int) forecolor.getGreen() * 255),
                        (int) (forecolor.getBlue() * 255), 0.05));
                gc.fillRect(currentX, startY, width, height);
            } else if (startX > currentX && startY > currentY) {
                gc.strokeRect(currentX, currentY, width, height);
                gc.setFill(Color.rgb((int) (forecolor.getRed() * 255), ((int) forecolor.getGreen() * 255),
                        (int) (forecolor.getBlue() * 255), 0.05));
                gc.fillRect(currentX, currentY, width, height);
            }
        }
    },
    ARROW{
        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double currentX, double currentY) {

        }
        @Override
        public void draw(GraphicsContext g2, double startX, double startY, double endX, double endY, Color foreColor) {
            // 计算箭头的角度
            double angle = Math.atan2(endY - startY, endX - startX);
            double arrowLength=25;
            // 绘制箭头的主体线
            g2.setStroke(foreColor);
            g2.setLineWidth(2);
            g2.strokeLine(startX, startY, endX, endY);
            // 计算箭头头部的两个点
            double arrowAngle = Math.toRadians(30); // 箭头两侧的角度（可以根据需要调整）
            double x1 = endX - arrowLength * Math.cos(angle - arrowAngle);
            double y1 = endY - arrowLength * Math.sin(angle - arrowAngle);
            double x2 = endX - arrowLength * Math.cos(angle + arrowAngle);
            double y2 = endY - arrowLength * Math.sin(angle + arrowAngle);
            // 绘制箭头的两侧
            g2.setFill(foreColor);
            g2.fillPolygon(new double[]{endX, x1, x2}, new double[]{endY, y1, y2}, 3);
        }
    },
    WAVE{
        @Override
        public void draw(GraphicsContext g2, double startX, double startY, double endX, double endY) {
            double dx = Math.abs(startX - endX);
            double dy = Math.abs(startY - endY);
            double t = Math.sqrt(dx * dx + dy * dy);
            double radius = 6;
            double s = 0;
            while (s <= t) {
                if ((s / (2.5 * radius)) % 2 == 0) {
                    // 上半弧
                    g2.strokeArc(s + startX, startY, 2.5 * radius, 1.2 * radius, 0, 180, ArcType.OPEN);
                } else {
                    // 下半弧
                    g2.strokeArc(s + startX, startY, 2.5 * radius, 1.2 * radius, 0, -180, ArcType.OPEN);
                }
                s += 2.5 * radius; // 按每个弧的宽度（radius）移动
            }
        }
        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double endX, double endY, Color foreColor) {

        }
    },
    CIRCLE{
        @Override
        public void draw(GraphicsContext g2, double startX, double startY, double endX, double endY) {
            if (startX < endX && startY < endY) {
                //左上到右下
                g2.strokeArc(startX,startY,Math.abs(startX-endX),Math.abs(startY-endY),0,360,ArcType.OPEN);
            } else if (startX < endX && startY > endY) {
                //左下到右上
                g2.strokeArc(startX,endY,Math.abs(startX-endX),Math.abs(startY-endY),0,360,ArcType.OPEN);
            } else if (startX > endX && startY < endY) {
                //右上到左下
                g2.strokeArc(endX,startY,Math.abs(startX-endX),Math.abs(startY-endY),0,360,ArcType.OPEN);
            } else if (startX > endX && startY > endY) {
                //右下到左上
                g2.strokeArc(endX,endY,Math.abs(startX-endX),Math.abs(startY-endY),0,360,ArcType.OPEN);
            }
        }
        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double endX, double endY, Color foreColor) {

        }
    },
    FILLED_CIRCLE{
        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double currentX, double currentY) {

        }

        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double endX, double endY, Color foreColor) {

        }
    },

}
