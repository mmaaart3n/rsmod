package org.rsmod.content.other.special.attacks.melee

import org.rsmod.api.combat.commons.CombatAttack
import org.rsmod.api.config.refs.projanims
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

class VoidwakerSpecialAttack : SpecialAttackMap {
    override fun SpecialAttackRepository.register(manager: SpecialAttackManager) {
        val special = Voidwaker(manager)
        registerMelee(special_objs.voidwaker, special)
        registerMelee(special_objs.br_voidwaker, special)
        registerMelee(special_objs.deadman_blighted_voidwaker, special)
        registerMelee(special_objs.deadman_voidwaker, special)
    }
}

private class Voidwaker(private val manager: SpecialAttackManager) : MeleeSpecialAttack {
    override suspend fun ProtectedAccess.attack(target: Npc, attack: CombatAttack.Melee): Boolean =
        discharge(target, attack)

    override suspend fun ProtectedAccess.attack(
        target: Player,
        attack: CombatAttack.Melee,
    ): Boolean = discharge(target, attack)

    private fun ProtectedAccess.discharge(
        target: PathingEntity,
        attack: CombatAttack.Melee,
    ): Boolean {
        anim(special_seqs.voidwaker)
        val projectile =
            manager.spawnProjectile(this, target, special_spots.voidwaker, projanims.thrown)
        val max =
            manager.calculateMeleeMaxHit(this, target, attack.type, attack.style, multiplier = 1.5)
        val min = (max * 0.5).toInt().coerceAtLeast(1)
        val damage = random.of(min..max.coerceAtLeast(min))
        manager.giveCombatXp(this, target, attack, damage)
        manager.queueMagicHit(
            this,
            target,
            damage,
            projectile.clientCycles,
            projectile.serverCycles,
        )
        target.spotanim(
            special_spots.voidwaker_impact,
            delay = projectile.clientCycles,
            height = 96,
        )
        manager.continueCombat(this, target)
        return true
    }
}
