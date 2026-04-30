package org.rsmod.api.net.rsprot.player

import com.github.michaelbull.logging.InlineLogger
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import net.rsprot.protocol.game.outgoing.social.FriendListLoaded
import net.rsprot.protocol.message.OutgoingGameMessage
import org.rsmod.api.account.character.social.SocialDisplayEntry
import org.rsmod.api.account.character.social.SocialRepository
import org.rsmod.api.db.Database
import org.rsmod.api.server.config.ServerConfig
import org.rsmod.game.entity.Player
import org.rsmod.game.entity.PlayerList

public class SocialLoginSync
@Inject
constructor(
    private val database: Database,
    private val socialRepository: SocialRepository,
    private val playerList: PlayerList,
    private val config: ServerConfig,
) {
    public fun sync(player: Player) {
        val accountId = player.accountId
        runCatching {
                val result = runBlocking {
                    database.withTransaction { connection ->
                        val friends =
                            socialRepository.getFriendDisplayEntries(connection, accountId)
                        val ignores =
                            socialRepository.getIgnoreDisplayEntries(connection, accountId)
                        SocialListState(friends = friends, ignores = ignores)
                    }
                }
                val packets =
                    initialSocialPackets(result.friends, result.ignores, ::isOnline, config.world)
                for (packet in packets) {
                    player.client.write(packet)
                }
            }
            .onFailure { cause ->
                logger.error(cause) {
                    "Social login sync failed for accountId=$accountId; continuing without social sync."
                }
            }
    }

    private fun isOnline(targetAccountId: Int): Boolean =
        playerList.any { it.accountId == targetAccountId }
}

internal fun initialSocialPackets(
    friends: List<SocialDisplayEntry>,
    ignores: List<SocialDisplayEntry>,
    isOnline: (Int) -> Boolean,
    worldId: Int,
): List<OutgoingGameMessage> {
    val packets = mutableListOf<OutgoingGameMessage>()
    if (friends.isEmpty()) {
        // Runtime capture proved rev233 clients can remain in "Loading friends list"
        // unless FriendListLoaded is paired with an explicit (possibly empty) friend state.
        packets += FriendListLoaded
        packets += toFriendListPacket(emptyList(), isOnline, worldId)
    } else {
        packets += toFriendListPacket(friends, isOnline, worldId)
    }
    // Runtime capture for rev233 ignore-tab behavior also requires sending ignore state on login,
    // including an empty list.
    packets += toIgnoreListPacket(ignores)
    return packets
}

private data class SocialListState(
    val friends: List<SocialDisplayEntry>,
    val ignores: List<SocialDisplayEntry>,
)

private val logger = InlineLogger()
