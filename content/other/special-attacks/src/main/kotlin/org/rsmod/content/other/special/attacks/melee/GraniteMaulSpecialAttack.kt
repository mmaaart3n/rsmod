package org.rsmod.content.other.special.attacks.melee

import org.rsmod.api.combat.commons.CombatAttack
import org.rsmod.api.combat.commons.types.MeleeAttackType
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

class GraniteMaulSpecialAttack : SpecialAttackMap {
    override fun SpecialAttackRepository.register(manager: SpecialAttackManager) {
        registerMelee(special_objs.granite_maul, GraniteMaul(manager))
        registerMelee(special_objs.granite_maul_pretty, GraniteMaul(manager))
        registerMelee(special_objs.granite_maul_plus, GraniteMaul(manager))
        registerMelee(special_objs.granite_maul_pretty_plus, GraniteMaul(manager))
        registerMelee(special_objs.br_granite_maul, GraniteMaul(manager))
    }

    private class GraniteMaul(private val manager: SpecialAttackManager) : MeleeSpecialAttack {
        override suspend fun ProtectedAccess.attack(
            target: Npc,
            attack: CombatAttack.Melee,
        ): Boolean {
            maul(target, attack)
            return true
        }

        override suspend fun ProtectedAccess.attack(
            target: Player,
            attack: CombatAttack.Melee,
        ): Boolean {
            maul(target, attack)
            return true
        }

        private fun ProtectedAccess.maul(target: PathingEntity, attack: CombatAttack.Melee) {
            anim(special_seqs.granite_maul)
            spotanim(
                spot = special_spots.granite_maul,
                slot = constants.spotanim_slot_combat,
                height = 96,
            )
            val damage =
                manager.rollMeleeDamage(
                    source = this,
                    target = target,
                    attack = attack,
                    accuracyMultiplier = 1.15,
                    maxHitMultiplier = 1.35,
                    blockType = MeleeAttackType.Crush,
                )
            manager.giveCombatXp(this, target, attack, damage)
            manager.queueMeleeHit(this, target, damage)
            manager.continueCombat(this, target)
        }
    }
}
