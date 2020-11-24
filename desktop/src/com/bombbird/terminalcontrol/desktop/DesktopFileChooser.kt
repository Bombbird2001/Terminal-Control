package com.bombbird.terminalcontrol.desktop

import com.bombbird.terminalcontrol.utilities.files.ExternalFileChooser
import java.awt.EventQueue
import javax.swing.JFileChooser
import javax.swing.UIManager

class DesktopFileChooser: ExternalFileChooser {
    override fun openFileChooser() {
        EventQueue.invokeLater {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
            val fileChooser = JFileChooser()
            val returnValue = fileChooser.showOpenDialog(null)
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                val file = fileChooser.selectedFile
                println(file.absolutePath)
            }
        }
    }
}