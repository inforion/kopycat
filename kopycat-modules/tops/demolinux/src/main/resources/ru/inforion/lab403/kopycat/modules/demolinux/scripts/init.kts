import ru.inforion.lab403.common.extensions.hexlify
import ru.inforion.lab403.common.extensions.ulong
import ru.inforion.lab403.kopycat.cores.base.extensions.TRACER_STATUS_SKIP
import ru.inforion.lab403.kopycat.modules.demolinux.DemoLinux
import ru.inforion.lab403.kopycat.cores.x86.config.*
import ru.inforion.lab403.kopycat.experimental.tracer.TracerBypassUtils

val x86 = kc.core as ru.inforion.lab403.kopycat.modules.cores.x86Core
val core = kc.core as ru.inforion.lab403.kopycat.modules.cores.x86Core
val top = kc.top as DemoLinux

val funUtils = top.demoLinuxTracer.funUtils
val data = top.demoLinuxTracer.data
val tracerUtils = top.demoLinuxTracer.tracerUtils
val tracerBypassUtils = top.demoLinuxTracer.TracerBypassUtils

fun mem_phys(address: ULong, size: Int): String = x86.mmu.ports.outp.load(address, size).hexlify()
fun mem_virt(address: ULong, size: Int): String = x86.cpu.ports.mem.load(address, size).hexlify()

top.demoLinuxTracer.hooks[0xFFFFFFFF8117F1A0uL] = { tracerBypassUtils.genericFunctionBypass(TracerBypassUtils.BypassData(0xFFFFFFFF8117F1A0uL, "module_sig_check (bypass)", 3, (0L).ulong)) }
top.demoLinuxTracer.hooks[0xFFFFFFFF81A71604uL] = {
    println("bypass net queue")
    x86.pc = 0xFFFFFFFF81A71606uL
    TRACER_STATUS_SKIP
}
top.demoLinuxTracer.hooks[0xFFFFFFFF81A7160FuL] = {
    x86.pc = 0xFFFFFFFF81A71799uL
    TRACER_STATUS_SKIP
}
