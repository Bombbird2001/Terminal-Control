package com.bombbird.terminalcontrol.ui.tutorial

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Queue
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival
import java.util.*

class TutorialGroup(private val tutorialManager: TutorialManager) {
    private val radarScreen = TerminalControl.radarScreen!!
    private val times: Queue<Int> = Queue()
    private val texts: Queue<String> = Queue()
    private val tasks: HashMap<Int, String> = HashMap()
    private var timer: Float = 0f
    var isActive = false

    /** Updates the individual timer for this tutorial group  */
    fun update() {
        if (!isActive) return
        if (times.isEmpty) {
            isActive = false
            return
        }
        timer += Gdx.graphics.deltaTime
        if (timer >= times.first()) {
            //First message can be posted and removed from queue
            if (!texts.isEmpty) tutorialManager.tutorialMsg(texts.removeFirst())

            //Check for other tasks
            checkAdditionalTasks(times.removeFirst())
        }
    }

    /** Adds a new message and its posting time to the end of the respective queues  */
    fun addMessage(time: Int, text: String) {
        if (!times.contains(time)) times.addLast(time)
        texts.addLast(text)
    }

    /** Adds a new task and its execution time  */
    fun addTask(time: Int, key: String) {
        if (!times.contains(time)) times.addLast(time)
        tasks[time] = key
    }

    /** Checks, carries out any other task that needs to be carried out along with the tutorial message  */
    private fun checkAdditionalTasks(time: Int) {
        if (tasks.containsKey(time)) {
            when (tasks[time]) {
                "spawnDeparture" -> {
                    val airport = radarScreen.airports["TCTP"]!!
                    radarScreen.newDeparture("CAL641", "A359", airport, airport.runways["05L"]!!, airport.sids["HICAL1C"]!!)
                }
                "spawnArrival" -> {
                    val airport = radarScreen.airports["TCTP"]!!
                    radarScreen.aircrafts["EVA226"] = Arrival("EVA226", "B77W", airport, airport.stars["NTN1A"]!!)
                }
                "pauseTutorial" -> tutorialManager.setPause(true)
                "continueTutorial" -> tutorialManager.setPause(false)
                "setPrompt1" -> tutorialManager.prompt1 = true
                "setPrompt2" -> tutorialManager.prompt2 = true
                "setPrompt3" -> tutorialManager.prompt3 = true
                "setPrompt4" -> tutorialManager.prompt4 = true
                "setPrompt5" -> tutorialManager.prompt5 = true
                "setPrompt6" -> tutorialManager.prompt6 = true
                "activateGroup2" -> tutorialManager.activateGroup2()
                "quit" -> radarScreen.tutorialQuit = true
                else -> Gdx.app.log("TutorialGroup", "Unknown task " + tasks[time] + " at time " + time)
            }
        }
    }
}