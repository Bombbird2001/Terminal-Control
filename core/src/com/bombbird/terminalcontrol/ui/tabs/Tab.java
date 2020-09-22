package com.bombbird.terminalcontrol.ui.tabs;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Queue;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.ui.Ui;
import com.bombbird.terminalcontrol.utilities.ErrorHandler;
import com.bombbird.terminalcontrol.utilities.Fonts;

public class Tab {
    public SelectBox<String> settingsBox;
    public SelectBox<String> valueBox;
    public Aircraft selectedAircraft;
    public static boolean notListening;
    public static Ui ui;

    public static final float boxHeight = 320;

    public static List.ListStyle listStyle;
    public static ScrollPane.ScrollPaneStyle paneStyle;

    public static String latMode;
    public static int clearedHdg;
    public static String clearedWpt;
    public static int afterWptHdg;
    public static String afterWpt;
    public static String holdWpt;
    public static String clearedILS;
    public static String newStar;

    public static String altMode;
    public static int clearedAlt;

    public static String spdMode;
    public static int clearedSpd;

    public boolean tabChanged;

    public boolean visible;

    public Queue<Object[]> infoQueue;

    private static boolean LOADED_STYLES = false;

    public Tab(Ui Ui) {
        ui = Ui;
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

    private void loadSelect() {
        SelectBox.SelectBoxStyle boxStyle = new SelectBox.SelectBoxStyle();
        boxStyle.font = Fonts.defaultFont20;
        boxStyle.fontColor = Color.WHITE;
        boxStyle.listStyle = listStyle;
        boxStyle.scrollStyle = paneStyle;
        boxStyle.background = Ui.lightBoxBackground;

        //Settings box for modes
        settingsBox = new SelectBox<>(boxStyle);
        settingsBox.setAlignment(Align.center);
        settingsBox.getList().setAlignment(Align.center);
        settingsBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
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
                event.handle();
            }
        });
        ui.addActor(settingsBox, 0.1f, 0.8f, 3240 - 1020, boxHeight);

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
                event.handle();
            }
        });
        ui.addActor(valueBox, 0.1f, 0.8f, 3240 - 1620, boxHeight);
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
        settingsBox.setVisible(show);
        valueBox.setVisible(show);
        notListening = false;
    }

    public float getPaneWidth() {
        return ui.getPaneWidth();
    }

    public SelectBox<String> getSettingsBox() {
        return settingsBox;
    }

    public SelectBox<String> getValueBox() {
        return valueBox;
    }

    public static void setLoadedStyles(boolean loadedStyles) {
        LOADED_STYLES = loadedStyles;
    }
}
