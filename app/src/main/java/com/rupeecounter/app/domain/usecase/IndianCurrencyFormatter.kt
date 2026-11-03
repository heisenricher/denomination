package com.rupeecounter.app.domain.usecase

object IndianCurrencyFormatter {

    const val MAX_ALLOWED_COUNT: Long = 999_999_999L

    /**
     * Formats a number according to the Indian numbering system:
     * Rightmost 3 digits group, then 2 digits per group.
     * e.g., 100000 -> "1,00,000", 12345678 -> "1,23,45,678"
     */
    fun formatIndianNumber(value: Long): String {
        if (value == 0L) return "0"
        val isNegative = value < 0
        val absVal = if (isNegative) {
            if (value == Long.MIN_VALUE) Long.MAX_VALUE else -value
        } else {
            value
        }

        val digits = absVal.toString()
        val len = digits.length

        if (len <= 3) {
            return if (isNegative) "-$digits" else digits
        }

        val lastThree = digits.substring(len - 3)
        val remaining = digits.substring(0, len - 3)

        val sb = StringBuilder()
        var count = 0
        for (i in remaining.length - 1 downTo 0) {
            sb.append(remaining[i])
            count++
            if (count % 2 == 0 && i > 0) {
                sb.append(',')
            }
        }
        val formattedRemaining = sb.reverse().toString()
        val result = "$formattedRemaining,$lastThree"
        return if (isNegative) "-$result" else result
    }

    /**
     * Formats an amount with the Indian Rupee symbol:
     * e.g., 5000 -> "₹5,000", 100000 -> "₹1,00,000"
     */
    fun formatCurrency(amount: Long): String {
        return "₹${formatIndianNumber(amount)}"
    }

    /**
     * Safely sanitizes user input string:
     * Keeps only digits, removes commas, whitespace, leading zeros, handles overflow.
     * Empty or invalid returns 0L.
     */
    fun parseInputCount(raw: String): Long {
        if (raw.isBlank()) return 0L
        val digitsOnly = raw.filter { it.isDigit() }
        if (digitsOnly.isEmpty()) return 0L

        return try {
            val parsed = digitsOnly.toLong()
            if (parsed > MAX_ALLOWED_COUNT) {
                MAX_ALLOWED_COUNT
            } else {
                parsed
            }
        } catch (_: NumberFormatException) {
            MAX_ALLOWED_COUNT
        }
    }
}
