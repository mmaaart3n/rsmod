package org.rsmod.content.other.special.attacks.boost

import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.specials.SpecialAttackManager
import org.rsmod.api.specials.SpecialAttackMap
import org.rsmod.api.specials.SpecialAttackRepository
import org.rsmod.content.other.special.attacks.configs.special_objs
import org.rsmod.content.other.special.attacks.configs.special_seqs

class DragonBattleaxeSpecialAttack : SpecialAttackMap {
    override fun SpecialAttackRepository.register(manager: SpecialAttackManager) {
        registerInstant(special_objs.dragon_battleaxe) { dragonBattleaxeRampage() }
    }

    private fun ProtectedAccess.dragonBattleaxeRampage(): Boolean {
        anim(special_seqs.dragon_battleaxe)
        statDrain(stats.attack, constant = 0, percent = 10)
        statDrain(stats.defence, constant = 0, percent = 10)
        statDrain(stats.ranged, constant = 0, percent = 10)
        statDrain(stats.magic, constant = 0, percent = 10)
        statBoost(stats.strength, constant = 10, percent = 0)
        mes("You feel a surge of strength.")
        return true
    }
}
