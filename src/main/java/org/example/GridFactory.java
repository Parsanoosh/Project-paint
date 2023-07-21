package org.example;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.ProjectileComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import javafx.geometry.Point2D;
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
                .with(new CellComponent())
                .build();
    }

    @Spawns("player")
    public Entity newPlayer(SpawnData data) {
        return FXGL.entityBuilder(data)
                .view("steve.png")
                .zIndex(100)
                .build();
    }

}
