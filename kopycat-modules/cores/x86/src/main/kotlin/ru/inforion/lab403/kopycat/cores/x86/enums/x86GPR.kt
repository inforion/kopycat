package ru.inforion.lab403.kopycat.cores.x86.enums



enum class x86GPR(val id: Int, val n8: String, val n16: String, val n32: String) {
    EAX(0, "al", "ax", "eax"),
    ECX(1, "cl", "cx", "ecx"),
    EDX(2, "dl", "dx", "edx"),
    EBX(3, "bl", "bx", "ebx"),
    ESP(4, "ah", "sp", "esp"),
    EBP(5, "ch", "bp", "ebp"),
    ESI(6, "dh", "si", "esi"),
    EDI(7, "bh", "di", "edi"),

    EIP(8, "??", "ip", "eip"),

    NONE(9, "noneB", "noneW", "noneD");

    companion object {
        val COUNT: Int get() = values().size
        fun from(id: Int): x86GPR = x86GPR.values().first { it.id == id }
        fun fromOrNull(name: String): x86GPR? = values().firstOrNull { it.name == name }
    }
}