package com.bombbird.terminalcontrol.entities.trafficmanager;

import com.badlogic.gdx.math.MathUtils;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.airports.Airport;
import com.bombbird.terminalcontrol.entities.Runway;

public class RunwayManager {
    private Airport airport;

    public RunwayManager(Airport airport) {
        this.airport = airport;
    }

    public void updateRunways(int windDir, int windSpd) {
        boolean pendingChange = false;
        if ("RCTP".equals(airport.getIcao())) {
            pendingChange = updateRCTP(windDir, windSpd);
        } else if ("RCSS".equals(airport.getIcao())) {
            pendingChange = updateRCSS(windDir, windSpd);
        } else if ("WSSS".equals(airport.getIcao())) {
            pendingChange = updateWSSS(windDir, windSpd);
        } else if ("RJTT".equals(airport.getIcao())) {
            pendingChange = updateRJTT(windDir, windSpd);
        } else if ("RJAA".equals(airport.getIcao())) {
            pendingChange = updateRJAA(windDir, windSpd);
        } else if ("RJBB".equals(airport.getIcao())) {
            pendingChange = updateRJBB(windDir, windSpd);
        } else if ("RJOO".equals(airport.getIcao())) {
            pendingChange = updateRJOO(windDir, windSpd);
        } else if ("RJBE".equals(airport.getIcao())) {
            pendingChange = updateRJBE(windDir, windSpd);
        } else if ("VHHH".equals(airport.getIcao())) {
            pendingChange = updateVHHH(windDir, windSpd);
        } else if ("VMMC".equals(airport.getIcao())) {
            pendingChange = updateVMMC(windDir, windSpd);
        } else if ("VTBD".equals(airport.getIcao())) {
            pendingChange = updateVTBD(windDir, windSpd);
        } else if ("VTBS".equals(airport.getIcao())) {
            pendingChange = updateVTBS(windDir, windSpd);
        } else if ("LEMD".equals(airport.getIcao())) {
            pendingChange = updateLEMD(windDir, windSpd);
        }

        if (pendingChange && !airport.isPendingRwyChange()) {
            airport.setPendingRwyChange(true);
            airport.setRwyChangeTimer(300);
            TerminalControl.radarScreen.getCommBox().alertMsg("Runway change will occur for " + airport.getIcao() + " soon due to change in winds. Tap the METAR label of " + airport.getIcao() + " for more information.");
        } else if (!pendingChange && airport.isPendingRwyChange()) {
            airport.setPendingRwyChange(false);
            airport.setRwyChangeTimer(301);
        }
    }

    /** Updates runway status for Taiwan Taoyuan */
    private boolean updateRCTP(int windDir, int windSpd) {
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
                    return true;
                }
            } else {
                //23s are active
                if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("23L").getHeading()) < -5) {
                    airport.setActive("05L", true, true);
                    airport.setActive("05R", true, true);
                    airport.setActive("23L", false, false);
                    airport.setActive("23R", false, false);
                    return true;
                }
            }
        }
        return false;
    }

    /** Updates runway status for Taipei Songshan */
    private boolean updateRCSS(int windDir, int windSpd) {
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
                    return true;
                }
            } else {
                //28 is active
                if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("28").getHeading()) < -5) {
                    airport.setActive("10", true, true);
                    airport.setActive("28", false, false);
                    return true;
                }
            }
        }
        return false;
    }

    /** Updates runway status for Singapore Changi */
    private boolean updateWSSS(int windDir, int windSpd) {
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
                    return true;
                }
            } else {
                //20s are active
                if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("20R").getHeading()) < -5) {
                    airport.setActive("02L", true, true);
                    airport.setActive("02C", true, true);
                    airport.setActive("20C", false, false);
                    airport.setActive("20R", false, false);
                    return true;
                }
            }
        }
        return false;
    }

    /** Updates runway status for Tokyo Haneda */
    private boolean updateRJTT(int windDir, int windSpd) {
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
            return true;
        }
        return false;
    }

    /** Updates runway status for Tokyo Narita */
    private boolean updateRJAA(int windDir, int windSpd) {
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
                    return true;
                }
            } else {
                //16s are active
                if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("16L").getHeading()) < -5) {
                    airport.setActive("34L", true, true);
                    airport.setActive("34R", true, true);
                    airport.setActive("16L", false, false);
                    airport.setActive("16R", false, false);
                    return true;
                }
            }
        }
        return false;
    }

    /** Updates runway status for Osaka Kansai */
    private boolean updateRJBB(int windDir, int windSpd) {
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
                    return true;
                }
            } else {
                //06s are active
                if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("06L").getHeading()) < -5) {
                    airport.setActive("24L", true, true);
                    airport.setActive("24R", true, true);
                    airport.setActive("06L", false, false);
                    airport.setActive("06R", false, false);
                    return true;
                }
            }
        }
        return false;
    }

    /** Updates runway status for Osaka Itami */
    private boolean updateRJOO(int windDir, int windSpd) {
        if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("32L").getHeading()) < -5) {
            airport.setActive("32L", false, false);
            return true;
        } else {
            airport.setActive("32L", true, true);
            return false;
        }
    }

    /** Updates runway status for Kobe */
    private boolean updateRJBE(int windDir, int windSpd) {
        if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("09").getHeading()) < -5) {
            airport.setActive("09", false, false);
            return true;
        } else {
            airport.setActive("09", true, true);
            return false;
        }
    }

    /** Updates runway status for Hong Kong*/
    private boolean updateVHHH(int windDir, int windSpd) {
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
                    return true;
                }
            } else {
                //23s are active
                if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("25L").getHeading()) < -5) {
                    airport.setActive("07L", true, true);
                    airport.setActive("07R", true, true);
                    airport.setActive("25L", false, false);
                    airport.setActive("25R", false, false);
                    return true;
                }
            }
        }
        return false;
    }

    /** Updates runway status for Macau */
    private boolean updateVMMC(int windDir, int windSpd) {
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
                    return true;
                }
            } else {
                //28 is active
                if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("34").getHeading()) < -5) {
                    airport.setActive("16", true, true);
                    airport.setActive("34", false, false);
                    return true;
                }
            }
        }
        return false;
    }

    /** Updates runway status for Bangkok Don Mueang */
    private boolean updateVTBD(int windDir, int windSpd) {
        if (airport.getLandingRunways().size() == 0) {
            //If is new game, no runways set yet
            if (windDir == 0 || runwayActiveForWind(windDir, airport.getRunways().get("21R"))) {
                airport.setActive("21L", false, true);
                airport.setActive("21R", true, false);
            } else {
                airport.setActive("03L", true, false);
                airport.setActive("03R", false, true);
            }
        } else if (windDir != 0) {
            //Runways are in use, check if tailwind component exceeds limit of 5 knots
            if (airport.getLandingRunways().get("03L") != null) {
                //03s are active
                if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("03L").getHeading()) < -5) {
                    airport.setActive("21L", false, true);
                    airport.setActive("21R", true, false);
                    airport.setActive("03L", false, false);
                    airport.setActive("03R", false, false);
                    return true;
                }
            } else {
                //21s are active
                if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("21L").getHeading()) < -5) {
                    airport.setActive("03L", true, false);
                    airport.setActive("03R", false, true);
                    airport.setActive("21L", false, false);
                    airport.setActive("21R", false, false);
                    return true;
                }
            }
        }
        return false;
    }

    /** Updates runway status for Bangkok Suvarnabhumi */
    private boolean updateVTBS(int windDir, int windSpd) {
        if (airport.getLandingRunways().size() == 0) {
            //If is new game, no runways set yet
            if (windDir == 0 || runwayActiveForWind(windDir, airport.getRunways().get("19L"))) {
                airport.setActive("19L", true, true);
                airport.setActive("19R", true, true);
            } else {
                airport.setActive("01L", true, true);
                airport.setActive("01R", true, true);
            }
        } else if (windDir != 0) {
            //Runways are in use, check if tailwind component exceeds limit of 5 knots
            if (airport.getLandingRunways().get("01L") != null) {
                //01s are active
                if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("01L").getHeading()) < -5) {
                    airport.setActive("19L", true, true);
                    airport.setActive("19R", true, true);
                    airport.setActive("01L", false, false);
                    airport.setActive("01R", false, false);
                    return true;
                }
            } else {
                //19s are active
                if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("19L").getHeading()) < -5) {
                    airport.setActive("01L", true, true);
                    airport.setActive("01R", true, true);
                    airport.setActive("19L", false, false);
                    airport.setActive("19R", false, false);
                    return true;
                }
            }
        }
        return false;
    }

    /** Updates runway status for Madrid Barajas */
    private boolean updateLEMD(int windDir, int windSpd) {
        if (MaxTraffic.isNight("LEMD")) {
            //Night mode witb only 1 landing runway, 1 departure runway
            if (airport.getLandingRunways().size() == 0) {
                //If is new game, no runways set yet
                if (windDir == 0 || runwayActiveForWind(windDir, airport.getRunways().get("36L"))) {
                    airport.setActive("36L", false, true);
                    airport.setActive("32R", true, false);
                } else {
                    airport.setActive("18L", true, false);
                    airport.setActive("14L", false, true);
                }
            } else if (windDir != 0) {
                //Runways are in use, check if tailwind component exceeds limit of 7 knots
                if (airport.getLandingRunways().get("32R") != null) {
                    //32R, 36L are active
                    if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("32R").getHeading()) < -7 || windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("36L").getHeading()) < -7) {
                        airport.setActive("18L", true, false);
                        airport.setActive("18R", false, false);
                        airport.setActive("14L", false, true);
                        airport.setActive("14R", false, false);
                        airport.setActive("36L", false, false);
                        airport.setActive("36R", false, false);
                        airport.setActive("32L", false, false);
                        airport.setActive("32R", false, false);
                        return true;
                    } else if (airport.getLandingRunways().containsKey("32L")) {
                        //Change 4 runways to 2 runways
                        airport.setActive("36L", false, true);
                        airport.setActive("36R", false, false);
                        airport.setActive("32L", false, false);
                        airport.setActive("32R", true, false);
                        airport.setActive("18L", false, false);
                        airport.setActive("18R", false, false);
                        airport.setActive("14L", false, false);
                        airport.setActive("14R", false, false);
                        return true;
                    }
                } else {
                    //14L, 18L are active
                    if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("14L").getHeading()) < -7 || windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("18L").getHeading()) < -7) {
                        airport.setActive("36L", false, true);
                        airport.setActive("36R", false, false);
                        airport.setActive("32L", false, false);
                        airport.setActive("32R", true, false);
                        airport.setActive("18L", false, false);
                        airport.setActive("18R", false, false);
                        airport.setActive("14L", false, false);
                        airport.setActive("14R", false, false);
                        return true;
                    } else if (airport.getLandingRunways().containsKey("18R")) {
                        //Change 4 runways to 2 runways
                        airport.setActive("18L", true, false);
                        airport.setActive("18R", false, false);
                        airport.setActive("14L", false, true);
                        airport.setActive("14R", false, false);
                        airport.setActive("36L", false, false);
                        airport.setActive("36R", false, false);
                        airport.setActive("32L", false, false);
                        airport.setActive("32R", false, false);
                        return true;
                    }
                }
            }
        } else {
            if (airport.getLandingRunways().size() == 0) {
                //If is new game, no runways set yet
                if (windDir == 0 || runwayActiveForWind(windDir, airport.getRunways().get("36L"))) {
                    airport.setActive("36L", false, true);
                    airport.setActive("36R", false, true);
                    airport.setActive("32L", true, false);
                    airport.setActive("32R", true, false);
                } else {
                    airport.setActive("18L", true, false);
                    airport.setActive("18R", true, false);
                    airport.setActive("14L", false, true);
                    airport.setActive("14R", false, true);
                }
            } else if (windDir != 0) {
                //Runways are in use, check if tailwind component exceeds limit of 7 knots
                if (airport.getLandingRunways().get("32R") != null) {
                    //32s, 36s are active
                    if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("32L").getHeading()) < -7 || windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("36L").getHeading()) < -7) {
                        airport.setActive("18L", true, false);
                        airport.setActive("18R", true, false);
                        airport.setActive("14L", false, true);
                        airport.setActive("14R", false, true);
                        airport.setActive("36L", false, false);
                        airport.setActive("36R", false, false);
                        airport.setActive("32L", false, false);
                        airport.setActive("32R", false, false);
                        return true;
                    }
                } else {
                    //14s, 18s are active
                    if (windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("14L").getHeading()) < -7 || windSpd * MathUtils.cosDeg(windDir - airport.getRunways().get("18L").getHeading()) < -7) {
                        airport.setActive("36L", false, true);
                        airport.setActive("36R", false, true);
                        airport.setActive("32L", true, false);
                        airport.setActive("32R", true, false);
                        airport.setActive("18L", false, false);
                        airport.setActive("18R", false, false);
                        airport.setActive("14L", false, false);
                        airport.setActive("14R", false, false);
                        return true;
                    }
                }
            }
        }
        return false;
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
