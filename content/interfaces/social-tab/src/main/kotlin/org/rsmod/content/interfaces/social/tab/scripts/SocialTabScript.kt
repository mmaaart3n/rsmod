package org.rsmod.content.interfaces.social.tab.scripts

import jakarta.inject.Inject
import org.rsmod.api.config.refs.components
import org.rsmod.api.player.ui.ifOpenOverlay
import org.rsmod.api.script.onIfOverlayButton
import org.rsmod.content.interfaces.social.tab.configs.social_components
import org.rsmod.content.interfaces.social.tab.configs.social_interfaces
import org.rsmod.events.EventBus
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class SocialTabScript @Inject constructor(private val eventBus: EventBus) : PluginScript() {
    override fun ScriptContext.startup() {
        onIfOverlayButton(social_components.friends_ignore) {
            player.ifOpenOverlay(
                social_interfaces.ignore,
                components.toplevel_target_side9,
                eventBus,
            )
        }

        onIfOverlayButton(social_components.ignore_friends) {
            player.ifOpenOverlay(
                social_interfaces.friends,
                components.toplevel_target_side9,
                eventBus,
            )
        }
    }
}
