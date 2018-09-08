package com.bombbird.atcsim.entities.aircrafts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import static com.bombbird.atcsim.screens.GameScreen.aircrafts;
import static com.bombbird.atcsim.screens.GameScreen.stage;

public class Aircraft extends Actor {
    //Rendering parameters
    private Label label;
    private boolean selected;
    private static boolean loadedIcons = false;
    public static TextureAtlas iconAtlas = new TextureAtlas(Gdx.files.internal("game/aircrafts/aircraftIcons.atlas"));
    public static Skin skin = new Skin();
    private ImageButton icon;
    private static ImageButton.ImageButtonStyle buttonStyleCtrl;
    private static ImageButton.ImageButtonStyle buttonStyleUnctrl;
    private static ImageButton.ImageButtonStyle buttonStyleEnroute;

    //Aircraft information
    //Aircraft characteristics
    String callsign;
    String icaoType;
    int wakeCat;
    int[] maxVertSpd;
    int minSpeed;
    int controlState;

    //Aircraft position
    float x;
    float y;
    String latMode;
    private int heading;
    private int track;

    //Altitude
    float altitude;
    int clearedAltitude;
    int targetAltitude;
    float verticalSpeed;
    String altMode;

    //Speed
    float ias;
    float gs;
    Vector2 deltaPosition;
    int clearedSpeed;
    int tagretSpeed;
    float deltaSpeed;
    String spdMode;

    Aircraft(String callsign, String icaoType, int wakeCat, int[] maxVertSpd, int minSpeed) {
        if (!loadedIcons) {
            skin.addRegions(iconAtlas);
            buttonStyleCtrl = new ImageButton.ImageButtonStyle();
            buttonStyleCtrl.imageUp = skin.getDrawable("aircraftControlled");
            buttonStyleCtrl.imageDown = skin.getDrawable("aircraftControlled");
            buttonStyleUnctrl = new ImageButton.ImageButtonStyle();
            buttonStyleUnctrl.imageUp = skin.getDrawable("aircraftNotControlled");
            buttonStyleUnctrl.imageDown = skin.getDrawable("aircraftNotControlled");
            buttonStyleEnroute = new ImageButton.ImageButtonStyle();
            buttonStyleEnroute.imageUp = skin.getDrawable("aircraftEnroute");
            buttonStyleEnroute.imageDown = skin.getDrawable("aircraftEnroute");
            loadedIcons = true;
        }

        x = 2880;
        y = 1620;
        this.callsign = callsign;
        stage.addActor(this);
        this.icaoType = icaoType;
        this.wakeCat = wakeCat;
        this.maxVertSpd = maxVertSpd;
        this.minSpeed = minSpeed;
        selected = false;
        icon = new ImageButton(buttonStyleUnctrl);
        icon.setSize(20, 20);
        icon.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                aircrafts.get(actor.getName()).setSelected(true);
                event.handle();
            }
        });
        stage.addActor(icon);
        icon.setName(callsign);
    }

    public void renderShape() {
        if (selected) {
            drawStar();
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        icon.setPosition(x, y);
        icon.setColor(Color.BLACK); //Icon doesn't draw without this for some reason
        icon.draw(batch, 1);
    }

    void drawStar() {

    }

    public void setControlState(int controlState) {
        this.controlState = controlState;
        if (controlState == -1) {
            icon.setStyle(buttonStyleEnroute);
        } else if (controlState == 0) {
            icon.setStyle(buttonStyleUnctrl);
        } else if (controlState == 1) {
            icon.setStyle(buttonStyleCtrl);
        } else {
            Gdx.app.log("Aircraft control state error", "Invalid control state " + controlState + " set!");
        }
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
