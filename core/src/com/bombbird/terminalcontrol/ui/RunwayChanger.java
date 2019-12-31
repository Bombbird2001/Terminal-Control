package com.bombbird.terminalcontrol.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.airports.Airport;
import com.bombbird.terminalcontrol.entities.trafficmanager.MaxTraffic;
import com.bombbird.terminalcontrol.utilities.Fonts;

public class RunwayChanger {
    private Image background;
    private Label airportLabel;
    private Label timeLabel;
    private TextButton changeButton;
    private ScrollPane scrollPane;
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
        background = new Image(TerminalControl.skin.getDrawable("ListBackground"));
        background.setX(0.1f * TerminalControl.radarScreen.ui.getPaneWidth());
        background.setY(3240 * 0.05f);
        background.setSize(0.8f * TerminalControl.radarScreen.ui.getPaneWidth(), 3240 * 0.35f);
        background.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                event.handle(); //Prevents hiding of runway changer when background is tapped
            }
        });
        TerminalControl.radarScreen.uiStage.addActor(background);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.fontColor = Color.WHITE;
        labelStyle.font = Fonts.defaultFont20;

        airportLabel = new Label("", labelStyle);
        airportLabel.setPosition(0.45f * TerminalControl.radarScreen.ui.getPaneWidth(), 3240 * 0.35f);
        TerminalControl.radarScreen.uiStage.addActor(airportLabel);

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = Fonts.defaultFont20;
        textButtonStyle.fontColor = Color.WHITE;
        textButtonStyle.up = TerminalControl.skin.getDrawable("Button_up");
        textButtonStyle.down = TerminalControl.skin.getDrawable("Button_down");
        changeButton = new TextButton("Change runway configuration", textButtonStyle);
        changeButton.align(Align.center);
        changeButton.setSize(0.7f * TerminalControl.radarScreen.ui.getPaneWidth(), 3240 * 0.08f);
        changeButton.setX(0.15f * TerminalControl.radarScreen.ui.getPaneWidth());
        changeButton.setY(3240 * 0.22f);
        doubleCfm = false;
        changeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                event.handle();
                if (!doubleCfm) {
                    scrollPane.setVisible(true);
                    if (runways.size > 0) {
                        doubleCfm = true;
                        changeButton.setText("Confirm runway change");
                    }
                } else if (runways.size > 0) {
                    airport.setPendingRwyChange(true);
                    airport.setRwyChangeTimer(-1);
                    updateRunways();
                    airport.setPendingRwyChange(false);
                    airport.setRwyChangeTimer(301);
                    doubleCfm = false;
                    hideAll();
                    TerminalControl.radarScreen.getCommBox().setVisible(true);
                }
            }
        });
        TerminalControl.radarScreen.uiStage.addActor(changeButton);

        Label.LabelStyle labelStyle1 = new Label.LabelStyle();
        labelStyle1.fontColor = Color.BLACK;
        labelStyle1.font = Fonts.defaultFont20;
        newRunwaysLabel = new Label("Loading...", labelStyle1);
        newRunwaysLabel.setX(0.15f * TerminalControl.radarScreen.ui.getPaneWidth());
        newRunwaysLabel.setY(3240 * 0.1f);
        newRunwaysLabel.setWrap(true);

        Table scrollTable = new Table();
        scrollPane = new ScrollPane(scrollTable);
        scrollPane.setX(0.11f * TerminalControl.radarScreen.ui.getPaneWidth());
        scrollPane.setY(3240 * 0.05f);
        scrollPane.setSize(0.79f * TerminalControl.radarScreen.ui.getPaneWidth(), 3240 * 0.15f);
        scrollPane.getStyle().background = TerminalControl.skin.getDrawable("ListBackground");
        newRunwaysLabel.setWidth(scrollPane.getWidth());
        scrollTable.add(newRunwaysLabel).width(scrollPane.getWidth()).pad(10, 0, 15, 0).getActor().invalidate();

        InputListener inputListener = null;
        for (EventListener eventListener: scrollPane.getListeners()) {
            if (eventListener instanceof InputListener) {
                inputListener = (InputListener) eventListener;
            }
        }
        if (inputListener != null) scrollPane.removeListener(inputListener);
        scrollPane.setVisible(false);
        TerminalControl.radarScreen.uiStage.addActor(scrollPane);

        timeLabel = new Label("Time left: ", labelStyle1);
        timeLabel.setX(0.15f * TerminalControl.radarScreen.ui.getPaneWidth());
        timeLabel.setY(3240 * 0.3f);
        timeLabel.setWidth(0.7f * TerminalControl.radarScreen.ui.getPaneWidth());
        TerminalControl.radarScreen.uiStage.addActor(timeLabel);
        timeLabel.setVisible(false);

        runways = new Array<>();
        tkofLdg = new Array<>();

        hideAll();
    }

    public void update() {
        if (airport == null) return;
        if (airport.getRwyChangeTimer() < 0) {
            hideAll();
        }
        int min = ((int) airport.getRwyChangeTimer()) / 60;
        int sec = (int) airport.getRwyChangeTimer() - min * 60;
        String secText = sec < 10 ? "0" + sec : Integer.toString(sec);
        timeLabel.setText("Time left: " + min + ":" + secText);
    }

    public void setMainVisible(boolean visible) {
        background.setVisible(visible);
        airportLabel.setVisible(visible);
        changeButton.setVisible(visible);
    }

    public void hideAll() {
        background.setVisible(false);
        timeLabel.setVisible(false);
        airportLabel.setVisible(false);
        changeButton.setVisible(false);
        scrollPane.setVisible(false);
        runways.clear();
        tkofLdg.clear();
        doubleCfm = false;
    }

    public void updateBoxWidth(float paneWidth) {
        background.setX(0.1f * paneWidth);
        background.setWidth(0.8f * paneWidth);

        timeLabel.setX(0.15f * paneWidth);

        airportLabel.setX(0.45f * paneWidth);

        changeButton.setX(0.15f * paneWidth);
        changeButton.setWidth(0.7f * paneWidth);
    }

    public void setAirport(String icao) {
        runways.clear();
        tkofLdg.clear();
        doubleCfm = false;
        scrollPane.setVisible(false);
        changeButton.setText("Change runway configuration");
        airportLabel.setText(icao);
        airport = TerminalControl.radarScreen.airports.get(icao);
        timeLabel.setVisible(airport.isPendingRwyChange());
        int windDir = airport.getMetar().isNull("windDirection") ? 0 : airport.getMetar().getInt("windDirection");
        int windSpd = airport.getMetar().getInt("windSpeed");
        if (windDir == 0) windSpd = 0;
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
        } else if ("LEMD".equals(icao)) {
            updateLEMD(windDir, windSpd);
        } else if ("LFPG".equals(icao)) {
            updateLFPG(windDir, windSpd);
        } else if ("LFPO".equals(icao)) {
            updateLFPO(windDir, windSpd);
        } else {
            Gdx.app.log("Runway changer", "Runway change settings for " + icao + " are unavailable.");
        }

        if (runways.size == 0) {
            newRunwaysLabel.setText("Runway change not permitted due to winds");
        } else {
            if (runways.size != tkofLdg.size) Gdx.app.log("Runway changer", "Runway array length not equal to tkofldg array length for " + icao);
            updateRunwayLabel();
            if (airport.isPendingRwyChange()) {
                doubleCfm = true;
                changeButton.setText("Change runways now");
                scrollPane.setVisible(true);
            }
        }
    }

    private void updateRunwayLabel() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < runways.size; i++) {
            if (tkofLdg.get(i)[0] && !airport.getRunways().get(runways.get(i)).isEmergencyClosed() && !airport.getRunways().get(runways.get(i)).getOppRwy().isEmergencyClosed()) {
                String tmp;
                if (tkofLdg.get(i)[1] && tkofLdg.get(i)[2]) {
                    tmp = "takeoffs and landings.";
                } else if (tkofLdg.get(i)[1]) {
                    tmp = "takeoffs.";
                } else {
                    tmp = "landings.";
                }
                stringBuilder.append("Runway ");
                stringBuilder.append(runways.get(i));
                stringBuilder.append(" will be active for ");
                stringBuilder.append(tmp);
                stringBuilder.append("\n");
            }
        }
        if (stringBuilder.length() == 0) {
            newRunwaysLabel.setText("All runways are closed");
            return;
        }
        newRunwaysLabel.setText(stringBuilder.toString());
    }

    //Change runways
    private void updateRunways() {
        airport.setRwyChangeTimer(-1);
        for (int i = 0; i < runways.size; i++) {
            airport.setActive(runways.get(i), tkofLdg.get(i)[2], tkofLdg.get(i)[1]);
        }
        airport.updateZoneStatus();
        airport.setPendingRwyChange(false);
        airport.setRwyChangeTimer(301);
    }

    public boolean containsLandingRunway(String icao, String rwy) {
        int index = runways.indexOf(rwy, false);
        return airport != null && icao.equals(airport.getIcao()) && index > -1 && tkofLdg.get(index)[0] && doubleCfm;
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

    private void updateLEMD(int windDir, int windSpd) {
        if (MaxTraffic.isNight("LEMD")) {
            //Night mode - 1 landing, 1 takeoff runway
            if (airport.getLandingRunways().get("32R") != null) {
                //32R, 36L active, change to 14L, 18L
                if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("14L").getHeading()) > -7 && windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("18L").getHeading()) > -7) {
                    runways.add("14L", "18L", "14R", "18R");
                    tkofLdg.add(TKOFF_ONLY, LDG_ONLY, ALL_INACTIVE, ALL_INACTIVE);
                    runways.add("36L", "36R", "32L", "32R");
                    tkofLdg.add(ALL_INACTIVE, ALL_INACTIVE, ALL_INACTIVE, ALL_INACTIVE);
                } else if (airport.getLandingRunways().containsKey("18R")) {
                    runways.add("14L", "18L", "14R", "18R");
                    tkofLdg.add(ALL_INACTIVE, ALL_INACTIVE, ALL_INACTIVE, ALL_INACTIVE);
                    runways.add("36L", "36R", "32L", "32R");
                    tkofLdg.add(TKOFF_ONLY, ALL_INACTIVE, ALL_INACTIVE, LDG_ONLY);
                }
            } else if (airport.getLandingRunways().get("18L") != null) {
                //14L, 18L active, change to 32R, 36L
                if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("32R").getHeading()) > -7 && windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("36L").getHeading()) > -7) {
                    runways.add("14L", "18L", "14R", "18R");
                    tkofLdg.add(ALL_INACTIVE, ALL_INACTIVE, ALL_INACTIVE, ALL_INACTIVE);
                    runways.add("36L", "36R", "32L", "32R");
                    tkofLdg.add(TKOFF_ONLY, ALL_INACTIVE, ALL_INACTIVE, LDG_ONLY);
                } else if (airport.getLandingRunways().containsKey("32L")) {
                    runways.add("14L", "18L", "14R", "18R");
                    tkofLdg.add(TKOFF_ONLY, LDG_ONLY, ALL_INACTIVE, ALL_INACTIVE);
                    runways.add("36L", "36R", "32L", "32R");
                    tkofLdg.add(ALL_INACTIVE, ALL_INACTIVE, ALL_INACTIVE, ALL_INACTIVE);
                }
            }
        } else {
            //Day mode - 2 landing, 2 takeoff runways
            if (airport.getLandingRunways().get("32R") != null) {
                //32s, 36s active, change to 14s, 18s
                if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("14L").getHeading()) > -7 && windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("18L").getHeading()) > -7) {
                    runways.add("14L", "18L", "14R", "18R");
                    tkofLdg.add(TKOFF_ONLY, LDG_ONLY, TKOFF_ONLY, LDG_ONLY);
                    runways.add("36L", "36R", "32L", "32R");
                    tkofLdg.add(ALL_INACTIVE, ALL_INACTIVE, ALL_INACTIVE, ALL_INACTIVE);
                }
            } else if (airport.getLandingRunways().get("18L") != null) {
                //14s, 18s active, change to 32s, 36s
                if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("32R").getHeading()) > -7 && windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("36L").getHeading()) > -7) {
                    runways.add("14L", "18L", "14R", "18R");
                    tkofLdg.add(ALL_INACTIVE, ALL_INACTIVE, ALL_INACTIVE, ALL_INACTIVE);
                    runways.add("36L", "36R", "32L", "32R");
                    tkofLdg.add(TKOFF_ONLY, TKOFF_ONLY, LDG_ONLY, LDG_ONLY);
                }
            }
        }
    }

    private void updateLFPG(int windDir, int windSpd) {
        if (airport.getLandingRunways().get("08R") != null) {
            //08s, 09s active, change to 26s, 27s
            if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("26L").getHeading()) > -5) {
                runways.add("26R", "26L", "27L", "27R");
                tkofLdg.add(TKOFF_ONLY, LDG_ONLY, TKOFF_ONLY, LDG_ONLY);
                runways.add("08L", "08R", "09L", "09R");
                tkofLdg.add(ALL_INACTIVE, ALL_INACTIVE, ALL_INACTIVE, ALL_INACTIVE);
            }
        } else if (airport.getLandingRunways().get("26L") != null) {
            //26s, 27s active, change to 08s, 09s
            if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("08R").getHeading()) > -5) {
                runways.add("26R", "26L", "27L", "27R");
                tkofLdg.add(ALL_INACTIVE, ALL_INACTIVE, ALL_INACTIVE, ALL_INACTIVE);
                runways.add("08L", "09R", "08R", "09L");
                tkofLdg.add(TKOFF_ONLY, TKOFF_ONLY, LDG_ONLY, LDG_ONLY);
            }
        }
    }

    private void updateLFPO(int windDir, int windSpd) {
        if (airport.getLandingRunways().get("25") != null) {
            //24, 25 are active, set to 06, 07
            if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("06").getHeading()) > -5) {
                runways.add("06", "07", "24", "25");
                tkofLdg.add(LDG_ONLY, TKOFF_ONLY, ALL_INACTIVE, ALL_INACTIVE);
            }
        } else {
            //06, 07 are active, set to 24, 25
            if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("25").getHeading()) > -5) {
                runways.add("24", "25", "06", "07");
                tkofLdg.add(TKOFF_ONLY, LDG_ONLY, ALL_INACTIVE, ALL_INACTIVE);
            }
        }
    }
}
