package se233.contra.view;

    /*คลาสนี้ทำหน้าที่เป็น หน้าจอเกม ที่:
        1.สร้าง Canvas สำหรับวาดเกม
        2.โหลดพื้นหลังตามด่าน / Boss
        3.รัน AnimationTimer (เกมลูป update + render)
        4.รับ Input ผ่าน InputController
      เรียก GameController เพื่ออัปเดตสถานะและให้วาดเกม*/

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import se233.contra.controller.GameController;
import se233.contra.controller.InputController;
import se233.contra.util.GameLogger;

    /*canvas → พื้นที่วาดภาพ
        gc → ตัวใช้วาดลง canvas (draw, fill, image ฯลฯ)
        gameController → ตัวควบคุมเกมทั้งหมด
        gameLoop → ตัวทำให้เกมวิ่ง 60fps
        currentBossLevel → ใช้เช็คว่าเปลี่ยนด่าน (เพื่อเปลี่ยน background)*/
public class GameCanvas {
    private Canvas canvas;
    private GraphicsContext gc;
    private Stage stage;
    private GameController gameController;
    private AnimationTimer gameLoop;
    private Image currentBackground;
    private int currentBossLevel;  // ✅ Track current boss level

    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;

    /*กำหนดขนาดจอ, เตรียมเครื่องมือวาด, เริ่มที่ Boss ด่าน 1*/
    public GameCanvas(Stage stage) {
        this.stage = stage;
        this.canvas = new Canvas(WINDOW_WIDTH, WINDOW_HEIGHT);
        this.gc = canvas.getGraphicsContext2D();
        this.currentBossLevel = 1;  // ✅ Initialize
    }

    /*show() → เริ่มเกม
        1.โหลดฉากพื้นหลังเริ่มต้น
        2.วาง canvas ลงใน Scene
        3.สร้าง GameController
        4.จับปุ่ม keyboard ผ่าน InputController*/
    public void show() {
        try {
            // Load initial background (Boss 1)
            loadBackground(1);

            StackPane root = new StackPane(canvas);
            Scene scene = new Scene(root);

            // Initialize game controller
            gameController = new GameController();

            // Setup input handling
            InputController inputController = new InputController(scene, gameController);

            // Game loop handle() ถูกเรียกตลอดเวลา ≈ 60 ครั้งต่อวินาที
            gameLoop = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    update();
                    render();
                }
            };
            stage.setScene(scene);
            //เซ็ตจอแล้วเริ่มเกม
            gameLoop.start();
            GameLogger.info("Game started successfully");

        } catch (Exception e) {
            GameLogger.error("Failed to start game", e);
        }
    }

    /*  1.เรียก Logic ทั้งหมดจาก GameController
        2.เช็คว่าด่านเปลี่ยนไหม
        3.ถ้าเปลี่ยน → โหลดพื้นหลังใหม่ทันที ทำให้พื้นหลังเปลี่ยนตามบอส แบบอัตโนมัติ
    */
    private void update() {
        gameController.update();

        int newBossLevel = gameController.getCurrentBossLevel();
        if (newBossLevel != currentBossLevel) {
            currentBossLevel = newBossLevel;
            loadBackground(currentBossLevel);
        }
    }

    /*
    1.ลบภาพในเฟรมก่อน
    2.วาดพื้นหลัง
    3.ให้ GameController วาด Player/Boss/Bullet ฯลฯ*/
    private void render() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw background
        if (currentBackground != null) {
            gc.drawImage(currentBackground, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        }

        // Draw all game entities
        gameController.render(gc);
    }

    // ✅ Load background based on boss level เลือกไฟล์รูปตามด่าน
    private void loadBackground(int bossLevel) {
        try {
            String backgroundPath;
            switch (bossLevel) {
                case 1:
                    backgroundPath = "/backgrounds/BossStage1.png";
                    break;
                case 2:
                    backgroundPath = "/backgrounds/BossStage2.png";
                    break;
                case 3:
                    backgroundPath = "/backgrounds/stage3.png";
                    break;
                default:
                    backgroundPath = "/backgrounds/BossStage1.png";
            }

            currentBackground = new Image(getClass().getResourceAsStream(backgroundPath));
            GameLogger.info("Loaded background for Boss " + bossLevel);
        } catch (Exception e) {
            GameLogger.error("Failed to load background for Boss " + bossLevel, e);
        }
    }

    public void stop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }
}
