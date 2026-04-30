package org.rsmod.content.other.special.weapons.ranged

import jakarta.inject.Inject
import org.rsmod.api.combat.commons.CombatAttack
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.projanims
import org.rsmod.api.config.refs.varobjs
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.righthand
import org.rsmod.api.weapons.RangedWeapon
import org.rsmod.api.weapons.WeaponAttackManager
import org.rsmod.api.weapons.WeaponMap
import org.rsmod.api.weapons.WeaponRepository
import org.rsmod.content.other.special.weapons.scripts.charge.blowpipe_objs
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.PathingEntity
import org.rsmod.game.entity.Player
import org.rsmod.game.inv.isType
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.utils.bits.getBits

class ToxicBlowpipeWeapons @Inject constructor(private val objTypes: ObjTypeList) : WeaponMap {
    override fun WeaponRepository.register(manager: WeaponAttackManager) {
        register(blowpipe_objs.toxic_blowpipe_loaded, ToxicBlowpipe(manager, objTypes))
    }
}

private class ToxicBlowpipe(
    private val manager: WeaponAttackManager,
    private val objTypes: ObjTypeList,
) : RangedWeapon {
    override suspend fun ProtectedAccess.attack(target: Npc, attack: CombatAttack.Ranged): Boolean =
        shoot(target, attack)

    override suspend fun ProtectedAccess.attack(
        target: Player,
        attack: CombatAttack.Ranged,
    ): Boolean = shoot(target, attack)

    private fun ProtectedAccess.shoot(target: PathingEntity, attack: CombatAttack.Ranged): Boolean {
        val weapon = player.righthand
        if (weapon == null || !weapon.isType(blowpipe_objs.toxic_blowpipe_loaded)) {
            manager.stopCombat(this)
            return false
        }

        val dartCount = weapon.vars.getBits(varobjs.snakeboss_blowpipe_dartcount.bits)
        val dartType = weapon.vars.getBits(varobjs.snakeboss_blowpipe_darrtype.bits)
        if (dartCount <= 0) {
            manager.stopCombat(this)
            mes("Your blowpipe has no darts loaded.")
            return false
        }

        val dartObj = dartByType(dartType)
        if (dartObj == null) {
            manager.stopCombat(this)
            mes("You are unable to fire your ammunition.")
            return false
        }

        val dartTypeDef = objTypes[dartObj]
        val travelSpot = dartTypeDef.paramOrNull(params.proj_travel)
        if (travelSpot == null) {
            manager.stopCombat(this)
            mes("You are unable to fire your ammunition.")
            return false
        }

        manager.playWeaponFx(this, attack)
        val projectile = manager.spawnProjectile(this, target, travelSpot, projanims.thrown)
        val damage = manager.rollRangedDamage(this, target, attack)
        manager.giveCombatXp(this, target, attack, damage)
        manager.queueRangedHit(
            source = this,
            target = target,
            ammo = dartObj,
            damage = damage,
            clientDelay = projectile.clientCycles,
            hitDelay = projectile.serverCycles,
        )
        manager.continueCombat(this, target)
        return true
    }

    private fun dartByType(type: Int): ObjType? =
        when (type) {
            0 -> blowpipe_objs.bronze_dart
            1 -> blowpipe_objs.iron_dart
            2 -> blowpipe_objs.steel_dart
            3 -> blowpipe_objs.black_dart
            4 -> blowpipe_objs.mithril_dart
            5 -> blowpipe_objs.adamant_dart
            6 -> blowpipe_objs.rune_dart
            7 -> blowpipe_objs.amethyst_dart
            8 -> blowpipe_objs.dragon_dart
            else -> null
        }
}
