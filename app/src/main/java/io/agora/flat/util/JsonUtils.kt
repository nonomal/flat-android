package io.agora.flat.util

import com.google.gson.Gson

object JsonUtils {
    val gson = Gson()

    fun toJson(src: Any): String {
        return gson.toJson(src)
    }
}