package se233.contra.model.entity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import se233.contra.util.GameLogger;

public class CrackWall extends Entity {
    private Image crackImage;
    private boolean isVisible;
    private boolean hasCollision;

    public CrackWall(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.active = true;
        this.isVisible = false;
        this.hasCollision = true;

        loadCrackImage();
    }

    private void loadCrackImage() {
        try {
            String crackPath = "/se233/background/windowless.png";
            crackImage = new Image(getClass().getResourceAsStream(crackPath));
            GameLogger.info("Crack wall image loaded");
        } catch (Exception e) {
            GameLogger.error("Failed to load crack wall image", e);
        }
    }

    // Call this when boss is defeated
    public void revealCrack() {
        isVisible = true;
        hasCollision = false;  // Can walk through now
        GameLogger.info("Crack wall revealed! Now walkable.");
    }

    @Override
    public void update() {
        // Crack wall is static, no update needed
    }

    @Override
    public void render(GraphicsContext gc) {
        if (isVisible && crackImage != null) {
            gc.drawImage(crackImage, 0, 0, 800, 600);
        }
    }

    public boolean hasCollision() {
        return hasCollision;
    }

    public boolean isVisible() {
        return isVisible;
    }
}