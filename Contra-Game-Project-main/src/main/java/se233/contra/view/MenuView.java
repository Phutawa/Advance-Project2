package se233.contra.view;
//หน้าจอเมนูหลักของเกม
//ทำหน้าที่แสดงหน้าจอ Start Game และ Exit ก่อนเริ่มเล่น

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import se233.contra.util.GameLogger;

public class MenuView {
    //ใช้จำว่าเรากำลังเลือกปุ่มไหนอยู่ (สำหรับการคุมด้วยคีย์บอร์ด)
    private Stage stage;
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private int selectedButton = 0;
    private Button startButton;
    private Button exitButton;

    public MenuView(Stage stage) {
        this.stage = stage;
    }

    //show() จะเริ่มสร้างหน้าจอเมนูทั้งหมด (พื้นหลัง + ปุ่ม + การควบคุมคีย์บอร์ด)
    public void show() {
        try {

            // Load title screen background
            /*
            1.ดึงภาพ title.PNG จากโฟลเดอร์ resources/backgrounds/
            2.สร้าง ImageView เพื่อแสดงรูป
            3.ปรับขนาดภาพให้พอดีกับหน้าจอ*/

            Image titleImage = new Image(
                    getClass().getResourceAsStream("/backgrounds/title.PNG")
            );
            ImageView backgroundView = new ImageView(titleImage);
            backgroundView.setFitWidth(WINDOW_WIDTH);
            backgroundView.setFitHeight(WINDOW_HEIGHT);
            backgroundView.setPreserveRatio(true);

            // Create buttons
            //เรียก createStyledButton เพื่อสร้างปุ่มพร้อมดีไซน์
            //true หมายถึงปุ่มนี้เป็นปุ่มที่ถูกเลือกเริ่มต้น (ไฮไลท์อยู่ที่ START)
            startButton = createStyledButton("START GAME", true);
            exitButton = createStyledButton("EXIT", false);

            //กดปุ่ม START → เรียกฟังก์ชัน startGame() → ไปหน้าเกม
            //กด EXIT → ปิดหน้าต่างเกม
            startButton.setOnAction(e -> startGame());
            exitButton.setOnAction(e -> stage.close());

            // Layout buttons
            /*  ใช้ VBox วางปุ่มแบบเรียงลงล่าง ระยะห่างระหว่างปุ่ม = 20px
                เลื่อนปุ่มลงมาจากกลางจอนิดนึง (TranslateY = 200)
            */
            VBox buttonBox = new VBox(20);
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.getChildren().addAll(startButton, exitButton);
            buttonBox.setTranslateY(200); // Position buttons

            // Combine background and buttons
            // รวมพื้นหลัง + ปุ่มเข้า StackPane
            // StackPane = วางซ้อนกัน → พื้นหลังอยู่ด้านล่าง ปุ่มอยู่ด้านบน
            StackPane root = new StackPane();
            root.getChildren().addAll(backgroundView, buttonBox);
            root.setStyle("-fx-background-color: black;"); // Black background

            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
            scene.setFill(Color.BLACK);

            // Keyboard controls
            // Button actions
            // W / ↑ → เลือกปุ่มบน (START)
            // S / ↓ → เลือกปุ่มล่าง (EXIT)
            //ENTER หรือ SPACE → ยืนยันตัวเลือก
            scene.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.W) {
                    selectedButton = 0;
                    updateButtonSelection();
                } else if (event.getCode() == KeyCode.DOWN || event.getCode() == KeyCode.S) {
                    selectedButton = 1;
                    updateButtonSelection();
                } else if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                    if (selectedButton == 0) {
                        startGame();
                    } else {
                        stage.close();
                    }
                }
            });

            stage.setTitle("Contra Boss Fight - SE233 Project");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();

            GameLogger.info("Menu displayed successfully");

        } catch (Exception e) {
            GameLogger.error("Failed to load menu", e);
            e.printStackTrace();
        }
    }

    private Button createStyledButton(String text, boolean selected) {
        Button button = new Button(text);
        updateButtonStyle(button, selected);

        // Mouse hover
        //→ ถ้าเอาเมาส์ชี้ → ปุ่มจะดู ใหญ่/เด่นขึ้น
        //→ ถ้าถอยเมาส์ → ปุ่มกลับไปยังสถานะเดิม (เลือกอยู่ หรือไม่ได้เลือก)
        button.setOnMouseEntered(e -> {
            updateButtonStyle(button, true);
        });

        button.setOnMouseExited(e -> {
            boolean isSelected = (button == startButton && selectedButton == 0) ||
                    (button == exitButton && selectedButton == 1);
            updateButtonStyle(button, isSelected);
        });

        return button;
    }

    private void updateButtonStyle(Button button, boolean selected) {
        if (selected) {
            //เปลี่ยนสี / ขนาด / เส้นขอบ ตามสถานะปุ่ม
            //เมื่อปุ่มถูกเลือก
            button.setStyle(
                    "-fx-font-size: 28px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: black;" +
                            "-fx-background-color: white;" +
                            "-fx-padding: 15 40 15 40;" +
                            "-fx-border-color: #FF4500;" +
                            "-fx-border-width: 3px;" +
                            "-fx-cursor: hand;"
            );


        } else {
            button.setStyle(
                    "-fx-font-size: 24px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-color: #FF4500;" +
                            "-fx-padding: 12 35 12 35;" +
                            "-fx-border-color: white;" +
                            "-fx-border-width: 2px;" +
                            "-fx-cursor: hand;"
            );
        }
    }

    /*
        ถ้า selected == true → ปุ่มเด่นขึ้น สีขาว ตัวหนา
        ถ้า false → สีปกติ
    */
    private void updateButtonSelection() {
        updateButtonStyle(startButton, selectedButton == 0);
        updateButtonStyle(exitButton, selectedButton == 1);
    }

    //เปลี่ยนจากเมนูไปยังหน้า Gameplay
    private void startGame() {
        GameLogger.info("Starting game...");
        GameCanvas gameCanvas = new GameCanvas(stage);
        gameCanvas.show();
    }
}