package mpeciakk.network.packet

class ItemData(val present: Boolean, val id: Int?, val count: Byte?) {
    override fun toString(): String {
        return "ItemData(present=$present, id=$id, count=$count)"
    }
}