package net.jackchuan.screencapturetool.test;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RocketFireworks extends Application {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private List<Rocket> rockets = new ArrayList<>();
    private List<Particle> particles = new ArrayList<>();
    private List<SmallHeart> smallHearts = new ArrayList<>();
    private String[] lines = {"", ""};

    @Override
    public void start(Stage primaryStage) {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        AnimationTimer timer = new AnimationTimer() {
            private long lastRocketTime = 0;
            private long lastSmallHeartTime = 0;
            private long startTime = -1;

            @Override
            public void handle(long now) {
                if (startTime == -1) {
                    startTime = now;
                }
                double seconds = (now - startTime) / 1_000_000_000.0;
                // 清屏（仅清除动态部分）
                gc.setFill(Color.BLACK);
                gc.fillRect(0, 0, WIDTH, HEIGHT);

                // 始终绘制主心形
                drawHeart(gc, WIDTH / 2.0, HEIGHT / 2.0, 10, Color.RED, seconds);

                // 发射火箭
                if (seconds > 3 && now - lastRocketTime > 1_000_000_000) { // 每秒发射一次
                    spawnRockets();
                    lastRocketTime = now;
                    spawnRockets();
                }

                // 随机生成小心形
                if (seconds > 3 && now - lastSmallHeartTime > 500_000_000L) { // 每隔0.5秒生成一个小心形
                    spawnSmallHeart(now);
                    lastSmallHeartTime = now;
                }

                // 更新火箭和粒子
                updateRockets(gc);
                updateParticles(gc);
                if(((now - startTime) / 1_000_000_000.0)%2==0){
                    checkRender();
                }

                // 更新小心形
                updateSmallHearts(gc, now);
            }
        };

        timer.start();

        Pane root = new Pane(canvas);
        Scene scene = new Scene(root, WIDTH, HEIGHT, Color.BLACK);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Heart and Fireworks");
        primaryStage.show();
    }

    private void checkRender() {
        for(Rocket rocket:rockets){
            if(!rocket.isShouldRender()&&Math.random()<=0.6){
                rocket.setShouldRender(true);
            }
        }
    }

    private void drawHeart(GraphicsContext gc, double centerX, double centerY, double scale, Color color, double seconds) {
        gc.setStroke(color);
        gc.setLineWidth(2);
        gc.beginPath();
        if (seconds <= 3) {
            double t1 = seconds / 3f * 2 * Math.PI;
            for (double t = 0; t <= t1; t += 0.01) {
                double x = 16 * Math.pow(Math.sin(t), 3);
                double y = 13 * Math.cos(t) - 5 * Math.cos(2 * t) - 2 * Math.cos(3 * t) - Math.cos(4 * t);
                double px = centerX + x * scale;
                double py = centerY - y * scale;
                if (t == 0) {
                    gc.moveTo(px, py);
                } else {
                    gc.lineTo(px, py);
                }
            }
        } else {
            for (double t = 0; t <= 2 * Math.PI; t += 0.01) {
                double x = 16 * Math.pow(Math.sin(t), 3);
                double y = 13 * Math.cos(t) - 5 * Math.cos(2 * t) - 2 * Math.cos(3 * t) - Math.cos(4 * t);
                double px = centerX + x * scale;
                double py = centerY - y * scale;
                if (t == 0) {
                    gc.moveTo(px, py);
                } else {
                    gc.lineTo(px, py);
                }
            }
        }

        gc.stroke();
    }

    private void spawnRockets() {
        Random random = new Random();
        int numRockets = random.nextInt(10); // 每次生成 3 个火箭
        for (int i = 0; i < numRockets; i++) {
            double x = random.nextDouble() * WIDTH;
            double targetY = HEIGHT / 3.0 + (2 * random.nextDouble() - 1) * (HEIGHT / 100.0);
            rockets.add(new Rocket(x, HEIGHT, targetY, Color.WHITE, Math.random() <= 0.32f));
        }
    }

    private void updateRockets(GraphicsContext gc) {
        List<Rocket> explodedRockets = new ArrayList<>();
        for (Rocket rocket : rockets) {
            if(rocket.isShouldRender()){
                rocket.update();
                rocket.draw(gc);
                if (rocket.hasExploded()) {
                    explodedRockets.add(rocket);
                    particles.addAll(createFirework(rocket.x, rocket.y));
                }
            }
        }
        rockets.removeAll(explodedRockets);
    }

    private List<Particle> createFirework(double centerX, double centerY) {
        List<Particle> fireworkParticles = new ArrayList<>();
        Random random = new Random();
        int numParticles = 100;
        for (int i = 0; i < numParticles; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double speed = 2 + random.nextDouble() * 3;
            double dx = Math.cos(angle) * speed;
            double dy = Math.sin(angle) * speed;
            Color color = Color.hsb(random.nextDouble() * 360, 1.0, 1.0);
            fireworkParticles.add(new Particle(centerX, centerY, dx, dy, color));
        }
        return fireworkParticles;
    }

    private void updateParticles(GraphicsContext gc) {
        particles.removeIf(Particle::isFadedOut);
        for (Particle particle : particles) {
            particle.update();
            particle.draw(gc);
        }
    }

    private void spawnSmallHeart(long now) {
        Random random = new Random();
        double x = random.nextDouble() * WIDTH;
        double y = random.nextDouble() * HEIGHT;
        smallHearts.add(new SmallHeart(x, y, 1, now));
    }

    private void updateSmallHearts(GraphicsContext gc, long now) {
        List<SmallHeart> expiredHearts = new ArrayList<>();
        for (SmallHeart heart : smallHearts) {
            heart.update(gc);
            if (now - heart.creationTime > 3_000_000_000L) { // 超过3秒自动清除
                expiredHearts.add(heart);
            }
        }
        smallHearts.removeAll(expiredHearts);
    }

    public static void main(String[] args) {
        launch(args);
    }

    // 火箭类
    static class Rocket {
        double x, y, targetY;
        Color color;
        private boolean shouldRender = true;
        private boolean exploded = false;

        public Rocket(double x, double y, double targetY, Color color, boolean shouldRender) {
            this.x = x;
            this.y = y;
            this.targetY = targetY;
            this.color = color;
            this.shouldRender = shouldRender;
        }

        public void update() {
            if (y > targetY) {
                y -= 5; // 向上移动
            } else {
                exploded = true;
            }
        }

        public void draw(GraphicsContext gc) {
            if (!exploded) {
                gc.setFill(color);
                gc.fillRect(x - 2, y - 10, 4, 20); // 火箭形状
            }
        }

        public boolean hasExploded() {
            return exploded;
        }

        public boolean isShouldRender() {
            return shouldRender;
        }

        public void setShouldRender(boolean shouldRender) {
            this.shouldRender = shouldRender;
        }
    }

    // 粒子类
    static class Particle {
        double x, y, dx, dy;
        Color color;
        double life = 1.0;

        public Particle(double x, double y, double dx, double dy, Color color) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
            this.color = color;
        }

        public void update() {
            x += dx;
            y += dy;
            life -= 0.02; // 粒子逐渐消失
        }

        public void draw(GraphicsContext gc) {
            if (life > 0) {
                gc.setFill(color.deriveColor(0, 1, 1, life));
                gc.fillOval(x, y, 4, 4);
            }
        }

        public boolean isFadedOut() {
            return life <= 0;
        }
    }

    // 小心形类
    static class SmallHeart {
        double x, y, scale;
        long creationTime;

        public SmallHeart(double x, double y, double scale, long creationTime) {
            this.x = x;
            this.y = y;
            this.scale = scale;
            this.creationTime = creationTime;
        }

        public void update(GraphicsContext gc) {
            gc.setStroke(Color.PINK);
            gc.setLineWidth(1);
            gc.beginPath();
            for (double t = 0; t <= 2 * Math.PI; t += 0.01) {
                double px = x + 16 * Math.pow(Math.sin(t), 3) * scale;
                double py = y - (13 * Math.cos(t) - 5 * Math.cos(2 * t) - 2 * Math.cos(3 * t) - Math.cos(4 * t)) * scale;
                if (t == 0) {
                    gc.moveTo(px, py);
                } else {
                    gc.lineTo(px, py);
                }
            }
            gc.stroke();
        }
    }
}
