package com.bombbird.terminalcontrol

import com.bombbird.terminalcontrol.utilities.files.FileLoader
import com.google.api.services.drive.Drive
import com.google.api.client.http.FileContent
import com.google.api.services.drive.model.File
import java.util.*
import com.google.api.services.drive.model.FileList

class DriveManager(private val drive: Drive) {
    fun createFile() {
        val fileMetadata = File()
        fileMetadata.name = "config.json"
        fileMetadata.parents = Collections.singletonList("appDataFolder")
        val filePath = FileLoader.getExtDir("settings.json")?.file()
        val mediaContent = FileContent("application/json", filePath)
        Thread {
            val file: File = drive.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute()
            println("Created file ID: " + file.id)
        }.start()
    }

    fun listFiles() {
        println("List files")
        Thread {
            val files: FileList = drive.files().list()
                .setSpaces("appDataFolder")
                .execute()
            for (file in files.files) {
                println("Found file: ${file.name}, ${file.id}")
                if (file.name == "config.json") drive.files().delete(file.id)
            }
        }.start()
    }
}