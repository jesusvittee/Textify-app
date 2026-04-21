package com.textify.app.data.util

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

class BooleanTypeAdapter : TypeAdapter<Boolean>() {
    override fun write(out: JsonWriter, value: Boolean?) {
        // Al escribir hacia el servidor, usamos el booleano estándar true/false
        out.value(value)
    }

    override fun read(`in`: JsonReader): Boolean? {
        val token = `in`.peek()
        return when (token) {
            JsonToken.BOOLEAN -> `in`.nextBoolean()
            JsonToken.NUMBER -> `in`.nextInt() != 0
            JsonToken.STRING -> `in`.nextString().toBoolean()
            JsonToken.NULL -> {
                `in`.nextNull()
                null
            }
            else -> throw IllegalStateException("Expected BOOLEAN, NUMBER or STRING but was $token")
        }
    }
}
