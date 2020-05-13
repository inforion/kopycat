package ru.inforion.lab403.kopycat.interfaces

import ru.inforion.lab403.kopycat.cores.base.GenericSerializer

/**
 * {RU}
 * Любой объект, который может быть сохранён snapshot-е должет быть унаследован от этого интерфейса.
 * {RU}
 */
interface ISerializable {
    /**
     * {RU}Сериализация объекта с использованием контекста <ctxt> в Map из строки.{RU}
     *
     * {EN}
     * Serialize object using specified context <ctxt> into Map from string to whatever you want.
     * Final Map should ended with Map<String, String>
     * {EN}
     */
    fun serialize(ctxt: GenericSerializer): Map<String, Any>

    /**
     * {RU}Загрузка snapshot-а с использованием указанного контекста <ctxt> из любого типа.{RU}
     *
     * {EN}
     * Loading snapshot using specified deserialization context <ctxt> from any type
     * Peripheral legacy support
     * {EN}
     */
    @Deprecated("Old legacy support")
    fun deserialize(ctxt: GenericSerializer, snapshot: Any) { }

    /**
     * {RU}Загрузка snapshot-а с использованием указанного контекста <ctxt> из Map<String, Any>{RU}
     *
     * {EN}Loading snapshot using specified deserialization context <ctxt> from Map<String, Any>{EN}
     */
    fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>)

    /**
     * {RU}
     * Восстановить последний загруженное или сериализованное состояние snapshot используя
     * указанный контекст десериализации <ctxt> из Map<String, Any>
     * {RU}
     *
     * {EN}
     * Restore to last loaded or serialized snapshot state using specified
     * deserialization context <ctxt> from Map<String, Any>
     * {EN}
     */
    fun restore(ctxt: GenericSerializer, snapshot: Map<String, Any>) = deserialize(ctxt, snapshot)
}