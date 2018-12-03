package com.bombbird.terminalcontrol.utilities;

import com.badlogic.gdx.utils.Timer;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.Airport;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival;
import com.bombbird.terminalcontrol.screens.MainMenuScreen;
import com.bombbird.terminalcontrol.screens.RadarScreen;

public class TutorialManager {
    private Timer timer;

    private RadarScreen radarScreen;

    private boolean prompt1 = false;
    private boolean prompt2 = false;
    private boolean prompt3 = false;
    private boolean prompt4 = false;
    private boolean prompt5 = false;
    private boolean prompt6 = false;

    private boolean deptContacted = false;
    private boolean deptClrd = false;
    private boolean arrContacted = false;
    private boolean arrAltSet = false;

    public TutorialManager(RadarScreen radarScreen) {
        this.radarScreen = radarScreen;
        timer = new Timer();
    }

    /** Sets the initial transmissions */
    public void init() {
        radarScreen.getCommBox().tutorialMsg("Welcome to Terminal Control! You are an air traffic controller in the Taipei Terminal Control Area, managing arrivals and departures from Taipei's 2 international airports.");

        timer.scheduleTask(new Timer.Task() {
            @Override
            public void run() {
                Airport airport = radarScreen.airports.get("RCTP");
                radarScreen.newDeparture("CAL641", "A359", airport, airport.getRunways().get("05L"));

                radarScreen.getCommBox().tutorialMsg("Firstly, we have Taoyuan International Airport (RCTP), Taiwan's main international airport, which handled almost 45 million passengers in 2017. It has 2 runways: 05L-23R and 05R-23L.");
            }
        }, 10);

        timer.scheduleTask(new Timer.Task() {
            @Override
            public void run() {
                radarScreen.getCommBox().tutorialMsg("Next, we have Taipei Songshan Airport (RCSS), located in the heart of downtown Taipei. It handled more than 6 million passengers in 2017. It has 1 runway, 10-28.");
            }
        }, 20);

        timer.scheduleTask(new Timer.Task() {
            @Override
            public void run() {
                radarScreen.getCommBox().tutorialMsg("Look, we have an aircraft taking off from RCTP's runway 05L. We'll wait for it to become airborne and contact us.");
            }
        }, 30);
    }

    /** Initialises the 2nd part of the tutorial */
    private void initPart2() {
        radarScreen.getCommBox().tutorialMsg("The aircraft will continue climbing, and will eventually contact the area control centre for further climb.");

        timer.scheduleTask(new Timer.Task() {
            @Override
            public void run() {
                radarScreen.aircrafts.put("EVA226", new Arrival("EVA226", "B77W", radarScreen.airports.get("RCTP")));
                radarScreen.setArrivals(radarScreen.getArrivals() + 1);
                radarScreen.getCommBox().tutorialMsg("Seems like we have a new arrival inbound. Let's wait for it to contact us.");
            }
        }, 5);
    }

    /** Initialises the 3rd part of the tutorial */
    private void initPart3() {
        radarScreen.getCommBox().tutorialMsg("All right, the aircraft will now descend via the STAR to the altitude. Similar to SIDs, STARs are predefined arrival routes into the airport with altitude and speed restrictions.");
        timer.scheduleTask(new Timer.Task() {
            @Override
            public void run() {
                radarScreen.getCommBox().tutorialMsg("You'll probably notice that the aircraft doesn't go directly to 4000 feet, but 5000 instead. This is because the current STAR it is on, TNN1A, has an altitude restriction of 5000 feet for all waypoints.");
            }
        }, 10);

        timer.scheduleTask(new Timer.Task() {
            @Override
            public void run() {
                radarScreen.getCommBox().tutorialMsg("Other STARs may not have altitude restrictions, or have different restrictions for different waypoints. The aircraft will always descend to the lowest altitude allowed at a waypoint if cleared to an altitude that is lower than it.");
            }
        }, 25);

        timer.scheduleTask(new Timer.Task() {
            @Override
            public void run() {
                radarScreen.getCommBox().tutorialMsg("If you wish, you can override this restriction by going to the altitude tab and select the climb/descend option. The aircraft will ignore altitude restrictions and descend directly to the altitude.");
            }
        }, 35);

        timer.scheduleTask(new Timer.Task() {
            @Override
            public void run() {
                radarScreen.getCommBox().tutorialMsg("If you do, you should ensure that the aircraft maintains separation from terrain and other aircraft; the SID/STAR altitude restrictions usually takes all this into account so it may be preferred to follow the restrictions");
            }
        }, 45);

        timer.scheduleTask(new Timer.Task() {
            @Override
            public void run() {
                radarScreen.getCommBox().tutorialMsg("Since we have no aircrafts in front, lets clear the arrival to waypoint JAMMY directly. Select the aircraft, go to the lateral tab, select JAMMY from the drop down box and transmit.");
                prompt1 = true;
            }
        }, 60);
    }

    /** Initialises part 4 of tutorial */
    private void initPart4() {
        timer.scheduleTask(new Timer.Task() {
            @Override
            public void run() {
                radarScreen.getCommBox().tutorialMsg("Now, we'll start setting the aircraft up for landing.");
            }
        }, 10);

        timer.scheduleTask(new Timer.Task() {
            @Override
            public void run() {
                radarScreen.getCommBox().tutorialMsg("To allow the plane to land, it needs to intercept the instrument landing system (ILS) beams, represented by the cyan lines extending from the runways. For now the active runways at RCTP are 05L and 05R, so you can use any runway.");
            }
        }, 20);

        timer.scheduleTask(new Timer.Task() {
            @Override
            public void run() {
                radarScreen.getCommBox().tutorialMsg("After intercepting the localizer (lateral component), the aircraft needs to follow the glide slope (vertical component) of the ILS down to the runway. The glide slope path is usually 3 degrees.");
            }
        }, 35);

        timer.scheduleTask(new Timer.Task() {
            @Override
            public void run() {
                radarScreen.getCommBox().tutorialMsg("To capture the glide slope, the aircraft needs to be low enough. The rings on the ILS line represents the altitude at each point on the ILS: 1st ring is 2000 feet, 2nd ring is 3000 feet and so on. The aircraft should be at or below that altitude when intercepting.");
            }
        }, 45);

        timer.scheduleTask(new Timer.Task() {
            @Override
            public void run() {
                radarScreen.getCommBox().tutorialMsg("Here at RCTP, the maximum altitude for intercepting is 4000 feet, hence there are 3 rings. Other airports may have higher maximum altitudes to intercept.");
            }
        }, 60);

        timer.scheduleTask(new Timer.Task() {
            @Override
            public void run() {
                radarScreen.getCommBox().tutorialMsg("To prepare the aircraft for intercepting the ILS, let's ask it to fly a heading after reaching a waypoint.");
            }
        }, 70);

        timer.scheduleTask(new Timer.Task() {
            @Override
            public void run() {
                radarScreen.getCommBox().tutorialMsg("Select the arrival, and go to the lateral tab. At the 1st dropdown box, select \"After waypoint, fly heading\". After that, select JAMMY in the 2nd dropdown box, and select heading 90 in the heading box.");
                prompt2 = true;
            }
        }, 75);
    }

    /** Initialises part 5 of tutorial */
    private void initPart5() {
        timer.scheduleTask(new Timer.Task() {
            @Override
            public void run() {
                radarScreen.getCommBox().tutorialMsg("You may notice that the line showing heading 90 is not exactly towards the east on the screen. This is because there is a deviation between true heading and magnetic heading, and here in Taipei that deviation is about 4 degrees west, which means a heading of 90 will give a track of about 86 degrees.");
            }
        }, 5);

        timer.scheduleTask(new Timer.Task() {
            @Override
            public void run() {
                radarScreen.getCommBox().tutorialMsg("Furthermore, you need to be mindful of the wind direction and speed when giving heading clearances, since the wind affects the aircraft's track. When flying in heading mode, the aircraft will also not follow any altitude restrictions, hence you have to keep it separated from terrain.");
            }
        }, 25);

        timer.scheduleTask(new Timer.Task() {
            @Override
            public void run() {
                radarScreen.getCommBox().tutorialMsg("You can specify the direction the aircraft turns by selecting the turn left/right heading option. By default the fly heading option chooses the quickest direction to turn.");
            }
        }, 40);

        timer.scheduleTask(new Timer.Task() {
            @Override
            public void run() {
                radarScreen.getCommBox().tutorialMsg("You can also instruct aircrafts to enter a holding pattern if there are too many aircrafts. Select the \"Hold at\" option, and select the waypoint you want the aircraft to hold at. The aircraft will enter a holding pattern as published once it reaches the waypoint.");
            }
        }, 50);
    }

    /** Updates timers */
    public void update() {
        if (radarScreen.aircrafts.get("CAL641") != null) {
            Aircraft aircraft = radarScreen.aircrafts.get("CAL641");
            if (!deptContacted && aircraft.getControlState() == 2) {
                //Aircraft has contacted
                deptContacted = true;
                radarScreen.getCommBox().tutorialMsg("The aircraft has contacted us. Click on its label to select it, switch to the altitude tab at the top of the panel, set the altitude from 3000 to FL200 and tap transmit.");
            }

            if (aircraft.getClearedAltitude() == 20000 && !deptClrd) {
                //Player has set altitude correctly
                radarScreen.getCommBox().tutorialMsg("Great job! The aircraft will now climb to FL200 via the " + aircraft.getSidStar().getName() + " SID.");
                deptClrd = true;

                timer.scheduleTask(new Timer.Task() {
                    @Override
                    public void run() {
                        radarScreen.getCommBox().tutorialMsg("SIDs are predefined departure routes from the airport which may have altitude and speed restrictions. The aircraft will fly according to the route as shown when you select it.");
                    }
                }, 10);

                timer.scheduleTask(new Timer.Task() {
                    @Override
                    public void run() {
                        initPart2();
                    }
                }, 25);
            }
        }

        if (radarScreen.aircrafts.get("EVA226") != null) {
            Aircraft aircraft = radarScreen.aircrafts.get("EVA226");
            if (!arrContacted && aircraft.getControlState() == 1) {
                //Aircraft contacted
                arrContacted = true;
                radarScreen.getCommBox().tutorialMsg("The aircraft has contacted us. Click on its label, go to the altitude tab, set the altitude to 4000 feet from FL150 and transmit.");
            }

            if (aircraft.getClearedAltitude() == 4000 && !arrAltSet) {
                //Correct altitude set
                arrAltSet = true;
                initPart3();
            }

            if (prompt1 && aircraft.getDirect().equals(radarScreen.waypoints.get("JAMMY"))) {
                prompt1 = false;
                radarScreen.getCommBox().tutorialMsg("Excellent, the aircraft will now fly directly to waypoint JAMMY.");
                initPart4();
            }

            if (prompt2 && "After waypoint, fly heading".equals(aircraft.getNavState().getDispLatMode().first()) && aircraft.getNavState().getClearedAftWpt().first().equals(radarScreen.waypoints.get("JAMMY")) && aircraft.getNavState().getClearedAftWptHdg().first() == 90) {
                prompt2 = false;
                radarScreen.getCommBox().tutorialMsg("Well done, the aircraft will automatically fly a heading of 90 after it reaches JAMMY.");
                prompt3 = true;
                initPart5();
            }

            if (prompt3 && "Fly heading".equals(aircraft.getNavState().getDispLatMode().first()) && aircraft.getClearedHeading() == 90) {
                prompt3 = false;
                radarScreen.getCommBox().tutorialMsg("Ok, the aircraft has reached JAMMY and will now fly a heading of 90. Now, select the aircraft, go to the lateral tab, and in the 2nd dropdown box, select ILS05L to replace \"Not cleared approach\".");
                prompt4 = true;
            }

            if (prompt4 && aircraft.getAirport().getApproaches().get("05L").equals(aircraft.getNavState().getClearedIls().last())) {
                prompt4 = false;
                radarScreen.getCommBox().tutorialMsg("Great job, the aircraft will now automatically capture the localizer and glide slope. Now we just need to wait to hand the aircraft over to the tower.");
                prompt5 = true;

                timer.scheduleTask(new Timer.Task() {
                    @Override
                    public void run() {
                        radarScreen.getCommBox().tutorialMsg("When the airspace becomes busy, parallel approaches can be conducted for parallel runways. However, you need to ensure the aircrafts maintain separated by the standard 3 nautical miles or 1000 feet until they are both established on the different localizers.");
                    }
                }, 10);

                timer.scheduleTask(new Timer.Task() {
                    @Override
                    public void run() {
                        radarScreen.getCommBox().tutorialMsg("For aircrafts on the same ILS, the standard 3nm or 1000 feet separation applies, but once both aircraft are less than 10nm from the runway, separation can be reduced to 2.5nm or 1000 feet.");
                    }
                }, 30);

                timer.scheduleTask(new Timer.Task() {
                    @Override
                    public void run() {
                        radarScreen.getCommBox().tutorialMsg("You can give manual speed assignments to the aircraft if needed, but the aircraft will slow down automatically as it approaches the airport.");
                    }
                }, 50);
            }

            if (prompt5 && aircraft.getControlState() == 0) {
                prompt5 = false;
                radarScreen.getCommBox().tutorialMsg("Alright! The aircraft has been handed off to the tower controller. For every departure that gets handed over, or for every arrival that lands, you receive 1 point to add to your score. But for every separation infringement, your score will be halved. Be careful!");
                prompt6 = true;
            }
        } else if (prompt6) {
            prompt6 = false;
            radarScreen.getCommBox().tutorialMsg("Congratulations, you have just handled an arrival and a departure! Are you ready to handle more aircrafts? If so, go ahead and start a new game! But feel free to revisit the tutorial again if you wish. All the best!");

            timer.scheduleTask(new Timer.Task() {
                @Override
                public void run() {
                    TerminalControl.radarScreen = null;
                    radarScreen.game.setScreen(new MainMenuScreen(radarScreen.game));
                }
            }, 10);
        }
    }
}
