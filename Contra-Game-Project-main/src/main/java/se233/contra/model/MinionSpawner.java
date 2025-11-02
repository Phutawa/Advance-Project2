package se233.contra.model;

import se233.contra.model.entity.Minion;
import se233.contra.util.GameLogger;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages minion wave spawning before boss fights
 */
public class MinionSpawner {
    private List<MinionWave> waves;
    private int currentWaveIndex;
    private int spawnTimer;
    private int spawnDelay;
    private boolean wavesComplete;

    public MinionSpawner(int bossLevel) {
        this.waves = new ArrayList<>();
        this.currentWaveIndex = 0;
        this.spawnTimer = 0;
        this.spawnDelay = 60; // Spawn interval (1 second at 60 FPS)
        this.wavesComplete = false;

        createWavesForBoss(bossLevel);
    }

    /**
     * Create minion waves based on boss level
     */
    private void createWavesForBoss(int bossLevel) {
        switch (bossLevel) {
            case 1: // Defense Wall Boss - Easy waves
                // Wave 1: 3 regular minions
                waves.add(new MinionWave(3, 1, 800, 400));
                // Wave 2: 5 regular minions
                waves.add(new MinionWave(5, 1, 800, 350));
                // Wave 3: 2 second-tier minions
                waves.add(new MinionWave(2, 2, 800, 300));
                break;

            case 2: // Java Boss - Medium waves
                // Wave 1: 4 regular minions
                waves.add(new MinionWave(4, 1, 800, 400));
                // Wave 2: 3 second-tier minions
                waves.add(new MinionWave(3, 2, 800, 350));
                // Wave 3: Mixed wave
                waves.add(new MinionWave(5, 1, 800, 300));
                waves.add(new MinionWave(2, 2, 800, 250));
                break;

            case 3: // Custom Boss - Hard waves
                // Wave 1: Large group of regular
                waves.add(new MinionWave(6, 1, 800, 400));
                // Wave 2: Many second-tier
                waves.add(new MinionWave(4, 2, 800, 350));
                // Wave 3: Final challenge
                waves.add(new MinionWave(3, 1, 800, 300));
                waves.add(new MinionWave(3, 2, 800, 250));
                break;
        }

        GameLogger.info("Created " + waves.size() + " minion waves for Boss Level " + bossLevel);
    }

    /**
     * Update spawner and spawn minions when ready
     */
    public void update(List<Minion> activeMinions) {
        if (wavesComplete) {
            return;
        }

        spawnTimer++;

        // Check if current wave is complete (all minions spawned)
        if (currentWaveIndex < waves.size()) {
            MinionWave currentWave = waves.get(currentWaveIndex);

            if (currentWave.isComplete()) {
                // Move to next wave
                currentWaveIndex++;
                spawnTimer = 0;

                if (currentWaveIndex >= waves.size()) {
                    // All waves spawned, now wait for them to be cleared
                    GameLogger.info("All minion waves spawned! Waiting for clear...");
                }
            } else if (spawnTimer >= spawnDelay) {
                // Spawn next minion from current wave
                Minion newMinion = currentWave.spawnNext();
                if (newMinion != null) {
                    activeMinions.add(newMinion);
                    GameLogger.debug("Spawned minion - Wave " + (currentWaveIndex + 1) +
                            ", Type: " + newMinion.getType());
                }
                spawnTimer = 0;
            }
        }
    }

    /**
     * Check if all waves are complete AND all minions are cleared
     */
    public boolean areWavesComplete(List<Minion> activeMinions) {
        // All waves must be spawned
        boolean allWavesSpawned = currentWaveIndex >= waves.size();

        // All active minions must be cleared
        boolean allMinionsCleared = activeMinions.isEmpty();

        if (allWavesSpawned && allMinionsCleared && !wavesComplete) {
            wavesComplete = true;
            GameLogger.info("✓ All minion waves cleared! Boss can now appear!");
        }

        return wavesComplete;
    }

    /**
     * Get current wave number for display
     */
    public int getCurrentWaveNumber() {
        return Math.min(currentWaveIndex + 1, waves.size());
    }

    /**
     * Get total number of waves
     */
    public int getTotalWaves() {
        return waves.size();
    }

    /**
     * Inner class representing a single wave of minions
     */
    private static class MinionWave {
        private int minionCount;
        private int minionType;
        private double spawnX;
        private double spawnY;
        private int spawned;

        public MinionWave(int count, int type, double x, double y) {
            this.minionCount = count;
            this.minionType = type;
            this.spawnX = x;
            this.spawnY = y;
            this.spawned = 0;
        }

        public Minion spawnNext() {
            if (spawned < minionCount) {
                // Vary spawn position slightly
                double yOffset = (spawned % 3) * 40;
                spawned++;
                return new Minion(spawnX, spawnY + yOffset, minionType);
            }
            return null;
        }

        public boolean isComplete() {
            return spawned >= minionCount;
        }
    }
}