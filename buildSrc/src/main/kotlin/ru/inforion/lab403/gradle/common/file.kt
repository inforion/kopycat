package ru.inforion.lab403.gradle.common

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.jetbrains.kotlin.gradle.internal.ensureParentDirsCreated
import ru.inforion.lab403.gradle.kodegen.Kodegen
import java.io.File
import java.io.InputStream
import java.security.MessageDigest

fun File.safeWriteText(text: String) {
    ensureParentDirsCreated()
    writeText(text)
}

fun File.safeWriteBytes(bytes: ByteArray) {
    ensureParentDirsCreated()
    writeBytes(bytes)
}

fun File.safeWriteStream(stream: InputStream) {
    val reader = stream.reader()
    safeWriteText(reader.readText())
}

fun File.safeWriteCode(kodegen: Kodegen) {
    safeWriteText(kodegen.toString())
}

fun File.sha1(): ByteArray {
    val digester = MessageDigest.getInstance("SHA1")
    return digester.digest(readBytes())
}

fun createJacksonWithOptions() = jacksonObjectMapper().apply {
    configure(JsonParser.Feature.ALLOW_COMMENTS, true)
    configure(SerializationFeature.INDENT_OUTPUT, true)
    // Java trash json serializer read long as int if:
    //  - it defined as value: Long
    //  - it shorter then long after serialization
    enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, JsonTypeInfo.As.EXISTING_PROPERTY)
}

inline fun <reified T: Any>File.readJson(): T {
    val text = readText()
    return createJacksonWithOptions().readValue(text)
}

inline fun <reified T: Any>File.safeWriteJson(data: T) {
    val text = createJacksonWithOptions().writeValueAsString(data)
    safeWriteText(text)
}