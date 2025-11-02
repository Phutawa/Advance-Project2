package se233.contra.controller;

import se233.contra.model.*;
import se233.contra.model.entity.*;
import se233.contra.model.entity.Character;
import se233.contra.util.GameLogger;

import java.util.List;

public class CollisionController {

    public void checkCollisions(
            Character player,
            Boss boss,
            List<Bullet> playerBullets,
            List<EnemyBullet> enemyBullets,
            List<Minion> minions,
            List<HitEffect> hitEffects,
            Score score,
            Lives lives
    ) {
        // Player bullets hit boss
        for (Bullet bullet : playerBullets) {
            if (bullet.intersects(boss)) {
                boss.takeDamage(bullet.getDamage());

                //     Spawn hit effect at bullet position
                hitEffects.add(new HitEffect(bullet.getX(), bullet.getY()));

                bullet.setActive(false);
                GameLogger.info("Boss hit!");
            }
        }

        // Player bullets hit minions
        for (Bullet bullet : playerBullets) {
            for (Minion minion : minions) {
                if (bullet.intersects(minion)) {
                    minion.takeDamage(bullet.getDamage());

                    //   Spawn hit effect at bullet position
                    hitEffects.add(new HitEffect(bullet.getX(), bullet.getY()));

                    bullet.setActive(false);
                    if (!minion.isActive()) {
                        score.addScore(minion.getScoreValue());
                    }
                }
            }
        }

        // Enemy bullets hit player
        for (EnemyBullet bullet : enemyBullets) {
            if (bullet.intersects(player)) {
                lives.loseLife();
                bullet.setActive(false);
                player.respawn();
                GameLogger.warn("Player hit by enemy bullet!");
            }
        }

        // Boss collides with player
        if (player.intersects(boss)) {
            lives.loseLife();
            player.respawn();
            GameLogger.warn("Player collided with boss!");
        }
    }
}