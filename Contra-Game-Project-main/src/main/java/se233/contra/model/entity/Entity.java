package se233.contra.model.entity;

import javafx.scene.canvas.GraphicsContext;

public abstract class Entity {
    protected double x, y;
    protected double width, height;
    protected double velocityX, velocityY;
    protected boolean active;

    // Abstract methods that all entities must implement
    public abstract void update();
    public abstract void render(GraphicsContext gc);

    // Collision detection
    public boolean intersects(Entity other) {
        double thisX, thisY, thisWidth, thisHeight;
        double otherX, otherY, otherWidth, otherHeight;

        // Use hitbox for Character, regular bounds for others
        if (this instanceof Character) {
            Character c = (Character) this;
            thisX = c.getHitboxX();
            thisY = c.getHitboxY();
            thisWidth = c.getHitboxWidth();
            thisHeight = c.getHitboxHeight();
        } else {
            thisX = this.x;
            thisY = this.y;
            thisWidth = this.width;
            thisHeight = this.height;
        }

        if (other instanceof Character) {
            Character c = (Character) other;
            otherX = c.getHitboxX();
            otherY = c.getHitboxY();
            otherWidth = c.getHitboxWidth();
            otherHeight = c.getHitboxHeight();
        } else {
            otherX = other.x;
            otherY = other.y;
            otherWidth = other.width;
            otherHeight = other.height;
        }

        return thisX < otherX + otherWidth &&
                thisX + thisWidth > otherX &&
                thisY < otherY + otherHeight &&
                thisY + thisHeight > otherY;
    }

    // Getters and Setters
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public boolean isActive() { return active; }

    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setActive(boolean active) { this.active = active; }
}