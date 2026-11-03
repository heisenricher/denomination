package com.rupeecounter.app.data.model

data class DenominationItem(
    val denomination: Denomination,
    val count: Long,
    val subtotal: Long,
    val formattedSubtotal: String
)

data class CalculationResult(
    val items: List<DenominationItem>,
    val notesItems: List<DenominationItem>,
    val coinsItems: List<DenominationItem>,
    val totalNotesCount: Long,
    val totalCoinsCount: Long,
    val totalPieces: Long,
    val totalNotesAmount: Long,
    val totalCoinsAmount: Long,
    val grandTotal: Long,
    val formattedGrandTotal: String,
    val formattedNotesAmount: String,
    val formattedCoinsAmount: String,
    val amountInWords: String
) {
    companion object {
        val EMPTY = CalculationResult(
            items = emptyList(),
            notesItems = emptyList(),
            coinsItems = emptyList(),
            totalNotesCount = 0L,
            totalCoinsCount = 0L,
            totalPieces = 0L,
            totalNotesAmount = 0L,
            totalCoinsAmount = 0L,
            grandTotal = 0L,
            formattedGrandTotal = "₹0",
            formattedNotesAmount = "₹0",
            formattedCoinsAmount = "₹0",
            amountInWords = "Zero Rupees Only"
        )
    }
}
