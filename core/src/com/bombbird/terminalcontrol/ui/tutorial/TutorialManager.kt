package com.bombbird.terminalcontrol.ui.tutorial

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft
import com.bombbird.terminalcontrol.entities.aircrafts.NavState
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen
import com.bombbird.terminalcontrol.utilities.Fonts

class TutorialManager(private val radarScreen: RadarScreen) {
    var isPausedForReading = false
        private set
    var prompt1 = false
    var prompt2 = false
    var prompt3 = false
    var prompt4 = false
    var prompt5 = false
    var prompt6 = false
    private lateinit var group1: TutorialGroup
    private lateinit var group2: TutorialGroup
    private lateinit var group3: TutorialGroup
    private lateinit var group4: TutorialGroup
    private lateinit var group5: TutorialGroup
    private lateinit var climbGroup: TutorialGroup
    private lateinit var locGroup: TutorialGroup
    private lateinit var handoverGroup: TutorialGroup
    private lateinit var quitGroup: TutorialGroup
    private var deptContacted = false
    private var deptCleared = false
    private var arrContacted = false
    private var arrAltSet = false
    lateinit var scrollPane: ScrollPane
        private set
    private lateinit var scrollTable: Table
    private var blackFont = true

    /** Sets the initial transmissions  */
    fun init() {
        loadScrollPane()
        loadAllTutorialGroups()
        group1.isActive = true
    }

    /** Loads the resources for tutorial scrollPane  */
    private fun loadScrollPane() {
        scrollTable = Table()
        scrollPane = ScrollPane(scrollTable)
        scrollPane.x = TerminalControl.WIDTH.toFloat() / TerminalControl.HEIGHT * 3240 - 1750
        scrollPane.y = 3240 * 0.6f
        scrollPane.setSize(1600f, 3240 * 0.35f)
        scrollPane.style?.background = TerminalControl.skin.getDrawable("ListBackground")
        var inputListener: InputListener? = null
        for (eventListener in scrollPane.listeners ?: Array()) {
            if (eventListener is InputListener) {
                inputListener = eventListener
            }
        }
        if (inputListener != null) scrollPane.removeListener(inputListener)
        radarScreen.uiStage.addActor(scrollPane)
    }

    /** Adds new message to tutorial box  */
    fun tutorialMsg(msg: String) {
        val labelStyle = Label.LabelStyle()
        labelStyle.fontColor = Color.WHITE
        val finalMsg: String = if (blackFont) {
            "[BLACK]$msg"
        } else {
            msg
        }
        blackFont = !blackFont
        labelStyle.font = Fonts.defaultFont20
        val label = Label(finalMsg, labelStyle)
        label.width = scrollPane.width
        label.wrap = true
        scrollTable.add(label).width(scrollPane.width).pad(15f, 10f, 15f, 0f).actor.invalidate()
        scrollTable.row()
        scrollPane.layout()
        scrollPane.layout() //Needs to be called 2 times since label is wrapped
        scrollPane.scrollTo(0f, 0f, 0f, 0f)
    }

    /** Loads all the times, messages in each tutorial group  */
    private fun loadAllTutorialGroups() {
        //Group 1
        group1 = TutorialGroup(this)
        group1.addMessage(0, "Welcome to Terminal Control! You are an air traffic controller in the Terminal Control Area, managing arrivals and departures from 2 international airports in the area.")
        group1.addMessage(10, "Firstly, we have Haoyuan International Airport (TCTP). It has 2 runways: 05L-23R and 05R-23L.")
        group1.addTask(10, "spawnDeparture")
        group1.addMessage(20, "Next, we have Rongshan Airport (TCSS). It has 1 runway, 10-28.")
        group1.addMessage(30, "Look, we have an aircraft taking off from TCTP's runway 05L. We'll wait for it to become airborne and contact us.")

        //Group 2
        group2 = TutorialGroup(this)
        group2.addMessage(15, "The aircraft will continue climbing, and will eventually contact the area control centre for further climb.")
        group2.addMessage(20, "Seems like we have a new arrival inbound. Let's wait for it to contact us.")
        group2.addTask(20, "spawnArrival")
        group2.addMessage(25, "You can drag an aircraft's data tag around! Let's take a look at [YELLOW]EVA226's simplified data tag[], displayed when an aircraft is not under your control. The game will now pause.")
        group2.addTask(25, "pauseTutorial")
        group2.addMessage(35, "The 1st line is the aircraft's [YELLOW]callsign[] and its recat [YELLOW]wake category[]. The 2nd line has 2 numbers: firstly the aircraft's [YELLOW]altitude[], while the second is its [YELLOW]heading[]. For example, 232 23 means the aircraft is at about 23200 feet, flying a heading of 23.")
        group2.addMessage(55, "The 3rd line has only 1 number: the aircraft's [YELLOW]ground speed[]. 450 would mean the aircraft is travelling at a ground speed of 450 knots (450 nautical miles/hour).")
        group2.addMessage(70, "Let's go back to [YELLOW]CAL641 and look at the full data tag[]. Similarly, the 1st line contains the [YELLOW]callsign[], but behind it the [YELLOW]aircraft type[] is displayed, followed by the [YELLOW]wake turbulence category[] represented by a single letter, M, H or J, then its recat wake category.")
        group2.addMessage(80, "The 2nd line contains altitude information. The 1st number is the [YELLOW]altitude[], followed by a [YELLOW]^, v or =[] representing whether the aircraft is [YELLOW]climbing, descending or level[] respectively.")
        group2.addMessage(100, "Continuing with the 2nd line, the 2nd number is the aircraft's [YELLOW]\"target\" altitude[] which is the altitude the aircraft will fly taking into consideration [YELLOW]altitude restrictions from SIDs or STARs[]. The 3rd number is the [YELLOW]altitude that you have cleared it to[].")
        group2.addMessage(120, "Now the 3rd line displays lateral information. The 1st number is the aircraft's [YELLOW]heading[]. The 2nd number/text displays the aircraft's [YELLOW]cleared heading OR its cleared waypoint[]. The 3rd text displays the aircraft's [YELLOW]SID, STAR or cleared ILS[].")
        group2.addMessage(140, "Finally, the 4th line. The 1st number is the aircraft's [YELLOW]ground speed[], the 2nd is its [YELLOW]cleared airspeed[], and the 3rd text is its [YELLOW]departure or arrival airport[].")
        group2.addMessage(160, "You can [YELLOW]double tap[] the label of a controlled aircraft to change between the full and simplified labels.")
        group2.addMessage(165, "You can change the data tag to a more compact style in the game settings when you are more familiar with it.")
        group2.addMessage(170, "The game will now continue.")
        group2.addTask(170, "continueTutorial")

        //Group 3
        group3 = TutorialGroup(this)
        group3.addMessage(0, "All right, the aircraft will now descend via the STAR to the altitude. Similar to SIDs, Standard Terminal Arrival Routes (STARs) are predefined arrival routes into the airport with altitude and speed restrictions.")
        group3.addMessage(10, "You will probably notice that the aircraft doesn't go directly to 4000 feet, but 5000 instead. This is because the current STAR it is on, NTN1A, has an altitude restriction of 5000 feet for all waypoints.")
        group3.addMessage(25, "Other STARs may not have altitude restrictions, or have different restrictions for different waypoints. The aircraft will always descend to the lowest altitude allowed at a waypoint if cleared to an altitude that is lower than it.")
        group3.addMessage(40, "If you wish, you can override this restriction by going to the altitude tab and select the \"Unrestricted\" option. The aircraft will ignore altitude restrictions and descend directly to the altitude.")
        group3.addMessage(55, "If you do, you should ensure that the aircraft maintains separation from terrain and other aircraft; the SID/STAR altitude restrictions usually takes all this into account so it may be preferred to follow the restrictions.")
        group3.addMessage(70, "Since we have no planes in front, lets clear the arrival to waypoint HAMMY directly. Select the aircraft, go to the [YELLOW]lateral tab[], select [YELLOW]HAMMY[] from the drop down box and [YELLOW]transmit[].")
        group3.addTask(70, "setPrompt1")

        //Group 4
        group4 = TutorialGroup(this)
        group4.addMessage(10, "Now, we'll start setting the aircraft up for landing.")
        group4.addMessage(20, "To allow the plane to land, it needs to intercept the [YELLOW]instrument landing system (ILS)[] beams, represented by the [CYAN]cyan lines[] extending from the runways. For now the active runways at TCTP are [YELLOW]05L and 05R[].")
        group4.addMessage(35, "After intercepting the localizer (lateral component), the aircraft needs to follow the [YELLOW]glide slope (vertical component)[] of the ILS down to the runway. The glide slope path is usually [YELLOW]3 degrees[].")
        group4.addMessage(50, "To capture the glide slope, the aircraft needs to be low enough. The rings on the ILS line represents the altitude at each point on the ILS: 1st ring is 2000 feet, 2nd ring is 3000 feet and so on. For [GREEN]4000 feet and above, the rings are green[] instead of cyan. The aircraft should be [YELLOW]at or below[] that altitude when intercepting.")
        group4.addMessage(75, "Here at TCTP, the maximum altitude for intercepting is 4000 feet, hence there are 3 rings. Other airports may have higher maximum altitudes to intercept.")
        group4.addMessage(90, "To prepare the aircraft for intercepting the ILS, let's ask it to fly a heading after reaching a waypoint.")
        group4.addMessage(100, "Select the arrival, and go to the [YELLOW]lateral tab[]. Select [YELLOW]\"After wpt, hdg\"[]. After that, select [YELLOW]HAMMY[] in the 2nd dropdown box, and select [YELLOW]heading 90[] in the heading box.")
        group4.addTask(100, "setPrompt2")

        //Group 5
        group5 = TutorialGroup(this)
        group5.addMessage(5, "You may notice that the line showing heading 90 is not exactly towards the east on the screen. This is because there is a deviation between true heading and magnetic heading, and here that deviation is about 4 degrees west, which means a heading of 90 will give a track of about 86 degrees.")
        group5.addMessage(30, "Furthermore, you need to be mindful of the [YELLOW]wind direction and speed[] when giving heading clearances, since the wind affects the aircraft's track. When flying in heading mode, the aircraft will also [YELLOW]not follow any altitude restrictions[], hence you have to keep it separated from terrain.")
        group5.addMessage(55, "You can specify the direction the aircraft turns by selecting the turn left/right heading option. By default the fly heading option chooses the quickest direction to turn.")
        group5.addMessage(80, "You can also instruct an aircraft to enter a holding pattern if there are too many planes. There is NO NEED to do so now, but you can select the \"Hold\" option, and select the waypoint you want the aircraft to hold at. The aircraft will enter a holding pattern as shown once it reaches the waypoint.")
        group5.addMessage(105, "We will now wait for EVA226 to reach HAMMY. You can speed up the tutorial in the settings while waiting - tap the pause || button and select settings.")

        //SID climb group
        climbGroup = TutorialGroup(this)
        climbGroup.addMessage(10, "Standard Instrument Departures (SIDs) are predefined departure routes from the airport which may have altitude and speed restrictions. The aircraft will fly according to the route as shown when you select it.")
        climbGroup.addTask(10, "activateGroup2")

        //Localizer capture group
        locGroup = TutorialGroup(this)
        locGroup.addMessage(15, "When the airspace becomes busy, [YELLOW]parallel approaches can be conducted[] for parallel runways. However, you need to ensure planes are [YELLOW]separated by the standard 3 nautical miles or 1000 feet[] until they are both established on the different localizers.")
        locGroup.addMessage(40, "For aircraft [YELLOW]established on parallel ILS approaches[], you need to ensure a [YELLOW]staggered separation of 2nm[]. Some airports have an NTZ for parallel approaches and will not require this separation.")
        locGroup.addMessage(60, "For aircraft on the same ILS, the standard 3nm or 1000 feet separation applies, but once both aircraft are [YELLOW]less than 10nm from the runway[], separation can be reduced to [YELLOW]2.5nm or 1000 feet[].")
        locGroup.addMessage(80, "You can also give manual speed assignments to the aircraft if needed, but the aircraft will slow down automatically as it approaches the airport.")
        locGroup.addMessage(110, "Let's wait till EVA226 captures the ILS and is handed over to the tower.")

        //Tower handover group
        handoverGroup = TutorialGroup(this)
        handoverGroup.addMessage(20, "The airport can suffer from [YELLOW]congestion[] if you let in the arrivals too quickly, causing aircraft on the ground to be unable to take off. Hence, you will need to reduce the number of arrivals into the airport by [YELLOW]reducing their speed, putting them in holding patterns or closing the sector[]. When an airport is congested, landing an aircraft into it [YELLOW]will not score you any points[]!")

        //Quit group
        quitGroup = TutorialGroup(this)
        quitGroup.addMessage(15, "Quitting tutorial...")
        quitGroup.addTask(18, "quit")
    }

    /** Updates timers  */
    fun update() {
        if (radarScreen.aircrafts["CAL641"] != null) {
            val aircraft = radarScreen.aircrafts["CAL641"]!!
            if (!deptContacted && aircraft.controlState == Aircraft.ControlState.DEPARTURE) {
                //Aircraft has contacted
                deptContacted = true
                tutorialMsg("The departure has contacted us. Click on its label to select it, switch to the [YELLOW]altitude tab[] at the top of the panel, set the altitude from 3000 to [YELLOW]FL200[] and tap [YELLOW]transmit[].")
            }
            if (aircraft.clearedAltitude == 20000 && !deptCleared) {
                //Player has set altitude correctly
                tutorialMsg("Great job! The aircraft will now climb to FL200 via the " + aircraft.sidStar.name + " SID.")
                deptCleared = true
                climbGroup.isActive = true
            }
        }
        if (radarScreen.aircrafts.containsKey("EVA226")) {
            val aircraft = radarScreen.aircrafts["EVA226"]!!
            if (!arrContacted && aircraft.controlState == Aircraft.ControlState.ARRIVAL) {
                //Aircraft contacted
                arrContacted = true
                tutorialMsg("The arrival has contacted us. Click on its label, go to the [YELLOW]altitude tab[], set the altitude to [YELLOW]4000[] feet from FL150 and [YELLOW]transmit[].")
            }
            if (aircraft.clearedAltitude == 4000 && !arrAltSet) {
                //Correct altitude set
                arrAltSet = true
                group3.isActive = true
            }
            if (prompt1 && aircraft.direct != null && aircraft.direct == radarScreen.waypoints["HAMMY"]) {
                prompt1 = false
                tutorialMsg("Excellent, the aircraft will now fly directly to waypoint HAMMY.")
                group4.isActive = (true)
            }
            if (prompt2 && aircraft.navState.dispLatMode.first() == NavState.AFTER_WPT_HDG && aircraft.navState.clearedAftWpt.first() == radarScreen.waypoints["HAMMY"] && aircraft.navState.clearedAftWptHdg.first() == 90) {
                prompt2 = false
                tutorialMsg("Well done, the aircraft will automatically fly a heading of 90 after it reaches HAMMY.")
                prompt3 = true
                group5.isActive = (true)
            }
            if (prompt3 && aircraft.navState.dispLatMode.first() == NavState.VECTORS && aircraft.clearedHeading == 90) {
                prompt3 = false
                tutorialMsg("Ok, the aircraft has reached HAMMY and will now fly a heading of 90. Now, select the aircraft, go to the [YELLOW]lateral tab[], tap \"Approach\" and select [YELLOW]ILS05L[].")
                prompt4 = true
            }
            if (prompt4 && aircraft.airport.approaches["05L"] == aircraft.navState.clearedApch.first()) {
                prompt4 = false
                tutorialMsg("Great job, the aircraft will now automatically capture the localizer and glide slope. Once the plane is close to landing it will contact the tower, but you can also hand it over to the tower once it is established on the localizer by pressing the [YELLOW]\"Handover\"[] button.")
                prompt5 = true
                locGroup.isActive = (true)
            }
            if (prompt5 && aircraft.controlState == Aircraft.ControlState.UNCONTROLLED) {
                prompt5 = false
                tutorialMsg("Alright! The aircraft has been handed off to the tower controller. [YELLOW]For every departure that gets handed over, or for every arrival that lands, you receive 1 point[] to add to your score. But [YELLOW]for every separation infringement, you will lose 5% of your points, and 1 point for every 5 seconds[] the incident continues. Be careful!")
                handoverGroup.isActive = (true)
                prompt6 = true
            }
        } else if (prompt6) {
            prompt6 = false
            tutorialMsg("Congratulations, you have just handled an arrival and a departure! Are you ready to handle more planes? If so, go ahead and start a new game! But feel free to revisit the tutorial again if you wish. The help manual on the main menu also contains more detailed information regarding game mechanics and airports, do refer to it. All the best!")
            quitGroup.isActive = (true)
        }
        group1.update()
        group2.update()
        group3.update()
        group4.update()
        group5.update()
        climbGroup.update()
        locGroup.update()
        handoverGroup.update()
        quitGroup.update()
    }

    fun activateGroup2() {
        group2.isActive = true
    }

    fun setPause(pause: Boolean) {
        isPausedForReading = pause
        if (pause) radarScreen.setSelectedAircraft(null)
    }
}