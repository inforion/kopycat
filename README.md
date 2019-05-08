<img src="https://kopy.cat/static/media/big_logo.169d84fb.png" width="384">

Kopycat is a modular software emulator of hardware systems

Main features are:

- Ease of creating a computer system emulator
- The platform can be described using JSON or in the Kotlin language
- The platform description completely coincides with the block diagram of the device
- Supported architectures: ARM, MIPS, MSP430, v850ES, x86

This project contains CPU cores (ARMv6, ARMv7, MIPS, MSP430, v850ES, x86) and MCU (CortexM0, STM32F0xx, MSP430x44x) for Kopycat. 
You can download JAR-files with this modules via this link https://kopy.cat/download

### Install Kopycat
1. Download emulator core kopycat-0.3.0-RC3 (https://kopy.cat/download)
2. Unzip archive in any directory
3. Add environment variable `KOPYCAT_HOME` to this directory, e.g. `KOPYCAT_HOME=D:\kopycat-0.3.0-RC3` (optional)

### Build current or custom modules
1. Clone this repo to any directory
2. Import project from this directory
3. If you have not set the environment variable `KOPYCAT_HOME`, fix all build.gradle files 
    3.1. Remove this lines
          
          ext.kcHome = System.getenv("KOPYCAT_HOME")
          ext.kcJar = new File(kcHome, "lib/kopycat.jar")
    3.2. Replace line
    3.3. Refresh gradle
4. Now you can build all available modules
