package com.rupeecounter.app.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class NumberToWordsConverterTest {

    @Test
    fun testZero() {
        assertEquals("Zero Rupees Only", NumberToWordsConverter.convertToWords(0L))
    }

    @Test
    fun testBasicUnits() {
        assertEquals("Five Rupees Only", NumberToWordsConverter.convertToWords(5L))
        assertEquals("Ten Rupees Only", NumberToWordsConverter.convertToWords(10L))
        assertEquals("Nineteen Rupees Only", NumberToWordsConverter.convertToWords(19L))
        assertEquals("Twenty Rupees Only", NumberToWordsConverter.convertToWords(20L))
        assertEquals("Fifty Rupees Only", NumberToWordsConverter.convertToWords(50L))
    }

    @Test
    fun testHundredsAndThousands() {
        assertEquals("Five Hundred Rupees Only", NumberToWordsConverter.convertToWords(500L))
        assertEquals("One Thousand Rupees Only", NumberToWordsConverter.convertToWords(1000L))
        assertEquals("Seven Thousand Four Hundred Rupees Only", NumberToWordsConverter.convertToWords(7400L))
    }

    @Test
    fun testLakhsAndCrores() {
        assertEquals("One Lakh Rupees Only", NumberToWordsConverter.convertToWords(100000L))
        assertEquals("Twelve Lakh Fifty Thousand Rupees Only", NumberToWordsConverter.convertToWords(1250000L))
        assertEquals("One Crore Twenty-Three Lakh Forty-Five Thousand Six Hundred Seventy-Eight Rupees Only",
            NumberToWordsConverter.convertToWords(12345678L))
    }
}
