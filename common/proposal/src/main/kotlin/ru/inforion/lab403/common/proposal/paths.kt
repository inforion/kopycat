package ru.inforion.lab403.common.proposal

import java.io.File

/**
 * {EN}
 * Join to string by system path separator
 *
 * @param other string to append to path
 *
 *
 * @return strings joined by system path separator
 * {EN}
 */
infix fun String.join(other: String): String = File(this, other).path
