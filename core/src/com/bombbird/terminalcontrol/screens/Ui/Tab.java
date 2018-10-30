package com.bombbird.terminalcontrol.screens.Ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.utilities.Fonts;

public class Tab {
    public SelectBox<String> settingsBox;
    public SelectBox<String> valueBox;
    public Aircraft selectedAircraft;
    public static boolean notListening;
    public static Ui ui;

    public static String latMode;
    public static int clearedHdg;
    public static String clearedWpt;
    public static int afterWptHdg;
    public static String afterWpt;

    public static String altMode;
    public static int clearedAlt;

    public static String spdMode;
    public static int clearedSpd;

    public boolean tabChanged;
    private TextButton resetTab;

    public boolean visible;

    public Tab(Ui Ui) {
        ui = Ui;
        notListening = false;
        visible = false;
        loadSelect();
        loadResetButton();
    }

    private void loadSelect() {
        ScrollPane.ScrollPaneStyle scrollPaneStyle = new ScrollPane.ScrollPaneStyle();
        scrollPaneStyle.background = TerminalControl.skin.getDrawable("ListBackground");

        List.ListStyle listStyle = new List.ListStyle();
        listStyle.font = Fonts.defaultFont20;
        listStyle.fontColorSelected = Color.WHITE;
        listStyle.fontColorUnselected = Color.BLACK;
        Drawable button_down = TerminalControl.skin.getDrawable("Button_down");
        button_down.setTopHeight(50);
        button_down.setBottomHeight(50);
        listStyle.selection = button_down;

        SelectBox.SelectBoxStyle boxStyle = new SelectBox.SelectBoxStyle();
        boxStyle.font = Fonts.defaultFont20;
        boxStyle.fontColor = Color.WHITE;
        boxStyle.listStyle = listStyle;
        boxStyle.scrollStyle = scrollPaneStyle;
        boxStyle.background = Ui.hdgBoxBackgroundDrawable;

        //Settings box for modes
        settingsBox = new SelectBox<String>(boxStyle);
        settingsBox.setPosition(0.1f * getPaneWidth(), 3240 - 970);
        settingsBox.setSize(0.8f * getPaneWidth(), 270);
        settingsBox.setAlignment(Align.center);
        settingsBox.getList().setAlignment(Align.center);
        settingsBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!notListening) {
                    getChoices();
                    updateElements();
                    compareWithAC();
                    updateElementColours();
                }
                event.handle();
            }
        });
        RadarScreen.uiStage.addActor(settingsBox);

        SelectBox.SelectBoxStyle boxStyle2 = new SelectBox.SelectBoxStyle();
        boxStyle2.font = Fonts.defaultFont20;
        boxStyle2.fontColor = Color.WHITE;
        boxStyle2.listStyle = listStyle;
        boxStyle2.scrollStyle = scrollPaneStyle;
        boxStyle2.background = Ui.hdgBoxBackgroundDrawable;

        //Valuebox for waypoint/altitude/speed selections
        valueBox = new SelectBox<String>(boxStyle2);
        valueBox.setPosition(0.1f * getPaneWidth(), 3240 - 1570);
        valueBox.setSize(0.8f * getPaneWidth(), 270);
        valueBox.setAlignment(Align.center);
        valueBox.getList().setAlignment(Align.center);
        valueBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!notListening) {
                    getChoices();
                    updateElements();
                    compareWithAC();
                    updateElementColours();
                }
                event.handle();
            }
        });
        RadarScreen.uiStage.addActor(valueBox);
    }

    private void loadResetButton() {
        //Separate buttonstyle for reset tab
        TextButton.TextButtonStyle textButtonStyle2 = new TextButton.TextButtonStyle();
        textButtonStyle2.font = Fonts.defaultFont20;
        textButtonStyle2.fontColor = Color.BLACK;
        textButtonStyle2.up = Ui.hdgBoxBackgroundDrawable;
        textButtonStyle2.down = TerminalControl.skin.getDrawable("Button_down");

        //Undo this tab button
        resetTab = new TextButton("Undo\nthis tab", textButtonStyle2);
        resetTab.setSize(0.25f * getPaneWidth(), 370);
        resetTab.setPosition(0.375f * getPaneWidth(), 3240 - 3070);
        resetTab.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                resetTab();
                event.handle();
            }
        });
        RadarScreen.uiStage.addActor(resetTab);
    }

    public void updateElements() {
        //Overriden method for updating each tab's elements
    }

    public void compareWithAC() {
        //Overriden method for comparing each tab's states with AC
    }

    public void updateElementColours() {
        //Overriden method for updating each tab's elements' colours
        if (tabChanged) {
            resetTab.getStyle().fontColor = Color.YELLOW;
        } else {
            resetTab.getStyle().fontColor = Color.WHITE;
        }
        ui.updateResetColours();
    }

    public void updateMode() {
        //Overriden method for updating aircraft mode
    }

    public void resetTab() {
        //Overriden method for updating aircraft mode
        notListening = true;
        getACState();
        updateElements();
        compareWithAC();
        updateElementColours();
        notListening = false;
    }

    public void getACState() {
        //Overriden method for getting current AC status
    }

    public void getChoices() {
        //Overriden method for getting choices from selectboxes
    }

    public void updatePaneWidth(float paneWidth) {
        //Overriden method for updating width of each element
        float paneSize = 0.8f * paneWidth;
        float leftMargin = 0.1f * paneWidth;
        settingsBox.setSize(paneSize, 270);
        settingsBox.setX(leftMargin);
        valueBox.setSize(paneSize, 270);
        valueBox.setX(leftMargin);
        resetTab.setSize(paneWidth / 4, 370);
        resetTab.setX(leftMargin + 0.275f * paneWidth);
    }

    public void setVisibility(boolean show) {
        notListening = true;
        visible = show;
        settingsBox.setVisible(show);
        valueBox.setVisible(show);
        notListening = false;
        resetTab.setVisible(show);
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
}
