import io.netty.buffer.Unpooled
import mpeciakk.network.packet.PacketByteBuf
import org.junit.jupiter.api.Test

class ByteBufTest {

    @Test
    fun byteBufTest() {
        val buf = PacketByteBuf(Unpooled.wrappedBuffer(byteArrayOf(4, 39, 0, 28, 0, 4)))

        buf.readVarInt()
        buf.readVarInt()
    }
}