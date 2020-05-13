package ru.inforion.lab403.kopycat.auxiliary

import ru.inforion.lab403.common.logging.logger
import java.net.*
import java.util.logging.Level


abstract class ANetworkThread(
        val desiredPort: Int,
        name: String,
        val bufSize: Int = 1024,
        start: Boolean = false,
        isDaemon: Boolean = false) : Thread(name), AutoCloseable {

    companion object {
        private val log = logger(Level.INFO)
    }

    private val server = ServerSocket()
    private var client: Socket? = null
    private var running = false

    init {
        this.isDaemon = isDaemon

        if (start) {
            this.bind()
            this.start()
        }
    }

    override fun start() {
        if (!isAlive) {
            running = true
            super.start()
        }
    }

    fun bind() {
        server.bind(InetSocketAddress(desiredPort))
    }

    val address: String get() = InetAddress.getLocalHost().hostAddress
    val port get() = server.localPort

    abstract fun onConnect(): Boolean
    abstract fun onReceive(data: ByteArray): Boolean
    open fun onDisconnect() = Unit

    override fun run() {
        log.info { "$name thread started on $name [$address:$port]" }
        while (running) {
            try {
                log.info { "$name waited for clients on $port..." }
                client = server.accept()
            } catch (e: SocketException) {
                log.info { "$name thread connection closed" }
                running = false
            }

            client?.let { client ->
                // See https://en.wikipedia.org/wiki/Nagle%27s_algorithm
                client.tcpNoDelay = true

                log.info { "Client $client connected to $name" }
                var connected = try {
                    onConnect()
                } catch (e: SocketException) {
                    log.info { "Client $client close connection with $name" }
                    false
                }

                val buf = ByteArray(bufSize)

                while (connected) {
                    try {
                        var bytes = client.inputStream.read(buf)
                        while (client.inputStream.available() > 0 && bytes < bufSize)
                            bytes += client.inputStream.read(buf, bytes, bufSize - bytes)

                        connected = if (bytes > 0) {
                            val data = ByteArray(bytes)
                            System.arraycopy(buf, 0, data, 0, bytes)
                            onReceive(data)
                        } else false
                    } catch (exc: SocketException) {
                        log.info { "$this -> $exc" }
                        connected = false
                    } catch (exc: Exception) {
                        log.severe { "$this -> $exc" }
                        exc.printStackTrace()
                        connected = false
                    }
                }

                log.info { "Client $client close connection with $name" }
                client.close()
                onDisconnect()
            }
        }
        log.info { "$name thread stopped" }
    }

    fun send(data: ByteArray) {
        // log.finer { "Send data: ${data.hexlify()}" }
        client?.outputStream?.write(data)
    }

    fun disconnect() {
        client?.close()
        if (this.isAlive) {
            this.join()
        }
    }

    override fun close() {
        running = false
        server.close()
        disconnect()
    }
}