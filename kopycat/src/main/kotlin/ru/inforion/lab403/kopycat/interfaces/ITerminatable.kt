package ru.inforion.lab403.kopycat.interfaces


interface ITerminatable {
    /**
     * {EN}
     * Will be executed at the last of the device life
     * No sensible operation can be performed after it
     * {EN}
     *
     * {RU}
     * Этот метод будет выполнен в конце срока службы устройства
     * Никакая операция не может быть выполнена после этого
     * {RU}
     */
    fun terminate() {  }
}