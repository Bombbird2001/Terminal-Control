package com.bombbird.terminalcontrol.desktop

import com.badlogic.gdx.Gdx
import com.bombbird.terminalcontrol.screens.selectgamescreen.LoadGameScreen
import com.bombbird.terminalcontrol.utilities.files.ExternalFileChooser
import java.awt.EventQueue
import java.awt.event.WindowEvent
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.UIManager
import javax.swing.filechooser.FileNameExtensionFilter

class DesktopFileChooser: ExternalFileChooser {
    private var jFrame: JFrame? = null

    override fun openFileChooser(loadGameScreen: LoadGameScreen) {
        EventQueue.invokeLater {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
            if (jFrame != null) {
                jFrame?.toFront()
                jFrame?.repaint()
                return@invokeLater
            }
            val fileChooser = JFileChooser()
            fileChooser.addChoosableFileFilter(FileNameExtensionFilter("Terminal Control saves (*.sav)", "sav"))
            fileChooser.isAcceptAllFileFilterUsed = true
            jFrame = JFrame()
            jFrame?.isUndecorated = true
            jFrame?.isVisible = true
            jFrame?.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
            val returnValue = fileChooser.showOpenDialog(jFrame)
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                val file = fileChooser.selectedFile
                val strData = Gdx.files.absolute(file.absolutePath).readString()
                notifyGame(strData, loadGameScreen)
            }
            jFrame?.dispatchEvent(WindowEvent(jFrame, WindowEvent.WINDOW_CLOSING))
            jFrame = null
        }
    }
}