package org.example;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import javafx.geometry.Rectangle2D;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGL.*;

public class Main extends GameApplication {
    private static final int BLOCK_SIZE = 20;
    private Entity player;
    private boolean moving = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void initInput() {
        Input input = FXGL.getInput();

        input.addAction(new UserAction("Move Up") {
            @Override
            protected void onAction() {
                movePlayer(0, -20);
            }
        }, KeyCode.W);

        input.addAction(new UserAction("Move Down") {
            @Override
            protected void onAction() {
                movePlayer(0, 20);
            }
        }, KeyCode.S);

        input.addAction(new UserAction("Move Left") {
            @Override
            protected void onAction() {
                movePlayer(-20, 0);
            }
        }, KeyCode.A);

        input.addAction(new UserAction("Move Right") {
            @Override
            protected void onAction() {
                movePlayer(20, 0);
            }
        }, KeyCode.D);
    }

    @Override
    protected void initGame() {
        getGameWorld()
                .addEntityFactory(new SimpleFactory());
        getGameWorld()
                .addEntityFactory(new GridFactory());

        for (int y = 0; y < 40; y++) {
            for (int x = 0; x < 40; x++) {
                spawn("cell", x * BLOCK_SIZE, y * BLOCK_SIZE);
            }
        }

        player = FXGL.spawn("player", 400, 400);
/*        spawn("enemy", 100, 100);

        spawn("ally", 600, 100);

        run(() -> {
            spawn("ally", FXGLMath.randomPoint(
                    new Rectangle2D(0,0, getAppWidth(), getAppHeight())));
            spawn("enemy", FXGLMath.randomPoint(
                    new Rectangle2D(0,0, getAppWidth(), getAppHeight())));
            spawn("cell", FXGLMath.randomPoint(
                    new Rectangle2D(0,0, getAppWidth(), getAppHeight())));

        }, Duration.seconds(1));*/
    }

    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setWidth(800);
        gameSettings.setHeight(810);
        gameSettings.setTitle("Paint I.O");
        gameSettings.setVersion("0.1");
        gameSettings.setIntroEnabled(false);
    }

    private void movePlayer(int dx, int dy) {
        if (moving) {
            return;
        }
        double futureX = player.getX() + dx;
        double futureY = player.getY() + dy;

        // check if future position is within game boundary
        if (futureX >= 0 && futureX <= (FXGL.getAppWidth() - BLOCK_SIZE)
                && futureY >= 0 && futureY <= (FXGL.getAppHeight() - BLOCK_SIZE)) {
            moving = true;
            player.translateX(dx);
            player.translateY(dy);
            System.out.println("Player position: X = " + player.getX() + ", Y = " + player.getY());
            FXGL.runOnce(new Runnable() {
                @Override
                public void run() {
                    moving = false;
                }
            }, Duration.seconds(0.2));
        }
    }
}