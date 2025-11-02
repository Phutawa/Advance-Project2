package se233.contra.util;

import javafx.scene.media.AudioClip;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    private static Map<String, AudioClip> soundCache = new HashMap<>();

    public static void loadSound(String name, String path) {
        try {
            AudioClip clip = new AudioClip(
                    SoundManager.class.getResource(path).toString()
            );
            soundCache.put(name, clip);
            GameLogger.info("Loaded sound: " + name);
        } catch (Exception e) {
            GameLogger.error("Failed to load sound: " + name, e);
        }
    }

    public static void playSound(String name) {
        AudioClip clip = soundCache.get(name);
        if (clip != null) {
            clip.play();
        }
    }
}