package mpeciakk.nbt

interface NumberTag<T> : Tag {

    fun get(): T
}