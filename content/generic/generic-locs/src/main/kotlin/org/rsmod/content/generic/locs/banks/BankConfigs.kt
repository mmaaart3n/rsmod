package org.rsmod.content.generic.locs.banks

import org.rsmod.api.config.refs.content
import org.rsmod.api.type.editors.loc.LocEditor
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.game.type.loc.LocType

internal typealias bank_locs = BankLocs

internal object BankLocs : LocReferences() {
    val dwarf_keldagrim_bankbooth = find("dwarf_keldagrim_bankbooth")

    val bankbooth = find("bankbooth")
    val bankbooth_multi = find("bankbooth_multi")
    val bankbooth_end_left = find("bankbooth_end_left")
    val bankbooth_end_right = find("bankbooth_end_right")
    val bankbooth_ap1 = find("bankbooth_ap1")

    val newbiebankbooth = find("newbiebankbooth")
    val elid_bankbooth = find("elid_bankbooth")
    val fever_bankbooth = find("fever_bankbooth")
    val corscurs_bankbooth_multi = find("corscurs_bankbooth_multi")
    val burgh_bank_booth_multiloc = find("burgh_bank_booth_multiloc")
    val burgh_bankbooth_repaired = find("burgh_bankbooth_repaired")
    val pest_bankbooth = find("pest_bankbooth")
    val pest_bankbooth_multi = find("pest_bankbooth_multi")
    val ahoy_bankbooth = find("ahoy_bankbooth")
    val ahoy_bankbooth_multi = find("ahoy_bankbooth_multi")
    val lunar_moonclan_bankbooth = find("lunar_moonclan_bankbooth")

    val fai_varrock_bankbooth = find("fai_varrock_bankbooth")
    val fai_varrock_bankbooth_multi = find("fai_varrock_bankbooth_multi")
    val fai_falador_bankbooth = find("fai_falador_bankbooth")
    val fai_falador_bankbooth_multi = find("fai_falador_bankbooth_multi")

    val contact_bank_booth = find("contact_bank_booth")
    val contact_bank_booth_multi = find("contact_bank_booth_multi")
    val dorgesh_bank_booth = find("dorgesh_bank_booth")
    val canafis_bankbooth = find("canafis_bankbooth")
    val kr_bankbooth = find("kr_bankbooth")
    val kr_bankbooth_multi = find("kr_bankbooth_multi")

    val piscarilius_bank_booth_01 = find("piscarilius_bank_booth_01")
    val piscarilius_bank_booth_02 = find("piscarilius_bank_booth_02")
    val piscarilius_bank_booth_03 = find("piscarilius_bank_booth_03")
    val piscarilius_bank_booth_04 = find("piscarilius_bank_booth_04")
    val archeeus_bank_booth_open_01 = find("archeeus_bank_booth_open_01")
    val archeeus_bank_booth_open_02 = find("archeeus_bank_booth_open_02")
    val archeeus_bank_booth_open_03 = find("archeeus_bank_booth_open_03")
    val archeeus_bank_booth_open_04 = find("archeeus_bank_booth_open_04")
    val lova_bank_booth_01 = find("lova_bank_booth_01")
    val lova_bank_booth_02 = find("lova_bank_booth_02")
    val lova_bank_booth_03 = find("lova_bank_booth_03")
    val lova_bank_booth_04 = find("lova_bank_booth_04")

    val tob_surface_bankbooth = find("tob_surface_bankbooth")
    val prif_bankbooth_open = find("prif_bankbooth_open")
    val tut2_bankbooth = find("tut2_bankbooth")
    val darkm_bankbooth = find("darkm_bankbooth")
    val gim_island_bankbooth = find("gim_island_bankbooth")

    val aide_bankbooth = find("aide_bankbooth")
    val aide_bankbooth_multi = find("aide_bankbooth_multi")
}

internal object BankLocEditor : LocEditor() {
    init {
        booth(bank_locs.dwarf_keldagrim_bankbooth)

        booth(bank_locs.bankbooth)
        booth(bank_locs.bankbooth_multi)
        booth(bank_locs.bankbooth_end_left)
        booth(bank_locs.bankbooth_end_right)
        booth(bank_locs.bankbooth_ap1)

        booth(bank_locs.newbiebankbooth)
        booth(bank_locs.elid_bankbooth)
        booth(bank_locs.fever_bankbooth)
        booth(bank_locs.corscurs_bankbooth_multi)
        booth(bank_locs.burgh_bank_booth_multiloc)
        booth(bank_locs.burgh_bankbooth_repaired)
        booth(bank_locs.pest_bankbooth)
        booth(bank_locs.pest_bankbooth_multi)
        booth(bank_locs.ahoy_bankbooth)
        booth(bank_locs.ahoy_bankbooth_multi)
        booth(bank_locs.lunar_moonclan_bankbooth)

        booth(bank_locs.fai_varrock_bankbooth)
        booth(bank_locs.fai_varrock_bankbooth_multi)
        booth(bank_locs.fai_falador_bankbooth)
        booth(bank_locs.fai_falador_bankbooth_multi)

        booth(bank_locs.contact_bank_booth)
        booth(bank_locs.contact_bank_booth_multi)
        booth(bank_locs.dorgesh_bank_booth)
        booth(bank_locs.canafis_bankbooth)
        booth(bank_locs.kr_bankbooth)
        booth(bank_locs.kr_bankbooth_multi)

        booth(bank_locs.piscarilius_bank_booth_01)
        booth(bank_locs.piscarilius_bank_booth_02)
        booth(bank_locs.piscarilius_bank_booth_03)
        booth(bank_locs.piscarilius_bank_booth_04)
        booth(bank_locs.archeeus_bank_booth_open_01)
        booth(bank_locs.archeeus_bank_booth_open_02)
        booth(bank_locs.archeeus_bank_booth_open_03)
        booth(bank_locs.archeeus_bank_booth_open_04)
        booth(bank_locs.lova_bank_booth_01)
        booth(bank_locs.lova_bank_booth_02)
        booth(bank_locs.lova_bank_booth_03)
        booth(bank_locs.lova_bank_booth_04)

        booth(bank_locs.tob_surface_bankbooth)
        booth(bank_locs.prif_bankbooth_open)
        booth(bank_locs.tut2_bankbooth)
        booth(bank_locs.darkm_bankbooth)
        booth(bank_locs.gim_island_bankbooth)

        booth(bank_locs.aide_bankbooth)
        booth(bank_locs.aide_bankbooth_multi)
    }

    private fun booth(type: LocType) {
        edit(type) { contentGroup = content.bank_booth }
    }
}
