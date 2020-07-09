package mpeciakk.manager

import mpeciakk.entity.Player
import mpeciakk.net.Connection
import mpeciakk.net.SocketState
import mpeciakk.packet.s2c.S2CChunkDataPacket
import mpeciakk.packet.s2c.S2CJoinGamePacket
import mpeciakk.packet.s2c.S2CLoginSuccess
import mpeciakk.packet.s2c.S2CPlayerPositionAndLookPacket
import mpeciakk.world.Chunk
import mpeciakk.world.ChunkSection
import java.util.*

class PlayerManager {

    val players: MutableMap<UUID, Player> = mutableMapOf()

    fun connectPlayer(connection: Connection, nick: String) {
        val player = Player(nick, connection)
        players[player.uuid] = player

        connection.send(S2CLoginSuccess(player.uuid, nick))
        connection.send(S2CJoinGamePacket())
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
    }

    fun getPlayer(uuid: UUID): Player? {
        return players[uuid]
    }
}