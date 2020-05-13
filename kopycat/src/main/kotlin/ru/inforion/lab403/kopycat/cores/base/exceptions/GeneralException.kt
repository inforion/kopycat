package ru.inforion.lab403.kopycat.cores.base.exceptions


open class GeneralException(message: String? = null) : Exception(message) {
    val type: String get() = this::class.java.simpleName
    val prefix: String get() = type

    override fun toString(): String {
        val msg = if (message != null) ": %s".format(message) else ""
        return "$prefix$msg"
    }
}