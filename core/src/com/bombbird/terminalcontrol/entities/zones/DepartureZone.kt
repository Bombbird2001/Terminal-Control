package com.bombbird.terminalcontrol.entities.zones

class DepartureZone(rwy1: String, rwy2: String, xMid: Float, yMid: Float, depHdg: Int, nozWidth: Float, nozLength: Float, ntzWidth: Float) : ApproachZone(rwy1, rwy2, xMid, yMid, depHdg + 180, nozWidth, nozLength, ntzWidth)