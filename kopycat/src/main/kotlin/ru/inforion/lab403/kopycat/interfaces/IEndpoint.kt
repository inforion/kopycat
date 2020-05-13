package ru.inforion.lab403.kopycat.interfaces


interface IEndpoint<T: Enum<T>> {
    fun recv(channel: T, data: ByteArray): Boolean
    fun send(channel: T): ByteArray?
    fun timestamp(): Long
    val maxBufferSize: Int
}
