package com.bombbird.atcsim.entities.aircrafts;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class Aircraft extends Actor {
    //Rendering parameters
    private Rectangle rectangle;
    private Label label;

    //Aircraft information
    //Aircraft characteristics
    String callsign;
    String icaoType;
    int wakeCat;
    int[] maxVertSpd;
    int minSpeed;

    //Aircraft position
    float x;
    float y;

    //Altitude
    float altitude;
    int clearedAltitude;
    int targetAltitude;
    float verticalSpeed;

    //Speed
    float ias;
    float gs;
    Vector2 deltaPosition;
    int clearedSpeed;
    int tagretSpeed;
    float deltaSpeed;
}
