package com.bombbird.terminalcontrol.screens.selectgamescreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.screens.BasicScreen;
import com.bombbird.terminalcontrol.screens.MainMenuScreen;
import com.bombbird.terminalcontrol.utilities.Fonts;

public class SelectGameScreen extends BasicScreen {
    private final Table scrollTable;

    public TextButton backButton;

    //Styles
    private Label.LabelStyle labelStyle;
    public TextButton.TextButtonStyle buttonStyle;

    //Background image (from MainMenuScreen)
    public Image background;

    public SelectGameScreen(final TerminalControl game, Image background) {
        super(game, 2880, 1620);

        //Set table params (for scrollpane)
        scrollTable = new Table();

        //Set background image to that shown on main menu screen
        this.background = background;
    }

    /** Loads the full UI of this screen */
    private void loadUI() {
        //Reset stage
        stage.clear();

        stage.addActor(background);

        loadLabel();
        loadButtons();
        loadScroll();
    }

    /** Loads the appropriate labelStyle, and is overridden to load a label with the appropriate text */
    public void loadLabel() {
        //Set label style
        labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont20;
        labelStyle.fontColor = Color.WHITE;
    }

    /** Loads the default button styles and back button */
    public void loadButtons() {
        //Set button textures
        buttonStyle = new TextButton.TextButtonStyle();
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
                game.setScreen(new MainMenuScreen(game, background));
            }
        });

        stage.addActor(backButton);
    }

    /** Loads the contents of the scrollPane */
    public void loadScroll() {
        //No default implementation
    }

    /** Implements show method of screen */
    @Override
    public void show() {
        loadUI();
    }

    /** Overrides render method to include detection of back button on android */
    @Override
    public void render(float delta) {
        super.render(delta);
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            //On android, emulate backButton is pressed
            backButton.toggle();
        }
    }

    public Label.LabelStyle getLabelStyle() {
        return labelStyle;
    }

    public TextButton.TextButtonStyle getButtonStyle() {
        return buttonStyle;
    }

    public Stage getStage() {
        return stage;
    }

    public Table getScrollTable() {
        return scrollTable;
    }
}
