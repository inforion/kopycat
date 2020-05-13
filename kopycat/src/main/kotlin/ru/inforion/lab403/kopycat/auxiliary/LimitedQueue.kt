package ru.inforion.lab403.kopycat.auxiliary

import java.util.*

class LimitedQueue<T>(val limit: Int) : LinkedList<T>() {
    override fun add(element: T): Boolean {
        val added = super.add(element)
        while (added && this.size > limit) {
            remove()
        }
        return added
    }
}