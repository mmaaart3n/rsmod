package org.rsmod.content.other.special.attacks.melee

import org.rsmod.api.combat.commons.CombatAttack
import org.rsmod.api.combat.commons.types.MeleeAttackType
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

class DragonClawsSpecialAttack : SpecialAttackMap {
    override fun SpecialAttackRepository.register(manager: SpecialAttackManager) {
        registerMelee(special_objs.dragon_claws, DragonClaws(manager))
    }

    private class DragonClaws(private val manager: SpecialAttackManager) : MeleeSpecialAttack {
        override suspend fun ProtectedAccess.attack(
            target: Npc,
            attack: CombatAttack.Melee,
        ): Boolean {
            sliceAndDice(target, attack)
            return true
        }

        override suspend fun ProtectedAccess.attack(
            target: Player,
            attack: CombatAttack.Melee,
        ): Boolean {
            sliceAndDice(target, attack)
            return true
        }

        private fun ProtectedAccess.sliceAndDice(
            target: PathingEntity,
            attack: CombatAttack.Melee,
        ) {
            anim(special_seqs.dragon_claws)
            target.spotanim(special_spots.dragon_claws, height = 96)

            val maxHit =
                manager.calculateMeleeMaxHit(
                    source = this,
                    target = target,
                    attackType = MeleeAttackType.Slash,
                    attackStyle = attack.style,
                    multiplier = 1.0,
                )
            val first = random.of(maxHit / 2..maxHit).coerceAtLeast(1)
            val second = (first / 2).coerceAtLeast(1)
            val third = random.of(0..second)
            val fourth = (first - second - third).coerceAtLeast(0)
            val total = first + second + third + fourth

            manager.giveCombatXp(this, target, attack, total)
            manager.queueMeleeHit(this, target, first, delay = 1)
            manager.queueMeleeHit(this, target, second, delay = 2)
            manager.queueMeleeHit(this, target, third, delay = 3)
            manager.queueMeleeHit(this, target, fourth, delay = 4)
            manager.continueCombat(this, target)
        }
    }
}
