package com.bombbird.terminalcontrol.ui;

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
import com.bombbird.terminalcontrol.entities.aircrafts.NavState;
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen;
import com.bombbird.terminalcontrol.ui.tabs.AltTab;
import com.bombbird.terminalcontrol.ui.tabs.LatTab;
import com.bombbird.terminalcontrol.ui.tabs.SpdTab;
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
    private static NinePatch LABEL_PATCH_MAGENTA;
    private static boolean LOADED_ICONS = false;

    private final Aircraft aircraft;

    private final Label label;
    private final String[] labelText;
    private final ImageButton icon;
    private final Button labelButton;
    private final Button clickSpot;
    private boolean dragging;
    private boolean flashing;

    private int tapCount;
    private boolean minimized;

    private final Queue<Image> trailDots;

    private final RadarScreen radarScreen;

    private static final Timer tapTimer = new Timer();
    private static final Timer flashTimer = new Timer();

    public DataTag(final Aircraft aircraft) {
        loadResources();

        this.aircraft = aircraft;
        radarScreen = aircraft.radarScreen;

        trailDots = new Queue<>();

        dragging = false;
        flashing = false;

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
                    if (radarScreen.tutorialManager == null || !radarScreen.tutorialManager.isPausedForReading()) {
                        radarScreen.setSelectedAircraft(radarScreen.aircrafts.get(actor.getName()));
                        radarScreen.addToEasterEggQueue(aircraft);
                        aircraft.setSilenced(true);
                    }
                    tapCount++;
                    if (tapCount >= 2) {
                        if (aircraft.isArrivalDeparture()) minimized = !minimized;
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
    private void loadResources() {
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
            LABEL_PATCH_RED = new NinePatch(SKIN.getRegion("labelBorderRed"), 3, 3, 3, 3);
            LABEL_PATCH_MAGENTA = new NinePatch(SKIN.getRegion("labelBorderMagenta"), 3, 3, 3, 3);

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
    public void updateIconColors(Aircraft.ControlState controlState) {
        if (controlState == Aircraft.ControlState.ENROUTE) { //En route aircraft - gray
            icon.setStyle(BUTTON_STYLE_ENROUTE);
        } else if (controlState == Aircraft.ControlState.UNCONTROLLED) { //Uncontrolled aircraft - yellow
            icon.setStyle(BUTTON_STYLE_UNCTRL);
        } else if (controlState == Aircraft.ControlState.ARRIVAL) { //Controlled arrival - blue
            icon.setStyle(BUTTON_STYLE_CTRL);
        } else if (controlState == Aircraft.ControlState.DEPARTURE) { //Controlled departure - green
            icon.setStyle(BUTTON_STYLE_DEPT);
        } else {
            Gdx.app.log("Aircraft control state error", "Invalid control state " + controlState + " set!");
        }
    }

    /** Called to start flashing an aircraft's label borders during initial contact or when a conflict is predicted */
    public void startFlash() {
        if (flashing) return;
        if (aircraft.hasEmergency()) return;
        if (aircraft.isTrajectoryConflict() || aircraft.isTrajectoryTerrainConflict() || aircraft.isActionRequired()) {
            flashing = true;
            continuousFlashing();
        }
    }

    /** Called by start flashing method and itself to keep flashing the label */
    private void continuousFlashing() {
        if (aircraft.isTrajectoryConflict() || aircraft.isTrajectoryTerrainConflict()) {
            NinePatchDrawable ninePatchDrawable = new NinePatchDrawable(LABEL_PATCH_MAGENTA);
            clickSpot.getStyle().up = ninePatchDrawable;
            clickSpot.getStyle().down = ninePatchDrawable;
            clickSpot.getStyle().checked = ninePatchDrawable;
            flashTimer.scheduleTask(new Timer.Task() {
                @Override
                public void run() {
                    resetFlash();
                }
            }, 1);
            flashTimer.scheduleTask(new Timer.Task() {
                @Override
                public void run() {
                    continuousFlashing();
                }
            }, 2);
        } else if (aircraft.isActionRequired()) {
            NinePatchDrawable ninePatchDrawable = new NinePatchDrawable(LABEL_PATCH_ORANGE);
            clickSpot.getStyle().up = ninePatchDrawable;
            clickSpot.getStyle().down = ninePatchDrawable;
            clickSpot.getStyle().checked = ninePatchDrawable;
            flashTimer.scheduleTask(new Timer.Task() {
                @Override
                public void run() {
                    resetFlash();
                }
            }, 1);
            flashTimer.scheduleTask(new Timer.Task() {
                @Override
                public void run() {
                    continuousFlashing();
                }
            }, 2);
        } else {
            flashing = false;
        }
    }

    /** Resets the border to blue/green depending on aircraft type */
    private void resetFlash() {
        if (aircraft.hasEmergency()) {
            setEmergency();
            return;
        }
        NinePatchDrawable ninePatchDrawable = new NinePatchDrawable(LABEL_PATCH_GREEN);
        if (aircraft instanceof Arrival) {
            ninePatchDrawable = new NinePatchDrawable(LABEL_PATCH_BLUE);
        }
        clickSpot.getStyle().up = ninePatchDrawable;
        clickSpot.getStyle().down = ninePatchDrawable;
        clickSpot.getStyle().checked = ninePatchDrawable;
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
        if (radarScreen.pastTrajTime == 0) return;
        int index = 0;
        int size = trailDots.size;
        int selectedRequired = radarScreen.pastTrajTime / 10;
        for (Image trail: trailDots) {
            if ((aircraft.isSelected() && (radarScreen.pastTrajTime == -1 || size - index <= selectedRequired)) || (size - index <= 6 && aircraft.isArrivalDeparture())) {
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
        LatTab latTab = aircraft.ui.latTab;
        AltTab altTab = aircraft.ui.altTab;
        SpdTab spdTab = aircraft.ui.spdTab;
        String vertSpd;
        if (aircraft.getRadarVs() < -150) {
            vertSpd = " v ";
        } else if (aircraft.getRadarVs() > 150) {
            vertSpd = " ^ ";
        } else {
            vertSpd = " = ";
        }
        labelText[0] = aircraft.getCallsign();
        labelText[1] = aircraft.getIcaoType() + "/" + aircraft.getWakeCat() + "/" + aircraft.getRecat();
        labelText[2] = Integer.toString(MathUtils.round(aircraft.getRadarAlt() / 100));
        labelText[3] = aircraft.isGsCap() ? "GS" : Integer.toString(aircraft.getTargetAltitude() / 100);
        labelText[10] = Integer.toString(aircraft.getNavState().getClearedAlt().last() / 100);
        if (aircraft.isSelected() && aircraft.isArrivalDeparture()) {
            labelText[10] = Integer.toString(AltTab.clearedAlt / 100);
            if (altTab.isAltChanged()) labelText[10] = "[YELLOW]" + labelText[10] + "[WHITE]";
        }
        if ((MathUtils.round((float) aircraft.getRadarHdg()) == 0)) {
            aircraft.setRadarHdg(aircraft.getRadarHdg() + 360);
        }
        labelText[4] = Integer.toString(MathUtils.round((float) aircraft.getRadarHdg()));
        labelText[6] = Integer.toString((int) aircraft.getRadarGs());
        labelText[7] = (aircraft.isSelected() && aircraft.isArrivalDeparture() && spdTab.isSpdChanged()) ? "[YELLOW]" + SpdTab.clearedSpd + "[WHITE]" : Integer.toString(aircraft.getNavState().getClearedSpd().last());

        String exped = aircraft.getNavState().getClearedExpedite().last() ? " =>> " : " => ";
        String updatedText;
        if (radarScreen.compactData) {
            if (aircraft.isSelected() && aircraft.isArrivalDeparture()) {
                boolean changed = false;
                if ((LatTab.latMode.contains("arrival") || LatTab.latMode.contains("departure")) && (aircraft.getNavState().getClearedDirect().last() == null || !LatTab.clearedWpt.equals(aircraft.getNavState().getClearedDirect().last().getName()) || aircraft.getNavState().containsCode(aircraft.getNavState().getDispLatMode().last(), NavState.FLY_HEADING, NavState.TURN_RIGHT, NavState.TURN_LEFT, NavState.AFTER_WAYPOINT_FLY_HEADING, NavState.HOLD_AT))) {
                    labelText[5] = LatTab.clearedWpt;
                    changed = latTab.isLatModeChanged() || latTab.isWptChanged();
                } else if ("Hold at".equals(LatTab.latMode)) {
                    labelText[5] = LatTab.holdWpt;
                    changed = latTab.isLatModeChanged() || latTab.isHoldWptChanged();
                } else if ("After waypoint, fly heading".equals(LatTab.latMode)) {
                    if (aircraft.getNavState().getClearedDirect().last().equals(aircraft.getNavState().getClearedAftWpt().last()) || latTab.isLatModeChanged() || latTab.isAfterWptChanged() || latTab.isAfterWptHdgChanged()) {
                        labelText[5] = LatTab.afterWpt + "=>" + LatTab.afterWptHdg;
                        changed = latTab.isLatModeChanged() || latTab.isAfterWptChanged() || latTab.isAfterWptHdgChanged();
                    } else {
                        labelText[5] = "";
                    }
                } else if (LatTab.latMode.contains("heading")) {
                    if (aircraft.isLocCap()) {
                        labelText[5] = "LOC";
                    } else {
                        labelText[5] = Integer.toString(LatTab.clearedHdg);
                        changed = latTab.isLatModeChanged() || latTab.isHdgChanged();
                    }
                } else {
                    labelText[5] = "";
                }
                if (changed) labelText[5] = "[YELLOW]" + labelText[5] + "[WHITE]";
            } else {
                if (aircraft.getNavState().containsCode(aircraft.getNavState().getDispLatMode().last(), NavState.FLY_HEADING, NavState.TURN_LEFT, NavState.TURN_RIGHT)) {
                    if (aircraft.isLocCap()) {
                        labelText[5] = "LOC";
                    } else {
                        labelText[5] = Integer.toString(aircraft.getNavState().getClearedHdg().last());
                    }
                } else if (aircraft.getNavState().getDispLatMode().last() == NavState.HOLD_AT) {
                    labelText[5] = aircraft.getHoldWpt().getName();
                } else if (aircraft.getNavState().getClearedDirect().last().equals(aircraft.getNavState().getClearedAftWpt().last()) && aircraft.getNavState().getDispLatMode().last() == NavState.AFTER_WAYPOINT_FLY_HEADING) {
                    labelText[5] = aircraft.getNavState().getClearedDirect().last().getName() + "=>" + aircraft.getNavState().getClearedAftWptHdg().last();
                } else {
                    labelText[5] = "";
                }
            }
            if (aircraft.isSelected() && aircraft.isArrivalDeparture()) {
                boolean changed = false;
                if (LatTab.latMode.contains("arrival") || LatTab.latMode.contains("departure")) {
                    labelText[8] = LatTab.latMode.split(" ")[0];
                    changed = latTab.isLatModeChanged() && aircraft.getNavState().getDispLatMode().last() != NavState.AFTER_WAYPOINT_FLY_HEADING && aircraft.getNavState().getDispLatMode().last() != NavState.HOLD_AT;
                } else if ("After waypoint, fly heading".equals(LatTab.latMode) || "Hold at".equals(LatTab.latMode)) {
                    labelText[8] = aircraft.getSidStar().getName();
                } else if (LatTab.latMode.contains("heading")) {
                    if ("Not cleared approach".equals(LatTab.clearedILS)) {
                        if (!aircraft.getEmergency().isEmergency() || !aircraft.getEmergency().isActive()) {
                            labelText[8] = aircraft.getSidStar().getName();
                        } else {
                            labelText[8] = "";
                        }
                    } else {
                        labelText[8] = LatTab.clearedILS;
                        changed = latTab.isLatModeChanged() || latTab.isIlsChanged();
                    }
                } else if ("Change STAR".equals(LatTab.latMode)) {
                    labelText[8] = LatTab.newStar.split(" ")[0];
                    changed = latTab.isStarChanged();
                } else {
                    labelText[8] = "";
                }
                if (changed) labelText[8] = "[YELLOW]" + labelText[8] + "[WHITE]";
            } else {
                if (aircraft.getNavState().containsCode(aircraft.getNavState().getDispLatMode().last(), NavState.FLY_HEADING, NavState.TURN_LEFT, NavState.TURN_RIGHT) && aircraft.getNavState().getClearedIls().last() != null) {
                    labelText[8] = aircraft.getNavState().getClearedIls().last().getName();
                } else {
                    labelText[8] = "";
                    if (!aircraft.getEmergency().isEmergency() || !aircraft.getEmergency().isActive()) {
                        labelText[8] = aircraft.getSidStar().getName();
                    }
                }
            }

            if (!minimized && aircraft.isArrivalDeparture()) {
                updatedText = labelText[0] + " " + labelText[1] + "\n" + labelText[2] + vertSpd + labelText[3] + exped + labelText[10] + "\n" + labelText[5] + (labelText[5] == null || labelText[5].length() == 0 ? "" : " ") + labelText[8] + "\n" + labelText[6] + " " + labelText[7] + " " + labelText[9];
            } else {
                if (System.currentTimeMillis() % 4000 >= 2000) {
                    updatedText = labelText[0] + "/" + aircraft.getRecat() + "\n" + labelText[2] + "  " + labelText[6];
                } else {
                    String clearedAltStr = labelText[10];
                    if (aircraft.isGsCap()) {
                        clearedAltStr = aircraft.getIls().getName().contains("IMG") ? "VIS" : "GS";
                    } else if (aircraft.isLocCap() && (aircraft.getIls().isNpa())) {
                        clearedAltStr = "NPA";
                    }
                    updatedText = labelText[0] + "/" + aircraft.getRecat() + "\n" + clearedAltStr + "  " + aircraft.getIcaoType();
                }
            }
        } else {
            if (aircraft.isSelected() && aircraft.isArrivalDeparture()) {
                boolean changed = false;
                if (LatTab.latMode.contains("heading") && !"After waypoint, fly heading".equals(LatTab.latMode)) {
                    if (aircraft.isLocCap()) {
                        labelText[5] = "LOC";
                    } else {
                        labelText[5] = Integer.toString(LatTab.clearedHdg);
                        changed = latTab.isLatModeChanged() || latTab.isHdgChanged();
                    }
                } else if ("Hold at".equals(LatTab.latMode)) {
                    labelText[5] = LatTab.holdWpt;
                    changed = latTab.isLatModeChanged() || latTab.isHoldWptChanged();
                } else if (LatTab.latMode.contains(aircraft.getSidStar().getName()) || "After waypoint, fly heading".equals(LatTab.latMode)) {
                    if ("After waypoint, fly heading".equals(LatTab.latMode) && ((aircraft.getDirect() != null && LatTab.afterWpt.equals(aircraft.getDirect().getName())) || latTab.isAfterWptChanged() || latTab.isAfterWptHdgChanged())) {
                        labelText[5] = LatTab.afterWpt + "=>" + LatTab.afterWptHdg;
                        changed = latTab.isLatModeChanged() || latTab.isAfterWptHdgChanged() || latTab.isAfterWptChanged();
                    } else {
                        labelText[5] = LatTab.clearedWpt;
                        changed = latTab.isWptChanged();
                    }
                }
                if (changed) labelText[5] = "[YELLOW]" + labelText[5] + "[WHITE]";
                changed = false;
                if (!"Not cleared approach".equals(LatTab.clearedILS)) {
                    labelText[8] = LatTab.clearedILS;
                    changed = latTab.isIlsChanged() || latTab.isLatModeChanged();
                } else if ("Change STAR".equals(LatTab.latMode)) {
                    labelText[8] = LatTab.newStar.split(" ")[0];
                    changed = latTab.isStarChanged();
                } else {
                    labelText[8] = "";
                    if (!aircraft.getEmergency().isEmergency() || !aircraft.getEmergency().isActive()) {
                        labelText[8] = aircraft.getSidStar().getName();
                        changed = latTab.isLatModeChanged() && LatTab.latMode.contains(aircraft.getSidStar().getName()) && aircraft.getNavState().getDispLatMode().last() != NavState.AFTER_WAYPOINT_FLY_HEADING && aircraft.getNavState().getDispLatMode().last() != NavState.HOLD_AT;
                    }
                }
                if (changed) labelText[8] = "[YELLOW]" + labelText[8] + "[WHITE]";
            } else {
                if (aircraft.getNavState().containsCode(aircraft.getNavState().getDispLatMode().first(), NavState.FLY_HEADING, NavState.TURN_RIGHT, NavState.TURN_LEFT)) {
                    if (aircraft.isLocCap()) {
                        labelText[5] = "LOC";
                    } else {
                        labelText[5] = Integer.toString(aircraft.getNavState().getClearedHdg().last());
                    }
                } else if (aircraft.getNavState().getDispLatMode().last() == NavState.HOLD_AT) {
                    if (aircraft.isHolding() || (aircraft.getDirect() != null && aircraft.getHoldWpt() != null && aircraft.getDirect().equals(aircraft.getHoldWpt()))) {
                        labelText[5] = aircraft.getHoldWpt().getName();
                    } else if (aircraft.getDirect() != null) {
                        labelText[5] = aircraft.getDirect().getName();
                    }
                } else if (aircraft.getNavState().containsCode(aircraft.getNavState().getDispLatMode().last(), NavState.SID_STAR, NavState.AFTER_WAYPOINT_FLY_HEADING)) {
                    if (aircraft.getNavState().getClearedDirect().last().equals(aircraft.getNavState().getClearedAftWpt().last()) && aircraft.getNavState().getDispLatMode().last() == NavState.AFTER_WAYPOINT_FLY_HEADING) {
                        labelText[5] = aircraft.getNavState().getClearedDirect().last().getName() + "=>" + aircraft.getNavState().getClearedAftWptHdg().last();
                    } else {
                        labelText[5] = aircraft.getNavState().getClearedDirect().last().getName();
                    }
                }
                if (aircraft.getNavState().getClearedIls().last() != null) {
                    labelText[8] = aircraft.getNavState().getClearedIls().last().getName();
                } else {
                    labelText[8] = "";
                    if (!aircraft.getEmergency().isEmergency() || !aircraft.getEmergency().isActive()) {
                        labelText[8] = aircraft.getSidStar().getName();
                    }
                }
            }


            if (!minimized && aircraft.isArrivalDeparture()) {
                updatedText = labelText[0] + " " + labelText[1] + "\n" + labelText[2] + vertSpd + labelText[3] + exped + labelText[10] + "\n" + labelText[4] + " " + labelText[5] + (labelText[5] == null || labelText[5].length() == 0 ? "" : " ") + labelText[8] + "\n" + labelText[6] + " " + labelText[7] + " " + labelText[9];
            } else {
                updatedText = labelText[0] + "/" + aircraft.getRecat() + "\n" + labelText[2] + " " + labelText[4] + "\n" + labelText[6];
            }
        }

        if (aircraft.getEmergency().isActive()) {
            if (aircraft.getEmergency().isReadyForApproach() && aircraft.getEmergency().isStayOnRwy()) {
                updatedText = updatedText + "\nStay on rwy";
            } else if (!aircraft.getEmergency().isReadyForApproach() && aircraft.getEmergency().isChecklistsSaid() && aircraft.getEmergency().isFuelDumpRequired()) {
                if (aircraft.getEmergency().isDumpingFuel()) {
                    updatedText = updatedText + "\nDumping fuel";
                } else {
                    updatedText = updatedText + "\nFuel dump req";
                }
            }
        } else if (aircraft.isWakeInfringe()) {
            updatedText = updatedText + "\nWake alert";
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
