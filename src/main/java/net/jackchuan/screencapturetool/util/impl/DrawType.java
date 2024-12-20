package net.jackchuan.screencapturetool.util.impl;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

import java.util.Random;

public enum DrawType implements DrawingAction {
    LINE {
        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double currentX, double currentY) {
            gc.setLineDashes(0);
            gc.strokeLine(startX, startY, currentX, currentY);
        }

        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double endX, double endY, Color foreColor) {

        }
    },
    RECTANGLE {
        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double currentX, double currentY) {
            gc.setLineDashes(0);
            double width = Math.abs(currentX - startX);
            double height = Math.abs(currentY - startY);
            gc.strokeRect(Math.min(startX,currentX), Math.min(startY,currentY),width,height);
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
            gc.setLineDashes(0);
            gc.strokeRect(Math.min(startX,currentX), Math.min(startY,currentY),width,height);
            gc.setFill(Color.rgb((int) (forecolor.getRed() * 255), ((int) forecolor.getGreen() * 255),
                    (int) (forecolor.getBlue() * 255), 0.05));
            gc.fillRect(Math.min(startX,currentX), Math.min(startY,currentY),width,height);
        }
    },
    ARROW{
        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double currentX, double currentY) {
            // 绘制箭头的主干（直线部分）
            gc.strokeLine(startX, startY, currentX, currentY);
            gc.setLineDashes(0);
            Random random=new Random();
            // 箭头的大小
            double arrowSize = 10;
            // 计算箭头的角度
            double angle = Math.atan2(currentY - startY, currentX - startX);
            // 计算箭头两侧的角度
            double delta = Math.toRadians(random.nextInt(15,25));
            double arrowAngle1 = angle + delta;  // 箭头左侧的角度
            double arrowAngle2 = angle - delta;  // 箭头右侧的角度
            // 计算箭头的两个点
            double x1 = currentX - arrowSize * Math.cos(arrowAngle1);
            double y1 = currentY - arrowSize * Math.sin(arrowAngle1);
            double x2 = currentX - arrowSize * Math.cos(arrowAngle2);
            double y2 = currentY - arrowSize * Math.sin(arrowAngle2);
            // 绘制箭头的两条斜线
            gc.strokeLine(currentX, currentY, x1, y1);  // 左侧的箭头
            gc.strokeLine(currentX, currentY, x2, y2);  // 右侧的箭头
        }
        @Override
        public void draw(GraphicsContext g2, double startX, double startY, double endX, double endY, Color foreColor) {

        }
    },
    WAVE{
        @Override
        public void draw(GraphicsContext g2, double startX, double startY, double endX, double endY) {
            g2.setLineDashes(0);
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
            g2.setLineDashes(0);
            g2.strokeArc(Math.min(startX,endX),Math.min(startY,endY),
                    Math.abs(startX-endX),Math.abs(startY-endY),0,360,ArcType.OPEN);
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
            gc.setLineDashes(0);
            gc.strokeArc(Math.min(startX,endX),Math.min(startY,endY),
                    Math.abs(startX-endX),Math.abs(startY-endY),0,360,ArcType.OPEN);
            gc.setFill(Color.rgb((int) (foreColor.getRed() * 255), ((int) foreColor.getGreen() * 255),
                    (int) (foreColor.getBlue() * 255), 0.05));
            gc.fillArc(Math.min(startX,endX),Math.min(startY,endY),
                    Math.abs(startX-endX),Math.abs(startY-endY),0,360,ArcType.OPEN);
        }
    },
    LINE_DASHED{
        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double currentX, double currentY) {
            gc.setLineDashes(10,10);
            gc.strokeLine(startX, startY, currentX, currentY);
        }

        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double endX, double endY, Color foreColor) {

        }
    },
    LINE_DOUBLE{
        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double currentX, double currentY) {
            gc.setLineDashes(0);
            gc.strokeLine(startX, startY, currentX, currentY);
            gc.strokeLine(startX, startY-5, currentX, currentY-5);
        }

        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double endX, double endY, Color foreColor) {

        }
    },
    RECTANGLE_ROUND{
        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double currentX, double currentY) {
            gc.setLineDashes(0);
            double width = Math.abs(currentX - startX);
            double height = Math.abs(currentY - startY);
            gc.strokeRoundRect(Math.min(startX,currentX), Math.min(startY,currentY),width,height,15,15);
        }

        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double endX, double endY, Color foreColor) {

        }
    },
    RECTANGLE_ROUND_FILLED{
        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double currentX, double currentY) {

        }

        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double endX, double endY, Color foreColor) {
            gc.setLineDashes(0);
            double width = Math.abs(endX - startX);
            double height = Math.abs(endY - startY);
            gc.strokeRoundRect(Math.min(startX,endX), Math.min(startY,endY),width,height,15,15);
            gc.setFill(Color.rgb((int) (foreColor.getRed() * 255), ((int) foreColor.getGreen() * 255),
                    (int) (foreColor.getBlue() * 255), 0.05));
            gc.fillRoundRect(Math.min(startX,endX), Math.min(startY,endY),width,height,15,15);
        }
    },
    OVAL_DASHED{
        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double currentX, double currentY) {
            gc.setLineDashes(10,10);
            gc.strokeArc(Math.min(startX,currentX),Math.min(startY,currentY),
                    Math.abs(startX-currentX),Math.abs(startY-currentY),0,360,ArcType.OPEN);
        }

        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double endX, double endY, Color foreColor) {

        }
    },
    ARROW_DASHED{
        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double currentX, double currentY) {
            gc.setLineDashes(10,10);
            // 绘制箭头的主干（直线部分）
            gc.strokeLine(startX, startY, currentX, currentY);
            // 箭头的大小
            gc.setLineDashes(0);
            Random random = new Random();
            double arrowSize = 10;
            // 计算箭头的角度
            double angle = Math.atan2(currentY - startY, currentX - startX);
            double delta = Math.toRadians(random.nextInt(15,25));
            double arrowAngle1 = angle + delta;  // 箭头左侧的角度
            double arrowAngle2 = angle - delta;  // 箭头右侧的角度
            // 计算箭头的两个点
            double x1 = currentX - arrowSize * Math.cos(arrowAngle1);
            double y1 = currentY - arrowSize * Math.sin(arrowAngle1);
            double x2 = currentX - arrowSize * Math.cos(arrowAngle2);
            double y2 = currentY - arrowSize * Math.sin(arrowAngle2);
            // 绘制箭头的两条斜线
            gc.strokeLine(currentX, currentY, x1, y1);  // 左侧的箭头
            gc.strokeLine(currentX, currentY, x2, y2);  // 右侧的箭头
        }

        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double endX, double endY, Color foreColor) {

        }
    },
    ARROW_TWO_DIR{
        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double currentX, double currentY) {
            double arrowSize = 10;
            gc.strokeLine(startX, startY, currentX, currentY);
            // 计算箭头的角度
            Random random=new Random();
            double angle = Math.atan2(currentY - startY, currentX - startX); // 主干的角度
            double delta = Math.toRadians(random.nextInt(15,25));
            double arrowAngle1 = angle + delta;  // 箭头左侧的角度
            double arrowAngle2 = angle - delta;  // 箭头右侧的角度
            // 箭头末端位置
            double arrowX1 = currentX - arrowSize * Math.cos(arrowAngle1);
            double arrowY1 = currentY - arrowSize * Math.sin(arrowAngle1);
            double arrowX2 = currentX - arrowSize * Math.cos(arrowAngle2);
            double arrowY2 = currentY - arrowSize * Math.sin(arrowAngle2);
            // 绘制箭头尾部
            gc.strokeLine(currentX, currentY, arrowX1, arrowY1);
            gc.strokeLine(currentX, currentY, arrowX2, arrowY2);
            // 计算反方向箭头的角度
            double reverseAngle = Math.atan2(startY - currentY, startX - currentX); // 反向角度
            double reverseArrow1 = reverseAngle + delta;  // 反向箭头左侧的角度
            double reverseArrow2 = reverseAngle - delta;  // 反向箭头右侧的角度
            // 反向箭头末端位置
            double reverseArrowX1 = startX - arrowSize * Math.cos(reverseArrow1);
            double reverseArrowY1 = startY - arrowSize * Math.sin(reverseArrow1);
            double reverseArrowX2 = startX - arrowSize * Math.cos(reverseArrow2);
            double reverseArrowY2 = startY - arrowSize * Math.sin(reverseArrow2);
            // 绘制反向箭头
            gc.strokeLine(startX, startY, reverseArrowX1, reverseArrowY1);
            gc.strokeLine(startX, startY, reverseArrowX2, reverseArrowY2);
        }

        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double endX, double endY, Color foreColor) {

        }
    },
    ARROW_DOUBLE{
        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double currentX, double currentY) {
            double angle = Math.atan2(currentY - startY, currentX - startX);
            double dx = currentX - startX;
            double dy = currentY - startY;
            // 计算正交方向向量 (垂直于主线段)
            double perpX = -dy;  // 逆时针旋转90度
            double perpY = dx;
            // 标准化正交向量，保证平行线之间的距离一致
            double length = Math.sqrt(perpX * perpX + perpY * perpY);
            perpX /= length;
            perpY /= length;
            // 偏移量，调整两条平行线的距离
            double offset = 3;
            gc.strokeLine(startX + perpX * offset, startY + perpY * offset,
                    currentX + perpX * offset, currentY + perpY * offset);
            gc.strokeLine(startX - perpX * offset, startY - perpY * offset,
                    currentX - perpX * offset, currentY - perpY * offset);
            // 箭头的大小
            Random random=new Random();
            double arrowSize = 10;
            // 计算箭头两侧的角度
            double delta = Math.toRadians(random.nextInt(30,45));
            double arrowAngle1 = angle + delta;  // 箭头左侧的角度
            double arrowAngle2 = angle - delta;  // 箭头右侧的角度
            // 计算箭头的两个点
            double x1 = currentX - arrowSize * Math.cos(arrowAngle1);
            double y1 = currentY - arrowSize * Math.sin(arrowAngle1);
            double x2 = currentX - arrowSize * Math.cos(arrowAngle2);
            double y2 = currentY - arrowSize * Math.sin(arrowAngle2);
            // 绘制箭头的两条斜线
            gc.strokeLine(currentX+10*Math.cos(angle), currentY+10*Math.sin(angle), x1, y1);  // 左侧的箭头
            gc.strokeLine(currentX+10*Math.cos(angle), currentY+10*Math.sin(angle), x2, y2);  // 右侧的箭头
        }

        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double endX, double endY, Color foreColor) {

        }
    },
    ARROW_DOUBLE_DASHED{
        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double currentX, double currentY) {
            gc.setLineDashes(10,10);
            // 绘制箭头的主干（直线部分）
            double dx = currentX - startX;
            double dy = currentY - startY;
            // 计算正交方向向量 (垂直于主线段)
            double perpX = -dy;  // 逆时针旋转90度
            double perpY = dx;
            // 标准化正交向量，保证平行线之间的距离一致
            double length = Math.sqrt(perpX * perpX + perpY * perpY);
            perpX /= length;
            perpY /= length;
            // 偏移量，调整两条平行线的距离
            double offset = 3;
            gc.strokeLine(startX + perpX * offset, startY + perpY * offset,
                    currentX + perpX * offset, currentY + perpY * offset);
            gc.strokeLine(startX - perpX * offset, startY - perpY * offset,
                    currentX - perpX * offset, currentY - perpY * offset);
            // 箭头的大小
            Random random=new Random();
            double arrowSize = 10;
            // 计算箭头的角度
            double angle = Math.atan2(currentY - startY, currentX - startX);
            // 计算箭头两侧的角度
            double delta = Math.toRadians(random.nextInt(30,45));
            double arrowAngle1 = angle + delta;  // 箭头左侧的角度
            double arrowAngle2 = angle - delta;  // 箭头右侧的角度
            // 计算箭头的两个点
            double x1 = currentX - arrowSize * Math.cos(arrowAngle1);
            double y1 = currentY - arrowSize * Math.sin(arrowAngle1);
            double x2 = currentX - arrowSize * Math.cos(arrowAngle2);
            double y2 = currentY - arrowSize * Math.sin(arrowAngle2);
            // 绘制箭头的两条斜线
            gc.setLineDashes(0);
            gc.strokeLine(currentX+10*Math.cos(angle), currentY+10*Math.sin(angle), x1, y1);  // 左侧的箭头
            gc.strokeLine(currentX+10*Math.cos(angle), currentY+10*Math.sin(angle), x2, y2);  // 右侧的箭头
        }

        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double endX, double endY, Color foreColor) {

        }
    },
    ARROW_DOUBLE_TWO_DIR{
        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double currentX, double currentY) {
            // 绘制箭头的主干（直线部分）
            double dx = currentX - startX;
            double dy = currentY - startY;
            // 计算正交方向向量 (垂直于主线段)
            double perpX = -dy;  // 逆时针旋转90度
            double perpY = dx;
            // 标准化正交向量，保证平行线之间的距离一致
            double length = Math.sqrt(perpX * perpX + perpY * perpY);
            perpX /= length;
            perpY /= length;
            // 偏移量，调整两条平行线的距离
            double offset = 3;
            gc.strokeLine(startX + perpX * offset, startY + perpY * offset,
                    currentX + perpX * offset, currentY + perpY * offset);
            gc.strokeLine(startX - perpX * offset, startY - perpY * offset,
                    currentX - perpX * offset, currentY - perpY * offset);
            // 箭头的大小
            Random random=new Random();
            double arrowSize = 10;
            // 计算箭头的角度
            double angle = Math.atan2(currentY - startY, currentX - startX);
            // 计算箭头两侧的角度
            double delta = Math.toRadians(random.nextInt(30,45));
            double arrowAngle1 = angle + delta;  // 箭头左侧的角度
            double arrowAngle2 = angle - delta;  // 箭头右侧的角度
            // 计算箭头的两个点
            double x1 = currentX - arrowSize * Math.cos(arrowAngle1);
            double y1 = currentY - arrowSize * Math.sin(arrowAngle1);
            double x2 = currentX - arrowSize * Math.cos(arrowAngle2);
            double y2 = currentY - arrowSize * Math.sin(arrowAngle2);
            // 绘制箭头的两条斜线
            gc.strokeLine(currentX+10*Math.cos(angle), currentY+10*Math.sin(angle), x1, y1);  // 左侧的箭头
            gc.strokeLine(currentX+10*Math.cos(angle), currentY+10*Math.sin(angle), x2, y2);  // 右侧的箭头
            // 反向箭头末端位置
            double reverseArrowX1 = startX + arrowSize * Math.cos(arrowAngle1);
            double reverseArrowY1 = startY + arrowSize * Math.sin(arrowAngle1);
            double reverseArrowX2 = startX + arrowSize * Math.cos(arrowAngle2);
            double reverseArrowY2 = startY + arrowSize * Math.sin(arrowAngle2);
            // 绘制反向箭头
            gc.strokeLine(startX-10*Math.cos(angle), startY-10*Math.sin(angle), reverseArrowX1, reverseArrowY1);
            gc.strokeLine(startX-10*Math.cos(angle), startY-10*Math.sin(angle), reverseArrowX2, reverseArrowY2);
        }

        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double endX, double endY, Color foreColor) {

        }
    },
    ARROW_FILLED{
        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double currentX, double currentY) {

        }

        @Override
        public void draw(GraphicsContext g2, double startX, double startY, double endX, double endY, Color foreColor) {
            // 计算箭头的角度
            g2.setLineDashes(0);
            double angle = Math.atan2(endY - startY, endX - startX);
            double arrowLength=20;
            // 绘制箭头的主体线
            g2.setStroke(foreColor);
            g2.setLineWidth(2);
            g2.strokeLine(startX, startY, endX, endY);
            // 计算箭头头部的两个点
            double arrowAngle = Math.toRadians(15); // 箭头两侧的角度（可以根据需要调整）
            double x1 = endX - arrowLength * Math.cos(angle - arrowAngle);
            double y1 = endY - arrowLength * Math.sin(angle - arrowAngle);
            double x2 = endX - arrowLength * Math.cos(angle + arrowAngle);
            double y2 = endY - arrowLength * Math.sin(angle + arrowAngle);
            // 绘制箭头的两侧
            g2.setFill(foreColor);
            g2.fillPolygon(new double[]{endX, x1, x2}, new double[]{endY, y1, y2}, 3);
        }
    },
    ARROW_EMPTY{
        @Override
        public void draw(GraphicsContext g2, double startX, double startY, double endX, double endY) {
            g2.setLineDashes(0);
            double angle = Math.atan2(endY - startY, endX - startX);
            double arrowLength=20;
            // 绘制箭头的主体线
            g2.setLineWidth(2);
            g2.strokeLine(startX, startY, endX, endY);
            // 计算箭头头部的两个点
            double arrowAngle = Math.toRadians(15); // 箭头两侧的角度（可以根据需要调整）
            double x1 = endX - arrowLength * Math.cos(angle - arrowAngle);
            double y1 = endY - arrowLength * Math.sin(angle - arrowAngle);
            double x2 = endX - arrowLength * Math.cos(angle + arrowAngle);
            double y2 = endY - arrowLength * Math.sin(angle + arrowAngle);
            // 绘制箭头的两侧
            g2.strokePolygon(new double[]{endX, x1, x2}, new double[]{endY, y1, y2}, 3);
        }

        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double endX, double endY, Color foreColor) {

        }
    },
    RECTANGLE_DASHED{
        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double currentX, double currentY) {
            double width = Math.abs(currentX - startX);
            double height = Math.abs(currentY - startY);
            gc.setLineDashes(10,10);
            gc.strokeRect(Math.min(startX,currentX), Math.min(startY,currentY),width,height);
        }

        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double endX, double endY, Color foreColor) {

        }
    },
    RECTANGLE_DASHED_FILLED{
        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double currentX, double currentY) {

        }

        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double endX, double endY, Color foreColor) {
            double width = Math.abs(endX - startX);
            double height = Math.abs(endY - startY);
            gc.setLineDashes(10,10);
            gc.strokeRect(Math.min(startX,endX), Math.min(startY,endY),width,height);
            gc.setFill(Color.rgb((int) (foreColor.getRed() * 255), ((int) foreColor.getGreen() * 255),
                    (int) (foreColor.getBlue() * 255), 0.05));
            gc.fillRect(Math.min(startX,endX), Math.min(startY,endY),width,height);
        }
    },
    RECTANGLE_ROUND_DASHED{
        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double currentX, double currentY) {
            double width = Math.abs(currentX - startX);
            double height = Math.abs(currentY - startY);
            gc.setLineDashes(10,10);
            gc.strokeRoundRect(Math.min(startX,currentX), Math.min(startY,currentY),width,height,15,15);
        }

        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double endX, double endY, Color foreColor) {

        }
    },
    RECTANGLE_ROUND_DASHED_FILLED{
        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double currentX, double currentY) {

        }

        @Override
        public void draw(GraphicsContext gc, double startX, double startY, double endX, double endY, Color foreColor) {
            double width = Math.abs(endX - startX);
            double height = Math.abs(endY - startY);
            gc.setLineDashes(10,10);
            gc.strokeRoundRect(Math.min(startX,endX), Math.min(startY,endY),width,height,10,10);
            gc.setFill(Color.rgb((int) (foreColor.getRed() * 255), ((int) foreColor.getGreen() * 255),
                    (int) (foreColor.getBlue() * 255), 0.05));
            gc.fillRoundRect(Math.min(startX,endX), Math.min(startY,endY),width,height,15,15);
        }
    }
}
