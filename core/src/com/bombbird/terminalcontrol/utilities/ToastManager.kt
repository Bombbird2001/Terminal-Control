package com.bombbird.terminalcontrol.utilities;

import com.badlogic.gdx.utils.GdxRuntimeException;

public interface ToastManager {
    default void saveFail(GdxRuntimeException e) {
        //No default implementation
    }

    default void readStorageFail() {
        //No default implementation
    }

    default void jsonParseFail() {
        //No default implementation
    }
}
