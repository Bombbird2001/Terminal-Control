package com.bombbird.terminalcontrol.entities.approaches

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Queue
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.airports.Airport
import com.bombbird.terminalcontrol.utilities.math.MathTools.nmToPixel
import org.apache.commons.lang3.StringUtils

class OffsetILS(airport: Airport, toParse: String): ILS(airport, toParse) {
    var nonPrecAlts: Queue<FloatArray>? = null
        private set
    var lineUpDist = 0f
        private set
    lateinit var imaginaryIls: ILS
        private set

    init {
        parseLDAInfo(toParse)
        loadImaginaryIls()
        calculateLDARings()
    }

    /** Overrides method in ILS to also load the non precision approach altitudes if applicable  */
    private fun parseLDAInfo(toParse: String) {
        super.parseInfo(toParse)
        val info = toParse.split(",".toRegex()).toTypedArray()
        lineUpDist = info[9].toFloat()
        if (info.size >= 11) {
            isNpa = true
            nonPrecAlts = Queue()
            for (s3 in info[10].split("-".toRegex()).toTypedArray()) {
                val altDist = FloatArray(2)
                for ((index1, s2) in s3.split(">".toRegex()).toTypedArray().withIndex()) {
                    altDist[index1] = s2.toFloat()
                }
                nonPrecAlts?.addLast(altDist)
            }
        }
    }

    /** Loads the imaginary ILS from runway center line  */
    private fun loadImaginaryIls() {
        val text = "IMG" + rwy?.name + "," + rwy?.name + "," + rwy?.heading + "," + rwy?.x + "," + rwy?.y + ",0,0,4000," + StringUtils.join(towerFreq, ">")
        imaginaryIls = ILS(airport, text)
    }

    /** Overrides method in ILS to ignore it if NPA  */
    private fun calculateLDARings() {
        if (!isNpa) {
            super.calculateGsRings()
        } else {
            val gsRings = Array<Vector2>()
            nonPrecAlts?.let {
                for (i in 0 until it.size) {
                    gsRings.add(Vector2(x + nmToPixel(it[i][1]) * MathUtils.cosDeg(270 - heading + (TerminalControl.radarScreen?.magHdgDev ?: 0f)), y + nmToPixel(it[i][1]) * MathUtils.sinDeg(270 - heading + (TerminalControl.radarScreen?.magHdgDev ?: 0f))))
                }
            }
            setGsRings(gsRings)
        }
    }
}