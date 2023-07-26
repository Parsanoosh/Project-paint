package org.example;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import static com.almasb.fxgl.dsl.FXGLForKtKt.geti;

public class Enemy {

    private final Stack<Entity> trail = new Stack<>();
    private final Set<Entity> territory = new HashSet<>();
    private Entity entity;
    private EntityType type;
    private Action action = Action.NONE;
    private boolean moving = false;

    public Enemy(Entity entity, EntityType type) {
        this.entity = entity;
        this.type = type;
    }

    public void colorizeTerritory(HashMap<Integer, HashMap<Integer, Entity>> mapOfCells) {
        int cellX = (int) entity.getX() / geti("block_size");
        int cellY = (int) entity.getY() / geti("block_size");

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                Entity cell = mapOfCells.get(cellX + dx).get(cellY + dy);
                cell.getComponent(CellComponent.class).setOwner(entity);
                territory.add(cell);
            }
        }

    }

    public void gameOver(HashMap<Integer, HashMap<Integer, Entity>> mapOfCells) {
        for (Entity cell : territory) {
            if (cell.getComponent(CellComponent.class).getOwner() == entity)
                cell.getComponent(CellComponent.class).setOwner(null);
        }
        territory.clear();
        for (Entity cell : trail) {
            if (cell.getComponent(CellComponent.class).getOwner() == entity)
                cell.getComponent(CellComponent.class).setOwner(null);
        }
        trail.clear();
        entity.removeFromWorld();
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public EntityType getType() {
        return type;
    }

    public void setType(EntityType type) {
        this.type = type;
    }

    public Stack<Entity> getTrail() {
        return trail;
    }

    public Set<Entity> getTerritory() {
        return territory;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }


    public boolean isMoving() {
        return moving;
    }

    public void setMoving(boolean moving) {
        this.moving = moving;
    }
}
