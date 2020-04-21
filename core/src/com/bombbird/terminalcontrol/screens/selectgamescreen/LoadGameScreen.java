package com.bombbird.terminalcontrol.screens.selectgamescreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.screens.MainMenuScreen;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.utilities.RenameManager;
import com.bombbird.terminalcontrol.utilities.saving.FileLoader;
import com.bombbird.terminalcontrol.utilities.saving.GameSaver;
import org.json.JSONArray;
import org.json.JSONObject;

public class LoadGameScreen extends SelectGameScreen {
    private final Timer timer;
    private Label loadingLabel;

    class MultithreadLoad implements Runnable {
        @Override
        public void run() {
            JSONArray saves = FileLoader.loadSaves();
            Gdx.app.postRunnable(() -> {
                loadSavedGamesUI(saves);
                stopAnimatingLabel();
            });
        }
    }

    public LoadGameScreen(final TerminalControl game, Image background) {
        super(game, background);
        timer = new Timer();
        TerminalControl.updateRevision();
    }

    /** Overrides loadLabel method in SelectGameScreen to load appropriate title for label */
    @Override
    public void loadLabel() {
        //Set label params
        super.loadLabel();
        Label headerLabel = new Label("Choose save to load:", getLabelStyle());
        headerLabel.setWidth(MainMenuScreen.BUTTON_WIDTH);
        headerLabel.setHeight(MainMenuScreen.BUTTON_HEIGHT);
        headerLabel.setPosition(2880 / 2.0f - MainMenuScreen.BUTTON_WIDTH / 2.0f, 1620 * 0.85f);
        headerLabel.setAlignment(Align.center);
        getStage().addActor(headerLabel);
    }

    /** Overrides loadScroll method in SelectGameScreen to load save info into scrollPane */
    @Override
    public void loadScroll() {
        loadingLabel = new Label("", getLabelStyle());
        loadingLabel.setPosition(2880 / 2.0f - loadingLabel.getWidth() / 2.0f, 1620 * 0.5f);
        loadingLabel.setVisible(true);
        stage.addActor(loadingLabel);
        animateLoadingLabel();

        new Thread(new MultithreadLoad()).start(); //Load the saves from another thread
    }

    /** Called after file I/O is complete to display the loaded saves */
    private void loadSavedGamesUI(JSONArray saves) {
        final Label label = new Label("No saves found!", getLabelStyle());
        label.setPosition(2880 / 2.0f - label.getWidth() / 2.0f, 1620 * 0.5f);
        label.setVisible(false);
        stage.addActor(label);
        if (saves.length() == 0) {
            label.setVisible(true);
        }
        for (int i = 0; i < saves.length(); i++) {
            float multiplier = 1f;
            final JSONObject jsonObject = saves.getJSONObject(i);
            final String toDisplay = RenameManager.renameAirportICAO(jsonObject.getString("MAIN_NAME")) + " (Score: " + jsonObject.getInt("score") + "    High score: " + jsonObject.getInt("highScore") + ")\nPlanes landed: " + jsonObject.getInt("landings") + "    Planes departed: " + jsonObject.getInt("airborne");

            final TextButton saveButton = new TextButton(toDisplay, getButtonStyle());
            saveButton.setName(toDisplay.substring(0, 4));

            FileHandle handle = Gdx.files.internal("game/available.arpt");
            Array<int[]> airacs = new Array<>();
            String[] airports = handle.readString().split("\\r?\\n");
            for (String icao: airports) {
                if (icao.split(":")[0].equals(saveButton.getName())) {
                    String airacRanges = icao.split(":")[1];
                    for (String range: airacRanges.split(",")) {
                        airacs.add(new int[] {Integer.parseInt(range.split("-")[0]), Integer.parseInt(range.split("-")[1])});
                    }
                    break;
                }
            }
            int newestAirac = airacs.get(0)[1]; //Newest airac is always the largest number at the 1st position in array
            int airac = jsonObject.getInt("AIRAC");
            for (int[] range: airacs) {
                if (airac >= range[0] && airac <= range[1]) {
                    airac = range[1];
                }
            }
            jsonObject.put("AIRAC", airac);
            if (airac < newestAirac) {
                saveButton.setText(toDisplay + "\nNote: New game AIRAC " + newestAirac + " has changed\nsignificantly from older AIRAC " + airac);
                multiplier = 1.75f;
            }

            saveButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    RadarScreen radarScreen = new RadarScreen(game, jsonObject);
                    TerminalControl.radarScreen = radarScreen;
                    game.setScreen(radarScreen);

                    event.handle();
                }
            });
            getScrollTable().add(saveButton).width(MainMenuScreen.BUTTON_WIDTH * 1.2f).height(MainMenuScreen.BUTTON_HEIGHT * multiplier);

            final TextButton deleteButton = new TextButton("Delete", getButtonStyle());
            deleteButton.setName("" + 0);
            deleteButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    Gdx.app.postRunnable(() -> {
                        if ("0".equals(deleteButton.getName())) {
                            deleteButton.setText("Press again to\nconfirm delete");
                            deleteButton.setName("" + 1);
                        } else {
                            GameSaver.deleteSave(jsonObject.getInt("saveId"));
                            Cell<TextButton> cell = getScrollTable().getCell(deleteButton);
                            Cell<TextButton> cell1 = getScrollTable().getCell(saveButton);
                            getScrollTable().removeActor(deleteButton);
                            getScrollTable().removeActor(saveButton);

                            //Fix UI bug that may happen after deleting cells - set cell size to 0 rather than deleting them
                            cell.size(cell.getPrefWidth(), 0);
                            cell1.size(cell1.getPrefWidth(), 0);

                            getScrollTable().invalidate();
                        }
                        if (!getScrollTable().hasChildren()) {
                            label.setVisible(true);
                        }
                    });
                }
            });
            getScrollTable().add(deleteButton).width(MainMenuScreen.BUTTON_WIDTH * 0.4f).height(MainMenuScreen.BUTTON_HEIGHT * multiplier);
            getScrollTable().row();
        }
        ScrollPane scrollPane = new ScrollPane(getScrollTable());
        scrollPane.setFadeScrollBars(true);
        scrollPane.setupFadeScrollBars(1, 1.5f);
        scrollPane.setX(2880 / 2f - MainMenuScreen.BUTTON_WIDTH * 0.8f);
        scrollPane.setY(1620 * 0.2f);
        scrollPane.setWidth(MainMenuScreen.BUTTON_WIDTH * 1.6f);
        scrollPane.setHeight(1620 * 0.6f);

        getStage().addActor(scrollPane);
    }

    /** Starts animating the loading label */
    private void animateLoadingLabel() {
        timer.scheduleTask(new Timer.Task() {
            @Override
            public void run() {
                Gdx.app.postRunnable(() -> {
                    loadingLabel.setText("Loading.");
                    loadingLabel.setPosition(2880 / 2.0f - loadingLabel.getPrefWidth() / 2.0f, 1620 * 0.5f);
                });
            }
        }, 0.25f);
        timer.scheduleTask(new Timer.Task() {
            @Override
            public void run() {
                Gdx.app.postRunnable(() -> {
                    loadingLabel.setText("Loading..");
                    loadingLabel.setPosition(2880 / 2.0f - loadingLabel.getPrefWidth() / 2.0f, 1620 * 0.5f);
                });
            }
        }, 0.5f);
        timer.scheduleTask(new Timer.Task() {
            @Override
            public void run() {
                Gdx.app.postRunnable(() -> {
                    loadingLabel.setText("Loading...");
                    loadingLabel.setPosition(2880 / 2.0f - loadingLabel.getPrefWidth() / 2.0f, 1620 * 0.5f);
                });
                animateLoadingLabel();
            }
        }, 0.75f);
    }

    /** Stops animating the loading label */
    private void stopAnimatingLabel() {
        Gdx.app.postRunnable(() -> {
            loadingLabel.setVisible(false);
            timer.clear();
        });
    }
}