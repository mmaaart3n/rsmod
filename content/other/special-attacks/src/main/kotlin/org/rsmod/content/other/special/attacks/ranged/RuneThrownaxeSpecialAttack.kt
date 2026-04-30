package org.rsmod.content.other.special.attacks.ranged

import org.rsmod.api.combat.commons.CombatAttack
import org.rsmod.api.config.refs.projanims
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.specials.SpecialAttackManager
import org.rsmod.api.specials.SpecialAttackMap
import org.rsmod.api.specials.SpecialAttackRepository
import org.rsmod.api.specials.combat.RangedSpecialAttack
import org.rsmod.content.other.special.attacks.configs.special_objs
import org.rsmod.content.other.special.attacks.configs.special_seqs
import org.rsmod.content.other.special.attacks.configs.special_spots
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.PathingEntity
import org.rsmod.game.entity.Player

class RuneThrownaxeSpecialAttack : SpecialAttackMap {
    override fun SpecialAttackRepository.register(manager: SpecialAttackManager) {
        registerRanged(special_objs.rune_thrownaxe, RuneThrownaxe(manager))
    }
}

private class RuneThrownaxe(private val manager: SpecialAttackManager) : RangedSpecialAttack {
    override suspend fun ProtectedAccess.attack(target: Npc, attack: CombatAttack.Ranged): Boolean =
        chainhit(target, attack)

    override suspend fun ProtectedAccess.attack(
        target: Player,
        attack: CombatAttack.Ranged,
    ): Boolean = chainhit(target, attack)

    private fun ProtectedAccess.chainhit(
        target: PathingEntity,
        attack: CombatAttack.Ranged,
    ): Boolean {
        anim(special_seqs.rune_thrownaxe)
        val projectile =
            manager.spawnProjectile(this, target, special_spots.rune_thrownaxe, projanims.thrown)
        val first =
            manager.rollRangedDamage(
                source = this,
                target = target,
                attack = attack,
                accuracyMultiplier = 1.1,
                maxHitMultiplier = 1.1,
            )
        val second = (first * 0.5).toInt().coerceAtLeast(0)
        manager.giveCombatXp(this, target, attack, first + second)
        manager.queueRangedHit(
            source = this,
            target = target,
            ammo = special_objs.rune_thrownaxe,
            damage = first,
            clientDelay = projectile.clientCycles,
            hitDelay = projectile.serverCycles,
        )
        // Fallback single-target chain simulation while multi-target selection remains limited.
        manager.queueRangedDamage(
            source = this,
            target = target,
            ammo = special_objs.rune_thrownaxe,
            damage = second,
            hitDelay = projectile.serverCycles + 1,
        )
        manager.continueCombat(this, target)
        return true
    }
}
