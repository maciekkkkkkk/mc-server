package mpeciakk.nbt

import java.io.DataOutput
import java.util.*

class ListTag : AbstractList<Tag>(), Tag {

    private val value = mutableListOf<Tag>()
    private var type: Byte = 0

    override fun write(output: DataOutput) {
        type = if (value.isEmpty()) {
            0
        } else {
            value[0].getType()
        }

        output.writeByte(type.toInt())

//        output.writeUTF(name)
        output.writeInt(value.size)

        for (tag in value) {
            tag.write(output)
        }
    }

    override fun getType(): Byte {
        return 9
    }

    override fun isEmpty(): Boolean {
        return value.isEmpty()
    }

    override fun get(index: Int): Tag? {
        return value[index]
    }

    override fun set(index: Int, element: Tag): Tag? {
        val tag2 = this[index]
        return if (!setTag(index, element)) {
            throw UnsupportedOperationException(
                java.lang.String.format(
                    "Trying to add tag of type %d to list of %d",
                    element.getType(),
                    type
                )
            )
        } else {
            tag2
        }
    }

    override fun add(index: Int, element: Tag) {
        if (!addTag(index, element)) {
            throw UnsupportedOperationException(
                java.lang.String.format(
                    "Trying to add tag of type %d to list of %d",
                    element.getType(),
                    type
                )
            )
        }
    }

    fun setTag(index: Int, tag: Tag): Boolean {
        return if (canAdd(tag)) {
            value[index] = tag
            true
        } else {
            false
        }
    }

    fun addTag(index: Int, tag: Tag): Boolean {
        return if (canAdd(tag)) {
            value.add(index, tag)
            true
        } else {
            false
        }
    }

    private fun canAdd(tag: Tag): Boolean {
        return when {
            tag.getType().toInt() == 0 -> {
                false
            }
            type.toInt() == 0 -> {
                type = tag.getType()
                true
            }
            else -> {
                type == tag.getType()
            }
        }
    }

    override fun toString(): String {
        return "ListTag(value=$value, type=$type)"
    }

    override val size: Int
        get() = value.size


}