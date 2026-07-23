package com.example.studentbag.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

object ServerApi {

    private const val SERVER_URL =
        "https://studentbag-backend-production.up.railway.app/summarize"

    suspend fun generateSummary(
        context: Context,
        data: SummaryData
    ): String {

        val temporaryFiles = mutableListOf<File>()

        return try {

            val bodyBuilder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)

            // نصوص PDF والملاحظات
            bodyBuilder.addFormDataPart(
                "text",
                data.text
            )

            // إرسال جميع الصور
            data.imageFiles.forEachIndexed { index, uri ->

                try {

                    val originalBitmap =
                        context.contentResolver
                            .openInputStream(uri)
                            ?.use { inputStream ->
                                BitmapFactory.decodeStream(inputStream)
                            }

                    if (originalBitmap == null) {

                        Log.e(
                            "ServerApi",
                            "تعذر قراءة الصورة رقم ${index + 1}: $uri"
                        )

                        return@forEachIndexed
                    }

                    val maxWidth = 1280

                    val resizedBitmap =
                        if (originalBitmap.width > maxWidth) {

                            val ratio =
                                originalBitmap.height.toFloat() /
                                        originalBitmap.width.toFloat()

                            Bitmap.createScaledBitmap(
                                originalBitmap,
                                maxWidth,
                                (maxWidth * ratio).toInt(),
                                true
                            )

                        } else {

                            originalBitmap
                        }

                    val tempFile = File.createTempFile(
                        "studentbag_image_${index + 1}_",
                        ".jpg",
                        context.cacheDir
                    )

                    temporaryFiles.add(tempFile)

                    tempFile.outputStream().use { outputStream ->

                        val success = resizedBitmap.compress(
                            Bitmap.CompressFormat.JPEG,
                            85,
                            outputStream
                        )

                        if (!success) {

                            Log.e(
                                "ServerApi",
                                "فشل ضغط الصورة رقم ${index + 1}"
                            )
                        }
                    }

                    bodyBuilder.addFormDataPart(
                        "files",
                        "image_${index + 1}.jpg",
                        tempFile.asRequestBody(
                            "image/jpeg".toMediaType()
                        )
                    )

                    if (resizedBitmap !== originalBitmap) {
                        resizedBitmap.recycle()
                    }

                    originalBitmap.recycle()

                    Log.d(
                        "ServerApi",
                        "تم تجهيز الصورة رقم ${index + 1}"
                    )

                } catch (e: Exception) {

                    Log.e(
                        "ServerApi",
                        "فشل تجهيز الصورة رقم ${index + 1}",
                        e
                    )
                }
            }

            val request = Request.Builder()
                .url(SERVER_URL)
                .post(bodyBuilder.build())
                .build()

            val client = OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .writeTimeout(300, TimeUnit.SECONDS)
                .callTimeout(300, TimeUnit.SECONDS)
                .build()

            client.newCall(request).execute().use { response ->

                val responseText =
                    response.body?.string().orEmpty()

                if (!response.isSuccessful) {

                    Log.e(
                        "ServerApi",
                        "Server error ${response.code}: $responseText"
                    )

                    return "فشل الاتصال بالسيرفر: ${response.code}"
                }

                if (responseText.isBlank()) {
                    return "السيرفر أعاد استجابة فارغة"
                }

                val result = JSONObject(responseText)

                result.optString(
                    "summary",
                    "لم يتم إنشاء الملخص"
                )
            }

        } catch (e: Exception) {

            Log.e(
                "ServerApi",
                "FULL ERROR",
                e
            )

            "حدث خطأ أثناء التلخيص: ${e.message ?: e.javaClass.simpleName}"

        } finally {

            temporaryFiles.forEach { file ->

                try {
                    file.delete()
                } catch (_: Exception) {
                }
            }
        }
    }
}