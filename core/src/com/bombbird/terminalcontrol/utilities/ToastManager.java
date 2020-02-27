package com.bombbird.terminalcontrol.utilities;

import com.badlogic.gdx.utils.GdxRuntimeException;

public interface ToastManager {
    void saveFail(GdxRuntimeException e);

    void readStorageFail();

    void jsonParseFail();
}
