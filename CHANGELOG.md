# Changelog

- Core - changes relates to Kopycat emulator core/kernel
- x86/ARM/MIPS etc - changes relates to this architecture/processor core
- Gradle - changes in Gradle plugins or build scripts

## Version 0.3.20

Release date:  05.2020

Kopycat now is fully open-source project. In this release we try do our best to run different widespread systems on Kopycat emulator. Previous release can run STM32F0xx with FreeRTOS operating system. This time we add various examples of firmwares for unittesting emulator and to work with different peripheral modules. But the most exciting addition is Kopycat runs Linux on ARM MCU's ARM1176JZ. All these samples presented in sources and as prebuild modules. Also, a lot of bugs has been fixed in emulator core and in modules.

### Added

- Core: full open-source release
- Core: moved to OpenJDK and version updated to 11.0.x
- Core: register bank system `ARegisterBankNG` to simplify configuring processor core (currently only in ARM)
- Core: implemented fast wait-for-interrupt processing
- Auxiliary: NANDGen - NAND modules generator based on standard NAND parameters
- Auxiliary: NANDPart - NAND dump loader to partition dump by pages and add ECC in spares  
- ARM: implemented partially MCU ARM1176JZ (enough to run Linux 2.6.x)
- ARM: implemented ARMv6 Coprocessor and MMU
- ARM: implemented generic timer for ARMv6
- ARM: implemented NS16550 UART-controller
- ARM: implemented PL190 (VIC) Vector Interrupt Controller
- ARM: implemented VirtARM - a virtual ARM-based device
- ARM: U-boot and Linux prebuild binaries based on buildroot in example 
- ARM: implemented DMA-controller in STM32F042
- Others: different examples to show possible cases how to use Kopycat in projects
- Others: tested development of firmware to STM32 controller using CLion and Kopycat
- Gradle: Kopycat build plugin to simplify a configuration `build.gradle` of new emulator modules
- Gradle: dokkaMultilang build plugin to generate documentation on multiple languages, each language tags with "{EN}"/"{RU}" tokens

### Modified

- Core: embedded Python interpreter fixes, add autodetect of Python version and Jep library path
- ARM: fixed a lot of bugs in CPU's instructions
- ARM: fixed bugs in STM32F042

## Version 0.3.1
 
Release date:  26.12.2019

### Added

- Core: dynamic loading of Jep library (only for Python2)
- Core: logging calls when access to buses
- Core: slave port now can be connected to several buses

### Modified

- Core: fixed different bugs
- Core: CPU's cores tracer modified
- Core: method `exec` renamed to `cont` (incompatibility with Python interpreter)
- x86: fixed buses connection in x86Core 
- x86: fixed critical bug of on cross page memory access (for Paging access mode)
- x86: bugs fixed in instructions (rol, ror) 
- x86: implemented instructions: hlt, btr, partially support FPU
