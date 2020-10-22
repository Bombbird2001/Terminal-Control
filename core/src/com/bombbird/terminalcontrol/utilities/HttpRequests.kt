package com.bombbird.terminalcontrol.utilities

import com.badlogic.gdx.Gdx
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.weather.Metar
import com.bombbird.terminalcontrol.utilities.RenameManager.reverseNameAirportICAO
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
                        radarScreen.loadingPercent = "10%"
                        getMetar(metar, false)
                    } else {
                        //Generate offline weather
                        response.close()
                        metar.randomWeather()
                    }
                } else {
                    val responseText = response.body?.string() ?: "Null getMetar response"
                    response.close()
                    if ("Update" == responseText) {
                        //Server requested for METAR update
                        println("Update requested")
                        if (metar.isQuit) return
                        radarScreen.loadingPercent = "20%"
                        getApiKey(metar, 0)
                    } else {
                        //METAR JSON text has been received
                        metar.metarObject = JSONObject(responseText)
                        metar.updateRadarScreenState()
                    }
                }
            }
        })
    }

    private fun getApiKey(metar: Metar, count: Int) {
        val body = "{\"password\":\"${Values.API_PASSWORD}\"}".toRequestBody(json)
        val request = Request.Builder()
                .url(Values.API_URL)
                .post(body)
                .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                //If requests fails due to timeout
                e.printStackTrace()
                if (count <= 2) {
                    getApiKey(metar, count + 1)
                    return
                }
                metar.randomWeather()
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Gdx.app.log("getApiKey", response.toString())
                    response.close()
                    if (count <= 2) {
                        getApiKey(metar, count + 1)
                        return
                    }
                    metar.randomWeather()
                } else {
                    val apiKey: String = response.body?.string() ?: "Null getApiKey response"
                    response.close()
                    if (apiKey == "Null getApiKey response") {
                        if (count <= 2) {
                            getApiKey(metar, count + 1)
                        } else {
                            metar.randomWeather()
                        }
                        return
                    }
                    if (metar.isQuit) return
                    receiveMetar(metar, apiKey, true)
                    TerminalControl.radarScreen?.loadingPercent = "40%"
                }
            }
        })
    }

    private fun receiveMetar(metar: Metar, apiKey: String, retry: Boolean) {
        val radarScreen = TerminalControl.radarScreen ?: return
        val stringBuilder = StringBuilder()
        for (newIcao in radarScreen.airports.keys) {
            if (stringBuilder.isNotEmpty()) stringBuilder.append(",")
            var arpt = reverseNameAirportICAO(newIcao)
            if ("VHHX" == arpt) arpt = "VHHH"
            stringBuilder.append(arpt)
        }
        val request = Request.Builder()
                .addHeader("X-API-KEY", apiKey)
                .url("https://api.checkwx.com/metar/$stringBuilder/decoded")
                .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                //If requests fails due to timeout
                e.printStackTrace()
                Gdx.app.log("API metar error", "CheckWX API may not be working!")

                //If retrying
                if (retry) {
                    Gdx.app.log("receiveMetar", "Retrying getting weather from API")
                    receiveMetar(metar, apiKey, false)
                    return
                }
                metar.randomWeather()
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Gdx.app.log("receiveMetar", response.toString())
                    response.close()
                    if (retry) {
                        Gdx.app.log("receiveMetar", "Retrying getting weather from API")
                        receiveMetar(metar, apiKey, false)
                        return
                    }
                    metar.randomWeather()
                } else {
                    val responseText = response.body?.string() ?: ""
                    response.close()
                    val jo = JSONObject(responseText)
                    sendMetar(metar, jo)
                    if (metar.isQuit) return
                    TerminalControl.radarScreen?.loadingPercent = "60%"
                }
            }
        })
    }

    private fun sendMetar(metar: Metar, jo: JSONObject) {
        jo.put("password", Values.SEND_METAR_PASSWORD)
        val body = jo.toString().toRequestBody(json)
        val request = Request.Builder()
                .url(Values.SEND_METAR_URL)
                .post(body)
                .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                //If requests fails due to timeout
                e.printStackTrace()
                sendMetar(metar, jo)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Gdx.app.log("sendMetar", response.toString())
                    metar.randomWeather()
                    response.close()
                    return
                } else {
                    println(response.body?.string())
                }
                response.close()
                if (metar.isQuit) return
                getMetar(metar, true)
                TerminalControl.radarScreen?.loadingPercent = "80%"
            }
        })
    }
}