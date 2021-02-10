package com.bombbird.terminalcontrol

import com.badlogic.gdx.Gdx
import com.bombbird.terminalcontrol.screens.StandardUIScreen
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
import kotlin.collections.HashMap
import kotlin.math.roundToInt

class DriveManager(private val drive: Drive, private val activity: AndroidLauncher) {
    fun saveGame() {
        var dialog: CustomDialog? = null
        (activity.game.screen as? StandardUIScreen)?.let {
            dialog = object : CustomDialog("Save to cloud", "Saving game data to cloud...", "", "") {
                override fun result(resObj: Any?) {
                    cancel()
                }
            }
            dialog?.show(it.stage)
        }

        val saveFiles = ArrayList<File>()
        val media = ArrayList<FileContent>()

        val settingsMeta = File()
        settingsMeta.name = "settings.json"
        settingsMeta.parents = Collections.singletonList("appDataFolder")
        settingsMeta.appProperties = HashMap<String, String>()
        settingsMeta.appProperties["type"] = if (TerminalControl.full) "full" else "lite"
        val settingsMedia = FileContent("application/json", FileLoader.getExtDir("settings.json")?.file())

        val statsMeta = File()
        statsMeta.name = "stats.json"
        statsMeta.parents = Collections.singletonList("appDataFolder")
        statsMeta.appProperties = HashMap<String, String>()
        statsMeta.appProperties["type"] = if (TerminalControl.full) "full" else "lite"
        val statsMedia = FileContent("application/json", FileLoader.getExtDir("stats.json")?.file())

        val saveMeta = File()
        saveMeta.name = "saves"
        saveMeta.parents = Collections.singletonList("appDataFolder")
        saveMeta.mimeType = "application/vnd.google-apps.folder"
        saveMeta.appProperties = HashMap<String, String>()
        saveMeta.appProperties["type"] = if (TerminalControl.full) "full" else "lite"

        FileLoader.getExtDir("saves")?.list()?.let {
            for (file in it) {
                val fileMeta = File()
                fileMeta.name = file.name()
                fileMeta.appProperties = HashMap<String, String>()
                fileMeta.appProperties["type"] = if (TerminalControl.full) "full" else "lite"
                val fileMedia = FileContent("*/*", file.file())
                saveFiles.add(fileMeta)
                media.add(fileMedia)
            }
        }

        val totalCount = 4 + saveFiles.size
        val atomicCounter = AtomicInteger(0)
        Thread {
                try {
                    val allFiles: FileList = drive.files().list()
                        .setSpaces("appDataFolder").setFields("files(id,name,appProperties)")
                        .execute()
                    var settingsId: String? = null
                    var statsId: String? = null
                    var savesId: String? = null
                    for (fileMeta in allFiles.files) {
                        println(fileMeta.toString())
                        if (fileMeta.name == "play_games") continue
                        var skip = false
                        fileMeta.appProperties?.let {
                            if (it["type"] != "full" && TerminalControl.full) skip = true
                            if (it["type"] != "lite" && !TerminalControl.full) skip = true
                        } ?: continue
                        if (skip) continue
                        //The file is confirmed to be used for the app
                        when (fileMeta.name) {
                            "settings.json" -> settingsId = fileMeta.id
                            "stats.json" -> statsId = fileMeta.id
                            "saves" -> savesId = fileMeta.id
                        }
                        if (settingsId != null && statsId != null && savesId != null) break //All 3 files found, exit loop
                    }

                    if (settingsId != null) {
                        settingsMeta.parents = null
                        drive.files().update(settingsId, settingsMeta, settingsMedia).execute()
                    } else drive.files().create(settingsMeta, settingsMedia).setFields("").execute()
                    incrementCounterDialog(atomicCounter, totalCount, dialog, true)

                    if (statsId != null) {
                        statsMeta.parents = null
                        drive.files().update(statsId, statsMeta, statsMedia).execute()
                    } else drive.files().create(statsMeta, statsMedia).setFields("").execute()
                    incrementCounterDialog(atomicCounter, totalCount, dialog, true)

                    if (savesId != null) drive.files().delete(savesId).setFields("").execute() //Delete existing saves folder
                    incrementCounterDialog(atomicCounter, totalCount, dialog, true)
                } catch (e: UserRecoverableAuthIOException) {
                    //User disconnected game from Google Drive
                    activity.playGamesManager.requestPermissions()
                    dialog?.hide()
                    return@Thread
                } catch (e: IllegalArgumentException) {
                    //Old error that would occur because .requestEmail() wasn't called
                    dialog?.hide()
                    (activity.game.screen as? StandardUIScreen)?.let {
                        object : CustomDialog("Save to cloud","An error occurred while saving -\nplease restart the app and try again","","Ok") {
                            override fun result(resObj: Any?) {
                                it.disableBackButton = false
                            }
                        }.show(it.stage)
                    }
                    return@Thread
                }

                val folder = drive.files().create(saveMeta).setFields("id").execute() //Create new save folder
                incrementCounterDialog(atomicCounter, totalCount, dialog, true)

                val executor = Executors.newFixedThreadPool(saveFiles.size.coerceAtMost(5))
                for ((index, fileMetadata) in saveFiles.withIndex()) {
                    //Save all game data into folder
                    fileMetadata.parents = Collections.singletonList(folder.id)
                    executor.submit {
                        drive.files().create(fileMetadata, media[index]).setFields("").execute()
                        incrementCounterDialog(atomicCounter, totalCount, dialog, true)
                    }
                }
                executor.shutdown()
                executor.awaitTermination(totalCount * 10L, TimeUnit.SECONDS)
            listFiles()
            dialog?.hide()
            (activity.game.screen as? StandardUIScreen)?.let {
                object : CustomDialog("Save to cloud", if (atomicCounter.get() < totalCount) "Failed to save to cloud - please try again" else "Data has been saved to cloud", "", "Ok") {
                    override fun result(resObj: Any?) {
                        it.disableBackButton = false
                    }
                }.show(it.stage)
            }
        }.start()
    }

    fun loadGame() {
        var dialog: CustomDialog? = null
        (activity.game.screen as? StandardUIScreen)?.let {
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
                fileList = filterList()
            } catch (e: UserRecoverableAuthIOException) {
                //User disconnected game from Google Drive
                activity.playGamesManager.requestPermissions()
                dialog?.hide()
                return@Thread
            } catch (e: IllegalArgumentException) {
                //Old error that would occur because .requestEmail() wasn't called
                dialog?.hide()
                (activity.game.screen as? StandardUIScreen)?.let {
                    object : CustomDialog("Load from cloud","An error occurred while loading -\nplease restart the app and try again","","Ok") {
                        override fun result(resObj: Any?) {
                            it.disableBackButton = false
                        }
                    }.show(it.stage)
                }
                return@Thread
            }

            val totalCount = fileList.size
            val atomicCounter = AtomicInteger(0)
            if (totalCount > 0) {
                //Delete existing local saves
                val folderHandle = FileLoader.getExtDir("saves")
                if (folderHandle?.exists() == true) {
                    folderHandle.deleteDirectory()
                }

                val executor = Executors.newFixedThreadPool(fileList.size.coerceAtMost(5))
                for (fileMeta in fileList) {
                    executor.submit {
                        val outputStream = ByteArrayOutputStream()
                        drive.files().get(fileMeta.id).executeMediaAndDownloadTo(outputStream)
                        val path = when (fileMeta.name) {
                            "settings.json", "stats.json" -> fileMeta.name
                            else -> "saves/${fileMeta.name}"
                        }
                        FileLoader.getExtDir(path)?.let {
                            it.writeString(outputStream.toString(StandardCharsets.UTF_8.name()), false)
                            //println("Written $path")
                            incrementCounterDialog(atomicCounter, totalCount, dialog, false)
                        }
                    }
                }
                executor.shutdown()
                executor.awaitTermination(totalCount * 10L, TimeUnit.SECONDS)
            }
            dialog?.hide()
            (activity.game.screen as? StandardUIScreen)?.let {
                object : CustomDialog("Load from cloud", if (totalCount <= 0) "No data saved in cloud - data on\nthis device remain unchanged" else "Data has been loaded from cloud", "", "Ok") {
                    override fun result(resObj: Any?) {
                        it.disableBackButton = false
                    }
                }.show(it.stage)
            }
        }.start()
    }

    private fun listFiles() {
        println("List files")
        val files: FileList = drive.files().list()
            .setSpaces("appDataFolder").setFields("files(id,name,appProperties)")
            .execute()
        for (file in files.files) {
            println(file.toString())
        }
    }

    private fun filterList(): ArrayList<File> {
        println("Filter files")
        val fileList = ArrayList<File>()
        val files: FileList = drive.files().list()
            .setSpaces("appDataFolder").setFields("files(id,name,appProperties)")
            .execute()
        for (fileMeta in files.files) {
            println(fileMeta.toString())
            if (fileMeta.name == "saves" || fileMeta.name == "play_games") continue
            var skip = false
            fileMeta.appProperties?.let {
                if (it["type"] != "full" && TerminalControl.full) skip = true
                if (it["type"] != "lite" && !TerminalControl.full) skip = true
            } ?: continue
            if (skip) continue
            fileList.add(fileMeta)
        }
        return fileList
    }

    /*
    fun clearFiles() {
        println("Clear files")
        val files: FileList = drive.files().list()
            .setSpaces("appDataFolder")
            .execute()
        for (file in files.files) {
            println(file.toString())
            if (file.name == "settings.json" || file.name == "stats.json" || file.name == "saves") {
                drive.files().delete(file.id).execute()
                println("${file.name} ${file.id} deleted")
            }
        }
        listFiles()
    }
     */

    private fun incrementCounterDialog(atomicCounter: AtomicInteger, total: Int, dialog: CustomDialog?, saving: Boolean) {
        val prevText = if (saving) "Saving game data to cloud..." else "Loading game data from cloud..."
        val finalInt = atomicCounter.incrementAndGet()
        Gdx.app.postRunnable {
            dialog?.updateText("$prevText ${(finalInt / total.toFloat() * 100).roundToInt()}%")
        }
    }
}