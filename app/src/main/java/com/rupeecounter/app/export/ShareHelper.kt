package com.rupeecounter.app.export

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.rupeecounter.app.data.model.CalculationResult
import com.rupeecounter.app.domain.usecase.IndianCurrencyFormatter
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ShareHelper {

    fun buildTextSummary(
        calculation: CalculationResult,
        timestamp: Long = System.currentTimeMillis(),
        remarks: String? = null
    ): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
        val dateStr = dateFormat.format(Date(timestamp))

        val sb = StringBuilder()
        sb.append("Rupee Counter - Denomination Summary\n")
        sb.append("Date: $dateStr\n")
        if (!remarks.isNullOrBlank()) {
            sb.append("Remarks: $remarks\n")
        }
        sb.append("----------------------------------------\n")
        sb.append("BANKNOTES:\n")
        for (item in calculation.notesItems) {
            val countStr = IndianCurrencyFormatter.formatIndianNumber(item.count)
            sb.append("  ${item.denomination.displayLabel.padEnd(6)} x ${countStr.padEnd(8)} = ${item.formattedSubtotal}\n")
        }
        sb.append("Notes Subtotal: ${IndianCurrencyFormatter.formatIndianNumber(calculation.totalNotesCount)} notes = ${calculation.formattedNotesAmount}\n")
        sb.append("----------------------------------------\n")
        sb.append("COINS:\n")
        for (item in calculation.coinsItems) {
            val countStr = IndianCurrencyFormatter.formatIndianNumber(item.count)
            sb.append("  ${item.denomination.displayLabel.padEnd(6)} x ${countStr.padEnd(8)} = ${item.formattedSubtotal}\n")
        }
        sb.append("Coins Subtotal: ${IndianCurrencyFormatter.formatIndianNumber(calculation.totalCoinsCount)} coins = ${calculation.formattedCoinsAmount}\n")
        sb.append("----------------------------------------\n")
        sb.append("TOTAL PIECES: ${IndianCurrencyFormatter.formatIndianNumber(calculation.totalPieces)}\n")
        sb.append("GRAND TOTAL:  ${calculation.formattedGrandTotal}\n")
        sb.append("(${calculation.amountInWords})\n")
        sb.append("----------------------------------------\n")
        sb.append("Counted with Rupee Counter (100% Offline)")

        return sb.toString()
    }

    fun sharePdf(context: Context, pdfFile: File, title: String = "Rupee Counter Report") {
        try {
            if (!pdfFile.exists()) {
                Toast.makeText(context, "Report file not found", Toast.LENGTH_SHORT).show()
                return
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "Indian Rupee Denomination Report")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share PDF Report via")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "No application found to share PDF", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to share PDF: ${e.localizedMessage ?: "Unknown error"}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareText(context: Context, text: String, title: String = "Rupee Counter Summary") {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, text)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Summary via")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "No application found to share text", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to share: ${e.localizedMessage ?: "Unknown error"}", Toast.LENGTH_SHORT).show()
        }
    }
}
