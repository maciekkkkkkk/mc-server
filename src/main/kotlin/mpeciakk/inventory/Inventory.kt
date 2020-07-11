package mpeciakk.inventory

class Inventory : Container() {
    var selectedSlot: Short = 36
    val selectedItem: Int?
        get() {
            return items[selectedSlot]
        }
}