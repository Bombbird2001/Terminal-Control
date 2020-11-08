package com.bombbird.terminalcontrol.sounds

import com.bombbird.terminalcontrol.utilities.saving.FileLoader
import org.apache.commons.lang3.ArrayUtils
import org.apache.commons.lang3.StringUtils
import java.util.*

object Pronunciation {
    val waypointPronunciations = HashMap<String, String>()
    val alphabetPronunciations = HashMap<Char, String>()
    lateinit var callsigns: HashMap<String, String>

    /** Loads the pronunciations  */
    fun loadPronunciation() {
        waypointPronunciations["PAU"] = "pan on"
        waypointPronunciations["LHG"] = "low hon"
        waypointPronunciations["UZH"] = "sheng lian"
        waypointPronunciations["LNG"] = "lung nan"
        waypointPronunciations["MST"] = "mo she toe"
        waypointPronunciations["TVB"] = "tilla vobas"
        waypointPronunciations["LTD"] = "tornado"
        waypointPronunciations["VNS"] = "vanas"
        waypointPronunciations["JCN"] = "jo castin"
        waypointPronunciations["BRO"] = "noble ditto"
        waypointPronunciations["ISE"] = "momo terra"
        alphabetPronunciations['A'] = "alpha"
        alphabetPronunciations['B'] = "bravo"
        alphabetPronunciations['C'] = "charlie"
        alphabetPronunciations['D'] = "delta"
        alphabetPronunciations['E'] = "echo"
        alphabetPronunciations['F'] = "foxtrot"
        alphabetPronunciations['G'] = "golf"
        alphabetPronunciations['H'] = "hotel"
        alphabetPronunciations['I'] = "india"
        alphabetPronunciations['J'] = "juliett"
        alphabetPronunciations['K'] = "kilo"
        alphabetPronunciations['L'] = "lima"
        alphabetPronunciations['M'] = "mike"
        alphabetPronunciations['N'] = "november"
        alphabetPronunciations['O'] = "oscar"
        alphabetPronunciations['P'] = "papa"
        alphabetPronunciations['Q'] = "quebec"
        alphabetPronunciations['R'] = "romeo"
        alphabetPronunciations['S'] = "sierra"
        alphabetPronunciations['T'] = "tango"
        alphabetPronunciations['U'] = "uniform"
        alphabetPronunciations['V'] = "victor"
        alphabetPronunciations['W'] = "whiskey"
        alphabetPronunciations['X'] = "x-ray"
        alphabetPronunciations['Y'] = "yankee"
        alphabetPronunciations['Z'] = "zulu"
        callsigns = FileLoader.loadIcaoCallsigns()
    }

    /** Checks whether direct contains a number  */
    fun checkNumber(direct: String): String {
        val numbers = arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
        val directList = direct.split("".toRegex()).toTypedArray()
        for (i in directList.indices.reversed()) {
            if (ArrayUtils.contains(numbers, directList[i])) return StringUtils.join(directList, " ")
        }
        return direct
    }

    /** Converts any number into text due to special pronunciation requirements for 0, 9 and .  */
    fun convertNoToText(number: String): String {
        val list = number.split("".toRegex()).toTypedArray()
        for (i in list.indices) {
            if (list[i] == "0") list[i] = "zero"
            if (list[i] == "9") list[i] = "niner"
            if (list[i] == ".") list[i] = "decimal"
        }
        return StringUtils.join(list, " ")
    }

    /** Converts any FLXXX in text to flight level X X X, returns joined text  */
    fun convertToFlightLevel(action: String): String {
        val actionList = action.split(" ".toRegex()).toTypedArray()
        for (i in actionList.indices) {
            if (actionList[i].contains("FL")) {
                val altitude = convertNoToText(actionList[i].substring(2))
                val phrase = "flight level$altitude"
                actionList[i] = phrase
            }
        }
        return StringUtils.join(actionList, " ")
    }
}