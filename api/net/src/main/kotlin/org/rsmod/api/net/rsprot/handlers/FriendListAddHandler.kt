package org.rsmod.api.net.rsprot.handlers

import jakarta.inject.Inject
import net.rsprot.protocol.game.incoming.social.FriendListAdd
import org.rsmod.api.net.rsprot.player.SocialService
import org.rsmod.game.entity.Player

class FriendListAddHandler @Inject constructor(private val socialService: SocialService) :
    MessageHandler<FriendListAdd> {
    override fun handle(player: Player, message: FriendListAdd) {
        socialService.addFriend(player, message.name)
    }
}
