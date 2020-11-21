package com.bombbird.terminalcontrol.utilities

import com.badlogic.gdx.Gdx
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.weather.Metar
import com.bombbird.terminalcontrol.ui.dialogs.CustomDialog
import com.bombbird.terminalcontrol.utilities.RenameManager.reverseNameAirportICAO
import com.bombbird.terminalcontrol.utilities.saving.GameSaver
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object HttpRequests {
    private val json: MediaType = "application/json; charset=utf-8".toMediaType()
    private val client = OkHttpClient()

    fun sendError(error: String?, count: Int) {
        if (!TerminalControl.sendAnonCrash) return
        val jo = JSONObject()
        jo.put("password", Values.SEND_ERROR_PASSWORD)
        jo.put("error", error)
        val body = jo.toString().toRequestBody(json)
        val request = Request.Builder()
                .url(Values.SEND_ERROR_URL)
                .post(body)
                .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                //If requests fails due to timeout
                e.printStackTrace()
                if (count <= 2) sendError(error, count + 1)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Gdx.app.log("sendError", response.toString())
                    response.close()
                } else {
                    println(response.body?.string())
                }
                response.close()
            }
        })
    }

    fun sendSaveError(saveError: String, count: Int, save: JSONObject, dialog: CustomDialog) {
        val jo = JSONObject()
        jo.put("password", Values.SEND_ERROR_PASSWORD)
        jo.put("saveError", saveError)
        val body = jo.toString().toRequestBody(json)
        val request = Request.Builder()
                .url(Values.SEND_ERROR_URL)
                .post(body)
                .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                //If requests fails due to timeout
                e.printStackTrace()
                if (count <= 2) sendError(saveError, count + 1)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Gdx.app.log("sendSaveError", response.toString())
                    response.close()
                } else {
                    println(response.body?.string())
                    save.put("errorSent", true)
                    GameSaver.writeObjectToFile(save, save.getInt("saveId"))
                    dialog.updateText("Save has been sent. Thank you!")
                    dialog.updateButtons("", "Ok!")
                }
                response.close()
            }
        })
    }

    fun getMetar(metar: Metar, retry: Boolean) {
        val radarScreen = TerminalControl.radarScreen ?: return
        val jo = JSONObject()
        jo.put("password", Values.GET_METAR_PASSWORD)
        val apts = JSONArray()
        for (newIcao in radarScreen.airports.keys) {
            apts.put(reverseNameAirportICAO(newIcao))
        }
        jo.put("airports", apts)
        val body = jo.toString().toRequestBody(json)
        val request = Request.Builder()
                .url(Values.GET_METAR_URL)
                .post(body)
                .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                metar.randomWeather()
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    if (response.code == 503 && retry) {
                        println("503 received: trying again")
                        response.close()
                        if (metar.isQuit) return
                        getMetar(metar, false)
                    } else {
                        //Generate offline weather
                        response.close()
                        metar.randomWeather()
                    }
                } else {
                    val responseText = response.body?.string()
                    response.close()

                    if (responseText == null) {
                        println("Null getMetar response")
                        metar.randomWeather()
                        return
                    }

                    //METAR JSON text has been received
                    if (!radarScreen.metarLoading) {
                        Gdx.app.postRunnable {
                            metar.metarObject = JSONObject(responseText)
                            metar.updateRadarScreenState()
                        }
                    } else {
                        //If not finished loading yet, game has just been loaded so don't run on main thread
                        metar.metarObject = JSONObject(responseText)
                        metar.updateRadarScreenState()
                    }
                }
            }
        })
    }
}