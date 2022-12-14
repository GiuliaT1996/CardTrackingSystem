package com.angiuprojects.cardtrackingapp.utilities

import com.angiuprojects.cardtrackingapp.entities.Card
import com.angiuprojects.cardtrackingapp.entities.Settings
import kotlinx.coroutines.Job

class Constants() {

    //singleton - same as using static in Java
    companion object {

        private lateinit var constantsIstance: Constants

        fun initializeConstantSingleton(): Constants {
            constantsIstance = Constants()
            return constantsIstance
        }

        fun getInstance(): Constants {
            return constantsIstance
        }
    }

    var cards: MutableList<Card>? = null

    fun getInstanceCards(): MutableList<Card>? {
        if (cards == null) {
            cards = ArrayList()
        }
        return cards
    }

    var settings: MutableList<Settings>? = null

    fun getInstanceSettings(): MutableList<Settings>? {
        if (settings == null) {
            settings = ArrayList()
        }
        return settings
    }

    val CARD_TRACKING_DEBUGGER = "CardTrackingDebugger"

    val fieldList = mutableListOf("Archetipo", "Duellante", "Set", "Prezzo")

    val priceRange = mutableListOf("N/A", "< 1,00", "1,00 < x < 2,50", "2,50 < x < 10", "> 10")

    val pricey = 2.0
}