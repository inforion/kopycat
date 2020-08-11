<img src="https://kopycat.ru/static/media/big_logo.169d84fb.png" width="384">

Kopycat is a multi-architecture hardware emulation solution

## Description

Main features are:

- Easy to assemble. Configure your own platform using JSON or Kotlin
- Easy to customise. Create your own platform-module using Kotlin
- One-to-one correspondence. Virtual platform representation is identical to block diagram
- Multiple supported architectures: MIPS, ARM, MSP430, v850ES, x86

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
2. Highly recommended Python (version 2.7 and 3.6, 3.8 tested) with Jep package (version 0.3.9 tested) for embedded console in Kopycat.
    
NOTE: prebuild OpenJDK installer is available on https://adoptopenjdk.net/ 
    
Linux and OSX users can use package manager to install OpenJDK and Python. To install **jep** `pip` command can be used, but before **compiler** and **toolchain** have to be installed for building **jep** package. On Linux system **gcc** from `apt` and on OSX `Developer Tools` with XCode (XCode by itself is not needed, but we need a compiler). As for Windows users you may face a lot of difficulties while compiling Python packages.
    
### Installation of requirements on Windows 10

1. For Windows, you should manually download OpenJDK package and setup `PATH` and `JAVA_HOME` environment variables, see https://openjdk.java.net/install/

1. Download and install Python (don't forget to add Python to `PATH` during installation and select to install **pip**) from the official site: https://www.python.org/downloads/

1. Download and install Visual Studio build tools: https://visualstudio.microsoft.com/visual-cpp-build-tools/ (**DON'T FORGET TO SELECT VERSION 14.x**)

1. Fix ¯\_(ツ)_/¯ Python setuptools to work with Visual Studio compiler: https://stackoverflow.com/a/20050195/1312718

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

1. IntelliJ (version >= 2020.1)
2. Kotlin plugin (version >= 1.3.72)

## Getting started

In this part of readme you will see how to start Kopycat for device with STM32F042 (core: Cortex-M0; architecture: ARMv6M) and virtual ARM device (core: ARM1176JZS; architecture: ARMv6/v7). 

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
    
    ```log
    bat@Kernel % kopycat -l mcu -n stm32f042_example -g 23946 -p "firmware=example:usart_poll,tty1=socat:,tty2=socat:"
    INFORION_LOGGING_PRINT: null
    INFORION_LOGGING_CONF_PATH: null
    15:43:40 INFO   [ KopycatStarter.main            ]: Java version: 11.0.6
    15:43:40 INFO   [ KopycatStarter.main            ]: Working Directory: <WORKING_DIR>
    15:43:40 INFO   [ KopycatStarter.main            ]: Build version information: kopycat-0.3.20-6e48fed1-2020.510-Regular
    15:43:40 INFO   [LibraryRegistry.create          ]: Library configuration line: ,mcu:modules/mcu,cores:modules/cores,devices:modules/devices
    15:43:43 INFO   [ KopycatStarter.main            ]: GDB_SERVER(port=23946,alive=true) was created
    15:43:43 INFO   [eFactoryLibrary.instantiate     ]: stm32f042_example(null, top, firmware=example:usart_poll, tty1=socat:, tty2=socat:)
    15:43:43 INFO   [ ANetworkThread.run             ]: GDB_SERVER thread started on GDB_SERVER [127.0.0.1:23946]
    15:43:43 INFO   [ ANetworkThread.run             ]: GDB_SERVER waited for clients on 23946...
    15:43:43 WARN   [         Module.createPseudoTerm]: Pseudo-terminals created for top.term1: /dev/ttys002 and /dev/ttys004
    15:43:44 WARN   [         Module.createPseudoTerm]: Pseudo-terminals created for top.term2: /dev/ttys005 and /dev/ttys007
    15:43:45 INFO   [         Module.initializeAndRes]: Setup core to top.stm32f042.cortexm0.arm for top
    15:43:45 INFO   [         Module.initializeAndRes]: Setup debugger to top.stm32f042.dbg for top
    15:43:45 WARN   [         Module.initializeAndRes]: Tracer wasn't found in top...
    15:43:45 INFO   [         Module.initializeAndRes]: Initializing ports and buses...
    15:43:45 WARN   [         Module.initializePortsA]: ATTENTION: Some ports has warning use printModulesPortsWarnings to see it...
    15:43:45 INFO   [         Module.reset           ]: Setup CORTEX-M0 core PC=0x080022C1 MSP=0x20001800
    15:43:45 INFO   [         Module.initializeAndRes]: Module top is successfully initialized and reset as a top cell!
    15:43:45 INFO   [        Kopycat.open            ]: Starting virtualization of board top[stm32f042_example] with arm[ARMv6MCore]
    15:43:45 INFO   [      GDBServer.debuggerModule  ]: Set new debugger module top.stm32f042.dbg for GDB_SERVER(port=23946,alive=true)
    15:43:45 CONFIG [      JepLoader.load            ]: Loading Jep using Python command 'python'
    15:43:45 CONFIG [    PythonShell.version         ]: Python Version(major=2, minor=7, micro=17)
    15:43:45 CONFIG [      JepLoader.findFileInPath  ]: Jep jar file: /usr/local/lib/python2.7/site-packages/jep/jep-3.9.0.jar
    15:43:45 CONFIG [      JepLoader.findFileInPath  ]: Jep shared library file: /usr/local/lib/python2.7/site-packages/jep/jep.so
    Jep starting successfully!
    Python > 
    ```
   
   Where `Pseudo-terminals created for top.term1: /dev/ttys002 and /dev/ttys004` 
   - /dev/ttys002 is endpoint to emulator connection to virtual COM port of USART1
   - /dev/ttys004 is endpoint to user connection to virtual COM port of USART1
    
1. Attach to `/dev/ttys004` (name may differ) COM port using, for example, **putty** or **screen**

    ```shell script
    screen /dev/ttys004
    ```

1. In Kopycat console print and press enter:

    ```python
    kc.start()  # run Kopycat emulation
    ```
   
1. Now you can print something in `/dev/ttys004` and will see echo. This echo is sent back by internal firmare of STM32F042. If emulation isn't running, there is no echo in the console.

1. To stop emulation in Kopycat console print and press enter:

    ```python
    kc.halt()
    ```
   
`kc` - is a special proxy object to make visible all methods of Kopycat class from Java in Python interpreter using **jep** library. You can see all available methods of **kc** object in sources of `Kopycat` class.   

### Run prebuild Kopycat core and module of virtual ARM on ARM1176JZS core    

1. Start emulation of another module: **Linux 2.6.xx on ARM core**
   
    ```shell script
    kopycat -y ${KOPYCAT_MODULES_LIB_PATH} -l mcu -n VirtARM -g 23946 -p "tty=socat:"
   
    16:30:16 INFO   [ KopycatStarter.main            ]: Java version: 11.0.6
    16:30:16 INFO   [ KopycatStarter.main            ]: Working Directory: ...
    16:30:16 INFO   [ KopycatStarter.main            ]: Build version information: kopycat-0.3.20-6e48fed1-2020.510-Regular
    16:30:16 INFO   [LibraryRegistry.create          ]: Library configuration line: ...
    16:30:18 INFO   [eFactoryLibrary.instantiate     ]: VirtARM(null, top, tty=socat:)
    16:30:19 WARN   [         Module.createPseudoTerm]: Pseudo-terminals created for top.term: /dev/ttys012 and /dev/ttys013
    16:30:20 INFO   [         Module.initializeAndRes]: Setup core to top.arm1176jzs for top
    16:30:20 INFO   [         Module.initializeAndRes]: Setup debugger to top.dbg for top
    16:30:20 WARN   [         Module.initializeAndRes]: Tracer wasn't found in top...
    16:30:20 INFO   [         Module.initializeAndRes]: Initializing ports and buses...
    16:30:20 WARN   [         Module.initializePortsA]: ATTENTION: Some ports has warning use printModulesPortsWarnings to see it...
    16:30:20 FINE   [       ARMv6CPU.reset           ]: pc=0x00000000 sp=0x00000000
    16:30:20 INFO   [         Module.initializeAndRes]: Module top is successfully initialized and reset as a top cell!
    16:30:20 INFO   [        Kopycat.open            ]: Starting virtualization of board top[VirtARM] with arm1176jzs[ARM1176JZS]
    16:30:20 CONFIG [      JepLoader.load            ]: Loading Jep using Python command 'python'
    16:30:20 CONFIG [    PythonShell.version         ]: Python Version(major=2, minor=7, micro=17)
    16:30:20 CONFIG [      JepLoader.findFileInPath  ]: Jep jar file: /usr/local/lib/python2.7/site-packages/jep/jep-3.9.0.jar
    16:30:20 CONFIG [      JepLoader.findFileInPath  ]: Jep shared library file: /usr/local/lib/python2.7/site-packages/jep/jep.so
    Jep starting successfully!
    Python > 
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
   
    Library 'PeripheralFactoryLibrary[mcu]':
        Module: [                 RTC] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/elanSC520.jar]
        Module: [           Testbench] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/testbench.jar]
        Module: [                 SCP] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/elanSC520.jar]
        Module: [     stm32f042_rhino] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/stm32f0xx.jar]
        Module: [               rhino] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/stm32f0xx.jar]
        Module: [               GPIOx] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/stm32f0xx.jar]
        Module: [                NVIC] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/cortexm0.jar]
        Module: [             VirtARM] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/virtarm.jar]
        Module: [                EXTI] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/stm32f0xx.jar]
        Module: [              Am5X86] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/elanSC520.jar]
        Module: [                 STK] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/cortexm0.jar]
        Module: [                DMAC] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/stm32f0xx.jar]
        Module: [            CORTEXM0] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/cortexm0.jar]
        Module: [              USARTx] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/stm32f0xx.jar]
        Module: [                  BT] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/stm32f0xx.jar]
        Module: [                IWDG] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/stm32f0xx.jar]
        Module: [              SYSCFG] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/stm32f0xx.jar]
        Module: [               GPBUS] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/elanSC520.jar]
        Module: [               TIM18] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/stm32f0xx.jar]
        Module: [                 FMI] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/stm32f0xx.jar]
        Module: [                 TSC] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/stm32f0xx.jar]
        Module: [                TIMx] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/stm32f0xx.jar]
        Module: [                UART] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/elanSC520.jar]
        Module: [           STM32F042] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/stm32f0xx.jar]
        Module: [                 LED] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/stm32f0xx.jar]
        Module: [                 PIC] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/elanSC520.jar]
        Module: [        AMDElanSC520] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/elanSC520.jar]
        Module: [                BOOT] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/elanSC520.jar]
        Module: [               FLASH] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/stm32f0xx.jar]
        Module: [                GPIO] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/elanSC520.jar]
        Module: [             NS16550] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/virtarm.jar]
        Module: [                 SCB] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/cortexm0.jar]
        Module: [                 RCC] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/stm32f0xx.jar]
        Module: [                 SAC] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/elanSC520.jar]
        Module: [               Timer] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/virtarm.jar]
        Module: [                 PCI] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/elanSC520.jar]
        Module: [                 PIT] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/elanSC520.jar]
        Module: [               SDRAM] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/elanSC520.jar]
        Module: [                 SAM] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/elanSC520.jar]
        Module: [   stm32f042_example] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/stm32f0xx.jar]
    Library 'PeripheralFactoryLibrary[cores]':
        Module: [             x86Core] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/cores/x86.jar]
        Module: [         x86Debugger] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/cores/x86.jar]
        Module: [            MipsCore] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/cores/mips.jar]
        Module: [        MipsDebugger] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/cores/mips.jar]
        Module: [         ARMDebugger] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/cores/arm.jar]
        Module: [          ARM1176JZS] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/cores/arm.jar]
        Module: [          ARMv6MCore] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/cores/arm.jar]
        Module: [           ARMv7Core] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/cores/arm.jar]
        Module: [          v850ESCore] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/cores/v850es.jar]
        Module: [      v850ESDebugger] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/cores/v850es.jar]
        Module: [      MSP430Debugger] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/cores/msp430.jar]
        Module: [          MSP430Core] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/cores/msp430.jar]
        Module: [         PPCDebugger] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/cores/ppc.jar]
        Module: [              E500v2] as JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/cores/ppc.jar]
    Library 'PeripheralFactoryLibrary[common]':
        Module: [                NAND] as ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.20.jar/ru/inforion/lab403/kopycat/modules/common/NAND.class]
        Module: [              i82551] as ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.20.jar/ru/inforion/lab403/kopycat/modules/common/i82551.class]
        Module: [              EEPROM] as ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.20.jar/ru/inforion/lab403/kopycat/modules/common/EEPROM.class]
        Module: [             Signals] as ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.20.jar/ru/inforion/lab403/kopycat/modules/common/Signals.class]
        Module: [            Am79C972] as ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.20.jar/ru/inforion/lab403/kopycat/modules/common/Am79C972.class]
        Module: [                  SD] as ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.20.jar/ru/inforion/lab403/kopycat/modules/common/SD.class]
        Module: [             PCIHost] as ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.20.jar/ru/inforion/lab403/kopycat/modules/common/pci/PCIHost.class]
        Module: [              M95160] as ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.20.jar/ru/inforion/lab403/kopycat/modules/common/M95160.class]
        Module: [        CompactFlash] as ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.20.jar/ru/inforion/lab403/kopycat/modules/common/CompactFlash.class]
        Module: [                 Hub] as ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.20.jar/ru/inforion/lab403/kopycat/modules/common/Hub.class]
        Module: [             ATACTRL] as ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.20.jar/ru/inforion/lab403/kopycat/modules/common/ATACTRL.class]
    Library 'PeripheralFactoryLibrary[memory]':
        Module: [                 ROM] as ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.20.jar/ru/inforion/lab403/kopycat/modules/memory/ROM.class]
        Module: [           SparseRAM] as ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.20.jar/ru/inforion/lab403/kopycat/modules/memory/SparseRAM.class]
        Module: [                VOID] as ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.20.jar/ru/inforion/lab403/kopycat/modules/memory/VOID.class]
        Module: [                 RAM] as ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.20.jar/ru/inforion/lab403/kopycat/modules/memory/RAM.class]
    Library 'terminals':
        Module: [  UartStreamTerminal] as ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.20.jar/ru/inforion/lab403/kopycat/modules/terminals/UartStreamTerminal.class]
        Module: [  UartSerialTerminal] as ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.20.jar/ru/inforion/lab403/kopycat/modules/terminals/UartSerialTerminal.class]
        Module: [        UartTerminal] as ClassModuleFactoryBuilder[<KOPYCAT_HOME>/lib/kopycat-0.3.20.jar/ru/inforion/lab403/kopycat/modules/terminals/UartTerminal.class]
    ```
   
1. To get info of only modules in libraries, that can be used as a top module run Kopycat using the following command: `kopycat -top`

    ```shell script
    Library 'PeripheralFactoryLibrary[mcu]':
        Module: [           Testbench] -> JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/testbench.jar]
        Module: [     stm32f042_rhino] -> JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/stm32f0xx.jar]
        Module: [               rhino] -> JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/stm32f0xx.jar]
        Module: [          MSP430x44x] -> JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/msp430x44x.jar]
        Module: [               P2020] -> JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/p2020.jar]
        Module: [             VirtARM] -> JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/virtarm.jar]
        Module: [   stm32f042_example] -> JarModuleFactoryBuilder[<KOPYCAT_MODULES_LIB_PATH>/mcu/stm32f0xx.jar]
    ```
   
### Get help 
   
To get full help, run Kopycat using the following command `kopycat --help`:

```
16:27:52 INFO   [ KopycatStarter.main            ]: Java version: 11.0.6
16:27:52 INFO   [ KopycatStarter.main            ]: Working Directory: ...
16:27:52 INFO   [ KopycatStarter.main            ]: Build version information: kopycat-0.3.20-6e48fed1-2020.510-Regular
usage: kopycat [-h] [-r REST] [-g GDB_PORT] [-gb] [-n NAME] [-l LIBRARY] [-s SNAPSHOT] [-u MODULES] [-y REGISTRY] [-p PARAMETERS] [--python PYTHON] [-pw] [-all] [-top] [-ci]

virtualization platform

optional arguments:
  -h, --help             show this help message and exit
  -r REST, --rest REST   REST server port. If null - Commander will work
  -g GDB_PORT, --gdb-port GDB_PORT
                         GDB server port (if not specified then not started)
  -gb, --gdb-bin-proto   GDB server enabled binary protocol (default: false)
  -n NAME, --name NAME   Top instance module name (with package path dot-separated)
  -l LIBRARY, --library LIBRARY
                         Top instance module library name
  -s SNAPSHOT, --snapshot SNAPSHOT
                         Snapshot file (top instance module/library can be obtained from here)
  -u MODULES, --modules MODULES
                         Modules libraries paths in format: lib1:path/to/lib1,lib2:path/to/lib2
  -y REGISTRY, --registry REGISTRY
                         Path to registry with libraries
  -p PARAMETERS, --parameters PARAMETERS
                         Parameters for top module constructor in format: arg1=100,arg2=/dev/io
  --python PYTHON        Python interpreter command (default: python)
  -pw, --ports-warnings  Print all ports warnings when loading Kopycat module at startup (default: false)
  -all, --modules-registry-all-info
                         Print all loaded modules info and exit (default: false)
  -top, --modules-registry-top-info
                         Print top loaded modules info and exit (default: false)
  -ci, --connections-info
                         Print hier. top module buses connections info at startup (default: false)
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
    22:44:26 WARN   [         Module.createPseudoTerm]: Pseudo-terminals created for top.term1: /dev/ttys002 and /dev/ttys004
    22:44:27 INFO   [         Module.initializeAndRes]: Setup core to top.mcu.cortexm0.arm for top
    22:44:27 INFO   [         Module.initializeAndRes]: Setup debugger to top.mcu.dbg for top
    22:44:27 WARN   [         Module.initializeAndRes]: Tracer wasn't found in top...
    22:44:27 INFO   [         Module.initializeAndRes]: Initializing ports and buses...
    22:44:27 WARN   [         Module.initializePortsA]: ATTENTION: Some ports has warning use printModulesPortsWarnings to see it...
    22:44:27 INFO   [         Module.reset           ]: Setup CORTEX-M0 core PC=0x00000009 MSP=0x00000000
    22:44:27 INFO   [         Module.initializeAndRes]: Module top is successfully initialized and reset as a top cell!
    22:44:27 INFO   [      GDBServer.debuggerModule  ]: Set new debugger module top.mcu.dbg for GDB_SERVER(port=23946,alive=true)
    22:44:27 INFO   [ ANetworkThread.run             ]: GDB_SERVER thread started on GDB_SERVER [127.0.0.1:23946]
    22:44:27 INFO   [ ANetworkThread.run             ]: GDB_SERVER waited for clients on 23946...
    using debugger API: r0 = 0x00000003 r15 = 0x0000000D
    using Core/CPU API: r0 = 0x00000003 r15 = 0x0000000C
    using internal API: r0 = 0x00000003 r15 = 0x0000000C
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
        19:29:04 INFO   [         Module.initializeAndRes]: Setup core to testbench.arm for testbench
        19:29:04 WARN   [         Module.initializeAndRes]: Debugger wasn't found in testbench...
        19:29:04 WARN   [         Module.initializeAndRes]: Tracer wasn't found in testbench...
        19:29:04 INFO   [         Module.initializeAndRes]: Initializing ports and buses...
        19:29:04 WARN   [         Module.initializePortsA]: ATTENTION: Some ports has warning use printModulesPortsWarnings to see it...
        19:29:04 INFO   [         Module.initializeAndRes]: Module testbench is successfully initialized and reset as a top cell!
        
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

    1. Add run configuration as show in the screenshot below:
    
        IMG1
    
    1. Start `kopycat-testbench` configuration and after successful start you will see the following log:

        ```log
        19:59:08 INFO   [ KopycatStarter.main            ]: Java version: 11.0.6
        19:59:08 INFO   [ KopycatStarter.main            ]: Working Directory: ...
        19:59:08 INFO   [ KopycatStarter.main            ]: Build version information: kopycat-0.3.20-a3078491-2020.417-Regular
        19:59:08 INFO   [LibraryRegistry.create          ]: Library configuration line: ,mcu:production/modules/mcu,cores:production/modules/cores,devices:production/modules/devices
        19:59:10 INFO   [ KopycatStarter.main            ]: GDB_SERVER(port=23946,alive=true) was created
        19:59:10 INFO   [eFactoryLibrary.instantiate     ]: Testbench(null, top)
        19:59:10 INFO   [ ANetworkThread.run             ]: GDB_SERVER thread started on GDB_SERVER [192.168.69.254:23946]
        19:59:10 INFO   [ ANetworkThread.run             ]: GDB_SERVER waited for clients on 23946...
        19:59:10 INFO   [         Module.initializeAndRes]: Setup core to top.arm for top
        19:59:10 INFO   [         Module.initializeAndRes]: Setup debugger to top.dbg for top
        19:59:10 WARN   [         Module.initializeAndRes]: Tracer wasn't found in top...
        19:59:10 INFO   [         Module.initializeAndRes]: Initializing ports and buses...
        19:59:10 WARN   [         Module.initializePortsA]: ATTENTION: Some ports has warning use printModulesPortsWarnings to see it...
        19:59:10 INFO   [         Module.initializeAndRes]: Module top is successfully initialized and reset as a top cell!
        19:59:10 INFO   [        Kopycat.open            ]: Starting virtualization of board top[Testbench] with arm[ARMv6MCore]
        19:59:10 INFO   [      GDBServer.debuggerModule  ]: Set new debugger module top.dbg for GDB_SERVER(port=23946,alive=true)
        Jep starting successfully!
        Python > 
        ```
       
        NOTE: In this case we have not added any data into RAM and have not set PC. You could do it inside `Testbench` 
