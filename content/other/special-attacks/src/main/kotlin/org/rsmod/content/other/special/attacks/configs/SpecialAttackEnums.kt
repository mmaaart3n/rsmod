package org.rsmod.content.other.special.attacks.configs

import org.rsmod.api.type.refs.enums.EnumReferences
import org.rsmod.game.type.obj.ObjType

typealias special_enums = SpecialAttackEnums

object SpecialAttackEnums : EnumReferences() {
    val energy_requirements = find<ObjType, Int>("sa_energy_requirements")
}
