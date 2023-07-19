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
            return Color.LIGHTGRAY;
        } else {
            return Color.BLUE; // Change this to your player's color
        }
    }

}
