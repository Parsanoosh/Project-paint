package org.example;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;

import java.util.*;

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

        var viewPort = getGameScene().getViewport();
        viewPort.bindToEntity(player, 400, 400);

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
        } else {
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
            }, Duration.seconds(0.1));
        }
    }

    private void returnToTerritory() {
        if (trail.empty()) {
            return;
        }

        // Add all the cells in the trail to the territory and also to a list of starting points for the flood fill.
        List<Entity> startingPoints = new ArrayList<>();
        while (!trail.empty()) {
            Entity cell = trail.pop();
            territory.add(cell);
            startingPoints.add(cell);
        }

        // Now, go through each cell in the starting points list.
        for (Entity startingPoint : startingPoints) {
            // Get the coordinates of the starting point.
            int startX = (int) startingPoint.getX() / BLOCK_SIZE;
            int startY = (int) startingPoint.getY() / BLOCK_SIZE;

            // Go through each cell that is adjacent to the starting point.
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    // Compute the coordinates of the adjacent cell.
                    int cellX = startX + dx;
                    int cellY = startY + dy;

                    // Check if the cell is within the grid and not part of the territory or the trail.
                    if (cellX >= 0 && cellX < cells.length && cellY >= 0 && cellY < cells[cellX].length) {
                        Entity cell = cells[cellX][cellY];
                        if (!territory.contains(cell) && !trail.contains(cell)) {
                            // Use this cell as the starting point of the verification flood fill.
                            if (verificationFloodFill(cell)) {
                                // If the verification flood fill did not reach the edges of the grid,
                                // the selected cell is inside the new territory.
                                // Now, start the actual flood fill from this cell.
                                floodFill(cell);
                            }
                        }
                    }
                }
            }
        }
    }

    private void floodFill(Entity startCell) {
        System.out.println("called floodFill:" + startCell);
        if (startCell.getComponent(CellComponent.class).getOwner() != null) {
            // This cell is already owned, so we don't need to fill it.
            return;
        }

        // Queue is used to implement flood fill iteratively instead of recursively.
        Queue<Entity> queue = new LinkedList<>();
        queue.add(startCell);

        while (!queue.isEmpty()) {
            Entity cell = queue.remove();
            CellComponent cellComponent = cell.getComponent(CellComponent.class);
            if (cellComponent.getOwner() == null) {
                cellComponent.setOwner(player);
                territory.add(cell);

                // Get neighboring cells and add them to the queue.
                int cellX = (int) cell.getX() / BLOCK_SIZE;
                int cellY = (int) cell.getY() / BLOCK_SIZE;

                if (cellX > 0) queue.add(cells[cellX - 1][cellY]);
                if (cellX < cells.length - 1) queue.add(cells[cellX + 1][cellY]);
                if (cellY > 0) queue.add(cells[cellX][cellY - 1]);
                if (cellY < cells[cellX].length - 1) queue.add(cells[cellX][cellY + 1]);
            }
        }
    }


    private boolean verificationFloodFill(Entity startCell) {
        // The verification flood fill works like the regular flood fill but does not modify the grid.
        // It only checks if the flood fill can reach the edges of the grid from the start cell.

        // Create a set to keep track of the cells that have been visited by the verification flood fill.
        Set<Entity> visited = new HashSet<>();

        // Use a stack to implement the flood fill without recursion.
        Stack<Entity> stack = new Stack<>();
        stack.push(startCell);

        while (!stack.empty()) {
            Entity cell = stack.pop();
            visited.add(cell);

            // Get the coordinates of the cell.
            int cellX = (int) cell.getX() / BLOCK_SIZE;
            int cellY = (int) cell.getY() / BLOCK_SIZE;

            // Check if the flood fill has reached the edges of the grid.
            if (cellX == 0 || cellX == cells.length - 1 || cellY == 0 || cellY == cells[cellX].length - 1) {
                // The flood fill has reached the edges of the grid, which means the start cell is outside the new territory.
                return false;
            }

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    // Compute the coordinates of the adjacent cell.
                    int adjacentCellX = cellX + dx;
                    int adjacentCellY = cellY + dy;

                    // Check if the cell is within the grid, not part of the territory or the trail, and has not been visited yet.
                    if (adjacentCellX >= 0 && adjacentCellX < cells.length && adjacentCellY >= 0 && adjacentCellY < cells[adjacentCellX].length) {
                        Entity adjacentCell = cells[adjacentCellX][adjacentCellY];
                        if (!territory.contains(adjacentCell) && !trail.contains(adjacentCell) && !visited.contains(adjacentCell)) {
                            stack.push(adjacentCell);
                        }
                    }
                }
            }
        }

        // The flood fill did not reach the edges of the grid, which means the start cell is inside the new territory.
        return true;
    }

    private void gameOver() {
        moving = false;
        FXGL.showMessage("Game Over!");
    }
}