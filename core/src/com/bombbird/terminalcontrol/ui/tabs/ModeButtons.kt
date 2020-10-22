package com.bombbird.terminalcontrol.ui.tabs;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.NavState;
import com.bombbird.terminalcontrol.ui.Ui;
import com.bombbird.terminalcontrol.utilities.Fonts;

import java.util.HashMap;
import java.util.HashSet;

public class ModeButtons {
    private final Tab tab;
    private int mode;

    private final HashMap<Integer, TextButton> buttons;
    private final HashSet<Integer> inactiveButtons;

    public ModeButtons(Tab tab) {
        this.tab = tab;
        buttons = new HashMap<>();
        inactiveButtons = new HashSet<>();
    }

    /** Initializes the button with its mode code and display text */
    public void addButton(int code, String text) {
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = Fonts.defaultFont20;
        textButtonStyle.fontColor = Color.BLACK;
        textButtonStyle.up = Ui.lightBoxBackground;
        textButtonStyle.down = Ui.lightBoxBackground;

        TextButton button = new TextButton(text, textButtonStyle);
        button.setName(String.valueOf(code));
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!isInactive(Integer.parseInt(actor.getName()))) {
                    mode = Integer.parseInt(actor.getName());
                    tab.choiceMade();
                }
                event.handle();
            }
        });

        int column = buttons.size() % 3;
        int row = buttons.size() / 3;
        tab.addActor(button, 0.1f + column * 0.275f, 0.25f, 2240 - 325 * row, 300);
        button.getLabel().setWrap(true);

        buttons.put(code, button);
    }

    /** Changes the text on the button */
    public void changeButtonText(int code, String text) {
        if (!buttons.containsKey(code)) return;
        buttons.get(code).setText(text);
    }

    /** Sets the currently selected button font as yellow when mode has changed */
    public void setButtonColour(boolean modeChanged) {
        for (TextButton textButton: buttons.values()) {
            if (textButton.getName().equals(Integer.toString(mode))) {
                textButton.getStyle().down = TerminalControl.skin.getDrawable("Button_down");
                textButton.getStyle().up = TerminalControl.skin.getDrawable("Button_down");
                textButton.getStyle().fontColor = modeChanged ? Color.YELLOW : Color.WHITE;
            } else {
                textButton.getStyle().fontColor = Color.BLACK;
            }
        }
    }

    /** Checks whether the mode for each button exists in the aircraft's allowed modes, sets activity */
    public void updateButtonActivity(Array<String> modes) {
        HashSet<Integer> allowedModes = new HashSet<>();
        for (String mode: modes) {
            allowedModes.add(NavState.getCodeFromString(mode));
        }

        for (TextButton textButton: buttons.values()) {
            int code = Integer.parseInt(textButton.getName());
            if (allowedModes.contains(code)) {
                setActive(code);
            } else {
                setInactive(code);
            }
        }
    }

    /** Sets the button with the code to be inactive (greyed out) */
    public void setInactive(int button) {
        inactiveButtons.add(button);
        buttons.get(button).getStyle().down = TerminalControl.skin.getDrawable("ListBackground");
        buttons.get(button).getStyle().up = TerminalControl.skin.getDrawable("ListBackground");
    }

    /** Sets the button with the code to be active */
    public void setActive(int button) {
        inactiveButtons.remove(button);
        buttons.get(button).getStyle().down = Ui.lightBoxBackground;
        buttons.get(button).getStyle().up = Ui.lightBoxBackground;
    }

    /** Checks whether the button with the code is inactive */
    private boolean isInactive(int button) {
        return inactiveButtons.contains(button);
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }
}
