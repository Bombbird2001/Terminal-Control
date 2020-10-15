package com.bombbird.terminalcontrol.entities.zones

import com.badlogic.gdx.utils.Array

object ZoneLoader {
    @JvmStatic
    fun loadApchZones(icao: String): Array<ApproachZone> {
        return when (icao) {
            "TCWS" -> loadApchTCWS()
            "TCTT" -> loadApchTCTT()
            "TCAA" -> loadApchTCAA()
            else -> Array()
        }
    }

    private fun loadApchTCWS(): Array<ApproachZone> {
        val approachZones = Array<ApproachZone>()
        approachZones.add(ApproachZone("02L", "02C", 2870.0f, 1594.5f, 23, 0.73f, 25f, 0.329f))
        approachZones.add(ApproachZone("20C", "20R", 2886.2f, 1630.7f, 203, 0.73f, 26f, 0.329f))
        return approachZones
    }

    private fun loadApchTCTT(): Array<ApproachZone> {
        val approachZones = Array<ApproachZone>()
        approachZones.add(ApproachZone("34L", "34R", 2895.4f, 1602.7f, 337, 0.75f, 26f, 0.329f))
        approachZones.add(ApproachZone("22", "23", 2881.8f, 1684.4f, 277, 1.25f, 26f, 0.329f))
        approachZones.add(ApproachZone("16L", "16R", 2876.5f, 1635.1f, 157, 0.75f, 26f, 0.329f))
        return approachZones
    }

    private fun loadApchTCAA(): Array<ApproachZone> {
        val approachZones = Array<ApproachZone>()
        approachZones.add(ApproachZone("34L", "34R", 3863.9f, 2000.4f, 337, 1.18f, 25f, 0.329f))
        approachZones.add(ApproachZone("16L", "16R", 3805.9f, 2098.3f, 157, 1.18f, 25f, 0.329f))
        return approachZones
    }

    @JvmStatic
    fun loadDepZones(icao: String): Array<DepartureZone> {
        return when (icao) {
            "TCTT" -> loadDepTCTT()
            "TCAA" -> loadDepTCAA()
            "TCMD" -> loadDepTCMD()
            "TCPG" -> loadDepTCPG()
            else -> Array()
        }
    }

    private fun loadDepTCTT(): Array<DepartureZone> {
        val departureZones = Array<DepartureZone>()
        departureZones.add(DepartureZone("16L", "16R", 2875.6f, 1636.8f, 157, 3.1f, 10f, 0.329f))
        return departureZones
    }

    private fun loadDepTCAA(): Array<DepartureZone> {
        val departureZones = Array<DepartureZone>()
        departureZones.add(DepartureZone("16L", "16R", 3805.9f, 2098.3f, 157, 3.1f, 19f, 0.329f))
        departureZones.add(DepartureZone("34L", "34R", 3863.9f, 2000.4f, 337, 3.1f, 16f, 0.329f))
        return departureZones
    }

    private fun loadDepTCMD(): Array<DepartureZone> {
        val departureZones = Array<DepartureZone>()
        departureZones.add(DepartureZone("14L", "14R", 2869.7f, 1653.5f, 143, 3.1f, 16f, 0.329f))
        departureZones.add(DepartureZone("36L", "36R", 2871.3f, 1676.2f, 1, 3.1f, 6f, 0.329f))
        return departureZones
    }

    private fun loadDepTCPG(): Array<DepartureZone> {
        val departureZones = Array<DepartureZone>()
        departureZones.add(DepartureZone("08L", "09R", 2837.8f, 1614.6f, 85, 3.1f, 16f, 0.329f))
        departureZones.add(DepartureZone("26R", "27L", 2956.2f, 1624.9f, 265, 3.1f, 10f, 0.329f))
        return departureZones
    }
}