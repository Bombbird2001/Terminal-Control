package com.bombbird.terminalcontrol.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival;
import com.bombbird.terminalcontrol.entities.aircrafts.Departure;
import com.bombbird.terminalcontrol.entities.airports.AirportName;
import com.bombbird.terminalcontrol.utilities.ErrorHandler;
import com.bombbird.terminalcontrol.utilities.Fonts;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.TimeZone;

public class CommBox {
    private Queue<Label> labels;
    private Label header;
    private Table scrollTable;
    private ScrollPane scrollPane;

    private static final Array<String> atcByeCtr = new Array<String>();
    private static final Array<String> atcByeTwr = new Array<String>();
    private static final Array<String> pilotBye = new Array<String>();

    public CommBox() {
        atcByeCtr.add("good day", "see you", "have a good flight", "have a safe flight");
        atcByeTwr.add("good day", "see you", "have a safe landing");
        pilotBye.add("good day", "see you", "have a nice day", "bye bye");

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

            Gdx.app.postRunnable(() -> {
                Label label = new Label(info.getString("message"), getLabelStyle(color));
                updateLabelQueue(label);
            });
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

        String bai = ", ";
        bai += callsign.contains("Tower") ? atcByeTwr.random() : atcByeCtr.random();
        if (aircraft.getEmergency().isActive()) bai = ", have a safe landing";
        String finalWake = wake;
        String finalBai = bai;
        Gdx.app.postRunnable(() -> {
            Label label = new Label(aircraft.getCallsign() + finalWake + ", contact " + callsign + " on " + freq + finalBai + ".", getLabelStyle(Color.BLACK));
            updateLabelQueue(label);
        });

        String bye = ", ";
        bye += pilotBye.random() + ", ";
        String finalBye = bye;
        String finalWake1 = wake;
        Gdx.app.postRunnable(() -> {
            Label label1 = new Label(freq + finalBye + aircraft.getCallsign() + finalWake1, getLabelStyle(aircraft.getColor()));
            updateLabelQueue(label1);
        });
        //Remove lowkey annoying message from TTS
        //TerminalControl.tts.contactOther(aircraft.getVoice(), freq, aircraft.getCallsign().substring(0, 3), aircraft.getCallsign().substring(3), wake);
    }

    /** Adds a message if the input aircraft goes around, with the reason for the go around */
    public void goAround(Aircraft aircraft, String reason) {
        Gdx.app.postRunnable(() -> {
            Label label = new Label(aircraft.getCallsign() + " performed a go around due to " + reason, getLabelStyle(Color.BLACK));
            updateLabelQueue(label);
        });
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

        String altitude;
        if (aircraft.getAltitude() >= TerminalControl.radarScreen.transLvl * 100) {
            altitude = "FL" + (int)(aircraft.getAltitude() / 100);
        } else {
            altitude = Integer.toString((int)(aircraft.getAltitude() / 100) * 100);
        }
        String clearedAltitude;
        if (aircraft.getClearedAltitude() >= TerminalControl.radarScreen.transLvl * 100) {
            clearedAltitude = "FL" + aircraft.getClearedAltitude() / 100;
        } else {
            clearedAltitude = (aircraft.getClearedAltitude() / 100) * 100 + " feet";
        }

        String action;
        float deltaAlt = aircraft.getClearedAltitude() - aircraft.getAltitude();
        if (deltaAlt < -600) {
            action = "descending through " + altitude + " for " + clearedAltitude;
        } else if (deltaAlt > 400) {
            action = "climbing through " + altitude + " for " + clearedAltitude;
        } else {
            action = "levelling off at " + clearedAltitude;
            if (Math.abs(deltaAlt) <= 50) {
                action = "at " + clearedAltitude;
            }
        }

        String text = "";
        String greeting = "";
        int random = MathUtils.random(2);
        if (random == 1) {
            greeting = getGreetingByTime();
        } else if (random == 2) {
            greeting = " hello";
        }
        String starString = "";
        boolean starSaid = MathUtils.randomBoolean();
        if (starSaid) {
            starString = " on the " + aircraft.getSidStar().getName() + " arrival";
        }
        String inboundString = "";
        boolean inboundSaid = MathUtils.randomBoolean();
        if (inboundSaid && !aircraft.isGoAroundWindow() && aircraft.getDirect() != null) {
            inboundString = ", inbound " + aircraft.getDirect().getName();
        }
        String infoString = "";
        if (MathUtils.randomBoolean()) {
            if (MathUtils.randomBoolean()) {
                infoString = ", information " + TerminalControl.radarScreen.getInformation();
            } else {
                infoString = ", we have information " + TerminalControl.radarScreen.getInformation();
            }
        }
        if (aircraft instanceof Arrival) {
            if (!aircraft.isGoAroundWindow()) {
                text = apchCallsign + greeting + ", " + aircraft.getCallsign() + wake + " with you, " + action + starString + inboundString + infoString;
                TerminalControl.tts.initArrContact(aircraft, wake, apchCallsign, greeting, action, aircraft.getSidStar().getPronunciation().toLowerCase(), starSaid, aircraft.getDirect().getName(), inboundSaid, infoString);
            } else {
                text = apchCallsign + ", " + aircraft.getCallsign() + wake + " with you, " + action + ", heading " + aircraft.getClearedHeading();
                TerminalControl.tts.goAroundContact(aircraft, wake, apchCallsign, action, Integer.toString(aircraft.getClearedHeading()));
            }
        } else if (aircraft instanceof Departure) {
            String outboundText = "";
            if (!"-".equals(AirportName.getAirportName(aircraft.getAirport().getIcao()))) outboundText = "outbound " + AirportName.getAirportName(aircraft.getAirport().getIcao()) + ", ";
            String sidString = "";
            boolean sidSaid = MathUtils.randomBoolean();
            if (sidSaid) {
                sidString = ", " + aircraft.getSidStar().getName() + " departure";
            }
            text = apchCallsign + greeting + ", " + aircraft.getCallsign() + wake + " with you, " + outboundText + action + sidString;
            TerminalControl.tts.initDepContact(aircraft, wake, apchCallsign, greeting, aircraft.getAirport().getIcao(), outboundText, action, aircraft.getSidStar().getPronunciation().toLowerCase(), sidSaid);
        }

        String finalText = text;
        Gdx.app.postRunnable(() -> {
            Label label = new Label(finalText, getLabelStyle(aircraft.getColor()));
            updateLabelQueue(label);
        });

        TerminalControl.radarScreen.soundManager.playInitialContact();
    }

    /** Adds a message for an aircraft established in hold over a waypoint */
    public void holdEstablishMsg(Aircraft aircraft, String wpt) {
        String wake = "";
        if (aircraft.getWakeCat() == 'H') {
            wake = " heavy";
        } else if (aircraft.getWakeCat() == 'J') {
            wake = " super";
        }

        String text = aircraft.getCallsign() + wake;
        int random = MathUtils.random(2);
        if (random == 0) {
            text += " is established in the hold over " + wpt;
        } else if (random == 1) {
            text += ", holding over " + wpt;
        } else if (random == 2) {
            text += ", we're holding at " + wpt;
        }

        TerminalControl.tts.holdEstablishMsg(aircraft, wake, wpt, random);

        String finalText = text;
        Gdx.app.postRunnable(() -> {
            Label label = new Label(finalText, getLabelStyle(aircraft.getColor()));
            updateLabelQueue(label);
        });

        TerminalControl.radarScreen.soundManager.playInitialContact();
    }

    /** Adds a normal message */
    public void normalMsg(String msg) {
        Gdx.app.postRunnable(() -> {
            for (int i = 0; i < 3; i++) {
                try {
                    Label label = new Label(msg, getLabelStyle(Color.BLACK));
                    updateLabelQueue(label);
                    break;
                } catch (NullPointerException e) {
                    ErrorHandler.sendRepeatableError("Normal message error", e, i + 1);
                }
            }
        });
    }

    /** Adds a message in red for warnings */
    public void warningMsg(String msg) {
        Gdx.app.postRunnable(() -> {
            Label label = new Label(msg, getLabelStyle(Color.RED));
            updateLabelQueue(label);
        });

        TerminalControl.radarScreen.soundManager.playConflict();
    }

    private String getGreetingByTime() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        int additional = calendar.get(Calendar.AM_PM) == Calendar.PM ? 12 : 0;
        int time = (calendar.get(Calendar.HOUR) + additional) * 100 + calendar.get(Calendar.MINUTE);

        String greeting = " good ";
        if (time <= 1200) {
            greeting += "morning";
        } else if (time <= 1700) {
            greeting += "afternoon";
        } else {
            greeting += "evening";
        }

        return greeting;
    }

    /** Adds the label to queue and removes labels if necessary, updates scrollPane to show messages */
    private void updateLabelQueue(Label label) {
        labels.addLast(label);
        label.setWidth(scrollPane.getWidth() - 20);
        label.setWrap(true);

        while (labels.size > 15) {
            labels.removeFirst();
        }

        scrollTable.clearChildren();
        for (int i = 0; i < labels.size; i++) {
            scrollTable.add(labels.get(i)).width(scrollPane.getWidth() - 20).pad(15, 10, 15, 0).getActor().invalidate();
            scrollTable.row();
        }

        try {
            scrollPane.layout();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            scrollPane.layout();
        }
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
