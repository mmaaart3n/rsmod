package org.rsmod.api.net.rsprot.handlers

import jakarta.inject.Inject
import net.rsprot.protocol.game.incoming.social.IgnoreListDel
import org.rsmod.api.net.rsprot.player.SocialService
import org.rsmod.game.entity.Player

class IgnoreListDelHandler @Inject constructor(private val socialService: SocialService) :
    MessageHandler<IgnoreListDel> {
    override fun handle(player: Player, message: IgnoreListDel) {
        socialService.removeIgnore(player, message.name)
    }
}
