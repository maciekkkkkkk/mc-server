package mpeciakk.manager

import mpeciakk.entity.Player
import mpeciakk.minecraftServer
import mpeciakk.network.Connection
import mpeciakk.network.SocketState
import mpeciakk.network.packet.s2c.*
import mpeciakk.text.TextComponent
import java.util.*
import kotlin.concurrent.fixedRateTimer

class PlayerManager {
    val players = mutableMapOf<UUID, Player>()

    fun connectPlayer(connection: Connection, nick: String) {
        val player = Player(nick, connection)
        players[player.uuid] = player
        player.position.x = 1.0
        player.position.y = 70.0
        player.position.z = 1.0

        connection.send(S2CLoginSuccess(player.uuid, nick))
        connection.send(S2CJoinGamePacket(player.id))

        for (chunk in minecraftServer.world.chunks) {
            connection.send(S2CChunkDataPacket(chunk))
        }

        connection.send(S2CPlayerPositionAndLookPacket(player.position, player.yaw, player.pitch, 1))

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

    private fun tick() {
        for (entry in players) {
            entry.value.tick()
        }
    }

    init {
        fixedRateTimer("PLAYER MANAGER TICK", period = 1000 / 20) {
            tick()
        }
    }
}