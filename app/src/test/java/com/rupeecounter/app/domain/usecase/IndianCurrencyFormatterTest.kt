package com.rupeecounter.app.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class IndianCurrencyFormatterTest {

    @Test
    fun testIndianNumberFormatting_zero() {
        assertEquals("0", IndianCurrencyFormatter.formatIndianNumber(0L))
    }

    @Test
    fun testIndianNumberFormatting_belowThousand() {
        assertEquals("5", IndianCurrencyFormatter.formatIndianNumber(5L))
        assertEquals("50", IndianCurrencyFormatter.formatIndianNumber(50L))
        assertEquals("500", IndianCurrencyFormatter.formatIndianNumber(500L))
        assertEquals("999", IndianCurrencyFormatter.formatIndianNumber(999L))
    }

    @Test
    fun testIndianNumberFormatting_thousands() {
        assertEquals("1,000", IndianCurrencyFormatter.formatIndianNumber(1000L))
        assertEquals("10,000", IndianCurrencyFormatter.formatIndianNumber(10000L))
        assertEquals("99,999", IndianCurrencyFormatter.formatIndianNumber(99999L))
    }

    @Test
    fun testIndianNumberFormatting_lakhs() {
        assertEquals("1,00,000", IndianCurrencyFormatter.formatIndianNumber(100000L))
        assertEquals("12,50,000", IndianCurrencyFormatter.formatIndianNumber(1250000L))
        assertEquals("99,99,999", IndianCurrencyFormatter.formatIndianNumber(9999999L))
    }

    @Test
    fun testIndianNumberFormatting_crores() {
        assertEquals("1,00,00,000", IndianCurrencyFormatter.formatIndianNumber(10000000L))
        assertEquals("1,23,45,678", IndianCurrencyFormatter.formatIndianNumber(12345678L))
        assertEquals("12,34,56,789", IndianCurrencyFormatter.formatIndianNumber(123456789L))
    }

    @Test
    fun testCurrencyFormattingWithRupeeSymbol() {
        assertEquals("₹0", IndianCurrencyFormatter.formatCurrency(0L))
        assertEquals("₹5,000", IndianCurrencyFormatter.formatCurrency(5000L))
        assertEquals("₹1,00,000", IndianCurrencyFormatter.formatCurrency(100000L))
        assertEquals("₹12,50,000", IndianCurrencyFormatter.formatCurrency(1250000L))
        assertEquals("₹1,23,45,678", IndianCurrencyFormatter.formatCurrency(12345678L))
    }

    @Test
    fun testParseInputCount_emptyAndBlank() {
        assertEquals(0L, IndianCurrencyFormatter.parseInputCount(""))
        assertEquals(0L, IndianCurrencyFormatter.parseInputCount("   "))
    }

    @Test
    fun testParseInputCount_validNumbers() {
        assertEquals(10L, IndianCurrencyFormatter.parseInputCount("10"))
        assertEquals(500L, IndianCurrencyFormatter.parseInputCount("500"))
        assertEquals(10000L, IndianCurrencyFormatter.parseInputCount("10,000"))
    }

    @Test
    fun testParseInputCount_nonNumericInput() {
        assertEquals(123L, IndianCurrencyFormatter.parseInputCount("abc123xyz"))
        assertEquals(50L, IndianCurrencyFormatter.parseInputCount("₹ 50"))
        assertEquals(0L, IndianCurrencyFormatter.parseInputCount("hello-world"))
    }

    @Test
    fun testParseInputCount_negativeAndDecimals() {
        // Negative sign stripped, treated as positive digits without crash
        assertEquals(50L, IndianCurrencyFormatter.parseInputCount("-50"))
        // Decimal point stripped, remaining digits parsed
        assertEquals(125L, IndianCurrencyFormatter.parseInputCount("12.5"))
    }

    @Test
    fun testParseInputCount_overflowProtection() {
        // Enormous number exceeding Long.MAX_VALUE or MAX_ALLOWED_COUNT
        val hugeNumber = "9999999999999999999999999999"
        val parsed = IndianCurrencyFormatter.parseInputCount(hugeNumber)
        assertEquals(IndianCurrencyFormatter.MAX_ALLOWED_COUNT, parsed)
    }
}
