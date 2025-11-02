package se233.contra.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import se233.contra.model.Score;
import se233.contra.model.Lives;

    /*แสดง คะแนน (Score) และ จำนวนชีวิตที่เหลือ (Lives)
        อยู่ด้านบนของหน้าจอในทุกเฟรมที่เกมถูก render เป็น UI ที่ผู้เล่นเห็นตลอด*/

public class HUD {
    //score = วัตถุที่เก็บคะแนนปัจจุบันของผู้เล่น
    //lives = วัตถุที่เก็บจำนวนชีวิตของผู้เล่นตอนนี้
    private Score score;
    private Lives lives;

    //ตอนสร้าง HUD ต้องส่งข้อมูล Score และ Lives มาด้วยเพื่อให้ HUD สามารถนำค่ามาแสดงได้
    public HUD(Score score, Lives lives) {
        this.score = score;
        this.lives = lives;
    }
    /*
        1.gc.setFill(Color.WHITE); → ตั้งสีตัวหนังสือเป็นสีขาว
        2.gc.setFont(new Font("Arial", 20)); → ใช้ฟอนต์ Arial ขนาด 20
        3.gc.fillText("SCORE: ...", 10, 30); → วาดคะแนนที่ตำแหน่ง x=10, y=30
        4.gc.fillText("LIVES: ...", 10, 60); → วาดจำนวนชีวิตด้านล่างคะแนนเล็กน้อย
    */
    public void render(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.setFont(new Font("Arial", 20));

        // Draw score
        gc.fillText("SCORE: " + score.getCurrentScore(), 10, 30);

        // Draw lives
        gc.fillText("LIVES: " + lives.getRemainingLives(), 10, 60);
    }
}