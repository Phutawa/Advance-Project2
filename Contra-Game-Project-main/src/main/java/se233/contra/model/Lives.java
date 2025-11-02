package se233.contra.model;

import se233.contra.util.GameLogger;

public class Lives {
    private int remainingLives;

    public Lives() {
        this.remainingLives = 999;
    }

    public void loseLife() {
        if (remainingLives > 0) {
            remainingLives--;
            GameLogger.warn("Life lost! Remaining: " + remainingLives);
        }
    }

    public boolean hasLivesLeft() {
        return remainingLives > 0;
    }

    public int getRemainingLives() {
        return remainingLives;
    }
}