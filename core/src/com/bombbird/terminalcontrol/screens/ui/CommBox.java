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
import com.bombbird.terminalcontrol.entities.airports.AirportName;
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

            Color color;
            if ("ff0000ff".equals(info.getString("color"))) {
                color = Color.RED;
            } else {
                color = new Color(Integer.parseInt(info.getString("color"), 16));
            }

            Label label = new Label(info.getString("message"), getLabelStyle(color));

            updateLabelQueue(label);
        }
    }

    /** Adds a message for the input aircraft to contact in the input frequency given the callsign of the next controller */
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
        TerminalControl.tts.contactOther(aircraft.getVoice(), freq, aircraft.getCallsign().substring(0, 3), aircraft.getCallsign().substring(3), wake);
    }

    /** Adds a message if the input aircraft goes around, with the reason for the go around */
    public void goAround(Aircraft aircraft, String reason) {
        Label label = new Label(aircraft.getCallsign() + " performed a go around due to " + reason, getLabelStyle(Color.BLACK));

        updateLabelQueue(label);
    }

    /** Adds a message for an aircraft contacting the player for the first time */
    public void initialContact(Aircraft aircraft) {
        String apchCallsign = aircraft instanceof Arrival ? TerminalControl.radarScreen.callsign : TerminalControl.radarScreen.deptCallsign;
        String wake = "";
        if (aircraft.getWakeCat() == 'H') {
            wake = " heavy";
        } else if (aircraft.getWakeCat() == 'J') {
            wake = " super";
        }

        Label label;

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

        String text = "";
        String icao = aircraft.getCallsign().substring(0, 3);
        String flightNo = aircraft.getCallsign().substring(3);
        if (aircraft instanceof Arrival) {
            if (!aircraft.isGoAroundWindow()) {
                text = apchCallsign + ", " + aircraft.getCallsign() + wake + " with you, " + action + " on the " + aircraft.getSidStar().getName() + " arrival, inbound " + aircraft.getDirect().getName();
                TerminalControl.tts.initArrContact(aircraft.getVoice(), apchCallsign, icao, flightNo, wake, action, aircraft.getSidStar().getPronounciation().toLowerCase(), aircraft.getDirect().getName());
            } else {
                text = apchCallsign + ", " + aircraft.getCallsign() + wake + " with you, " + action + ", heading " + aircraft.getClearedHeading();
                TerminalControl.tts.goAroundContact(aircraft.getVoice(), apchCallsign, icao, flightNo, wake, action, Integer.toString(aircraft.getClearedHeading()));
            }
        } else if (aircraft instanceof Departure) {
            String outboundText = "";
            if (!"-".equals(AirportName.getAirportName(aircraft.getAirport().getIcao()))) outboundText = "outbound " + AirportName.getAirportName(aircraft.getAirport().getIcao()) + ", ";
            text = apchCallsign + ", " + aircraft.getCallsign() + wake + " with you, " + outboundText + action + ", " + aircraft.getSidStar().getName() + " departure";
            TerminalControl.tts.initDepContact(aircraft.getVoice(), apchCallsign, icao, flightNo, wake, aircraft.getAirport().getIcao(), outboundText, action, aircraft.getSidStar().getPronounciation().toLowerCase());
        }

        label = new Label(text, getLabelStyle(aircraft.getColor()));

        updateLabelQueue(label);

        TerminalControl.radarScreen.soundManager.playInitialContact();
    }

    /** Adds a normal message */
    public void normalMsg(String msg) {
        Label label = new Label(msg, getLabelStyle(Color.BLACK));
        updateLabelQueue(label);
    }

    /** Adds a message in red for warnings */
    public void warningMsg(String msg) {
        Label label = new Label(msg, getLabelStyle(Color.RED));
        updateLabelQueue(label);
    }

    /** Adds the label to queue and removes labels if necessary, updates scrollPane to show messages */
    private void updateLabelQueue(Label label) {
        labels.addLast(label);
        label.setWidth(scrollPane.getWidth() - 20);
        label.setWrap(true);

        while (labels.size > 10) {
            labels.removeFirst();
        }

        scrollTable.clearChildren();
        for (int i = 0; i < labels.size; i++) {
            scrollTable.add(labels.get(i)).width(scrollPane.getWidth() - 20).pad(15, 10, 15, 0);
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
        header.setX(0.3f * TerminalControl.radarScreen.ui.getPaneWidth());
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
