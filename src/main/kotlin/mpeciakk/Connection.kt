package mpeciakk

import io.netty.channel.ChannelHandlerContext

class Connection(private val ctx: ChannelHandlerContext) {

    fun write(packet: S2CPacket) {
        ctx.writeAndFlush(packet.get())
    }
}