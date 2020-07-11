package mpeciakk.network

import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.group.DefaultChannelGroup
import io.netty.util.concurrent.GlobalEventExecutor
import mpeciakk.entity.Player
import mpeciakk.minecraftServer
import mpeciakk.network.packet.PacketByteBuf
import mpeciakk.network.packet.PacketRegistry
import mpeciakk.network.packet.s2c.S2CPacket

class Connection : ChannelInboundHandlerAdapter() {
    private lateinit var channel: Channel
    var state: SocketState = SocketState.HANDSHAKE
    lateinit var player: Player

    override fun channelActive(ctx: ChannelHandlerContext?) {
        if (ctx != null) {
            channel = ctx.channel()
        }

        channels.add(channel)
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        handlePacket(ctx, msg as ByteArray)
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        ctx.flush()
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.close()
    }

    private fun handlePacket(ctx: ChannelHandlerContext, bytes: ByteArray) {
        if (bytes.size < 2) return

        val buf = PacketByteBuf(Unpooled.wrappedBuffer(bytes))

        val length = buf.readVarInt()
        val packetId = buf.readVarInt()

        if (packetId != 16 && packetId != 18 && packetId != 19 && packetId != 20) {
            println("Handling $packetId")
        }

        val packet = PacketRegistry.get(packetId)

        if (packet != null) {
            val data = if (length >= bytes.size || length + 1 < 2) ByteArray(0) else bytes.copyOfRange(2, length + 1)

            try {
                packet.read(this, PacketByteBuf(Unpooled.wrappedBuffer(data)))
            } catch (e: Exception) {
                e.message?.let { error(it) }
            }
        } else {
            error("Unhandled packet $packetId")
        }

        if (bytes.size > length + 1) {
            handlePacket(ctx, bytes.copyOfRange(length + 1, bytes.size))
        }
    }

    fun send(packet: S2CPacket) {
        channel.writeAndFlush(packet.get())
    }

    companion object {
        val channels = DefaultChannelGroup(GlobalEventExecutor.INSTANCE)

        fun sendToAll(packet: S2CPacket) {
            channels.writeAndFlush(packet.get())
        }

        fun sendToAllExcept(packet: S2CPacket, id: Int) {
            for (player in minecraftServer.playerManager.players.values) {
                if (player.id != id) {
                    player.connection.send(packet)
                }
            }
        }
    }
}