package com.rupeecounter.app.domain.usecase

import com.rupeecounter.app.data.model.Denomination
import com.rupeecounter.app.data.model.DenominationType
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DenominationCalculationTest {

    private lateinit var useCase: CalculateTotalUseCase

    @Before
    fun setUp() {
        useCase = CalculateTotalUseCase()
    }

    @Test
    fun testAllDenominationsIndividually() {
        // Banknotes
        val testCases = listOf(
            Triple(Denomination.NOTE_500, 10L, 5000L),
            Triple(Denomination.NOTE_200, 5L, 1000L),
            Triple(Denomination.NOTE_100, 8L, 800L),
            Triple(Denomination.NOTE_50, 4L, 200L),
            Triple(Denomination.NOTE_20, 10L, 200L),
            Triple(Denomination.NOTE_10, 10L, 100L),
            // Coins
            Triple(Denomination.COIN_20, 5L, 100L),
            Triple(Denomination.COIN_10, 5L, 50L),
            Triple(Denomination.COIN_5, 10L, 50L),
            Triple(Denomination.COIN_2, 10L, 20L),
            Triple(Denomination.COIN_1, 10L, 10L)
        )

        for ((denom, count, expectedSubtotal) in testCases) {
            val result = useCase.execute(mapOf(denom to count))
            assertEquals("Failed for ${denom.name}", expectedSubtotal, result.grandTotal)
            val item = result.items.first { it.denomination == denom }
            assertEquals("Item subtotal mismatch for ${denom.name}", expectedSubtotal, item.subtotal)
            assertEquals("Item count mismatch for ${denom.name}", count, item.count)
        }
    }

    @Test
    fun testTwentyNoteAndTwentyCoinAreSeparateEntries() {
        val result = useCase.execute(
            mapOf(
                Denomination.NOTE_20 to 10L, // 10 * 20 = 200
                Denomination.COIN_20 to 5L   // 5 * 20 = 100
            )
        )

        assertEquals(2, result.items.count { it.denomination.value == 20L })
        val note20 = result.items.first { it.denomination == Denomination.NOTE_20 }
        val coin20 = result.items.first { it.denomination == Denomination.COIN_20 }

        assertEquals(DenominationType.NOTE, note20.denomination.type)
        assertEquals(DenominationType.COIN, coin20.denomination.type)
        assertEquals(10L, note20.count)
        assertEquals(200L, note20.subtotal)
        assertEquals(5L, coin20.count)
        assertEquals(100L, coin20.subtotal)
        assertEquals(300L, result.grandTotal)
        assertEquals(10L, result.totalNotesCount)
        assertEquals(5L, result.totalCoinsCount)
        assertEquals(15L, result.totalPieces)
    }

    @Test
    fun testTenNoteAndTenCoinAreSeparateEntries() {
        val result = useCase.execute(
            mapOf(
                Denomination.NOTE_10 to 8L, // 8 * 10 = 80
                Denomination.COIN_10 to 4L  // 4 * 10 = 40
            )
        )

        val note10 = result.items.first { it.denomination == Denomination.NOTE_10 }
        val coin10 = result.items.first { it.denomination == Denomination.COIN_10 }

        assertEquals(80L, note10.subtotal)
        assertEquals(40L, coin10.subtotal)
        assertEquals(120L, result.grandTotal)
    }

    @Test
    fun testSpecificationExampleOne() {
        // Spec Example:
        // ₹500 - 10 notes = ₹5,000
        // ₹200 - 5 notes = ₹1,000
        // ₹100 - 8 notes = ₹800
        // ₹50 - 4 notes = ₹200
        // ₹20 - 10 notes = ₹200
        // ₹10 - 10 notes = ₹100
        // Total = ₹7,300
        val counts = mapOf(
            Denomination.NOTE_500 to 10L,
            Denomination.NOTE_200 to 5L,
            Denomination.NOTE_100 to 8L,
            Denomination.NOTE_50 to 4L,
            Denomination.NOTE_20 to 10L,
            Denomination.NOTE_10 to 10L
        )

        val result = useCase.execute(counts)

        assertEquals(7300L, result.grandTotal)
        assertEquals("₹7,300", result.formattedGrandTotal)
        assertEquals(47L, result.totalNotesCount)
        assertEquals(0L, result.totalCoinsCount)
        assertEquals(47L, result.totalPieces)
        assertEquals("Seven Thousand Three Hundred Rupees Only", result.amountInWords)
    }

    @Test
    fun testSpecificationExampleTwo_SessionRestorationExample() {
        // Spec Example 2:
        // ₹500 = 20 (10,000)
        // ₹200 = 10 (2,000)
        // ₹100 = 15 (1,500)
        // ₹50 = 5 (250)
        // ₹20 = 10 (200)
        // ₹10 = 5 (50)
        // ₹5 coin = 10 (50)
        // Total = ₹14,050
        val counts = mapOf(
            Denomination.NOTE_500 to 20L,
            Denomination.NOTE_200 to 10L,
            Denomination.NOTE_100 to 15L,
            Denomination.NOTE_50 to 5L,
            Denomination.NOTE_20 to 10L,
            Denomination.NOTE_10 to 5L,
            Denomination.COIN_5 to 10L
        )

        val result = useCase.execute(counts)

        assertEquals(14050L, result.grandTotal)
        assertEquals("₹14,050", result.formattedGrandTotal)
        assertEquals(65L, result.totalNotesCount)
        assertEquals(10L, result.totalCoinsCount)
        assertEquals(75L, result.totalPieces)
        assertEquals(14000L, result.totalNotesAmount)
        assertEquals(50L, result.totalCoinsAmount)
        assertEquals("Fourteen Thousand Fifty Rupees Only", result.amountInWords)
    }

    @Test
    fun testAllDenominationsTogether() {
        val counts = mapOf(
            Denomination.NOTE_500 to 2L, // 1000
            Denomination.NOTE_200 to 2L, // 400
            Denomination.NOTE_100 to 2L, // 200
            Denomination.NOTE_50 to 2L,  // 100
            Denomination.NOTE_20 to 2L,  // 40
            Denomination.NOTE_10 to 2L,  // 20
            Denomination.COIN_20 to 2L,  // 40
            Denomination.COIN_10 to 2L,  // 20
            Denomination.COIN_5 to 2L,   // 10
            Denomination.COIN_2 to 2L,   // 4
            Denomination.COIN_1 to 2L    // 2
        )

        val result = useCase.execute(counts)
        val expected = 1000 + 400 + 200 + 100 + 40 + 20 + 40 + 20 + 10 + 4 + 2L
        assertEquals(expected, result.grandTotal)
        assertEquals(12L, result.totalNotesCount)
        assertEquals(10L, result.totalCoinsCount)
        assertEquals(22L, result.totalPieces)
    }

    @Test
    fun testZeroAndNegativeCountsHandling() {
        val counts = mapOf(
            Denomination.NOTE_500 to 0L,
            Denomination.NOTE_200 to -10L // negative coerced to 0
        )
        val result = useCase.execute(counts)
        assertEquals(0L, result.grandTotal)
        assertEquals("₹0", result.formattedGrandTotal)
    }

    @Test
    fun testLargeCountsSafeArithmetic() {
        val counts = mapOf(
            Denomination.NOTE_500 to 1_000_000L, // 500,000,000 (50 Crore)
            Denomination.NOTE_100 to 1_000_000L  // 100,000,000 (10 Crore)
        )
        val result = useCase.execute(counts)
        assertEquals(600_000_000L, result.grandTotal)
        assertEquals("₹60,00,00,000", result.formattedGrandTotal)
        assertEquals("Sixty Crore Rupees Only", result.amountInWords)
    }
}
