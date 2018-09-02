package com.bombbird.atcsim.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.bombbird.atcsim.AtcSim;
import com.bombbird.atcsim.entities.restrictions.Obstacle;

import java.util.ArrayList;

class RctpScreen extends GameScreen {
    private FileHandle obstacles;
    private Array<Obstacle> obsArray;

    RctpScreen(final AtcSim game) {
        super(game);

        //Set stage params
        stage = new Stage(new FitViewport(1440, 810));
        stage.getViewport().update(AtcSim.WIDTH, AtcSim.HEIGHT, true);
        inputProcessor2 = stage;
        inputMultiplexer.addProcessor(inputProcessor1);
        inputMultiplexer.addProcessor(inputProcessor2);
        Gdx.input.setInputProcessor(inputMultiplexer);

        //Set camera params
        camera = (OrthographicCamera) stage.getViewport().getCamera();
        camera.setToOrtho(false,1440, 810);
        viewport = new FitViewport(AtcSim.WIDTH, AtcSim.HEIGHT, camera);
        viewport.apply();

        //Load files containing obstacle information
        obstacles = Gdx.files.internal("game/rctp/rctp.obs");
        obsArray = new Array<Obstacle>();
    }

    private void loadUI() {
        //Reset stage
        stage.clear();

        //Load range circles
        loadRange();

        //Load scroll listener
        loadScroll();

        //Load altitude restrictions
        String obsStr = obstacles.readString();
        String[] indivObs = obsStr.split("\\r?\\n");
        for (String s: indivObs) {
            //For each individual obstacle:
            String[] obsInfo = s.split(", ");
            int index = 0;
            int minAlt = 0;
            String text = "";
            int textX = 0;
            int textY = 0;
            ArrayList<Float> vertices = new ArrayList<Float>();
            for (String s1: obsInfo) {
                switch (index) {
                    case 0: minAlt = Integer.parseInt(s1); break;
                    case 1: text = s1; break;
                    case 2: textX = Integer.parseInt(s1); break;
                    case 3: textY = Integer.parseInt(s1); break;
                    default: vertices.add(Float.parseFloat(s1));
                }
                index++;
            }
            int i = 0;
            float[] verts = new float[vertices.size()];
            for (float f: vertices) {
                verts[i++] = f;
            }
            Obstacle obs = new Obstacle(game, verts, minAlt, text, textX, textY, shapeRenderer);
            obsArray.add(obs);
            stage.addActor(obs);
        }
    }

    @Override
    void renderShape() {
        for (Obstacle obstacle: obsArray) {
            obstacle.renderShape();
        }
    }

    @Override
    public void show() {
        loadUI();
    }

    @Override
    public void dispose() {
        stage.clear();
        stage.dispose();
        skin.dispose();
    }
}
