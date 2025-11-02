package se233.contra.model.entity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import se233.contra.util.AnimationManager;
import se233.contra.util.GameLogger;
import se233.contra.util.SpriteLoader;

import java.util.ArrayList;
import java.util.List;

public class CustomBoss extends Boss {
    private double moveSpeed;
    private int attackCooldown;
    private int attackTimer;

    // ✅ CUSTOM BACKGROUND SYSTEM
    private Image customBackground;
    private boolean hasCustomBackground;

    // ✅ ANIMATION SYSTEM
    private AnimationManager standstillAnimation;
    private AnimationManager forwardAnimation;
    private AnimationManager backwardAnimation;
    private AnimationManager skillAnimation;
    private AnimationManager currentAnimation;

    private enum BossState {
        STANDSTILL,     // Idle/standing still
        MOVING_FORWARD, // Moving toward player
        MOVING_BACKWARD,// Moving away from player
        USING_SKILL,    // Time stop skill active
        DEAD           // Defeated
    }
    private BossState currentState;
    private BossState previousState; // For returning after skill

    private boolean timeStopActive;
    private int timeStopDuration;
    private static final int TIME_STOP_MAX_DURATION = 180; // 3 seconds at 60 FPS
    private int skillCooldown;
    private static final int SKILL_COOLDOWN = 600; // 10 seconds cooldown
    private int skillActivationTimer;
    private static final int SKILL_ACTIVATION_TIME = 60; // 1 second skill animation

    // Movement and animation
    private double time;
    private int movementTimer;
    private int movementDuration;
    private double targetX;
    private double targetY;

    // ✅ BULLET FIRING SYSTEM
    private int shootCooldown;
    private static final int SHOOT_COOLDOWN = 150; // Fire every 1.5 seconds
    private List<EnemyBullet> bossBullets;
    private Character playerReference;

    public CustomBoss(double x, double y) {
        this.x = x;
        this.y = y;
        this.width = 150;
        this.height = 150;
        this.health = 200;
        this.maxHealth = 200;
        this.scoreValue = 5;
        this.moveSpeed = 2.0;
        this.active = true;
        this.attackCooldown = 40;
        this.attackTimer = 0;
        this.time = 0;
        this.movementTimer = 0;
        this.movementDuration = 120; // 2 seconds per movement
        this.targetX = x;
        this.targetY = y;

        // ✅ Initialize state machine
        this.currentState = BossState.STANDSTILL;
        this.previousState = BossState.STANDSTILL;

        // ✅ Initialize time stop skill
        this.timeStopActive = false;
        this.timeStopDuration = 0;
        this.skillCooldown = SKILL_COOLDOWN;
        this.skillActivationTimer = 0;

        // ✅ Initialize bullet system
        this.shootCooldown = SHOOT_COOLDOWN;
        this.bossBullets = new ArrayList<>();

        // Load animations and background
        loadCustomBackground();
        loadAnimations();
    }

    /**
     * ✅ Load the custom background for Boss 3
     */
    private void loadCustomBackground() {
        try {
            String backgroundPath = "/backgrounds/BossStage3.png";
            customBackground = new Image(getClass().getResourceAsStream(backgroundPath));
            hasCustomBackground = true;
            GameLogger.info("CustomBoss: Loaded custom background successfully");
        } catch (Exception e) {
            hasCustomBackground = false;
            GameLogger.warn("CustomBoss: Could not load custom background, using default");
        }
    }

    /**
     * ✅ Load sprite sheet animations
     */
    private void loadAnimations() {
        try {
            String spritePath = "/se233/sprites/bosses/CustomBoss.png";
            int frameWidth = 160;
            int frameHeight = 166;

            // ✅ ANIMATION 0: Standstill (idle)
            List<Image> standstillFrames = SpriteLoader.extractFramesFromRow(
                    spritePath, 0, 0, 4, frameWidth, frameHeight
            );
            if (standstillFrames != null && !standstillFrames.isEmpty()) {
                standstillAnimation = new AnimationManager(standstillFrames, 15);
                GameLogger.info("CustomBoss: Loaded standstill animation (" + standstillFrames.size() + " frames)");
            }

            // ✅ ANIMATION 1: Go Forward
            List<Image> forwardFrames = SpriteLoader.extractFramesFromRow(
                    spritePath, 1, 0, 4, frameWidth, frameHeight
            );
            if (forwardFrames != null && !forwardFrames.isEmpty()) {
                forwardAnimation = new AnimationManager(forwardFrames, 10);
                GameLogger.info("CustomBoss: Loaded forward animation (" + forwardFrames.size() + " frames)");
            }

            // ✅ ANIMATION 2: Go Backward
            List<Image> backwardFrames = SpriteLoader.extractFramesFromRow(
                    spritePath, 2, 0, 4, frameWidth, frameHeight
            );
            if (backwardFrames != null && !backwardFrames.isEmpty()) {
                backwardAnimation = new AnimationManager(backwardFrames, 10);
                GameLogger.info("CustomBoss: Loaded backward animation (" + backwardFrames.size() + " frames)");
            }

            List<Image> skillFrames = SpriteLoader.extractFramesFromRow(
                    spritePath, 3, 0, 4, frameWidth, frameHeight
            );
            if (skillFrames != null && !skillFrames.isEmpty()) {
                skillAnimation = new AnimationManager(skillFrames, 8);
                GameLogger.info("CustomBoss: Loaded skill animation (" + skillFrames.size() + " frames)");
            }

            currentAnimation = standstillAnimation;

            GameLogger.info("CustomBoss: All animations loaded successfully");

        } catch (Exception e) {
            GameLogger.error("Failed to load CustomBoss animations", e);
        }
    }

    public boolean hasCustomBackground() {
        return hasCustomBackground && customBackground != null;
    }

    /**
     * ✅ Get the custom background image
     */
    public Image getCustomBackground() {
        return customBackground;
    }

    /**
     * ✅ Render the custom background
     */
    public void renderBackground(GraphicsContext gc) {
        if (hasCustomBackground && customBackground != null) {
            gc.drawImage(customBackground, 0, 0, 800, 600);
        }
    }

    /**
     * ✅ Check if time stop is active
     */
    public boolean isTimeStopActive() {
        return timeStopActive;
    }

    /**
     * ✅ Get time stop progress (for grey screen effect)
     */
    public double getTimeStopIntensity() {
        if (!timeStopActive) return 0.0;
        return 0.6; // 60% grey overlay
    }

    /**
     * ✅ Get bullets fired by the boss (can be accessed by GameController)
     */
    public List<EnemyBullet> getBossBullets() {
        return bossBullets;
    }

    /**
     * ✅ Set player reference for aiming bullets
     */
    public void setPlayer(Character player) {
        this.playerReference = player;
    }

    /**
     * ✅ Fire bullet aimed at player's current position
     * Bullet travels in straight line after being fired
     */
    private void fireBullet(Character player) {
        // Calculate bullet spawn position (center of boss)
        double bulletX = x + width / 2;
        double bulletY = y + height / 2;

        // Calculate direction toward player's current position
        double targetPlayerX = player.getHitboxX() + player.getHitboxWidth() / 2;
        double targetPlayerY = player.getHitboxY() + player.getHitboxHeight() / 2;

        double dx = targetPlayerX - bulletX;
        double dy = targetPlayerY - bulletY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        // Normalize direction
        double dirX = dx / distance;
        double dirY = dy / distance;

        // Create time-stop-proof bullet (straight line, no tracking)
        TimeStopBullet bullet = new TimeStopBullet(bulletX, bulletY, dirX, dirY);
        bossBullets.add(bullet);

        GameLogger.debug("CustomBoss fired bullet aimed at player!");
    }

    @Override
    public void attack() {
        // Try to activate time stop skill
        if (skillCooldown <= 0 && !timeStopActive && currentState != BossState.USING_SKILL) {
            activateTimeStopSkill();
        }
    }

    /**
     * ✅ Activate the time stop skill
     */
    private void activateTimeStopSkill() {
        previousState = currentState;
        currentState = BossState.USING_SKILL;
        currentAnimation = skillAnimation;
        skillActivationTimer = 0;
        getTimeStopIntensity();
        GameLogger.info("CustomBoss: Activating TIME STOP skill!");
    }

    @Override
    public void move() {
        if (currentState == BossState.USING_SKILL || currentState == BossState.DEAD) {
            return; // Don't move during skill or when dead
        }

        movementTimer++;

        // Change movement pattern periodically
        if (movementTimer >= movementDuration) {
            movementTimer = 0;
            chooseNewMovement();
        }

        // Execute current movement
        switch (currentState) {
            case MOVING_FORWARD:
                // Move toward player area (left side)
                if (x > 450) {
                    x -= moveSpeed;
                } else {
                    currentState = BossState.STANDSTILL;
                    currentAnimation = standstillAnimation;
                }
                break;

            case MOVING_BACKWARD:
                // Move away from player (right side)
                if (x < 650) {
                    x += moveSpeed;
                } else {
                    currentState = BossState.STANDSTILL;
                    currentAnimation = standstillAnimation;
                }
                break;

            case STANDSTILL:
                // Small floating motion while standing still
                time += 0.05;
                y = targetY + Math.sin(time) * 10;
                break;
        }

        // Keep boss within bounds
        if (x < 400) x = 400;
        if (x > 700) x = 700;
        if (y < 100) y = 100;
        if (y > 450) y = 450;
    }

    /**
     * ✅ Choose new movement direction
     */
    private void chooseNewMovement() {
        int choice = (int) (Math.random() * 3);

        switch (choice) {
            case 0:
                // Standstill
                currentState = BossState.STANDSTILL;
                currentAnimation = standstillAnimation;
                targetY = y;
                movementDuration = 120; // 2 seconds
                GameLogger.debug("CustomBoss: Standing still");
                break;

            case 1:
                // Move forward
                currentState = BossState.MOVING_FORWARD;
                currentAnimation = forwardAnimation;
                movementDuration = 90; // 1.5 seconds
                GameLogger.debug("CustomBoss: Moving forward");
                break;

            case 2:
                // Move backward
                currentState = BossState.MOVING_BACKWARD;
                currentAnimation = backwardAnimation;
                movementDuration = 90; // 1.5 seconds
                GameLogger.debug("CustomBoss: Moving backward");
                break;
        }
    }

    @Override
    public void update() {
        if (isDefeated) {
            currentState = BossState.DEAD;
            return;
        }

        // ✅ Update bullet firing cooldown (ALWAYS updates, even during time stop)
        if (playerReference != null) {
            shootCooldown--;
            if (shootCooldown <= 0) {
                fireBullet(playerReference);
                shootCooldown = SHOOT_COOLDOWN;
            }
        }

        // ✅ Update bullets (ALWAYS move, even during time stop)
        for (EnemyBullet bullet : bossBullets) {
            bullet.update();
        }
        bossBullets.removeIf(b -> !b.isActive());

        // Update skill cooldown
        if (skillCooldown > 0) {
            skillCooldown--;
        }

        // Update attack timer
        attackTimer++;
        if (attackTimer >= attackCooldown) {
            attack();
            attackTimer = 0;
        }

        // Handle different states
        switch (currentState) {
            case USING_SKILL:
                updateSkillState();
                break;

            case STANDSTILL:
            case MOVING_FORWARD:
            case MOVING_BACKWARD:
                move();
                break;

            case DEAD:
                // Dead state - no update needed
                break;
        }

        // Update current animation
        if (currentAnimation != null) {
            currentAnimation.update();
        }
    }

    /**
     * ✅ Update skill state (time stop)
     */
    private void updateSkillState() {
        skillActivationTimer++;

        // Skill activation phase (1 second)
        if (skillActivationTimer < SKILL_ACTIVATION_TIME) {
            // Playing skill animation
            return;
        }

        // Activate time stop after animation
        if (!timeStopActive) {
            timeStopActive = true;
            timeStopDuration = TIME_STOP_MAX_DURATION;
            GameLogger.info("TIME STOP ACTIVATED! Everything frozen for 3 seconds!");
        }

        // Time stop duration
        timeStopDuration--;

        if (timeStopDuration <= 0) {
            // End time stop
            timeStopActive = false;
            currentState = previousState;

            // Return to appropriate animation
            switch (currentState) {
                case STANDSTILL:
                    currentAnimation = standstillAnimation;
                    break;
                case MOVING_FORWARD:
                    currentAnimation = forwardAnimation;
                    break;
                case MOVING_BACKWARD:
                    currentAnimation = backwardAnimation;
                    break;
            }

            // Start cooldown
            skillCooldown = SKILL_COOLDOWN;

            GameLogger.info("TIME STOP ENDED! Everything resumes.");
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (active && !isDefeated) {
            // Render animated sprite
            if (currentAnimation != null) {
                Image currentFrame = currentAnimation.getCurrentFrame();
                if (currentFrame != null) {
                    gc.drawImage(currentFrame, x, y, width, height);
                }
            } else {
                // Fallback rendering (if animations failed to load)
                renderFallback(gc);
            }

            // Draw health bar
            renderHealthBar(gc);

            // Draw boss label
            gc.setFill(Color.BLACK);
            //gc.fillText("FINAL BOSS", x + 32, y - 18);
            gc.setFill(Color.YELLOW);
            //gc.fillText("FINAL BOSS", x + 30, y - 20);

            // ✅ Draw skill cooldown indicator
            if (skillCooldown > 0) {
                renderSkillCooldown(gc);
            }
        }
    }

    /**
     * ✅ Fallback rendering if animations don't load
     */
    private void renderFallback(GraphicsContext gc) {
        // Outer glow effect
        gc.setFill(Color.rgb(255, 215, 0, 0.3));
        gc.fillOval(x - 10, y - 10, width + 20, height + 20);

        // Main body
        gc.setFill(Color.GOLD);
        gc.fillOval(x, y, width, height);

        // Middle layer
        gc.setFill(Color.ORANGE);
        gc.fillOval(x + 20, y + 20, width - 40, height - 40);

        // Core
        double pulse = Math.sin(System.currentTimeMillis() * 0.005) * 0.1 + 0.9;
        gc.setFill(Color.rgb(255, 255, 0, pulse));
        gc.fillOval(x + width/3, y + height/3, width/3, height/3);

        // Eyes
        gc.setFill(Color.DARKRED);
        gc.fillOval(x + 40, y + 40, 25, 25);
        gc.fillOval(x + 85, y + 40, 25, 25);

        gc.setFill(Color.RED);
        gc.fillOval(x + 45, y + 45, 15, 15);
        gc.fillOval(x + 90, y + 45, 15, 15);

        // Outline
        gc.setStroke(Color.ORANGE);
        gc.setLineWidth(5);
        gc.strokeOval(x, y, width, height);
    }

    /**
     * ✅ Render health bar
     */
    private void renderHealthBar(GraphicsContext gc) {
        double healthBarWidth = width * ((double) health / maxHealth);

        // Background
        gc.setFill(Color.BLACK);
        gc.fillRect(x - 2, y - 17, width + 4, 12);

        // Empty portion (red)
        gc.setFill(Color.DARKRED);
        gc.fillRect(x, y - 15, width, 8);

        // Filled portion (green)
        gc.setFill(Color.LIME);
        gc.fillRect(x, y - 15, healthBarWidth, 8);

        // Outline
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeRect(x, y - 15, width, 8);
    }

    /**
     * ✅ Render skill cooldown indicator
     */
    private void renderSkillCooldown(GraphicsContext gc) {
        double cooldownPercent = (double) skillCooldown / SKILL_COOLDOWN;
        double barWidth = 60;
        double barHeight = 8;
        double barX = x + width / 2 - barWidth / 2;
        double barY = y + height + 10;

        // Background
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(barX - 2, barY - 2, barWidth + 4, barHeight + 4);

        // Cooldown bar
        gc.setFill(Color.GRAY);
        gc.fillRect(barX, barY, barWidth, barHeight);

        // Progress
        gc.setFill(Color.CYAN);
        gc.fillRect(barX, barY, barWidth * (1 - cooldownPercent), barHeight);
    }

    public int getTimeStopDuration() {
        return timeStopDuration;
    }
}