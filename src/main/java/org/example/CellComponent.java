package org.example;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class CellComponent extends Component {

    private Entity owner;

    public Entity getOwner() {
        return owner;
    }

    public void setOwner(Entity owner) {
        this.owner = owner;
        ((Rectangle) entity.getViewComponent().getChildren().get(0)).setFill(playerColor(owner));
    }

    private Color playerColor(Entity player) {
        if (player == null) {
            return EntityType.CELL.getColor();
        } else {
            return switch ((EntityType) player.getType()) {
                case PLAYER -> EntityType.PLAYER.getColor();
                case ENEMY_1 -> EntityType.ENEMY_1.getColor();
                case ENEMY_2 -> EntityType.ENEMY_2.getColor();
                case ENEMY_3 -> EntityType.ENEMY_3.getColor();
                default -> EntityType.CELL.getColor();
            };
        }
    }

}
