package org.rsmod.api.net.rsprot.handlers

import jakarta.inject.Inject
import net.rsprot.protocol.game.incoming.social.IgnoreListAdd
import org.rsmod.api.net.rsprot.player.SocialService
import org.rsmod.game.entity.Player

class IgnoreListAddHandler @Inject constructor(private val socialService: SocialService) :
    MessageHandler<IgnoreListAdd> {
    override fun handle(player: Player, message: IgnoreListAdd) {
        socialService.addIgnore(player, message.name)
    }
}
