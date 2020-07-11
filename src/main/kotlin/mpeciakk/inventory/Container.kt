package mpeciakk.inventory

open class Container {
    protected val items = mutableMapOf<Short, Int>()

    operator fun set(slot: Short, id: Int) {
        items[slot] = id
    }

    operator fun get(slot: Short): Int? {
        return items[slot]
    }
}