package se233.contra;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se233.contra.model.Score;

import static org.junit.jupiter.api.Assertions.*;


public class ScoreTest {

    private Score score;

    @BeforeEach
    public void setUp() {
        score = new Score();
    }

    @Test
    public void testScoreInitialValue() {
        assertEquals(0, score.getCurrentScore(),
                "Score should start at 0");
    }

    @Test
    public void testAddScore() {
        score.addScore(10);
        assertEquals(10, score.getCurrentScore(), "Score should be 10 after adding 10 points");
    }

    @Test
    public void testAddMultipleScores() {
        score.addScore(5);
        score.addScore(10);
        score.addScore(15);

        assertEquals(30, score.getCurrentScore(), "Score should be cumulative (5 + 10 + 15 = 30)");
    }

    @Test
    public void testMinionScoring() {
        // Regular minion: 1 point
        score.addScore(1);
        assertEquals(1, score.getCurrentScore(), "Regular minion should give 1 point");

        // Second-tier minion: 2 points
        score.addScore(2);
        assertEquals(3, score.getCurrentScore(), "Second-tier minion should give 2 points");
    }

    @Test
    public void testBossScoring() {
        score.addScore(2);
        assertEquals(2, score.getCurrentScore(), "Boss 1 should give 2 points");

        score.addScore(3);
        assertEquals(5, score.getCurrentScore(), "Boss 2 should give 3 points");

        score.addScore(5);
        assertEquals(10, score.getCurrentScore(),
                "Boss 3 should give 5 points");
    }

    @Test
    public void testResetScore() {
        score.addScore(100);
        score.reset();

        assertEquals(0, score.getCurrentScore(),
                "Score should reset to 0");
    }
}