package com.bombbird.terminalcontrol

import android.content.Context
import com.badlogic.gdx.Gdx
import com.bombbird.terminalcontrol.screens.BasicScreen
import com.bombbird.terminalcontrol.ui.dialogs.CustomDialog
import com.bombbird.terminalcontrol.utilities.files.FileLoader
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.services.drive.Drive
import com.google.api.client.http.FileContent
import com.google.api.services.drive.model.File
import java.util.*
import com.google.api.services.drive.model.FileList
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class DriveManager(private val drive: Drive, private val activity: AndroidLauncher) {
    fun saveGame() {
        var dialog: CustomDialog? = null
        (activity.playGamesManager.game.screen as? BasicScreen)?.let {
            dialog = object : CustomDialog("Save to cloud", "Saving game data to cloud...", "", "") {
                override fun result(resObj: Any?) {
                    cancel()
                }
            }
            dialog?.show(it.stage)
        }

        val files = ArrayList<File>()
        val media = ArrayList<FileContent>()

        val settingsMeta = File()
        settingsMeta.name = "settings.json"
        settingsMeta.parents = Collections.singletonList("appDataFolder")
        val settingsMedia = FileContent("application/json", FileLoader.getExtDir("settings.json")?.file())

        val statsMeta = File()
        statsMeta.name = "stats.json"
        statsMeta.parents = Collections.singletonList("appDataFolder")
        val statsMedia = FileContent("application/json", FileLoader.getExtDir("stats.json")?.file())

        val saveMeta = File()
        saveMeta.name = "saves"
        saveMeta.parents = Collections.singletonList("appDataFolder")
        saveMeta.mimeType = "application/vnd.google-apps.folder"

        FileLoader.getExtDir("saves")?.list()?.let {
            for (file in it) {
                val fileMeta = File()
                fileMeta.name = file.name()
                val fileMedia = FileContent("*/*", file.file())
                files.add(fileMeta)
                media.add(fileMedia)
            }
        }

        val totalCount = 4 + files.size
        val atomicCounter = AtomicInteger(0)
        Thread {
            val pref = activity.getPreferences(Context.MODE_PRIVATE)
            val settingsID = pref.getString("settingsId", null)
            val statsID = pref.getString("statsId", null)
            val savesID = pref.getString("savesId", null)
            try {
                if (settingsID == null || statsID == null) {
                    //If settings, stats don't exist yet, create new files with ID
                    val settings = drive.files().create(settingsMeta, settingsMedia).execute()
                    incrementCounterDialog(atomicCounter, totalCount, dialog, true)
                    val stats = drive.files().create(statsMeta, statsMedia).execute()
                    incrementCounterDialog(atomicCounter, totalCount, dialog, true)
                    with(pref.edit()) {
                        putString("settingsId", settings.id)
                        putString("statsId", stats.id)
                        apply()
                    }
                    println("Created settings ${settings.name} ${settings.id}")
                    println("Created stats ${stats.name} ${stats.id}")
                } else {
                    //Update changes to existing files
                    settingsMeta.parents = null
                    statsMeta.parents = null
                    val settings = drive.files().update(settingsID, settingsMeta, settingsMedia).execute()
                    incrementCounterDialog(atomicCounter, totalCount, dialog, true)
                    val stats = drive.files().update(statsID, statsMeta, statsMedia).execute()
                    incrementCounterDialog(atomicCounter, totalCount, dialog, true)
                    println("Updated settings ${settings.name} ${settings.id}")
                    println("Updated stats ${stats.name} ${stats.id}")
                }
            } catch (e: UserRecoverableAuthIOException) {
                activity.playGamesManager.requestPermissions()
                dialog?.hide()
                return@Thread
            }

            if (savesID != null) drive.files().delete(savesID).execute() //Delete existing saves folder
            incrementCounterDialog(atomicCounter, totalCount, dialog, true)

            val folder = drive.files().create(saveMeta).setFields("id, parents").execute() //Create new save folder
            pref.edit().putString("savesId", folder.id).apply() //Save the new folder ID

            val executor = Executors.newFixedThreadPool(5)
            for ((index, fileMetadata) in files.withIndex()) {
                //Save all game data into folder
                fileMetadata.parents = Collections.singletonList(folder.id)
                executor.submit {
                    val file: File = drive.files().create(fileMetadata, media[index])
                        .setFields("id, parents")
                        .execute()
                    println("Created save ${file.name} ${file.id}")
                    incrementCounterDialog(atomicCounter, totalCount, dialog, true)
                }
            }
            executor.shutdown()
            executor.awaitTermination(60, TimeUnit.SECONDS)
            listFiles()
            dialog?.hide()
            (activity.playGamesManager.game.screen as? BasicScreen)?.let {
                CustomDialog("Save to cloud", if (atomicCounter.get() < totalCount) "Failed to save to cloud - please try again" else "Data has been saved to cloud", "", "Ok").show(it.stage)
            }
        }.start()
    }

    fun loadGame() {
        var dialog: CustomDialog? = null
        (activity.playGamesManager.game.screen as? BasicScreen)?.let {
            dialog = object : CustomDialog("Load from cloud", "Loading game data from cloud...", "", "") {
                override fun result(resObj: Any?) {
                    cancel()
                }
            }
            dialog?.show(it.stage)
        }

        Thread {
            val fileList: List<File>
            try {
                fileList = listFiles()
            } catch (e: UserRecoverableAuthIOException) {
                activity.playGamesManager.requestPermissions()
                dialog?.hide()
                return@Thread
            }

            val folderHandle = FileLoader.getExtDir("saves")
            if (folderHandle?.exists() == true) {
                folderHandle.deleteDirectory() //Delete existing local saves
            }

            val totalCount = fileList.size - 2
            val atomicCounter = AtomicInteger(0)
            if (totalCount > 0) {
                val executor = Executors.newFixedThreadPool(5)
                for (fileMeta in fileList) {
                    if (fileMeta.name == "saves" || fileMeta.name == "play_games") continue
                    executor.submit {
                        val outputStream = ByteArrayOutputStream()
                        drive.files().get(fileMeta.id).executeMediaAndDownloadTo(outputStream)
                        val path = when (fileMeta.name) {
                            "settings.json", "stats.json" -> fileMeta.name
                            else -> "saves/${fileMeta.name}"
                        }
                        FileLoader.getExtDir(path)?.let {
                            it.writeString(outputStream.toString(StandardCharsets.UTF_8.name()), false)
                            println("Written $path")
                            incrementCounterDialog(atomicCounter, totalCount, dialog, false)
                        }
                    }
                }
                executor.shutdown()
                executor.awaitTermination(60, TimeUnit.SECONDS)
            }
            dialog?.hide()
            (activity.playGamesManager.game.screen as? BasicScreen)?.let {
                CustomDialog("Load from cloud", if (atomicCounter.get() < totalCount) "Failed to load from cloud - please try again" else if (totalCount <= 0) "No data saved in cloud - data on\nthis device remain unchanged" else "Data has been loaded from cloud", "", "Ok").show(it.stage)
            }
        }.start()
    }

    private fun listFiles(): List<File> {
        println("List files")
        val files: FileList = drive.files().list()
            .setSpaces("appDataFolder")
            .execute()
        for (file in files.files) {
            println("Found file: ${file.name}, ${file.id}")
        }
        return files.files
    }

    private fun incrementCounterDialog(atomicCounter: AtomicInteger, total: Int, dialog: CustomDialog?, saving: Boolean) {
        val prevText = if (saving) "Saving game data to cloud..." else "Loading game data from cloud..."
        val finalInt = atomicCounter.incrementAndGet()
        Gdx.app.postRunnable {
            dialog?.updateText("$prevText ${(finalInt / total.toFloat() * 100).roundToInt()}%")
        }
    }
}