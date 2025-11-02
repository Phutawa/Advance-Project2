package se233.contra.model.entity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import se233.contra.util.AnimationManager;
import se233.contra.util.GameLogger;
import se233.contra.util.SpriteLoader;

import java.util.List;

public class HitEffect extends Entity {
    private AnimationManager animation;
    private boolean finished;
    private static final double EFFECT_SIZE = 40; // Adjust size as needed

    public HitEffect(double x, double y) {
        this.x = x - EFFECT_SIZE / 2; // Center the effect
        this.y = y - EFFECT_SIZE / 2;
        this.width = EFFECT_SIZE;
        this.height = EFFECT_SIZE;
        this.active = true;
        this.finished = false;

        loadAnimation();
    }

    private void loadAnimation() {
        try {
            String effectPath = "/se233/sprites/effects/gunEffect.png";
            int frameWidth = 197; // Width of each frame in your sprite
            int frameHeight = 192; // Height based on your image
            int frameCount = 3; // You have 3 frames

            List<Image> frames = SpriteLoader.extractFramesFromRow(
                    effectPath,
                    0,  // row
                    0,  // start column
                    frameCount,
                    frameWidth,
                    frameHeight
            );

            if (frames != null && !frames.isEmpty()) {
                // Play once only (1 loop max)
                animation = new AnimationManager(frames, 4, 1);
                GameLogger.debug("Hit effect animation loaded: " + frames.size() + " frames");
            }

        } catch (Exception e) {
            GameLogger.error("Failed to load hit effect animation", e);
            finished = true; // Mark as finished if load fails
        }
    }

    @Override
    public void update() {
        if (animation != null && !finished) {
            animation.update();

            // Check if animation completed
            if (animation.hasCompleted()) {
                finished = true;
                active = false;
                GameLogger.debug("Hit effect finished");
            }
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (active && animation != null && !finished) {
            Image currentFrame = animation.getCurrentFrame();
            if (currentFrame != null) {
                gc.drawImage(currentFrame, x, y, width, height);
            }
        }
    }

    public boolean isFinished() {
        return finished;
    }
}