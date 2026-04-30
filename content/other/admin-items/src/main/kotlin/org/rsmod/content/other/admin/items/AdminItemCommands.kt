package org.rsmod.content.other.admin.items

import com.github.michaelbull.logging.InlineLogger
import jakarta.inject.Inject
import org.rsmod.api.cheat.CheatHandlerBuilder
import org.rsmod.api.config.refs.modlevels
import org.rsmod.api.invtx.invAdd
import org.rsmod.api.player.output.mes
import org.rsmod.api.script.onCommand
import org.rsmod.game.cheat.Cheat
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.objtx.TransactionResult
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class AdminItemCommands @Inject constructor(private val objTypes: ObjTypeList) : PluginScript() {
    private val logger = InlineLogger()

    override fun ScriptContext.startup() {
        onAdminCommand("item", "Spawn item by id.", ::item) {
            invalidArgs = "Usage: ::item <itemId> [amount]"
        }
    }

    private fun item(cheat: Cheat) =
        with(cheat) {
            val itemId = args.getOrNull(0)?.toIntOrNull()
            if (itemId == null) {
                player.mes("Usage: ::item <itemId> [amount]")
                return
            }
            val amount =
                args.getOrNull(1)?.toLongOrNull()?.coerceIn(1L, Int.MAX_VALUE.toLong()) ?: 1L
            val type = objTypes[itemId]
            if (type == null) {
                player.mes("Invalid item id.")
                return
            }

            val result = player.invAdd(player.inv, type, count = amount.toInt(), strict = true)
            when (result.err) {
                TransactionResult.NotEnoughSpace,
                TransactionResult.StrictSlotTaken -> player.mes("Not enough inventory space.")
                TransactionResult.RestrictedDummyitem -> player.mes("Invalid item id.")
                null -> {
                    val itemName = type.name.takeIf { it.isNotBlank() } ?: "Item ${type.id}"
                    player.mes("Spawned ${result.completed()} x $itemName.")
                    logger.info {
                        "Admin item spawn: player=${player.username}, id=${type.id}, amount=${result.completed()}"
                    }
                }
                else -> player.mes("Not enough inventory space.")
            }
        }

    private fun ScriptContext.onAdminCommand(
        command: String,
        desc: String,
        cheat: Cheat.() -> Unit,
        init: CheatHandlerBuilder.() -> Unit = {},
    ) {
        onCommand(command) {
            this.modLevel = modlevels.moderator
            this.invalidModLevel = "You do not have permission to use this command."
            this.desc = desc
            this.cheat(cheat)
            init()
        }
    }
}
