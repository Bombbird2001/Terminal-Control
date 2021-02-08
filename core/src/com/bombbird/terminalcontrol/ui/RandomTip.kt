package com.bombbird.terminalcontrol.ui

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl

object RandomTip {
    private val tips = Array<String>()

    /** Loads a list of tips to be shown during loading screen  */

    fun loadTips() {
        tips.add("Double tap the label of a controlled aircraft to minimise/maximise it")
        tips.add("Separation is reduced to 2.5nm for aircraft on the same ILS both within 10nm from touchdown")
        tips.add("Staggered separation of 2nm is required for planes on approach to parallel runways (without an NTZ)")
        tips.add("Put planes in holding patterns if the airspace is becoming congested")
        tips.add("Be careful of the MVAs (gray) when assigning headings manually")
        tips.add("MVAs (gray) do not apply to aircraft following a SID/STAR, unless it deviates significantly from the path or is below the minimum waypoint altitude")
        tips.add("Restricted areas (orange) must be avoided by all aircraft")
        tips.add("Windshear can cause an aircraft to go around")
        tips.add("Circles on the LDA indicate the step-down points for a non-precision approach")
        tips.add("Some waypoints are fly-over points; planes will fly directly over it before turning")
        tips.add("Some aircraft climb/descend slower than others")
        tips.add("An aircraft will go around if it is too high or too fast")
        tips.add("An aircraft will go around if there is another aircraft on the runway")
        tips.add("A minimum separation of 3nm or 1000 feet must be kept between all aircraft (except in some cases)")
        tips.add("An aircraft will divert if it is low on fuel")
        tips.add("Fly-over waypoints are indicated by a filled circle")
        tips.add("Tap on the METAR label of an airport (top left of screen) to change its runway configuration")
        tips.add("Wake turbulence can cause an aircraft to go around")
        tips.add("Check out the help manual for more detailed descriptions of airports, game mechanics")
        tips.add("ILS display style can be changed in Settings => Display")
        tips.add("Aircraft data tag display style can be changed in Settings => Datatag")
        tips.add("Screen too cluttered? MVA sector altitudes can be set to hidden in Settings => Display")
        tips.add("Airspace too congested? Go to Settings => Traffic => Arrival traffic settings to adjust the traffic")
        tips.add("Colour scheme can be changed in Settings => Display")
        tips.add("Choose whether to display remaining distance to each waypoint on aircraft routes, in Settings => Display")
        tips.add("Measure distance on the radar screen by ${if (Gdx.app.type == Application.ApplicationType.Android) "changing to \"Dist mode\" and tapping with 2 fingers" else "dragging with the right mouse button"}")
        tips.add("Some airports have a night mode with different procedures from day operations. You can change these settings under Settings => Traffic")
        if (Gdx.app.type == Application.ApplicationType.Android) tips.add("Like this game? Please rate it on the Google Play Store!")
    }

    /** Checks if the tips array has been loaded  */

    fun tipsLoaded(): Boolean {
        return tips.size > 0
    }

    /** Returns a random tip to be used  */

    fun randomTip(): String {
        return tips.random()
    }
}