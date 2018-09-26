package com.bombbird.atcsim.screens;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Ui implements Disposable {
    private Stage stage;
    private SelectBox<String> selectBox;

    Ui() {
        stage = new Stage(new FitViewport(5760, 3240));
    }

    @Override
    public void dispose() {
        stage.clear();
        stage.dispose();
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
