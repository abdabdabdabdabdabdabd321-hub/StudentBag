package com.example.studentbag.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SummaryFileHelper {

    private fun createTempFile(
        context: Context,
        subject: String,
        summary: String
    ): File {

        val folder = File(context.cacheDir, "summaries")

        if (!folder.exists()) {
            folder.mkdirs()
        }

        val time = SimpleDateFormat(
            "yyyy-MM-dd_HH-mm",
            Locale.getDefault()
        ).format(Date())

        val file = File(
            folder,
            "${subject}_$time.txt"
        )

        file.writeText(summary)

        return file
    }

    fun shareSummary(
        context: Context,
        subject: String,
        summary: String
    ) {

        val file = createTempFile(
            context,
            subject,
            summary
        )

        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND)

        intent.type = "text/plain"

        intent.putExtra(Intent.EXTRA_STREAM, uri)

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        context.startActivity(
            Intent.createChooser(
                intent,
                "مشاركة الملخص"
            )
        )
    }

    fun saveSummaryToDownloads(
        context: Context,
        subject: String,
        summary: String
    ): Uri {

        val time = SimpleDateFormat(
            "yyyy-MM-dd_HH-mm",
            Locale.getDefault()
        ).format(Date())

        val fileName = "${subject}_$time.txt"

        val values = ContentValues().apply {

            put(
                MediaStore.Downloads.DISPLAY_NAME,
                fileName
            )

            put(
                MediaStore.Downloads.MIME_TYPE,
                "text/plain"
            )

            put(
                MediaStore.Downloads.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS + "/StudentBag"
            )
        }

        val resolver = context.contentResolver

        val uri = resolver.insert(
            MediaStore.Files.getContentUri("external"),
            values
        )!!

        resolver.openOutputStream(uri)?.use {

            it.write(summary.toByteArray())

        }

        return uri
    }

}