package mpeciakk.net

import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.group.ChannelGroup
import io.netty.channel.group.DefaultChannelGroup
import io.netty.util.concurrent.GlobalEventExecutor
import mpeciakk.entity.Player
import mpeciakk.minecraftServer
import mpeciakk.packet.PacketByteBuf
import mpeciakk.packet.PacketRegistry
import mpeciakk.packet.s2c.S2CPacket
import java.util.*

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
        val buf = PacketByteBuf(Unpooled.wrappedBuffer(bytes))

        val length = buf.readVarInt()
        val packetId = buf.readVarInt()

        println("Handling $packetId")

        val data = bytes.copyOfRange(2, length + 1)

        val packet = PacketRegistry.get(packetId)

        if (packet != null) {
            packet.read(this, PacketByteBuf(Unpooled.wrappedBuffer(data)))
        } else {
            throw Exception("Unhandled packet $packetId")
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
    }
}