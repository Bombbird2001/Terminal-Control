package com.bombbird.terminalcontrol.entities.weather

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.utilities.math.MathTools
import org.json.JSONObject
import kotlin.math.pow
import kotlin.math.sqrt

class ThunderCell(save: JSONObject?) {
    var duration: Int = 0 //Time since formation, in seconds
    var matureDuration: Int = 0 //Time that storm spends in mature stage, in seconds
    var topAltitude: Int = 0
    val borderSet: HashSet<String> = HashSet() //Set of points that are borders (not all 8 bordering spots have been generated)
    val intensityMap: HashMap<String, Int> = HashMap() //10 levels of intensity from 1 to 10, 0 means spot can be deleted
    var centreX: Float = 0f
    var centreY: Float = 0f

    private val radarScreen = TerminalControl.radarScreen!!

    init {
        centreX = MathUtils.random(1560, 4200).toFloat()
        centreY = MathUtils.random(300, 2940).toFloat()
        if (save == null) {
            //If generating new storm, check for distance from other current storms
            while (true) {
                var distEnsured = true
                for (storm in radarScreen.thunderCellArray) {
                    val distPx = MathTools.distanceBetween(centreX, centreY, storm.centreX, storm.centreY)
                    if (MathTools.pixelToNm(distPx) < 12) { //Minimum 12nm from centre of other storms
                        distEnsured = false
                        break
                    }
                }
                if (distEnsured) break
                centreX = MathUtils.random(1560, 4200).toFloat()
                centreY = MathUtils.random(300, 2940).toFloat()
            }
        }
        matureDuration = MathUtils.random(1200, 3600) //Mature duration from 20 minutes to 1h
        borderSet.add("0 0")
        borderSet.add("-1 0")
        borderSet.add("0 -1")
        borderSet.add("-1 -1")
        intensityMap["0 0"] = 1
        intensityMap["-1 0"] = 1
        intensityMap["0 -1"] = 1
        intensityMap["-1 -1"] = 1
        topAltitude = radarScreen.minAlt + MathUtils.random(1000, 3000)
    }

    init {
        if (save != null) {
            duration = save.optInt("duration", duration)
            matureDuration = save.optInt("matureDuration", matureDuration)
            topAltitude = save.optInt("topAltitude", topAltitude)
            centreX = save.optDouble("centreX", centreX.toDouble()).toFloat()
            centreY = save.optDouble("centreY", centreY.toDouble()).toFloat()

            val borderArray = save.optJSONArray("borderSet")
            if (borderArray != null) {
                borderSet.clear()
                for (i in 0 until borderArray.length()) {
                    borderSet.add(borderArray.optString(i, "0 0"))
                }
            }

            val intensityObject = save.optJSONObject("intensityMap")
            if (intensityObject != null) {
                intensityMap.clear()
                for (key in intensityObject.keys()) {
                    intensityMap[key] = intensityObject.optInt(key, 1)
                }
            }
        }
    }

    /** Updates the storm status, run every 10s */
    fun update() {
        duration += 10
        when {
            duration < 1800 -> {
                //Developing stage
                //Start generating spots in the intensity map
                generateSpots(0.01f)

                //Increase the intensity of spots
                increaseIntensity(0.03f)

                //Start increasing top altitude
                if (topAltitude < 43000) topAltitude += MathUtils.random(160, 230)
            }
            duration < 1800 + matureDuration -> {
                //Mature stage - do small random changes on spots in intensity map
                generateSpots(0.0025f)
                increaseIntensity(0.005f)
            }
            else -> {
                //Dissipating stage - reduce intensity in spots, delete once intensity is 0
                decreaseIntensity()
            }
        }

        //Change centreX, centreY according to winds
        val mainAirport = radarScreen.airports[radarScreen.mainName] ?: return
        val windVector = Vector2(MathTools.nmToPixel(mainAirport.winds[1] * MathUtils.random(0.8f, 1.4f)) / 360, 0f)
        var hdg = mainAirport.winds[0]
        if (hdg <= 0) hdg = MathUtils.random(1, 360)
        val track = 270 - (hdg - radarScreen.magHdgDev) + MathUtils.random(-20, 20)
        windVector.rotateDeg(track)

        centreX += windVector.x
        centreY += windVector.y
    }

    /** Generates spots at the borders, given a base probability */
    private fun generateSpots(baseProbability: Float) {
        for (spot in HashSet(borderSet)) {
            val x = spot.split(" ")[0].toInt()
            val y = spot.split(" ")[1].toInt()
            val distSqr = x * x + y * y
            val probability = (baseProbability * (15625 - distSqr.toFloat().pow(3 / 2f)).pow(1 / 3f) / 25 * if (intensityMap[spot] ?: continue >= 2) 2.5f else 1f).coerceAtLeast(0.002f)
            var allBordersFilled = true
            for (i in x - 1..x + 1) {
                for (j in y - 1..y + 1) {
                    if (i == 0 && j == 0) continue
                    if (!intensityMap.containsKey("$i $j") && MathUtils.randomBoolean(probability)) {
                        intensityMap["$i $j"] = 1
                        borderSet.add("$i $j")
                    }
                    if (!intensityMap.containsKey("$i $j")) allBordersFilled = false
                }
            }
            if (allBordersFilled) borderSet.remove(spot)
        }
    }

    /** Increase the intensity of spots given a base probability */
    private fun increaseIntensity(baseProbability: Float) {
        for (spot in intensityMap) {
            if (spot.value >= 10) continue
            val x = spot.key.split(" ")[0].toInt()
            val y = spot.key.split(" ")[1].toInt()
            val dist = sqrt((x * x + y * y).toDouble()).toFloat()
            var probability = baseProbability * (25 - dist) / 25
            for (i in x - 1..x + 1) {
                for (j in y - 1..y + 1) {
                    if (i == 0 && j == 0) continue
                    val intensity = intensityMap["$i $j"] ?: continue
                    probability *= when (intensity >= 4) {
                        intensity <= 6 -> 1.2f
                        else -> 1.125f
                    }
                }
            }
            if (MathUtils.randomBoolean(probability)) spot.setValue(spot.value + 1)
        }
    }

    /** Decrease intensity of spots */
    private fun decreaseIntensity() {
        for (spot in HashMap(intensityMap)) {
            val x = spot.key.split(" ")[0].toInt()
            val y = spot.key.split(" ")[1].toInt()
            val dist = sqrt((x * x + y * y).toDouble()).toFloat()
            var probability = 0.05f * (30 - dist) / 30 * (if (spot.value >= 7) 1.5f else 1f)
            if (intensityMap.size <= 100) probability = 0.16f
            if (spot.value > 0 && MathUtils.randomBoolean(probability)) {
                intensityMap[spot.key] = spot.value - 1
                spot.setValue(spot.value - 1)
            }
            if (spot.value <= 0) intensityMap.remove(spot.key)
        }
    }

    /** Checks whether the storm is over and can be deleted */
    fun canBeDeleted(): Boolean {
        return intensityMap.isEmpty()
    }

    /** Draws the thunder cell intensity spots */
    fun renderShape() {
        for (spot in intensityMap) {
            if (spot.value <= 0) continue
            //Each spot is 10 px by 10 px big
            val x = spot.key.split(" ")[0].toInt()
            val y = spot.key.split(" ")[1].toInt()
            radarScreen.shapeRenderer.color = when {
                spot.value <= 2 -> Color.BLUE
                spot.value <= 4 -> Color.LIME
                spot.value <= 6 -> Color.YELLOW
                spot.value <= 8 -> Color.ORANGE
                else -> Color.RED
            }
            radarScreen.shapeRenderer.rect(centreX + x * 10, centreY + y * 10, 10f, 10f)
        }
    }
}