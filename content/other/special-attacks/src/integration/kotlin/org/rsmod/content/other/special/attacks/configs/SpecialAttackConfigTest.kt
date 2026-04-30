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
            special_objs.dragon_warhammer,
            special_objs.dragon_warhammer_ornament,
            special_objs.bh_dragon_warhammer_corrupted,
            special_objs.br_dragon_warhammer,
            special_objs.dragon_halberd,
            special_objs.bh_dragon_halberd_corrupted,
            special_objs.abyssal_whip,
            special_objs.abyssal_whip_lava,
            special_objs.abyssal_whip_ice,
            special_objs.br_abyssal_whip,
            special_objs.elder_maul,
            special_objs.elder_maul_ornament,
            special_objs.br_elder_maul,
            special_objs.ags,
            special_objs.bgs,
            special_objs.sgs,
            special_objs.zgs,
            special_objs.dragon_claws,
            special_objs.dragon_spear,
            special_objs.dragon_spear_p,
            special_objs.dragon_spear_p_plus,
            special_objs.dragon_spear_p_plus_plus,
            special_objs.tbwt_dragon_spear_kp,
            special_objs.brut_dragon_spear,
            special_objs.brut_dragon_spear_p,
            special_objs.brut_dragon_spear_p_plus,
            special_objs.brut_dragon_spear_p_plus_plus,
            special_objs.brut_dragon_spear_kp,
            special_objs.bh_dragon_spear_corrupted,
            special_objs.bh_dragon_spear_p_corrupted,
            special_objs.bh_dragon_spear_p_plus_corrupted,
            special_objs.bh_dragon_spear_p_plus_plus_corrupted,
            special_objs.zamorak_spear,
            special_objs.zamorak_hasta,
            special_objs.rune_thrownaxe,
            special_objs.light_ballista,
            special_objs.heavy_ballista,
            special_objs.br_light_ballista,
            special_objs.br_heavy_ballista,
            special_objs.heavy_ballista_ornament,
            special_objs.voidwaker,
            special_objs.br_voidwaker,
            special_objs.deadman_blighted_voidwaker,
            special_objs.deadman_voidwaker,
            special_objs.ancient_godsword,
            special_objs.br_ancient_godsword,
        )
        val missing = required.filter { it !in requirements.keys }
        assertEquals(emptyList(), missing) {
            "Missing sa_energy_requirements enum entries for: $missing"
        }
    }
}
