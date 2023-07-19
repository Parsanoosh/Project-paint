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

public class SimpleFactory implements EntityFactory {

    @Spawns("enemy")
    public Entity newEnemy(SpawnData data) {
        return FXGL.entityBuilder(data)
                .view(new Rectangle(40,40, Color.RED))
                //.view("steve.png")
                .with(new ProjectileComponent(new Point2D(1, 0), 50))
                .build();
    }

    @Spawns("ally")
    public Entity newAlly(SpawnData data) {
        //var texture = FXGL.texture("steve.png").multiplyColor(Color.BLACK);

        return FXGL.entityBuilder(data)
                .view(new Rectangle(40,40, Color.BLUE))
                .with(new ProjectileComponent(new Point2D(-1, 0), 50))
                .build();
    }

}
