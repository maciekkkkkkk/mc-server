package mpeciakk.manager

import mpeciakk.entity.Player
import mpeciakk.math.Vector3d
import mpeciakk.network.Connection
import mpeciakk.network.SocketState
import mpeciakk.network.packet.s2c.*
import mpeciakk.text.TextComponent
import mpeciakk.world.Chunk
import mpeciakk.world.ChunkSection
import java.util.*

class PlayerManager {
    val players = mutableMapOf<UUID, Player>()

    fun connectPlayer(connection: Connection, nick: String) {
        val player = Player(nick, connection)
        players[player.uuid] = player
        player.position.x = 1.0
        player.position.y = 20.0
        player.position.z = 1.0

        connection.send(S2CLoginSuccess(player.uuid, nick))
        connection.send(S2CJoinGamePacket(player.id))
//                connection.write(S2CHeldItemChangePacket())
//                connection.write(S2CEntityStatusPacket())
        connection.send(S2CPlayerPositionAndLookPacket())

        for (x in -2..8) {
            for (z in -2..8) {
                val chunk = Chunk(x, z, full = true, ignoreOldData = false)

                val section = ChunkSection()

                for (x2 in 0 until 16) {
                    for (y2 in 0 until 16) {
                        for (z2 in 0 until 16) {
                            section[x2, y2, z2] = 1
                        }
                    }
                }

                chunk.sections.add(section)
                chunk.sections.add(null)
                chunk.sections.add(null)
                chunk.sections.add(null)
                chunk.sections.add(null)
                chunk.sections.add(null)
                chunk.sections.add(null)
                chunk.sections.add(null)
                chunk.sections.add(null)
                chunk.sections.add(null)
                chunk.sections.add(null)
                chunk.sections.add(null)
                chunk.sections.add(null)
                chunk.sections.add(null)
                chunk.sections.add(null)
                chunk.sections.add(null)
                chunk.sections.add(null)
                connection.send(S2CChunkDataPacket(chunk))
            }
        }

        connection.state = SocketState.PLAY
        connection.player = player

        for (existingPlayer in players.values) {
            connection.send(S2CPlayerInfoPacket(existingPlayer.uuid, existingPlayer.nick))

            if (existingPlayer.id != player.id) {
                connection.send(existingPlayer.getSpawnPacket())
            }
        }

        Connection.sendToAll(
            S2CPlayerInfoPacket(player.uuid, player.nick)
        )

        Connection.sendToAllExcept(
            player.getSpawnPacket(),
            player.id
        )
    }

    fun chatMessage(text: String) {
        val component = TextComponent()
        component.text = text

        players.forEach { (_, player) ->
            player.sendMessage(component)
        }
    }
}