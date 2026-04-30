package org.rsmod.content.other.special.attacks.ranged

import jakarta.inject.Inject
import org.rsmod.api.combat.commons.CombatAttack
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.projanims
import org.rsmod.api.config.refs.stats
import org.rsmod.api.config.refs.varobjs
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.righthand
import org.rsmod.api.specials.SpecialAttackManager
import org.rsmod.api.specials.SpecialAttackMap
import org.rsmod.api.specials.SpecialAttackRepository
import org.rsmod.api.specials.combat.RangedSpecialAttack
import org.rsmod.content.other.special.attacks.configs.special_objs
import org.rsmod.content.other.special.attacks.configs.special_spots
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.PathingEntity
import org.rsmod.game.entity.Player
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.utils.bits.getBits

class ToxicBlowpipeSpecialAttack @Inject constructor(private val objTypes: ObjTypeList) :
    SpecialAttackMap {
    override fun SpecialAttackRepository.register(manager: SpecialAttackManager) {
        val special = ToxicBlowpipe(manager, objTypes)
        registerRanged(special_objs.toxic_blowpipe, special)
        registerRanged(special_objs.toxic_blowpipe_loaded, special)
        registerRanged(special_objs.toxic_blowpipe_loaded_ornament, special)
    }
}

private class ToxicBlowpipe(
    private val manager: SpecialAttackManager,
    private val objTypes: ObjTypeList,
) : RangedSpecialAttack {
    override suspend fun ProtectedAccess.attack(target: Npc, attack: CombatAttack.Ranged): Boolean =
        siphon(target, attack)

    override suspend fun ProtectedAccess.attack(
        target: Player,
        attack: CombatAttack.Ranged,
    ): Boolean = siphon(target, attack)

    private fun ProtectedAccess.siphon(
        target: PathingEntity,
        attack: CombatAttack.Ranged,
    ): Boolean {
        val weapon =
            player.righthand
                ?: run {
                    manager.stopCombat(this)
                    return false
                }

        val dartCount = weapon.vars.getBits(varobjs.snakeboss_blowpipe_dartcount.bits)
        if (dartCount <= 0) {
            manager.stopCombat(this)
            mes("Your blowpipe has no darts loaded.")
            return false
        }
        val dartType = weapon.vars.getBits(varobjs.snakeboss_blowpipe_darrtype.bits)
        val dartObj = dartByType(dartType)
        if (dartObj == null) {
            manager.stopCombat(this)
            mes("You are unable to fire your ammunition.")
            return false
        }

        val weaponType = objTypes[attack.weapon]
        val attackAnim = weaponType.paramOrNull(params.attack_anim_stance1)
        val attackSound = weaponType.paramOrNull(params.attack_sound_stance1)
        attackAnim?.let(::anim)
        attackSound?.let(::soundSynth)

        val dartTypeDef = objTypes[dartObj]
        val travelSpot = special_spots.toxic_blowpipe
        val projanim = projanims.thrown

        val projectile = manager.spawnProjectile(this, target, travelSpot, projanim)
        val damage = manager.rollRangedDamage(this, target, attack, accuracyMultiplier = 1.5)

        if (damage > 0) {
            val heal = (damage * 0.5).toInt().coerceAtLeast(1)
            val prayer = (damage * 0.25).toInt().coerceAtLeast(0)
            statHeal(stats.hitpoints, constant = heal, percent = 0)
            statBoost(stats.prayer, constant = prayer, percent = 0)
        }

        manager.giveCombatXp(this, target, attack, damage)
        manager.queueRangedHit(
            this,
            target,
            dartObj,
            damage,
            projectile.clientCycles,
            projectile.serverCycles,
        )
        manager.continueCombat(this, target)
        return true
    }

    private fun dartByType(type: Int): ObjType? =
        when (type) {
            0 -> special_objs.bronze_dart
            1 -> special_objs.iron_dart
            2 -> special_objs.steel_dart
            3 -> special_objs.black_dart
            4 -> special_objs.mithril_dart
            5 -> special_objs.adamant_dart
            6 -> special_objs.rune_dart
            7 -> special_objs.amethyst_dart
            8 -> special_objs.dragon_dart
            else -> null
        }
}
