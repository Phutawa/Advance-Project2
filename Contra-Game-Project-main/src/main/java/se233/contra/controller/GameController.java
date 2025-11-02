package se233.contra.controller;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import se233.contra.model.*;
import se233.contra.model.entity.*;
import se233.contra.model.entity.Character;
import se233.contra.util.GameLogger;
import se233.contra.view.HUD;
import se233.contra.model.Platform;
import se233.contra.model.entity.DefenseWallBoss;
import java.util.ArrayList;
import java.util.List;

public class GameController {
    private Character player;
    private Boss currentBoss;
    private List<Bullet> playerBullets;
    private List<EnemyBullet> enemyBullets;
    private List<Minion> minions;
    private List<HitEffect> hitEffects;
    private Score score;
    private Lives lives;
    private GameState gameState;
    private CollisionController collisionController;
    private List<Platform> platforms;
    private CrackWall crackWall;
    private boolean canTransition;

    private MinionSpawner minionSpawner;
    private boolean bossSpawned;

    //   WARNING ANIMATION SYSTEM (for Boss 3)
    private int warningTimer;
    private static final int WARNING_DURATION = 240; // 4 seconds (60 FPS × 4)
    private boolean warningComplete;

    /*initializeGame() --> player = new Character(100, 400);
    สร้างสร้าง ตัวผู้เล่น (Character) และวางตำแหน่งเริ่มต้นที่ x=100, y=400 */
    public GameController() {
        initializeGame();
    }
    /*สร้างออบเจ็กต์จัดการ คะแนน, จำนวนชีวิต และ สถานะเกม (GameState) ตามลำดับ*/
    private void initializeGame() {
        player = new Character(100, 400);
        score = new Score();
        lives = new Lives();
        gameState = new GameState();

        /*สร้างลิสต์สำหรับเก็บเอนทิตีไดนามิกที่เปลี่ยนแปลงขณะเล่น: กระสุนผู้เล่น, กระสุนศัตรู, มินิออน, เอฟเฟกต์เมื่อโดน*/
        playerBullets = new ArrayList<>();
        enemyBullets = new ArrayList<>();
        minions = new ArrayList<>();
        hitEffects = new ArrayList<>();

        /*สร้างตัวจัดการการชน — GameController จะเรียก collisionController เมื่อมีบอสเพื่อทำ collision logic แบบรวมศูนย์*/
        collisionController = new CollisionController();
        platforms = new ArrayList<>();
        /*สร้างลิสต์ platforms แล้วเรียกเมธอด createPlatformsBoss1() เพื่อเติมข้อมูลแพลตฟอร์มเริ่มต้นสำหรับด่าน (Boss 1 layout)*/
        createPlatformsBoss1();

        /*สร้างวัตถุ CrackWall — พารามิเตอร์ (500, 0, 100, 600) น่าจะเป็น (x, y, width, height)
        ในหน้าจอขนาด 800×600 ผนังนี้น่าจะตั้งอยู่ทางขวาและมีความสูงเต็มจอ — ใช้สำหรับ effect/obstacle ของ Boss1*/
        crackWall = new CrackWall(500, 0, 100, 600);

        /*รีเซ็ต flag สถานะเริ่มต้น:ยังไม่สามารถย้ายไปด่านถัดไป (canTransition=false) และยังไม่มีบอสถูก spawn (bossSpawned=false)*/
        canTransition = false;
        bossSpawned = false;

        /*เริ่ม มินิออนเวฟ สำหรับบอสระดับ 1 — โดยเมธอดนี้จะเปลี่ยน gameState เป็น MINION_WAVE และสร้าง MinionSpawner ให้กับระดับนั้น
        นั่นหมายความว่าเกมจะเริ่มจากการ spawn มินิออนก่อนเจอบอส — เหมาะสำหรับ flow แบบ wave → boss*/
        startMinionWave(1);

        /*บันทึกข้อมูลว่าการเริ่มต้นเกมเสร็จเรียบร้อยแล้ว — ดีสำหรับการ debug*/
        GameLogger.info("Game initialized - Starting with minion wave");

    }
    /*ทำหน้าที่เติมรายการ platforms ด้วยแพลตฟอร์มสำหรับด่านบอส 1
    แต่ละบรรทัด new Platform(x, y, width, height) โดยค่าพารามิเตอร์น่าจะเป็น (x, y, w, h) ตามนิยามของ Platform
    พิกัดใช้ระบบพิกัดของ JavaFX: จุดเริ่มต้น (0,0) อยู่มุมบนซ้าย ค่า y ยิ่งมาก → ยิ่งต่ำลงบนหน้าจอ*/
    private void createPlatformsBoss1() {
        platforms.add(new Platform(0, 540, 800, 60));
        platforms.add(new Platform(80, 420, 240, 20));
        platforms.add(new Platform(320, 380, 70, 20));
        platforms.add(new Platform(0, 284, 320, 20));
        platforms.add(new Platform(400, 460, 70, 20));
    }
    /*ลบแพลตฟอร์มเดิมทั้งหมด (platforms.clear()) แล้วสร้างแพลตฟอร์มใหม่แบบง่าย ๆ สำหรับบอส 2 — ในที่นี้เป็นพื้นเดียว (50,500,800,60)
    ถ้าเรียก createPlatformsBoss1() อีกครั้งโดยไม่ได้ clear() ก่อน จะทับรายการเดิมได้
    (ปัจจุบัน boss1 ถูกเรียกตอน initializeGame ดังนั้นปัญหานี้มักไม่เกิด แต่ถ้ามีการเรียกซ้ำควรระวัง)*/
    private void createPlatformsBoss2() {
        platforms.clear();
        platforms.add(new Platform(0, 500, 800, 60));
    }
    /*เปลี่ยนเฟสของเกมเป็น MINION_WAVE (ระบบ phase-based)
    สร้าง MinionSpawner ใหม่โดยส่ง bossLevel เพื่อให้สปอว์นตามระดับบอส
    รีเซ็ตสถานะที่เกี่ยวกับบอส: bossSpawned = false; currentBoss = null;*/
    private void startMinionWave(int bossLevel) {
        gameState.setPhase(GameState.Phase.MINION_WAVE);
        minionSpawner = new MinionSpawner(bossLevel);
        bossSpawned = false;
        currentBoss = null;
        GameLogger.info("=== MINION WAVE STARTED FOR BOSS " + bossLevel + " ===");
    }
    /*ถ้าเกมไม่อยู่ในสถานะ PLAYING เช่น PAUSED, GAME_OVER, VICTORY จะไม่ทำอะไรต่อเลย — ป้องกันการอัปเดตที่ไม่ต้องการ*/
    public void update() {
        if (gameState.getCurrentState() != GameState.State.PLAYING) {
            return;
        }

        //    Check if time stop is active (Boss 3 skill)
        /*เช็คว่าบอสปัจจุบันเป็น CustomBoss และมีสกิล time stop กำลัง active หรือไม่
        timeStopActive คือตัวสวิตช์เพื่อบล็อกการอัปเดตของเอนทิตีส่วนใหญ่เมื่อเวลาถูกหยุด*/
        boolean timeStopActive = false;
        if (currentBoss instanceof CustomBoss) {
            CustomBoss customBoss = (CustomBoss) currentBoss;
            timeStopActive = customBoss.isTimeStopActive();
        }

        //     Only update player if time stop is NOT active
        /*ถ้าเวลาไม่ถูกหยุด → อัปเดตการเคลื่อนที่ของผู้เล่นและตรวจการชนกับแพลตฟอร์ม
        เมื่อต้องการหยุดเวลา จะไม่อัปเดตตำแหน่ง/ฟิสิกส์ของผู้เล่น*/
        if (!timeStopActive) {
            player.update();
            player.checkPlatformCollision(platforms);
        }

        // Handle different game phases
        /*แยกการอัปเดตตาม phase ของเกม:
            WARNING — เรียก updateWarningPhase() (ควรทำเสมอ)
            MINION_WAVE — อัปเดตเฉพาะเมื่อเวลาไม่ถูกหยุด
            BOSS_FIGHT — อัปเดตบอสเสมอ (แม้จะ time stop ก็ตาม)
          — เหตุผล: บอสอาจยังต้องทำงาน (เช่น bullet ที่พิสูจน์ว่าไม่ถูกหยุด)*/
        if (gameState.getCurrentPhase() == GameState.Phase.WARNING) {
            updateWarningPhase();
        } else if (gameState.getCurrentPhase() == GameState.Phase.MINION_WAVE) {
            //       Only update minion phase if time stop is NOT active
            if (!timeStopActive) {
                updateMinionPhase();
            }
        } else if (gameState.getCurrentPhase() == GameState.Phase.BOSS_FIGHT) {
            updateBossPhase(); // Boss always updates (including during time stop)
        }

        // Only update bullets, minions, and effects if time stop is NOT active
        /*ถ้าเวลาไม่ถูกหยุด → อัปเดตทุก entity ที่ได้รับผล (กระสุน มินิออน เอฟเฟกต์)
        ตรวจ crackWall collision กับ player และเรียก collisionController หรือ checkMinionCollisions()
        เพื่อจัดการการชน --> ลบทิ้ง object ที่ไม่ active/finished โดยใช้ removeIf — ปลอดภัยเพราะไม่แก้ขณะ iterate (update ขั้นแรกทำแยกก่อน)*/
        if (!timeStopActive) {
            playerBullets.forEach(Bullet::update);
            enemyBullets.forEach(EnemyBullet::update);
            minions.forEach(Minion::update);
            hitEffects.forEach(HitEffect::update);

            if (crackWall != null && crackWall.hasCollision() && player.intersects(crackWall)) {
                lives.loseLife();
                player.respawn();
                GameLogger.warn("Player hit the wall!");
            }

            if (currentBoss != null) {
                collisionController.checkCollisions(
                        player, currentBoss, playerBullets,
                        enemyBullets, minions, hitEffects,
                        score, lives
                );
            } else {
                checkMinionCollisions();
            }

            playerBullets.removeIf(b -> !b.isActive());
            enemyBullets.removeIf(b -> !b.isActive());
            minions.removeIf(m -> !m.isActive());
            hitEffects.removeIf(e -> e.isFinished());
        }


        //  ALWAYS check for time-stop-proof bullets (CustomBoss bullets)
        /*ตรวจ collision จาก boss-specific bullets ของ CustomBoss เสมอ (แม้ time stop จะ active)
        — นี่คือเหตุผลหลักที่แยกเช็คนี้ออกจาก collisionController (เพราะ collisionController ถูกเรียกเฉพาะเมื่อเวลาไม่ถูกหยุดในโค้ดปัจจุบัน)
        เมื่อโดน → ลดชีวิต ปิดกระสุน รีสปอว์นผู้เล่น และ log*/
        if (currentBoss instanceof CustomBoss) {
            CustomBoss customBoss = (CustomBoss) currentBoss;
            List<EnemyBullet> bossBullets = customBoss.getBossBullets();

            // Check collisions with player (even during time stop)
            for (EnemyBullet bullet : bossBullets) {
                if (bullet.intersects(player)) {
                    lives.loseLife();
                    bullet.setActive(false);
                    player.respawn();
                    GameLogger.warn("Player hit by time-stop bullet!");
                }
            }
        }
        /*หากชีวิตหมด → เปลี่ยนสถานะเกมเป็น GAME_OVER*/
        if (!lives.hasLivesLeft()) {
            gameState.setState(GameState.State.GAME_OVER);
        }
    }

    /*Handle WARNING phase (Boss 3 only) Displays dramatic warning animation for 4 seconds before minions spawn */
    /*warningTimer++ — เพิ่มตัวนับเฟรมทีละ 1 ทุกครั้งที่ updateWarningPhase() ถูกเรียก (โดยปกติเรียกจาก update() ทุกเฟรม)
    WARNING_DURATION ถูกกำหนดที่อื่นเป็น 240 — หมายถึง 240 เฟรม → ถ้าเกมรันที่ 60 FPS ก็เท่ากับ 4 วินาที
    เงื่อนไข warningTimer >= WARNING_DURATION && !warningComplete ป้องกันไม่ให้โค้ดภายในรันซ้ำหลายครั้ง*/
    private void updateWarningPhase() {
        warningTimer++;

        if (warningTimer >= WARNING_DURATION && !warningComplete) {
            warningComplete = true;
            gameState.setPhase(GameState.Phase.MINION_WAVE);
            startMinionWave(3);  // Start Boss 3 minion wave
            GameLogger.info("Warning complete! Minion wave starting...");
        }
    }
    /*ตรวจว่า minionSpawner มีอยู่จริงก่อนจะเรียก .update(minions)
    minionSpawner.update(minions) → สปอว์หรืออัปเดต logic ของมินิออน (เติม/จัดการ wave ตาม bossLevel ที่สร้างตอน startMinionWave)
    เช็ค areWavesComplete(minions) — ถ้าระบุว่า wave ทั้งหมดเสร็จ → เรียก transitionToBossFight() เพื่อไปสู่ช่วงบอส*/
    private void updateMinionPhase() {
        if (minionSpawner != null) {
            minionSpawner.update(minions);

            if (minionSpawner.areWavesComplete(minions)) {
                transitionToBossFight();
            }
        }
    }

    // In GameController.java, replace the updateBossPhase() method with this:

    private void updateBossPhase() {
        if (currentBoss != null) {
            currentBoss.update();

            //    Check if JavaBoss spawned minions
            /*ถ้าบอสเป็น JavaBoss ให้ดึงมินิออนที่บอส spawn ออกมาและเพิ่มเข้า minions list ของ controller
            วิธี getAndClearSpawnedMinions() ดี — แยกความรับผิดชอบให้บอสจัดการ spawning ในตัวเองแล้ว controller ดึงมาใช้*/
            if (currentBoss instanceof JavaBoss) {
                JavaBoss javaBoss = (JavaBoss) currentBoss;
                if (javaBoss.hasSpawnedMinions()) {
                    List<Minion> newMinions = javaBoss.getAndClearSpawnedMinions();
                    minions.addAll(newMinions);
                    GameLogger.info("JavaBoss spawned " + newMinions.size() + " minions!");
                }
            }

            //     FIX: Check collisions between player and minions during boss fight
            /*ตรวจว่า player ชน minion ตัวใด → ลดชีวิต รีสปอว์น ปิด minion เห็นว่าโค้ดตรวจทุก minion โดยไม่เช็ค minion.isActive() ก่อน
            — อาจทำให้ logic ทำงานบน minion ที่ถูกปิดแล้ว (แต่ถ้ามี isActive ถูกเช็กที่อื่นและ removeIf
            รันหลังจากนี้ อาจไม่เป็นปัญหาเชิง correctness แต่เป็นประสิทธิภาพ/clarity)*/
            for (Minion minion : minions) {
                if (player.intersects(minion)) {
                    lives.loseLife();
                    player.respawn();
                    minion.setActive(false);
                    GameLogger.warn("Player collided with JavaBoss minion!");
                }
            }

            //      FIX: Check if player bullets hit minions during boss fight
            /*แบบ nested loop เช็คทุกคู่ bullet × minion — ถูกต้องเชิง functional
            แต่เป็น O(B × M) — ถ้าจำนวนเยอะอาจทำให้ช้า ไม่มี continue/break เมื่อ bullet ถูกปิดแล้ว
            — จะยังเช็ค bullet เดิมกับ minion ตัวต่อไป (wasted) ไม่มีการเช็ค bullet.isActive() และ minion.isActive() ก่อนเช็ค intersection
            — อาจทำงานซ้ำกับที่เพิ่ง setActive(false)/isActive false*/
            for (Bullet bullet : playerBullets) {
                for (Minion minion : minions) {
                    if (bullet.intersects(minion)) {
                        minion.takeDamage(bullet.getDamage());
                        hitEffects.add(new HitEffect(bullet.getX(), bullet.getY()));
                        bullet.setActive(false);

                        if (!minion.isActive()) {
                            score.addScore(minion.getScoreValue());
                            GameLogger.info("Minion destroyed during boss fight! Score: " + minion.getScoreValue());
                        }
                    }
                }
            }
            /*เงื่อนไข !hasAwardedScore() ป้องกันการให้คะแนนซ้ำ — ดีมาก เปิด canTransition สำหรับ level 1 และ 2
            — design choice crackWall.revealCrack() สำหรับ level 1 — effect วัยรุ่น!
            ข้อสังเกต: หากบอสตายแต่มี minions ค้างอยู่ในฉาก ผู้เล่นยังต้องเคลียร์/เดินไปมุมขวา
            — โค้ด transition จะ clear lists ต่อใน transitionToNextBoss() ซึ่งเรียกได้เมื่อผู้เล่นถึง x>=700
            — ต้องแน่ใจว่าการมี minions ค้างไม่ทำให้ผู้เล่นตายก่อนจะวิ่งไป (อาจเป็น intended challenge)*/
            if (currentBoss.isDefeated() && !currentBoss.hasAwardedScore()) {
                score.addScore(currentBoss.getScoreValue());
                currentBoss.awardScore();

                int currentLevel = gameState.getCurrentBossLevel();

                //       Boss 1: Show crack wall effect
                if (currentLevel == 1) {
                    if (crackWall != null && !crackWall.isVisible()) {
                        crackWall.revealCrack();
                    }
                }

                //Enable transition for Boss 1 and Boss 2
                if (currentLevel == 1 || currentLevel == 2) {
                    canTransition = true;
                    GameLogger.info("Transition enabled! Walk to the right edge to proceed to next boss.");
                }

                GameLogger.info("Boss defeated! Score awarded: " + currentBoss.getScoreValue());
            }

            //   Check if player reaches right edge for transition
            /*Threshold 700 เป็น magic number
            — ควรเปลี่ยนเป็น constant เช่น TRANSITION_X_THRESHOLD ต้องแน่ใจว่า player.getX()
            เป็นตำแหน่งที่สอดคล้องกับขนาดหน้าจอ/edges*/
            if (canTransition && player.getX() >= 700) {
                transitionToNextBoss();
            }
        }
    }

    /*gameState.setPhase(GameState.Phase.BOSS_FIGHT);
    เปลี่ยน phase ของเกมเป็น BOSS_FIGHT (เพื่อให้ logic อื่น ๆ รู้ว่าเข้าสู่ช่วงบอสแล้ว)

    loadBoss(gameState.getCurrentBossLevel());
    เรียกเมธอด loadBoss(level) เพื่อสร้าง instance ของบอสตามระดับปัจจุบัน (gameState.getCurrentBossLevel() ควรคืนค่าระดับบอสที่ต้องโหลด เช่น 1,2,3)

    bossSpawned = true;
    เซ็ต flag ว่าบอสถูก spawn แล้ว — อาจถูกใช้ที่อื่นเพื่อข้ามการ spawn ซ้ำหรือเปลี่ยนพฤติกรรมของสปอว์นเนอร์

    GameLogger.info("=== BOSS FIGHT STARTED ===");
    Log เพื่อ debug/trace ว่าเริ่มบอสไฟท์*/
    private void transitionToBossFight() {
        gameState.setPhase(GameState.Phase.BOSS_FIGHT);
        loadBoss(gameState.getCurrentBossLevel());
        bossSpawned = true;
        GameLogger.info("=== BOSS FIGHT STARTED ===");
    }

    /*สำหรับทุก bullet ใน playerBullets จะไล่เช็คกับทุก minion ใน minions:
    ถ้า bullet.intersects(minion) → minion รับ damage (takeDamage), สร้าง HitEffect
    , ปิดกระสุน (bullet.setActive(false)), ถ้ามินิออนตาย (!minion.isActive())
    ให้เพิ่มคะแนน หลังจากเช็กกระสุนแล้ว loop ถัดมาเช็กว่า player ชนมินิออนตัวใดหรือไม่:
    ถ้าชน → ลดชีวิต, รีสปอว์น player, ปิดมินิออน, log warning*/
    private void checkMinionCollisions() {
        /*ไล่กระสุนแต่ละนัดในลิสต์กระสุนของผู้เล่น*/
        for (Bullet bullet : playerBullets) {
            /*ไล่มินิออน แต่ข้ามตัวที่ไม่ active (ตาย/ปิดไปแล้ว)*/
            for (Minion minion : minions) {
                /*ตรวจตรา collision จริง ๆ (มักเป็นการเช็ค bounding box หรือ pixel collision ขึ้นกับการ implement ของ intersects)*/
                if (bullet.intersects(minion)) {
                    /*หัก HP ของมินิออนตาม damage ของกระสุน*/
                    minion.takeDamage(bullet.getDamage());
                    /*สร้างเอฟเฟกต์การชน (เช่น ระเบิด เลือด) ที่ตำแหน่งกระสุนยิงโดน*/
                    hitEffects.add(new HitEffect(bullet.getX(), bullet.getY()));
                    /*ถ้ากระสุน ไม่ทะลุ (non-piercing) ให้ปิดกระสุน (เอาออกจากเกมในรอบถัดไป)
                    ถ้ากระสุนเป็น piercing จะไม่ได้ปิดและสามารถโดนมอนตัวถัดไปได้*/
                    bullet.setActive(false);

                    /*ถ้ามินิออนตาย (HP ≤ 0 และ takeDamage() ต้องตั้ง isActive=false) ให้เพิ่มคะแนน*/
                    /*ถากระสุนถูกปิดแล้ว ให้ break ออกจาก loop ของมินิออน (ไปเช็กกระสุนถัดไป) — ประหยัดการคำนวณ*/
                    if (!minion.isActive()) {
                        score.addScore(minion.getScoreValue());
                    }
                }
            }
        }

        /*ไล่มินิออนที่ active แล้วเช็คว่า player ชนมินิออนตัวนั้นไหม
        ถ้าชน:  lives.loseLife(); — ลดชีวิตผู้เล่น
               player.respawn(); — รีเซ็ตตำแหน่ง/สถานะผู้เล่นตามที่กำหนดใน respawn()
               minion.setActive(false); — ปิดมินิออน (ตาย) log warning เพื่อ debug*/
        for (Minion minion : minions) {
            if (player.intersects(minion)) {
                lives.loseLife();
                player.respawn();
                minion.setActive(false);
                GameLogger.warn("Player collided with minion!");
            }
        }
    }

    /* ปิดสวิตช์ canTransition (ห้าม transition ซ้ำ)
    คำนวณ nextLevel เป็น bossLevel ปัจจุบัน + 1 แล้วเรียก gameState.nextBoss() (ซึ่งน่าจะทำให้ internal boss level advance)
        ถ้า nextLevel ยังไม่เกิน 3 (ยังมีบอสถัดไป):ล้างสถานะโลกชั่วคราว: กระสุนผู้เล่น/ศัตรู, minions, hitEffects → เตรียมฉากใหม่ รีเซ็ตตำแหน่งผู้เล่นเป็น (100,350) แล้วเรียก player.respawn()
        ถ้า nextLevel == 3 → เตรียมค่าที่ต้องการสำหรับด่านบอส 3 (รีเซ็ต warningTimer, warningComplete, เอา crackWall ออก) และ log ว่าจะไป WARNING PHASE
        ถ้า nextLevel != 3 (คือ 1 หรือ 2) → เริ่ม startMinionWave(nextLevel) เพื่อ spawn มินิออนก่อนบอส จะพิเศษสำหรับ nextLevel == 2 คือเรียก createPlatformsBoss2() และเอา crackWall ออกด้วย
        ถ้า nextLevel > 3 → ตั้งสถานะเกมเป็น VICTORY (ชนะ) */
    private void transitionToNextBoss() {
        canTransition = false;

        int nextLevel = gameState.getCurrentBossLevel() + 1;
        gameState.nextBoss();

        if (nextLevel <= 3) {
            playerBullets.clear();
            enemyBullets.clear();
            minions.clear();
            hitEffects.clear();

            player.setX(100);
            player.setY(350);
            player.respawn();

            //               Boss 3: Start with WARNING phase
            if (nextLevel == 3) {
                warningTimer = 0;
                warningComplete = false;
                crackWall = null;
                GameLogger.info("Transitioned to Boss 3 stage - WARNING PHASE");
            } else {
                // Boss 1 and 2: Start with minion wave
                startMinionWave(nextLevel);

                if (nextLevel == 2) {
                    createPlatformsBoss2();
                    crackWall = null;
                    GameLogger.info("Transitioned to Boss 2 stage");
                }
            }
        } else {
            gameState.setState(GameState.State.VICTORY);
        }
    }

    /*สร้างออบเจ็กต์บอสตามระดับที่ส่งเข้า: DefenseWallBoss, JavaBoss, CustomBoss (ตำแหน่ง spawn เป็น 600,300)
        สำหรับ CustomBoss จะเรียก setPlayer(player) เพื่อให้บอสรู้ตำแหน่งผู้เล่น (จำเป็นถ้าบอสยิง homing bullets หรือใช้ตำแหน่งผู้เล่น)
        log ว่าโหลดบอสแล้ว*/
    private void loadBoss(int level) {
        switch (level) {
            case 1:
                currentBoss = new DefenseWallBoss(600, 300);
                break;
            case 2:
                currentBoss = new JavaBoss(600, 300);
                break;
            case 3:
                currentBoss = new CustomBoss(600, 300);
                //                Set player reference for homing bullets
                ((CustomBoss) currentBoss).setPlayer(player);
                break;
        }
        GameLogger.info("Loaded Boss " + level);
    }

    /**/
    public void render(GraphicsContext gc) {
        // Render custom boss background if available (Boss 3)
        /*ถ้าบอสมี background พิเศษ ให้วาดก่อนเป็นพื้นหลังของฉากบอส*/
        renderBossBackground(gc);

        /*วาด crackWall (effect ของบอส 1) — อยู่เหนือ background แต่ยังอยู่ใต้ตัวละคร/บอส ขึ้นอยู่กับต้องการให้มันทับหรือไม่*/
        if (crackWall != null) {
            crackWall.render(gc);
        }

        /*วาดผู้เล่น ณ จุดนี้ — หมายความผู้เล่นจะถูกวาดก่อนบอสและกระสุนที่ถูกวาดหลังจากนี้ (ขึ้นกับลำดับด้านล่าง)*/
        player.render(gc);

        /*วาดบอสเมื่ออยู่ในเฟส BOSS_FIGHT
        กรณีพิเศษ: สำหรับ CustomBoss จะวาดกระสุนของบอสด้วย (และต้องการให้กระสุนพิเศษนี้ แสดงแม้เวลา stop — ดังนั้นถูกวาดที่นี่)*/
        if (currentBoss != null && gameState.getCurrentPhase() == GameState.Phase.BOSS_FIGHT) {
            currentBoss.render(gc);

            //                  Render CustomBoss bullets (visible even during time stop)
            if (currentBoss instanceof CustomBoss) {
                CustomBoss customBoss = (CustomBoss) currentBoss;
                List<EnemyBullet> bossBullets = customBoss.getBossBullets();
                bossBullets.forEach(b -> b.render(gc));
            }
        }
        /*วาดลิสต์ entity ต่าง ๆ — หมายเหตุ: ลำดับที่เลือกอาจทำให้กระสุนของผู้เล่นถูกวาดบนบอส หรือไม่ก็แล้วแต่ต้องการ*/
        playerBullets.forEach(b -> b.render(gc));
        enemyBullets.forEach(b -> b.render(gc));
        minions.forEach(m -> m.render(gc));
        hitEffects.forEach(e -> e.render(gc));

        // Render based on current phase
        /*วาด UI/overlay เฉพาะเฟส เช่น WARNING cinematic หรือ WAVE info*/
        if (gameState.getCurrentPhase() == GameState.Phase.WARNING) {
            renderWarning(gc);
        } else if (gameState.getCurrentPhase() == GameState.Phase.MINION_WAVE && minionSpawner != null) {
            renderWaveInfo(gc);
        }

        /*สร้าง HUD ใหม่ทุกเฟรมแล้ววาด — นี่เป็นจุดที่สามารถปรับปรุงได้ (สร้างซ้ำแค่ครั้งเดียวแล้ว reuse จะดีกว่า)*/
        HUD hud = new HUD(score, lives);
        hud.render(gc);

        // Render grey screen overlay during time stop
        /*วาด overlay สำหรับ time stop (มักมี alpha/ข้อความ)
        — มาวางสุดท้ายเพื่อให้ปิดทับทุกอย่าง (ทั้ง HUD ด้วย)
        — ถ้าต้องการ overlay ให้ไม่ปิด HUD ควรวางก่อน HUD แทน*/
        renderTimeStopOverlay(gc);
    }

    //Render custom boss background if the boss supports it
    /*ถ้าบอสเป็น CustomBoss และมี background พิเศษ (hasCustomBackground() true) ให้เรียก customBoss.renderBackground(gc)
    — ดีแล้วที่แยก responsibility ให้บอสจัดการ background ของตัวเอง*/
    private void renderBossBackground(GraphicsContext gc) {
        if (currentBoss instanceof CustomBoss) {
            CustomBoss customBoss = (CustomBoss) currentBoss;
            if (customBoss.hasCustomBackground()) {
                customBoss.renderBackground(gc);
            }
        }
    }

    // Render grey screen overlay during time stop
    /*ฟังก์ชันนี้เช็กว่า currentBoss เป็น CustomBoss และมี background พิเศษหรือไม่ (hasCustomBackground()).
    ถ้ามี ให้เรียก customBoss.renderBackground(gc) เพื่อให้บอสวาดฉากหลังของมันเอง (เช่น ภาพ/เอฟเฟกต์พิเศษสำหรับบอสที่ 3)*/
    private void renderTimeStopOverlay(GraphicsContext gc) {

        /*ถ้า customBoss.isTimeStopActive() คืน true จะวาด overlay สีเทาโปร่งทับหน้าจอ:
            gc.setGlobalAlpha(0.6) + fillRect(0,0,800,600) — ทำหน้าจอมืดลง 60% ความทึบ
            หลังจากนั้น gc.setGlobalAlpha(1.0) เพื่อคืนค่าสีเต็ม (แต่แนะนำให้ใช้ gc.save()/gc.restore() แทนการ set ค่าโดยตรง)
            มีการเตรียมการวาดข้อความ "TIME STOP!" (font Impact 60) และเอฟเฟกต์ glow (stroke) แต่ทุกบรรทัดที่วาดข้อความถูกคอมเมนต์ไว้
                — ดังนั้นตอนนี้ overlay ไม่ได้แสดงข้อความใด ๆ แต่แค่ทำมืดจอ คำนวณ secondsLeft โดยใช้ customBoss.getTimeStopDuration() / 60 + 1
                — แสดงว่าฟังก์ชันคิดว่า getTimeStopDuration() น่าจะคืนค่าเป็นจำนวนเฟรม (frames) ที่เหลือ และหารด้วย 60 เพื่อแปลงเป็นวินาที (แบบปัดขึ้นด้วย +1)*/
        if (currentBoss instanceof CustomBoss) {
            CustomBoss customBoss = (CustomBoss) currentBoss;
            if (customBoss.isTimeStopActive()) {
                //                       Use setGlobalAlpha for reliable transparency
                gc.setGlobalAlpha(0.6); // 60% opacity
                gc.setFill(Color.rgb(30, 30, 30)); // Dark grey
                gc.fillRect(0, 0, 800, 600);
                gc.setGlobalAlpha(1.0); // Reset to full opacity

                // Draw TIME STOP text in center
                gc.setFill(Color.rgb(200, 0, 255)); // Bright purple
                gc.setFont(javafx.scene.text.Font.font("Impact", 60));
                //gc.fillText("TIME STOP!", 260, 290);

                // Add glow effect for text
                gc.setStroke(Color.rgb(255, 0, 255)); // Magenta glow
                gc.setLineWidth(3);
                //gc.strokeText("TIME STOP!", 260, 290);

                gc.setFill(Color.CYAN);
                gc.setFont(javafx.scene.text.Font.font("Arial", 28));
                //gc.fillText("Everything is frozen!", 250, 340);

                // Add timer countdown
                int secondsLeft = (customBoss.getTimeStopDuration() / 60) + 1;
                //gc.setFill(Color.YELLOW);
                //gc.setFont(javafx.scene.text.Font.font("Impact", 40));
                //gc.fillText(String.valueOf(secondsLeft), 380, 400);
            }
        }
    }

    /*เลือกสีตัวอักษรเป็นสีเหลือง และฟอนต์ Arial ขนาด 24px สร้างสตริง WAVE x/y จาก minionSpawner แล้ววาดตำแหน่ง (320,50)
        — ตำแหน่งเป็นค่าตายตัว (magic numbers) เปลี่ยนฟอนต์เล็กกว่า (16) แล้ววาดจำนวนศัตรู (minions.size()) ที่ (340,80)*/
    private void renderWaveInfo(GraphicsContext gc) {
        gc.setFill(Color.YELLOW);
        gc.setFont(javafx.scene.text.Font.font("Arial", 24));

        String waveText = "WAVE " + minionSpawner.getCurrentWaveNumber() +
                "/" + minionSpawner.getTotalWaves();
        gc.fillText(waveText, 320, 50);

        gc.setFont(javafx.scene.text.Font.font("Arial", 16));
        gc.fillText("Enemies: " + minions.size(), 340, 80);
    }

    /* Render WARNING animation for Boss 3
     * Displays dramatic warning with flashing effects
     */
    /*วาดพื้นมืดครึ่งโปร่งบนทั้งหน้าจอ (ปัจจุบันใช้ 800×600 คงที่ — ควรใช้ขนาด canvas แทน)*/
    private void renderWarning(GraphicsContext gc) {
        // Dark overlay for dramatic effect
        gc.setFill(Color.rgb(0, 0, 0, 0.5));
        gc.fillRect(0, 0, 800, 600);

        // Calculate animation progress (0.0 to 1.0)
        /*progress เป็น 0..1 บอกความคืบหน้า (frame-based)
        flashInterval ลดลงเมื่อ warningTimer เพิ่ม → แปลว่ากระพริบเร็วขึ้นเมื่อเข้าใกล้จบ
        ถ้าอยากให้ timing ไม่ผูกกับ FPS ให้เปลี่ยนเป็น time-based*/
        double progress = (double) warningTimer / WARNING_DURATION;

        // Flashing effect (faster as time progresses)
        int flashInterval = Math.max(10, 60 - warningTimer / 4);
        int flashFrame = (warningTimer / flashInterval) % 2;

        // Red alert flashing
        if (flashFrame == 0) {
            gc.setFill(Color.rgb(255, 0, 0, 0.3));
        } else {
            gc.setFill(Color.rgb(200, 0, 0, 0.3));
        }

        // Flash borders
        gc.fillRect(0, 0, 800, 30);      // Top
        gc.fillRect(0, 570, 800, 30);    // Bottom
        gc.fillRect(0, 0, 30, 600);      // Left
        gc.fillRect(770, 0, 30, 600);    // Right

        /*ใช้ save()/restore() รอบการแปลง (translate/scale) — ถูกต้องแล้ว
        แปลว่า origin ถูกย้ายไป (400,250) แล้ววาด "WARNING!" โดยใช้ offset ลบเพื่อ center แบบ manual
        (ใช้ค่ -160/-163 ฯลฯ)*/

        // Main WARNING text
        gc.setFont(javafx.scene.text.Font.font("Impact", 72));

        // Pulsing effect
        double pulse = Math.sin(warningTimer * 0.1) * 0.2 + 1.0;
        gc.save();
        gc.translate(400, 250);
        gc.scale(pulse, pulse);

        // Text shadow
        gc.setFill(Color.BLACK);
        gc.fillText("WARNING!", -160, 5);

        // Main text (red with yellow outline)
        gc.setFill(Color.RED);
        gc.fillText("WARNING!", -163, 0);

        gc.setStroke(Color.YELLOW);
        gc.setLineWidth(3);
        gc.strokeText("WARNING!", -163, 0);

        gc.restore();

        // Subtitle text
        gc.setFont(javafx.scene.text.Font.font("Arial", 36));
        gc.setFill(Color.YELLOW);
        gc.fillText("FINAL BOSS APPROACHING", 200, 350);

        // Countdown or progress indicator
        int secondsLeft = (WARNING_DURATION - warningTimer) / 60 + 1;
        gc.setFont(javafx.scene.text.Font.font("Arial", 48));
        gc.setFill(Color.WHITE);
        gc.fillText(String.valueOf(secondsLeft), 380, 450);

        // Animated danger lines (like in old arcade games)
        gc.setStroke(Color.rgb(255, 255, 0, 0.7));
        gc.setLineWidth(3);

        /*สร้างเส้นทแยงที่เคลื่อนที่ตาม warningTimer ให้ดูเหมือนสายอันตรายไหลผ่านจอ*/
        for (int i = 0; i < 5; i++) {
            double offset = (warningTimer * 2 + i * 100) % 800;
            gc.strokeLine(offset, 0, offset - 100, 600);
            gc.strokeLine(800 - offset, 0, 900 - offset, 600);
        }

        // Ready message at the end
        //ถ้า progress > 0.8 จะกระพริบข้อความ "GET READY!" เป็นสีเขียว
        if (progress > 0.8) {
            int readyFlash = (warningTimer / 10) % 2;
            if (readyFlash == 0) {
                gc.setFont(javafx.scene.text.Font.font("Arial", 32));
                gc.setFill(Color.LIME);
                gc.fillText("GET READY!", 310, 520);
            }
        }
    }

    /*player.shoot() ควรเป็นเมธอดที่สร้าง Bullet พร้อมตำแหน่งเริ่มต้น/ความเร็ว/owner info
    หรือคืน null หากยิงไม่ได้ (เช่นไม่มีแม็กกาซีน/อยู่ในคูลดาวน์)
    การ add ลง playerBullets ทำให้ระบบ update()/render() จะอัปเดตและวาดกระสุนเหล่านี้ต่อไป*/
    public void shoot() {
        Bullet bullet = player.shoot();
        if (bullet != null) {
            playerBullets.add(bullet);
            GameLogger.debug("Player shot bullet");
        }
    }

    /*player.shootSpecialAttack() คืน list ของกระสุน (เช่น 3 นัดในมุมต่างกัน)
    — อาจคืน null ถ้าไม่พร้อมยิงหรือเมื่อไม่มีสเปเชียลเหลือ ใช้ addAll เพื่อรวมเข้า list หลัก*/
    public void shootSpecialAttack() {
        List<Bullet> spreadBullets = player.shootSpecialAttack();
        if (spreadBullets != null && !spreadBullets.isEmpty()) {
            playerBullets.addAll(spreadBullets);
            GameLogger.info("Special Attack: Spread Shot fired! (" + spreadBullets.size() + " bullets)");
        }
    }

    public Character getPlayer() {
        return player;
    }

    public GameState getGameState() {
        return gameState;
    }

    public Boss getCurrentBoss() {
        return currentBoss;
    }

    public int getCurrentBossLevel() {
        return gameState.getCurrentBossLevel();
    }
}