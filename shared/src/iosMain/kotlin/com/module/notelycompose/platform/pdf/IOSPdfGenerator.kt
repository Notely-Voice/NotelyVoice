package com.module.notelycompose.platform.pdf

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSData
import platform.Foundation.NSMutableData
import platform.Foundation.NSString
import platform.UIKit.*
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSAttributedStringKey
import platform.Foundation.create
import platform.UIKit.*

@OptIn(ExperimentalForeignApi::class)
class IOSPdfGenerator {

    companion object {
        private const val PAGE_WIDTH = 612.0 // 8.5 inches * 72 points per inch
        private const val PAGE_HEIGHT = 792.0 // 11 inches * 72 points per inch
        private const val MARGIN = 72.0 // 1 inch margin
        private const val LINE_SPACING_FACTOR = 1.2
    }

    private val contentWidth = PAGE_WIDTH - (2 * MARGIN)
    private val contentHeight = PAGE_HEIGHT - (2 * MARGIN)

    /**
     * Create PDF data from text with specified text size
     */
    fun createPDFData(text: String, textSize: Float): NSData {
        val pdfData = NSMutableData()

        // Begin PDF context
        UIGraphicsBeginPDFContextToData(pdfData, CGRectMake(0.0, 0.0, 0.0, 0.0), null)

        // Create font and text attributes
        val font = UIFont.systemFontOfSize(textSize.toDouble())
        val textColor = UIColor.blackColor

        // Create paragraph style
        val paragraphStyle = NSMutableParagraphStyle()
        paragraphStyle.setAlignment(NSTextAlignmentLeft)
        paragraphStyle.setLineSpacing(textSize * 0.2) // 20% line spacing

        // Create text attributes dictionary
        val textAttributes: Map<NSAttributedStringKey?, Any?> = mapOf(
            NSFontAttributeName to font,
            NSForegroundColorAttributeName to textColor,
            NSParagraphStyleAttributeName to paragraphStyle
        )

        // Split text into pages
        val pages = splitTextIntoPages(text, font, contentWidth, contentHeight)

        // Create PDF pages
        for (pageText in pages) {
            addPDFPage(pageText, textAttributes, PAGE_WIDTH, PAGE_HEIGHT, MARGIN, contentWidth, contentHeight)
        }

        UIGraphicsEndPDFContext()

        return pdfData
    }

    private fun addPDFPage(
        text: String,
        textAttributes: Map<NSAttributedStringKey?, Any?>,
        pageWidth: Double,
        pageHeight: Double,
        margin: Double,
        contentWidth: Double,
        contentHeight: Double
    ) {
        val pageRect = CGRectMake(0.0, 0.0, pageWidth, pageHeight)
        UIGraphicsBeginPDFPageWithInfo(pageRect, null)

        val contentRect = CGRectMake(margin, margin, contentWidth, contentHeight)

        // Create NSString first
        val nsString = platform.Foundation.NSString.create(string = text)

        // Draw string with attributes directly
        nsString.drawInRect(contentRect, withAttributes = textAttributes as Map<Any?, *>)
    }

    private fun splitTextIntoPages(
        text: String,
        font: UIFont,
        pageWidth: Double,
        pageHeight: Double
    ): List<String> {
        val pages = mutableListOf<String>()
        val lineHeight = font.lineHeight * LINE_SPACING_FACTOR
        val maxLinesPerPage = (pageHeight / lineHeight).toInt()

        val words = text.split(" ")
        var currentPage = StringBuilder()
        var currentLineCount = 0
        var currentLineWidth = 0.0

        // Create temporary attributes for measuring text
        val measureAttributes: Map<NSAttributedStringKey?, Any?> = mapOf(
            NSFontAttributeName to font
        )

        for (word in words) {
            val wordString = NSString.create(string = "$word ")
            val wordSize = wordString.sizeWithAttributes(measureAttributes as Map<Any?, *>)
            val wordWidth = wordSize.useContents { width }

            // Check if word fits on current line
            if (currentLineWidth + wordWidth <= pageWidth) {
                if (currentPage.isEmpty()) {
                    currentPage.append(word)
                } else {
                    currentPage.append(" $word")
                }
                currentLineWidth += wordWidth
            } else {
                // New line needed
                currentPage.append("\n$word")
                currentLineCount++
                currentLineWidth = wordWidth

                // Check if page is full
                if (currentLineCount >= maxLinesPerPage) {
                    pages.add(currentPage.toString().trim())
                    currentPage = StringBuilder()
                    currentLineCount = 0
                    currentLineWidth = 0.0
                }
            }
        }

        // Add remaining text as last page
        if (currentPage.isNotEmpty()) {
            pages.add(currentPage.toString().trim())
        }

        return if (pages.isEmpty()) listOf("") else pages
    }
}
