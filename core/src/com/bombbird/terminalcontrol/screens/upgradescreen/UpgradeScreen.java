package com.bombbird.terminalcontrol.screens.upgradescreen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager;
import com.bombbird.terminalcontrol.screens.MainMenuScreen;
import com.bombbird.terminalcontrol.screens.PauseScreen;
import com.bombbird.terminalcontrol.screens.StandardUIScreen;
import com.bombbird.terminalcontrol.utilities.Fonts;

import java.util.Map;

public class UpgradeScreen extends StandardUIScreen {
    //Scroll table
    public Table scrollTable;

    public UpgradeScreen(final TerminalControl game, Image background) {
        super(game, background);
    }

    /** Loads the UI elements to be rendered on screen */
    public void loadUI() {
        super.loadUI();

        loadButtons();
        loadLabel();
        loadScroll();
        loadUnlocks();
    }

    /** Loads back button */
    public void loadButtons() {
        //Set button textures
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = Fonts.defaultFont12;
        buttonStyle.up = TerminalControl.skin.getDrawable("Button_up");
        buttonStyle.down = TerminalControl.skin.getDrawable("Button_down");

        //Set back button params
        backButton = new TextButton("<= Back", buttonStyle);
        backButton.setWidth(MainMenuScreen.BUTTON_WIDTH);
        backButton.setHeight(MainMenuScreen.BUTTON_HEIGHT);
        backButton.setPosition(2880 / 2.0f - MainMenuScreen.BUTTON_WIDTH / 2.0f, 1620 * 0.05f);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Go back to main menu
                if (TerminalControl.radarScreen == null) {
                    game.setScreen(new MainMenuScreen(game, background));
                } else {
                    game.setScreen(new PauseScreen(game, TerminalControl.radarScreen));
                }
            }
        });

        stage.addActor(backButton);
    }

    /** Loads the appropriate title for label */
    public void loadLabel() {
        super.loadLabel("Milestones & Unlocks");

        //Set additional description label style
        Label.LabelStyle labelStyle1 = new Label.LabelStyle();
        labelStyle1.font = Fonts.defaultFont12;
        labelStyle1.fontColor = Color.WHITE;

        //Set description label
        Label label = new Label("Once an option is unlocked, you can visit the settings page to change to the desired option.\nTotal planes landed: " + UnlockManager.getPlanesLanded(), labelStyle1);
        label.setPosition((2880 - label.getWidth()) / 2, 1620 * 0.75f);
        label.setAlignment(Align.center);
        stage.addActor(label);
    }

    /** Loads the scrollpane used to contain unlocks, milestones */
    public void loadScroll() {
        scrollTable = new Table();
        ScrollPane scrollPane = new ScrollPane(scrollTable);

        scrollPane.setX(2880 / 2f - MainMenuScreen.BUTTON_WIDTH * 0.8f);
        scrollPane.setY(1620 * 0.2f);
        scrollPane.setWidth(MainMenuScreen.BUTTON_WIDTH * 1.6f);
        scrollPane.setHeight(1620 * 0.5f);
        scrollPane.getStyle().background = TerminalControl.skin.getDrawable("ListBackground");

        stage.addActor(scrollPane);
    }

    /** Loads the unlocks into scroll pane */
    public void loadUnlocks() {
        //Set scroll pane label textures
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont12;
        labelStyle.fontColor = Color.WHITE;
        labelStyle.background = TerminalControl.skin.getDrawable("Button_up");

        for (Map.Entry<String, Integer> entry: UnlockManager.unlockList.entrySet()) {
            Label label = new Label("\n" + UnlockManager.unlockDescription.get(entry.getKey()) + "\n", labelStyle);
            label.setWrap(true);
            label.setAlignment(Align.center);
            scrollTable.add(label).width(MainMenuScreen.BUTTON_WIDTH * 1.1f);
            //Layout twice to set correct width & height
            scrollTable.layout();
            scrollTable.layout();
            int required = entry.getValue();
            Label label1 = new Label(Math.min(UnlockManager.getPlanesLanded(), required) + "/" + required, labelStyle);
            label1.setAlignment(Align.center);
            scrollTable.add(label1).width(MainMenuScreen.BUTTON_WIDTH * 0.3f).height(label.getHeight());
            Image image = new Image(TerminalControl.skin.getDrawable(UnlockManager.getPlanesLanded() >= required ? "Checked" : "Unchecked"));
            float ratio = MainMenuScreen.BUTTON_WIDTH * 0.15f / image.getWidth();
            scrollTable.add(image).width(MainMenuScreen.BUTTON_WIDTH * 0.15f).height(ratio * image.getHeight()).padLeft(MainMenuScreen.BUTTON_WIDTH * 0.025f).padRight(MainMenuScreen.BUTTON_WIDTH * 0.025f);
            scrollTable.row();
        }
    }
}
