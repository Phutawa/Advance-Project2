package se233.contra.model.entity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public abstract class Boss extends Entity {
    protected int health;
    protected int maxHealth;
    protected int scoreValue;
    protected boolean isDefeated;
    protected boolean scoreAwarded;  // Add this flag

    public abstract void attack();
    public abstract void move();

    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            health = 0;
            isDefeated = true;
        }
    }

    public boolean isDefeated() {
        return health <= 0;
    }

    // Add this method
    public boolean hasAwardedScore() {
        return scoreAwarded;
    }

    // Add this method
    public void awardScore() {
        scoreAwarded = true;
    }

    public int getScoreValue() {
        return scoreValue;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }
}