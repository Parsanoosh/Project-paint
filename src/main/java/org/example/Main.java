package org.example;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;
import javafx.util.Pair;

import java.util.*;

import static com.almasb.fxgl.dsl.FXGL.*;
import static org.example.EntityType.*;

public class Main extends GameApplication {
    private static final int BLOCK_SIZE = 20;
    private static int MAX_X = 29;
    private static int MAX_Y = 29;
    private static int MIN_X = 0;
    private static int MIN_Y = 0;

    private final int[][] initialTerritoryPositions = {
            {14, 15}, {14, 14}, {15, 14}, {15, 15}, {16, 14},
            {16, 15}, {16, 16}, {15, 16}, {14, 16}
    };

    private static final Pair<Integer, Integer> NORTH = new Pair<>(0, -1);
    private static final Pair<Integer, Integer> SOUTH = new Pair<>(0, +1);
    private static final Pair<Integer, Integer> EAST = new Pair<>(+1, 0);
    private static final Pair<Integer, Integer> WEST = new Pair<>(-1, 0);

    private static final int BOUND_TARGET = 5;
    private Entity player;
    private List<Enemy> enemies = new ArrayList<>();
    private boolean moving = false;
    private final HashMap<Integer, HashMap<Integer, Entity>> mapOfCells = new HashMap<>();
    private final Stack<Entity> trail = new Stack<>();
    private final Set<Entity> territory = new HashSet<>();
    private Action action = Action.NONE;
    private boolean smartAI = true;

    public static void main(String[] args) {
        launch(args);
    }


    @Override
    protected void initInput() {
        Input input = FXGL.getInput();

        input.addAction(new UserAction("Move Up") {
            @Override
            protected void onAction() {
                movePlayer(Action.UP.dx, Action.UP.dy);
                action = Action.UP;
            }
        }, KeyCode.W);

        input.addAction(new UserAction("Move Down") {
            @Override
            protected void onAction() {
                movePlayer(Action.DOWN.dx, Action.DOWN.dy);
                action = Action.DOWN;
            }
        }, KeyCode.S);

        input.addAction(new UserAction("Move Left") {
            @Override
            protected void onAction() {
                movePlayer(Action.LEFT.dx, Action.LEFT.dy);
                action = Action.LEFT;
            }
        }, KeyCode.A);

        input.addAction(new UserAction("Move Right") {
            @Override
            protected void onAction() {
                movePlayer(Action.RIGHT.dx, Action.RIGHT.dy);
                action = Action.RIGHT;
            }
        }, KeyCode.D);
    }

    static double distance(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2)
                + Math.pow(y2 - y1, 2));
    }


    @Override
    protected void initGame() {
        getGameWorld()
                .addEntityFactory(new SimpleFactory());
        getGameWorld()
                .addEntityFactory(new GridFactory());

        initLeveL();
        initView();
    }

    static double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2)
                + Math.pow(y2 - y1, 2));
    }

    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setWidth(600);
        gameSettings.setHeight(600);
        gameSettings.setTitle("Paint I.O");
        gameSettings.setVersion("1.0");
        gameSettings.setIntroEnabled(false);
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("block_size", BLOCK_SIZE);
    }

    @Override
    protected void onUpdate(double tpf) {
        super.onUpdate(tpf);
        updateGrid();
        for (Enemy enemy : enemies) {
            moveEnemy(enemy);
        }
    }

    private void initLeveL() {
        player = spawn("player", 300, 300);
        enemies.add(new Enemy(spawn("enemy_1", 5 * BLOCK_SIZE, 25 * BLOCK_SIZE), ENEMY_1));
        enemies.add(new Enemy(spawn("enemy_2", 25 * BLOCK_SIZE, 25 * BLOCK_SIZE), ENEMY_2));

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

        for (Enemy enemy : enemies) {
            enemy.colorizeTerritory(mapOfCells);
        }
    }

    private void initView() {
        var viewPort = getGameScene().getViewport();
        viewPort.bindToEntity(player, 300, 300);
        viewPort.setZoom(1);
        //viewPort.setBounds(0,0,800,800)
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
                returnToTerritory(this.trail, this.territory, player);
            }
        } else {
            if (trail.contains(currentCell)) {
                gameOver();
                return;
            }
            trail.push(currentCell);
        }

        for (Enemy otherEnemy : enemies) {
            if (otherEnemy.getTrail().contains(currentCell)) {
                otherEnemy.gameOver(this.mapOfCells);
                otherEnemy.setEntity(null);
                FXGL.getNotificationService().pushNotification("Enemy Died.");
            }
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

    private void moveEnemy(Enemy enemy) {
        if (enemy.getEntity() != null) {
            if (enemy.isMoving()) {
                return;
            }

            int cellX = (int) enemy.getEntity().getX() / BLOCK_SIZE;
            int cellY = (int) enemy.getEntity().getY() / BLOCK_SIZE;
            Entity currentCell = mapOfCells.get(cellX).get(cellY);


            if (enemy.getTerritory().contains(currentCell)) {
                if (!enemy.getTrail().empty()) {
                    System.out.println("Enemy Back");
                    returnToTerritory(enemy.getTrail(), enemy.getTerritory(), enemy.getEntity());
                }
            } else {
                if (enemy.getTrail().contains(currentCell)) {
                    System.out.println("ENEMY OVER");
                    enemy.gameOver(this.mapOfCells);
                    enemy.setEntity(null);
                    FXGL.getNotificationService().pushNotification("Enemy Died.");
                    return;
                }
                enemy.getTrail().push(currentCell);
            }

            for (Enemy otherEnemy : enemies) {
                if (otherEnemy != enemy) {
                    if (otherEnemy.getTrail().contains(currentCell)) {
                        otherEnemy.gameOver(this.mapOfCells);
                        otherEnemy.setEntity(null);
                        FXGL.getNotificationService().pushNotification("Enemy Died.");
                    }
                }
            }

            if (this.trail.contains(currentCell)) {
                gameOver();
            }

            currentCell.getComponent(CellComponent.class).setOwner(enemy.getEntity());

            var move = Action.NONE;

            if (smartAI) {
                Map<Action, Double> distanceMap = new HashMap<>();
                distanceMap.put(Action.UP, distance(player.getX(), player.getY(), enemy.getEntity().getX() + Action.UP.dx, enemy.getEntity().getY() + Action.UP.dy));
                distanceMap.put(Action.RIGHT, distance(player.getX(), player.getY(), enemy.getEntity().getX() + Action.RIGHT.dx, enemy.getEntity().getY() + Action.RIGHT.dy));
                distanceMap.put(Action.DOWN, distance(player.getX(), player.getY(), enemy.getEntity().getX() + Action.DOWN.dx, enemy.getEntity().getY() + Action.DOWN.dy));
                distanceMap.put(Action.LEFT, distance(player.getX(), player.getY(), enemy.getEntity().getX() + Action.LEFT.dx, enemy.getEntity().getY() + Action.LEFT.dy));

                double minDistance = Collections.min(distanceMap.values());
                for (Map.Entry<Action, Double> entry : distanceMap.entrySet()) {
                    if (entry.getValue() == minDistance) {
                        move = entry.getKey();
                    }
                }
            } else {
                move = Action.values()[random(0, Action.values().length - 1)];
            }


            enemy.setMoving(true);
            enemy.getEntity().translateX(move.dx);
            enemy.getEntity().translateY(move.dy);
            System.out.println("Enemy position: X = " + ((int) enemy.getEntity().getX() / BLOCK_SIZE) + ", Y = " + ((int) enemy.getEntity().getY() / BLOCK_SIZE));
            FXGL.runOnce(new Runnable() {
                @Override
                public void run() {
                    enemy.setMoving(false);
                }
            }, Duration.seconds(1));
        }
    }

    private void returnToTerritory(Stack<Entity> trail, Set<Entity> territory, Entity player) {
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
                        if (verification(cell, territory)) {
                            // If the verification flood fill did not reach the edges of the grid,
                            // the selected cell is inside the new territory.
                            // Now, start the actual flood fill from this cell.
                            floodFill(cell, territory, player);
                        }
                    }
                }
            }
        }
    }

    private void floodFill(Entity startCell, Set<Entity> territory, Entity player) {
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

    private boolean verification(Entity currentCell, Set<Entity> territory) {
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

    private void gameOver() {
        moving = false;
        getDialogService().showMessageBox("Game Over. Press OK to Exit.", getGameController()::exit);
    }


}