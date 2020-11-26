package com.bombbird.terminalcontrol.desktop

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Base64Coder
import com.badlogic.gdx.utils.GdxRuntimeException
import com.bombbird.terminalcontrol.screens.selectgamescreen.LoadGameScreen
import com.bombbird.terminalcontrol.utilities.files.ExternalFileHandler
import org.json.JSONObject
import java.awt.EventQueue
import java.awt.event.WindowEvent
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.UIManager
import javax.swing.filechooser.FileNameExtensionFilter


class DesktopFileHandler: ExternalFileHandler {
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
            fileChooser.addChoosableFileFilter(FileNameExtensionFilter("Terminal Control saves (*.tcsav)", "tcsav"))
            fileChooser.isAcceptAllFileFilterUsed = true
            jFrame = JFrame()
            jFrame?.isUndecorated = true
            jFrame?.isVisible = true
            jFrame?.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
            val returnValue = fileChooser.showOpenDialog(jFrame)
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                val file = fileChooser.selectedFile
                var strData = ""
                try {
                    strData = Gdx.files.absolute(file.absolutePath).readString()
                } catch (e: GdxRuntimeException) {
                    e.printStackTrace()
                }
                notifyLoaded(strData, loadGameScreen)
            }
            jFrame?.dispatchEvent(WindowEvent(jFrame, WindowEvent.WINDOW_CLOSING))
            jFrame = null
        }
    }

    override fun openFileSaver(save: JSONObject, loadGameScreen: LoadGameScreen) {
        EventQueue.invokeLater {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
            if (jFrame != null) {
                jFrame?.toFront()
                jFrame?.repaint()
                return@invokeLater
            }
            val fileChooser = object : JFileChooser() {
                override fun approveSelection() {
                    val f: File = selectedFile
                    if (f.exists() && dialogType == SAVE_DIALOG) {
                        when (JOptionPane.showConfirmDialog(this, "File already exists, overwrite?", "Existing file", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE)) {
                            JOptionPane.YES_OPTION -> {
                                super.approveSelection()
                                return
                            }
                            JOptionPane.NO_OPTION -> return
                            JOptionPane.CLOSED_OPTION -> return
                        }
                    }
                    super.approveSelection()
                }
            }
            fileChooser.addChoosableFileFilter(FileNameExtensionFilter("Terminal Control saves (*.tcsav)", "tcsav"))
            fileChooser.isAcceptAllFileFilterUsed = true
            jFrame = JFrame()
            jFrame?.isUndecorated = true
            jFrame?.isVisible = true
            jFrame?.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
            when (fileChooser.showSaveDialog(jFrame)) {
                JFileChooser.APPROVE_OPTION -> {
                    try {
                        val encode = Base64Coder.encodeString(save.toString())
                        Gdx.files.absolute(fileChooser.selectedFile.absolutePath).writeString(encode, false)
                        notifySaved(true, loadGameScreen)
                    } catch (e: GdxRuntimeException) {
                        notifySaved(false, loadGameScreen)
                    }
                }
                JFileChooser.ERROR_OPTION -> notifySaved(false, loadGameScreen)
            }

            jFrame?.dispatchEvent(WindowEvent(jFrame, WindowEvent.WINDOW_CLOSING))
            jFrame = null
        }
    }
}