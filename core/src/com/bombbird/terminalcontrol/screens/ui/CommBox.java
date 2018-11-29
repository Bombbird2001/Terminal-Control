package com.bombbird.terminalcontrol.screens.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Queue;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival;
import com.bombbird.terminalcontrol.entities.aircrafts.Departure;
import com.bombbird.terminalcontrol.utilities.Fonts;
import org.json.JSONArray;
import org.json.JSONObject;

public class CommBox {
    private Queue<Label> labels;
    private Label header;
    private Table scrollTable;
    private ScrollPane scrollPane;

    public CommBox() {
        labels = new Queue<Label>();

        header = new Label("Communication box", getLabelStyle(Color.WHITE));
        header.setPosition(0.3f * TerminalControl.radarScreen.ui.getPaneWidth(), 3240 * 0.42f);
        TerminalControl.radarScreen.uiStage.addActor(header);

        scrollTable = new Table();
        scrollPane = new ScrollPane(scrollTable);

        scrollPane.setX(0.1f * TerminalControl.radarScreen.ui.getPaneWidth());
        scrollPane.setY(3240 * 0.05f);
        scrollPane.setSize(0.8f * TerminalControl.radarScreen.ui.getPaneWidth(), 3240 * 0.35f);
        scrollPane.getStyle().background = TerminalControl.skin.getDrawable("ListBackground");

        InputListener inputListener = null;
        for (EventListener eventListener: scrollPane.getListeners()) {
            if (eventListener instanceof InputListener) {
                inputListener = (InputListener) eventListener;
            }
        }
        if (inputListener != null) scrollPane.removeListener(inputListener);

        TerminalControl.radarScreen.uiStage.addActor(scrollPane);
    }

    public CommBox(JSONArray save) {
        this();
        for (int i = 0; i < save.length(); i++) {
            JSONObject info = save.getJSONObject(i);

            Label label = new Label(info.getString("message"), getLabelStyle(new Color(Integer.parseInt(info.getString("color"), 16))));

            updateLabelQueue(label);
        }
    }

    public void contactFreq(Aircraft aircraft, String callsign, String freq) {
        String wake = "";
        if (aircraft.getWakeCat() == 'H') {
            wake = " heavy";
        } else if (aircraft.getWakeCat() == 'J') {
            wake = " super";
        }

        Label label = new Label(aircraft.getCallsign() + wake + ", contact " + callsign + " on " + freq + ", good day.", getLabelStyle(Color.BLACK));
        updateLabelQueue(label);

        Label label1 = new Label(freq + ", good day, " + aircraft.getCallsign() + wake, getLabelStyle(aircraft.getColor()));
        updateLabelQueue(label1);
    }

    public void goAround(Aircraft aircraft, String reason) {
        Label label = new Label(aircraft.getCallsign() + " performed a go around due to " + reason, getLabelStyle(Color.BLACK));

        updateLabelQueue(label);
    }

    public void initialContact(Aircraft aircraft) {
        String wake = "";
        if (aircraft.getWakeCat() == 'H') {
            wake = " heavy";
        } else if (aircraft.getWakeCat() == 'J') {
            wake = " super";
        }

        Label label = null;

        String altitude;
        if (aircraft.getAltitude() >= TerminalControl.radarScreen.transLvl * 100) {
            altitude = "FL" + (int)(aircraft.getAltitude() / 100);
        } else {
            altitude = (int)(aircraft.getAltitude() / 100) * 100 + " feet";
        }
        String clearedAltitude;
        if (aircraft.getClearedAltitude() >= TerminalControl.radarScreen.transLvl * 100) {
            clearedAltitude = "FL" + aircraft.getClearedAltitude() / 100;
        } else {
            clearedAltitude = (aircraft.getClearedAltitude() / 100) * 100 + " feet";
        }

        String action;
        if (aircraft.getVerticalSpeed() < -1000) {
            action = "descending through " + altitude + " for " + clearedAltitude;
        } else if (aircraft.getVerticalSpeed() > 1000) {
            action = "climbing through " + altitude + " for " + clearedAltitude;
        } else {
            action = "levelling off at " + clearedAltitude;
            if (Math.abs(aircraft.getClearedAltitude() - aircraft.getAltitude()) <= 50) {
                action = "at " + clearedAltitude;
            }
        }
        if (aircraft instanceof Arrival) {
            if (!aircraft.isGoAroundWindow()) {
                label = new Label(aircraft.getCallsign() + wake + " with you, " + action + " on the " + aircraft.getSidStar().getName() + " arrival, inbound " + aircraft.getDirect().getName(), getLabelStyle(aircraft.getColor()));
            } else {
                label = new Label(aircraft.getCallsign() + wake + " with you, " + action + ", heading " + aircraft.getClearedHeading(), getLabelStyle(aircraft.getColor()));
            }
        } else if (aircraft instanceof Departure) {
            label = new Label(aircraft.getCallsign() + wake + " with you, " + action + ", " + aircraft.getSidStar().getName() + " departure", getLabelStyle(aircraft.getColor()));
        }

        updateLabelQueue(label);
    }

    private void updateLabelQueue(Label label) {
        labels.addLast(label);
        label.setWidth(scrollPane.getWidth() - 20);
        label.setWrap(true);

        while (labels.size > 10) {
            labels.removeFirst();
        }

        scrollTable.clearChildren();
        for (Label label1: labels) {
            scrollTable.add(label1).width(scrollPane.getWidth() - 20).pad(15, 10, 15, 0);
            scrollTable.row();
        }

        scrollPane.layout();
        scrollPane.scrollTo(0, 0, 0, 0);
    }

    private Label.LabelStyle getLabelStyle(Color color) {
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.fontColor = color;
        labelStyle.font = Fonts.defaultFont20;

        return labelStyle;
    }

    public void updateBoxWidth(float paneWidth) {
        scrollPane.setX(0.1f * paneWidth);
        scrollPane.setWidth(0.8f * paneWidth);
    }

    public void setVisible(boolean show) {
        scrollPane.setVisible(show);
        header.setVisible(show);
    }

    public void remove() {
        scrollPane.remove();
        header.remove();
    }

    public Queue<Label> getLabels() {
        return labels;
    }
}
