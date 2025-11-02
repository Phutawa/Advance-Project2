package se233.contra.util;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import se233.contra.exception.SpriteLoadException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpriteLoader {
    private static Map<String, Image> spriteCache = new HashMap<>();
    private static Map<String, List<Image>> animationCache = new HashMap<>();

    /**
     * Load a single sprite image
     */
    public static Image loadSprite(String path) throws SpriteLoadException {
        try {
            if (spriteCache.containsKey(path)) {
                return spriteCache.get(path);
            }

            Image image = new Image(
                    SpriteLoader.class.getResourceAsStream(path)
            );
            spriteCache.put(path, image);
            GameLogger.info("Loaded sprite: " + path);
            return image;

        } catch (Exception e) {
            throw new SpriteLoadException("Failed to load sprite: " + path, e);
        }
    }

    /**
     * Extract a single frame from a sprite sheet
     */
    public static Image extractFrame(String spriteSheetPath, int col, int row,
                                     int frameWidth, int frameHeight)
            throws SpriteLoadException {
        try {
            Image spriteSheet = loadSprite(spriteSheetPath);
            PixelReader reader = spriteSheet.getPixelReader();

            int x = col * frameWidth;
            int y = row * frameHeight;

            return new WritableImage(reader, x, y, frameWidth, frameHeight);

        } catch (Exception e) {
            throw new SpriteLoadException(
                    "Failed to extract frame from: " + spriteSheetPath, e
            );
        }
    }

    /**
     * Extract multiple frames from a single row of a sprite sheet
     */
    public static List<Image> extractFramesFromRow(String spriteSheetPath,
                                                   int row, int startCol,
                                                   int frameCount,
                                                   int frameWidth,
                                                   int frameHeight)
            throws SpriteLoadException {

        String cacheKey = spriteSheetPath + "_row" + row + "_" + startCol + "_" + frameCount;

        if (animationCache.containsKey(cacheKey)) {
            return animationCache.get(cacheKey);
        }

        List<Image> frames = new ArrayList<>();
        try {
            Image spriteSheet = loadSprite(spriteSheetPath);
            PixelReader reader = spriteSheet.getPixelReader();

            for (int i = 0; i < frameCount; i++) {
                int x = (startCol + i) * frameWidth;
                int y = row * frameHeight;
                frames.add(new WritableImage(reader, x, y, frameWidth, frameHeight));
            }

            animationCache.put(cacheKey, frames);
            GameLogger.info("Extracted " + frameCount + " frames from row " + row);
            return frames;

        } catch (Exception e) {
            throw new SpriteLoadException(
                    "Failed to extract frames from: " + spriteSheetPath, e
            );
        }
    }

    /**
     * Extract frames from a rectangular region of a sprite sheet
     */
    public static List<Image> extractFramesFromRegion(String spriteSheetPath,
                                                      int startCol, int startRow,
                                                      int cols, int rows,
                                                      int frameWidth,
                                                      int frameHeight)
            throws SpriteLoadException {

        String cacheKey = spriteSheetPath + "_region_" + startCol + "_" + startRow +
                "_" + cols + "x" + rows;

        if (animationCache.containsKey(cacheKey)) {
            return animationCache.get(cacheKey);
        }

        List<Image> frames = new ArrayList<>();
        try {
            Image spriteSheet = loadSprite(spriteSheetPath);
            PixelReader reader = spriteSheet.getPixelReader();

            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    int x = (startCol + col) * frameWidth;
                    int y = (startRow + row) * frameHeight;
                    frames.add(new WritableImage(reader, x, y, frameWidth, frameHeight));
                }
            }

            animationCache.put(cacheKey, frames);
            GameLogger.info("Extracted " + frames.size() + " frames from region");
            return frames;

        } catch (Exception e) {
            throw new SpriteLoadException(
                    "Failed to extract region from: " + spriteSheetPath, e
            );
        }
    }

    /**
     * Clear all cached sprites (useful for memory management)
     */
    public static void clearCache() {
        spriteCache.clear();
        animationCache.clear();
        GameLogger.info("Sprite cache cleared");
    }
    public static String getCacheStats() {
        return "Sprites cached: " + spriteCache.size() +
                ", Animations cached: " + animationCache.size();
    }
}