package se233.contra.model.entity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import se233.contra.util.AnimationManager;
import se233.contra.util.GameLogger;
import se233.contra.util.SpriteLoader;

import java.util.ArrayList;
import java.util.List;

public class JavaBoss extends Boss {
    private double moveSpeed;
    private int attackCooldown;
    private int attackTimer;
    private double initialX;
    private double initialY;

    // Enhanced movement system
    private int movementPattern;
    private int patternTimer;
    private int patternDuration;
    private double time;

    // ✅ THREE ANIMATIONS
    private AnimationManager idleAnimation;      // Normal movement
    private AnimationManager summonAnimation;    // Summoning minions
    private AnimationManager deathAnimation;     // Death animation
    private AnimationManager currentAnimation;   // Current active animation

    // ✅ BOSS STATE MACHINE
    private enum BossState {
        IDLE,           // Normal movement
        SUMMONING,      // Spawning minions
        DEAD            // Defeated
    }
    private BossState currentState;
    private int summonTimer;
    private int summonDuration;
    private boolean summonComplete;

    // ✅ MINION SPAWNING
    private List<Minion> spawnedMinions;
    private int minionSpawnCount;
    private static final int MINIONS_PER_SUMMON = 2;
    private static final int SUMMON_INTERVAL = 180; // 3 seconds

    // Death animation control
    private int deathTimer;
    private static final int DEATH_DURATION = 120; // 2 seconds
    private boolean deathComplete;

    public JavaBoss(double x, double y) {
        this.x = x;
        this.y = y;
        this.initialX = x;
        this.initialY = y;
        this.width = 120;
        this.height = 100;
        this.health = 150;
        this.maxHealth = 150;
        this.scoreValue = 3;
        this.moveSpeed = 0.8;
        this.active = true;
        this.attackCooldown = 45;
        this.attackTimer = 0;
        this.movementPattern = 0;
        this.patternTimer = 0;
        this.patternDuration = 300;
        this.time = 0;

        // ✅ Initialize state machine
        this.currentState = BossState.IDLE;
        this.summonTimer = SUMMON_INTERVAL;
        this.summonDuration = 0;
        this.summonComplete = false;
        this.spawnedMinions = new ArrayList<>();
        this.minionSpawnCount = 0;

        this.deathTimer = 0;
        this.deathComplete = false;

        loadAnimations();
    }

    private void loadAnimations() {
        try {
            String spritePath = "/se233/sprites/bosses/java.png";
            int frameWidth = 102;
            int frameHeight = 113;

            Image idleFrame = SpriteLoader.extractFrame(spritePath, 0, 0, frameWidth, frameHeight);
            List<Image> idleFrames = new ArrayList<>();
            idleFrames.add(idleFrame);
            idleAnimation = new AnimationManager(idleFrames, 30);

            Image summonFrame = SpriteLoader.extractFrame(spritePath, 1, 0, frameWidth, frameHeight);
            List<Image> summonFrames = new ArrayList<>();
            summonFrames.add(summonFrame);
            summonAnimation = new AnimationManager(summonFrames, 10);

            Image deathFrame = SpriteLoader.extractFrame(spritePath, 2, 0, frameWidth, frameHeight);
            List<Image> deathFrames = new ArrayList<>();
            deathFrames.add(deathFrame);
            deathAnimation = new AnimationManager(deathFrames, 20);

            currentAnimation = idleAnimation;

            GameLogger.info("Java Boss animations loaded: idle, summon, death");

        } catch (Exception e) {
            GameLogger.error("Failed to load Java Boss animations", e);
        }
    }

    @Override
    public void takeDamage(int damage) {
        if (currentState == BossState.DEAD) {
            return; // Don't take damage when dead
        }

        super.takeDamage(damage);

        if (health <= 0 && currentState != BossState.DEAD) {
            transitionToDeath();
        }
    }

    private void transitionToDeath() {
        currentState = BossState.DEAD;
        currentAnimation = deathAnimation;
        deathTimer = 0;
        deathComplete = false;
        GameLogger.info("Java Boss defeated! Playing death animation...");
    }

    @Override
    public void attack() {
        // Start summoning minions
        if (currentState == BossState.IDLE) {
            startSummoning();
        }
    }

    private void startSummoning() {
        currentState = BossState.SUMMONING;
        currentAnimation = summonAnimation;
        summonDuration = 0;
        summonComplete = false;
        minionSpawnCount = 0;
        GameLogger.info("Java Boss summoning minions!");
    }

    @Override
    public void move() {
        if (currentState != BossState.IDLE) {
            return; // Don't move while summoning or dead
        }

        time += 0.02 * moveSpeed;
        patternTimer++;

        if (patternTimer >= patternDuration) {
            patternTimer = 0;
            movementPattern = (movementPattern + 1) % 3;
            GameLogger.debug("Java Boss switching to pattern: " + movementPattern);
        }

        double offsetX = 0;
        double offsetY = 0;

        switch (movementPattern) {
            case 0:  // Figure-8 pattern
                offsetX = Math.sin(time) * 80;
                offsetY = Math.sin(time * 2) * 60;
                break;

            case 1:  // Circular pattern
                offsetX = Math.cos(time) * 70;
                offsetY = Math.sin(time) * 70;
                break;

            case 2:  // Serpentine wave
                offsetX = Math.sin(time * 0.7) * 60;
                offsetY = Math.cos(time * 1.3) * 50;
                break;
        }

        x = initialX + offsetX;
        y = initialY + offsetY;

        // Keep boss within bounds
        if (x < 400) x = 400;
        if (x > 700) x = 700;
        if (y < 100) y = 100;
        if (y > 500) y = 500;
    }

    @Override
    public void update() {
        // Don't update if death animation is complete
        if (deathComplete) {
            return;
        }

        switch (currentState) {
            case IDLE:
                updateIdle();
                break;
            case SUMMONING:
                updateSummoning();
                break;
            case DEAD:
                updateDeath();
                break;
        }

        // Update current animation
        if (currentAnimation != null) {
            currentAnimation.update();
        }
    }

    private void updateIdle() {
        move();

        // Check if it's time to summon minions
        summonTimer++;
        if (summonTimer >= SUMMON_INTERVAL) {
            attack();
            summonTimer = 0;
        }

        attackTimer++;
        if (attackTimer >= attackCooldown) {
            attackTimer = 0;
        }
    }

    private void updateSummoning() {
        summonDuration++;

        // Spawn minions during summoning
        if (summonDuration % 30 == 0 && minionSpawnCount < MINIONS_PER_SUMMON) {
            spawnMinionFromMouth();
            minionSpawnCount++;
        }

        // After 90 frames (1.5 seconds), return to idle
        if (summonDuration >= 90) {
            summonComplete = true;
            currentState = BossState.IDLE;
            currentAnimation = idleAnimation;
            GameLogger.info("Java Boss finished summoning, returning to idle");
        }
    }

    private void updateDeath() {
        deathTimer++;

        // Stand still during death animation
        // No movement

        if (deathTimer >= DEATH_DURATION && !deathComplete) {
            deathComplete = true;
            this.active = false;

            // ✅ Move boss off-screen to remove from view
            this.x = -1000;
            this.y = -1000;

            GameLogger.info("Java Boss death animation complete - removed from scene");
        }
    }

    /**
     * ✅ Spawn minion from boss's mouth position
     */
    private void spawnMinionFromMouth() {
        // Calculate mouth position (center-left of boss sprite)
        double mouthX = x + 20; // Adjust based on sprite
        double mouthY = y + height / 2;

        Minion minion = new Minion(mouthX, mouthY, 1); // Type 2 (stronger minion)
        spawnedMinions.add(minion);

        GameLogger.debug("Java Boss spawned minion from mouth at (" + mouthX + ", " + mouthY + ")");
    }

    /**
     * ✅ Get and clear spawned minions (called by GameController)
     */
    public List<Minion> getAndClearSpawnedMinions() {
        List<Minion> minions = new ArrayList<>(spawnedMinions);
        spawnedMinions.clear();
        return minions;
    }

    /**
     * Check if boss has minions to spawn
     */
    public boolean hasSpawnedMinions() {
        return !spawnedMinions.isEmpty();
    }

    @Override
    public void render(GraphicsContext gc) {
        // ✅ Don't render if death animation is complete (boss removed from scene)
        if (deathComplete) {
            return;
        }

        if (active || currentState == BossState.DEAD) {
            // Render animated sprite
            if (currentAnimation != null) {
                Image currentFrame = currentAnimation.getCurrentFrame();
                if (currentFrame != null) {
                    gc.drawImage(currentFrame, x, y, width, height);
                }
            } else {
                // Fallback rendering
                gc.setFill(Color.DARKRED);
                gc.fillOval(x, y, width, height);
            }

            // Draw health bar (only when alive)
            if (currentState != BossState.DEAD) {
                double healthBarWidth = width * ((double) health / maxHealth);
                gc.setFill(Color.RED);
                gc.fillRect(x, y - 10, width, 5);
                gc.setFill(Color.GREEN);
                gc.fillRect(x, y - 10, healthBarWidth, 5);
            }
        }
    }

    // Getters for state
    public BossState getCurrentState() {
        return currentState;
    }

    public boolean isDeathComplete() {
        return deathComplete;
    }
}