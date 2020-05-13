package ru.inforion.lab403.kopycat.interfaces


interface IConfigurator<out O> {
    fun configure(path: String): O
}