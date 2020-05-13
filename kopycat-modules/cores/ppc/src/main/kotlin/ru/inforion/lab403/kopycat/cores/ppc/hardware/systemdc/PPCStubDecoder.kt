package ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc

import ru.inforion.lab403.kopycat.modules.cores.PPCCore



class PPCStubDecoder(core: PPCCore) : APPCSystemDecoder(core) {
    override val name: String = "PowerPC [stub] decoder"
}