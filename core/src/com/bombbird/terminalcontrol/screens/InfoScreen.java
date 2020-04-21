package com.bombbird.terminalcontrol.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.utilities.Fonts;

public class InfoScreen extends StandardUIScreen {
    public InfoScreen(final TerminalControl game, Image background) {
        super(game, background);
    }

    /** Loads the full UI of this screen */
    public void loadUI() {
        super.loadUI();
        loadLabel();
        loadButtons();
    }

    /** Loads labels for credits, disclaimers, etc */
    public void loadLabel() {
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont12;
        labelStyle.fontColor = Color.WHITE;

        Label copyright = new Label("Terminal Control" + (TerminalControl.full ? "" : ": Lite") + "\nCopyright \u00a9 2018-2020, Bombbird\nVersion " + TerminalControl.versionName + ", build " + TerminalControl.versionCode, labelStyle);
        copyright.setPosition(918, 1375);
        stage.addActor(copyright);

        Label licenses = new Label("Open source software/libraries used:\n\n" +
                "libGDX - Apache License 2.0\n" +
                "JSON In Java - JSON License\n" +
                "OkHttp3 - Apache License 2.0\n" +
                "Apache Commons Lang - Apache License 2.0\n" +
                "Open Sans font - Apache License 2.0", labelStyle);
        licenses.setPosition(1440 - licenses.getWidth() / 2f, 825);
        stage.addActor(licenses);

        Label disclaimer = new Label("While we make effort to ensure that this game is as realistic as possible, " +
                "please note that this game is not a completely accurate representation of real life air traffic control " +
                "and should not be used for purposes such as real life training. SID, STAR and other navigation data are fictitious " +
                "and should never be used for real life navigation. Names used are fictional, any resemblance to real world entities " +
                "is purely coincidental.", labelStyle);
        disclaimer.setWrap(true);
        disclaimer.setWidth(2400);
        disclaimer.setPosition(1465 - disclaimer.getWidth() / 2f, 460);
        stage.addActor(disclaimer);
    }
}
