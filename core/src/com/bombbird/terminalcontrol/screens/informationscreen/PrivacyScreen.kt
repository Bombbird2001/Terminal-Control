package com.bombbird.terminalcontrol.screens.informationscreen

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.screens.MainMenuScreen
import com.bombbird.terminalcontrol.screens.StandardUIScreen
import com.bombbird.terminalcontrol.utilities.Fonts

class PrivacyScreen(game: TerminalControl, background: Image?) : StandardUIScreen(game, background) {
    private val scrollTable: Table = Table()

    /** Loads the full UI of this screen  */
    override fun loadUI() {
        super.loadUI()
        loadLabel()
        loadScroll()
        loadContent()
        loadButtons()
    }

    /** Loads labels for credits, disclaimers, etc  */
    fun loadLabel() {
        super.loadLabel("Data & Privacy Policy")
    }

    private fun loadScroll() {
        val scrollPane = ScrollPane(scrollTable)
        scrollPane.x = 2880 / 2f - MainMenuScreen.BUTTON_WIDTH
        scrollPane.y = 1620 * 0.22f
        scrollPane.width = MainMenuScreen.BUTTON_WIDTH * 2f
        scrollPane.height = 1620 * 0.6f
        scrollPane.style.background = BaseDrawable()
        stage.addActor(scrollPane)
    }

    private fun loadContent() {
        var text = """
            Terminal Control collects the following data when the game accesses functionalities from our server, which are stored in server access logs:
            - Date, time of access
            - Public IP address
            
            If you enable the sending of crash reports or send a crash report when a game load error occurs, the following data are also sent to the server:
            - Game version
            - Date, time of error occurrence
            - Crash logs
            - Game save data (if game load error occurs)
        """.trimIndent()

        if (Gdx.app.type == Application.ApplicationType.Android) text += """
            
            
            The following data is also collected through Google LLC's Google Play Console when a crash occurs:
            - Device model, OS version
            - Game version
            - Date, time of occurrence
            - Crash logs
        """.trimIndent()

        text += "\n\nThe above data are used solely for the purpose of diagnosing and fixing errors, crashes, and bugs, and are not shared with any 3rd party entities."


        if (Gdx.app.type == Application.ApplicationType.Android) text += """
            
            If you sign in to Google Play Games, and/or use the cloud save feature, Google LLC may collect information used to identify you. See the privacy policy here: https://policies.google.com/privacy
        """.trimIndent()

        if (!TerminalControl.full && Gdx.app.type == Application.ApplicationType.Android) text += """
            
            
            
            Survey Serving Technology

            This app uses Pollfish SDK. Pollfish is an on-line survey platform, through which, anyone may conduct surveys. Pollfish collaborates with Publishers of applications for smartphones in order to have access to users of such applications and address survey questionnaires to them. When a user connects to this app, a specific set of user's device data (including Advertising ID, Device ID, other available electronic identifiers and further response meta-data is automatically sent, via our app, to Pollfish servers, in order for Pollfish to discern whether the user is eligible for a survey. For a full list of data received by Pollfish through this app, please read carefully the Pollfish respondent terms located at https://www.pollfish.com/terms/respondent. These data will be associated with your answers to the questionnaires whenever Pollfish sends such questionnaires to eligible users. Pollfish may share such data with third parties, clients and business partners, for commercial purposes. By downloading the application, you accept this privacy policy document and you hereby give your consent for the processing by Pollfish of the aforementioned data. Furthermore, you are informed that you may disable Pollfish operation at any time by visiting the following link https://www.pollfish.com/respondent/opt-out. We once more invite you to check the Pollfish respondent's terms of use, if you wish to have more detailed view of the way Pollfish works and with whom Pollfish may further share your data.
            
            
            Advertisement Serving
            
            This app uses Appodeal SDK. Appodeal is an online monetization platform, ad mediation being one of the services provided. When a user uses this app, some data may be sent to Appodeal's servers in order to provide advertising services. When you first view ads served by Appodeal on this app, you will be asked whether or not to provide consent for a more personalized ad experience. If you change your mind later, you may change your consent preferences in the game global settings. You may view the types of data collected as stated in Appodeal's privacy policy at https://appodeal.com/privacy-policy.
        """.trimIndent()

        val labelStyle = Label.LabelStyle()
        labelStyle.font = Fonts.defaultFont12
        labelStyle.fontColor = Color.WHITE

        val label = Label(text, labelStyle)
        label.wrap = true
        label.width = MainMenuScreen.BUTTON_WIDTH * 2f - 20
        scrollTable.add(label).width(MainMenuScreen.BUTTON_WIDTH * 2f - 40).pad(15f, 30f, 15f, 0f)
    }

    /** Overridden to go back to info screen instead of main menu screen */
    override fun loadButtons() {
        super.loadButtons()
        removeDefaultBackButtonChangeListener()
        backButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor?) {
                //Back to info screen
                game.screen = InfoScreen(game, background)
            }
        })
    }
}