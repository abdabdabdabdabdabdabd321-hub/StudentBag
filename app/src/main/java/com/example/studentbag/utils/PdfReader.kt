package com.example.studentbag.utils

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

object PdfReader {

    fun readText(context: Context, uri: Uri): String {

        return try {

            // 🔥 مهم جداً (بدونه التطبيق ينهار)
            PDFBoxResourceLoader.init(context)

            val inputStream = context.contentResolver.openInputStream(uri)

            if (inputStream == null) {
                return "فشل فتح الملف"
            }

            val document = PDDocument.load(inputStream)

            val stripper = PDFTextStripper()
            val text = stripper.getText(document)

            document.close()
            inputStream.close()

            text

        } catch (e: Exception) {
            e.printStackTrace()
            "فشل قراءة الملف"
        }
    }
}