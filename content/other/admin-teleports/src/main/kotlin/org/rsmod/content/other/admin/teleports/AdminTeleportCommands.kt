package org.rsmod.content.other.admin.teleports

import jakarta.inject.Inject
import org.rsmod.api.cheat.CheatHandlerBuilder
import org.rsmod.api.config.refs.modlevels
import org.rsmod.api.player.output.mes
import org.rsmod.api.player.protect.ProtectedAccessLauncher
import org.rsmod.api.script.onCommand
import org.rsmod.game.cheat.Cheat
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class AdminTeleportCommands
@Inject
constructor(private val protectedAccess: ProtectedAccessLauncher) : PluginScript() {
    override fun ScriptContext.startup() {
        onAdminCommand("tele", "Teleport to map coordinates.", ::tele) {
            invalidArgs = "Usage: ::tele <x> <z> [level]"
        }
        onAdminCommand("tp", "Teleport to a named location.", ::tp) {
            invalidArgs = "Usage: ::tp <locationName>"
        }
        onAdminCommand("tplist", "List named teleports.", ::tpList)
    }

    private fun tele(cheat: Cheat) =
        with(cheat) {
            val x = args.getOrNull(0)?.toIntOrNull()
            val z = args.getOrNull(1)?.toIntOrNull()
            if (x == null || z == null) {
                player.mes("Usage: ::tele <x> <z> [level]")
                return
            }
            val level = args.getOrNull(2)?.toIntOrNull() ?: player.level
            if (level !in 0..3) {
                player.mes("Usage: ::tele <x> <z> [level]")
                return
            }
            val destination = CoordGrid(x = x, z = z, level = level)
            protectedAccess.launch(player) {
                telejump(destination)
                player.mes("Teleported to $x, $z, $level.")
            }
            Unit
        }

    private fun tp(cheat: Cheat) =
        with(cheat) {
            val key = args.firstOrNull()?.lowercase()
            if (key.isNullOrBlank()) {
                player.mes("Usage: ::tp <locationName>")
                return
            }
            val destination = TELEPORTS[key]
            if (destination == null) {
                player.mes("Unknown location. Use ::tplist.")
                return
            }
            protectedAccess.launch(player) {
                telejump(destination.coords)
                player.mes("Teleported to ${destination.displayName}.")
            }
            Unit
        }

    private fun tpList(cheat: Cheat) =
        with(cheat) { player.mes("Available teleports: ${TELEPORTS.keys.joinToString(", ")}") }

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

    private data class NamedTeleport(val displayName: String, val coords: CoordGrid)

    private companion object {
        private val TELEPORTS =
            linkedMapOf(
                "home" to NamedTeleport("Home", CoordGrid(3222, 3218, 0)),
                "lumbridge" to NamedTeleport("Lumbridge", CoordGrid(3222, 3218, 0)),
                "varrock" to NamedTeleport("Varrock", CoordGrid(3210, 3424, 0)),
                "falador" to NamedTeleport("Falador", CoordGrid(2964, 3378, 0)),
                "edgeville" to NamedTeleport("Edgeville", CoordGrid(3087, 3496, 0)),
                "ge" to NamedTeleport("Grand Exchange", CoordGrid(3165, 3487, 0)),
                "draynor" to NamedTeleport("Draynor", CoordGrid(3093, 3244, 0)),
                "alkharid" to NamedTeleport("Al Kharid", CoordGrid(3293, 3174, 0)),
            )
    }
}
