package org.rsmod.api.net.rsprot.player

import com.github.michaelbull.logging.InlineLogger
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import org.rsmod.api.account.character.social.SocialRepository
import org.rsmod.api.db.Database
import org.rsmod.api.server.config.ServerConfig
import org.rsmod.game.entity.Player
import org.rsmod.game.entity.PlayerList

public class SocialPresenceService
@Inject
constructor(
    private val database: Database,
    private val socialRepository: SocialRepository,
    private val playerList: PlayerList,
    private val config: ServerConfig,
) {
    public fun notifyOnline(player: Player) {
        notifyPresence(player = player, isOnline = true)
    }

    public fun notifyOffline(player: Player) {
        notifyPresence(player = player, isOnline = false)
    }

    private fun notifyPresence(player: Player, isOnline: Boolean) {
        val watcherIds =
            runCatching {
                    runBlocking {
                        database.withTransaction { connection ->
                            socialRepository.getAccountsThatHaveFriend(connection, player.accountId)
                        }
                    }
                }
                .onFailure { cause ->
                    logger.error(cause) {
                        "Social presence lookup failed for accountId=${player.accountId}"
                    }
                }
                .getOrElse {
                    return
                }

        if (watcherIds.isEmpty()) {
            return
        }
        val watcherSet = watcherIds.toSet()
        val packet = singleFriendPresencePacket(player.displayName, isOnline, config.world)
        var sent = 0
        for (onlinePlayer in playerList) {
            if (onlinePlayer.accountId !in watcherSet) {
                continue
            }
            onlinePlayer.client.write(packet)
            sent++
        }
        logger.info {
            "SocialPresenceService: notified ${if (isOnline) "online" else "offline"} presence " +
                "for accountId=${player.accountId} displayName=${player.displayName} recipients=$sent"
        }
    }
}

private val logger = InlineLogger()
