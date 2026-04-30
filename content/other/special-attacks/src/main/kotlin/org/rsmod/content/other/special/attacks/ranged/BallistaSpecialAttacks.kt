package org.rsmod.content.other.special.attacks.ranged

import jakarta.inject.Inject
import org.rsmod.api.combat.commons.CombatAttack
import org.rsmod.api.combat.manager.RangedAmmoManager
import org.rsmod.api.config.refs.projanims
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.quiver
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
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.ObjTypeList

class BallistaSpecialAttacks
@Inject
constructor(private val objTypes: ObjTypeList, private val ammunition: RangedAmmoManager) :
    SpecialAttackMap {
    override fun SpecialAttackRepository.register(manager: SpecialAttackManager) {
        val special = Ballista(manager, ammunition, objTypes)
        registerRanged(special_objs.light_ballista, special)
        registerRanged(special_objs.heavy_ballista, special)
        registerRanged(special_objs.br_light_ballista, special)
        registerRanged(special_objs.br_heavy_ballista, special)
        registerRanged(special_objs.heavy_ballista_ornament, special)
    }
}

private class Ballista(
    private val manager: SpecialAttackManager,
    private val ammunition: RangedAmmoManager,
    private val objTypes: ObjTypeList,
) : RangedSpecialAttack {
    override suspend fun ProtectedAccess.attack(target: Npc, attack: CombatAttack.Ranged): Boolean =
        powerShot(target, attack)

    override suspend fun ProtectedAccess.attack(
        target: Player,
        attack: CombatAttack.Ranged,
    ): Boolean = powerShot(target, attack)

    private fun ProtectedAccess.powerShot(
        target: PathingEntity,
        attack: CombatAttack.Ranged,
    ): Boolean {
        val weaponType = objTypes[attack.weapon]
        val quiverType = objTypes.getOrNull(player.quiver)
        val canUseAmmo = ammunition.attemptAmmoUsage(player, weaponType, quiverType)
        if (!canUseAmmo || quiverType == null) {
            manager.stopCombat(this)
            mes("You need javelins in your quiver for this special attack.")
            return false
        }

        anim(special_seqs.ballista)
        val projectile =
            manager.spawnProjectile(this, target, special_spots.ballista, projanims.bolt)
        val damage =
            manager.rollRangedDamage(
                source = this,
                target = target,
                attack = attack,
                accuracyMultiplier = 1.25,
                maxHitMultiplier = 1.25,
            )
        manager.giveCombatXp(this, target, attack, damage)
        ammunition.useQuiverAmmo(player, quiverType, target.coords, projectile.serverCycles)
        val ammo: ObjType? = quiverType
        manager.queueRangedHit(
            this,
            target,
            ammo,
            damage,
            projectile.clientCycles,
            projectile.serverCycles,
        )
        manager.continueCombat(this, target)
        return true
    }
}
