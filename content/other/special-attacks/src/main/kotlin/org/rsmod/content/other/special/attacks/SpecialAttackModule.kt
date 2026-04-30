package org.rsmod.content.other.special.attacks

import org.rsmod.api.specials.SpecialAttackMap
import org.rsmod.content.other.special.attacks.boost.DragonBattleaxeSpecialAttack
import org.rsmod.content.other.special.attacks.boost.StatBoostSpecialAttacks
import org.rsmod.content.other.special.attacks.melee.AbyssalDaggerSpecialAttack
import org.rsmod.content.other.special.attacks.melee.AbyssalWhipSpecialAttack
import org.rsmod.content.other.special.attacks.melee.AncientGodswordSpecialAttack
import org.rsmod.content.other.special.attacks.melee.BarrelchestAnchorSpecialAttack
import org.rsmod.content.other.special.attacks.melee.DragonClawsSpecialAttack
import org.rsmod.content.other.special.attacks.melee.DragonDaggerSpecialAttack
import org.rsmod.content.other.special.attacks.melee.DragonHalberdSpecialAttack
import org.rsmod.content.other.special.attacks.melee.DragonLongswordSpecialAttack
import org.rsmod.content.other.special.attacks.melee.DragonMaceSpecialAttack
import org.rsmod.content.other.special.attacks.melee.DragonScimitarSpecialAttack
import org.rsmod.content.other.special.attacks.melee.DragonWarhammerSpecialAttack
import org.rsmod.content.other.special.attacks.melee.ElderMaulSpecialAttack
import org.rsmod.content.other.special.attacks.melee.GodswordSpecialAttacks
import org.rsmod.content.other.special.attacks.melee.GraniteMaulSpecialAttack
import org.rsmod.content.other.special.attacks.melee.SpearSpecialAttacks
import org.rsmod.content.other.special.attacks.melee.VoidwakerSpecialAttack
import org.rsmod.content.other.special.attacks.ranged.ArmadylCrossbowSpecialAttack
import org.rsmod.content.other.special.attacks.ranged.BallistaSpecialAttacks
import org.rsmod.content.other.special.attacks.ranged.DarkBowSpecialAttack
import org.rsmod.content.other.special.attacks.ranged.DragonCrossbowSpecialAttack
import org.rsmod.content.other.special.attacks.ranged.MagicShortbowSpecialAttack
import org.rsmod.content.other.special.attacks.ranged.RuneThrownaxeSpecialAttack
import org.rsmod.content.other.special.attacks.ranged.ToxicBlowpipeSpecialAttack
import org.rsmod.plugin.module.PluginModule

class SpecialAttackModule : PluginModule() {
    override fun bind() {
        addSetBinding<SpecialAttackMap>(StatBoostSpecialAttacks::class.java)
        addSetBinding<SpecialAttackMap>(DragonBattleaxeSpecialAttack::class.java)

        addSetBinding<SpecialAttackMap>(DarkBowSpecialAttack::class.java)
        addSetBinding<SpecialAttackMap>(MagicShortbowSpecialAttack::class.java)
        addSetBinding<SpecialAttackMap>(DragonCrossbowSpecialAttack::class.java)
        addSetBinding<SpecialAttackMap>(ArmadylCrossbowSpecialAttack::class.java)
        addSetBinding<SpecialAttackMap>(BallistaSpecialAttacks::class.java)
        addSetBinding<SpecialAttackMap>(RuneThrownaxeSpecialAttack::class.java)

        addSetBinding<SpecialAttackMap>(DragonLongswordSpecialAttack::class.java)
        addSetBinding<SpecialAttackMap>(DragonDaggerSpecialAttack::class.java)
        addSetBinding<SpecialAttackMap>(DragonClawsSpecialAttack::class.java)
        addSetBinding<SpecialAttackMap>(DragonScimitarSpecialAttack::class.java)
        addSetBinding<SpecialAttackMap>(DragonMaceSpecialAttack::class.java)
        addSetBinding<SpecialAttackMap>(AbyssalDaggerSpecialAttack::class.java)
        addSetBinding<SpecialAttackMap>(AbyssalWhipSpecialAttack::class.java)
        addSetBinding<SpecialAttackMap>(GraniteMaulSpecialAttack::class.java)
        addSetBinding<SpecialAttackMap>(BarrelchestAnchorSpecialAttack::class.java)
        addSetBinding<SpecialAttackMap>(DragonWarhammerSpecialAttack::class.java)
        addSetBinding<SpecialAttackMap>(DragonHalberdSpecialAttack::class.java)
        addSetBinding<SpecialAttackMap>(SpearSpecialAttacks::class.java)
        addSetBinding<SpecialAttackMap>(ElderMaulSpecialAttack::class.java)
        addSetBinding<SpecialAttackMap>(GodswordSpecialAttacks::class.java)
        addSetBinding<SpecialAttackMap>(AncientGodswordSpecialAttack::class.java)
        addSetBinding<SpecialAttackMap>(VoidwakerSpecialAttack::class.java)
        addSetBinding<SpecialAttackMap>(ToxicBlowpipeSpecialAttack::class.java)
    }
}
