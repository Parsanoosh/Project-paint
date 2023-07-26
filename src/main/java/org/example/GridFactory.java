package org.example;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

public class GridFactory implements EntityFactory {
    private static final int BLOCK_SIZE = 20;

    @Spawns("cell")
    public Entity newCell(SpawnData data) {
        var view = new Rectangle(BLOCK_SIZE, BLOCK_SIZE, Color.WHITE);
        view.setStroke(Color.BLACK);
        view.setStrokeType(StrokeType.INSIDE);
        view.setStrokeWidth(0.4);
        return FXGL.entityBuilder(data)
                .view(view)
                .type(EntityType.CELL)
                .with(new CellComponent())
                .build();
    }

    @Spawns("player")
    public Entity newPlayer(SpawnData data) {
        return FXGL.entityBuilder(data)
                .view("steve.png")
                .type(EntityType.PLAYER)
                .zIndex(100)
                .build();
    }

    @Spawns("enemy_1")
    public Entity newEnemy_1(SpawnData data) {
        return FXGL.entityBuilder(data)
                .view("maxpayne.png")
                .type(EntityType.ENEMY_1)
                .zIndex(99)
                .build();
    }

    @Spawns("enemy_2")
    public Entity newEnemy_2(SpawnData data) {
        return FXGL.entityBuilder(data)
                .view("barbie.png")
                .type(EntityType.ENEMY_2)
                .zIndex(99)
                .build();
    }

    @Spawns("enemy_3")
    public Entity newEnemy_3(SpawnData data) {
        return FXGL.entityBuilder(data)
                .view("guts.png")
                .type(EntityType.ENEMY_3)
                .zIndex(99)
                .build();
    }
}
