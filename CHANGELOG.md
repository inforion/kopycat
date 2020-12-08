# Changelog

- Core - changes related to the emulator core/kernel
- x86/ARM/MIPS etc - changes related to this architecture/processor core
- Gradle - changes in Gradle plugins or build scripts

## Version 0.3.30

Release date: 16.12.2020

### Summary

In this release Kopycat got a new fancy module, VEOS that is stand for Virtual Emulator Operating System. With this module Kopycat learned to emulate standalone ELF-binaries from different architectures (now MIPS and ARM were heavily tested). Using VEOS there is no need to somehow configure the Kopycat emulator, just specify target ELF-file and architecture and that's it. Kopycat will parse and load ELF-file into virtual memory and start emulate it.

### Added:

- Added emulator module VEOS (Virtual Emulator Operating System). Using VEOS it's possible to run ELF-files with ARM or MIPS (and in future others) architectures on PC.
- Added ELFLoader Kotlin library for ELF-files parsing and loading into the emulator virtual memory.
- Implemented VEOS API functions stubs for standard libraries like: POSIX, stdlib, strings, stdio, etc. The libraries not fully implemented but over time the level of implementation will grow.

- Added command history for Kopycat REPL
- Added Kotlin-script as one of available programming language for Kopycat REPL (in future releases will be set as default REPL language)
- Added console option for logging level configuration, i.e. `-ll Kopycat=ALL` or `-ll ALL`
- Added possibility not to specify in console module parameters with **default values**
- Added possibility to loading modules not just from a directory (`-y` console option) but also from Java classpath (by default `ru.inforion.lab403.kopycat.modules` used)
- Added possibility to embed Kotlin-script into reset method for JSON modules

- Added modules parameters list and its description for methods getAvailableTopModules/getAvailableAllModules
- Added synchronization for REST startup with Kopycat emulator start. It's critical when emulator embedded in other application.
- Added new serialization subsystem for automatic store/load of the emulator state (currently only in VEOS)
- Added possibility to disconnect bus from port in the emulator runtime
- Added source-jar generation during Kopycat project build
- Added tracer subsystem method onStart()

### Fixed:

- Fixed debuggers modules location that leads to different library when loading as JSON and as Kotlin
- Fixed bugs in serialization/deserialization of various modules
- Fixed bug with ARMv6M Thumb working mode with occasional skipping the second instruction
- Fixed bug with ComponentTracer connection to debugger on startup
- Fixed bug with Kopycat crashes when run ComponentTracer without any child tracer
- Fixed returned list of registers for ARMDebugger
- Fixed various bugs with bus architecture

### Modified:

- ARM and MIPS cores moved to a new CPU registers system that increase 10% performance in average and simplify code developing
- Fully reworked REST interface. All REST methods split over endpoints and now correspond Kopycat Kotlin class.
- Kopycat modules and devices code improved to be compatible with Java object serialization streams for distributed systems computations

### Others:

- A lot of proposal library code moved into kotlin-extensions library
- A lot of various bugs fixed
- Code review and refactoring: changes with new Kotlin features, added comments, doc-strings, translations
- Kopycat code one more time was checked on obscene language

## Version 0.3.20

Release date:  27.05.2020

Kopycat now is a fully open-source project. In this release we have tried to do our best to run different widespread systems on the emulator. Previous release could run STM32F0xx with FreeRTOS operating system. This time we have added various examples of firmwares for unittesting emulator and to work with different peripheral modules. The most exciting addition is that Kopycat runs Linux on ARM MCU's ARM1176JZ. All these examples are presented in sources and as prebuild modules. A lot of bugs have been fixed in the emulator core and modules.

### Added

**Core**: 
- full open-source release
- moved to OpenJDK and version updated to 11.0.x
- register bank system `ARegisterBankNG` to simplify configuring processor core (currently only in ARM)
- implemented fast wait-for-interrupt processing

**Auxiliary**: 
- NANDGen - NAND modules generator based on standard NAND parameters
- NANDPart - NAND dump loader to partition dump by pages and add ECC in spares  

**ARM**: 
- implemented partially MCU ARM1176JZ (enough to run Linux 2.6.x)
- implemented ARMv6 Coprocessor and MMU
- implemented generic timer for ARMv6
- implemented NS16550 UART-controller
- implemented PL190 (VIC) Vector Interrupt Controller
- implemented VirtARM - a virtual ARM-based device
- U-boot and Linux prebuild binaries based on buildroot in example 
- implemented DMA-controller in STM32F042

**Others**:
- different examples to show possible cases how to use Kopycat in projects
- tested development of firmware to STM32 controller using CLion and Kopycat
- Kopycat build plugin to simplify a configuration `build.gradle` of new emulator modules
- dokkaMultilang build plugin to generate documentation on multiple languages, each language tags with "{EN}"/"{RU}" tokens

### Modified

**Core**:
- embedded Python interpreter fixes, add autodetect of Python version and Jep library path

**ARM:**
- fixed a lot of bugs in CPU's instructions
- fixed bugs in STM32F042

## Version 0.3.1
 
Release date:  26.12.2019

### Added

**Core**:
- dynamic loading of Jep library (only for Python2)
- logging calls when access to buses
- slave port now can be connected to several buses

### Modified

**Core**:
- fixed different bugs
- modified CPU's cores tracer
- renamed method `exec` to `cont` (incompatibility with Python interpreter)
- fixed buses connection in x86Core 
- fixed critical bug of on cross page memory access (for Paging access mode)
- fixed bugs in instructions (rol, ror) 
- implemented instructions: hlt, btr, partially support FPU
