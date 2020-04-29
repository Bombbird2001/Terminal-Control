package com.bombbird.terminalcontrol.ui.tutorial;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Queue;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival;
import com.bombbird.terminalcontrol.entities.airports.Airport;

import java.util.HashMap;

public class TutorialGroup {
    private final TutorialManager tutorialManager;

    private final Queue<Integer> times;
    private final Queue<String> texts;
    private final HashMap<Integer, String> tasks;

    private float timer;
    private boolean active;

    public TutorialGroup(TutorialManager tutorialManager) {
        this.tutorialManager = tutorialManager;

        times = new Queue<>();
        texts = new Queue<>();
        tasks = new HashMap<>();

        timer = 0;
        active = false;
    }

    /** Updates the individual timer for this tutorial group */
    public void update() {
        if (!active) return;
        if (times.isEmpty() || texts.isEmpty()) return;

        timer += Gdx.graphics.getDeltaTime();

        if (timer >= times.first()) {
            //First message can be posted and removed from queue
            tutorialManager.tutorialMsg(texts.removeFirst());

            //Check for other tasks
            checkAdditionalTasks(times.removeFirst());
        }

        if (times.isEmpty() || texts.isEmpty()) active = false;
    }

    /** Adds a new message and its posting time to the end of the respective queues */
    public void addMessage(int time, String text) {
        times.addLast(time);
        texts.addLast(text);
    }

    /** Adds a new task and its execution time */
    public void addTask(int time, String key) {
        tasks.put(time, key);
    }

    /** Checks, carries out any other task that needs to be carried out along with the tutorial message */
    private void checkAdditionalTasks(int time) {
        if (tasks.containsKey(time)) {
            switch (tasks.get(time)) {
                case "spawnDeparture":
                    Airport airport = TerminalControl.radarScreen.airports.get("TCTP");
                    TerminalControl.radarScreen.newDeparture("CAL641", "A359", airport, airport.getRunways().get("05L"));
                    break;
                case "spawnArrival":
                    TerminalControl.radarScreen.aircrafts.put("EVA226", new Arrival("EVA226", "B77W", TerminalControl.radarScreen.airports.get("TCTP")));
                    break;
                case "pauseTutorial":
                    tutorialManager.setPause(true);
                    break;
                case "continueTutorial":
                    tutorialManager.setPause(false);
                    break;
                case "setPrompt1":
                    tutorialManager.setPrompt1(true);
                    break;
                case "setPrompt2":
                    tutorialManager.setPrompt2(true);
                    break;
                case "setPrompt3":
                    tutorialManager.setPrompt3(true);
                    break;
                case "setPrompt4":
                    tutorialManager.setPrompt4(true);
                    break;
                case "setPrompt5":
                    tutorialManager.setPrompt5(true);
                    break;
                case "setPrompt6":
                    tutorialManager.setPrompt6(true);
                    break;
                case "activateGroup2":
                    tutorialManager.activateGroup2();
                    break;
                case "quit":
                    TerminalControl.radarScreen.setTutorialQuit(true);
                    break;
                default:
                    Gdx.app.log("TutorialGroup", "Unknown task " + tasks.get(time) + " at time " + time);
            }
        }
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
