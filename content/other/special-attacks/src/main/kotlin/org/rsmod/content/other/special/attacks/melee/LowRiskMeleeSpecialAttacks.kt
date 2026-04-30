package org.rsmod.content.other.special.attacks.melee

import org.rsmod.api.combat.commons.CombatAttack
import org.rsmod.api.combat.commons.types.MeleeAttackType
import org.rsmod.api.config.constants
import org.rsmod.api.config.refs.stats
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

class LowRiskMeleeSpecialAttacks : SpecialAttackMap {
    override fun SpecialAttackRepository.register(manager: SpecialAttackManager) {
        registerMelee(special_objs.dragon_dagger, DragonDagger(manager))
        registerMelee(special_objs.dragon_dagger_p, DragonDagger(manager))
        registerMelee(special_objs.dragon_dagger_p_plus, DragonDagger(manager))
        registerMelee(special_objs.dragon_dagger_p_plus_plus, DragonDagger(manager))

        registerMelee(special_objs.dragon_scimitar, DragonScimitar(manager))
        registerMelee(special_objs.dragon_mace, DragonMace(manager))
        registerMelee(special_objs.abyssal_dagger, AbyssalDagger(manager))
        registerMelee(special_objs.abyssal_dagger_p, AbyssalDagger(manager))
        registerMelee(special_objs.abyssal_dagger_p_plus, AbyssalDagger(manager))
        registerMelee(special_objs.abyssal_dagger_p_plus_plus, AbyssalDagger(manager))

        registerInstant(special_objs.dragon_battleaxe) { dragonBattleaxeRampage() }
    }

    private class DragonDagger(private val manager: SpecialAttackManager) : MeleeSpecialAttack {
        override suspend fun ProtectedAccess.attack(
            target: Npc,
            attack: CombatAttack.Melee,
        ): Boolean {
            puncture(target, attack)
            return true
        }

        override suspend fun ProtectedAccess.attack(
            target: Player,
            attack: CombatAttack.Melee,
        ): Boolean {
            puncture(target, attack)
            return true
        }

        private fun ProtectedAccess.puncture(target: PathingEntity, attack: CombatAttack.Melee) {
            anim(special_seqs.dragon_dagger)
            spotanim(
                spot = special_spots.dragon_dagger,
                slot = constants.spotanim_slot_combat,
                height = 96,
            )
            val first =
                manager.rollMeleeDamage(
                    source = this,
                    target = target,
                    attack = attack,
                    accuracyMultiplier = 1.15,
                    maxHitMultiplier = 1.0,
                    blockType = MeleeAttackType.Stab,
                )
            val second =
                manager.rollMeleeDamage(
                    source = this,
                    target = target,
                    attack = attack,
                    accuracyMultiplier = 1.15,
                    maxHitMultiplier = 1.0,
                    blockType = MeleeAttackType.Stab,
                )
            manager.giveCombatXp(this, target, attack, first + second)
            manager.queueMeleeHit(this, target, first, delay = 1)
            manager.queueMeleeHit(this, target, second, delay = 2)
            manager.continueCombat(this, target)
        }
    }

    private class DragonScimitar(private val manager: SpecialAttackManager) : MeleeSpecialAttack {
        override suspend fun ProtectedAccess.attack(
            target: Npc,
            attack: CombatAttack.Melee,
        ): Boolean {
            sever(target, attack)
            return true
        }

        override suspend fun ProtectedAccess.attack(
            target: Player,
            attack: CombatAttack.Melee,
        ): Boolean {
            sever(target, attack)
            mes("Your target's protection prayers are disrupted.")
            return true
        }

        private fun ProtectedAccess.sever(target: PathingEntity, attack: CombatAttack.Melee) {
            anim(special_seqs.dragon_scimitar)
            spotanim(
                spot = special_spots.dragon_scimitar,
                slot = constants.spotanim_slot_combat,
                height = 96,
            )
            val damage =
                manager.rollMeleeDamage(
                    source = this,
                    target = target,
                    attack = attack,
                    accuracyMultiplier = 1.25,
                    maxHitMultiplier = 1.0,
                    blockType = MeleeAttackType.Slash,
                )
            manager.giveCombatXp(this, target, attack, damage)
            manager.queueMeleeHit(this, target, damage)
            manager.continueCombat(this, target)
        }
    }

    private class DragonMace(private val manager: SpecialAttackManager) : MeleeSpecialAttack {
        override suspend fun ProtectedAccess.attack(
            target: Npc,
            attack: CombatAttack.Melee,
        ): Boolean {
            shatter(target, attack)
            return true
        }

        override suspend fun ProtectedAccess.attack(
            target: Player,
            attack: CombatAttack.Melee,
        ): Boolean {
            shatter(target, attack)
            return true
        }

        private fun ProtectedAccess.shatter(target: PathingEntity, attack: CombatAttack.Melee) {
            anim(special_seqs.dragon_mace)
            spotanim(
                spot = special_spots.dragon_mace,
                slot = constants.spotanim_slot_combat,
                height = 96,
            )
            val damage =
                manager.rollMeleeDamage(
                    source = this,
                    target = target,
                    attack = attack,
                    accuracyMultiplier = 1.25,
                    maxHitMultiplier = 1.5,
                )
            manager.giveCombatXp(this, target, attack, damage)
            manager.queueMeleeHit(this, target, damage)
            manager.continueCombat(this, target)
        }
    }

    private class AbyssalDagger(private val manager: SpecialAttackManager) : MeleeSpecialAttack {
        override suspend fun ProtectedAccess.attack(
            target: Npc,
            attack: CombatAttack.Melee,
        ): Boolean {
            abyssalPuncture(target, attack)
            return true
        }

        override suspend fun ProtectedAccess.attack(
            target: Player,
            attack: CombatAttack.Melee,
        ): Boolean {
            abyssalPuncture(target, attack)
            return true
        }

        private fun ProtectedAccess.abyssalPuncture(
            target: PathingEntity,
            attack: CombatAttack.Melee,
        ) {
            anim(special_seqs.abyssal_dagger)
            spotanim(
                spot = special_spots.abyssal_dagger,
                slot = constants.spotanim_slot_combat,
                height = 96,
            )
            val damage =
                manager.rollMeleeDamage(
                    source = this,
                    target = target,
                    attack = attack,
                    accuracyMultiplier = 1.15,
                    maxHitMultiplier = 1.25,
                    blockType = MeleeAttackType.Stab,
                )
            manager.giveCombatXp(this, target, attack, damage)
            manager.queueMeleeHit(this, target, damage)
            manager.continueCombat(this, target)
        }
    }

    private fun ProtectedAccess.dragonBattleaxeRampage(): Boolean {
        anim(special_seqs.dragon_battleaxe)
        statDrain(stats.attack, constant = 0, percent = 10)
        statDrain(stats.defence, constant = 0, percent = 10)
        statDrain(stats.ranged, constant = 0, percent = 10)
        statDrain(stats.magic, constant = 0, percent = 10)
        statBoost(stats.strength, constant = 10, percent = 0)
        mes("You feel a surge of strength.")
        return true
    }
}
