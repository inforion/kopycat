rootProject.name = "kopycat"

if (gradle is ExtensionAware) {
    (gradle as ExtensionAware).extra["copyExternalDependencies"] = true
}

include(":kopycat")



include(":kopycat-modules:cores:mips")
include(":kopycat-modules:cores:x86")
include(":kopycat-modules:cores:v850es")
include(":kopycat-modules:cores:msp430")
include(":kopycat-modules:cores:ppc")
include(":kopycat-modules:cores:arm")





include(":kopycat-modules:mcu:cortexm0")
include(":kopycat-modules:mcu:stm32f0xx")
include(":kopycat-modules:mcu:elanSC520")
include(":kopycat-modules:mcu:atom2758")
include(":kopycat-modules:mcu:pic32mz")
include(":kopycat-modules:mcu:msp430x44x")
include(":kopycat-modules:mcu:p2020")
include(":kopycat-modules:mcu:virtarm")
include(":kopycat-modules:mcu:testbench")
include(":kopycat-modules:mcu:virtmips")

include(":kopycat-modules:misc:examples")



include(":kopycat-modules:misc:experimental")



include(":common:proposal")
include(":common:elfloader")
include(":common:elfloader2")

include (":kopycat-modules:tops:demolinux")
