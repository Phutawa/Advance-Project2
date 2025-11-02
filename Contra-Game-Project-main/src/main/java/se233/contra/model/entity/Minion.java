package se233.contra.model.entity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import se233.contra.util.GameLogger;

public class Minion extends Entity {
    private int health;
    private int maxHealth;
    private int scoreValue;
    private double moveSpeed;
    private int type; // 1 = regular, 2 = second-tier

    public Minion(double x, double y, int type) {
        this.x = x;
        this.y = y;
        this.type = type;

        // ✅ Updated health values for new damage system
        if (type == 1) {
            this.health = 20;      // Regular minion: 1 hit
            this.maxHealth = 20;
            this.scoreValue = 1;
            this.width = 30;
            this.height = 30;
            this.moveSpeed = 1.5;
        } else {
            this.health = 40;      // Second-tier: 2 hits
            this.maxHealth = 40;
            this.scoreValue = 2;
            this.width = 40;
            this.height = 40;
            this.moveSpeed = 1.0;
        }

        this.active = true;
    }

    @Override
    public void update() {
        // Move toward left (toward player)
        x -= moveSpeed;

        // Simple up-down movement pattern
        y += Math.sin(x * 0.05) * 0.5;

        // Deactivate if out of bounds (left side)
        if (x + width < 0) {
            active = false;
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (active) {
            // Different colors based on type
            if (type == 1) {
                // Regular minion - Green
                gc.setFill(Color.rgb(50, 200, 50));
            } else {
                // Second-tier minion - Purple
                gc.setFill(Color.rgb(150, 50, 200));
            }

            // Draw minion body
            gc.fillRect(x, y, width, height);

            // Draw outline
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.strokeRect(x, y, width, height);

            // Draw health bar
            double healthBarWidth = width * ((double) health / maxHealth);
            gc.setFill(Color.RED);
            gc.fillRect(x, y - 8, width, 4);
            gc.setFill(Color.GREEN);
            gc.fillRect(x, y - 8, healthBarWidth, 4);

            // Draw eyes to make it look more like an enemy
            gc.setFill(Color.RED);
            double eyeSize = type == 1 ? 6 : 8;
            gc.fillOval(x + width * 0.25 - eyeSize/2, y + height * 0.4, eyeSize, eyeSize);
            gc.fillOval(x + width * 0.75 - eyeSize/2, y + height * 0.4, eyeSize, eyeSize);
        }
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            active = false;
            GameLogger.debug("Minion destroyed. Score value: " + scoreValue);
        } else {
            GameLogger.debug("Minion took " + damage + " damage. Health remaining: " + health);
        }
    }

    public int getScoreValue() {
        return scoreValue;
    }

    public int getType() {
        return type;
    }

    public int getHealth() {
        return health;
    }
}