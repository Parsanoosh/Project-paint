package org.example;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Rectangle2D;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGL.*;

public class Main extends GameApplication {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void initInput() {
        super.initInput();
    }

    @Override
    protected void initGame() {
        getGameWorld()
                .addEntityFactory(new SimpleFactory());

        spawn("enemy", 100, 100);

        spawn("ally", 600, 100);

        run(() -> {
            spawn("ally", FXGLMath.randomPoint(
                    new Rectangle2D(0,0, getAppWidth(), getAppHeight())));
            spawn("enemy", FXGLMath.randomPoint(
                    new Rectangle2D(0,0, getAppWidth(), getAppHeight())));

        }, Duration.seconds(1));
    }

    @Override
    protected void initSettings(GameSettings gameSettings) {

    }
}