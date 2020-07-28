package mpeciakk.nbt

import java.util.*

abstract class AbstractListTag<T : Tag?> : AbstractList<T>(), Tag {
    abstract override fun set(index: Int, element: T): T
    abstract override fun add(index: Int, element: T)
    abstract fun setTag(index: Int, tag: Tag?): Boolean
    abstract fun addTag(index: Int, tag: Tag?): Boolean
    abstract val elementType: Byte
}
