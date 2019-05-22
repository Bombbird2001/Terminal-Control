package com.bombbird.terminalcontrol.screens.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.airports.Airport;
import com.bombbird.terminalcontrol.utilities.Fonts;

public class RunwayChanger {
    private ImageButton background;
    private Label airportLabel;
    private TextButton changeButton;
    private Label newRunwaysLabel;
    private boolean doubleCfm;
    private Array<String> runways;
    private Array<boolean[]> tkofLdg;
    private Airport airport;

    private static final boolean[] ALL_ACTIVE = {true, true, true};
    private static final boolean[] ALL_INACTIVE = {false, false, false};
    private static final boolean[] TKOFF_ONLY = {true, true, false};
    private static final boolean[] LDG_ONLY = {true, false, true};

    public RunwayChanger() {
        background = new ImageButton(TerminalControl.skin.getDrawable("ListBackground"));
        background.setX(0.1f * TerminalControl.radarScreen.ui.getPaneWidth());
        background.setY(3240 * 0.05f);
        background.setSize(0.8f * TerminalControl.radarScreen.ui.getPaneWidth(), 3240 * 0.35f);
        TerminalControl.radarScreen.uiStage.addActor(background);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.fontColor = Color.WHITE;
        labelStyle.font = Fonts.defaultFont20;

        airportLabel = new Label("", labelStyle);
        airportLabel.setPosition(0.45f * TerminalControl.radarScreen.ui.getPaneWidth(), 3240 * 0.4f);
        TerminalControl.radarScreen.uiStage.addActor(airportLabel);

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = Fonts.defaultFont30;
        textButtonStyle.up = TerminalControl.skin.getDrawable("Button_up");
        textButtonStyle.down = TerminalControl.skin.getDrawable("Button_down");
        changeButton = new TextButton("Change runway configuration", textButtonStyle);
        changeButton.setSize(0.6f * TerminalControl.radarScreen.ui.getPaneWidth(), 3240 * 0.05f);
        changeButton.setX(0.2f * TerminalControl.radarScreen.ui.getPaneWidth());
        changeButton.setY(3240 * 0.20f);
        doubleCfm = false;
        TerminalControl.radarScreen.uiStage.addActor(changeButton);

        Label.LabelStyle labelStyle1 = new Label.LabelStyle();
        labelStyle1.fontColor = Color.BLACK;
        labelStyle1.font = Fonts.defaultFont20;
        newRunwaysLabel = new Label("Loading...", labelStyle1);
        TerminalControl.radarScreen.uiStage.addActor(newRunwaysLabel);

        setVisible(false);

        runways = new Array<String>();
        tkofLdg = new Array<boolean[]>();
    }

    public void setVisible(boolean visible) {
        background.setVisible(visible);
        airportLabel.setVisible(visible);
        changeButton.setVisible(visible);
        newRunwaysLabel.setVisible(visible);
    }

    public void setAirport(String icao) {
        runways.clear();
        tkofLdg.clear();
        airport = TerminalControl.radarScreen.airports.get(icao);
        int windDir = airport.getMetar().isNull("windDirection") ? 0 : airport.getMetar().getInt("windDirection");
        int windSpd = airport.getMetar().getInt("windSpeed");
        if ("RCTP".equals(icao)) {
            updateRCTP(windDir, windSpd);
        } else if ("RCSS".equals(icao)) {
            updateRCSS(windDir, windSpd);
        } else if ("WSSS".equals(icao)) {
            updateWSSS(windDir, windSpd);
        } else if ("RJTT".equals(icao)) {
            updateRJTT(windSpd);
        } else if ("RJAA".equals(icao)) {
            updateRJAA(windDir, windSpd);
        } else if ("RJBB".equals(icao)) {
            updateRJBB(windDir, windSpd);
        } else if ("RJOO".equals(icao)) {
            updateRJOO();
        } else if ("RJBE".equals(icao)) {
            updateRJBE();
        } else if ("VHHH".equals(icao)) {
            updateVHHH(windDir, windSpd);
        } else if ("VMMC".equals(icao)) {
            updateVMMC(windDir, windSpd);
        } else if ("VTBD".equals(icao)) {
            updateVTBD(windDir, windSpd);
        } else if ("VTBS".equals(icao)) {
            updateVTBS(windDir, windSpd);
        }
    }

    private void updateRCTP(int windDir, int windSpd) {
        if (airport.getLandingRunways().get("05L") != null) {
            //05s are active, set to 23s
            if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("23L").getHeading()) > -5) {
                runways.add("23L", "23R", "05L", "05R");
                tkofLdg.add(ALL_ACTIVE, ALL_ACTIVE, ALL_INACTIVE, ALL_INACTIVE);
            }
        } else {
            //23s are active, set to 05s
            if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("05L").getHeading()) > -5) {
                runways.add("05L", "05R", "23L", "23R");
                tkofLdg.add(ALL_ACTIVE, ALL_ACTIVE, ALL_INACTIVE, ALL_INACTIVE);
            }
        }
    }

    private void updateRCSS(int windDir, int windSpd) {
        if (airport.getLandingRunways().get("10") != null) {
            //10 is active, set to 28
            if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("28").getHeading()) > -5) {
                runways.add("28", "10");
                tkofLdg.add(ALL_ACTIVE, ALL_INACTIVE);
            }
        } else {
            //28 is active, set to 10
            if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("10").getHeading()) > -5) {
                runways.add("10", "28");
                tkofLdg.add(ALL_ACTIVE, ALL_INACTIVE);
            }
        }
    }

    private void updateWSSS(int windDir, int windSpd) {
        if (airport.getLandingRunways().get("02L") != null) {
            //02s are active, set to 20s
            if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("20C").getHeading()) > -5) {
                runways.add("20C", "20R", "02L", "02C");
                tkofLdg.add(ALL_ACTIVE, ALL_ACTIVE, ALL_INACTIVE, ALL_INACTIVE);
            }
        } else {
            //20s are active, set to 02s
            if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("02L").getHeading()) > -5) {
                runways.add("02L", "02C", "20C", "20R");
                tkofLdg.add(ALL_ACTIVE, ALL_ACTIVE, ALL_INACTIVE, ALL_INACTIVE);
            }
        }
    }

    private void updateRJTT(int windSpd) {
        if (windSpd < 7) {
            //Runway change permitted only when wind speed is below 7 knots
            if (airport.getTakeoffRunways().get("05") != null) {
                //34s for landing, 05 and 34R for takeoff, set to 16s for takeoff, 22 and 23 for landings
                runways.add("16L", "16R", "22", "23");
                tkofLdg.add(TKOFF_ONLY, TKOFF_ONLY, LDG_ONLY, LDG_ONLY);
                runways.add("34L", "34R", "05");
                tkofLdg.add(ALL_INACTIVE, ALL_INACTIVE, ALL_INACTIVE);
            } else {
                //16s for takeoff, 22 and 23 for landing, set to 34s for landing, 05 and 34R for takeoff
                runways.add("34L", "34R", "05");
                tkofLdg.add(LDG_ONLY, ALL_ACTIVE, TKOFF_ONLY);
                runways.add("16L", "16R", "22", "23");
                tkofLdg.add(ALL_INACTIVE, ALL_INACTIVE, ALL_INACTIVE, ALL_INACTIVE);
            }
        }
    }

    private void updateRJAA(int windDir, int windSpd) {
        if (airport.getLandingRunways().get("16L") != null) {
            //16s are active, set to 34s
            if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("34L").getHeading()) > -5) {
                runways.add("34L", "34R", "16L", "16R");
                tkofLdg.add(ALL_ACTIVE, ALL_ACTIVE, ALL_INACTIVE, ALL_INACTIVE);
            }
        } else {
            //34s are active, set to 16s
            if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("16L").getHeading()) > -5) {
                runways.add("16L", "16R", "34L", "34R");
                tkofLdg.add(ALL_ACTIVE, ALL_ACTIVE, ALL_INACTIVE, ALL_INACTIVE);
            }
        }
    }

    private void updateRJBB(int windDir, int windSpd) {
        if (airport.getLandingRunways().get("06L") != null) {
            //06s are active, set to 24s
            if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("24L").getHeading()) > -5) {
                runways.add("24L", "24R", "06L", "06R");
                tkofLdg.add(ALL_ACTIVE, ALL_ACTIVE, ALL_INACTIVE, ALL_INACTIVE);
            }
        } else {
            //24s are active, set to 06s
            if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("06L").getHeading()) > -5) {
                runways.add("06L", "06R", "24L", "24R");
                tkofLdg.add(ALL_ACTIVE, ALL_ACTIVE, ALL_INACTIVE, ALL_INACTIVE);
            }
        }
    }

    private void updateRJOO() {
        //RJOO cannot undergo runway change
    }

    private void updateRJBE() {
        //RJOO cannot undergo runway change
    }

    private void updateVHHH(int windDir, int windSpd) {
        if (airport.getLandingRunways().get("07L") != null) {
            //07s are active, set to 25s
            if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("25L").getHeading()) > -5) {
                runways.add("25L", "25R", "07L", "07R");
                tkofLdg.add(ALL_ACTIVE, ALL_ACTIVE, ALL_INACTIVE, ALL_INACTIVE);
            }
        } else {
            //25s are active, set to 07s
            if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("07L").getHeading()) > -5) {
                runways.add("07L", "07R", "25L", "25R");
                tkofLdg.add(ALL_ACTIVE, ALL_ACTIVE, ALL_INACTIVE, ALL_INACTIVE);
            }
        }
    }

    private void updateVMMC(int windDir, int windSpd) {
        if (airport.getLandingRunways().get("16") != null) {
            //16 is active, set to 34
            if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("34").getHeading()) > -5) {
                runways.add("34", "16");
                tkofLdg.add(ALL_ACTIVE, ALL_INACTIVE);
            }
        } else {
            //34 is active, set to 16
            if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("16").getHeading()) > -5) {
                runways.add("16", "34");
                tkofLdg.add(ALL_ACTIVE, ALL_INACTIVE);
            }
        }
    }

    private void updateVTBD(int windDir, int windSpd) {
        if (airport.getLandingRunways().get("03L") != null) {
            //03s are active, set to 21s
            if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("21L").getHeading()) > -5) {
                runways.add("21L", "21R", "03L", "03R");
                tkofLdg.add(TKOFF_ONLY, LDG_ONLY, ALL_INACTIVE, ALL_INACTIVE);
            }
        } else {
            //21s are active, set to 03s
            if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("03L").getHeading()) > -5) {
                runways.add("03L", "03R", "21L", "21R");
                tkofLdg.add(LDG_ONLY, TKOFF_ONLY, ALL_INACTIVE, ALL_INACTIVE);
            }
        }
    }

    private void updateVTBS(int windDir, int windSpd) {
        if (airport.getLandingRunways().get("01L") != null) {
            //01s are active, set to 19s
            if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("19L").getHeading()) > -5) {
                runways.add("19L", "19R", "01L", "01R");
                tkofLdg.add(ALL_ACTIVE, ALL_ACTIVE, ALL_INACTIVE, ALL_INACTIVE);
            }
        } else {
            //25s are active, set to 01s
            if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("01L").getHeading()) > -5) {
                runways.add("01L", "01R", "19L", "19R");
                tkofLdg.add(ALL_ACTIVE, ALL_ACTIVE, ALL_INACTIVE, ALL_INACTIVE);
            }
        }
    }
}
