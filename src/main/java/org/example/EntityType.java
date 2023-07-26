package org.example;

import javafx.scene.paint.Color;

public enum EntityType {

    PLAYER(Color.BLUE), CELL(Color.WHITE), ENEMY_1(Color.RED),
    ENEMY_2(Color.PINK), ENEMY_3(Color.YELLOW);

    private Color color;

    EntityType(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
