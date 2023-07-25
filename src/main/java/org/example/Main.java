package org.example;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.pathfinding.CellMoveComponent;
import com.almasb.fxgl.pathfinding.CellState;
import com.almasb.fxgl.pathfinding.astar.AStarGrid;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;
import javafx.util.Pair;

import java.util.*;

import static com.almasb.fxgl.dsl.FXGL.*;
import static org.example.EntityType.CELL;
import static org.example.EntityType.PLAYER;

public class Main extends GameApplication {
    private static final int BLOCK_SIZE = 20;
    private static int MAX_X = 39;
    private static int MAX_Y = 39;
    private static int MIN_X = 0;
    private static int MIN_Y = 0;

    private static final Pair<Integer, Integer> NORTH = new Pair<>(0, -1);
    private static final Pair<Integer, Integer> SOUTH = new Pair<>(0, +1);
    private static final Pair<Integer, Integer> EAST = new Pair<>(+1, 0);
    private static final Pair<Integer, Integer> WEST = new Pair<>(-1, 0);

    private static final int BOUND_TARGET = 5;
    private Entity player;
    private boolean moving = false;
    private final HashMap<Integer, HashMap<Integer, Entity>> mapOfCells = new HashMap<>();
    private final Stack<Entity> trail = new Stack<>();
    private final Set<Entity> territory = new HashSet<>();

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
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setWidth(800);
        gameSettings.setHeight(810);
        gameSettings.setTitle("Paint I.O");
        gameSettings.setVersion("1.0");
        gameSettings.setIntroEnabled(false);
    }

    static double distance(int x1, int y1, int x2, int y2) {
        // Calculating distance
        return Math.sqrt(Math.pow(x2 - x1, 2)
                + Math.pow(y2 - y1, 2));
    }

    @Override
    protected void initGame() {
        getGameWorld()
                .addEntityFactory(new SimpleFactory());
        getGameWorld()
                .addEntityFactory(new GridFactory());

        int[][] initialTerritoryPositions = {
                {19, 20}, {19, 19}, {20, 19}, {20, 20}, {21, 19},
                {21, 20}, {21, 21}, {20, 21}, {19, 21}
        };

        player = spawn("player", 400, 400);

        for (int x = 0; x <= MAX_X; x++) {
            var map = new HashMap<Integer, Entity>();
            for (int y = 0; y <= MAX_Y; y++) {
                map.put(y, spawn("cell", x * BLOCK_SIZE, y * BLOCK_SIZE));
            }
            mapOfCells.put(x, map);
        }

        for (int[] position : initialTerritoryPositions) {
            Entity cell = mapOfCells.get(position[0]).get(position[1]);
            cell.getComponent(CellComponent.class).setOwner(player);
            territory.add(cell);
        }

        var viewPort = getGameScene().getViewport();
        viewPort.bindToEntity(player, 400, 400);
        viewPort.setZoom(1.83);
        //viewPort.setBounds(0,0,800,800)

    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("score", 0);
    }

    @Override
    protected void onUpdate(double tpf) {
        super.onUpdate(tpf);
        updateGrid();
    }

    private void addCellToUp() {
        int updatedMinY = MIN_Y - 10;

        for (int x = MIN_X; x <= MAX_X; x++) {
            var map = mapOfCells.get(x);
            for (int y = MIN_Y - 1; y >= updatedMinY; y--) {
                map.put(y, spawn("cell", x * BLOCK_SIZE, y * BLOCK_SIZE));
                System.out.println("UP X:" + x + " Y:" + y);
            }
            mapOfCells.put(x, map);
        }
        MIN_Y = updatedMinY;
    }

    private void addCellToDown() {
        int updatedMaxY = MAX_Y + 10;

        for (int x = MIN_X; x <= MAX_X; x++) {
            var map = mapOfCells.get(x);
            for (int y = MAX_Y; y <= updatedMaxY; y++) {
                map.put(y, spawn("cell", x * BLOCK_SIZE, y * BLOCK_SIZE));
                System.out.println("DOWN X:" + x + " Y:" + y);
            }
            mapOfCells.put(x, map);
        }
        MAX_Y = updatedMaxY;
    }

    private void addCellToRight() {
        int updatedMaxX = MAX_X + 10;

        for (int x = MAX_X; x <= updatedMaxX; x++) {
            var map = new HashMap<Integer, Entity>();
            for (int y = MIN_Y; y <= MAX_Y; y++) {
                map.put(y, spawn("cell", x * BLOCK_SIZE, y * BLOCK_SIZE));
                System.out.println("R X:" + x + " Y:" + y);
            }
            mapOfCells.put(x, map);
        }
        MAX_X = updatedMaxX;
    }

    private void addCellToLeft() {
        int updatedMinX = MIN_X - 10;

        for (int x = MIN_X; x >= updatedMinX; x--) {
            var map = new HashMap<Integer, Entity>();
            for (int y = MIN_Y; y <= MAX_Y; y++) {
                map.put(y, spawn("cell", x * BLOCK_SIZE, y * BLOCK_SIZE));
                System.out.println("L X:" + x + " Y:" + y);
            }
            mapOfCells.put(x, map);
        }
        MIN_X = updatedMinX;

    }

    private void updateGrid() {
        int cellX = (int) player.getX() / BLOCK_SIZE;
        int cellY = (int) player.getY() / BLOCK_SIZE;

        boolean closeToLeft = distance(cellX, cellY, MIN_X, cellY) < BOUND_TARGET;
        boolean closeToRight = distance(cellX, cellY, MAX_X, cellY) < BOUND_TARGET;
        boolean closeToUp = distance(cellX, cellY, cellX, MIN_Y) < BOUND_TARGET;
        boolean closeToDown = distance(cellX, cellY, cellX, MAX_Y) < BOUND_TARGET;

        if (closeToLeft) {
            addCellToLeft();
        }

        if (closeToRight) {
            addCellToRight();
        }

        if (closeToUp) {
            addCellToUp();
        }

        if (closeToDown) {
            addCellToDown();
        }

    }

    private void movePlayer(int dx, int dy) {
        if (moving) {
            return;
        }
        int cellX = (int) player.getX() / BLOCK_SIZE;
        int cellY = (int) player.getY() / BLOCK_SIZE;
        Entity currentCell = mapOfCells.get(cellX).get(cellY);


        if (territory.contains(currentCell)) {
            if (!trail.empty()) {
                // Player has returned to territory, claim the cells in the trail
                returnToTerritory();
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

        moving = true;
        player.translateX(dx);
        player.translateY(dy);
        System.out.println("Player position: X = " + ((int) player.getX() / BLOCK_SIZE) + ", Y = " + ((int) player.getY() / BLOCK_SIZE));
        FXGL.runOnce(new Runnable() {
            @Override
            public void run() {
                moving = false;
            }
        }, Duration.seconds(0.1));

    }

    private void returnToTerritory() {
        if (trail.empty()) {
            return;
        }

        System.out.println("RETURN TO TERRITORY CALLED.");
        // Add all the cells in the trail to the territory and also to a list of starting points for the flood fill.
        List<Entity> startingPoints = new ArrayList<>();
        while (!trail.empty()) {
            Entity cell = trail.pop();
            territory.add(cell);
            startingPoints.add(cell);
        }
        System.out.println("STARTING POINTS:" + startingPoints);
        // Now, go through each cell in the starting points list.
        for (Entity startingPoint : startingPoints) {
            // Get the coordinates of the starting point.
            int startX = (int) startingPoint.getX() / BLOCK_SIZE;
            int startY = (int) startingPoint.getY() / BLOCK_SIZE;

            Pair<Integer, Integer> NORTH = new Pair<>(0, -1);
            Pair<Integer, Integer> SOUTH = new Pair<>(0, +1);
            Pair<Integer, Integer> EAST = new Pair<>(+1, 0);
            Pair<Integer, Integer> WEST = new Pair<>(-1, 0);
            List<Pair<Integer, Integer>> directions = new ArrayList<>();
            directions.add(NORTH);
            directions.add(SOUTH);
            directions.add(EAST);
            directions.add(WEST);

            for (var pair : directions) {
                int cellX = startX + pair.getKey();
                int cellY = startY + pair.getValue();

                boolean condition = cellX >= MIN_X && cellX < MAX_X && cellY >= MIN_Y && cellY < MAX_Y;
                // Check if the cell is within the grid and not part of the territory or the trail.
                if (condition) {
                    Entity cell = mapOfCells.get(cellX).get(cellY);
                    if (!territory.contains(cell)) {
                        // Use this cell as the starting point of the verification flood fill.
                        if (verification(cell)) {
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


                if (cellX > MIN_X) queue.add(mapOfCells.get(cellX - 1).get(cellY));
                if (cellX < MAX_X) queue.add(mapOfCells.get(cellX + 1).get(cellY));
                if (cellY > MIN_Y) queue.add(mapOfCells.get(cellX).get(cellY - 1));
                if (cellY < MAX_Y) queue.add(mapOfCells.get(cellX).get(cellY + 1));
            }
        }
    }

    private boolean verification(Entity currentCell) {
        boolean north = false;
        boolean south = false;
        boolean east = false;
        boolean west = false;

        int cellX = (int) currentCell.getX() / BLOCK_SIZE;
        int cellY = (int) currentCell.getY() / BLOCK_SIZE;

        while (cellY > MIN_Y) {
            cellY += NORTH.getValue();

            if (mapOfCells.get(cellX) != null) {
                Entity newCell = mapOfCells.get(cellX).get(cellY);

                if (newCell != null) {
                    if (territory.contains(newCell)) {
                        north = true;
                        break;
                    }
                }
            }
        }
        cellX = (int) currentCell.getX() / BLOCK_SIZE;
        cellY = (int) currentCell.getY() / BLOCK_SIZE;

        while (cellX < MAX_X) {
            cellX += EAST.getKey();

            if (mapOfCells.get(cellX) != null) {
                Entity newCell = mapOfCells.get(cellX).get(cellY);
                if (newCell != null) {
                    if (territory.contains(newCell)) {
                        east = true;
                        break;
                    }
                }
            }
        }
        cellX = (int) currentCell.getX() / BLOCK_SIZE;
        cellY = (int) currentCell.getY() / BLOCK_SIZE;

        while (cellY < MAX_Y) {
            cellY += SOUTH.getValue();

            if (mapOfCells.get(cellX) != null) {
                Entity newCell = mapOfCells.get(cellX).get(cellY);
                if (newCell != null) {
                    if (territory.contains(newCell)) {
                        south = true;
                        break;
                    }
                }
            }
        }
        cellX = (int) currentCell.getX() / BLOCK_SIZE;
        cellY = (int) currentCell.getY() / BLOCK_SIZE;

        while (cellX > MIN_X) {
            cellX += WEST.getKey();


            if (mapOfCells.get(cellX) != null) {
                Entity newCell = mapOfCells.get(cellX).get(cellY);
                if (newCell != null) {
                    if (territory.contains(newCell)) {
                        west = true;
                        break;
                    }
                }
            }
        }

        System.out.println("NORTH:" + north + " SOUTH:" + south + " EAST:" + east + " WEST:" + west + " ENTITY:" + currentCell);
        return north && south && east && west;
    }

    private void gameOver() {
        moving = false;
        getDialogService().showMessageBox("Game Over. Press OK to restart.", getGameController()::startNewGame);
    }

}