package org.rsmod.api.net.rsprot.player

import net.rsprot.protocol.game.outgoing.social.FriendListLoaded
import net.rsprot.protocol.game.outgoing.social.UpdateFriendList
import net.rsprot.protocol.game.outgoing.social.UpdateIgnoreList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.api.account.character.social.SocialDisplayEntry

class SocialLoginSyncTest {
    @Test
    fun `empty social lists map to empty outgoing packets`() {
        val friendPacket = toFriendListPacket(emptyList(), isOnline = { false }, worldId = 1)
        val ignorePacket = toIgnoreListPacket(emptyList())

        assertEquals(0, friendPacket.friends.size)
        assertEquals(0, ignorePacket.ignores.size)
    }

    @Test
    fun `friend mapping uses offline packet when target is offline`() {
        val packet =
            toFriendListPacket(
                friends = listOf(SocialDisplayEntry(accountId = 42, displayName = "Buddy")),
                isOnline = { false },
                worldId = 1,
            )

        assertEquals(1, packet.friends.size)
        val entry = packet.friends.single()
        check(entry is UpdateFriendList.OfflineFriend)
        assertEquals("Buddy", entry.name)
        assertEquals(0, entry.worldId)
        assertEquals(false, entry.added)
    }

    @Test
    fun `friend mapping uses online packet when target is online`() {
        val packet =
            toFriendListPacket(
                friends = listOf(SocialDisplayEntry(accountId = 7, displayName = "OnlineBuddy")),
                isOnline = { true },
                worldId = 301,
            )

        assertEquals(1, packet.friends.size)
        val entry = packet.friends.single()
        check(entry is UpdateFriendList.OnlineFriend)
        assertEquals("OnlineBuddy", entry.name)
        assertEquals(301, entry.worldId)
        assertEquals(false, entry.added)
    }

    @Test
    fun `ignore mapping creates added ignore entries`() {
        val packet =
            toIgnoreListPacket(
                listOf(
                    SocialDisplayEntry(accountId = 11, displayName = "MutedOne"),
                    SocialDisplayEntry(accountId = 12, displayName = "MutedTwo"),
                )
            )

        assertEquals(2, packet.ignores.size)
        val names = packet.ignores.map(UpdateIgnoreList.IgnoredPlayer::name)
        assertEquals(listOf("MutedOne", "MutedTwo"), names)
        packet.ignores.forEach {
            check(it is UpdateIgnoreList.AddedIgnoredEntry)
            assertEquals(false, it.added)
        }
    }

    @Test
    fun `initial social packets for empty lists include friendlist loaded and empty friend update`() {
        val packets =
            initialSocialPackets(
                friends = emptyList(),
                ignores = emptyList(),
                isOnline = { false },
                worldId = 1,
            )

        assertEquals(3, packets.size)
        assertTrue(packets[0] is FriendListLoaded)
        assertTrue(packets[1] is UpdateFriendList)
        val update = packets[1] as UpdateFriendList
        assertTrue(update.friends.isEmpty())
        assertTrue(packets[2] is UpdateIgnoreList)
        val ignoreUpdate = packets[2] as UpdateIgnoreList
        assertTrue(ignoreUpdate.ignores.isEmpty())
    }

    @Test
    fun `initial social packets with data include friend and ignore updates without friendlist loaded`() {
        val packets =
            initialSocialPackets(
                friends = listOf(SocialDisplayEntry(accountId = 1, displayName = "Alpha")),
                ignores = listOf(SocialDisplayEntry(accountId = 2, displayName = "Beta")),
                isOnline = { false },
                worldId = 1,
            )

        assertEquals(2, packets.size)
        assertTrue(packets[0] is UpdateFriendList)
        assertTrue(packets[1] is UpdateIgnoreList)
    }

    @Test
    fun `initial social packets with only friends include friend update and empty ignore update`() {
        val packets =
            initialSocialPackets(
                friends = listOf(SocialDisplayEntry(accountId = 1, displayName = "Alpha")),
                ignores = emptyList(),
                isOnline = { false },
                worldId = 1,
            )

        assertEquals(2, packets.size)
        assertTrue(packets[0] is UpdateFriendList)
        assertTrue(packets[1] is UpdateIgnoreList)
        val ignoreUpdate = packets[1] as UpdateIgnoreList
        assertTrue(ignoreUpdate.ignores.isEmpty())
    }

    @Test
    fun `initial social packets with only ignores include friendlist loaded and ignore update`() {
        val packets =
            initialSocialPackets(
                friends = emptyList(),
                ignores = listOf(SocialDisplayEntry(accountId = 2, displayName = "Beta")),
                isOnline = { false },
                worldId = 1,
            )

        assertEquals(3, packets.size)
        assertTrue(packets[0] is FriendListLoaded)
        assertTrue(packets[1] is UpdateFriendList)
        val friendUpdate = packets[1] as UpdateFriendList
        assertTrue(friendUpdate.friends.isEmpty())
        assertTrue(packets[2] is UpdateIgnoreList)
    }
}
