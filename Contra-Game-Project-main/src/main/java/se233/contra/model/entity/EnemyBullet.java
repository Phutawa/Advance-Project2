package se233.contra.model.entity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class EnemyBullet extends Bullet {

    public EnemyBullet(double x, double y, double directionX, double directionY) {
        super(x, y, directionX, directionY);
        this.damage = 999; // One-hit kill
    }

    @Override
    public void render(GraphicsContext gc) {
        if (active) {
            // Draw enemy bullet as a red circle (different from player bullets)
            gc.setFill(Color.RED);
            gc.fillOval(x, y, width, height);

            // Add orange outline
            gc.setStroke(Color.ORANGE);
            gc.setLineWidth(1);
            gc.strokeOval(x, y, width, height);
        }
    }
}