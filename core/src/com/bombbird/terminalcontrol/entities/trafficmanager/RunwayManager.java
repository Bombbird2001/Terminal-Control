package com.bombbird.terminalcontrol.entities.trafficmanager;

import com.badlogic.gdx.math.MathUtils;
import com.bombbird.terminalcontrol.entities.airports.Airport;
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
            updateRJTT(windDir, windSpd);
        } else if ("RJAA".equals(airport.getIcao())) {
            updateRJAA(windDir, windSpd);
        } else if ("RJBB".equals(airport.getIcao())) {
            updateRJBB(windDir, windSpd);
        } else if ("RJOO".equals(airport.getIcao())) {
            updateRJOO(windDir, windSpd);
        } else if ("RJBE".equals(airport.getIcao())) {
            updateRJBE(windDir, windSpd);
        } else if ("VHHH".equals(airport.getIcao())) {
            updateVHHH(windDir, windSpd);
        } else if ("VMMC".equals(airport.getIcao())) {
            updateVMMC(windDir, windSpd);
        }
    }

    /** Updates runway status for Taiwan Taoyuan */
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

    /** Updates runway status for Taipei Songshan */
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

    /** Updates runway status for Singapore Changi */
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

    /** Updates runway status for Tokyo Haneda */
    private void updateRJTT(int windDir, int windSpd) {
        if (airport.getLandingRunways().size() == 0 && windDir == 0) {
            airport.setActive("34L", true, false);
            airport.setActive("34R", true, true);
            airport.setActive("05", false, true);
        } else if (windDir != 0 && windSpd >= 7 || airport.getLandingRunways().size() == 0) {
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

    /** Updates runway status for Tokyo Narita */
    private void updateRJAA(int windDir, int windSpd) {
        if (airport.getLandingRunways().size() == 0) {
            //If is new game, no runways set yet
            if (windDir == 0 || runwayActiveForWind(windDir, airport.getRunways().get("34L"))) {
                airport.setActive("34L", true, true);
                airport.setActive("34R", true, true);
            } else {
                airport.setActive("16L", true, true);
                airport.setActive("16R", true, true);
            }
        } else if (windDir != 0) {
            //Runways are in use, check if tailwind component exceeds limit of 5 knots
            if (airport.getLandingRunways().get("34L") != null) {
                //34s are active
                if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("34L").getHeading()) < -5) {
                    airport.setActive("16L", true, true);
                    airport.setActive("16R", true, true);
                    airport.setActive("34L", false, false);
                    airport.setActive("34R", false, false);
                }
            } else {
                //16s are active
                if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("16L").getHeading()) < -5) {
                    airport.setActive("34L", true, true);
                    airport.setActive("34R", true, true);
                    airport.setActive("16L", false, false);
                    airport.setActive("16R", false, false);
                }
            }
        }
    }

    /** Updates runway status for Osaka Kansai */
    private void updateRJBB(int windDir, int windSpd) {
        if (airport.getLandingRunways().size() == 0) {
            //If is new game, no runways set yet
            if (windDir == 0 || runwayActiveForWind(windDir, airport.getRunways().get("06L"))) {
                airport.setActive("06L", true, true);
                airport.setActive("06R", true, true);
            } else {
                airport.setActive("24L", true, true);
                airport.setActive("24R", true, true);
            }
        } else if (windDir != 0) {
            //Runways are in use, check if tailwind component exceeds limit of 5 knots
            if (airport.getLandingRunways().get("24L") != null) {
                //24s are active
                if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("24L").getHeading()) < -5) {
                    airport.setActive("06L", true, true);
                    airport.setActive("06R", true, true);
                    airport.setActive("24L", false, false);
                    airport.setActive("24R", false, false);
                }
            } else {
                //06s are active
                if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("06L").getHeading()) < -5) {
                    airport.setActive("24L", true, true);
                    airport.setActive("24R", true, true);
                    airport.setActive("06L", false, false);
                    airport.setActive("06R", false, false);
                }
            }
        }
    }

    /** Updates runway status for Osaka Itami */
    private void updateRJOO(int windDir, int windSpd) {
        if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("32L").getHeading()) < -5) {
            airport.setActive("32L", false, false);
        } else {
            airport.setActive("32L", true, true);
        }
    }

    /** Updates runway status for Kobe */
    private void updateRJBE(int windDir, int windSpd) {
        if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("09").getHeading()) < -5) {
            airport.setActive("09", false, false);
        } else {
            airport.setActive("09", true, true);
        }
    }

    /** Updates runway status for Hong Kong*/
    private void updateVHHH(int windDir, int windSpd) {
        if (airport.getLandingRunways().size() == 0) {
            //If is new game, no runways set yet
            if (windDir == 0 || runwayActiveForWind(windDir, airport.getRunways().get("07L"))) {
                airport.setActive("07L", true, true);
                airport.setActive("07R", true, true);
            } else {
                airport.setActive("25L", true, true);
                airport.setActive("25R", true, true);
            }
        } else if (windDir != 0) {
            //Runways are in use, check if tailwind component exceeds limit of 5 knots
            if (airport.getLandingRunways().get("07L") != null) {
                //05s are active
                if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("07L").getHeading()) < -5) {
                    airport.setActive("25L", true, true);
                    airport.setActive("25R", true, true);
                    airport.setActive("07L", false, false);
                    airport.setActive("07R", false, false);
                }
            } else {
                //23s are active
                if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("25L").getHeading()) < -5) {
                    airport.setActive("07L", true, true);
                    airport.setActive("07R", true, true);
                    airport.setActive("25L", false, false);
                    airport.setActive("25R", false, false);
                }
            }
        }
    }

    /** Updates runway status for Macau */
    private void updateVMMC(int windDir, int windSpd) {
        if (airport.getLandingRunways().size() == 0) {
            //If is new game, no runways set yet
            if (windDir == 0 || runwayActiveForWind(windDir, airport.getRunways().get("16"))) {
                airport.setActive("16", true, true);
            } else {
                airport.setActive("34", true, true);
            }
        } else if (windDir != 0) {
            //Runways are in use, check if tailwind component exceeds limit of 5 knots
            if (airport.getLandingRunways().get("16") != null) {
                //10 is active
                if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("16").getHeading()) < -5) {
                    airport.setActive("34", true, true);
                    airport.setActive("16", false, false);
                }
            } else {
                //28 is active
                if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("34").getHeading()) < -5) {
                    airport.setActive("16", true, true);
                    airport.setActive("34", false, false);
                }
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
