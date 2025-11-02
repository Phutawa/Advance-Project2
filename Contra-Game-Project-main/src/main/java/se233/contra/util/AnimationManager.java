package se233.contra.util;

import javafx.scene.image.Image;
import java.util.List;

public class AnimationManager {
    private List<Image> frames;
    private int currentFrame;
    private int frameDelay;
    private int frameCounter;
    private int loopCount;
    private int maxLoops;         // Add this (-1 for infinite)

    public AnimationManager(List<Image> frames, int frameDelay) {
        this(frames, frameDelay, -1); // Default to infinite loops
    }

    public AnimationManager(List<Image> frames, int frameDelay, int maxLoops) {
        this.frames = frames;
        this.frameDelay = frameDelay;
        this.currentFrame = 0;
        this.frameCounter = 0;
        this.loopCount = 0;
        this.maxLoops = maxLoops;
    }

    public void update() {
        // Don't update if max loops reached
        if (maxLoops > 0 && loopCount >= maxLoops) {
            return;
        }

        frameCounter++;
        if (frameCounter >= frameDelay) {
            int previousFrame = currentFrame;
            currentFrame = (currentFrame + 1) % frames.size();
            frameCounter = 0;

            // Detect when animation loops back to start
            if (previousFrame > currentFrame) {
                loopCount++;
            }
        }
    }

    public void reset() {
        currentFrame = 0;
        frameCounter = 0;
        loopCount = 0;
    }

    public Image getCurrentFrame() {
        return frames.get(currentFrame);
    }

    // Check if animation has completed all loops
    public boolean hasCompleted() {
        return maxLoops > 0 && loopCount >= maxLoops;
    }

    public int getCurrentFrameIndex() {
        return currentFrame;
    }

    public int getLoopCount() {
        return loopCount;
    }
}