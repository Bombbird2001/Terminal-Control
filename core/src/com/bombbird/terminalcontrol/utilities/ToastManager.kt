package com.bombbird.terminalcontrol.utilities

import com.badlogic.gdx.utils.GdxRuntimeException

interface ToastManager {
    fun saveFail(e: GdxRuntimeException) {
        //No default implementation
    }

    fun readStorageFail() {
        //No default implementation
    }

    fun jsonParseFail() {
        //No default implementation
    }
}