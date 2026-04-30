package org.rsmod.api.net.rsprot.player

import com.github.michaelbull.logging.InlineLogger
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import org.rsmod.api.account.character.social.SocialAccount
import org.rsmod.api.account.character.social.SocialRepository
import org.rsmod.api.db.Database
import org.rsmod.api.player.output.mes
import org.rsmod.api.server.config.ServerConfig
import org.rsmod.game.entity.Player
import org.rsmod.game.entity.PlayerList

public class SocialService
@Inject
constructor(
    private val database: Database,
    private val socialRepository: SocialRepository,
    private val playerList: PlayerList,
    private val config: ServerConfig,
) {
    public fun addFriend(player: Player, rawName: String) {
        val normalized = normalizeSearchName(rawName)
        if (normalized.isEmpty()) {
            return
        }
        val account = resolveAccount(player, normalized) ?: return
        if (account.accountId == player.accountId) {
            player.mes("You cannot add yourself as a friend.")
            return
        }
        runCatching {
                val inserted = runBlocking {
                    database.withTransaction { connection ->
                        socialRepository.addFriend(connection, player.accountId, account.accountId)
                    }
                }
                if (!inserted) {
                    player.mes("That player is already on your friends list.")
                    return
                }
                player.client.write(
                    singleFriendAddPacket(
                        account.displayName,
                        isOnline = isOnline(account.accountId),
                        worldId = config.world,
                    )
                )
                player.mes("Added ${account.displayName} to your friends list.")
                logger.debug {
                    "Added friend: playerAccountId=${player.accountId}, target=${account.accountId}"
                }
            }
            .onFailure { cause ->
                logger.error(cause) { "Failed to add friend for accountId=${player.accountId}" }
                player.mes("Unable to add that friend right now. Please try again.")
            }
    }

    public fun removeFriend(player: Player, rawName: String) {
        val normalized = normalizeSearchName(rawName)
        if (normalized.isEmpty()) {
            return
        }
        val account = resolveAccount(player, normalized) ?: return
        runCatching {
                val removed = runBlocking {
                    database.withTransaction { connection ->
                        socialRepository.removeFriend(
                            connection,
                            player.accountId,
                            account.accountId,
                        )
                    }
                }
                if (!removed) {
                    player.mes("That player is not on your friends list.")
                    return
                }
                player.client.write(singleFriendRemovePacket(account.displayName))
                player.mes("Removed ${account.displayName} from your friends list.")
                logger.debug {
                    "Removed friend: playerAccountId=${player.accountId}, target=${account.accountId}"
                }
            }
            .onFailure { cause ->
                logger.error(cause) { "Failed to remove friend for accountId=${player.accountId}" }
                player.mes("Unable to remove that friend right now. Please try again.")
            }
    }

    public fun addIgnore(player: Player, rawName: String) {
        val normalized = normalizeSearchName(rawName)
        if (normalized.isEmpty()) {
            return
        }
        val account = resolveAccount(player, normalized) ?: return
        if (account.accountId == player.accountId) {
            player.mes("You cannot add yourself to your ignore list.")
            return
        }
        runCatching {
                val inserted = runBlocking {
                    database.withTransaction { connection ->
                        socialRepository.addIgnore(connection, player.accountId, account.accountId)
                    }
                }
                if (!inserted) {
                    player.mes("That player is already on your ignore list.")
                    return
                }
                player.client.write(singleIgnoreAddPacket(account.displayName))
                player.mes("Added ${account.displayName} to your ignore list.")
                logger.debug {
                    "Added ignore: playerAccountId=${player.accountId}, target=${account.accountId}"
                }
            }
            .onFailure { cause ->
                logger.error(cause) { "Failed to add ignore for accountId=${player.accountId}" }
                player.mes("Unable to add that ignore right now. Please try again.")
            }
    }

    public fun removeIgnore(player: Player, rawName: String) {
        val normalized = normalizeSearchName(rawName)
        if (normalized.isEmpty()) {
            return
        }
        val account = resolveAccount(player, normalized) ?: return
        runCatching {
                val removed = runBlocking {
                    database.withTransaction { connection ->
                        socialRepository.removeIgnore(
                            connection,
                            player.accountId,
                            account.accountId,
                        )
                    }
                }
                if (!removed) {
                    player.mes("That player is not on your ignore list.")
                    return
                }
                player.client.write(singleIgnoreRemovePacket(account.displayName))
                player.mes("Removed ${account.displayName} from your ignore list.")
                logger.debug {
                    "Removed ignore: playerAccountId=${player.accountId}, target=${account.accountId}"
                }
            }
            .onFailure { cause ->
                logger.error(cause) { "Failed to remove ignore for accountId=${player.accountId}" }
                player.mes("Unable to remove that ignore right now. Please try again.")
            }
    }

    private fun resolveAccount(player: Player, normalizedName: String): SocialAccount? {
        val account = runBlocking {
            database.withTransaction { connection ->
                socialRepository.findAccountByName(connection, normalizedName)
            }
        }
        if (account != null) {
            return account
        }
        player.mes("That player does not exist.")
        return null
    }

    private fun isOnline(targetAccountId: Int): Boolean =
        playerList.any { it.accountId == targetAccountId }
}

internal fun normalizeSearchName(input: String): String =
    input.trim().split(Regex(" +")).joinToString(" ")

private val logger = InlineLogger()
