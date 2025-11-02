package se233.contra.model.entity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Bullet extends Entity {
    private double speed;
    protected int damage;

    public Bullet(double x, double y, double directionX, double directionY) {
        this.x = x;
        this.y = y;
        this.width = 8;
        this.height = 8;
        this.speed = 5;
        this.damage = 20;  // ✅ Increased from 5 to 20 for better gameplay
        this.active = true;
        this.velocityX = directionX * speed;
        this.velocityY = directionY * speed;
    }

    @Override
    public void update() {
        x += velocityX;
        y += velocityY;

        // Deactivate if out of bounds
        if (x < 0 || x > 800 || y < 0 || y > 600) {
            active = false;
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (active) {
            gc.setFill(Color.YELLOW);
            gc.fillOval(x, y, width, height);

            gc.setStroke(Color.WHITE);
            gc.setLineWidth(1);
            gc.strokeOval(x, y, width, height);
        }
    }

    public int getDamage() {
        return damage;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}