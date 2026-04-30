package org.rsmod.api.net.rsprot.player

import com.github.michaelbull.logging.InlineLogger
import jakarta.inject.Inject
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.runBlocking
import net.rsprot.protocol.game.outgoing.social.MessagePrivate
import net.rsprot.protocol.game.outgoing.social.MessagePrivateEcho
import org.rsmod.api.account.character.social.SocialRepository
import org.rsmod.api.db.Database
import org.rsmod.api.player.output.mes
import org.rsmod.api.server.config.ServerConfig
import org.rsmod.game.entity.Player
import org.rsmod.game.entity.PlayerList

/** Maximum UTF-16 code units for a private message body (single-world MVP). */
internal const val MAX_PRIVATE_MESSAGE_LENGTH = 80

public class PrivateMessageService
@Inject
constructor(
    private val database: Database,
    private val socialRepository: SocialRepository,
    private val playerList: PlayerList,
    private val config: ServerConfig,
) {
    private val worldMessageCounter = AtomicInteger(1)

    public fun sendPrivateMessage(sender: Player, targetNameRaw: String, messageRaw: String) {
        val normalizedTarget = normalizeSearchName(targetNameRaw)
        if (normalizedTarget.isEmpty()) {
            sender.mes("Enter a valid player name.")
            return
        }

        val trimmed = sanitizePrivateMessage(messageRaw)
        if (trimmed.isEmpty()) {
            sender.mes("Your message is empty.")
            return
        }
        if (trimmed.length > MAX_PRIVATE_MESSAGE_LENGTH) {
            sender.mes("That message is too long.")
            return
        }

        val targetAccount =
            runCatching {
                    runBlocking {
                        database.withTransaction { connection ->
                            socialRepository.findAccountByName(connection, normalizedTarget)
                        }
                    }
                }
                .getOrElse { cause ->
                    logger.error(cause) {
                        "Private message lookup failed for senderAccountId=${sender.accountId}"
                    }
                    sender.mes("Unable to send that message right now. Please try again.")
                    return
                }

        if (targetAccount == null) {
            sender.mes("That player does not exist.")
            return
        }

        if (targetAccount.accountId == sender.accountId) {
            sender.mes("You cannot message yourself.")
            return
        }

        val blocked =
            runCatching {
                    runBlocking {
                        database.withTransaction { connection ->
                            // Recipient ignores sender
                            socialRepository.isIgnored(
                                connection,
                                targetAccount.accountId,
                                sender.accountId,
                            ) ||
                                // Sender ignores recipient (symmetric block)
                                socialRepository.isIgnored(
                                    connection,
                                    sender.accountId,
                                    targetAccount.accountId,
                                )
                        }
                    }
                }
                .getOrElse { cause ->
                    logger.error(cause) {
                        "Private message ignore check failed for senderAccountId=${sender.accountId}"
                    }
                    sender.mes("Unable to send that message right now. Please try again.")
                    return
                }

        if (blocked) {
            sender.mes("You cannot message that player.")
            return
        }

        val recipient = findOnlinePlayer(targetAccount.accountId)
        if (recipient == null) {
            sender.mes("That player is not online.")
            return
        }

        val counter = worldMessageCounter.getAndIncrement() and 0xFFFFFF
        val crown = sender.modLevel.clientCode

        recipient.client.write(
            MessagePrivate(sender.displayName, config.world, counter, crown, trimmed)
        )
        sender.client.write(MessagePrivateEcho(targetAccount.displayName, trimmed))

        logger.info {
            "Private message sent: from=${sender.displayName} to=${targetAccount.displayName} length=${trimmed.length}"
        }
    }

    private fun findOnlinePlayer(accountId: Int): Player? {
        for (p in playerList) {
            if (p.accountId == accountId) {
                return p
            }
        }
        return null
    }
}

/** Removes leading/trailing whitespace; collapses internal newlines to spaces. */
private fun sanitizePrivateMessage(raw: String): String =
    raw.trim().replace("\r\n", " ").replace("\n", " ").replace("\r", " ")

private val logger = InlineLogger()
