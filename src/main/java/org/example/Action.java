package org.example;

public enum Action {
    UP(0, -20),
    DOWN(0, +20),
    LEFT(-20, 0),
    RIGHT(+20, 0),
    NONE(0, 0);


    final int dx, dy;

    Action(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }
}
