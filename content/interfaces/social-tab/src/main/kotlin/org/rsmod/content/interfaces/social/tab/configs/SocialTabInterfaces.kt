package org.rsmod.content.interfaces.social.tab.configs

import org.rsmod.api.type.refs.interf.InterfaceReferences

typealias social_interfaces = SocialTabInterfaces

object SocialTabInterfaces : InterfaceReferences() {
    val friends = find("friends")
    val ignore = find("ignore")
}
