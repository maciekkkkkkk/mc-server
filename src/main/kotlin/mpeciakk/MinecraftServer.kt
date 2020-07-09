package mpeciakk

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.bytes.ByteArrayDecoder
import io.netty.handler.codec.bytes.ByteArrayEncoder
import mpeciakk.entity.Player
import mpeciakk.manager.PlayerManager
import mpeciakk.net.Connection
import mpeciakk.packet.PacketRegistry
import mpeciakk.packet.c2s.*
import mpeciakk.packet.s2c.S2CKeepAlivePacket
import java.util.*
import kotlin.concurrent.fixedRateTimer
import kotlin.concurrent.thread

class MinecraftServer {

    val playerManager = PlayerManager()

    fun start() {
        PacketRegistry.register(C2SHandshakePacket())
        PacketRegistry.register(C2SPingPacket())
        PacketRegistry.register(C2SClientSettingsPacket())
        PacketRegistry.register(C2SPluginMessagePacket())
        PacketRegistry.register(C2SPlayerPositionAndRotationPacket())
        PacketRegistry.register(C2SChatMessagePacket())
        PacketRegistry.register(C2SKeepAlivePacket())
        PacketRegistry.register(C2SPlayerPositionPacket())
        PacketRegistry.register(C2SPlayerMovementPacket())
        PacketRegistry.register(C2SPlayerRotationPacket())
        PacketRegistry.register(C2SEntityActionPacket())

        thread(name = "NETTY THREAD") {
            val bossGroup = NioEventLoopGroup(1)
            val workerGroup = NioEventLoopGroup()

            try {
                val b = ServerBootstrap()
                b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel::class.java)
                    .childHandler(object : ChannelInitializer<SocketChannel>() {
                        override fun initChannel(ch: SocketChannel) {
                            val p = ch.pipeline()

                            p.addLast(
                                ByteArrayEncoder(),
                                ByteArrayDecoder(),
                                Connection()
                            )
                        }
                    })

                val f = b.bind(25565).sync()

                f.channel().closeFuture().sync()
            } finally {
                bossGroup.shutdownGracefully()
                workerGroup.shutdownGracefully()
            }
        }

        fixedRateTimer(name = "KEEP ALIVE PACKET", period = 1000) {
            Connection.sendToAll(S2CKeepAlivePacket(System.nanoTime()))
        }
    }
}