package com.ultraone.nottie.util

import android.net.Uri

const val IMAGE_REQUEST_CODE: Int = 1001
const val FILE_REQUEST_CODE: Int = 1002

data class Request(val data: Uri?)
