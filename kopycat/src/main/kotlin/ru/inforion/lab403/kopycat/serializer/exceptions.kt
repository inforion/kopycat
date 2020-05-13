package ru.inforion.lab403.kopycat.serializer


class NotSerializableObjectException(override val message: String?) : Exception()
class NotDeserializableObjectException(override val message: String) : Exception()