package com.rupeecounter.app.export

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.rupeecounter.app.data.model.CalculationResult
import com.rupeecounter.app.domain.usecase.IndianCurrencyFormatter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfReportGenerator(private val context: Context) {

    fun generateReport(
        calculation: CalculationResult,
        timestamp: Long = System.currentTimeMillis(),
        remarks: String? = null
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 (595 x 842 points)
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        try {
            drawReportContent(canvas, calculation, timestamp, remarks)
        } finally {
            pdfDocument.finishPage(page)
        }

        // Output directory inside app cache
        val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val dateSuffix = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date(timestamp))
        val outputFile = File(reportsDir, "RupeeCounter_$dateSuffix.pdf")

        FileOutputStream(outputFile).use { outputStream ->
            pdfDocument.writeTo(outputStream)
        }
        pdfDocument.close()

        return outputFile
    }

    private fun drawReportContent(
        canvas: Canvas,
        calc: CalculationResult,
        timestamp: Long,
        remarks: String?
    ) {
        val pageWidth = 595f
        val margin = 36f
        val contentWidth = pageWidth - (margin * 2)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Header Banner
        paint.color = Color.rgb(0, 109, 68) // Material Forest Green
        canvas.drawRect(0f, 0f, pageWidth, 75f, paint)

        // Header Title
        paint.color = Color.WHITE
        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("RUPEE COUNTER", margin, 38f, paint)

        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Indian Rupee Denomination Breakdown Report", margin, 58f, paint)

        val dateFormat = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.ENGLISH)
        val dateStr = dateFormat.format(Date(timestamp))
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(dateStr, pageWidth - margin, 48f, paint)
        paint.textAlign = Paint.Align.LEFT

        var y = 100f

        // Optional Remarks
        if (!remarks.isNullOrBlank()) {
            paint.color = Color.DKGRAY
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            canvas.drawText("Remarks: $remarks", margin, y, paint)
            y += 24f
        }

        // 2. Grand Total Card (Summary Box)
        val cardRect = RectF(margin, y, pageWidth - margin, y + 80f)
        paint.color = Color.rgb(238, 248, 242)
        canvas.drawRoundRect(cardRect, 10f, 10f, paint)

        paint.color = Color.rgb(0, 109, 68)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        canvas.drawRoundRect(cardRect, 10f, 10f, paint)
        paint.style = Paint.Style.FILL

        // Grand Total Label
        paint.color = Color.rgb(0, 77, 46)
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("GRAND TOTAL CASH AMOUNT", margin + 16f, y + 26f, paint)

        // Grand Total Value
        paint.textSize = 26f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(calc.formattedGrandTotal, margin + 16f, y + 56f, paint)

        // Summary details on the right
        paint.textAlign = Paint.Align.RIGHT
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.BLACK
        canvas.drawText("Total Notes: ${calc.totalNotesCount}", pageWidth - margin - 16f, y + 26f, paint)
        canvas.drawText("Total Coins: ${calc.totalCoinsCount}", pageWidth - margin - 16f, y + 44f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Total Pieces: ${calc.totalPieces}", pageWidth - margin - 16f, y + 62f, paint)
        paint.textAlign = Paint.Align.LEFT

        y += 92f

        // Amount in Words
        paint.color = Color.rgb(60, 60, 60)
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Amount in Words: ", margin, y, paint)
        val wordsWidth = paint.measureText("Amount in Words: ")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        paint.color = Color.rgb(20, 20, 20)
        canvas.drawText(calc.amountInWords, margin + wordsWidth, y, paint)

        y += 26f

        // 3. Banknotes Table
        y = drawTable(
            canvas = canvas,
            paint = paint,
            startY = y,
            title = "BANKNOTES",
            items = calc.notesItems,
            totalCount = calc.totalNotesCount,
            totalAmount = calc.formattedNotesAmount,
            margin = margin,
            contentWidth = contentWidth,
            headerBgColor = Color.rgb(0, 109, 68)
        )

        y += 20f

        // 4. Coins Table
        drawTable(
            canvas = canvas,
            paint = paint,
            startY = y,
            title = "COINS",
            items = calc.coinsItems,
            totalCount = calc.totalCoinsCount,
            totalAmount = calc.formattedCoinsAmount,
            margin = margin,
            contentWidth = contentWidth,
            headerBgColor = Color.rgb(78, 99, 84)
        )

        // 5. Footer
        paint.color = Color.LTGRAY
        paint.strokeWidth = 1f
        canvas.drawLine(margin, 800f, pageWidth - margin, 800f, paint)

        paint.color = Color.GRAY
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Generated 100% offline by Rupee Counter for Android | Private & Secure", margin, 816f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Page 1 of 1", pageWidth - margin, 816f, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    private fun drawTable(
        canvas: Canvas,
        paint: Paint,
        startY: Float,
        title: String,
        items: List<com.rupeecounter.app.data.model.DenominationItem>,
        totalCount: Long,
        totalAmount: String,
        margin: Float,
        contentWidth: Float,
        headerBgColor: Int
    ): Float {
        var y = startY
        val col1Width = contentWidth * 0.38f

        val col1X = margin
        val col2X = margin + col1Width
        val tableRight = margin + contentWidth

        // Table Header Banner
        paint.color = headerBgColor
        paint.style = Paint.Style.FILL
        canvas.drawRect(margin, y, tableRight, y + 22f, paint)

        paint.color = Color.WHITE
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(title, col1X + 8f, y + 15f, paint)
        canvas.drawText("Count (Pieces)", col2X + 8f, y + 15f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Subtotal (INR)", tableRight - 8f, y + 15f, paint)
        paint.textAlign = Paint.Align.LEFT

        y += 22f

        val rowHeight = 20f
        var isOdd = false

        for (item in items) {
            paint.style = Paint.Style.FILL
            paint.color = if (isOdd) Color.rgb(246, 248, 246) else Color.WHITE
            canvas.drawRect(margin, y, tableRight, y + rowHeight, paint)

            paint.color = Color.rgb(220, 220, 220)
            paint.strokeWidth = 0.5f
            paint.style = Paint.Style.STROKE
            canvas.drawRect(margin, y, tableRight, y + rowHeight, paint)
            paint.style = Paint.Style.FILL

            paint.color = Color.rgb(30, 30, 30)
            paint.textSize = 10.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(item.denomination.displayLabel, col1X + 8f, y + 14f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val countStr = if (item.count > 0L) IndianCurrencyFormatter.formatIndianNumber(item.count) else "0"
            canvas.drawText(countStr, col2X + 8f, y + 14f, paint)

            paint.textAlign = Paint.Align.RIGHT
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(item.formattedSubtotal, tableRight - 8f, y + 14f, paint)
            paint.textAlign = Paint.Align.LEFT

            y += rowHeight
            isOdd = !isOdd
        }

        // Subtotal Footer Row for Table
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(230, 238, 232)
        canvas.drawRect(margin, y, tableRight, y + 22f, paint)

        paint.color = Color.BLACK
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Section Subtotal", col1X + 8f, y + 15f, paint)
        canvas.drawText(IndianCurrencyFormatter.formatIndianNumber(totalCount), col2X + 8f, y + 15f, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(totalAmount, tableRight - 8f, y + 15f, paint)
        paint.textAlign = Paint.Align.LEFT

        y += 22f
        return y
    }
}
