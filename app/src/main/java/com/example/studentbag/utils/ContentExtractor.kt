package com.example.studentbag.utils

import android.content.Context
import android.net.Uri
import com.example.studentbag.database.SubjectContent

object ContentExtractor {

    suspend fun extractAllData(
        context: Context,
        contents: List<SubjectContent>
    ): SummaryData {

        val result = StringBuilder()

        val powerpointFiles = mutableListOf<Uri>()

        val imageFiles = mutableListOf<Uri>()

        for (item in contents) {

            when (item.type) {

                "note" -> {

                    result.append(item.content)
                        .append("\n\n")
                }

                "file" -> {

                    try {

                        val uri = Uri.parse(item.content)

                        val mimeType =
                            context.contentResolver
                                .getType(uri)
                                ?.lowercase() ?: ""

                        if (mimeType.contains("pdf")) {

                            val text =
                                PdfReader.readText(
                                    context,
                                    uri
                                )

                            result.append(text)

                        } else if (
                            mimeType.contains("presentation") ||
                            mimeType.contains("powerpoint") ||
                            mimeType.contains("ms-powerpoint")
                        ) {

                            powerpointFiles.add(uri)
                        }

                        result.append("\n\n")

                    } catch (_: Exception) {
                    }
                }

                "image" -> {

                    imageFiles.add(Uri.parse(item.content))

                }
            }
        }

        return SummaryData(

            text = result.toString(),

            powerpointFiles = powerpointFiles,

            imageFiles = imageFiles

        )
    }
}