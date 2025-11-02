package se233.contra.view;

    /*
        1.isVictory = true หมายถึงผู้เล่นชนะ → จะแสดงข้อความ VICTORY!
        2.false → แสดง GAME OVER
        3.finalScore = คะแนนสุดท้าย นำมาวาดโชว์
    */
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class GameOverView {
    private boolean isVictory;
    private int finalScore;

    //ตอนสร้างวัตถุ เราจะส่งว่าแพ้/ชนะ และคะแนนสุดท้ายเข้ามา
    public GameOverView(boolean isVictory, int finalScore) {
        this.isVictory = isVictory;
        this.finalScore = finalScore;
    }

    //รับ GraphicsContext เพื่อวาดภาพลง Canvas เหมือนส่วนอื่นๆ
    public void render(GraphicsContext gc) {
        //ล้างหน้าจอ และทำฉากจบให้เป็นโทนมืด
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, 800, 600);

        //ตัวหนังสือสีขาว ขนาดใหญ่
        //เลือกข้อความตาม isVictory
        gc.setFill(Color.WHITE);
        gc.setFont(new Font("Arial", 48));

        String message = isVictory ? "VICTORY!" : "GAME OVER";
        gc.fillText(message, 300, 250);

        //ข้อความขนาดเล็กลง
        //แจ้งคะแนนสุดท้าย
        //บอกวิธีกลับไปเมนู
        gc.setFont(new Font("Arial", 24));
        gc.fillText("Final Score: " + finalScore, 320, 300);
        gc.fillText("Press ESC to return to menu", 240, 350);
    }
}