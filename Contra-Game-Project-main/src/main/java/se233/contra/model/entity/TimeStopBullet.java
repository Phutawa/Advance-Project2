package se233.contra.model.entity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class TimeStopBullet extends EnemyBullet {
    private int lifetime;

    public TimeStopBullet(double x, double y, double directionX, double directionY) {
        super(x, y, directionX, directionY);
        this.damage = 999; // One-hit kill
        this.lifetime = 0;
    }

    @Override
    public void update() {
        lifetime++;

        // Move bullet in straight line (no tracking)
        x += velocityX;
        y += velocityY;

        // Deactivate if out of bounds or too old
        if (x < -50 || x > 850 || y < -50 || y > 650 || lifetime > 600) {
            active = false;
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (active) {
            // Render as a distinctive purple/magenta bullet

            // Outer glow (pulsing effect)
            double pulseSize = Math.sin(lifetime * 0.2) * 2 + 6;
            gc.setFill(Color.rgb(200, 0, 255, 0.5));
            gc.fillOval(x - pulseSize, y - pulseSize, width + pulseSize * 2, height + pulseSize * 2);

            // Main body - bright purple
            gc.setFill(Color.rgb(200, 0, 255));
            gc.fillOval(x, y, width, height);

            // Inner core - white (pulsing)
            double coreSize = Math.sin(lifetime * 0.3) * 2 + 4;
            gc.setFill(Color.WHITE);
            gc.fillOval(x + (width - coreSize) / 2, y + (height - coreSize) / 2, coreSize, coreSize);

            // Outline - magenta
            gc.setStroke(Color.MAGENTA);
            gc.setLineWidth(2);
            gc.strokeOval(x, y, width, height);

            // Draw trail effect every few frames
            if (lifetime % 3 == 0) {
                gc.setFill(Color.rgb(200, 0, 255, 0.3));
                gc.fillOval(x + width / 4, y + height / 4, width / 2, height / 2);
            }
        }
    }
}