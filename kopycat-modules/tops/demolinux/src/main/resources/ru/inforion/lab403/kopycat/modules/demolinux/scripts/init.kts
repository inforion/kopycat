import ru.inforion.lab403.common.extensions.hexlify
import ru.inforion.lab403.common.extensions.startOrNull
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.modules.demolinux.DemoLinux
import ru.inforion.lab403.kopycat.cores.x86.config.*
import ru.inforion.lab403.kopycat.experimental.hazard.linux.specific.x86_64.api.queued.LinuxQueuedFilesystemWrite
import ru.inforion.lab403.kopycat.experimental.hazard.linux.specific.x86_64.api.queued.LinuxQueuedPrintk
import ru.inforion.lab403.kopycat.experimental.hazard.linux.specific.x86_64.data.interfaces.LinuxThreadInfo
import ru.inforion.lab403.kopycat.experimental.linux.common.file.x86LinuxFileControl
import ru.inforion.lab403.kopycat.runtime.analyzer.stack.printer
import ru.inforion.lab403.kopycat.runtime.funcall.FunArg
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.io.path.invariantSeparatorsPathString

val x86 = kc.core as ru.inforion.lab403.kopycat.modules.cores.x86Core
val core = kc.core as ru.inforion.lab403.kopycat.modules.cores.x86Core
val top = kc.top as DemoLinux

//val funUtils = top.demoLinuxTracer.funUtils
val data = top.demoLinuxTracer.data
val tracerUtils = top.demoLinuxTracer.tracerUtils
val tracerBypassUtils = top.demoLinuxTracer.TracerBypassUtils

@Transient val log = logger()

fun linuxThreadInfoWorkaround(): LinuxThreadInfo = object : LinuxThreadInfo {
    override var addrLimit: ULong = 0xFFFFFFFF_FFFFFFFFuL

    override fun isAddrInLimit(address: ULong): Boolean = true
}

fun copyFromOsQ(filepath: String, destination: String) {
    println("copyFromOsQ (lazy). filepath='$filepath' destination='$destination'")

    LinuxQueuedFilesystemWrite(
        top.linux.raw,
        top.queueApi,
        x86LinuxFileControl(),
        filepath,
        destination,
        bucketSize = 1024,
        threadInfoBlock = { linuxThreadInfoWorkaround() },
    ).start()
}

fun copyFromOsDirQ(directoryPath: String, destination: String) {
    println("copyFromOsQ (lazy). directoryPath='$directoryPath' destination='$destination'")

    val all = File(directoryPath).let { path ->
        path
            .walk()
            .filter { it.isFile }
            .map {
                val linuxDestination = (Path(destination) / it.relativeTo(path).toPath()).invariantSeparatorsPathString
                println("Copying $it -> $linuxDestination")

                LinuxQueuedFilesystemWrite(
                    top.linux.raw,
                    top.queueApi,
                    x86LinuxFileControl(),
                    it.absolutePath,
                    linuxDestination,
                    bucketSize = 1024,
                    threadInfoBlock = { linuxThreadInfoWorkaround() },
                )
            }
    }.toList()

    all.mapIndexed { i, d -> i to d }.slice(1 until all.size).forEach { (i, d) ->
        all[i - 1].onClose.add {
            println("Finished '${this@add.sourcePath}'")
            d.start()
        }
    }
    all.startOrNull?.start()
}

fun mem_phys(address: ULong, size: Int): String = x86.mmu.ports.outp.load(address, size).hexlify()
fun mem_virt(address: ULong, size: Int): String = x86.cpu.ports.mem.load(address, size).hexlify()

fun testPrintk() {
    require(x86.isRing0) { "CPU must be in ring 0" }
    LinuxQueuedPrintk(top.linux.raw, top.queueApi)
        .start(
            "Test %s string\n",
            FunArg.String("test"),
        )
}

fun saPrintIda() {
    log.severe { top.stackAnalyzer.printer.toIDAPython() }
}
