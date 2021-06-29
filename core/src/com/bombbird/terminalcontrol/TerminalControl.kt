package com.bombbird.terminalcontrol

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Window
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.entities.aircrafts.Emergency
import com.bombbird.terminalcontrol.screens.MainMenuScreen
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen
import com.bombbird.terminalcontrol.sounds.TextToSpeechInterface
import com.bombbird.terminalcontrol.sounds.TextToSpeechManager
import com.bombbird.terminalcontrol.utilities.*
import com.bombbird.terminalcontrol.utilities.RenameManager.loadMaps
import com.bombbird.terminalcontrol.utilities.files.ExternalFileHandler
import com.bombbird.terminalcontrol.utilities.files.FileLoader
import com.bombbird.terminalcontrol.utilities.files.GameSaver

class TerminalControl(tts: TextToSpeechInterface, toastManager: ToastManager, discordManager: DiscordManager, externalFileHandler: ExternalFileHandler, browserInterface: BrowserInterface, playServicesInterface: PlayServicesInterface) : Game() {
    companion object {
        //Get screen size
        var WIDTH = 0
        var HEIGHT = 0

        //Use discord? (initialisation successful?)
        var useDiscord = false

        //Version info
        const val full = true
        lateinit var versionName: String
        var versionCode = 0

        //Active gameScreen instance
        var radarScreen: RadarScreen? = null

        //Create texture stuff
        private lateinit var buttonAtlas: TextureAtlas
        lateinit var skin: Skin

        //Text-to-speech
        lateinit var ttsManager: TextToSpeechManager
        lateinit var tts: TextToSpeechInterface

        //Toast (for Android only)
        lateinit var toastManager: ToastManager

        //Discord RPC (for Desktop only)
        var loadedDiscord = false
        lateinit var discordManager: DiscordManager

        //External file loader
        lateinit var externalFileHandler: ExternalFileHandler

        //Browser interface
        lateinit var browserInterface: BrowserInterface

        //Play Games interface
        lateinit var playServicesInterface: PlayServicesInterface

        //Datatag config list
        var datatagConfigs = Array<String>()

        //Default settings
        var trajectorySel = 0
        var pastTrajTime = 0
        lateinit var weatherSel: RadarScreen.Weather
        var stormNumber = 0
        var soundSel = 0
        lateinit var emerChance: Emergency.Chance
        var sendAnonCrash = false
        var increaseZoom = false
        var saveInterval = 0
        var radarSweep = 0f
        var advTraj = 0
        var areaWarning = 0
        var collisionWarning = 0
        var showMva = false
        var showIlsDash = false
        var datatagConfig = "Default"
        var showUncontrolled = false
        var alwaysShowBordersBackground = false
        var rangeCircleDist = 0
        var lineSpacingValue = 0
        var colourStyle = 0
        var realisticMetar = false
        var distToGoVisible = 0
        var defaultTabNo = 0
        var revision = 0
        const val LATEST_REVISION = 1

        fun loadSettings() {
            val settings = FileLoader.loadSettings()
            if (settings == null) {
                //Default settings if save unavailable
                trajectorySel = 90
                pastTrajTime = -1
                weatherSel = RadarScreen.Weather.LIVE
                stormNumber = 0
                soundSel = 2
                sendAnonCrash = true
                increaseZoom = false
                saveInterval = 60
                radarSweep = 2f
                advTraj = -1
                areaWarning = -1
                collisionWarning = -1
                showMva = true
                showIlsDash = false
                datatagConfig = "Default"
                showUncontrolled = false
                alwaysShowBordersBackground = true
                rangeCircleDist = 0
                lineSpacingValue = 1
                colourStyle = 0
                realisticMetar = false
                distToGoVisible = 0
                defaultTabNo = 0
                emerChance = Emergency.Chance.MEDIUM
                revision = 0
            } else {
                trajectorySel = settings.optInt("trajectory", 90)
                pastTrajTime = settings.optInt("pastTrajTime", -1)
                val weather = settings.optString("weather")
                weatherSel = when (weather) {
                    "true" -> RadarScreen.Weather.LIVE //Old format
                    "false" -> RadarScreen.Weather.RANDOM //Old format
                    else -> RadarScreen.Weather.valueOf(settings.getString("weather")) //New format
                }
                soundSel = settings.optInt("sound", 2)
                stormNumber = settings.optInt("stormNumber", 0)
                sendAnonCrash = settings.optBoolean("sendCrash", true)
                emerChance = if (settings.isNull("emerChance")) {
                    Emergency.Chance.MEDIUM
                } else {
                    Emergency.Chance.valueOf(settings.getString("emerChance"))
                }
                increaseZoom = settings.optBoolean("increaseZoom", false)
                saveInterval = settings.optInt("saveInterval", 60)
                radarSweep = settings.optDouble("radarSweep", 2.0).toFloat()
                advTraj = settings.optInt("advTraj", -1)
                areaWarning = settings.optInt("areaWarning", -1)
                defaultTabNo = settings.optInt("defaultTabNo", 0)
                collisionWarning = settings.optInt("collisionWarning", -1)
                showMva = settings.optBoolean("showMva", true)
                showIlsDash = settings.optBoolean("showIlsDash", false)
                datatagConfig = settings.optString("datatagConfig", if (settings.optBoolean("compactData", false)) "Compact" else "Default")
                showUncontrolled = settings.optBoolean("showUncontrolled", false)
                alwaysShowBordersBackground = settings.optBoolean("alwaysShowBordersBackground", true)
                rangeCircleDist = settings.optInt("rangeCircleDist", 0)
                lineSpacingValue = settings.optInt("lineSpacingValue", 1)
                colourStyle = settings.optInt("colourStyle", 0)
                realisticMetar = settings.optBoolean("realisticMetar", false)
                distToGoVisible = settings.optInt("distToGoVisible", 0)
                revision = settings.optInt("revision", 0)
            }
            GameSaver.saveSettings()
        }

        fun loadVersionInfo() {
            val info = Gdx.files.internal("game/type.type").readString().split(" ".toRegex()).toTypedArray()
            versionName = info[0]
            versionCode = info[1].toInt()
            FileLoader.mainDir = if (full) "AppData/Roaming/TerminalControlFull" else "AppData/Roaming/TerminalControl"
        }

        fun revisionNeedsUpdate(): Boolean {
            return revision < LATEST_REVISION
        }

        fun updateRevision() {
            revision = LATEST_REVISION
            GameSaver.saveSettings()
        }

        fun loadDatatagConfigs() {
            datatagConfigs.clear()
            datatagConfigs.add("Default", "Compact")
            if (full) datatagConfigs.addAll(Array(FileLoader.getAvailableDatatagConfigs()))
        }
    }

    //The one and only spritebatch
    lateinit var batch: SpriteBatch

    init {
        Companion.tts = tts
        Companion.toastManager = toastManager
        Companion.discordManager = discordManager
        Companion.externalFileHandler = externalFileHandler
        Companion.browserInterface = browserInterface
        Companion.playServicesInterface = playServicesInterface
        loadedDiscord = false
        useDiscord = false
        ttsManager = TextToSpeechManager()
    }

    private fun loadDialogSkin() {
        val ws = Window.WindowStyle()
        ws.titleFont = Fonts.defaultFont20
        ws.titleFontColor = Color.WHITE
        ws.background = skin.getDrawable("ListBackground")
        ws.stageBackground = skin.getDrawable("DarkBackground")
        skin.add("defaultDialog", ws)
    }

    override fun create() {
        WIDTH = Gdx.graphics.width
        HEIGHT = Gdx.graphics.height
        batch = SpriteBatch()
        buttonAtlas = TextureAtlas(Gdx.files.internal("game/ui/terminal-control.atlas"))
        skin = Skin()
        skin.addRegions(buttonAtlas)
        loadDialogSkin()
        loadMaps()
        Gdx.input.setCatchKey(Input.Keys.BACK, true)
        setScreen(MainMenuScreen(this, null))
    }

    override fun dispose() {
        batch.dispose()
        Fonts.dispose()
        buttonAtlas.dispose()
        skin.dispose()
    }
}