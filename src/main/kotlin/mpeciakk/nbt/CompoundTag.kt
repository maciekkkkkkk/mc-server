package mpeciakk.nbt

import java.io.DataOutput

class CompoundTag() : Tag {

    private val tags: MutableMap<String, Tag> = HashMap()

    override fun write(output: DataOutput) {
        for (entry in tags.entries) {
            write(entry.key, entry.value, output)
        }

        output.writeByte(0)
    }

    private fun write(key: String, tag: Tag, output: DataOutput) {
        output.writeByte(tag.getType().toInt())

        if (tag.getType() != 0.toByte()) {
            output.writeUTF(key)
            tag.write(output)
        }
    }

    fun isEmpty(): Boolean {
        return tags.isEmpty()
    }

    fun remove(key: String?) {
        tags.remove(key)
    }

    fun getType(key: String): Byte {
        return tags[key]?.getType() ?: 0
    }

    fun contains(key: String, type: Int): Boolean {
        val i = getType(key).toInt()
        return when {
            i == type -> {
                true
            }
            type != 99 -> {
                false
            }
            else -> {
                i == 1 || i == 2 || i == 3 || i == 4 || i == 5 || i == 6
            }
        }
    }

    fun getByte(key: String): Byte {
        if (contains(key, 99)) {
            return (tags[key] as ByteTag).get()
        }

        return 0
    }

    fun putByte(key: String, value: Byte) {
        tags[key] = ByteTag(value)
    }

    fun getShort(key: String): Short {
        if (contains(key, 99)) {
            return (tags[key] as ShortTag).get()
        }

        return 0
    }

    fun putShort(key: String, value: Short) {
        tags[key] = ShortTag(value)
    }

    fun getInt(key: String): Int {
        if (contains(key, 99)) {
            return (tags[key] as IntTag).get()
        }

        return 0
    }

    fun putInt(key: String, value: Int) {
        tags[key] = IntTag(value)
    }

    fun getLong(key: String): Long {
        if (contains(key, 99)) {
            return (tags[key] as LongTag).get()
        }

        return 0
    }

    fun putLong(key: String, value: Long) {
        tags[key] = LongTag(value)
    }

    fun getFloat(key: String): Float {
        if (contains(key, 99)) {
            return (tags[key] as FloatTag).get()
        }

        return 0.0f
    }

    fun putFloat(key: String, value: Float) {
        tags[key] = FloatTag(value)
    }

    fun getDouble(key: String): Double {
        if (contains(key, 99)) {
            return (tags[key] as DoubleTag).get()
        }

        return 0.0
    }

    fun putDouble(key: String, value: Double) {
        tags[key] = DoubleTag(value)
    }

    fun getBoolean(key: String): Boolean {
        return getByte(key).toInt() != 0
    }

    fun putBoolean(key: String, value: Boolean) {
        putByte(key, if (value) 1 else 0)
    }

    fun getString(key: String): String {
        if (contains(key, 8)) {
            return (tags[key] as StringTag).getValue()
        }

        return ""
    }

    fun putString(key: String, value: String) {
        tags[key] = StringTag(value)
    }

    override fun getType(): Byte {
        return 10
    }

    fun put(key: String, value: Tag) {
        tags[key] = value
    }

    override fun toString(): String {
        return "CompoundTag(tags=$tags)"
    }
}