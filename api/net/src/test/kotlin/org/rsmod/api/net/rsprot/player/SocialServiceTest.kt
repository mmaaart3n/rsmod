package org.rsmod.api.net.rsprot.player

import java.nio.file.Files
import java.nio.file.Path
import java.sql.Statement
import net.rsprot.protocol.game.outgoing.misc.player.MessageGame
import net.rsprot.protocol.game.outgoing.social.UpdateFriendList
import net.rsprot.protocol.game.outgoing.social.UpdateIgnoreList
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

class SocialServiceTest {
    private lateinit var dbPath: Path
    private lateinit var sqlConnection: java.sql.Connection
    private lateinit var sqliteDatabase: SqliteDatabase
    private lateinit var directConnection: DatabaseConnection
    private lateinit var playerList: PlayerList
    private lateinit var service: SocialService
    private val repository = SocialRepository()

    @BeforeEach
    fun setUp() {
        dbPath = Files.createTempFile("rsmod-social-service-", ".db")
        val config = DatabaseConfig("jdbc:sqlite:", dbPath.toString(), null, null)
        FlywayMigration(config).migrate()
        sqlConnection = SqliteConnection(config).connect()
        directConnection = DatabaseConnection(sqlConnection)

        sqliteDatabase = SqliteDatabase()
        sqliteDatabase.connect(SqliteConnection(config))
        playerList = PlayerList()
        service =
            SocialService(
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
    fun `add known friend writes repository and sends add update packet`() {
        val selfId = insertAccount("self", "Self")
        val targetId = insertAccount("alt", "Alt")
        val (player, client) = createPlayer(selfId, "Self")

        service.addFriend(player, "Alt")

        sqlConnection.commit()
        assertTrue(repository.isFriend(directConnection, selfId, targetId))
        val update = client.single<UpdateFriendList>()
        val entry = update.friends.single()
        assertTrue(entry.added)
        assertEquals("Alt", entry.name)
    }

    @Test
    fun `add known online friend sends online add update packet`() {
        val selfId = insertAccount("self", "Self")
        val targetId = insertAccount("alt", "Alt")
        val (player, client) = createPlayer(selfId, "Self")
        val (targetPlayer, _) = createPlayer(targetId, "Alt")
        targetPlayer.slotId = 1
        playerList[1] = targetPlayer

        service.addFriend(player, "Alt")

        val update = client.single<UpdateFriendList>()
        val entry = update.friends.single()
        assertTrue(entry is UpdateFriendList.OnlineFriend)
        assertEquals("Alt", entry.name)
        assertTrue(entry.added)
        assertEquals(1, entry.worldId)
    }

    @Test
    fun `add duplicate friend is safe and sends game message`() {
        val selfId = insertAccount("self", "Self")
        val targetId = insertAccount("alt", "Alt")
        val (player, client) = createPlayer(selfId, "Self")
        repository.addFriend(directConnection, selfId, targetId)
        sqlConnection.commit()

        service.addFriend(player, "Alt")

        assertEquals(1, repository.getFriends(directConnection, selfId).size)
        assertTrue(client.any<MessageGame> { it.message.contains("already on your friends list") })
    }

    @Test
    fun `add self friend is blocked with message`() {
        val selfId = insertAccount("self", "Self")
        val (player, client) = createPlayer(selfId, "Self")

        service.addFriend(player, "Self")

        assertFalse(client.any<UpdateFriendList>())
        assertTrue(
            client.any<MessageGame> { it.message.contains("cannot add yourself as a friend") }
        )
    }

    @Test
    fun `add unknown friend is blocked with message`() {
        val selfId = insertAccount("self", "Self")
        val (player, client) = createPlayer(selfId, "Self")

        service.addFriend(player, "unknown")

        assertTrue(client.any<MessageGame> { it.message.contains("does not exist") })
        assertFalse(client.any<UpdateFriendList>())
    }

    @Test
    fun `remove existing friend sends remove update packet`() {
        val selfId = insertAccount("self", "Self")
        val targetId = insertAccount("alt", "Alt")
        repository.addFriend(directConnection, selfId, targetId)
        sqlConnection.commit()
        val (player, client) = createPlayer(selfId, "Self")

        service.removeFriend(player, "Alt")

        assertFalse(repository.isFriend(directConnection, selfId, targetId))
        val update = client.single<UpdateFriendList>()
        val entry = update.friends.single()
        assertFalse(entry.added)
        assertEquals("Alt", entry.name)
    }

    @Test
    fun `add known ignore writes repository and sends add update packet`() {
        val selfId = insertAccount("self", "Self")
        val targetId = insertAccount("alt", "Alt")
        val (player, client) = createPlayer(selfId, "Self")

        service.addIgnore(player, "Alt")

        sqlConnection.commit()
        assertTrue(repository.isIgnored(directConnection, selfId, targetId))
        val update = client.single<UpdateIgnoreList>()
        assertEquals("Alt", update.ignores.single().name)
    }

    @Test
    fun `add self ignore is blocked`() {
        val selfId = insertAccount("self", "Self")
        val (player, client) = createPlayer(selfId, "Self")

        service.addIgnore(player, "Self")

        assertTrue(
            client.any<MessageGame> {
                it.message.contains("cannot add yourself to your ignore list")
            }
        )
        assertFalse(client.any<UpdateIgnoreList>())
    }

    @Test
    fun `add duplicate ignore is blocked with message`() {
        val selfId = insertAccount("self", "Self")
        val targetId = insertAccount("alt", "Alt")
        val (player, client) = createPlayer(selfId, "Self")
        repository.addIgnore(directConnection, selfId, targetId)
        sqlConnection.commit()

        service.addIgnore(player, "Alt")

        assertEquals(1, repository.getIgnores(directConnection, selfId).size)
        assertTrue(client.any<MessageGame> { it.message.contains("already on your ignore list") })
        assertFalse(client.any<UpdateIgnoreList>())
    }

    @Test
    fun `remove missing ignore sends clear message and no packet`() {
        val selfId = insertAccount("self", "Self")
        insertAccount("alt", "Alt")
        val (player, client) = createPlayer(selfId, "Self")

        service.removeIgnore(player, "Alt")

        assertTrue(client.any<MessageGame> { it.message.contains("not on your ignore list") })
        assertFalse(client.any<UpdateIgnoreList>())
    }

    @Test
    fun `remove ignore sends remove update packet`() {
        val selfId = insertAccount("self", "Self")
        val targetId = insertAccount("alt", "Alt")
        repository.addIgnore(directConnection, selfId, targetId)
        sqlConnection.commit()
        val (player, client) = createPlayer(selfId, "Self")

        service.removeIgnore(player, "Alt")

        assertFalse(repository.isIgnored(directConnection, selfId, targetId))
        val update = client.single<UpdateIgnoreList>()
        assertEquals("Alt", update.ignores.single().name)
    }

    private fun createPlayer(accountId: Int, displayName: String): Pair<Player, RecordingClient> {
        val client = RecordingClient()
        val player =
            Player(client = client).apply {
                this.accountId = accountId
                this.displayName = displayName
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
        private val messages = mutableListOf<Any>()

        override fun close() {}

        override fun write(message: Any) {
            messages += message
        }

        override fun read(player: Player) {}

        override fun flush() {}

        override fun flushHighPriority() {}

        override fun unregister(service: Any, player: Player) {}

        inline fun <reified T> any(predicate: (T) -> Boolean = { true }): Boolean =
            messages.filterIsInstance<T>().any(predicate)

        inline fun <reified T> single(): T = messages.filterIsInstance<T>().single()
    }
}
