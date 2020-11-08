package com.bombbird.terminalcontrol.ui.tabs

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.List
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Queue
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft
import com.bombbird.terminalcontrol.ui.Ui
import com.bombbird.terminalcontrol.utilities.errors.ErrorHandler
import com.bombbird.terminalcontrol.utilities.Fonts

open class Tab(ui: Ui) {
    companion object {
        var notListening = false
        lateinit var ui: Ui
        const val boxHeight = 320f
        lateinit var listStyle: List.ListStyle
        lateinit var paneStyle: ScrollPane.ScrollPaneStyle
        var latMode = 0
        var clearedHdg = 0
        var clearedWpt: String? = null
        var afterWptHdg = 0
        var afterWpt: String? = null
        var holdWpt: String? = null
        var clearedILS: String? = null
        var newStar: String? = null
        var turnDir = 0
        var altMode = 0
        var clearedAlt = 0
        var clearedExpedite = false
        var spdMode = 0
        var clearedSpd = 0

        var LOADED_STYLES = false
    }

    lateinit var valueBox: SelectBox<String>
    var selectedAircraft: Aircraft? = null
    var tabChanged = false
    var visible: Boolean
    var infoQueue: Queue<Array<Any>>

    val modeButtons: ModeButtons = ModeButtons(this)
    private val hideArray: com.badlogic.gdx.utils.Array<Actor> = com.badlogic.gdx.utils.Array()
    val radarScreen = TerminalControl.radarScreen!!

    init {
        Companion.ui = ui
        notListening = false
        visible = false
        if (!LOADED_STYLES) {
            loadStyles()
            LOADED_STYLES = true
        }
        loadSelect()
        infoQueue = Queue()
    }

    private fun loadStyles() {
        paneStyle = ScrollPane.ScrollPaneStyle()
        paneStyle.background = TerminalControl.skin.getDrawable("ListBackground")
        listStyle = List.ListStyle()
        listStyle.font = Fonts.defaultFont20
        listStyle.fontColorSelected = Color.WHITE
        listStyle.fontColorUnselected = Color.BLACK
        val buttonDown = TerminalControl.skin.getDrawable("Button_down")
        buttonDown.topHeight = 75f
        buttonDown.bottomHeight = 75f
        listStyle.selection = buttonDown
    }

    fun choiceMade() {
        if (!notListening && selectedAircraft != null) {
            try {
                choices
                updateElements()
                compareWithAC()
                updateElementColours()
            } catch (e: Exception) {
                ErrorHandler.sendGenericError(e, false)
            }
        }
    }

    private fun loadSelect() {
        val boxStyle = SelectBox.SelectBoxStyle()
        boxStyle.font = Fonts.defaultFont20
        boxStyle.fontColor = Color.WHITE
        boxStyle.listStyle = listStyle
        boxStyle.scrollStyle = paneStyle
        boxStyle.background = Ui.lightBoxBackground
        val boxStyle2 = SelectBox.SelectBoxStyle()
        boxStyle2.font = Fonts.defaultFont20
        boxStyle2.fontColor = Color.WHITE
        boxStyle2.listStyle = listStyle
        boxStyle2.scrollStyle = paneStyle
        boxStyle2.background = Ui.lightBoxBackground

        //Valuebox for waypoint/altitude/speed selections
        valueBox = SelectBox(boxStyle2)
        valueBox.setAlignment(Align.center)
        valueBox.list.setAlignment(Align.center)
        valueBox.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                choiceMade()
                event.handle()
            }
        })
        addActor(valueBox, 0.1f, 0.8f, 3240 - 1670.toFloat(), boxHeight)
    }

    fun addActor(actor: Actor, xRatio: Float, widthRatio: Float, y: Float, height: Float) {
        actor.setPosition(xRatio * paneWidth, y)
        actor.setSize(widthRatio * paneWidth, height)
        radarScreen.uiStage.addActor(actor)
        hideArray.add(actor)
        radarScreen.ui.actorArray.add(actor)
        radarScreen.ui.actorXArray.add(xRatio)
        radarScreen.ui.actorWidthArray.add(widthRatio)
    }

    open fun updateElements() {
        //Overridden method for updating each tab's elements
    }

    open fun compareWithAC() {
        //Overridden method for comparing each tab's states with AC
    }

    open fun updateElementColours() {
        //Overridden method for updating each tab's elements' colours
        ui.updateResetColours()
    }

    open fun updateMode() {
        //Overridden method for updating aircraft mode
    }

    open fun resetTab() {
        //Overridden method for updating aircraft mode
        notListening = true
        acState
        updateElements()
        compareWithAC()
        updateElementColours()
        notListening = false
    }

    //Overridden method for getting current AC status
    open val acState: Unit
        get() {
            //Overridden method for getting current AC status
        }

    //Overridden method for getting choices from selectboxes
    open val choices: Unit
        get() {
            //Overridden method for getting choices from selectboxes
        }

    fun setVisibility(show: Boolean) {
        notListening = true
        visible = show
        for (actor in hideArray) {
            actor.isVisible = show
        }
        notListening = false
    }

    private val paneWidth: Float
        get() = ui.paneWidth
}