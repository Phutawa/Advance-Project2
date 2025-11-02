package se233.contra.model;

import se233.contra.util.GameLogger;

public class Score {
    private int currentScore;

    public Score() {
        this.currentScore = 0;
    }

    public void addScore(int points) {
        currentScore += points;
        GameLogger.info("Score increased by " + points + ". Total: " + currentScore);
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public void reset() {
        currentScore = 0;
    }
}