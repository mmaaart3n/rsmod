package org.rsmod.content.other.special.attacks.melee

import jakarta.inject.Inject
import kotlin.math.abs
import kotlin.math.max
import org.rsmod.api.combat.commons.CombatAttack
import org.rsmod.api.combat.weapon.WeaponSpeeds
import org.rsmod.api.config.constants
import org.rsmod.api.config.refs.spotanims
import org.rsmod.api.config.refs.walktriggers
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
import org.rsmod.game.map.collision.isZoneValid
import org.rsmod.map.CoordGrid

private const val SPEAR_STUN_TICKS = 4

private const val LARGE_NPC_KNOCKBACK_MSG = "That creature is too large to knock back!"

class SpearSpecialAttacks @Inject constructor(private val speeds: WeaponSpeeds) : SpecialAttackMap {
    override fun SpecialAttackRepository.register(manager: SpecialAttackManager) {
        val special = SpearShove(manager, speeds)
        registerMelee(special_objs.dragon_spear, special)
        registerMelee(special_objs.dragon_spear_p, special)
        registerMelee(special_objs.dragon_spear_p_plus, special)
        registerMelee(special_objs.dragon_spear_p_plus_plus, special)
        registerMelee(special_objs.tbwt_dragon_spear_kp, special)
        registerMelee(special_objs.brut_dragon_spear, special)
        registerMelee(special_objs.brut_dragon_spear_p, special)
        registerMelee(special_objs.brut_dragon_spear_p_plus, special)
        registerMelee(special_objs.brut_dragon_spear_p_plus_plus, special)
        registerMelee(special_objs.brut_dragon_spear_kp, special)
        registerMelee(special_objs.bh_dragon_spear_corrupted, special)
        registerMelee(special_objs.bh_dragon_spear_p_corrupted, special)
        registerMelee(special_objs.bh_dragon_spear_p_plus_corrupted, special)
        registerMelee(special_objs.bh_dragon_spear_p_plus_plus_corrupted, special)
        registerMelee(special_objs.zamorak_spear, special)
        registerMelee(special_objs.zamorak_hasta, special)
    }
}

private class SpearShove(
    private val manager: SpecialAttackManager,
    private val speeds: WeaponSpeeds,
) : MeleeSpecialAttack {
    override suspend fun ProtectedAccess.attack(target: Npc, attack: CombatAttack.Melee): Boolean =
        shove(target, attack)

    override suspend fun ProtectedAccess.attack(
        target: Player,
        attack: CombatAttack.Melee,
    ): Boolean = shove(target, attack)

    private fun ProtectedAccess.shove(target: PathingEntity, attack: CombatAttack.Melee): Boolean {
        anim(special_seqs.dragon_spear)
        spotanim(special_spots.dragon_spear, slot = constants.spotanim_slot_combat, height = 96)
        val damage =
            manager.rollMeleeDamage(
                source = this,
                target = target,
                attack = attack,
                accuracyMultiplier = 1.2,
                maxHitMultiplier = 1.0,
            )
        manager.giveCombatXp(this, target, attack, damage)
        if (damage > 0) {
            manager.queueMeleeHit(this, target, damage, retaliateDelay = SPEAR_STUN_TICKS)
            applySpearKnockback(target)
            applyVictimActionLock(target)
            applyStunVisual(target)
            suppressPostShoveAutoChase()
        } else {
            manager.queueMeleeHit(this, target, damage)
            manager.continueCombat(this, target)
        }
        manager.setNextAttackDelay(this, speeds.actual(player))
        return true
    }

    private fun ProtectedAccess.suppressPostShoveAutoChase() {
        player.abortRoute()
        player.clearInteraction()
    }

    /**
     * One visible walk step away from [player] (movement processor), not telejump: cardinal if
     * aligned else dominant axis. Skips if blocked/invalid. Only [Npc] with [Npc.size] == 1.
     */
    private fun ProtectedAccess.applySpearKnockback(target: PathingEntity) {
        if (target is Npc && target.size != 1) {
            mes(LARGE_NPC_KNOCKBACK_MSG)
            return
        }
        val flagMap = collision
        val dest = shoveDestinationTile(target) ?: return
        if (!flagMap.isZoneValid(dest) || mapBlocked(dest)) return
        target.clearInteraction()
        target.pendingForcedWalkDest = dest
    }

    private fun ProtectedAccess.shoveDestinationTile(target: PathingEntity): CoordGrid? {
        val px = player.coords.x
        val pz = player.coords.z
        val tx = target.coords.x
        val tz = target.coords.z
        val dx = tx - px
        val dz = tz - pz
        val stepX: Int
        val stepZ: Int
        when {
            dx == 0 && dz != 0 -> {
                stepX = 0
                stepZ = axisSign(dz)
            }
            dz == 0 && dx != 0 -> {
                stepX = axisSign(dx)
                stepZ = 0
            }
            dx != 0 && dz != 0 -> {
                if (abs(dx) >= abs(dz)) {
                    stepX = axisSign(dx)
                    stepZ = 0
                } else {
                    stepX = 0
                    stepZ = axisSign(dz)
                }
            }
            else -> return null
        }
        return target.coords.translate(stepX, stepZ)
    }

    private fun applyVictimActionLock(target: PathingEntity) {
        val until = target.currentMapClock + SPEAR_STUN_TICKS
        when (target) {
            is Npc -> {
                target.actionDelay = max(target.actionDelay, until)
                target.controlLockUntil = max(target.controlLockUntil, until)
            }
            is Player -> target.actionDelay = max(target.actionDelay, until)
        }
    }

    private fun axisSign(delta: Int): Int =
        when {
            delta > 0 -> 1
            delta < 0 -> -1
            else -> 0
        }

    private fun ProtectedAccess.applyStunVisual(target: PathingEntity) {
        target.walkTrigger(walktriggers.stunned)
        target.spotanim(spotanims.stunned, height = 96)
    }
}
