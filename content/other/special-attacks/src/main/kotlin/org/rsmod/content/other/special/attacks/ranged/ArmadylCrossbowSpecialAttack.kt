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

class ArmadylCrossbowSpecialAttack
@Inject
constructor(private val objTypes: ObjTypeList, private val ammunition: RangedAmmoManager) :
    SpecialAttackMap {
    override fun SpecialAttackRepository.register(manager: SpecialAttackManager) {
        registerRanged(special_objs.acb, ArmadylCrossbow(manager, ammunition, objTypes))
        registerRanged(special_objs.br_acb, ArmadylCrossbow(manager, ammunition, objTypes))
    }
}

private class ArmadylCrossbow(
    private val manager: SpecialAttackManager,
    private val ammunition: RangedAmmoManager,
    private val objTypes: ObjTypeList,
) : RangedSpecialAttack {
    override suspend fun ProtectedAccess.attack(target: Npc, attack: CombatAttack.Ranged): Boolean =
        blast(target, attack)

    override suspend fun ProtectedAccess.attack(
        target: Player,
        attack: CombatAttack.Ranged,
    ): Boolean = blast(target, attack)

    private fun ProtectedAccess.blast(target: PathingEntity, attack: CombatAttack.Ranged): Boolean {
        val weaponType = objTypes[attack.weapon]
        val quiverType = objTypes.getOrNull(player.quiver)

        val canUseAmmo = ammunition.attemptAmmoUsage(player, weaponType, quiverType)
        if (!canUseAmmo) {
            manager.stopCombat(this)
            return false
        }
        if (quiverType == null) {
            manager.stopCombat(this)
            mes("You need bolts in your quiver for this special attack.")
            return false
        }

        anim(special_seqs.human_crossbow)
        val projectile =
            manager.spawnProjectile(this, target, special_spots.armadyl_crossbow, projanims.bolt)
        val damage =
            manager.rollRangedDamage(
                source = this,
                target = target,
                attack = attack,
                accuracyMultiplier = 1.15,
                maxHitMultiplier = 1.25,
            )

        manager.giveCombatXp(this, target, attack, damage)
        ammunition.useQuiverAmmo(player, quiverType, target.coords, projectile.serverCycles)
        val ammo: ObjType? = quiverType
        manager.queueRangedHit(
            source = this,
            target = target,
            ammo = ammo,
            damage = damage,
            clientDelay = projectile.clientCycles,
            hitDelay = projectile.serverCycles,
        )
        manager.continueCombat(this, target)
        return true
    }
}
