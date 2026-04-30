package org.rsmod.api.account.character.social

import jakarta.inject.Inject
import java.time.LocalDateTime
import org.rsmod.api.db.DatabaseConnection
import org.rsmod.api.db.util.getLocalDateTime
import org.rsmod.api.db.util.getStringOrNull

public class SocialRepository @Inject constructor() {
    public fun findAccountByName(
        connection: DatabaseConnection,
        inputName: String,
    ): SocialAccount? {
        val normalized = normalizeSearchName(inputName)
        if (normalized.isEmpty()) {
            return null
        }
        val loginName = normalized.lowercase()
        val displayName = normalized.lowercase()
        val select =
            connection.prepareStatement(
                """
                    SELECT
                        id,
                        login_username,
                        COALESCE(NULLIF(display_name, ''), login_username) AS resolved_name
                    FROM accounts
                    WHERE login_username = ?
                        OR LOWER(display_name) = ?
                    LIMIT 1
                """
                    .trimIndent()
            )
        select.use {
            it.setString(1, loginName)
            it.setString(2, displayName)
            it.executeQuery().use { resultSet ->
                if (!resultSet.next()) {
                    return null
                }
                return SocialAccount(
                    accountId = resultSet.getInt("id"),
                    loginName = resultSet.getString("login_username"),
                    displayName = resultSet.getString("resolved_name"),
                )
            }
        }
    }

    public fun addFriend(
        connection: DatabaseConnection,
        accountId: Int,
        friendAccountId: Int,
    ): Boolean {
        require(accountId != friendAccountId) { "Account cannot add itself as friend." }
        val insert =
            connection.prepareStatement(
                """
                    INSERT INTO friends (account_id, friend_account_id)
                    VALUES (?, ?)
                    ON CONFLICT(account_id, friend_account_id) DO NOTHING
                """
                    .trimIndent()
            )
        insert.use {
            it.setInt(1, accountId)
            it.setInt(2, friendAccountId)
            return it.executeUpdate() > 0
        }
    }

    public fun removeFriend(
        connection: DatabaseConnection,
        accountId: Int,
        friendAccountId: Int,
    ): Boolean {
        val delete =
            connection.prepareStatement(
                "DELETE FROM friends WHERE account_id = ? AND friend_account_id = ?"
            )
        delete.use {
            it.setInt(1, accountId)
            it.setInt(2, friendAccountId)
            return it.executeUpdate() > 0
        }
    }

    public fun getFriends(connection: DatabaseConnection, accountId: Int): List<SocialEntry> {
        val select =
            connection.prepareStatement(
                """
                    SELECT friend_account_id, friend_display_name_snapshot, created_at
                    FROM friends
                    WHERE account_id = ?
                    ORDER BY created_at ASC, friend_account_id ASC
                """
                    .trimIndent()
            )
        select.use {
            it.setInt(1, accountId)
            it.executeQuery().use { resultSet ->
                val entries = mutableListOf<SocialEntry>()
                while (resultSet.next()) {
                    val targetId = resultSet.getInt("friend_account_id")
                    val snapshot = resultSet.getStringOrNull("friend_display_name_snapshot")
                    val createdAt = resultSet.getLocalDateTime("created_at")
                    entries += SocialEntry(targetId, snapshot, createdAt)
                }
                return entries
            }
        }
    }

    public fun getFriendDisplayEntries(
        connection: DatabaseConnection,
        accountId: Int,
    ): List<SocialDisplayEntry> {
        val select =
            connection.prepareStatement(
                """
                    SELECT
                        f.friend_account_id,
                        COALESCE(NULLIF(a.display_name, ''), a.login_username) AS resolved_name
                    FROM friends f
                    JOIN accounts a ON a.id = f.friend_account_id
                    WHERE f.account_id = ?
                    ORDER BY f.created_at ASC, f.friend_account_id ASC
                """
                    .trimIndent()
            )
        select.use {
            it.setInt(1, accountId)
            it.executeQuery().use { resultSet ->
                val entries = mutableListOf<SocialDisplayEntry>()
                while (resultSet.next()) {
                    entries +=
                        SocialDisplayEntry(
                            accountId = resultSet.getInt("friend_account_id"),
                            displayName = resultSet.getString("resolved_name"),
                        )
                }
                return entries
            }
        }
    }

    public fun getAccountsThatHaveFriend(
        connection: DatabaseConnection,
        friendAccountId: Int,
    ): List<Int> {
        val select =
            connection.prepareStatement(
                """
                    SELECT account_id
                    FROM friends
                    WHERE friend_account_id = ?
                """
                    .trimIndent()
            )
        select.use {
            it.setInt(1, friendAccountId)
            it.executeQuery().use { resultSet ->
                val accounts = mutableListOf<Int>()
                while (resultSet.next()) {
                    accounts += resultSet.getInt("account_id")
                }
                return accounts
            }
        }
    }

    public fun isFriend(
        connection: DatabaseConnection,
        accountId: Int,
        friendAccountId: Int,
    ): Boolean {
        val select =
            connection.prepareStatement(
                """
                    SELECT 1
                    FROM friends
                    WHERE account_id = ? AND friend_account_id = ?
                    LIMIT 1
                """
                    .trimIndent()
            )
        select.use {
            it.setInt(1, accountId)
            it.setInt(2, friendAccountId)
            it.executeQuery().use { resultSet ->
                return resultSet.next()
            }
        }
    }

    public fun addIgnore(
        connection: DatabaseConnection,
        accountId: Int,
        ignoredAccountId: Int,
    ): Boolean {
        require(accountId != ignoredAccountId) { "Account cannot ignore itself." }
        val insert =
            connection.prepareStatement(
                """
                    INSERT INTO ignores (account_id, ignored_account_id)
                    VALUES (?, ?)
                    ON CONFLICT(account_id, ignored_account_id) DO NOTHING
                """
                    .trimIndent()
            )
        insert.use {
            it.setInt(1, accountId)
            it.setInt(2, ignoredAccountId)
            return it.executeUpdate() > 0
        }
    }

    public fun removeIgnore(
        connection: DatabaseConnection,
        accountId: Int,
        ignoredAccountId: Int,
    ): Boolean {
        val delete =
            connection.prepareStatement(
                "DELETE FROM ignores WHERE account_id = ? AND ignored_account_id = ?"
            )
        delete.use {
            it.setInt(1, accountId)
            it.setInt(2, ignoredAccountId)
            return it.executeUpdate() > 0
        }
    }

    public fun getIgnores(connection: DatabaseConnection, accountId: Int): List<SocialEntry> {
        val select =
            connection.prepareStatement(
                """
                    SELECT ignored_account_id, ignored_display_name_snapshot, created_at
                    FROM ignores
                    WHERE account_id = ?
                    ORDER BY created_at ASC, ignored_account_id ASC
                """
                    .trimIndent()
            )
        select.use {
            it.setInt(1, accountId)
            it.executeQuery().use { resultSet ->
                val entries = mutableListOf<SocialEntry>()
                while (resultSet.next()) {
                    val targetId = resultSet.getInt("ignored_account_id")
                    val snapshot = resultSet.getStringOrNull("ignored_display_name_snapshot")
                    val createdAt = resultSet.getLocalDateTime("created_at")
                    entries += SocialEntry(targetId, snapshot, createdAt)
                }
                return entries
            }
        }
    }

    public fun getIgnoreDisplayEntries(
        connection: DatabaseConnection,
        accountId: Int,
    ): List<SocialDisplayEntry> {
        val select =
            connection.prepareStatement(
                """
                    SELECT
                        i.ignored_account_id,
                        COALESCE(NULLIF(a.display_name, ''), a.login_username) AS resolved_name
                    FROM ignores i
                    JOIN accounts a ON a.id = i.ignored_account_id
                    WHERE i.account_id = ?
                    ORDER BY i.created_at ASC, i.ignored_account_id ASC
                """
                    .trimIndent()
            )
        select.use {
            it.setInt(1, accountId)
            it.executeQuery().use { resultSet ->
                val entries = mutableListOf<SocialDisplayEntry>()
                while (resultSet.next()) {
                    entries +=
                        SocialDisplayEntry(
                            accountId = resultSet.getInt("ignored_account_id"),
                            displayName = resultSet.getString("resolved_name"),
                        )
                }
                return entries
            }
        }
    }

    public fun isIgnored(
        connection: DatabaseConnection,
        accountId: Int,
        ignoredAccountId: Int,
    ): Boolean {
        val select =
            connection.prepareStatement(
                """
                    SELECT 1
                    FROM ignores
                    WHERE account_id = ? AND ignored_account_id = ?
                    LIMIT 1
                """
                    .trimIndent()
            )
        select.use {
            it.setInt(1, accountId)
            it.setInt(2, ignoredAccountId)
            it.executeQuery().use { resultSet ->
                return resultSet.next()
            }
        }
    }
}

public data class SocialEntry(
    val accountId: Int,
    val displayNameSnapshot: String?,
    val createdAt: LocalDateTime?,
)

public data class SocialDisplayEntry(val accountId: Int, val displayName: String)

public data class SocialAccount(val accountId: Int, val loginName: String, val displayName: String)

private fun normalizeSearchName(input: String): String =
    input.trim().split(Regex(" +")).joinToString(" ")
