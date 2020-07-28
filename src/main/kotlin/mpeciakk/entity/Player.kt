package mpeciakk.entity

import com.google.gson.*
import mpeciakk.inventory.Inventory
import mpeciakk.network.Connection
import mpeciakk.network.packet.s2c.*
import mpeciakk.text.TextComponent
import java.lang.reflect.Type

class Player(val nick: String, val connection: Connection) : Entity() {
    val inventory = Inventory()

    fun sendMessage(component: TextComponent) {
        connection.send(
            S2CChatMessagePacket(
                GsonBuilder().registerTypeHierarchyAdapter(
                    Collection::class.java,
                    CollectionAdapter()
                ).create().toJson(component), 0
            )
        )
    }

    override fun tick() {
        if (position != prevPosition) {
            Connection.sendToAllExcept(
                S2CEntityPositionPacket(
                    id, ((position.x * 32 - prevPosition.x * 32) * 128).toInt(),
                    ((position.y * 32 - prevPosition.y * 32) * 128).toInt(),
                    ((position.z * 32 - prevPosition.z * 32) * 128).toInt(), onGround
                ),
                id
            )
        }

        Connection.sendToAllExcept(
            S2CEntityRotationPacket(
                id, yaw, pitch, onGround
            ),
            id
        )
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

    override fun getSpawnPacket(): S2CPacket {
        return S2CSpawnPlayerPacket(id, uuid, position, yaw, pitch)
    }
}