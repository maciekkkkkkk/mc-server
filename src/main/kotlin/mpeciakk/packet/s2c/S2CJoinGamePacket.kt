package mpeciakk.packet.s2c

import mpeciakk.nbt.CompoundTag
import mpeciakk.nbt.ListTag
import mpeciakk.packet.PacketByteBuf

class S2CJoinGamePacket : S2CPacket(0x25) {
    override fun write(buf: PacketByteBuf) {
        buf.writeInt(0)
        buf.writeByte(1)
        buf.writeByte(0)
        buf.writeVarInt(1)
        buf.writeString("minecraft:overworld")

        val root = CompoundTag()
        val list = ListTag()

        val dimension = CompoundTag()

        dimension.putString("name", "minecraft:overworld")
        dimension.putByte("natural", 1)
        dimension.putFloat("ambient_light", 0.5f)
        dimension.putByte("has_ceiling", 0)
        dimension.putByte("has_skylight", 1)
        dimension.putLong("fixed_time", 0)
        dimension.putByte("shrunk", 0)
        dimension.putByte("ultrawarm", 0)
        dimension.putByte("has_raids", 0)
        dimension.putByte("respawn_anchor_works", 0)
        dimension.putByte("bed_works", 0)
        dimension.putByte("piglin_safe", 0)
        dimension.putInt("logical_height", 256)
        dimension.putString("infiniburn", "")

        list.addTag(0, dimension)
        root.put("dimension", list)

        buf.writeTag(root)
        buf.writeString("minecraft:overworld")
        buf.writeString("minecraft:overworld")
        buf.writeLong(0)
        buf.writeByte(11)
        buf.writeVarInt(5)
        buf.writeBoolean(false)
        buf.writeBoolean(true)
        buf.writeBoolean(false)
        buf.writeBoolean(false)
    }
}