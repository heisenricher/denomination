package com.rupeecounter.app.data.model

enum class DenominationType {
    NOTE,
    COIN
}

enum class Denomination(
    val value: Long,
    val type: DenominationType,
    val key: String,
    val displayLabel: String,
    val order: Int
) {
    // Banknotes
    NOTE_500(500L, DenominationType.NOTE, "NOTE_500", "₹500", 1),
    NOTE_200(200L, DenominationType.NOTE, "NOTE_200", "₹200", 2),
    NOTE_100(100L, DenominationType.NOTE, "NOTE_100", "₹100", 3),
    NOTE_50(50L, DenominationType.NOTE, "NOTE_50", "₹50", 4),
    NOTE_20(20L, DenominationType.NOTE, "NOTE_20", "₹20", 5),
    NOTE_10(10L, DenominationType.NOTE, "NOTE_10", "₹10", 6),

    // Coins
    COIN_20(20L, DenominationType.COIN, "COIN_20", "₹20", 7),
    COIN_10(10L, DenominationType.COIN, "COIN_10", "₹10", 8),
    COIN_5(5L, DenominationType.COIN, "COIN_5", "₹5", 9),
    COIN_2(2L, DenominationType.COIN, "COIN_2", "₹2", 10),
    COIN_1(1L, DenominationType.COIN, "COIN_1", "₹1", 11);

    companion object {
        val notes: List<Denomination> = entries.filter { it.type == DenominationType.NOTE }
        val coins: List<Denomination> = entries.filter { it.type == DenominationType.COIN }

        fun fromKey(key: String): Denomination? = entries.find { it.key == key }
    }
}
