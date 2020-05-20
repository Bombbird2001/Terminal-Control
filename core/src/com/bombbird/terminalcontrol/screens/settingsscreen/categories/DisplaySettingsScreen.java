package com.bombbird.terminalcontrol.screens.settingsscreen.categories;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen;
import com.bombbird.terminalcontrol.screens.settingsscreen.SettingsTab;
import com.bombbird.terminalcontrol.screens.settingsscreen.SettingsTemplateScreen;
import com.bombbird.terminalcontrol.utilities.saving.GameSaver;

public class DisplaySettingsScreen extends SettingsTemplateScreen {
    public SelectBox<String> trajectoryLine;
    public SelectBox<String> pastTrajectory;
    public SelectBox<String> mva;
    public SelectBox<String> ilsDash;
    public SelectBox<String> showUncontrolledTrail;
    public SelectBox<String> rangeCircle;
    public SelectBox<String> colour;

    public Label trajectoryLabel;
    public int trajectorySel;

    public Label pastTrajLabel;
    public int pastTrajTime;

    public Label mvaLabel;
    public boolean showMva;

    public Label ilsDashLabel;
    public boolean showIlsDash;

    public Label showUncontrolledLabel;
    public boolean showUncontrolled;

    public Label rangeCircleLabel;
    public int rangeCircleDist;

    public Label colourLabel;
    public int colourStyle;

    public DisplaySettingsScreen(TerminalControl game, RadarScreen radarScreen, Image background) {
        super(game, radarScreen, background);

        infoString = "Set the game display settings below.";
        loadUI(-1200, -200);

        setOptions();
    }

    /** Loads selectBox for display settings */
    @Override
    public void loadBoxes() {
        trajectoryLine = createStandardSelectBox();
        trajectoryLine.setItems("Off", "60 sec", "90 sec", "120 sec", "150 sec");
        trajectoryLine.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if ("Off".equals(trajectoryLine.getSelected())) {
                    trajectorySel = 0;
                } else {
                    trajectorySel = Integer.parseInt(trajectoryLine.getSelected().split(" ")[0]);
                }
            }
        });

        pastTrajectory = createStandardSelectBox();
        pastTrajectory.setItems("Off", "60 sec", "120 sec", "All");
        pastTrajectory.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if ("Off".equals(pastTrajectory.getSelected())) {
                    pastTrajTime = 0;
                } else if ("All".equals(pastTrajectory.getSelected())) {
                    pastTrajTime = -1;
                } else {
                    pastTrajTime = Integer.parseInt(pastTrajectory.getSelected().split(" ")[0]);
                }
            }
        });

        mva = createStandardSelectBox();
        mva.setItems("Show", "Hide");
        mva.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if ("Show".equals(mva.getSelected())) {
                    showMva = true;
                } else if ("Hide".equals(mva.getSelected())) {
                    showMva = false;
                } else {
                    Gdx.app.log(getClass().getName(), "Unknown MVA setting " + mva.getSelected());
                }
            }
        });

        ilsDash = createStandardSelectBox();
        ilsDash.setItems("Simple", "Realistic");
        ilsDash.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if ("Simple".equals(ilsDash.getSelected())) {
                    showIlsDash = false;
                } else if ("Realistic".equals(ilsDash.getSelected())) {
                    showIlsDash = true;
                } else {
                    Gdx.app.log(getClass().getName(), "Unknown ILS dash setting " + ilsDash.getSelected());
                }
            }
        });

        showUncontrolledTrail = createStandardSelectBox();
        showUncontrolledTrail.setItems("When selected", "Always");
        showUncontrolledTrail.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if ("Always".equals(showUncontrolledTrail.getSelected())) {
                    showUncontrolled = true;
                } else if ("When selected".equals(showUncontrolledTrail.getSelected())) {
                    showUncontrolled = false;
                } else {
                    Gdx.app.log(getClass().getName(), "Unknown uncontrolled trail setting " + ilsDash.getSelected());
                }
            }
        });

        rangeCircle = createStandardSelectBox();
        rangeCircle.setItems("Off", "5nm", "10nm", "15nm", "20nm");
        rangeCircle.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if ("Off".equals(rangeCircle.getSelected())) {
                    rangeCircleDist = 0;
                } else {
                    rangeCircleDist = Integer.parseInt(rangeCircle.getSelected().replace("nm", ""));
                }
            }
        });

        colour = createStandardSelectBox();
        colour.setItems("More colourful", "More standardised");
        colour.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if ("More colourful".equals(colour.getSelected())) {
                    colourStyle = 0;
                } else if ("More standardised".equals(colour.getSelected())) {
                    colourStyle = 1;
                } else {
                    Gdx.app.log(getClass().getName(), "Unknown colour style setting " + colour.getSelected());
                }
            }
        });
    }

    /** Loads labels for display settings */
    @Override
    public void loadLabel() {
        super.loadLabel();

        trajectoryLabel = new Label("Trajectory line: ", labelStyle);
        pastTrajLabel = new Label("Aircraft trail: ", labelStyle);
        mvaLabel = new Label("MVA altitude: ", labelStyle);
        ilsDashLabel = new Label("ILS display: ", labelStyle);
        showUncontrolledLabel = new Label("Show uncontrolled:\naircraft trail", labelStyle);
        rangeCircleLabel = new Label("Range rings:", labelStyle);
        colourLabel = new Label("Colour style:", labelStyle);
    }

    /** Loads actors for display settings into tabs */
    @Override
    public void loadTabs() {
        SettingsTab tab1 = new SettingsTab(this, 2);
        tab1.addActors(trajectoryLine, trajectoryLabel);
        tab1.addActors(pastTrajectory, pastTrajLabel);
        tab1.addActors(mva, mvaLabel);
        tab1.addActors(ilsDash, ilsDashLabel);
        tab1.addActors(showUncontrolledTrail, showUncontrolledLabel);
        tab1.addActors(rangeCircle, rangeCircleLabel);
        tab1.addActors(colour, colourLabel);

        settingsTabs.add(tab1);
    }

    /** Sets relevant options into select boxes */
    @Override
    public void setOptions() {
        if (radarScreen == null) {
            //Use default settings
            trajectorySel = TerminalControl.trajectorySel;
            pastTrajTime = TerminalControl.pastTrajTime;
            showMva = TerminalControl.showMva;
            showIlsDash = TerminalControl.showIlsDash;
            showUncontrolled = TerminalControl.showUncontrolled;
            rangeCircleDist = TerminalControl.rangeCircleDist;
            colourStyle = TerminalControl.colourStyle;
        } else {
            //Use game settings
            trajectorySel = radarScreen.trajectoryLine;
            pastTrajTime = radarScreen.pastTrajTime;
            showMva = radarScreen.showMva;
            showIlsDash = radarScreen.showIlsDash;
            showUncontrolled = radarScreen.showUncontrolled;
            rangeCircleDist = radarScreen.rangeCircleDist;
            colourStyle = radarScreen.colourStyle;
        }

        trajectoryLine.setSelected(trajectorySel == 0 ? "Off" : trajectorySel + " sec");
        if (pastTrajTime == -1) {
            pastTrajectory.setSelected("All");
        } else if (pastTrajTime == 0) {
            pastTrajectory.setSelected("Off");
        } else {
            pastTrajectory.setSelected(pastTrajTime + " sec");
        }

        mva.setSelected(showMva ? "Show" : "Hide");
        ilsDash.setSelected(showIlsDash ? "Realistic" : "Simple");
        showUncontrolledTrail.setSelected(showUncontrolled ? "Always" : "When selected");
        rangeCircle.setSelected(rangeCircleDist == 0 ? "Off" : rangeCircleDist + "nm");
        colour.setSelectedIndex(colourStyle);
    }

    /** Confirms and applies the changes set */
    @Override
    public void sendChanges() {
        if (radarScreen != null) {
            radarScreen.trajectoryLine = trajectorySel;
            radarScreen.pastTrajTime = pastTrajTime;
            radarScreen.showMva = showMva;
            radarScreen.showIlsDash = showIlsDash;
            radarScreen.showUncontrolled = showUncontrolled;
            boolean rangeDistChanged = radarScreen.rangeCircleDist != rangeCircleDist;
            radarScreen.rangeCircleDist = rangeCircleDist;
            boolean colourChanged = radarScreen.colourStyle != colourStyle;
            radarScreen.colourStyle = colourStyle;

            if (rangeDistChanged) radarScreen.loadRange(); //Reload the range circles in case of any changes
            if (colourChanged) radarScreen.updateColourStyle(); //Update the label colours
        } else {
            TerminalControl.trajectorySel = trajectorySel;
            TerminalControl.pastTrajTime = pastTrajTime;
            TerminalControl.showMva = showMva;
            TerminalControl.showIlsDash = showIlsDash;
            TerminalControl.showUncontrolled = showUncontrolled;
            TerminalControl.rangeCircleDist = rangeCircleDist;
            TerminalControl.colourStyle = colourStyle;

            GameSaver.saveSettings();
        }
    }
}
