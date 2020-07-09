package mpeciakk.nbt

import java.util.*

abstract class AbstractListTag<T : Tag?> : AbstractList<T>(), Tag {
    abstract override fun set(i: Int, tag: T): T
    abstract override fun add(i: Int, tag: T)
    abstract fun setTag(index: Int, tag: Tag?): Boolean
    abstract fun addTag(index: Int, tag: Tag?): Boolean
    abstract val elementType: Byte
}
