package mpeciakk

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.bytes.ByteArrayDecoder
import io.netty.handler.codec.bytes.ByteArrayEncoder
import mpeciakk.manager.EntityManager
import mpeciakk.manager.PlayerManager
import mpeciakk.network.Connection
import mpeciakk.network.packet.PacketRegistry
import mpeciakk.network.packet.c2s.*
import mpeciakk.network.packet.s2c.S2CKeepAlivePacket
import mpeciakk.world.World
import kotlin.concurrent.fixedRateTimer
import kotlin.concurrent.thread


class MinecraftServer {
    val playerManager = PlayerManager()
    val entityManager = EntityManager()
    val world = World()

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
        PacketRegistry.register(C2SCreativeInventoryActionPacket())
        PacketRegistry.register(C2SCloseWindowPacket())
        PacketRegistry.register(C2SPlayerBlockPlacementPacket())
        PacketRegistry.register(C2SAnimationPacket())
        PacketRegistry.register(C2SPlayerAbilitiesPacket())
        PacketRegistry.register(C2SHeldItemChangePacket())
        PacketRegistry.register(C2SPlayerDiggingPacket())
        PacketRegistry.register(C2SClientStatusPacket())
        PacketRegistry.register(C2SUseItemPacket())

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