package org.rsmod.content.other.special.attacks.configs

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.rsmod.api.testing.GameTestState

class SpecialAttackConfigTest {
    @Test
    fun GameTestState.`ensure mapped special weapons have energy requirements`() = runBasicGameTest {
        val requirements = cacheTypes.enums[special_enums.energy_requirements]
        val required = listOf(
            special_objs.dragon_longsword,
            special_objs.bh_dragon_longsword_imbue,
            special_objs.bh_dragon_longsword_corrupted,
            special_objs.dragon_dagger,
            special_objs.dragon_dagger_p,
            special_objs.dragon_dagger_p_plus,
            special_objs.dragon_dagger_p_plus_plus,
            special_objs.dragon_scimitar,
            special_objs.dragon_mace,
            special_objs.dragon_battleaxe,
            special_objs.granite_maul,
            special_objs.granite_maul_pretty,
            special_objs.granite_maul_plus,
            special_objs.granite_maul_pretty_plus,
            special_objs.br_granite_maul,
            special_objs.brain_anchor,
            special_objs.bh_brain_anchor_imbue,
            special_objs.magic_shortbow,
            special_objs.magic_shortbow_i,
            special_objs.xbows_crossbow_dragon,
            special_objs.bh_xbows_crossbow_dragon_corrupted,
            special_objs.acb,
            special_objs.br_acb,
            special_objs.abyssal_dagger,
            special_objs.abyssal_dagger_p,
            special_objs.abyssal_dagger_p_plus,
            special_objs.abyssal_dagger_p_plus_plus,
        )
        val missing = required.filter { it !in requirements.keys }
        assertEquals(emptyList(), missing) {
            "Missing sa_energy_requirements enum entries for: $missing"
        }
    }
}
