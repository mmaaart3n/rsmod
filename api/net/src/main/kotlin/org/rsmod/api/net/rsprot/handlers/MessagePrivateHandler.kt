package org.rsmod.api.net.rsprot.handlers

import jakarta.inject.Inject
import net.rsprot.protocol.game.incoming.messaging.MessagePrivate
import org.rsmod.api.net.rsprot.player.PrivateMessageService
import org.rsmod.game.entity.Player

class MessagePrivateHandler
@Inject
constructor(private val privateMessageService: PrivateMessageService) :
    MessageHandler<MessagePrivate> {
    override fun handle(player: Player, message: MessagePrivate) {
        privateMessageService.sendPrivateMessage(player, message.name, message.message)
    }
}
