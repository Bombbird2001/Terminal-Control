package com.bombbird.terminalcontrol

import android.app.Activity
import com.badlogic.gdx.Game
import com.bombbird.terminalcontrol.screens.BasicScreen
import com.bombbird.terminalcontrol.screens.MainMenuScreen
import com.bombbird.terminalcontrol.screens.selectgamescreen.LoadGameScreen
import com.bombbird.terminalcontrol.screens.selectgamescreen.NewGameScreen
import com.bombbird.terminalcontrol.ui.dialogs.CustomDialog
import com.bombbird.terminalcontrol.utilities.Values
import com.pollfish.main.PollFish

class PollfishManager(private val activity: Activity, private val game: Game) {
    fun initPollfish() {
        val paramsBuilder = PollFish.ParamsBuilder(Values.POLLFISH_KEY).releaseMode(true).rewardMode(true)
            .pollfishReceivedSurveyListener {
                game.screen?.let {
                    if (it is MainMenuScreen || it is LoadGameScreen || it is NewGameScreen) {
                        object : CustomDialog("Survey", "A survey is available. Take it?", "No", "Sure") {
                            override fun result(resObj: Any?) {
                                super.result(resObj)
                                if (resObj == DIALOG_POSITIVE) {
                                    //Launch survey
                                    PollFish.show()
                                }
                            }
                        }.show((it as BasicScreen).stage)
                    }
                }
            }.pollfishCompletedSurveyListener {
                game.screen?.let {
                    if (it is MainMenuScreen || it is LoadGameScreen || it is NewGameScreen) {
                        CustomDialog("Survey", "Survey has been completed. Thank you!", "", "Ok").show((it as BasicScreen).stage)
                    }
                }
            }.build()
        PollFish.initWith(activity, paramsBuilder)
    }
}