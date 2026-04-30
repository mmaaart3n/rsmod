package org.rsmod.content.interfaces.social.tab.configs

import org.rsmod.api.type.refs.comp.ComponentReferences

typealias social_components = SocialTabComponents

object SocialTabComponents : ComponentReferences() {
    val friends_ignore = find("friends:ignore")
    val ignore_friends = find("ignore:friends")
}
