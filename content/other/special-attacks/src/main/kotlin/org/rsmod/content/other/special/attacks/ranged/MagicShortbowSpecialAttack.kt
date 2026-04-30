package org.rsmod.content.other.special.attacks.ranged

import jakarta.inject.Inject
import org.rsmod.api.combat.commons.CombatAttack
import org.rsmod.api.combat.manager.RangedAmmoManager
import org.rsmod.api.config.constants
import org.rsmod.api.config.refs.params
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
import org.rsmod.game.type.obj.ObjTypeList

class MagicShortbowSpecialAttack
@Inject
constructor(private val objTypes: ObjTypeList, private val ammunition: RangedAmmoManager) :
    SpecialAttackMap {
    override fun SpecialAttackRepository.register(manager: SpecialAttackManager) {
        registerRanged(special_objs.magic_shortbow, MagicShortbow(manager, ammunition, objTypes))
        registerRanged(special_objs.magic_shortbow_i, MagicShortbow(manager, ammunition, objTypes))
    }
}

private class MagicShortbow(
    private val manager: SpecialAttackManager,
    private val ammunition: RangedAmmoManager,
    private val objTypes: ObjTypeList,
) : RangedSpecialAttack {
    override suspend fun ProtectedAccess.attack(target: Npc, attack: CombatAttack.Ranged): Boolean =
        snapshot(target, attack)

    override suspend fun ProtectedAccess.attack(
        target: Player,
        attack: CombatAttack.Ranged,
    ): Boolean = snapshot(target, attack)

    private fun ProtectedAccess.snapshot(
        target: PathingEntity,
        attack: CombatAttack.Ranged,
    ): Boolean {
        val weaponType = objTypes[attack.weapon]
        val quiverType = objTypes.getOrNull(player.quiver)

        val canUseAmmo = ammunition.attemptAmmoUsage(player, weaponType, quiverType)
        if (!canUseAmmo) {
            manager.stopCombat(this)
            return false
        }
        if (quiverType == null) {
            manager.stopCombat(this)
            mes("You need arrows in your quiver for this special attack.")
            return false
        }
        if (player.quiver?.count ?: 0 < 2) {
            manager.stopCombat(this)
            mes("You need at least 2 arrows in your quiver for this special attack.")
            return false
        }
        val travelSpot = quiverType.paramOrNull(params.proj_travel)
        if (travelSpot == null) {
            manager.stopCombat(this)
            mes("You are unable to fire your ammunition.")
            return false
        }

        anim(special_seqs.magic_shortbow)
        spotanim(special_spots.magic_shortbow, slot = constants.spotanim_slot_combat, height = 96)

        val proj1 = manager.spawnProjectile(this, target, travelSpot, projanims.doublearrow_one)
        val proj2 = manager.spawnProjectile(this, target, travelSpot, projanims.doublearrow_two)
        val damage1 = manager.rollRangedDamage(this, target, attack, accuracyMultiplier = 1.0)
        val damage2 = manager.rollRangedDamage(this, target, attack, accuracyMultiplier = 1.0)

        manager.giveCombatXp(this, target, attack, damage1 + damage2)
        ammunition.useQuiverAmmo(player, quiverType, target.coords, proj1.serverCycles)
        manager.queueRangedHit(
            this,
            target,
            quiverType,
            damage1,
            proj1.clientCycles,
            proj1.serverCycles,
        )
        ammunition.useQuiverAmmo(player, quiverType, target.coords, proj2.serverCycles)
        manager.queueRangedDamage(this, target, quiverType, damage2, proj2.serverCycles)
        manager.continueCombat(this, target)
        return true
    }
}
