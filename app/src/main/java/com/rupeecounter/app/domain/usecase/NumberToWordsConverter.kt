package com.rupeecounter.app.domain.usecase

object NumberToWordsConverter {

    private val units = arrayOf(
        "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
        "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
        "Seventeen", "Eighteen", "Nineteen"
    )

    private val tens = arrayOf(
        "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    )

    /**
     * Converts a Rupee amount into Indian Words representation:
     * e.g., 100000 -> "One Lakh Rupees Only"
     * e.g., 1250000 -> "Twelve Lakh Fifty Thousand Rupees Only"
     * e.g., 12345678 -> "One Crore Twenty-Three Lakh Forty-Five Thousand Six Hundred Seventy-Eight Rupees Only"
     */
    fun convertToWords(amount: Long): String {
        if (amount == 0L) return "Zero Rupees Only"
        if (amount < 0L) return "Minus " + convertToWords(-amount)

        val words = convertNumberToWords(amount)
        return "$words Rupees Only"
    }

    private fun convertNumberToWords(number: Long): String {
        if (number == 0L) return ""

        val sb = StringBuilder()

        val crores = number / 10_000_000L
        var remaining = number % 10_000_000L

        if (crores > 0) {
            sb.append(convertNumberToWords(crores)).append(" Crore ")
        }

        val lakhs = remaining / 100_000L
        remaining %= 100_000L

        if (lakhs > 0) {
            sb.append(formatTensAndUnits(lakhs)).append(" Lakh ")
        }

        val thousands = remaining / 1_000L
        remaining %= 1_000L

        if (thousands > 0) {
            sb.append(formatTensAndUnits(thousands)).append(" Thousand ")
        }

        val hundreds = remaining / 100L
        remaining %= 100L

        if (hundreds > 0) {
            sb.append(units[hundreds.toInt()]).append(" Hundred ")
        }

        if (remaining > 0) {
            sb.append(formatTensAndUnits(remaining)).append(" ")
        }

        return sb.toString().trim().replace("\\s+".toRegex(), " ")
    }

    private fun formatTensAndUnits(value: Long): String {
        val num = (value % 100).toInt()
        val hundreds = (value / 100).toInt()

        val sb = StringBuilder()
        if (hundreds > 0) {
            sb.append(units[hundreds]).append(" Hundred ")
        }

        if (num > 0) {
            if (num < 20) {
                sb.append(units[num])
            } else {
                val t = num / 10
                val u = num % 10
                if (u > 0) {
                    sb.append(tens[t]).append("-").append(units[u])
                } else {
                    sb.append(tens[t])
                }
            }
        }

        return sb.toString().trim()
    }
}
