package org.example;

public enum SpeedType {
    FAST(0.1, 0.75), NORMAL(0.25, 1), SLOW(0.5, 1.5);
    final double speedOfPlayer, speedOfEnemy;

    SpeedType(double speedOfPlayer, double speedOfEnemy) {
        this.speedOfPlayer = speedOfPlayer;
        this.speedOfEnemy = speedOfEnemy;
    }
}
