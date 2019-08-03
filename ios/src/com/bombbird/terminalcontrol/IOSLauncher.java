package com.bombbird.terminalcontrol;

import com.bombbird.terminalcontrol.utilities.ToastManager;
import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.uikit.UIApplication;

import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration;

public class IOSLauncher extends IOSApplication.Delegate {
    @Override
    protected IOSApplication createApplication() {
        IOSApplicationConfiguration config = new IOSApplicationConfiguration();
        return new IOSApplication(new TerminalControl(new TextToSpeechManager(), new ToastManager() {
            @Override
            public void saveFail() {
                //No default implementation
            }

            @Override
            public void readStorageFail() {
                //No default implementation
            }

            @Override
            public void jsonParseFail() {
                //No default implementation
            }
        }), config);
    }

    public static void main(String[] argv) {
        NSAutoreleasePool pool = new NSAutoreleasePool();
        UIApplication.main(argv, null, IOSLauncher.class);
        pool.close();
    }
}