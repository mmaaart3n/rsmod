package org.rsmod.api.account.character.social

import java.nio.file.Files
import java.nio.file.Path
import java.sql.Statement
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.rsmod.api.db.DatabaseConfig
import org.rsmod.api.db.DatabaseConnection
import org.rsmod.api.db.migration.FlywayMigration
import org.rsmod.api.db.sqlite.SqliteConnection

class SocialRepositoryTest {
    private lateinit var dbPath: Path
    private lateinit var connection: java.sql.Connection
    private lateinit var db: DatabaseConnection
    private val repository = SocialRepository()

    @BeforeEach
    fun setUp() {
        dbPath = Files.createTempFile("rsmod-social-", ".db")
        val config =
            DatabaseConfig(
                scheme = "jdbc:sqlite:",
                path = dbPath.toString(),
                user = null,
                password = null,
            )
        FlywayMigration(config).migrate()
        connection = SqliteConnection(config).connect()
        db = DatabaseConnection(connection)
    }

    @AfterEach
    fun tearDown() {
        if (::connection.isInitialized && !connection.isClosed) {
            connection.close()
        }
        Files.deleteIfExists(dbPath)
    }

    @Test
    fun `repository works after migration on empty database`() {
        val account = insertAccount("alpha")
        assertEquals(emptyList<SocialEntry>(), repository.getFriends(db, account))
        assertEquals(emptyList<SocialEntry>(), repository.getIgnores(db, account))
    }

    @Test
    fun `add and load friend`() {
        val account = insertAccount("alpha")
        val friend = insertAccount("beta")

        assertTrue(repository.addFriend(db, account, friend))
        assertTrue(repository.isFriend(db, account, friend))

        val friends = repository.getFriends(db, account)
        assertEquals(1, friends.size)
        assertEquals(friend, friends.single().accountId)
    }

    @Test
    fun `duplicate add friend does not crash`() {
        val account = insertAccount("alpha")
        val friend = insertAccount("beta")

        assertTrue(repository.addFriend(db, account, friend))
        assertFalse(repository.addFriend(db, account, friend))
        assertEquals(1, repository.getFriends(db, account).size)
    }

    @Test
    fun `remove friend and remove missing friend are safe`() {
        val account = insertAccount("alpha")
        val friend = insertAccount("beta")

        assertFalse(repository.removeFriend(db, account, friend))
        assertTrue(repository.addFriend(db, account, friend))
        assertTrue(repository.removeFriend(db, account, friend))
        assertFalse(repository.removeFriend(db, account, friend))
    }

    @Test
    fun `self add friend is blocked`() {
        val account = insertAccount("alpha")
        assertThrows(IllegalArgumentException::class.java) {
            repository.addFriend(db, account, account)
        }
    }

    @Test
    fun `add and load ignore`() {
        val account = insertAccount("alpha")
        val ignored = insertAccount("beta")

        assertTrue(repository.addIgnore(db, account, ignored))
        assertTrue(repository.isIgnored(db, account, ignored))

        val ignores = repository.getIgnores(db, account)
        assertEquals(1, ignores.size)
        assertEquals(ignored, ignores.single().accountId)
    }

    @Test
    fun `duplicate add ignore does not crash`() {
        val account = insertAccount("alpha")
        val ignored = insertAccount("beta")

        assertTrue(repository.addIgnore(db, account, ignored))
        assertFalse(repository.addIgnore(db, account, ignored))
        assertEquals(1, repository.getIgnores(db, account).size)
    }

    @Test
    fun `remove ignore and remove missing ignore are safe`() {
        val account = insertAccount("alpha")
        val ignored = insertAccount("beta")

        assertFalse(repository.removeIgnore(db, account, ignored))
        assertTrue(repository.addIgnore(db, account, ignored))
        assertTrue(repository.removeIgnore(db, account, ignored))
        assertFalse(repository.removeIgnore(db, account, ignored))
    }

    @Test
    fun `self ignore is blocked`() {
        val account = insertAccount("alpha")
        assertThrows(IllegalArgumentException::class.java) {
            repository.addIgnore(db, account, account)
        }
    }

    @Test
    fun `friend and ignore may coexist for same pair in phase one`() {
        val account = insertAccount("alpha")
        val other = insertAccount("beta")

        assertTrue(repository.addFriend(db, account, other))
        assertTrue(repository.addIgnore(db, account, other))

        assertTrue(repository.isFriend(db, account, other))
        assertTrue(repository.isIgnored(db, account, other))
    }

    @Test
    fun `friend display entries resolve display name with username fallback`() {
        val account = insertAccount("alpha")
        val withDisplay = insertAccount("beta", displayName = "Beta Name")
        val withoutDisplay = insertAccount("gamma", displayName = null)

        repository.addFriend(db, account, withDisplay)
        repository.addFriend(db, account, withoutDisplay)

        val entries = repository.getFriendDisplayEntries(db, account)
        assertEquals(listOf("Beta Name", "gamma"), entries.map(SocialDisplayEntry::displayName))
    }

    @Test
    fun `get accounts that have friend returns all owners`() {
        val alpha = insertAccount("alpha")
        val beta = insertAccount("beta")
        val gamma = insertAccount("gamma")
        val delta = insertAccount("delta")

        repository.addFriend(db, alpha, delta)
        repository.addFriend(db, beta, delta)
        repository.addFriend(db, gamma, beta)

        val owners = repository.getAccountsThatHaveFriend(db, delta)
        assertEquals(listOf(alpha, beta), owners.sorted())
    }

    @Test
    fun `ignore display entries resolve display name with username fallback`() {
        val account = insertAccount("alpha")
        val withDisplay = insertAccount("beta", displayName = "Beta Name")
        val withoutDisplay = insertAccount("gamma", displayName = null)

        repository.addIgnore(db, account, withDisplay)
        repository.addIgnore(db, account, withoutDisplay)

        val entries = repository.getIgnoreDisplayEntries(db, account)
        assertEquals(listOf("Beta Name", "gamma"), entries.map(SocialDisplayEntry::displayName))
    }

    @Test
    fun `find account by login name and display name`() {
        val loginId = insertAccount("alpha", displayName = "Alpha Hero")
        val displayId = insertAccount("beta", displayName = "Beta Hero")

        val byLogin = repository.findAccountByName(db, "alpha")
        val byDisplay = repository.findAccountByName(db, "beta hero")

        assertEquals(loginId, byLogin?.accountId)
        assertEquals("Alpha Hero", byLogin?.displayName)
        assertEquals(displayId, byDisplay?.accountId)
        assertEquals("Beta Hero", byDisplay?.displayName)
    }

    private fun insertAccount(username: String, displayName: String? = null): Int {
        val insert =
            db.prepareStatement(
                "INSERT INTO accounts (login_username, display_name, password_hash) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS,
            )
        insert.use {
            it.setString(1, username.lowercase())
            it.setString(2, displayName)
            it.setString(3, "hash")
            val updated = it.executeUpdate()
            check(updated == 1) { "Expected exactly one row inserted for account '$username'." }
            val id =
                it.generatedKeys.use { keys ->
                    if (keys.next()) {
                        keys.getInt(1)
                    } else {
                        error("No generated key returned for account '$username'.")
                    }
                }
            return id
        }
    }
}
