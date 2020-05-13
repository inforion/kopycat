package ru.inforion.lab403.kopycat.device

enum class TestGPR(val id: Int) {
    r0(0),
    r1(1),
    pc(2);

    companion object {
        val COUNT: Int get() = values().size
        fun from(id: Int): TestGPR = TestGPR.values().first { it.id == id }
    }
}