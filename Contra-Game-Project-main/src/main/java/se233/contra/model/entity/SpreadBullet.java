package se233.contra.model.entity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Special Attack Bullet - Spread Shot
 * Fires in different angles for wider coverage
 */
public class SpreadBullet extends Bullet {
    private double angle; // Angle of spread

    /**
     * Create a spread bullet
     * @param x Starting X position
     * @param y Starting Y position
     * @param directionX Base direction X (-1 or 1)
     * @param directionY Base direction Y
     * @param spreadAngle Angle offset in degrees (-30, 0, +30 for 3-bullet spread)
     */
    public SpreadBullet(double x, double y, double directionX, double directionY, double spreadAngle) {
        super(x, y, directionX, directionY);

        this.angle = Math.toRadians(spreadAngle);
        this.damage = 30; // Higher damage than normal bullets (20)
        this.width = 10;  // Slightly larger
        this.height = 10;

        // Calculate velocity with angle
        double baseSpeed = 10;
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        // Apply rotation to direction
        this.velocityX = (directionX * cos - directionY * sin) * baseSpeed;
        this.velocityY = (directionX * sin + directionY * cos) * baseSpeed;
    }

    @Override
    public void render(GraphicsContext gc) {
        if (active) {
            // Render as blue/cyan bullet (different from normal yellow)
            gc.setFill(Color.CYAN);
            gc.fillOval(x - 1, y - 1, width + 2, height + 2);

            // Inner bright core
            gc.setFill(Color.WHITE);
            gc.fillOval(x + 2, y + 2, width - 4, height - 4);

            // Glowing outline
            gc.setStroke(Color.LIGHTBLUE);
            gc.setLineWidth(2);
            gc.strokeOval(x, y, width, height);
        }
    }

    @Override
    public int getDamage() {
        return damage; // 30 damage (vs normal 20)
    }
}