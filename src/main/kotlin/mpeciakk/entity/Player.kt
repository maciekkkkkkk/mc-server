package mpeciakk.entity

import com.google.gson.*
import mpeciakk.net.Connection
import mpeciakk.packet.s2c.S2CChatMessagePacket
import mpeciakk.text.TextComponent
import java.lang.reflect.Type

class Player(val nick: String, val connection: Connection) : Entity() {
    fun sendMessage(component: TextComponent) {
        connection.send(S2CChatMessagePacket(GsonBuilder().registerTypeHierarchyAdapter(Collection::class.java, CollectionAdapter()).create().toJson(component), 0))
    }

    internal class CollectionAdapter : JsonSerializer<Collection<*>?> {
        override fun serialize(
            src: Collection<*>?, typeOfSrc: Type?,
            context: JsonSerializationContext
        ): JsonElement? {
            if (src == null || src.isEmpty()) return null
            val array = JsonArray()
            for (child in src) {
                val element = context.serialize(child)
                array.add(element)
            }
            return array
        }
    }
}