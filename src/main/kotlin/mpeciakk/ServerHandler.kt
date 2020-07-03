package mpeciakk

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

class ServerHandler : ChannelInboundHandlerAdapter() {

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val buf = PacketByteBuf(Unpooled.wrappedBuffer((msg as ByteArray)))

        val length = buf.readVarInt()
        val packetId = buf.readVarInt()

        println(packetId)

        PacketRegistry.get(packetId)!!.handle(Connection(ctx), buf);
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        ctx.flush()
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.close()
    }
}