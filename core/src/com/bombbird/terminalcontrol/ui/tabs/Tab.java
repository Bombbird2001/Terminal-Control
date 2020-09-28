package com.bombbird.terminalcontrol.ui.tabs;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen;
import com.bombbird.terminalcontrol.ui.Ui;
import com.bombbird.terminalcontrol.utilities.ErrorHandler;
import com.bombbird.terminalcontrol.utilities.Fonts;

public class Tab {
    public SelectBox<String> valueBox;
    public Aircraft selectedAircraft;
    public static boolean notListening;
    public static Ui ui;

    public static final float boxHeight = 320;

    public static List.ListStyle listStyle;
    public static ScrollPane.ScrollPaneStyle paneStyle;

    public static int latMode;
    public static int clearedHdg;
    public static String clearedWpt;
    public static int afterWptHdg;
    public static String afterWpt;
    public static String holdWpt;
    public static String clearedILS;
    public static String newStar;
    public static int turnDir;

    public static int altMode;
    public static int clearedAlt;
    public static boolean clearedExpedite;

    public static int spdMode;
    public static int clearedSpd;

    public boolean tabChanged;

    public boolean visible;

    public Queue<Object[]> infoQueue;

    public final ModeButtons modeButtons;

    private static boolean LOADED_STYLES = false;

    private final Array<Actor> hideArray;

    private final RadarScreen radarScreen;

    public Tab(Ui ui) {
        radarScreen  = TerminalControl.radarScreen;

        modeButtons = new ModeButtons(this);
        hideArray = new Array<>();

        Tab.ui = ui;
        notListening = false;
        visible = false;
        if (!LOADED_STYLES) {
            loadStyles();
            LOADED_STYLES = true;
        }
        loadSelect();
        infoQueue = new Queue<>();
    }

    private void loadStyles() {
        paneStyle = new ScrollPane.ScrollPaneStyle();
        paneStyle.background = TerminalControl.skin.getDrawable("ListBackground");

        listStyle = new List.ListStyle();
        listStyle.font = Fonts.defaultFont20;
        listStyle.fontColorSelected = Color.WHITE;
        listStyle.fontColorUnselected = Color.BLACK;
        Drawable button_down = TerminalControl.skin.getDrawable("Button_down");
        button_down.setTopHeight(75);
        button_down.setBottomHeight(75);
        listStyle.selection = button_down;
    }

    public void choiceMade() {
        if (!notListening && selectedAircraft != null) {
            try {
                getChoices();
                updateElements();
                compareWithAC();
                updateElementColours();
            } catch (Exception e) {
                ErrorHandler.sendGenericError(e, false);
            }
        }
    }

    private void loadSelect() {
        SelectBox.SelectBoxStyle boxStyle = new SelectBox.SelectBoxStyle();
        boxStyle.font = Fonts.defaultFont20;
        boxStyle.fontColor = Color.WHITE;
        boxStyle.listStyle = listStyle;
        boxStyle.scrollStyle = paneStyle;
        boxStyle.background = Ui.lightBoxBackground;

        SelectBox.SelectBoxStyle boxStyle2 = new SelectBox.SelectBoxStyle();
        boxStyle2.font = Fonts.defaultFont20;
        boxStyle2.fontColor = Color.WHITE;
        boxStyle2.listStyle = listStyle;
        boxStyle2.scrollStyle = paneStyle;
        boxStyle2.background = Ui.lightBoxBackground;

        //Valuebox for waypoint/altitude/speed selections
        valueBox = new SelectBox<>(boxStyle2);
        valueBox.setAlignment(Align.center);
        valueBox.getList().setAlignment(Align.center);
        valueBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                choiceMade();
                event.handle();
            }
        });
        addActor(valueBox, 0.1f, 0.8f, 3240 - 1670, boxHeight);
    }

    public void addActor(Actor actor, float xRatio, float widthRatio, float y, float height) {
        actor.setPosition(xRatio * getPaneWidth(), y);
        actor.setSize(widthRatio * getPaneWidth(), height);
        radarScreen.uiStage.addActor(actor);
        hideArray.add(actor);
        radarScreen.ui.actorArray.add(actor);
        radarScreen.ui.actorXArray.add(xRatio);
        radarScreen.ui.actorWidthArray.add(widthRatio);
    }

    public void updateElements() {
        //Overridden method for updating each tab's elements
    }

    public void compareWithAC() {
        //Overridden method for comparing each tab's states with AC
    }

    public void updateElementColours() {
        //Overridden method for updating each tab's elements' colours
        ui.updateResetColours();
    }

    public void updateMode() {
        //Overridden method for updating aircraft mode
    }

    public void resetTab() {
        //Overridden method for updating aircraft mode
        notListening = true;
        getACState();
        updateElements();
        compareWithAC();
        updateElementColours();
        notListening = false;
    }

    public void getACState() {
        //Overridden method for getting current AC status
    }

    public void getChoices() {
        //Overridden method for getting choices from selectboxes
    }

    public void setVisibility(boolean show) {
        notListening = true;
        visible = show;
        for (Actor actor: hideArray) {
            actor.setVisible(show);
        }
        notListening = false;
    }

    public float getPaneWidth() {
        return ui.getPaneWidth();
    }

    public SelectBox<String> getValueBox() {
        return valueBox;
    }

    public static void setLoadedStyles(boolean loadedStyles) {
        LOADED_STYLES = loadedStyles;
    }
}
