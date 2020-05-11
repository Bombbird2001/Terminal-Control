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

public class DisplaySettingsScreen extends SettingsTemplateScreen {
    public SelectBox<String> trajectoryLine;
    public SelectBox<String> pastTrajectory;
    public SelectBox<String> mva;
    public SelectBox<String> ilsDash;

    public Label trajectoryLabel;
    public int trajectorySel;

    public Label pastTrajLabel;
    public int pastTrajTime;

    public Label mvaLabel;
    public boolean showMva;

    public Label ilsDashLabel;
    public boolean showIlsDash;

    public DisplaySettingsScreen(TerminalControl game, RadarScreen radarScreen, Image background) {
        super(game, radarScreen, background);

        loadUI(-1200, 0);

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
                    Gdx.app.log("SettingsScreen", "Unknown MVA setting " + mva.getSelected());
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
                    Gdx.app.log("SettingsScreen", "Unknown ILS dash setting " + ilsDash.getSelected());
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
    }

    /** Loads actors for display settings into tabs */
    @Override
    public void loadTabs() {
        SettingsTab tab1 = new SettingsTab(this, 2);
        tab1.addActors(trajectoryLine, trajectoryLabel);
        tab1.addActors(pastTrajectory, pastTrajLabel);
        tab1.addActors(mva, mvaLabel);
        tab1.addActors(ilsDash, ilsDashLabel);

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
        } else {
            //Use game settings
            trajectorySel = radarScreen.trajectoryLine;
            pastTrajTime = radarScreen.pastTrajTime;
            showMva = radarScreen.showMva;
            showIlsDash = radarScreen.showIlsDash;
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
    }

    /** Confirms and applies the changes set */
    @Override
    public void sendChanges() {
        if (radarScreen != null) {
            radarScreen.trajectoryLine = trajectorySel;
            radarScreen.pastTrajTime = pastTrajTime;
            radarScreen.showMva = showMva;
            radarScreen.showIlsDash = showIlsDash;
        } else {
            TerminalControl.trajectorySel = trajectorySel;
            TerminalControl.pastTrajTime = pastTrajTime;
            TerminalControl.showMva = showMva;
            TerminalControl.showIlsDash = showIlsDash;
        }
    }
}
