package com.bombbird.terminalcontrol

import com.google.api.services.drive.Drive
import com.google.api.client.http.FileContent
import com.google.api.services.drive.model.File
import java.util.*
import com.google.api.services.drive.model.FileList

class DriveManager(private val drive: Drive) {
    fun createFile() {
        println("Create file stub")
        /*
        val fileMetadata = File()
        fileMetadata.name = "settings.json"
        fileMetadata.parents = Collections.singletonList("appDataFolder")
        val filePath = java.io.File("files/config.json")
        val mediaContent = FileContent("application/json", filePath)
        val file: File = drive.files().create(fileMetadata, mediaContent)
            .setFields("id")
            .execute()
        println("Created file ID: " + file.id)
         */
    }

    fun listFiles() {
        println("List files stub")
        /*
        val files: FileList = drive.files().list()
            .setSpaces("appDataFolder")
            .setFields("nextPageToken, files(id, name)")
            .setPageSize(10)
            .execute()
        for (file in files.files) {
            println("Found file: ${file.name}, ${file.id}")
        }
         */
    }
}