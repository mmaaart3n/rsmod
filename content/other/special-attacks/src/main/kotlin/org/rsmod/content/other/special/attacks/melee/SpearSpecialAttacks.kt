package org.rsmod.content.other.special.attacks.melee

import org.rsmod.api.combat.commons.CombatAttack
import org.rsmod.api.config.constants
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

class SpearSpecialAttacks : SpecialAttackMap {
    override fun SpecialAttackRepository.register(manager: SpecialAttackManager) {
        val special = SpearShove(manager)
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

private class SpearShove(private val manager: SpecialAttackManager) : MeleeSpecialAttack {
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
        manager.queueMeleeHit(this, target, damage)
        manager.setNextAttackDelay(this, cycles = 1)
        manager.continueCombat(this, target)
        return true
    }
}
