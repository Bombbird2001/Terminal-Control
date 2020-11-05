package com.bombbird.terminalcontrol.ui.dialogs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Timer
import com.bombbird.terminalcontrol.utilities.Fonts
import com.bombbird.terminalcontrol.utilities.saving.GameSaver

class DeleteDialog: CustomDialog("Delete save?", "", "Keep", "Delete") {
    var saveId = -1
    var scrollTable: Table? = null
    var deleteButton: TextButton? = null
    var saveButton: TextButton? = null
    var label: Label? = null

    override fun result(resObj: Any?) {
        super.result(resObj)
        if (resObj == DIALOG_POSITIVE) {
            //Delete save
            Gdx.app.postRunnable {
                GameSaver.deleteSave(saveId)
                val cell = scrollTable?.getCell(deleteButton)
                val cell1 = scrollTable?.getCell(saveButton)
                scrollTable?.removeActor(deleteButton)
                scrollTable?.removeActor(saveButton)

                //Fix UI bug that may happen after deleting cells - set cell size to 0 rather than deleting them
                cell?.size(cell.prefWidth, 0f)
                cell1?.size(cell1.prefWidth, 0f)
                scrollTable?.invalidate()

                if (scrollTable?.hasChildren() != true) {
                    label?.isVisible = true
                }
            }
        }
    }

    fun show(stage: Stage, saveInfo: String, saveId: Int, scrollTable: Table, deleteButton: TextButton, saveButton: TextButton, label: Label) {
        val labelStyle = Label.LabelStyle()
        labelStyle.font = Fonts.defaultFont12
        val display = Label("$saveInfo\nThis cannot be undone!", labelStyle)
        display.setAlignment(Align.center)
        text(display)

        this.saveId = saveId
        this.scrollTable = scrollTable
        this.deleteButton = deleteButton
        this.saveButton = saveButton
        this.label = label

        super.show(stage)
    }

    override fun hide() {
        super.hide()

        Timer.schedule(object : Timer.Task() {
            override fun run() {
                Gdx.app.postRunnable {
                    saveId = -1
                    scrollTable = null
                    deleteButton = null
                    saveButton = null
                    label = null
                    contentTable.clearChildren()
                }
            }
        }, 0.4f)
    }
}