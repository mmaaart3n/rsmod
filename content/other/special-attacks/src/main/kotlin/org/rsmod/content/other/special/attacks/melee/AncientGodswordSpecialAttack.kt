package org.rsmod.content.other.special.attacks.melee

import org.rsmod.api.combat.commons.CombatAttack
import org.rsmod.api.config.constants
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.specials.SpecialAttackManager
import org.rsmod.api.specials.SpecialAttackMap
import org.rsmod.api.specials.SpecialAttackRepository
import org.rsmod.api.specials.combat.MeleeSpecialAttack
import org.rsmod.content.other.special.attacks.configs.special_objs
import org.rsmod.content.other.special.attacks.configs.special_seqs
import org.rsmod.content.other.special.attacks.configs.special_spots
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.PathingEntity
import org.rsmod.game.entity.Player

class AncientGodswordSpecialAttack : SpecialAttackMap {
    override fun SpecialAttackRepository.register(manager: SpecialAttackManager) {
        val special = AncientGodsword(manager)
        registerMelee(special_objs.ancient_godsword, special)
        registerMelee(special_objs.br_ancient_godsword, special)
    }
}

private class AncientGodsword(private val manager: SpecialAttackManager) : MeleeSpecialAttack {
    override suspend fun ProtectedAccess.attack(target: Npc, attack: CombatAttack.Melee): Boolean =
        bloodSacrifice(target, attack)

    override suspend fun ProtectedAccess.attack(
        target: Player,
        attack: CombatAttack.Melee,
    ): Boolean = bloodSacrifice(target, attack)

    private fun ProtectedAccess.bloodSacrifice(
        target: PathingEntity,
        attack: CombatAttack.Melee,
    ): Boolean {
        anim(special_seqs.ancient_godsword)
        spotanim(special_spots.ancient_godsword, slot = constants.spotanim_slot_combat, height = 96)
        val damage =
            manager.rollMeleeDamage(
                source = this,
                target = target,
                attack = attack,
                accuracyMultiplier = 1.25,
                maxHitMultiplier = 1.1,
            )
        manager.giveCombatXp(this, target, attack, damage)
        manager.queueMeleeHit(this, target, damage)
        if (damage > 0) {
            val delayed = (damage * 0.25).toInt().coerceAtLeast(1)
            manager.queueMeleeHit(this, target, delayed, delay = 9)
            statHeal(stats.hitpoints, constant = delayed, percent = 0)
        }
        manager.continueCombat(this, target)
        return true
    }
}
