# Changelog

- Core - changes related to the emulator core/kernel
- x86/ARM/MIPS etc - changes related to this architecture/processor core
- Gradle - changes in Gradle plugins or build scripts

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
