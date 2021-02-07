package com.bombbird.terminalcontrol

import android.content.Context
import android.util.Log
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
import kotlin.collections.HashMap
import kotlin.math.roundToInt

class DriveManager(private val drive: Drive, private val activity: AndroidLauncher) {
    fun saveGame() {
        var dialog: CustomDialog? = null
        (activity.game.screen as? BasicScreen)?.let {
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
                files.add(fileMeta)
                media.add(fileMedia)
            }
        }

        val totalCount = 4 + files.size
        val atomicCounter = AtomicInteger(0)
        Thread {
            val pref = activity.playGamesManager.signedInAccount?.let {
                val prefName = "${activity.getString(R.string.package_name)}_${it.id}"
                activity.getSharedPreferences(prefName, Context.MODE_PRIVATE)
            }
            if (pref != null) {
                val settingsID = pref.getString("settingsId", null)
                val statsID = pref.getString("statsId", null)
                val savesID = pref.getString("savesId", null)
                try {
                    if (settingsID == null || statsID == null) {
                        //If settings, stats don't exist yet, create new files with ID
                        val settings = drive.files().create(settingsMeta, settingsMedia).setFields("id").execute()
                        incrementCounterDialog(atomicCounter, totalCount, dialog, true)
                        val stats = drive.files().create(statsMeta, statsMedia).setFields("id").execute()
                        incrementCounterDialog(atomicCounter, totalCount, dialog, true)
                        with(pref.edit()) {
                            putString("settingsId", settings.id)
                            putString("statsId", stats.id)
                            apply()
                        }
                    } else {
                        //Update changes to existing files
                        settingsMeta.parents = null
                        statsMeta.parents = null
                        drive.files().update(settingsID, settingsMeta, settingsMedia).execute()
                        incrementCounterDialog(atomicCounter, totalCount, dialog, true)
                        drive.files().update(statsID, statsMeta, statsMedia).execute()
                        incrementCounterDialog(atomicCounter, totalCount, dialog, true)
                    }
                } catch (e: UserRecoverableAuthIOException) {
                    activity.playGamesManager.requestPermissions()
                    dialog?.hide()
                    return@Thread
                }

                if (savesID != null) drive.files().delete(savesID).execute() //Delete existing saves folder
                incrementCounterDialog(atomicCounter, totalCount, dialog, true)

                val folder = drive.files().create(saveMeta).setFields("id").execute() //Create new save folder
                pref.edit().putString("savesId", folder.id).apply() //Save the new folder ID
                incrementCounterDialog(atomicCounter, totalCount, dialog, true)

                val executor = Executors.newFixedThreadPool(5)
                for ((index, fileMetadata) in files.withIndex()) {
                    //Save all game data into folder
                    fileMetadata.parents = Collections.singletonList(folder.id)
                    executor.submit {
                        drive.files().create(fileMetadata, media[index])
                            .setFields("id, name")
                            .execute()
                        //println("Created save ${file.name} ${file.id}")
                        incrementCounterDialog(atomicCounter, totalCount, dialog, true)
                    }
                }
                executor.shutdown()
                executor.awaitTermination(60, TimeUnit.SECONDS)
            } else Log.e("Cloud save", "Null account or account ID")
            listFiles()
            dialog?.hide()
            (activity.game.screen as? BasicScreen)?.let {
                CustomDialog("Save to cloud", if (atomicCounter.get() < totalCount) "Failed to save to cloud - please try again" else "Data has been saved to cloud", "", "Ok").show(it.stage)
            }
        }.start()
    }

    fun loadGame() {
        var dialog: CustomDialog? = null
        (activity.game.screen as? BasicScreen)?.let {
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
                activity.playGamesManager.requestPermissions()
                dialog?.hide()
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

                val executor = Executors.newFixedThreadPool(5)
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
                executor.awaitTermination(60, TimeUnit.SECONDS)
            }
            dialog?.hide()
            (activity.game.screen as? BasicScreen)?.let {
                CustomDialog("Load from cloud", if (totalCount <= 0) "No data saved in cloud - data on\nthis device remain unchanged" else "Data has been loaded from cloud", "", "Ok").show(it.stage)
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