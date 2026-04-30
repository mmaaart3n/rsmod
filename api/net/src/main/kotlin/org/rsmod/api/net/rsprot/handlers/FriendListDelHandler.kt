package org.rsmod.api.net.rsprot.handlers

import jakarta.inject.Inject
import net.rsprot.protocol.game.incoming.social.FriendListDel
import org.rsmod.api.net.rsprot.player.SocialService
import org.rsmod.game.entity.Player

class FriendListDelHandler @Inject constructor(private val socialService: SocialService) :
    MessageHandler<FriendListDel> {
    override fun handle(player: Player, message: FriendListDel) {
        socialService.removeFriend(player, message.name)
    }
}
