package com.bombbird.terminalcontrol.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.utilities.Fonts;

public class NotifScreen extends StandardUIScreen {
    private final Timer timer;

    private static final Array<String> notifArray = new Array<>();

    public NotifScreen(final TerminalControl game, Image background) {
        super(game, background);

        timer = new Timer();

        loadNotifs();
    }

    /** Loads all the notifications needed */
    private void loadNotifs() {
        notifArray.add("As part of a recommendation by an aviation authority, in order to reduce resemblance to real life airports, " +
                "airport names, airport ICAO codes, SID/STAR names, as well as waypoint names have been changed. However, the procedures " +
                "themselves remain the same and is still based on that of the real airport. Saves will remain compatible - existing planes " +
                "will continue using old SIDs and STARs, while newly generated ones will use the new SIDs and STARs. " +
                "Old saves that have not been loaded for 6 months or more may not be compatible. We apologise for the inconvenience, " +
                "and we thank you for your understanding and continued support of Terminal Control.");
    }

    /** Loads the full UI of this screen */
    public void loadUI() {
        super.loadUI();
        background.setVisible(false);

        loadLabel();
        loadButtons();
    }

    /** Loads labels for credits, disclaimers, etc */
    public void loadLabel() {
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont12;
        labelStyle.fontColor = Color.WHITE;

        Label notif = new Label("Hello players, here's a message from the developer:\n\n" + notifArray.get(TerminalControl.LATEST_REVISION - 1), labelStyle);
        notif.setWrap(true);
        notif.setWidth(2000);
        notif.setPosition(1465 - notif.getWidth() / 2f, 800);
        stage.addActor(notif);
    }

    /** Loads the default button styles and back button */
    public void loadButtons() {
        //Set button textures
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = Fonts.defaultFont12;
        buttonStyle.up = TerminalControl.skin.getDrawable("Button_up");
        buttonStyle.down = TerminalControl.skin.getDrawable("Button_down");

        //Set back button params
        TextButton backButton = new TextButton("Ok!", buttonStyle);
        backButton.setWidth(MainMenuScreen.BUTTON_WIDTH);
        backButton.setHeight(MainMenuScreen.BUTTON_HEIGHT);
        backButton.setPosition(2880 / 2.0f - MainMenuScreen.BUTTON_WIDTH / 2.0f, 1620 * 0.05f);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Go back to main menu
                TerminalControl.updateRevision();
                game.setScreen(new MainMenuScreen(game, background));
                dispose();
            }
        });
        backButton.setVisible(false);

        stage.addActor(backButton);

        timer.scheduleTask(new Timer.Task() {
            @Override
            public void run() {
                Gdx.app.postRunnable(() -> backButton.setVisible(true));
            }
        }, 5f);
    }
}
