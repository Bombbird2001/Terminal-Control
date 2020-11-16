package com.bombbird.terminalcontrol.sounds

import com.bombbird.terminalcontrol.utilities.saving.FileLoader
import org.apache.commons.lang3.StringUtils
import kotlin.collections.HashMap

object Pronunciation {
    val waypointPronunciations = HashMap<String, String>()
    val alphabetPronunciations = HashMap<Char, String>()
    val numberPronunciations = HashMap<Char, String>()
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
        numberPronunciations['0'] = "zero"
        numberPronunciations['1'] = "one"
        numberPronunciations['2'] = "two"
        numberPronunciations['3'] = "three"
        numberPronunciations['4'] = "four"
        numberPronunciations['5'] = "five"
        numberPronunciations['6'] = "six"
        numberPronunciations['7'] = "seven"
        numberPronunciations['8'] = "eight"
        numberPronunciations['9'] = "niner"
        numberPronunciations['.'] = "decimal"
        callsigns = FileLoader.loadIcaoCallsigns()
    }

    /** Checks whether direct contains a number  */
    fun checkNumber(direct: String): String {
        val directList = direct.toCharArray()
        for (i in directList) {
            if (numberPronunciations.containsKey(i)) return StringUtils.join(directList, " ")
        }
        return direct
    }

    /** Converts any number into text due to special pronunciation requirements for 0, 9 and .  */
    fun convertNoToText(number: String): String {
        val list = number.toCharArray()
        val newList = ArrayList<String>()
        for (i in list) {
            newList.add(numberPronunciations[i] ?: i.toString())
        }
        return StringUtils.join(newList, " ")
    }

    /** Converts any FLXXX in text to flight level X X X, returns joined text  */
    fun convertToFlightLevel(action: String): String {
        val actionList = action.split(" ".toRegex()).toTypedArray()
        for (i in actionList.indices) {
            if (actionList[i].contains("FL")) {
                val altitude = convertNoToText(actionList[i].substring(2))
                val phrase = "flight level $altitude"
                actionList[i] = phrase
            }
        }
        return StringUtils.join(actionList, " ")
    }
}