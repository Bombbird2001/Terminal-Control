package com.bombbird.terminalcontrol.screens.upgradescreen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.achievements.Achievement;
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager;
import com.bombbird.terminalcontrol.screens.MainMenuScreen;
import com.bombbird.terminalcontrol.utilities.Fonts;

import java.util.Map;

public class AchievementScreen extends UpgradeScreen {

    public AchievementScreen(TerminalControl game, Image background) {
        super(game, background);
    }

    @Override
    public void loadLabel() {
        //Set title label style
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont20;
        labelStyle.fontColor = Color.WHITE;

        //Set title label
        Label headerLabel = new Label("Achievements", labelStyle);
        headerLabel.setWidth(MainMenuScreen.BUTTON_WIDTH);
        headerLabel.setHeight(MainMenuScreen.BUTTON_HEIGHT);
        headerLabel.setPosition(2880 / 2.0f - MainMenuScreen.BUTTON_WIDTH / 2.0f, 1620 * 0.85f);
        headerLabel.setAlignment(Align.center);
        stage.addActor(headerLabel);
    }

    /** Loads the scrollpane used to contain unlocks, milestones */
    @Override
    public void loadScroll() {
        scrollTable = new Table();
        ScrollPane scrollPane = new ScrollPane(scrollTable);

        scrollPane.setX(2880 / 2f - MainMenuScreen.BUTTON_WIDTH * 0.8f);
        scrollPane.setY(1620 * 0.25f);
        scrollPane.setWidth(MainMenuScreen.BUTTON_WIDTH * 1.6f);
        scrollPane.setHeight(1620 * 0.55f);
        scrollPane.getStyle().background = TerminalControl.skin.getDrawable("ListBackground");

        stage.addActor(scrollPane);
    }

    /** Loads the achievements into scroll pane */
    @Override
    public void loadUnlocks() {
        //Set scroll pane label textures
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont12;
        labelStyle.fontColor = Color.WHITE;
        labelStyle.background = TerminalControl.skin.getDrawable("Button_up");

        for (Map.Entry<String, Achievement> entry: UnlockManager.achievementList.entrySet()) {
            Label label = new Label("\n[ " + entry.getValue().getTitle() + " ]\n" + entry.getValue().getDescription() + "\n", labelStyle);
            label.setWrap(true);
            label.setAlignment(Align.center);
            scrollTable.add(label).width(MainMenuScreen.BUTTON_WIDTH * 1.1f);
            //Layout twice to set correct width & height
            scrollTable.layout();
            scrollTable.layout();
            int value = entry.getValue().getCurrentValue();
            int needed = entry.getValue().getValueNeeded();
            Label label1 = new Label(value >= 0 ? Math.min(value, needed) + "/" + needed : "- / -", labelStyle);
            label1.setAlignment(Align.center);
            scrollTable.add(label1).width(MainMenuScreen.BUTTON_WIDTH * 0.3f).height(label.getHeight());
            Image image = new Image(TerminalControl.skin.getDrawable(entry.getValue().isUnlocked() ? "Checked" : "Unchecked"));
            float ratio = MainMenuScreen.BUTTON_WIDTH * 0.15f / image.getWidth();
            scrollTable.add(image).width(MainMenuScreen.BUTTON_WIDTH * 0.15f).height(ratio * image.getHeight()).padLeft(MainMenuScreen.BUTTON_WIDTH * 0.025f).padRight(MainMenuScreen.BUTTON_WIDTH * 0.025f);
            scrollTable.row();
        }

        for (Map.Entry<String, String> entry: UnlockManager.easterEggList.entrySet()) {
            boolean unlocked = UnlockManager.unlocks.contains(entry.getKey());
            Label label = new Label("\n[ " + entry.getKey() + " ]\n" + (unlocked ? entry.getValue() : "?????")+ "\n", labelStyle);
            label.setWrap(true);
            label.setAlignment(Align.center);
            scrollTable.add(label).width(MainMenuScreen.BUTTON_WIDTH * 1.1f);
            //Layout twice to set correct width & height
            scrollTable.layout();
            scrollTable.layout();
            Label label1 = new Label("- / -", labelStyle);
            label1.setAlignment(Align.center);
            scrollTable.add(label1).width(MainMenuScreen.BUTTON_WIDTH * 0.3f).height(label.getHeight());
            Image image = new Image(TerminalControl.skin.getDrawable(unlocked ? "Checked" : "Unchecked"));
            float ratio = MainMenuScreen.BUTTON_WIDTH * 0.15f / image.getWidth();
            scrollTable.add(image).width(MainMenuScreen.BUTTON_WIDTH * 0.15f).height(ratio * image.getHeight()).padLeft(MainMenuScreen.BUTTON_WIDTH * 0.025f).padRight(MainMenuScreen.BUTTON_WIDTH * 0.025f);
            scrollTable.row();
        }
    }
}
