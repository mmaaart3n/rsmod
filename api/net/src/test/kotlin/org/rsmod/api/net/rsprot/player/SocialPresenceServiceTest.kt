package org.rsmod.api.net.rsprot.player

import java.nio.file.Files
import java.nio.file.Path
import java.sql.Statement
import net.rsprot.protocol.game.outgoing.social.UpdateFriendList
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

class SocialPresenceServiceTest {
    private lateinit var dbPath: Path
    private lateinit var sqlConnection: java.sql.Connection
    private lateinit var sqliteDatabase: SqliteDatabase
    private lateinit var directConnection: DatabaseConnection
    private lateinit var playerList: PlayerList
    private lateinit var service: SocialPresenceService
    private val repository = SocialRepository()

    @BeforeEach
    fun setUp() {
        dbPath = Files.createTempFile("rsmod-social-presence-", ".db")
        val config = DatabaseConfig("jdbc:sqlite:", dbPath.toString(), null, null)
        FlywayMigration(config).migrate()
        sqlConnection = SqliteConnection(config).connect()
        directConnection = DatabaseConnection(sqlConnection)

        sqliteDatabase = SqliteDatabase()
        sqliteDatabase.connect(SqliteConnection(config))
        playerList = PlayerList()
        service =
            SocialPresenceService(
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
    fun `notify online sends update only to players who have target as friend`() {
        val targetId = insertAccount("alt", "Alt")
        val watcherId = insertAccount("test", "Test")
        val bystanderId = insertAccount("other", "Other")
        repository.addFriend(directConnection, watcherId, targetId)
        sqlConnection.commit()

        val (target, _) = createPlayer(targetId, "Alt")
        val (watcher, watcherClient) = createPlayer(watcherId, "Test")
        val (bystander, bystanderClient) = createPlayer(bystanderId, "Other")
        addOnlinePlayer(target, slot = 1)
        addOnlinePlayer(watcher, slot = 2)
        addOnlinePlayer(bystander, slot = 3)

        service.notifyOnline(target)

        val packet = watcherClient.single<UpdateFriendList>()
        val entry = packet.friends.single() as UpdateFriendList.OnlineFriend
        assertEquals("Alt", entry.name)
        assertEquals(1, entry.worldId)
        assertFalse(entry.added)
        assertFalse(bystanderClient.any<UpdateFriendList>())
    }

    @Test
    fun `notify offline sends offline update to friends`() {
        val targetId = insertAccount("alt", "Alt")
        val watcherId = insertAccount("test", "Test")
        repository.addFriend(directConnection, watcherId, targetId)
        sqlConnection.commit()

        val (target, _) = createPlayer(targetId, "Alt")
        val (watcher, watcherClient) = createPlayer(watcherId, "Test")
        addOnlinePlayer(target, slot = 1)
        addOnlinePlayer(watcher, slot = 2)

        service.notifyOffline(target)

        val packet = watcherClient.single<UpdateFriendList>()
        val entry = packet.friends.single() as UpdateFriendList.OfflineFriend
        assertEquals("Alt", entry.name)
        assertEquals(0, entry.worldId)
        assertFalse(entry.added)
    }

    @Test
    fun `notify online does nothing when no friend relation exists`() {
        val targetId = insertAccount("alt", "Alt")
        val watcherId = insertAccount("test", "Test")

        val (target, _) = createPlayer(targetId, "Alt")
        val (watcher, watcherClient) = createPlayer(watcherId, "Test")
        addOnlinePlayer(target, slot = 1)
        addOnlinePlayer(watcher, slot = 2)

        service.notifyOnline(target)

        assertTrue(watcherClient.messages.isEmpty())
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

    private fun addOnlinePlayer(player: Player, slot: Int) {
        player.slotId = slot
        playerList[slot] = player
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

        inline fun <reified T> any(): Boolean = messages.any { it is T }

        inline fun <reified T> single(): T = messages.filterIsInstance<T>().single()
    }
}
