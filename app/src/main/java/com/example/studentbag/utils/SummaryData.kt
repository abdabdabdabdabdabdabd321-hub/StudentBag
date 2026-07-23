package com.example.studentbag.utils

import android.net.Uri

data class SummaryData(

    val text: String,

    val powerpointFiles: List<Uri>,

    val imageFiles: List<Uri>

)