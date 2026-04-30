package org.rsmod.api.net.rsprot.player

import net.rsprot.protocol.game.outgoing.social.UpdateFriendList
import net.rsprot.protocol.game.outgoing.social.UpdateIgnoreList
import org.rsmod.api.account.character.social.SocialDisplayEntry

internal fun toFriendListPacket(
    friends: List<SocialDisplayEntry>,
    isOnline: (Int) -> Boolean,
    worldId: Int,
): UpdateFriendList {
    val mapped =
        friends.map { entry ->
            if (isOnline(entry.accountId)) {
                UpdateFriendList.OnlineFriend(
                    false,
                    entry.displayName,
                    "",
                    worldId,
                    0,
                    0,
                    "",
                    "",
                    0,
                    0,
                )
            } else {
                UpdateFriendList.OfflineFriend(false, entry.displayName, "", 0, 0, "")
            }
        }
    return UpdateFriendList(mapped)
}

internal fun singleFriendAddPacket(
    name: String,
    isOnline: Boolean,
    worldId: Int,
): UpdateFriendList {
    val entry =
        if (isOnline) {
            UpdateFriendList.OnlineFriend(true, name, "", worldId, 0, 0, "", "", 0, 0)
        } else {
            UpdateFriendList.OfflineFriend(true, name, "", 0, 0, "")
        }
    return UpdateFriendList(listOf(entry))
}

internal fun singleFriendRemovePacket(name: String): UpdateFriendList {
    val removed = UpdateFriendList.OfflineFriend(false, name, "", 0, 0, "")
    return UpdateFriendList(listOf(removed))
}

internal fun singleFriendPresencePacket(
    name: String,
    isOnline: Boolean,
    worldId: Int,
): UpdateFriendList {
    val entry =
        if (isOnline) {
            UpdateFriendList.OnlineFriend(false, name, "", worldId, 0, 0, "", "", 0, 0)
        } else {
            UpdateFriendList.OfflineFriend(false, name, "", 0, 0, "")
        }
    return UpdateFriendList(listOf(entry))
}

internal fun toIgnoreListPacket(ignores: List<SocialDisplayEntry>): UpdateIgnoreList {
    val mapped = ignores.map { UpdateIgnoreList.AddedIgnoredEntry(it.displayName, "", "", false) }
    return UpdateIgnoreList(mapped)
}

internal fun singleIgnoreAddPacket(name: String): UpdateIgnoreList {
    val added = UpdateIgnoreList.AddedIgnoredEntry(name, "", "", true)
    return UpdateIgnoreList(listOf(added))
}

internal fun singleIgnoreRemovePacket(name: String): UpdateIgnoreList {
    val removed = UpdateIgnoreList.RemovedIgnoredEntry(name)
    return UpdateIgnoreList(listOf(removed))
}
