# Description

Анализатор стека используется в процессе эмуляции для сохранения порядка вызова функций.
Работа основана на сохранении изменений стековой памяти.

# Usage in Top Module

В коде ниже необходимо заменить `YourCore` на название ядра (например, `x86Core`).

В класс топ-модуля необходимо добавить:
```kotlin
val stackAnalyzer = StackAnalyzer(
    YourCoreStackAnalyzerCore(core.cpu)
) {
//    Ring StackPointer protection. Example for Linux
//    For the details see https://www.kernel.org/doc/Documentation/x86/x86_64/mm.txt
    set(0, 0xFFFF_FFFF_FFFF_FFFFuL..0x1000_0000_0000_0000uL)
    set(3, 0x0FFF_FFFF_FFFF_FFFFuL..0x0000_0000_0000_0000uL)
}

private val stackAnalyzerTracer = stackAnalyzer.tracer<YourCore>(this, "sa-trc")
```

В конструктор класса добавить следующую строку.
Под `yourComponentTracer` подразумевается поле типа `ComponentTracer<YourCore>`
```kotlin
yourComponentTracer.addTracer(stackAnalyzerTracer)
```

Для сохранения состояния анализатора стека необходимо перегрузить функции `serialize` и `deserialize`.
Сохранение состояния, которое необходимо добавить в возвращаемое значение функции `serialize`:
```kotlin
"stackAnalyzer" to this.stackAnalyzer.serialize(ctxt)
```

Восстановление состояния в функции `deserialize`:
```kotlin
snapshot["stackAnalyzer"]?.let { this.stackAnalyzer.deserialize(ctxt, it as Map<String, Any>) }
```

# Usage in Core

Для добавления в новое ядро поддержки StackAnalyzerCore необходимо реализовать интерфейс StackAnalyzerCore.
См., например, `x86StackAnalyzerCore`

# Details

Заметки по внутренней реализации.

`getRingProtection` -- необходим для разделения адресов стека в зависимости от колец защиты процессора.

`isCallPerhaps` -- анализ, является ли предыдущая выполненная инструкция вызовом функции.
Для `x86` команда `call` кладёт адрес возврата на стек.
Для RISC архитектур `call`-подобные инструкции сохраняют адрес возврата в специально отведённый для этого регистр.
