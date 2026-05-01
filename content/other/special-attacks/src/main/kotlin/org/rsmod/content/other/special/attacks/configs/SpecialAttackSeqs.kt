package org.rsmod.content.other.special.attacks.configs

import org.rsmod.api.type.refs.seq.SeqReferences

typealias special_seqs = SpecialAttackSeqs

object SpecialAttackSeqs : SeqReferences() {
    val lumber_up = find("dragon_smallaxe_anim", 912284910659953726)

    val fishstabber_dragon_harpoon = find("fishstabber", 1350668019423100939)
    val fishstabber_infernal_harpoon = find("fishstabber_infernal", 1350668173984224023)
    val fishstabber_crystal_harpoon = find("fishstabber_crystal", 1350808876126604853)
    val fishstabber_infernal_harpoon_or = find("fishstabber_trailblazer", 1350875698052154560)

    val rock_knocker_dragon_pickaxe = find("rockknocker", 1137620852524073858)
    val rock_knocker_dragon_pickaxe_or_zalcano = find("rockknocker_zalcano", 1138221956779074556)
    val rock_knocker_dragon_pickaxe_or_trailblazer =
        find("rockknocker_trailblazer", 1138296294767106863)
    val rock_knocker_dragon_pickaxe_upgraded = find("rockknocker_pretty", 1137666037581574253)
    val rock_knocker_infernal_pickaxe = find("rockknocker_infernal", 1137687323942151432)
    val rock_knocker_3rd_age_pickaxe = find("rockknocker_3a", 1137546472306230121)
    val rock_knocker_crystal_pickaxe = find("rockknocker_crystal", 1138222214380946360)

    val dragon_longsword = find("cleave", 5532192131862460952)
    val human_crossbow = find("human_crossbow")
    val granite_maul = find("slayer_granite_maul_special_attack")
    val brain_anchor = find("brain_player_anchor_special_attack")
    val magic_shortbow = find("snapshot")
    val dragon_dagger = find("puncture")
    val dragon_scimitar = find("sp_attack_dragon_scimitar")
    val dragon_mace = find("shatter")
    val dragon_battleaxe = find("rampage")
    val abyssal_dagger = find("abyssal_dagger_special")
    val dragon_warhammer = find("dragon_warhammer_sa_player")
    val dragon_halberd = find("dragon_halberd_special_attack")
    val abyssal_whip = find("slayer_abyssal_whip_attack")
    val elder_maul = find("human_elder_maul_spec")
    val ags = find("ags_special_player")
    val bgs = find("bgs_special_player")
    val sgs = find("sgs_special_player")
    val zgs = find("zgs_special_player")
    val dragon_claws = find("human_dragon_claws_spec")
    val dragon_spear = find("shove_1h")
    val rune_thrownaxe = find("human_throw_dart1")
    val ballista = find("ballista_special_attack")
    val voidwaker = find("human_special_voidwaker")
    val ancient_godsword = find("xbows_blood_sacrifice")
    val toxic_blowpipe = find("snakeboss_blowpipe_attack")
    val toxic_blowpipe_ornament = find("snakeboss_blowpipe_attack_ornament")
}
