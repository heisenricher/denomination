package com.rupeecounter.app.domain.usecase

import com.rupeecounter.app.data.model.CalculationResult
import com.rupeecounter.app.data.model.Denomination
import com.rupeecounter.app.data.model.DenominationItem
import com.rupeecounter.app.data.model.DenominationType

class CalculateTotalUseCase {

    fun execute(counts: Map<Denomination, Long>): CalculationResult {
        val items = mutableListOf<DenominationItem>()
        var totalNotesCount = 0L
        var totalCoinsCount = 0L
        var totalNotesAmount = 0L
        var totalCoinsAmount = 0L

        // Order according to the defined denomination list
        for (denom in Denomination.entries) {
            val count = (counts[denom] ?: 0L).coerceIn(0L, IndianCurrencyFormatter.MAX_ALLOWED_COUNT)
            val subtotal = count * denom.value
            val formattedSubtotal = IndianCurrencyFormatter.formatCurrency(subtotal)

            val item = DenominationItem(
                denomination = denom,
                count = count,
                subtotal = subtotal,
                formattedSubtotal = formattedSubtotal
            )
            items.add(item)

            if (denom.type == DenominationType.NOTE) {
                totalNotesCount += count
                totalNotesAmount += subtotal
            } else {
                totalCoinsCount += count
                totalCoinsAmount += subtotal
            }
        }

        val grandTotal = totalNotesAmount + totalCoinsAmount
        val totalPieces = totalNotesCount + totalCoinsCount

        val notesItems = items.filter { it.denomination.type == DenominationType.NOTE }
        val coinsItems = items.filter { it.denomination.type == DenominationType.COIN }

        return CalculationResult(
            items = items,
            notesItems = notesItems,
            coinsItems = coinsItems,
            totalNotesCount = totalNotesCount,
            totalCoinsCount = totalCoinsCount,
            totalPieces = totalPieces,
            totalNotesAmount = totalNotesAmount,
            totalCoinsAmount = totalCoinsAmount,
            grandTotal = grandTotal,
            formattedGrandTotal = IndianCurrencyFormatter.formatCurrency(grandTotal),
            formattedNotesAmount = IndianCurrencyFormatter.formatCurrency(totalNotesAmount),
            formattedCoinsAmount = IndianCurrencyFormatter.formatCurrency(totalCoinsAmount),
            amountInWords = NumberToWordsConverter.convertToWords(grandTotal)
        )
    }
}
