package com.bombbird.terminalcontrol.entities.trafficmanager;

import com.badlogic.gdx.math.MathUtils;
import com.bombbird.terminalcontrol.entities.Airport;
import com.bombbird.terminalcontrol.entities.Runway;

public class RunwayManager {
    private Airport airport;

    public RunwayManager(Airport airport) {
        this.airport = airport;
    }

    public void updateRunways(int windDir, int windSpd) {
        if ("RCTP".equals(airport.getIcao())) {
            updateRCTP(windDir, windSpd);
        } else if ("RCSS".equals(airport.getIcao())) {
            updateRCSS(windDir, windSpd);
        } else if ("WSSS".equals(airport.getIcao())) {
            updateWSSS(windDir, windSpd);
        } else if ("RJTT".equals(airport.getIcao())) {
            updateRJTT(windDir);
        }
    }

    private void updateRCTP(int windDir, int windSpd) {
        if (airport.getLandingRunways().size() == 0) {
            //If is new game, no runways set yet
            if (windDir == 0 || runwayActiveForWind(windDir, airport.getRunways().get("05L"))) {
                airport.setActive("05L", true, true);
                airport.setActive("05R", true, true);
            } else {
                airport.setActive("23L", true, true);
                airport.setActive("23R", true, true);
            }
        } else if (windDir != 0) {
            //Runways are in use, check if tailwind component exceeds limit of 5 knots
            if (airport.getLandingRunways().get("05L") != null) {
                //05s are active
                if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("05L").getHeading()) < -5) {
                    airport.setActive("23L", true, true);
                    airport.setActive("23R", true, true);
                    airport.setActive("05L", false, false);
                    airport.setActive("05R", false, false);
                }
            } else {
                //23s are active
                if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("23L").getHeading()) < -5) {
                    airport.setActive("05L", true, true);
                    airport.setActive("05R", true, true);
                    airport.setActive("23L", false, false);
                    airport.setActive("23R", false, false);
                }
            }
        }
    }

    private void updateRCSS(int windDir, int windSpd) {
        if (airport.getLandingRunways().size() == 0) {
            //If is new game, no runways set yet
            if (windDir == 0 || runwayActiveForWind(windDir, airport.getRunways().get("10"))) {
                airport.setActive("10", true, true);
            } else {
                airport.setActive("28", true, true);
            }
        } else if (windDir != 0) {
            //Runways are in use, check if tailwind component exceeds limit of 5 knots
            if (airport.getLandingRunways().get("10") != null) {
                //10 is active
                if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("10").getHeading()) < -5) {
                    airport.setActive("28", true, true);
                    airport.setActive("10", false, false);
                }
            } else {
                //28 is active
                if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("28").getHeading()) < -5) {
                    airport.setActive("10", true, true);
                    airport.setActive("28", false, false);
                }
            }
        }
    }

    private void updateWSSS(int windDir, int windSpd) {
        if (airport.getLandingRunways().size() == 0) {
            //If is new game, no runways set yet
            if (windDir == 0 || runwayActiveForWind(windDir, airport.getRunways().get("02L"))) {
                airport.setActive("02L", true, true);
                airport.setActive("02C", true, true);
            } else {
                airport.setActive("20C", true, true);
                airport.setActive("20R", true, true);
            }
        } else {
            //Runways are in use, check if tailwinds component exceeds limit of 5 knots
            if (airport.getLandingRunways().get("02L") != null) {
                //02s are active
                if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("02L").getHeading()) < -5) {
                    airport.setActive("20C", true, true);
                    airport.setActive("20R", true, true);
                    airport.setActive("02L", false, false);
                    airport.setActive("02C", false, false);
                }
            } else {
                //20s are active
                if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("20R").getHeading()) < -5) {
                    airport.setActive("02L", true, true);
                    airport.setActive("02C", true, true);
                    airport.setActive("20C", false, false);
                    airport.setActive("20R", false, false);
                }
            }
        }
    }

    private void updateRJTT(int windDir) {
        if (airport.getLandingRunways().size() == 0 && windDir == 0) {
            airport.setActive("34L", true, false);
            airport.setActive("34R", true, true);
            airport.setActive("05", false, true);
        } else {
            if (windDir > 283.5 && windDir <= 360 || windDir > 0 && windDir < 103.5) {
                airport.setActive("34L", true, false);
                airport.setActive("34R", true, true);
                airport.setActive("04", false, false);
                airport.setActive("05", false, true);
                airport.setActive("16L", false, false);
                airport.setActive("16R", false, false);
                airport.setActive("22", false, false);
                airport.setActive("23", false, false);
            } else {
                airport.setActive("34L", false, false);
                airport.setActive("34R", false, false);
                airport.setActive("04", false, false);
                airport.setActive("05", false, false);
                airport.setActive("16L", false, true);
                airport.setActive("16R", false, true);
                airport.setActive("22", true, false);
                airport.setActive("23", true, false);
            }
        }
    }

    private boolean runwayActiveForWind(int windHdg, Runway runway) {
        boolean active;
        int rightHeading = runway.getHeading() + 90;
        int leftHeading = runway.getHeading() - 90;
        if (rightHeading > 360) {
            rightHeading -= 360;
            active = !(windHdg >= rightHeading && windHdg < leftHeading);
        } else if (leftHeading < 0) {
            leftHeading += 360;
            active = !(windHdg >= rightHeading && windHdg < leftHeading);
        } else {
            active = (windHdg >= leftHeading && windHdg < rightHeading);
        }
        return active;
    }
}
