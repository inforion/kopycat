<img src="https://kopy.cat/static/media/big_logo.169d84fb.png" width="384">

Kopycat is a multi-processor architectures system and user-level (with VEOS module) emulator.

## Description

Main features are:

- Easy to assemble a new device. Configure your own platform using JSON or Kotlin.
- Easy to customise. Create your own platform-module using Kotlin.
- Cross-platform. Kopycat uses JVM as a backend and can be run on Windows, Linux and OSX.   
- One-to-one correspondence. Virtual platform representation is identical to block diagram.
- Multiple supported architectures: MIPS, ARM, MSP430, v850ES, x86.
- User-level mode. Can emulate a standalone ELF-file without full system emulation.

This project contains CPU cores: ARMv6, ARMv6M, ARMv7, MIPS, MSP430, v850ES, x86, and MCUs: CortexM0, STM32F0xx, MSP430x44x. 

You can download prebuild JAR-files modules via the link: https://kopy.cat/download

## Videos

- [How to install and run Linux Buildroot guest on Debian 9 host](https://youtu.be/lM2AWJG_ck4)
- [How to use snapshot for quick start of guest system](https://youtu.be/Q4rXs9GF8BQ)
- [Developing firmware for STM32 in CLion using Kopycat](https://youtu.be/GN-uI5s1_iU)
- [Run from IntelliJ Linux Buildroot guest and make rm -rf /](https://youtu.be/KYMhrf2QzEg)

## Requirements to run prebuild Kopycat

To run Kopycat you have to install the following software:

1. OpenJDK (version 11.0.6/7 tested)
2. If Python REPL console is preferred instead of Kotlin console then Python required (version 2.7 and 3.6, 3.8 tested) with Jep package (version 0.3.9 tested) for embedded console in Kopycat.
    
NOTE: prebuild OpenJDK installer is available on https://adoptopenjdk.net/ 
    
Linux and OSX users can use package manager to install OpenJDK and Python. To install **jep** `pip` command can be used, but before **compiler** and **toolchain** have to be installed for building **jep** package. On Linux system **gcc** from `apt` and on OSX `Developer Tools` with XCode (XCode by itself is not needed, but we need a compiler). As for Windows users you may face a lot of difficulties while compiling Python packages.
    
### Installation of requirements on Windows 10

1. For Windows, you should manually download OpenJDK package and setup `PATH` and `JAVA_HOME` environment variables, see https://openjdk.java.net/install/

1. Download and install Python (don't forget to add Python to `PATH` during installation and select to install **pip**) from the official site: https://www.python.org/downloads/

1. Download and install Visual Studio build tools: https://visualstudio.microsoft.com/visual-cpp-build-tools/ (**DON'T FORGET TO SELECT VERSION 14.x**)

1. Fix ¯\\\_(ツ)\_/¯ Python setuptools to work with Visual Studio compiler: https://stackoverflow.com/a/20050195/1312718

1. Run Console from **x64 Native Tools Command Prompt** (installed in the main menu of Windows) and execute:

    ```shell script
    pip install jep
    ``` 

### Installation of requirements on Linux (i.e. Debian 9)

```shell script
# only for debian 9
echo 'deb http://ftp.debian.org/debian stretch-backports main' | sudo tee /etc/apt/sources.list.d/stretch-backports.list
sudo apt update

sudo apt install gcc openjdk-11-jdk curl python socat

# path may differ. JAVA_HOME required to be set for jep installation
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64

curl https://bootstrap.pypa.io/get-pip.py -o get-pip.py
sudo python get-pip.py
pip install jep
```

NOTE: OpenJDK installation reference for Ubuntu https://dzone.com/articles/installing-openjdk-11-on-ubuntu-1804-for-real

### Installation of requirements on OSX

```shell script
brew install socat
brew cask install adoptopenjdk
brew install adoptopenjdk/openjdk/adoptopenjdk-openjdk11
brew install python@3.7
pip install jep
```

NOTE: OpenJDK installation reference https://dzone.com/articles/install-openjdk-versions-on-the-mac

## Requirements for developing modules and software with Kopycat

For module development and working with sources the following software is required:

1. IntelliJ (version >= 2020.2)
2. Kotlin plugin (version >= 1.4.21)

## Getting started

In this part of readme you will see how to start Kopycat: 
- for a device with STM32F042 (core: Cortex-M0; architecture: ARMv6M)
- virtual ARM device (core: ARM1176JZS; architecture: ARMv6/v7) 
- `ls` and `cat` command for ARM architecture in user-level mode with VEOS

The peripheral modules implemented for STM32F042 are UART, TIMx, DMAC, GPIOx, WDG. These peripheral modules are enough to run FreeRTOS. How to work with FreeRTOS is shown in the `freertos_uart` firmware example. 

Virtual ARM (VirtARM) runs UBoot, Linux with kernel 2.6.x, and ext2 filesystem.

ARM architecture is used in all the examples as the most popular in embedded devices nowadays. 

**NOTE**: The concept of architecture and core may differ from manufacturer to manufacturer. For example, ARM has a complicated system of architecture and core. The low-level layer is architecture, i.e. ARMv6, ARMv7 (CPUs), ARMv6M (embedded MCUs), etc. Mentioned architecture are used in cores, i.e. ARM1176JZS (not ARMv11!), CortexM0, CortexM3, etc. And then MCUs are implemented, i.e. STM32F04. For TI MSP430 core, architecture and MCU itself are almost the same. So we will often use "core" to refer to core or architecture. Moreover, there is no architecture in sources of an entity. Kopycat lowest-level is `Core` that consists of `CPU`, actual CPU, which executes instructions, `Decoder`, `COP`, coprocessor, which process interrupts, `MMU`, memory management unit.

### Run prebuild Kopycat core and module STM32F042 on Cortex-M0 core 

1. Download prebuild emulator core `kopycat-X.Y.AB` from [latest release](https://github.com/inforion/kopycat/releases/latest) and unzip the archive into any directory (**it is strongly recommended not to use directories with spaces or special symbols!**)
1. Add environment variable `KOPYCAT_HOME` (recommended, used by Kopycat core to lookup default modules library) to this directory, e.g. `KOPYCAT_HOME=/opt/kopycat-X.Y.Z-RCx` and add to environment variable `PATH` path to `KOPYCAT_HOME/bin`   
1. Download prebuild modules libraries for Kopycat `modules-X.Y.AB` from [latest release](https://github.com/inforion/kopycat/releases/latest) and:
    - unzip this archive into any directory (**it is also strongly recommended not to use directories with spaces or special symbols!**) and add environment variable `KOPYCAT_MODULES_LIB_PATH` (only to simplify readme commands) to the directory
    
    OR
    
    - unzip this archive into `${KOPYCAT_HOME}/modules` directory (if you setup `KOPYCAT_HOME`)
1. Start emulation of one of the modules from the library, e.g. STM32F042

    ```shell script
    kopycat -y ${KOPYCAT_MODULES_LIB_PATH} -l mcu -n stm32f042_example -g 23946 -p "firmware=example:usart_poll,tty1=socat:,tty2=socat:"
    ```
    
    OR if you set up `KOPYCAT_HOME` environment variable and copy modules into `${KOPYCAT_HOME}/modules` just:
    
    ```shell script
    kopycat -l mcu -n stm32f042_example -g 23946 -p "firmware=example:usart_poll,tty1=socat:,tty2=socat:"
    ```
   
    - `-y` - path to a bunch of prebuild module's libraries (aka **registry**)
    - `-l` - actual library for the module (name of directory in the registry)
    - `-n` - name of module, in our case is `stm32f042_example` - name of Kotlin class inside jar
    - `-p` - parameters for `stm32f042_example` module (class arguments). To get more information about arguments of module you should see sources of module (for this module available in repository on GitHub). 
        - `firmware` - is firmware to load into FLASH of STM32F042 (in this case simple tty echo)
            - **example** - means usage of one of binaries from `stm32f0xx.jar`: `benchmark_qsort`, `freertos_uart`, `gpiox_led`, `gpiox_registers`, `rhino_fw42k6`, `usart_dma`, `usart_poll`. Sources for each firmware published in GitHub repository in the `kopycat-modules/mcu/stm32f0xx/firmwares`.
            - **file** - path to binary file i.e. *.bin from CLion.
            - **bytes** - hex string i.e. `AACCDDEE90909090`.
        - `ttyX` - virtual terminal connected to usart1 and usart2 of STM32F042.
    - `-g` - GDB server port

    **NOTES:**
    1. If you've added `KOPYCAT_HOME` environment variable you can put prebuild modules libraries into `${KOPYCAT_HOME}/modules` without necessity to specify `-y` parameter explicitly.
    1. Due to socat this line will work only in **nix** system with installed socat. To disable it use `tty1=null,tty2=null`. For windows system com0com can be used or any other software to create virtual COM ports. In this case you should specify directly virtual com-port name `tty1=COM1,tty2=COM2`.
    
1. You should see the following start log:
    
    - for python console:
    
    ```log
    bat@Kernel ~ % kopycat -l mcu -n stm32f042_example -g 23946 -p "firmware=example:usart_poll,tty1=socat:,tty2=socat:"
    12:53:54 INF ...pycat.KopycatStarter.main(KopycatStarter.kt:54): Build version: kopycat-0.3.30-e87ed235-2021.112-Regular [JRE v11.0.6]
    12:53:54 INF ...atStarter.getRegistryPath(KopycatStarter.kt:34): Kopycat directory: '...'
    12:53:55 INF ...kopycat.Kopycat.setSnapshotsDir(Kopycat.kt:103): Change snapshots directory to '/Users/bat'
    12:53:55 INF ...pycat.KopycatStarter.main(KopycatStarter.kt:78): GDB_SERVER(port=23946,alive=true) was created
    12:53:55 INF ...ibrary.instantiate(ModuleFactoryLibrary.kt:105): stm32f042_example(null, top, firmware=example:usart_poll, tty1=socat:, tty2=socat:)
    12:53:55 INF ...iliary.ANetworkThread.run(ANetworkThread.kt:55): GDB_SERVER waited for clients on [172.20.10.6:23946]
    12:53:56 WRN ...cat$Companion.createPseudoTerminal(Socat.kt:75): Pseudo-terminals created for top.term1: /dev/ttys014 and /dev/ttys015
    12:53:56 WRN ...cat$Companion.createPseudoTerminal(Socat.kt:75): Pseudo-terminals created for top.term2: /dev/ttys016 and /dev/ttys017
    12:53:56 CFG ....initializeAndResetAsTopInstance(Module.kt:189): Setup core to top.stm32f042.cortexm0.arm for top
    12:53:56 CFG ....initializeAndResetAsTopInstance(Module.kt:194): Setup debugger to top.stm32f042.dbg for top
    12:53:56 WRN ....initializeAndResetAsTopInstance(Module.kt:210): Tracer wasn't found in top...
    12:53:56 CFG ...s.UartTerminal$tx$2.invoke(UartTerminal.kt:176): Create transmitter UART terminal thread: 'top.term2'
    12:53:56 CFG ...s.UartTerminal$tx$2.invoke(UartTerminal.kt:176): Create transmitter UART terminal thread: 'top.term1'
    12:53:56 CFG ....initializeAndResetAsTopInstance(Module.kt:218): Initializing ports and buses...
    12:53:56 WRN ....initializeAndResetAsTopInstance(Module.kt:220): ATTENTION: Some ports has warning use printModulesPortsWarnings to see it...
    12:53:56 INF ...modules.cortexm0.CORTEXM0.reset(CORTEXM0.kt:58): Setup CORTEX-M0 core PC=0x080022C1 MSP=0x20001800
    12:53:56 CFG ....initializeAndResetAsTopInstance(Module.kt:232): Module top is successfully initialized and reset as a top cell!
    12:53:56 INF ...ion.lab403.kopycat.Kopycat.open(Kopycat.kt:151): Board top[stm32f042_example] with arm[ARMv6MCore] is ready
    12:53:56 INF ...bstub.GDBServer.debuggerModule(GDBServer.kt:78): Set new debugger module top.stm32f042.dbg for GDB_SERVER(port=23946,alive=true)
    12:53:56 WRN ...at.KopycatStarter.console(KopycatStarter.kt:44): Use -kts option to enable Kotlin console. In the next version Kotlin console will be default.
    12:53:56 CFG ...at.consoles.jep.JepLoader.load(JepLoader.kt:53): Loading Jep using Python command 'python3' to overwrite use '--python' option
    12:53:56 CFG ...oles.jep.PythonShell.version(PythonShell.kt:34): Python Version(major=3, minor=9, micro=0)
    12:53:56 CFG ...s.jep.JepLoader.findFileInPath(JepLoader.kt:25): Jep jar file: /usr/local/lib/python3.9/site-packages/jep/jep-3.9.1.jar
    12:53:56 CFG ...s.jep.JepLoader.findFileInPath(JepLoader.kt:25): Jep shared library file: /usr/local/lib/python3.9/site-packages/jep/jep.cpython-39-darwin.so
    Jep starting successfully!
    12:53:56 INF ...ycat.KopycatStarter.main(KopycatStarter.kt:112): Python console enabled
    Python > 
    ```

    - for kotlin console:
    
    ```
    bat@Kernel ~ % kopycat -l mcu -n stm32f042_example -g 23946 -p "firmware=example:usart_poll,tty1=socat:,tty2=socat:" -kts           
    12:55:07 INF ...pycat.KopycatStarter.main(KopycatStarter.kt:54): Build version: kopycat-0.3.30-e87ed235-2021.112-Regular [JRE v11.0.6]
    12:55:08 INF ...atStarter.getRegistryPath(KopycatStarter.kt:34): Kopycat directory: '<KOPYCAT_HOME>'
    12:55:09 INF ...kopycat.Kopycat.setSnapshotsDir(Kopycat.kt:103): Change snapshots directory to '/Users/bat'
    12:55:09 INF ...pycat.KopycatStarter.main(KopycatStarter.kt:78): GDB_SERVER(port=23946,alive=true) was created
    12:55:09 INF ...ibrary.instantiate(ModuleFactoryLibrary.kt:105): stm32f042_example(null, top, firmware=example:usart_poll, tty1=socat:, tty2=socat:)
    12:55:09 INF ...iliary.ANetworkThread.run(ANetworkThread.kt:55): GDB_SERVER waited for clients on [172.20.10.6:23946]
    12:55:09 WRN ...cat$Companion.createPseudoTerminal(Socat.kt:75): Pseudo-terminals created for top.term1: /dev/ttys018 and /dev/ttys019
    12:55:10 WRN ...cat$Companion.createPseudoTerminal(Socat.kt:75): Pseudo-terminals created for top.term2: /dev/ttys020 and /dev/ttys021
    12:55:10 CFG ....initializeAndResetAsTopInstance(Module.kt:189): Setup core to top.stm32f042.cortexm0.arm for top
    12:55:10 CFG ....initializeAndResetAsTopInstance(Module.kt:194): Setup debugger to top.stm32f042.dbg for top
    12:55:10 WRN ....initializeAndResetAsTopInstance(Module.kt:210): Tracer wasn't found in top...
    12:55:10 CFG ...s.UartTerminal$tx$2.invoke(UartTerminal.kt:176): Create transmitter UART terminal thread: 'top.term2'
    12:55:10 CFG ...s.UartTerminal$tx$2.invoke(UartTerminal.kt:176): Create transmitter UART terminal thread: 'top.term1'
    12:55:10 CFG ....initializeAndResetAsTopInstance(Module.kt:218): Initializing ports and buses...
    12:55:10 WRN ....initializeAndResetAsTopInstance(Module.kt:220): ATTENTION: Some ports has warning use printModulesPortsWarnings to see it...
    12:55:10 INF ...modules.cortexm0.CORTEXM0.reset(CORTEXM0.kt:58): Setup CORTEX-M0 core PC=0x080022C1 MSP=0x20001800
    12:55:10 CFG ....initializeAndResetAsTopInstance(Module.kt:232): Module top is successfully initialized and reset as a top cell!
    12:55:10 INF ...ion.lab403.kopycat.Kopycat.open(Kopycat.kt:151): Board top[stm32f042_example] with arm[ARMv6MCore] is ready
    12:55:10 INF ...bstub.GDBServer.debuggerModule(GDBServer.kt:78): Set new debugger module top.stm32f042.dbg for GDB_SERVER(port=23946,alive=true)
    warning: runtime JAR files in the classpath should have the same version. These files were found in the classpath:
        <KOPYCAT_HOME>/lib/kotlin-reflect-1.4.10.jar (version 1.4)
        <KOPYCAT_HOME>/lib/kotlin-stdlib-1.4.10.jar (version 1.4)
        <KOPYCAT_HOME>/lib/kotlin-stdlib-jdk8-1.3.71.jar (version 1.3)
        <KOPYCAT_HOME>/lib/kotlin-stdlib-jdk7-1.3.71.jar (version 1.3)
        <KOPYCAT_HOME>/lib/kotlin-script-runtime-1.4.10.jar (version 1.4)
        <KOPYCAT_HOME>/lib/kotlin-stdlib-common-1.4.10.jar (version 1.4)
    warning: some runtime JAR files in the classpath have an incompatible version. Consider removing them from the classpath
    12:55:13 INF ...ycat.KopycatStarter.main(KopycatStarter.kt:112): Kotlin console enabled
    Kotlin > 
    ```
   
    NOTES:
   
    1. `Pseudo-terminals created for top.term1: /dev/ttys002 and /dev/ttys004` 
        - /dev/ttys002 is endpoint to emulator connection to virtual COM port of USART1
        - /dev/ttys004 is endpoint to user connection to virtual COM port of USART1
        
    2. `warning: runtime JAR files in the classpath should have the same version` caused by Javalin library because it uses Kotlin 1.3.x that clashes with current Kopycat Kotlin version 1.4.10 but it not interferes.
    
    3. The next examples will be shown for Python console but almost all examples for Kotlin will be the same. 
    
    4. Currently, Kotlin console supports autocomplete feature, for Python it will be added in next releases.
    
1. Attach to `/dev/ttys004` (name may differ) COM port using, for example, **putty** or **screen**

    ```shell script
    screen /dev/ttys004
    ```

1. In Kopycat console print and press enter:

    ```python
    kc.start()  # run Kopycat emulation
    ```
   
1. Now you can print something in `/dev/ttys004` and will see echo. This echo is sent back by the internal firmware of STM32F042. If emulation isn't running, there is no echo in the console.

1. To stop emulation in Kopycat console print and press enter:

    ```python
    kc.halt()
    ```
   
`kc` - is a special proxy object to make visible all methods of Kopycat class from Java in Python interpreter using **jep** library. You can see all available methods of **kc** object in sources of `Kopycat` class.   

### Run prebuild Kopycat core and module of virtual ARM on ARM1176JZS core    

1. Start emulation of another module: **Linux 2.6.xx on ARM core**
   
    ```shell script
    kopycat -y ${KOPYCAT_MODULES_LIB_PATH} -l mcu -n VirtARM -g 23946 -p "tty=socat:"
   
    13:29:02 INF ...pycat.KopycatStarter.main(KopycatStarter.kt:54): Build version: kopycat-0.3.30-e87ed235-2021.112-Regular [JRE v11.0.6]
    13:29:02 INF ...atStarter.getRegistryPath(KopycatStarter.kt:34): Kopycat directory: '<KOPYCAT_HOME>'
    13:29:03 INF ...kopycat.Kopycat.setSnapshotsDir(Kopycat.kt:103): Change snapshots directory to '/Users/bat'
    13:29:03 INF ...pycat.KopycatStarter.main(KopycatStarter.kt:78): GDB_SERVER(port=23946,alive=true) was created
    13:29:03 INF ...ibrary.instantiate(ModuleFactoryLibrary.kt:105): VirtARM(null, top, tty=socat:)
    13:29:03 INF ...iliary.ANetworkThread.run(ANetworkThread.kt:55): GDB_SERVER waited for clients on [172.20.10.6:23946]
    13:29:04 WRN ...cat$Companion.createPseudoTerminal(Socat.kt:75): Pseudo-terminals created for top.term: /dev/ttys018 and /dev/ttys019
    13:29:04 CFG ....modules.virtarm.VirtARM.<init>(VirtARM.kt:115): Setting bootloaderCmd: 'setenv machid 25f8
    setenv bootargs console=ttyS0,115200n8 ignore_loglevel root=/dev/mtdblock0 init=/linuxrc lpj=622592
    setenv verify n
    bootm 1000000
    '
    13:29:04 CFG ....modules.virtarm.VirtARM.<init>(VirtARM.kt:120): Loading GCC map-file...
    13:29:04 CFG ....initializeAndResetAsTopInstance(Module.kt:189): Setup core to top.arm1176jzs for top
    13:29:04 CFG ....initializeAndResetAsTopInstance(Module.kt:194): Setup debugger to top.dbg for top
    13:29:04 WRN ....initializeAndResetAsTopInstance(Module.kt:210): Tracer wasn't found in top...
    13:29:04 CFG ...s.UartTerminal$tx$2.invoke(UartTerminal.kt:176): Create transmitter UART terminal thread: 'top.term'
    13:29:04 CFG ....initializeAndResetAsTopInstance(Module.kt:218): Initializing ports and buses...
    13:29:04 WRN ....initializeAndResetAsTopInstance(Module.kt:220): ATTENTION: Some ports has warning use printModulesPortsWarnings to see it...
    13:29:04 FNE ...ware.processors.ARMv6CPU.reset(ARMv6CPU.kt:151): pc=0x00000000 sp=0x00000000
    13:29:05 CFG ....initializeAndResetAsTopInstance(Module.kt:232): Module top is successfully initialized and reset as a top cell!
    13:29:05 INF ...ion.lab403.kopycat.Kopycat.open(Kopycat.kt:151): Board top[VirtARM] with arm1176jzs[ARM1176JZS] is ready
    13:29:05 INF ...bstub.GDBServer.debuggerModule(GDBServer.kt:78): Set new debugger module top.dbg for GDB_SERVER(port=23946,alive=true)
    13:29:05 WRN ...at.KopycatStarter.console(KopycatStarter.kt:44): Use -kts option to enable Kotlin console. In the next version Kotlin console will be default.
    13:29:05 CFG ...at.consoles.jep.JepLoader.load(JepLoader.kt:53): Loading Jep using Python command 'python3' to overwrite use '--python' option
    13:29:05 CFG ...oles.jep.PythonShell.version(PythonShell.kt:34): Python Version(major=3, minor=9, micro=0)
    13:29:05 CFG ...s.jep.JepLoader.findFileInPath(JepLoader.kt:25): Jep jar file: /usr/local/lib/python3.9/site-packages/jep/jep-3.9.1.jar
    13:29:05 CFG ...s.jep.JepLoader.findFileInPath(JepLoader.kt:25): Jep shared library file: /usr/local/lib/python3.9/site-packages/jep/jep.cpython-39-darwin.so
    Jep starting successfully!
    13:29:05 INF ...ycat.KopycatStarter.main(KopycatStarter.kt:112): Python console enabled
    ```
   
1. Attach to `/dev/ttys013` (name may differ) COM port using for example **putty** or **screen**

    ```shell script
    screen /dev/ttys013
    ```
   
1. Run Kopycat emulation using `kc.start()` command and see U-boot and Linux loading. After Linux load you can log in into it using root/toor pair in `/dev/ttys013` terminal.
   
### Get available prebuild Kopycat modules from registry 
   
1. To get info of all available modules in libraries, run Kopycat using the following command: 
   
    ```shell script
    kopycat -y ${KOPYCAT_MODULES_LIB_PATH} -all
   
    13:29:57 INF ...pycat.KopycatStarter.main(KopycatStarter.kt:54): Build version: kopycat-0.3.30-e87ed235-2021.112-Regular [JRE v11.0.6]
    13:29:57 INF ...atStarter.getRegistryPath(KopycatStarter.kt:34): Kopycat directory: '<KOPYCAT_HOME>'
    13:29:58 INF ...kopycat.Kopycat.setSnapshotsDir(Kopycat.kt:103): Change snapshots directory to '/Users/bat'
    13:29:58 INF ...opycat.printModulesRegistryInfo(Kopycat.kt:722): 
    Library 'PeripheralFactoryLibrary[veos]':
        Module: [       VirtualMemory] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/veos/veos.jar]
        Module: [     MIPSApplication] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/veos/veos.jar]
        Module: [x86WindowsApplication] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/veos/veos.jar]
        Module: [      ARMApplication] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/veos/veos.jar]
    
    Library 'PeripheralFactoryLibrary[mcu]':
        Module: [                 SCB] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/cortexm0.jar]
        Module: [                 STK] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/cortexm0.jar]
        Module: [            CORTEXM0] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/cortexm0.jar]
        Module: [                NVIC] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/cortexm0.jar]
        Module: [   stm32f042_example] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
        Module: [     stm32f042_rhino] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
        Module: [                EXTI] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
        Module: [           STM32F042] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
        Module: [                 LED] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
        Module: [                IWDG] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
        Module: [               GPIOx] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
        Module: [               FLASH] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
        Module: [                  BT] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
        Module: [                DMAC] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
        Module: [                 RCC] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
        Module: [                TIMx] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
        Module: [              SYSCFG] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
        Module: [                 TSC] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
        Module: [                 FMI] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
        Module: [               TIM18] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
        Module: [              USARTx] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
        Module: [               rhino] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
        Module: [             VirtARM] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/virtarm.jar]
        Module: [               Timer] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/virtarm.jar]
        Module: [            NANDCtrl] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/virtarm.jar]
        Module: [             NS16550] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/virtarm.jar]
        Module: [               PL190] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/virtarm.jar]
    
    Library 'PeripheralFactoryLibrary[cores]':
        Module: [        MipsDebugger] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/cores/mips.jar]
        Module: [            MipsCore] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/cores/mips.jar]
        Module: [          ARM1176JZS] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/cores/arm.jar]
        Module: [          ARMv6MCore] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/cores/arm.jar]
        Module: [           ARMv7Core] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/cores/arm.jar]
        Module: [         ARMDebugger] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/cores/arm.jar]
        Module: [         x86Debugger] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/cores/x86.jar]
        Module: [             x86Core] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/cores/x86.jar]
    
    Library 'PeripheralFactoryLibrary[common]':
        Module: [             ATACTRL] -> ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.30.jar/ru/inforion/lab403/kopycat/modules/common/ATACTRL.class]
        Module: [              M95160] -> ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.30.jar/ru/inforion/lab403/kopycat/modules/common/M95160.class]
        Module: [                NAND] -> ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.30.jar/ru/inforion/lab403/kopycat/modules/common/NAND.class]
        Module: [                 Hub] -> ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.30.jar/ru/inforion/lab403/kopycat/modules/common/Hub.class]
        Module: [              i82551] -> ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.30.jar/ru/inforion/lab403/kopycat/modules/common/i82551.class]
        Module: [        CompactFlash] -> ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.30.jar/ru/inforion/lab403/kopycat/modules/common/CompactFlash.class]
        Module: [            Am79C972] -> ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.30.jar/ru/inforion/lab403/kopycat/modules/common/Am79C972.class]
        Module: [             Signals] -> ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.30.jar/ru/inforion/lab403/kopycat/modules/common/Signals.class]
        Module: [                  SD] -> ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.30.jar/ru/inforion/lab403/kopycat/modules/common/SD.class]
        Module: [              EEPROM] -> ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.30.jar/ru/inforion/lab403/kopycat/modules/common/EEPROM.class]
        Module: [             PCIHost] -> ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.30.jar/ru/inforion/lab403/kopycat/modules/common/pci/PCIHost.class]
    
    Library 'PeripheralFactoryLibrary[memory]':
        Module: [                 ROM] -> ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.30.jar/ru/inforion/lab403/kopycat/modules/memory/ROM.class]
        Module: [                VOID] -> ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.30.jar/ru/inforion/lab403/kopycat/modules/memory/VOID.class]
        Module: [           SparseRAM] -> ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.30.jar/ru/inforion/lab403/kopycat/modules/memory/SparseRAM.class]
        Module: [                 RAM] -> ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.30.jar/ru/inforion/lab403/kopycat/modules/memory/RAM.class]
    
    Library 'PeripheralFactoryLibrary[terminals]':
        Module: [  UartStreamTerminal] -> ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.30.jar/ru/inforion/lab403/kopycat/modules/terminals/UartStreamTerminal.class]
        Module: [        UartTerminal] -> ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.30.jar/ru/inforion/lab403/kopycat/modules/terminals/UartTerminal.class]
        Module: [  UartSerialTerminal] -> ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.30.jar/ru/inforion/lab403/kopycat/modules/terminals/UartSerialTerminal.class]
    ```
   
1. To get info of only modules in libraries, that can be used as a top module run Kopycat using the following command: `kopycat -top`

    ```shell script
    13:41:17 INF ...pycat.KopycatStarter.main(KopycatStarter.kt:54): Build version: kopycat-0.3.30-e87ed235-2021.112-Regular [JRE v11.0.6]
    13:41:18 INF ...atStarter.getRegistryPath(KopycatStarter.kt:34): Kopycat directory: '<KOPYCAT_HOME>'
    13:41:19 INF ...kopycat.Kopycat.setSnapshotsDir(Kopycat.kt:103): Change snapshots directory to '/Users/bat'
    13:41:19 INF ...opycat.printModulesRegistryInfo(Kopycat.kt:722): 
    Library 'PeripheralFactoryLibrary[veos]':
        Module: [     MIPSApplication] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/veos/veos.jar]
        Module: [x86WindowsApplication] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/veos/veos.jar]
        Module: [      ARMApplication] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/veos/veos.jar]
    
    Library 'PeripheralFactoryLibrary[mcu]':
        Module: [   stm32f042_example] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
        Module: [     stm32f042_rhino] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
        Module: [               rhino] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/stm32f0xx.jar]
        Module: [             VirtARM] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/virtarm.jar]
        Module: [           Testbench] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/testbench.jar]
        Module: [          MSP430x44x] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/msp430x44x.jar]
        Module: [               P2020] -> JarModuleFactoryBuilder[<KOPYCAT_HOME>/modules/mcu/p2020.jar]
    ```
   
### Get help 
   
To get full help, run Kopycat using the following command `kopycat --help`:

```
13:43:06 INF ...pycat.KopycatStarter.main(KopycatStarter.kt:54): Build version: kopycat-0.3.30-e87ed235-2021.112-Regular [JRE v11.0.6]
usage: kopycat [-h] [-u MODULES] [-y REGISTRY] [-n NAME] [-l LIBRARY] [-s SNAPSHOT] [-p PARAMETERS] [-w SNAPSHOTS_DIR] [-g GDB_PORT] [-r REST] [-gb] [-run] [-standalone] [-all] [-top]
               [-ci] [-pw] [-python PYTHON] [-kts] [-ll LOG_LEVEL]

virtualization platform

named arguments:
  -h, --help             show this help message and exit
  -u MODULES, --modules MODULES
                         Modules libraries paths in format: lib1:path/to/lib1,lib2:path/to/lib2
  -y REGISTRY, --registry REGISTRY
                         Path to registry with libraries
  -n NAME, --name NAME   Top instance module name (with package path dot-separated)
  -l LIBRARY, --library LIBRARY
                         Top instance module library name
  -s SNAPSHOT, --snapshot SNAPSHOT
                         Snapshot file (top instance module/library can be obtained from here)
  -p PARAMETERS, --parameters PARAMETERS
                         Parameters for top module constructor in format: arg1=100,arg2=/dev/io
  -w SNAPSHOTS_DIR, --snapshots-dir SNAPSHOTS_DIR
                         Snapshots directory path (default path to store and load snapshots)
  -g GDB_PORT, --gdb-port GDB_PORT
                         GDB server port (if not specified then not started)
  -r REST, --rest REST   REST server port. If null - Commander will work
  -gb, --gdb-bin-proto   GDB server enabled binary protocol (default: false)
  -run, --run            Run emulation as soon as Kopycat ready (default: false)
  -standalone, --standalone
                         Run emulation as soon as Kopycat ready and exit when guest application stops (default: false)
  -all, --modules-registry-all-info
                         Print all loaded modules info and exit (default: false)
  -top, --modules-registry-top-info
                         Print top loaded modules info and exit (default: false)
  -ci, --connections-info
                         Print hier. top module buses connections info at startup (default: false)
  -pw, --ports-warnings  Print all ports warnings when loading Kopycat module at startup (default: false)
  -python PYTHON, --python PYTHON
                         Python interpreter command
  -kts, --kotlin-script  Set REPL to Kotlin script language (default: false)
  -ll LOG_LEVEL, --log-level LOG_LEVEL
                         Set messages minimum logging level for specified loggers in format logger0=LEVEL,logger1=LEVEL
                         Or for all loggers if no '=' was found in value just logger level, i.e. FINE
                         Available levels: ALL, SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST, DEBUG, TRACE, OFF
``` 

### Run Kopycat in user-level emulation mode with VEOS module

1. `KOPYCAT_HOME` environment variable is required and `KOPYCAT_HOME/bin` is required to be added into `PATH` environment variable

1. VEOS module should be placed in modules directory into `KOPYCAT_HOME/modules`

1. Download other architecture ELF binary files to run using Kopycat, i.e. from Debian Stretch distributive: 
    - binutils: https://packages.debian.org/stretch/armel/binutils/download
    - coreutils: https://packages.debian.org/stretch/armel/coreutils/download
    
1. Unpack it to some folders and change directory to where you unpack

1. Run the required binary using command for example:
        
    ```
    kopycat-veos-arm bin/ls -la /usr/share
    ```
    
    The output of this command will be something like that:
    
    ```
    ... tons of log ...
    13:07:24 CFG ...at.veos.api.impl.StatAPI.stat64(StatAPI.kt:140): [0x00009ACC] stat64(path='/usr/share/doc' buf=0x100004E8) -> stat(st_dev=0, st_ino=0, st_mode=16877, st_nlink=1, st_uid=0, st_gid=0, st_rdev=0, st_size=96, st_blksize=0, st_blocks=0, st_atime=1607940014, st_mtime=1487766225, st_ctime=1487766225) in Process:1(state=Running)
    total 0
    drwxr-xr-x  1 root root   96 Feb 22  2017 doc
    drwxr-xr-x  1 root root   96 Feb 22  2017 info
    drwxr-xr-x 43 root root 1440 Feb 22  2017 locale
    drwxr-xr-x  2 root root  128 Feb 22  2017 man
    13:07:24 FST ...ab403.kopycat.veos.VEOS.preExecute(VEOS.kt:402): Application exited
    ```
    
    Command `kopycat-veos-arm` is a script located in `<KOPYCAT_HOME>/bin` directory. This script run Kopycat in a standalone mode. Standalone mode is not waiting until a debugger connected but rather run emulated processor as soon as it is ready. To configure Kopycat with VEOS module the next environment options may be used: 
    
    - `KOPYCAT_VEOS_GDB_PORT` - starts GDB server for specified port (default: not started).
    - `KOPYCAT_VEOS_CONSOLE` - what type of console to be used, may be: `kotlin` or `python=<PYTHON_COMMAND>` (default: `python=python`). 
    - `KOPYCAT_VEOS_WORKING_DIR` - working root directory for emulation i.e. directory in host system that will be `/` for emulating (default: is current a directory).
    - `KOPYCAT_VEOS_STANDALONE` - if `NO` don't run emulated processor in Kopycat as soon as emulator ready (default: standalone mode)
    - `KOPYCAT_VEOS_LD_PRELOAD` - comma-separated list of preload dynamic libraries if these libraries required for ELF-file and not resolved automatically (default: empty)
    - `KOPYCAT_VEOS_LOGGING_CONF` - logging level for whole Kopycat or if specified module for the module, i.e. `KOPYCAT_VEOS_LOGGING_CONF=OFF` disable all logging messages, or `KOPYCAT_VEOS_LOGGING_CONF=TimeAPI=OFF,StdlibAPI=FINE` - disable logging for TimeAPI and set minimum logging level for StdlibAPI to FINE. 
    
    For example with disabled logging:
    
    ```
    export KOPYCAT_VEOS_LOGGING_CONF=OFF
    kopycat-veos-arm usr/bin/readelf -S /usr/bin/readelf
    ```
    
    Output is:
    
    ```
    13:52:05 INF ...pycat.KopycatStarter.main(KopycatStarter.kt:54): Build version: kopycat-0.3.30-e87ed235-2021.115-Regular [JRE v11.0.6]
    13:52:05 INF ...atStarter.getRegistryPath(KopycatStarter.kt:34): Kopycat directory: '<KOPYCAT_HOME>'
    There are 28 section headers, starting at offset 0x862fc:
    
    Section Headers:
      [Nr] Name              Type            Addr     Off    Size   ES Flg Lk Inf Al
      [ 0]                   NULL            00000000 000000 000000 00      0   0  0
      [ 1] .interp           PROGBITS        00000000 000154 000013 00   A  0   0  1
      [ 2] .note.ABI-tag     NOTE            00000000 000168 000020 00   A  0   0  4
      [ 3] .note.gnu.build-i NOTE            00000000 000188 000024 00   A  0   0  4
      [ 4] .gnu.hash         GNU_HASH        00000000 0001ac 00015c 04   A  5   0  4
      [ 5] .dynsym           DYNSYM          00000000 000308 000750 10   A  6   3  4
      [ 6] .dynstr           STRTAB          00000000 000a58 0004e4 00   A  0   0  1
      [ 7] .gnu.version      VERSYM          00000000 000f3c 0000ea 02   A  5   0  2
      [ 8] .gnu.version_r    VERNEED         00000000 001028 000020 00   A  6   1  4
      [ 9] .rel.dyn          REL             00000000 001048 001c28 08   A  5   0  4
      [10] .rel.plt          REL             00000000 002c70 000210 08  AI  5  23  4
      [11] .init             PROGBITS        00000000 002e80 000010 00  AX  0   0  4
      [12] .plt              PROGBITS        00000000 002e90 00032c 04  AX  0   0  4
      [13] .text             PROGBITS        00000000 0031c0 05d65c 00  AX  0   0  8
      [14] .fini             PROGBITS        00000000 06081c 00000c 00  AX  0   0  4
      [15] .rodata           PROGBITS        00000000 060828 023418 00   A  0   0  4
      [16] .ARM.exidx        ARM_EXIDX       00000000 083c40 000008 00  AL 13   0  4
      [17] .eh_frame         PROGBITS        00000000 083c48 000004 00   A  0   0  4
      [18] .init_array       INIT_ARRAY      00000000 0842f4 000004 04  WA  0   0  4
      [19] .fini_array       FINI_ARRAY      00000000 0842f8 000004 04  WA  0   0  4
      [20] .jcr              PROGBITS        00000000 0842fc 000004 00  WA  0   0  4
      [21] .data.rel.ro      PROGBITS        00000000 084300 000c00 00  WA  0   0  4
      [22] .dynamic          DYNAMIC         00000000 084f00 000100 08  WA  6   0  4
      [23] .got              PROGBITS        00000000 085000 0001cc 04  WA  0   0  4
      [24] .data             PROGBITS        00000000 0851d0 001004 00  WA  0   0  8
      [25] .bss              NOBITS          00000000 0861d4 00269c 00  WA  0   0  8
      [26] .ARM.attributes   ARM_ATTRIBUTES  00000000 0861d4 00002a 00      0   0  1
      [27] .shstrtab         STRTAB          00000000 0861fe 0000fe 00      0   0  1
    Key to Flags:
      W (write), A (alloc), X (execute), M (merge), S (strings), I (info),
      L (link order), O (extra OS processing required), G (group), T (TLS),
      C (compressed), x (unknown), o (OS specific), E (exclude),
      y (purecode), p (processor specific)  
    ```

### Run core from sources with an implemented module STM32F042

1. Clone Kopycat project git repo:

    ```shell script
    git clone https://github.com/inforion/kopycat.git
    ```

1. Import whole Kopycat project into IntelliJ

    1. Create a new project in IntelliJ from existing sources where you have cloned Kopycat repository
    1. When creating new project specify `Import project from external model -> Gradle`
    1. Choose import project when IntelliJ has loaded sources and wait until project is indexed
    
    After project has been imported, you will have the next project structure:
    - `buildSrc` contains Gradle plugins for build system
    - `kopycat` contains the emulator core and library manager. Also in `src/main/kotlin/ru/inforion/lab403/kopycat/modules` embedded modules such as memories (RAM, ROM, NAND, etc), terminals, etc. are placed
    - `kopycat-modules` contains user's modules (devices)
        - `cores` contains processor cores x86, mips, arm, etc.
        - `mcu` contains microprocessors units based on these cores (i.e. elanSC520, stm32f0xx, etc.)
        - `devices` contains devices based on MCUs
        
    `cores`, `mcu`, `devices` in project naming convention called **library** and a bunch of libraries called **registry**. When starting Kopycat you should specify **registry** path with compiled modules using `-y` parameter. Division on libraries is just an agreement to organize modules.

1. Go to the sources of examples in `kopycat-modules`: `kopycat-modules/misc/examples/src/main/kotlin/ru/inforion/lab403/examples/`
    NOTE: As an example `virtarm`, `stm32f042_bytes` and `stm32f042_ihex` are placed here
    
1. Create an Object file in this directory using IntelliJ right-click menu, for example, `test`. This will be an entry point.

1. Write the next code in it:

    ```kotlin
    package ru.inforion.lab403.examples
    
    import ru.inforion.lab403.common.extensions.hex8
    import ru.inforion.lab403.common.extensions.unhexlify
    import ru.inforion.lab403.kopycat.cores.arm.hardware.processors.AARMCPU
    import ru.inforion.lab403.kopycat.cores.base.common.Module
    import ru.inforion.lab403.kopycat.gdbstub.GDBServer
    import ru.inforion.lab403.kopycat.modules.stm32f042.STM32F042
    import ru.inforion.lab403.kopycat.modules.terminals.UartSerialTerminal
    
    object test {
        @JvmStatic
        fun main(args: Array<String>) {
            // Create simple firmware
            // movs  r0, #3
            // movs  r1, #7
            // adds  r2, r1, r0
            val firmware = "0000000009000000032007210a18".unhexlify()
    
            // Create top-level module. It's necessarily only one top!
            val top = object : Module(null, "top") {
                // Place STM32F042 inside top module
                val mcu = STM32F042(this, "mcu", firmware)
    
                // Place virtual terminal -> will be created using socat
                // You could create virtual terminal by yourself using socat and specify path to /dev/tty...
                // For windows user you should use Com2Com and specify manually COMX from it
                val term1 = UartSerialTerminal(this, "term1", "socat:")
    
                init {
                    // Make actual connection between STM32F042 and Virtual terminal
                    buses.connect(mcu.ports.usart1_m, term1.ports.term_s)
                    buses.connect(mcu.ports.usart1_s, term1.ports.term_m)
    
                    // ARM debugger already in stm
                }
            }
    
            // initialize and reset top module and all inside
            top.initializeAndResetAsTopInstance()
    
            // start GDB server on port 23946
            val gdb = GDBServer(23946, true, binaryProtoEnabled = false)
    
            // connect GDB and device debugger
            gdb.debuggerModule(top.debugger)
    
            // HERE EMULATOR READ TO WORK WITH GDB
            // Below just code to see different API styles
    
            // step CPU core using debugger
            top.debugger.step()
    
            // read CPU register using debugger API
            var r0 = top.debugger.regRead(0)
            var r15 = top.debugger.regRead(15)
            println("using debugger API: r0 = 0x${r0.hex8} r15 = 0x${r15.hex8}")
    
            // read CPU register using core API
            r0 = top.core.reg(0)
            r15 = top.core.reg(15)
            println("using Core/CPU API: r0 = 0x${r0.hex8} r15 = 0x${r15.hex8}")
    
            // read CPU register using internal CPU API
            val arm = top.core.cpu as AARMCPU
            r0 = arm.regs.r0.value
            r15 = arm.regs.pc.value
            println("using internal API: r0 = 0x${r0.hex8} r15 = 0x${r15.hex8}")
    
            // process here will wait until debugger stop
        }
    }
    ```
    
    NOTE: This example is also placed in project sources as `stm32f042_bytes`.
    
1. Run the application using green triangle near `fun main(args: Array<String>)` and you should see log:

    ```log
    15:28:04 WRN ...cat$Companion.createPseudoTerminal(Socat.kt:75): Pseudo-terminals created for top.term1: /dev/ttys020 and /dev/ttys021
    15:28:05 CFG ....initializeAndResetAsTopInstance(Module.kt:189): Setup core to top.mcu.cortexm0.arm for top
    15:28:05 CFG ....initializeAndResetAsTopInstance(Module.kt:194): Setup debugger to top.mcu.dbg for top
    15:28:05 WRN ....initializeAndResetAsTopInstance(Module.kt:210): Tracer wasn't found in top...
    15:28:05 CFG ...s.UartTerminal$tx$2.invoke(UartTerminal.kt:176): Create transmitter UART terminal thread: 'top.term1'
    15:28:05 CFG ....initializeAndResetAsTopInstance(Module.kt:218): Initializing ports and buses...
    15:28:05 WRN ....initializeAndResetAsTopInstance(Module.kt:220): ATTENTION: Some ports has warning use printModulesPortsWarnings to see it...
    15:28:05 INF ...modules.cortexm0.CORTEXM0.reset(CORTEXM0.kt:58): Setup CORTEX-M0 core PC=0x00000009 MSP=0x00000000
    15:28:05 CFG ....initializeAndResetAsTopInstance(Module.kt:232): Module top is successfully initialized and reset as a top cell!
    15:28:05 INF ...bstub.GDBServer.debuggerModule(GDBServer.kt:78): Set new debugger module top.mcu.dbg for GDB_SERVER(port=23946,alive=true)
    using debugger API: r0 = 0x00000003 r15 = 0x0000000B
    using Core/CPU API: r0 = 0x00000003 r15 = 0x0000000A
    using internal API: r0 = 0x00000003 r15 = 0x0000000A
    15:28:05 INF ...iliary.ANetworkThread.run(ANetworkThread.kt:55): GDB_SERVER waited for clients on [192.168.69.254:23946]
    ```
      
    NOTE: Different r15 register value is the result of the convention: technically ARM CPU is at 0x00000008 (set using second dword of firmware), but the last two bits of PC specifies in which mode CPU operates (ARM, THUMB). For Kopycat this information is stored in a special internal variable, but for the debugger we should signal that CPU is in THUMB mode.
    
    In this example a different method to work with emulator is also shown.

### Run core from sources with an implemented module VirtARM on ARM1176JZS core

Similar example for `VirtARM` with Linux is shown in the source `misc/examples/main/kotlin/ru/inforion/lab403/examples/virtarm.kt` 

### Run core from sources with your own module (device) on ARMv6M core

1. Do steps 1-3 from the previous abstract

1. Create a module code structure

    1. Create a new library or choose one of the existing in the `kopycat-modules`. For example, in the next part we will use `mcu` library.
    1. Inside `mcu` library create a new folder and name it after your module, for example, `testbench`. 
    1. Inside `testbench` create a new text file with a name `build.gradle` and write the next code into it:
        
        ```groovy
        plugins {
            id 'ru.inforion.lab403.gradle.kopycat'  // kopycat gradle build plugin 
        }
        
        group 'ru.inforion.lab403.kopycat'  // choose any group you want but follow package rules of Java
        version '0.1'
        
        buildKopycatModule {
            library = "mcu"  // library must be a similar to modules library (in our case mcu)
            require += "cores:arm"  // comma separated modules dependency - in our case only arm core 
        } 
        ```

    1. To the file `settings.gradle` in the project root add the following line:
        ```groovy
        include(":kopycat-modules:mcu:testbench")
        ```
       
    1. Reimport the project and IntelliJ should create a folder structure by itself and a blue square should appear near `testbench`. If the structure has not been created then make it by yourself following the next rule:
    
        ```src/main/kotlin/<GROUP>/modules/<MODULE_NAME>``` - let's name this directory `SRC_DIR`
        
        where `<GROUP>` - group that you specified in `build.gradle` file and `<MODULE_NAME>`, your module name.
        
    1. Create a new Kotlin class file in `SRC_DIR` (name it as you wish but follow Kotlin file naming convention, and we recommend to use module name for the main file) i.e. `Testbench`
    
    NOTES: 
    - you could use this structure for future modules
    - you could place several devices in one module
    
1. Write code of your `testbench` device in `Testbench` file

    ```kotlin
    package ru.inforion.lab403.kopycat.modules.testbench
    
    import ru.inforion.lab403.common.extensions.MHz
    import ru.inforion.lab403.kopycat.cores.base.common.Module
    import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
    import ru.inforion.lab403.kopycat.modules.cores.ARMv6MCore
    import ru.inforion.lab403.kopycat.modules.memory.RAM
    
    // Kopycat library manager looks for classes inherited from Module class
    class Testbench(parent: Module?, name: String) : Module(parent, name) {
    
        // Add ARMv6 core into testbench device
        // First argument is parent module (where instantiated device fold)
        // Second argument is name (aka designator) can be any unique name  
        val arm = ARMv6MCore(this, "arm", frequency = 10.MHz, ipc = 1.0)
    
        // Add modifiable memory region into testbench device (size = 1 MB)
        val ram = RAM(this, "ram", size = 0x10_0000)
    
        // Create internal buses description for testbench device
        // Buses are somelike wires and used to connect different parts of device 
        inner class Buses : ModuleBuses(this) {
            val mem = Bus("mem")
        }
    
        // Assign new buses description to testbench device
        override val buses = Buses()
    
        // Make actual connection between CORE and RAM
        init {
            arm.ports.mem.connect(buses.mem)
            ram.ports.mem.connect(buses.mem, offset = 0x0000_0000)
        }
    }
    ```

    Now you could compile and run emulator in two ways: 
    - first: instantiate testbench using code
    - second: use Kopycat library manager 
    
1. Instantiate testbench
    
    1. Create somewhere in the project folder structure **object** file `Starter`, i.e. inside `SRC_DIR`
    1. Write the following code in it:
    
        ```kotlin
        package ru.inforion.lab403.kopycat.modules.testbench
        
        import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
        
        object Starter {
            @JvmStatic
            fun main(args: Array<String>) {
                // Create our Testbench device
                val top = Testbench(null, "testbench")
        
                // Initialize it as a top device (device that has no parent)
                top.initializeAndResetAsTopInstance()
        
                // Write some instructions into memory
                top.core.write(WORD, 0x0000_0000, 0x2003) // movs  r0, #3
                top.core.write(WORD, 0x0000_0002, 0x2107) // movs  r1, #7
                top.core.write(WORD, 0x0000_0004, 0x180A) // adds  r2, r1, r0
        
                // Setup program counter
                // Note, that we may use top.arm.cpu.pc but there is some caveat here
                // top.arm.cpu.pc just change PC but don't make flags changing (i.e. change core mode)
                // so be aware when change PC. 
                top.arm.cpu.BXWritePC(0x0000_0000)
        
                // Make a step
                top.arm.step()
                assert(top.core.reg(0) == 3L)
        
                // Make another step
                top.arm.step()
                assert(top.core.reg(1) == 7L)
        
                // And one more step
                top.arm.step()
                assert(top.core.reg(2) == 10L)
            }
        }
        ```

        Note, this is simple standard application for Kotlin language.
    
    1. Run the application using green triangle near `fun main(args: Array<String>)` and you should see:
    
        ```log
        12:25:28 CFG ....initializeAndResetAsTopInstance(Module.kt:189): Setup core to testbench.arm for testbench
        12:25:28 CFG ....initializeAndResetAsTopInstance(Module.kt:194): Setup debugger to testbench.dbg for testbench
        12:25:28 WRN ....initializeAndResetAsTopInstance(Module.kt:210): Tracer wasn't found in testbench...
        12:25:28 CFG ....initializeAndResetAsTopInstance(Module.kt:218): Initializing ports and buses...
        12:25:28 WRN ....initializeAndResetAsTopInstance(Module.kt:220): ATTENTION: Some ports has warning use printModulesPortsWarnings to see it...
        12:25:28 CFG ....initializeAndResetAsTopInstance(Module.kt:232): Module testbench is successfully initialized and reset as a top cell!
        
        Process finished with exit code 0
        ```
        
        Since we set `assert` function on registers values, it means that the execution has successfully completed.

    1. To make possible connection with GDB debugger (IDA Pro or CLion):
    
        - add the following line after the `arm` instantiation in `Testbench` class: 
        
            ```kotlin
            val dbg = ARMDebugger(this, "dbg")
            ```
        
        - add the following lines to the `init` section in `Testbench` class:
        
            ```kotlin
            dbg.ports.breakpoint.connect(buses.mem)
            dbg.ports.reader.connect(buses.mem)
            ```
          
        - add the following line at the end of the `main` function in `Starter`:
            ```kotlin
            GDBServer(23946, true, binaryProtoEnabled = false).also { it.debuggerModule(top.debugger) }
            ``` 
          
        - after that you can connect using IDA Pro, CLion or other GDB compatible debugger using port 23946

1. Use Kopycat library manager

    This case may be required for dynamic loading modules from library.  

    1. Add run configurations as show in the screenshot below:
    
        First add kopycat-arm-build configuration:
        ![image](https://user-images.githubusercontent.com/2856140/104707644-bcf13000-572d-11eb-9f19-f28184d07402.png)
        
        Then add kopycat-testbench-build (please, pay attention on "before launch" section at bottom of run configuration window):
        ![image](https://user-images.githubusercontent.com/2856140/104707659-bfec2080-572d-11eb-8af3-ffd93ba65efd.png)
        
        And finally kopycat-testbench (please, pay attention on "before launch" section at bottom of run configuration window):
        ![image](https://user-images.githubusercontent.com/2856140/104708043-3ee15900-572e-11eb-926c-cee5b87d1443.png)
    
    1. Start `kopycat-testbench` configuration and after successful start you will see the following log:

        ```log
        12:28:09 INF ...pycat.KopycatStarter.main(KopycatStarter.kt:54): Build version: kopycat-0.3.30-e87ed235-2021.115-Regular [JRE v11.0.6]
        12:28:10 INF ...atStarter.getRegistryPath(KopycatStarter.kt:34): Kopycat directory: '<KOPYCAT_HOME>'
        12:28:13 INF ...kopycat.Kopycat.setSnapshotsDir(Kopycat.kt:103): Change snapshots directory to '/Users/bat/Documents/repos/kopycat-private/temp'
        12:28:13 INF ...pycat.KopycatStarter.main(KopycatStarter.kt:78): GDB_SERVER(port=23946,alive=true) was created
        12:28:13 INF ...ibrary.instantiate(ModuleFactoryLibrary.kt:105): Testbench(null, top)
        12:28:13 INF ...iliary.ANetworkThread.run(ANetworkThread.kt:55): GDB_SERVER waited for clients on [192.168.76.24:23946]
        12:28:14 CFG ....initializeAndResetAsTopInstance(Module.kt:189): Setup core to top.arm for top
        12:28:14 CFG ....initializeAndResetAsTopInstance(Module.kt:194): Setup debugger to top.dbg for top
        12:28:14 WRN ....initializeAndResetAsTopInstance(Module.kt:210): Tracer wasn't found in top...
        12:28:14 CFG ....initializeAndResetAsTopInstance(Module.kt:218): Initializing ports and buses...
        12:28:14 WRN ....initializeAndResetAsTopInstance(Module.kt:220): ATTENTION: Some ports has warning use printModulesPortsWarnings to see it...
        12:28:14 CFG ....initializeAndResetAsTopInstance(Module.kt:232): Module top is successfully initialized and reset as a top cell!
        12:28:14 INF ...ion.lab403.kopycat.Kopycat.open(Kopycat.kt:151): Board top[Testbench] with arm[ARMv6MCore] is ready
        12:28:14 INF ...bstub.GDBServer.debuggerModule(GDBServer.kt:78): Set new debugger module top.dbg for GDB_SERVER(port=23946,alive=true)
        12:28:14 WRN ...at.KopycatStarter.console(KopycatStarter.kt:44): Use -kts option to enable Kotlin console. In the next version Kotlin console will be default.
        12:28:14 CFG ...at.consoles.jep.JepLoader.load(JepLoader.kt:53): Loading Jep using Python command 'python3' to overwrite use '--python' option
        12:28:14 CFG ...oles.jep.PythonShell.version(PythonShell.kt:34): Python Version(major=3, minor=9, micro=0)
        12:28:14 CFG ...s.jep.JepLoader.findFileInPath(JepLoader.kt:25): Jep jar file: /usr/local/lib/python3.9/site-packages/jep/jep-3.9.1.jar
        12:28:14 CFG ...s.jep.JepLoader.findFileInPath(JepLoader.kt:25): Jep shared library file: /usr/local/lib/python3.9/site-packages/jep/jep.cpython-39-darwin.so
        Jep starting successfully!
        12:28:15 INF ...ycat.KopycatStarter.main(KopycatStarter.kt:112): Python console enabled
        Python > 
        ```
       
        NOTE: In this case we have not added any data into RAM and have not set PC. You could do it inside `Testbench` 