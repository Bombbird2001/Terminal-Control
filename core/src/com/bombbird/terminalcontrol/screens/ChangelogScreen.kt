package com.bombbird.terminalcontrol.screens

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.utilities.Fonts
import kotlin.collections.LinkedHashMap

class ChangelogScreen(game: TerminalControl, background: Image?) : StandardUIScreen(game, background) {
    companion object {
        private val changeLogContent = LinkedHashMap<String, LinkedHashMap<String, Array<String>>>()
        const val MINOR_WIDTH = 300f
        const val MAJOR_HEIGHT = 180f
        const val MAIN_BOX_WIDTH = MainMenuScreen.BUTTON_WIDTH * 2f - MINOR_WIDTH
        const val MAIN_BOX_HEIGHT = 1620 * 0.6f - MAJOR_HEIGHT
    }

    private val majorVersionTable = Table()
    private val minorVersionTable = Table()
    private val scrollTable = Table()

    private var currMajor: String? = null
    private var currMinor: String? = null
    private var prevMajor: String? = null
    private var prevMinor: String? = null
    private var latestMajor: String? = null
    private var latestMinor: String? = null

    /** Loads the full UI of this screen  */
    override fun loadUI() {
        super.loadUI()
        loadLabel("Changelog")
        loadScroll()
        loadContent()
        loadButtons()
    }

    private fun loadScroll() {
        val scrollPane = ScrollPane(scrollTable)
        scrollPane.x = 2880 / 2f - MainMenuScreen.BUTTON_WIDTH + MINOR_WIDTH
        scrollPane.y = 1620 * 0.22f
        scrollPane.width = MAIN_BOX_WIDTH
        scrollPane.height = MAIN_BOX_HEIGHT
        scrollPane.style.background = TerminalControl.skin.getDrawable("ListBackground")
        stage.addActor(scrollPane)

        val majorPane = ScrollPane(majorVersionTable)
        majorPane.x = 2880 / 2f - MainMenuScreen.BUTTON_WIDTH + MINOR_WIDTH
        majorPane.y = 1620 * 0.82f - MAJOR_HEIGHT
        majorPane.width = MAIN_BOX_WIDTH
        majorPane.height = MAJOR_HEIGHT
        majorPane.style.background = TerminalControl.skin.getDrawable("ListBackground")
        stage.addActor(majorPane)

        val minorPane = ScrollPane(minorVersionTable)
        minorPane.x = 2880 / 2f - MainMenuScreen.BUTTON_WIDTH
        minorPane.y = 1620 * 0.22f
        minorPane.width = MINOR_WIDTH
        minorPane.height = MAIN_BOX_HEIGHT
        minorPane.style.background = TerminalControl.skin.getDrawable("ListBackground")
        stage.addActor(minorPane)
    }

    private fun loadContent() {
        loadHashmapContent()

        //Set button textures
        val buttonStyle = TextButton.TextButtonStyle()
        buttonStyle.font = Fonts.defaultFont12
        buttonStyle.up = TerminalControl.skin.getDrawable("Button_up")
        buttonStyle.down = TerminalControl.skin.getDrawable("Button_up")

        for (majorVersion in changeLogContent.keys) {
            val button = TextButton(majorVersion, TextButton.TextButtonStyle(buttonStyle))
            button.addListener(object: ChangeListener() {
                override fun changed(event: ChangeEvent?, actor: Actor?) {
                    currMajor = button.text.toString()
                    currMinor = null
                    updateContent()
                }
            })
            majorVersionTable.add(button).height(MAJOR_HEIGHT - 30).width(MINOR_WIDTH).align(Align.top)
        }

        updateContent()
    }

    private fun updateContent() {
        if (currMajor == null) {
            for (majV in changeLogContent.keys) {
                currMajor = majV
                currMinor = null
                break
            }
        }
        if (currMinor == null) {
            changeLogContent[currMajor]?.let {
                for (minV in it.keys) {
                    currMinor = minV
                    break
                }
            }
        }

        if (currMajor == prevMajor && currMinor == prevMinor) return

        val buttonStyle = TextButton.TextButtonStyle()
        buttonStyle.font = Fonts.defaultFont12
        buttonStyle.up = TerminalControl.skin.getDrawable("Button_up_sharp")
        buttonStyle.down = TerminalControl.skin.getDrawable("Button_up_sharp")

        if (currMajor != prevMajor) {
            minorVersionTable.clear()
            changeLogContent[currMajor]?.let {
                for (minorVersion in it.keys) {
                    val button = TextButton(minorVersion, TextButton.TextButtonStyle(buttonStyle))
                    button.addListener(object: ChangeListener() {
                        override fun changed(event: ChangeEvent?, actor: Actor?) {
                            currMinor = button.text.toString()
                            updateContent()
                        }
                    })
                    minorVersionTable.add(button).width(MINOR_WIDTH).height(MAJOR_HEIGHT - 30).row()
                }
            }

            for (i in 0 until majorVersionTable.children.size) {
                (majorVersionTable.children[i] as? TextButton)?.let { button ->
                    val drawable = TerminalControl.skin.getDrawable(if (button.text.toString() == currMajor) "Button_down" else "Button_up")
                    button.style.up = drawable
                    button.style.down = drawable
                }
            }
        }
        if (currMinor != prevMinor) for (i in 0 until minorVersionTable.children.size) {
            (minorVersionTable.children[i] as? TextButton)?.let { button ->
                val drawable = TerminalControl.skin.getDrawable(if (button.text.toString() == currMinor) "Button_down_sharp" else "Button_up_sharp")
                button.style.up = drawable
                button.style.down = drawable
            }
        }

        prevMajor = currMajor
        prevMinor = currMinor

        val labelStyle = Label.LabelStyle()
        labelStyle.fontColor = Color.WHITE
        labelStyle.font = Fonts.defaultFont12
        val labelStyle1 = Label.LabelStyle()
        labelStyle1.fontColor = Color.BLACK
        labelStyle1.font = Fonts.defaultFont16

        scrollTable.clear()

        val versionLabel = Label("Version $currMajor.$currMinor${if (currMajor == latestMajor && currMinor == latestMinor) " (Current version)" else ""}", labelStyle1)
        versionLabel.setAlignment(Align.center)
        scrollTable.add(versionLabel).width(MAIN_BOX_WIDTH - 20).pad(15f, 10f, 15f, 0f)
        scrollTable.row()
        val label = Label("Loading...", labelStyle)
        val content = StringBuilder()
        changeLogContent[currMajor]?.get(currMinor)?.let {
            for (change in it) {
                content.append(change).append("\n")
            }
        }
        label.setText(content.toString())
        label.wrap = true
        label.width = MainMenuScreen.BUTTON_WIDTH * 2f - 20
        scrollTable.add(label).width(MAIN_BOX_WIDTH - 80).pad(15f, 50f, 15f, 20f)
    }

    private fun addVersionInfo(major: String, minor: String, data: Array<String>) {
        if (!changeLogContent.containsKey(major)) changeLogContent[major] = LinkedHashMap()
        changeLogContent[major]?.set(minor, data)
        if (latestMajor == null) {
            latestMajor = major
            latestMinor = minor
        }
    }

    private fun loadHashmapContent() {
        if (changeLogContent.size > 0) return

        //Version 1.5.2109.1
        val content27 = Array<String>()
        content27.add("-UI: Allow player to switch pane between left and right")
        content27.add("-Bug fixes")
        addVersionInfo("1.5", "2109.1", content27)

        //Version 1.5.2108.1
        val content26 = Array<String>()
        content26.add("-Weather: Allow player to change airport visibility")
        content26.add("-UI: Choose to show or hide the sector boundary (shown by default)")
        content26.add("-Bug fixes")
        addVersionInfo("1.5", "2108.1", content26)

        //Version 1.5.2107.1
        val content25 = Array<String>()
        if (TerminalControl.full) content25.add("-Datatag: Added customisable data tags: go to Settings => Data tag => Datatag style => Manage layouts...")
        content25.add("-UI: Refreshed changelog screen (you are looking at it right now!)")
        content25.add("-Bug fixes, optimisations and code improvements for future updates")
        addVersionInfo("1.5", "2107.1", content25)

        //Version 1.5.2103.1
        val content24 = Array<String>()
        content24.add("-Mechanics: Added approach transitions - transition is selected automatically based on aircraft flight path")
        content24.add("-Mechanics: Added RNAV/RNP approaches for TCWS")
        content24.add("-Mechanics: Added radar departures for TCWS")
        content24.add("-Bug fixes")
        addVersionInfo("1.5", "2103.1", content24)

        //Version 1.5.2102.1
        val content23 = Array<String>()
        content23.add("-Weather: Greatly improved game performance when storms are enabled")
        content23.add("-UI: Some cosmetic changes")
        content23.add("-Bug fixes, optimisations")
        addVersionInfo("1.5", "2102.1", content23)

        //Version 1.5.2101.2
        val content22 = Array<String>()
        if (Gdx.app.type == Application.ApplicationType.Android) {
            if (!TerminalControl.full) content22.add("-Airports: More airports have been made available - complete surveys or watch ads to unlock new airports for a set period of time!")
            content22.add("-Google Play Games: Integrated Play Games functionality")
            content22.add("-Cloud saves: Added cloud save functionality, accessible in the Play Games settings page - requires permission for data to be stored in your Google Drive")
        }
        content22.add("-Bug fixes, optimisations")
        addVersionInfo("1.5", "2101.2", content22)

        //Version 1.5.2101.1
        val content21 = Array<String>()
        content21.add("-Feature: Allow user to spawn custom arrivals under Settings => Traffic => Spawn custom arrival")
        if (Gdx.app.type == Application.ApplicationType.Desktop) content21.add("-TTS: Added TTS for desktop (Linux, Windows XP and later); if you are using Windows, you can install more voices from Microsoft")
        content21.add("-Bug fixes, optimisations")
        addVersionInfo("1.5", "2101.1", content21)

        //Version 1.5.2014.2
        val content20 = Array<String>()
        if (TerminalControl.full) content20.add("-Mechanics: Added circling approaches for rwy 14R at TCOO and rwy 27 at TCBE")
        content20.add("-Bug fixes, optimisations")
        addVersionInfo("1.5", "2014.2", content20)

        //Version 1.5.2014.1
        val content19 = Array<String>()
        content19.add("-Mechanics: Added thunder storm cells")
        content19.add("-You may change the number of storm cells from the game settings")
        content19.add("-Please note that enabling storms may affect game performance")
        content19.add("-Bug fixes, optimisations")
        addVersionInfo("1.5", "2014.1", content19)

        //Version 1.4.2013.2
        val content18 = Array<String>()
        content18.add("-Mechanics: Centre/control will now prevent aircraft under their control from coming into conflict")
        content18.add("-A couple of bug fixes, optimisations")
        addVersionInfo("1.4", "2013.2", content18)

        //Version 1.4.2013.1
        val content17 = Array<String>()
        content17.add("-UI: Added importing, exporting of game saves to allow transfer of game progress across devices")
        content17.add("-Bug fixes, optimisations")
        addVersionInfo("1.4", "2013.1", content17)

        //Version 1.4.2012.2
        val content16 = Array<String>()
        if (Gdx.app.type == Application.ApplicationType.Android) content16.add("-UI: Added distance measuring tool - switch to \"Dist mode\" and measure the distance between 2 points with 2 fingers")
        if (Gdx.app.type == Application.ApplicationType.Desktop) content16.add("-UI: Added distance measuring tool - right click and drag to measure distance between 2 points")
        content16.add("-UI: Added options to enable display of distance remaining to each waypoint on an aircraft's route")
        content16.add("-Mechanics: Departures will now take into account any landing aircraft or go-arounds on dependent parallel runways to prevent potential conflicts")
        content16.add("-Bug fixes, optimisations")
        addVersionInfo("1.4", "2012.2", content16)

        //Version 1.4.2012.1
        val content15 = Array<String>()
        content15.add("-UI: Added conflict display, reason for conflict to status pane")
        content15.add("-UI: Added dialogs to prevent accidental quitting, deleting")
        if (TerminalControl.full) content15.add("-TCMD, TCPG: Updated MVA data")
        content15.add("-Bug fixes, optimisations")
        addVersionInfo("1.4", "2012.1", content15)

        //Version 1.4.2011.1
        val content14 = Array<String>()
        content14.add("-Traffic: Custom traffic settings are now available under Settings => Traffic => Arrival traffic settings - 3 different modes are available")
        content14.add("-UI: Added a new status pane which can be toggled from the communication pane; useful for keeping track of requests and better situational awareness when traffic volume is high")
        if (TerminalControl.full) content14.add("-TCTT: Added new south runway configurations, SIDs and STARs")
        content14.add("-Many bug fixes, optimisations")
        addVersionInfo("1.4", "2011.1", content14)

        //Version 1.4.2010.2
        val content13 = Array<String>()
        content13.add("-UI: Aircraft pane overhaul - more intuitive user interface when selecting clearance for aircraft")
        content13.add("-Mechanics: Planes can now be cleared for approach even in STAR mode; when cleared the aircraft will descend via STAR to the minimum altitude, and capture the ILS automatically - useful for STARs that end on the final approach course")
        content13.add("-Mechanics: MVA check will now be activated for aircraft flying in SID/STAR mode whose position deviate significantly from the original route")
        content13.add("-Achievements: Added new parallel landing achievement")
        content13.add("-Bug fixes, optimisations")
        addVersionInfo("1.4", "2010.2", content13)

        //Version 1.4.2010.1
        val content12 = Array<String>()
        content12.add("-Updated list of airlines, aircraft types at airports")
        content12.add("-Minor bug fixes")
        addVersionInfo("1.4", "2010.1", content12)

        //Version 1.4.2008.1
        val content11 = Array<String>()
        content11.add("-Mechanics: Departures will no longer automatically climb to the minimum SID altitude if below - instead the MVA check will be activated for the sector")
        content11.add("-A couple of bug fixes")
        addVersionInfo("1.4", "2008.2", content11)

        //Version 1.4.2006.1
        val content10 = Array<String>()
        content10.add("-Mechanics: More realistic cruise altitude, speeds for certain aircraft types")
        content10.add("-Mechanics: Departures may now request for shortcuts, high speed climb, or reminder to be cleared to a higher altitude")
        if (TerminalControl.full) content10.add("-TCPG: Added STAR transitions for TCPG, TCPO")
        content10.add("-Stats: Individual game stats are shown on the pause screen - most stats will start from 0")
        content10.add("-Settings: A refreshed settings page, with options grouped into different sections for easier navigation")
        content10.add("-Display: New option to set different colour styles for the radar screen - colourful or standardised")
        content10.add("-Display: New option whether to show the trail of an uncontrolled aircraft always, or only when selected")
        content10.add("-Display: New option to set the intervals between range rings, or to turn range rings off")
        content10.add("-Data tag: New option whether to show the data tag border, background always, or only when selected")
        content10.add("-Data tag: New option to set spacing between data tag rows")
        content10.add("-Sounds: New sound for alerts (yellow messages) and go-arounds")
        content10.add("-Tutorial: Important points, key words are now highlighted in different colours for easier following")
        content10.add("-Many bug fixes, including restrictions being incorrectly displayed for SIDs")
        addVersionInfo("1.4", "2006.1", content10)

        //Version 1.4.2005.2
        val content9 = Array<String>()
        content9.add("-Weather: New custom weather option in settings - set your own static weather for each airport")
        content9.add("-Mechanics: New close/open sector button to manage arrival flow to prevent over-congestion")
        content9.add("-Mechanics: New Change STAR option for arrivals; arrival will change to fly heading mode and needs to be re-cleared to waypoint on the new STAR")
        content9.add("-UI: Made SID/STAR restriction display more compact and realistic")
        content9.add("-UI: New option to turn the trajectory line off in settings")
        content9.add("-UI: Options for displaying of aircraft trail in settings")
        content9.add("-Tutorial: Added option to pause, speed up, quit tutorial")
        content9.add("-Optimisations, bug fixes")
        addVersionInfo("1.4", "2005.2", content9)

        //Version 1.4.2005.1
        val content8 = Array<String>()
        content8.add("-Mechanics: More realistic initial climb altitudes")
        content8.add("-Mechanics: Reduced descent rates for increased realism")
        content8.add("-Wake turbulence: Reduced altitude separation required")
        content8.add("-UI: Data tag overhaul - choose between the default and compact data tag styles")
        content8.add("-UI: Data tag will show changes to aircraft clearances")
        content8.add("-UI: If present, SID/STAR restrictions are displayed beside the waypoint")
        content8.add("-UI: You can now choose to hide the MVA sector altitudes to reduce clutter")
        content8.add("-UI: Choose between default or realistic ILS display")
        if (Gdx.app.type == Application.ApplicationType.Desktop) {
            content8.add("-Discord: Added Discord Rich Presence integration")
        }
        content8.add("-And of course, various bug fixes")
        addVersionInfo("1.4", "2005.1", content8)

        //Version 1.4.2004.2
        val content7 = Array<String>()
        content7.add("-Handover: You can now hand planes over to the next controller with the handover button")
        content7.add("-Acknowledge: You can acknowledge new arrivals if you do not wish to give them any new clearances")
        content7.add("-Heading UI: Heading buttons are now positioned left-right instead of up-down")
        content7.add("-Default tab: Change the default UI tab (lateral, altitude or speed) in the game settings")
        content7.add("-Conflict warnings: Mute conflict warnings by tapping on the planes involved")
        content7.add("-Communications: Go-arounds are now stated explicitly by pilots")
        if (TerminalControl.full) {
            content7.add("-TCBD: Minimum separation for TCBD has been corrected to 3nm")
        }
        content7.add("-A couple of bug fixes")
        addVersionInfo("1.4", "2004.2", content7)

        //Version 1.3.2004.1
        val content6 = Array<String>()
        content6.add("-Better fly-over waypoint display")
        content6.add("-A couple of bug fixes, optimisations")
        addVersionInfo("1.3", "2004.1", content6)

        //Version 1.3.2003.3
        val content5 = Array<String>()
        content5.add("-Just a couple of bug fixes, optimisations")
        addVersionInfo("1.3", "2003.3", content5)

        //Version 1.3.2003.2
        val content4 = Array<String>()
        content4.add("-Achievements: Unlock new achievements as you play - check out the achievements tab in the main menu")
        content4.add("-Mechanics: Improved holding pattern entry")
        content4.add("-A couple of bug fixes")
        content4.add("-HX: (Hmm, maybe doing something will unlock a new airport?)")
        addVersionInfo("1.3", "2003.2", content4)

        //Version 1.3.2003.1
        val content3 = Array<String>()
        if (TerminalControl.full) {
            content3.add("-Unlocks & upgrades: Unlock new features as you play! Check out the upgrade menu in the main menu for more information")
            content3.add("-TCTT: Changed STARs for TCTT south wind night operations")
        }
        content3.add("-Autosave: Added option to change autosave frequency")
        content3.add("-A couple of small bug fixes")
        addVersionInfo("1.3", "2003.1", content3)

        //Version 1.3.2002.2
        val content2 = Array<String>()
        if (TerminalControl.full) {
            content2.add("-Simultaneous departure: Simultaneous departures (if available) will only be active if traffic volume is high")
        }
        content2.add("-A couple of small bug fixes")
        addVersionInfo("1.3", "2002.2", content2)

        //Version 1.3.2002.1
        val content1 = Array<String>()
        content1.add("-Increased zoom: Option for increased radar zoom has been added in the default settings page")
        content1.add("-Bug fixes")
        addVersionInfo("1.3", "2002.1", content1)

        //Version 1.3.2001.4
        val content = Array<String>()
        if (TerminalControl.full) {
            content.add("-Night operations: Added customisable time settings for night operations at select airports")
        }
        content.add("-Minor bug fixes")
        addVersionInfo("1.3", "2001.4", content)
    }
}