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

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import static com.almasb.fxgl.dsl.FXGL.*;

public class Main extends GameApplication {
    private static final int BLOCK_SIZE = 20;
    private Entity player;
    private boolean moving = false;
    private Entity[][] cells;
    private Stack<Entity> trail = new Stack<>();
    private Set<Entity> territory = new HashSet<>();

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

        cells = new Entity[40][40];
        int[][] initialTerritoryPositions = {
                {19, 20}, {19, 19}, {20, 19}, {20, 20}, {21, 19},
                {21, 20}, {21, 21}, {20, 21}, {19, 21}
        };

        player = spawn("player", 400, 400);
        for (int y = 0; y < 40; y++) {
            for (int x = 0; x < 40; x++) {
                cells[x][y] = spawn("cell", x * BLOCK_SIZE, y * BLOCK_SIZE);
            }
        }

        for (int[] position : initialTerritoryPositions) {
            Entity cell = cells[position[0]][position[1]];
            cell.getComponent(CellComponent.class).setOwner(player);
            territory.add(cell);
        }


/*        Entity startingCell = cells[20][20];
        startingCell.getComponent(CellComponent.class).setOwner(player);
        territory.add(startingCell);*/
        System.out.println("STARTING TERRR:" + territory);
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
        int cellX = (int) player.getX() / BLOCK_SIZE;
        int cellY = (int) player.getY() / BLOCK_SIZE;
        Entity currentCell = cells[cellX][cellY];

        if (territory.contains(currentCell)) {
            if (!trail.empty()) {
                // Player has returned to territory, claim the cells in the trail
                returnToTerritory();
                System.out.println("NEW TERRIROTY:" + territory);
            }
        }
        else {
            if (trail.contains(currentCell)) {
                // Player has collided with their own trail, game over
                gameOver();
                return;
            }
            trail.push(currentCell);
        }

        currentCell.getComponent(CellComponent.class).setOwner(player);


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

    private void returnToTerritory() {
        while (!trail.empty()) {
            Entity cell = trail.pop();
            territory.add(cell);
        }
    }

    private void gameOver() {
        moving = false;
        FXGL.showMessage("Game Over!");
    }
}