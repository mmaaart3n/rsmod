package org.rsmod.api.net.rsprot.player

import java.nio.file.Files
import java.nio.file.Path
import java.sql.Statement
import net.rsprot.protocol.game.outgoing.misc.player.MessageGame
import net.rsprot.protocol.game.outgoing.social.MessagePrivate
import net.rsprot.protocol.game.outgoing.social.MessagePrivateEcho
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.rsmod.api.account.character.social.SocialRepository
import org.rsmod.api.db.DatabaseConfig
import org.rsmod.api.db.DatabaseConnection
import org.rsmod.api.db.migration.FlywayMigration
import org.rsmod.api.db.sqlite.SqliteConnection
import org.rsmod.api.db.sqlite.SqliteDatabase
import org.rsmod.api.server.config.ServerConfig
import org.rsmod.game.client.Client
import org.rsmod.game.entity.Player
import org.rsmod.game.entity.PlayerList
import org.rsmod.game.type.mod.UnpackedModLevelType

class PrivateMessageServiceTest {
    private lateinit var dbPath: Path
    private lateinit var sqlConnection: java.sql.Connection
    private lateinit var sqliteDatabase: SqliteDatabase
    private lateinit var directConnection: DatabaseConnection
    private lateinit var playerList: PlayerList
    private lateinit var service: PrivateMessageService
    private val repository = SocialRepository()

    @BeforeEach
    fun setUp() {
        dbPath = Files.createTempFile("rsmod-pm-", ".db")
        val config = DatabaseConfig("jdbc:sqlite:", dbPath.toString(), null, null)
        FlywayMigration(config).migrate()
        sqlConnection = SqliteConnection(config).connect()
        directConnection = DatabaseConnection(sqlConnection)

        sqliteDatabase = SqliteDatabase()
        sqliteDatabase.connect(SqliteConnection(config))
        playerList = PlayerList()
        service =
            PrivateMessageService(
                database = sqliteDatabase,
                socialRepository = repository,
                playerList = playerList,
                config = ServerConfig(realm = "dev", world = 1, firstLaunch = false),
            )
    }

    @AfterEach
    fun tearDown() {
        if (::sqlConnection.isInitialized && !sqlConnection.isClosed) {
            sqlConnection.close()
        }
        if (::sqliteDatabase.isInitialized) {
            sqliteDatabase.close()
        }
        Files.deleteIfExists(dbPath)
    }

    @Test
    fun `pm to online player delivers to recipient and echo to sender`() {
        val selfId = insertAccount("self", "Self")
        val targetId = insertAccount("alt", "Alt")
        val (sender, senderClient) = createPlayer(selfId, "Self")
        val (target, targetClient) = createPlayer(targetId, "Alt")
        registerOnline(sender, 1)
        registerOnline(target, 2)

        service.sendPrivateMessage(sender, "Alt", "  hello  ")

        val toRecipient = targetClient.single<MessagePrivate>()
        assertEquals("Self", toRecipient.sender)
        assertEquals(1, toRecipient.worldId)
        assertEquals("hello", toRecipient.message)
        val echo = senderClient.single<MessagePrivateEcho>()
        assertEquals("Alt", echo.recipient)
        assertEquals("hello", echo.message)
    }

    @Test
    fun `pm to offline target sends game message`() {
        val selfId = insertAccount("self", "Self")
        val targetId = insertAccount("alt", "Alt")
        val (sender, senderClient) = createPlayer(selfId, "Self")
        registerOnline(sender, 1)

        service.sendPrivateMessage(sender, "Alt", "hi")

        assertTrue(senderClient.anyMessageContaining("not online"))
        assertFalse(senderClient.any<MessagePrivate>())
        assertFalse(senderClient.any<MessagePrivateEcho>())
    }

    @Test
    fun `pm to unknown name fails`() {
        val selfId = insertAccount("self", "Self")
        val (sender, senderClient) = createPlayer(selfId, "Self")
        registerOnline(sender, 1)

        service.sendPrivateMessage(sender, "nobody_here", "hi")

        assertTrue(senderClient.anyMessageContaining("does not exist"))
    }

    @Test
    fun `pm blocked when recipient ignores sender`() {
        val selfId = insertAccount("self", "Self")
        val targetId = insertAccount("alt", "Alt")
        repository.addIgnore(directConnection, targetId, selfId)
        sqlConnection.commit()
        val (sender, senderClient) = createPlayer(selfId, "Self")
        val (target, targetClient) = createPlayer(targetId, "Alt")
        registerOnline(sender, 1)
        registerOnline(target, 2)

        service.sendPrivateMessage(sender, "Alt", "hi")

        assertTrue(senderClient.anyMessageContaining("cannot message that player"))
        assertTrue(targetClient.messages.none { it is MessagePrivate })
    }

    @Test
    fun `pm blocked when sender ignores recipient`() {
        val selfId = insertAccount("self", "Self")
        val targetId = insertAccount("alt", "Alt")
        repository.addIgnore(directConnection, selfId, targetId)
        sqlConnection.commit()
        val (sender, senderClient) = createPlayer(selfId, "Self")
        val (target, targetClient) = createPlayer(targetId, "Alt")
        registerOnline(sender, 1)
        registerOnline(target, 2)

        service.sendPrivateMessage(sender, "Alt", "hi")

        assertTrue(senderClient.anyMessageContaining("cannot message that player"))
        assertTrue(targetClient.messages.none { it is MessagePrivate })
    }

    @Test
    fun `empty message after trim is blocked`() {
        val selfId = insertAccount("self", "Self")
        insertAccount("alt", "Alt")
        val (sender, senderClient) = createPlayer(selfId, "Self")
        registerOnline(sender, 1)

        service.sendPrivateMessage(sender, "Alt", "   ")

        assertTrue(senderClient.anyMessageContaining("empty"))
    }

    @Test
    fun `message too long is blocked`() {
        val selfId = insertAccount("self", "Self")
        insertAccount("alt", "Alt")
        val (sender, senderClient) = createPlayer(selfId, "Self")
        registerOnline(sender, 1)

        service.sendPrivateMessage(sender, "Alt", "x".repeat(MAX_PRIVATE_MESSAGE_LENGTH + 1))

        assertTrue(senderClient.anyMessageContaining("too long"))
    }

    private fun registerOnline(player: Player, slot: Int) {
        player.slotId = slot
        playerList[slot] = player
    }

    private fun createPlayer(accountId: Int, displayName: String): Pair<Player, RecordingClient> {
        val client = RecordingClient()
        val player =
            Player(client = client).apply {
                this.accountId = accountId
                this.displayName = displayName
                this.modLevel =
                    UnpackedModLevelType(
                        clientCode = 0,
                        accessFlags = 0,
                        internalId = 0,
                        internalName = "player",
                    )
            }
        return player to client
    }

    private fun insertAccount(username: String, displayName: String): Int {
        val insert =
            directConnection.prepareStatement(
                "INSERT INTO accounts (login_username, display_name, password_hash) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS,
            )
        insert.use {
            it.setString(1, username.lowercase())
            it.setString(2, displayName)
            it.setString(3, "hash")
            check(it.executeUpdate() == 1)
            val id =
                it.generatedKeys.use { keys ->
                    check(keys.next())
                    keys.getInt(1)
                }
            sqlConnection.commit()
            return id
        }
    }

    private class RecordingClient : Client<Any, Any> {
        val messages = mutableListOf<Any>()

        override fun close() {}

        override fun write(message: Any) {
            messages += message
        }

        override fun read(player: Player) {}

        override fun flush() {}

        override fun flushHighPriority() {}

        override fun unregister(service: Any, player: Player) {}

        inline fun <reified T> single(): T = messages.filterIsInstance<T>().single()

        inline fun <reified T> any(predicate: (T) -> Boolean = { true }): Boolean =
            messages.filterIsInstance<T>().any(predicate)

        fun anyMessageContaining(substring: String): Boolean =
            messages.filterIsInstance<MessageGame>().any {
                it.message.contains(substring, ignoreCase = true)
            }
    }
}
