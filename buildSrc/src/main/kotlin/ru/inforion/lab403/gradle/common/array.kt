package ru.inforion.lab403.gradle.common

import java.security.MessageDigest

fun ByteArray.sha1(): ByteArray {
    val digester = MessageDigest.getInstance("SHA1")
    return digester.digest(this)
}