package com.bombbird.terminalcontrol.screens.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Queue;
import com.badlogic.gdx.utils.Timer;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival;
import com.bombbird.terminalcontrol.entities.aircrafts.Departure;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.utilities.Fonts;

public class DataTag {
    //Rendering parameters
    public static TextureAtlas ICON_ATLAS;
    public static Skin SKIN;
    private static final ImageButton.ImageButtonStyle BUTTON_STYLE_CTRL = new ImageButton.ImageButtonStyle();
    private static final ImageButton.ImageButtonStyle BUTTON_STYLE_DEPT = new ImageButton.ImageButtonStyle();
    private static final ImageButton.ImageButtonStyle BUTTON_STYLE_UNCTRL = new ImageButton.ImageButtonStyle();
    private static final ImageButton.ImageButtonStyle BUTTON_STYLE_ENROUTE = new ImageButton.ImageButtonStyle();
    private static NinePatch LABEL_PATCH_GREEN;
    private static NinePatch LABEL_PATCH_BLUE;
    private static NinePatch LABEL_PATCH_ORANGE;
    private static NinePatch LABEL_PATCH_RED;
    private static boolean LOADED_ICONS = false;

    private Aircraft aircraft;

    private Label label;
    private String[] labelText;
    private ImageButton icon;
    private Button labelButton;
    private Button clickSpot;
    private boolean dragging;

    private int tapCount;
    private boolean minimized;

    private Queue<Image> trailDots;

    private RadarScreen radarScreen;

    private static final Timer tapTimer = new Timer();
    private static final Timer flashTimer = new Timer();

    public DataTag(final Aircraft aircraft) {
        loadResources();

        this.aircraft = aircraft;
        radarScreen = aircraft.radarScreen;

        trailDots = new Queue<Image>();

        dragging = false;

        icon = new ImageButton(BUTTON_STYLE_UNCTRL);
        icon.setSize(20, 20);
        icon.getImageCell().size(20, 20);
        TerminalControl.radarScreen.stage.addActor(icon);

        labelText = new String[11];
        labelText[9] = aircraft.getAirport().getIcao();
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont6;
        labelStyle.fontColor = Color.WHITE;
        label = new Label("Loading...", labelStyle);
        label.setPosition(aircraft.getX() - label.getWidth() / 2, aircraft.getY() + 25);

        labelButton = new Button(SKIN.getDrawable("labelBackgroundSmall"), SKIN.getDrawable("labelBackgroundSmall"));
        labelButton.setSize(label.getWidth() + 10, label.getHeight());


        NinePatchDrawable ninePatchDrawable;
        if (aircraft instanceof Arrival) {
            ninePatchDrawable = new NinePatchDrawable(LABEL_PATCH_BLUE);
        } else {
            ninePatchDrawable = new NinePatchDrawable(LABEL_PATCH_GREEN);
        }
        Button.ButtonStyle clickSpotStyle = new Button.ButtonStyle(ninePatchDrawable, ninePatchDrawable, ninePatchDrawable);
        clickSpot = new Button(clickSpotStyle);
        clickSpot.setSize(labelButton.getWidth(), labelButton.getHeight());
        clickSpot.setName(aircraft.getCallsign());
        clickSpot.addListener(new DragListener() {
            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
                label.moveBy(x - labelButton.getWidth() / 2, y - labelButton.getHeight() / 2);
                dragging = true;
                event.handle();
            }
        });
        clickSpot.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!dragging) {
                    TerminalControl.radarScreen.setSelectedAircraft(TerminalControl.radarScreen.aircrafts.get(actor.getName()));
                    tapCount++;
                    if (tapCount >= 2) {
                        if (aircraft.getControlState() == 1 || aircraft.getControlState() == 2) minimized = !minimized;
                        tapCount = 0;
                        tapTimer.clear();
                    }
                    tapTimer.scheduleTask(new Timer.Task() {
                        @Override
                        public void run() {
                            tapCount = 0;
                        }
                    }, 0.2f);
                } else {
                    dragging = false;
                }
            }
        });

        Stage labelStage = TerminalControl.radarScreen.labelStage;
        labelStage.addActor(labelButton);
        labelStage.addActor(label);
        labelStage.addActor(clickSpot);
    }

    public static void setLoadedIcons(boolean loadedIcons) {
        LOADED_ICONS = loadedIcons;
    }

    /** Loads label, icon resources */
    public void loadResources() {
        if (!LOADED_ICONS) {
            ICON_ATLAS = new TextureAtlas(Gdx.files.internal("game/aircrafts/aircraftIcons.atlas"));
            SKIN = new Skin(ICON_ATLAS);

            BUTTON_STYLE_CTRL.imageUp = SKIN.getDrawable("aircraftControlled");
            BUTTON_STYLE_CTRL.imageDown = SKIN.getDrawable("aircraftControlled");
            BUTTON_STYLE_DEPT.imageUp = SKIN.getDrawable("aircraftDeparture");
            BUTTON_STYLE_DEPT.imageDown = SKIN.getDrawable("aircraftDeparture");
            BUTTON_STYLE_UNCTRL.imageUp = SKIN.getDrawable("aircraftNotControlled");
            BUTTON_STYLE_UNCTRL.imageDown = SKIN.getDrawable("aircraftNotControlled");
            BUTTON_STYLE_ENROUTE.imageUp = SKIN.getDrawable("aircraftEnroute");
            BUTTON_STYLE_ENROUTE.imageDown = SKIN.getDrawable("aircraftEnroute");

            LABEL_PATCH_GREEN = new NinePatch(SKIN.getRegion("labelBorderGreen"), 3, 3, 3, 3);
            LABEL_PATCH_BLUE = new NinePatch(SKIN.getRegion("labelBorderBlue"), 3, 3, 3, 3);
            LABEL_PATCH_ORANGE = new NinePatch(SKIN.getRegion("labelBorderOrange"), 3, 3, 3, 3);
            LABEL_PATCH_RED= new NinePatch(SKIN.getRegion("labelBorderRed"), 3, 3, 3, 3);

            LOADED_ICONS = true;
        }
    }

    /** Renders the line joining label and aircraft icon */
    public void renderShape() {
        radarScreen.shapeRenderer.line(label.getX() + label.getWidth() / 2, label.getY() + label.getHeight() / 2, aircraft.getRadarX(), aircraft.getRadarY());
    }

    /** Updates the position, draws of the icon */
    public void updateIcon(Batch batch) {
        icon.setPosition(aircraft.getRadarX() - 10, aircraft.getRadarY() - 10);
        icon.setColor(Color.BLACK); //Icon doesn't draw without this for some reason
        icon.draw(batch, 1);
    }

    /** Updates the icon colour depending on aircraft control state */
    public void updateIconColors(int controlState) {
        if (controlState == -1) { //En route aircraft - gray
            icon.setStyle(BUTTON_STYLE_ENROUTE);
        } else if (controlState == 0) { //Uncontrolled aircraft - yellow
            icon.setStyle(BUTTON_STYLE_UNCTRL);
        } else if (controlState == 1) { //Controlled arrival - blue
            icon.setStyle(BUTTON_STYLE_CTRL);
        } else if (controlState == 2) { //Controlled departure - green
            icon.setStyle(BUTTON_STYLE_DEPT);
        } else {
            Gdx.app.log("Aircraft control state error", "Invalid control state " + controlState + " set!");
        }
    }

    /** Called to start flashing an aircraft's label borders during initial contact */
    public void flashIcon() {
        if (aircraft.isActionRequired() && !aircraft.isEmergency()) {
            NinePatchDrawable ninePatchDrawable = new NinePatchDrawable(LABEL_PATCH_ORANGE);
            clickSpot.getStyle().up = ninePatchDrawable;
            clickSpot.getStyle().down = ninePatchDrawable;
            clickSpot.getStyle().checked = ninePatchDrawable;
            flashTimer.scheduleTask(new Timer.Task() {
                @Override
                public void run() {
                    NinePatchDrawable ninePatchDrawable = new NinePatchDrawable(LABEL_PATCH_GREEN);
                    if (aircraft instanceof Arrival) {
                        ninePatchDrawable = new NinePatchDrawable(LABEL_PATCH_BLUE);
                    }
                    clickSpot.getStyle().up = ninePatchDrawable;
                    clickSpot.getStyle().down = ninePatchDrawable;
                    clickSpot.getStyle().checked = ninePatchDrawable;
                }
            }, 1);

            flashTimer.scheduleTask(new Timer.Task() {
                @Override
                public void run() {
                    flashIcon();
                }
            }, 2);
        }
    }

    /** Called to change aircraft label to red for emergencies */
    public void setEmergency() {
        NinePatchDrawable ninePatchDrawable = new NinePatchDrawable(LABEL_PATCH_RED);
        clickSpot.getStyle().up = ninePatchDrawable;
        clickSpot.getStyle().down = ninePatchDrawable;
        clickSpot.getStyle().checked = ninePatchDrawable;
    }

    /** Draws the trail dots for aircraft */
    public void drawTrailDots(Batch batch, float parentAlpha) {
        int index = 0;
        int size = trailDots.size;
        for (Image trail: trailDots) {
            if (aircraft.isSelected() || (size - index <= 5 && (aircraft.getControlState() == 1 || aircraft.getControlState() == 2))) {
                trail.draw(batch, parentAlpha);
            }
            index++;
        }
    }

    /** Appends a new image to end of queue for drawing trail dots given a set of coordinates */
    public void addTrailDot(float x, float y) {
        Image image;
        if (aircraft instanceof Arrival) {
            image = new Image(DataTag.SKIN.getDrawable("DotsArrival"));
        } else if (aircraft instanceof Departure) {
            image = new Image(DataTag.SKIN.getDrawable("DotsDeparture"));
        } else {
            image = new Image();
            Gdx.app.log("Trail dot error", "Aircraft not instance of arrival or departure, trail image not found!");
        }
        image.setPosition(x - image.getWidth() / 2, y - image.getHeight() / 2);
        trailDots.addLast(image);
    }

    /** Updates the position of the label to prevent it from going out of bounds */
    public void moderateLabel() {
        if (label.getX() < 936) {
            label.setX(936);
        } else if (label.getX() + label.getWidth() > 4824) {
            label.setX(4824 - label.getWidth());
        }
        if (label.getY() < 0) {
            label.setY(0);
        } else if (label.getY() + label.getHeight() > 3240) {
            label.setY(3240 - label.getHeight());
        }
    }

    /** Updates the label on the radar given aircraft's radar data and other data */
    public void updateLabel() {
        String vertSpd;
        if (aircraft.getRadarVs() < -150) {
            vertSpd = " DOWN ";
        } else if (aircraft.getRadarVs() > 150) {
            vertSpd = " UP ";
        } else {
            vertSpd = " = ";
        }
        labelText[0] = aircraft.getCallsign();
        labelText[1] = aircraft.getIcaoType() + "/" + aircraft.getWakeCat();
        labelText[2] = Integer.toString(MathUtils.round(aircraft.getRadarAlt() / 100));
        labelText[3] = aircraft.isGsCap() ? "GS" : Integer.toString(aircraft.getTargetAltitude() / 100);
        labelText[10] = Integer.toString(aircraft.getNavState().getClearedAlt().last() / 100);
        if ((MathUtils.round((float) aircraft.getRadarHdg()) == 0)) {
            aircraft.setRadarHdg(aircraft.getRadarHdg() + 360);
        }
        labelText[4] = Integer.toString(MathUtils.round((float) aircraft.getRadarHdg()));
        if (aircraft.getNavState().getDispLatMode().first().contains("heading") && !aircraft.getNavState().getDispLatMode().first().equals("After waypoint, fly heading")) {
            if (aircraft.isLocCap()) {
                labelText[5] = "LOC";
            } else {
                labelText[5] = Integer.toString(aircraft.getNavState().getClearedHdg().last());
            }
        } else if ("Hold at".equals(aircraft.getNavState().getDispLatMode().last())) {
            if (aircraft.isHolding() || (aircraft.getDirect() != null && aircraft.getDirect().equals(aircraft.getHoldWpt()))) {
                labelText[5] = aircraft.getHoldWpt().getName();
            } else if (aircraft.getDirect() != null) {
                labelText[5] = aircraft.getDirect().getName();
            }
        } else if (aircraft.getNavState().getDispLatMode().last().contains(aircraft.getSidStar().getName()) || aircraft.getNavState().getDispLatMode().last().equals("After waypoint, fly heading")) {
            if (aircraft.getNavState().getClearedDirect().last().equals(aircraft.getNavState().getClearedAftWpt().last()) && aircraft.getNavState().getDispLatMode().last().equals("After waypoint, fly heading")) {
                labelText[5] = aircraft.getNavState().getClearedDirect().last().getName() + aircraft.getNavState().getClearedAftWptHdg().last();
            } else {
                labelText[5] = aircraft.getNavState().getClearedDirect().last().getName();
            }
        }
        labelText[6] = Integer.toString((int) aircraft.getRadarGs());
        labelText[7] = Integer.toString(aircraft.getNavState().getClearedSpd().last());
        if (aircraft.getNavState().getClearedIls().last() != null) {
            labelText[8] = aircraft.getNavState().getClearedIls().last().getName();
        } else {
            labelText[8] = aircraft.getSidStar().getName();
        }
        String exped = aircraft.getNavState().getClearedExpedite().last() ? " =>> " : " => ";
        String updatedText;
        if (!minimized && (aircraft.getControlState() == 1 || aircraft.getControlState() == 2)) {
            updatedText = labelText[0] + " " + labelText[1] + "\n" + labelText[2] + vertSpd + labelText[3] + exped + labelText[10] + "\n" + labelText[4] + " " + labelText[5] + " " + labelText[8] + "\n" + labelText[6] + " " + labelText[7] + " " + labelText[9];
        } else {
            updatedText = labelText[0] + "\n" + labelText[2] + " " + labelText[4] + "\n" + labelText[6];
        }
        label.setText(updatedText);
        label.pack();
        labelButton.setSize(label.getWidth() + 10, label.getHeight());
        labelButton.setPosition(label.getX() - 5, label.getY());
        clickSpot.setSize(labelButton.getWidth(), labelButton.getHeight());
        clickSpot.setPosition(labelButton.getX(), labelButton.getY());
    }

    /** Moves label as aircraft moves */
    public void moveLabel(float deltaX, float deltaY) {
        label.moveBy(deltaX, deltaY);
    }

    /** Removes all components of label from stage */
    public void removeLabel() {
        label.remove();
        icon.remove();
        labelButton.remove();
        clickSpot.remove();
    }

    /** Sets the position of the label */
    public void setLabelPosition(float x, float y) {
        label.setPosition(x, y);
    }

    public float[] getLabelPosition() {
        return new float[] {label.getX(), label.getY()};
    }

    public Queue<Image> getTrailDots() {
        return trailDots;
    }

    public boolean isMinimized() {
        return minimized;
    }

    public void setMinimized(boolean minimized) {
        this.minimized = minimized;
    }

    /** Pauses all timers */
    public static void pauseTimers() {
        tapTimer.stop();
        flashTimer.stop();
    }

    /** Resumes all timers */
    public static void startTimers() {
        tapTimer.start();
        flashTimer.start();
    }
}
