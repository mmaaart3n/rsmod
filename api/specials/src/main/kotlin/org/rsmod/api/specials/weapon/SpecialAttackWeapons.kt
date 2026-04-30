package org.rsmod.api.specials.weapon

import jakarta.inject.Inject
import org.rsmod.api.specials.configs.energy_enums
import org.rsmod.api.specials.energy.SpecialAttackEnergy
import org.rsmod.game.enums.EnumTypeMapResolver
import org.rsmod.game.type.obj.ObjType

public class SpecialAttackWeapons
@Inject
constructor(private val enumResolver: EnumTypeMapResolver) {
    private lateinit var energyRequirements: Map<Int, Int>
    private lateinit var descriptions: Map<Int, String>

    /**
     * Returns the special attack energy requirement for [objType] from the `energy_requirements`
     * enum.
     *
     * @return the special attack energy requirement for [objType] in the range of `1` to
     *   [MAX_ENERGY] (`1000`), or `null` if [objType] does not have an associated special attack.
     * @see [loadEnergyRequirements]
     */
    public fun getSpecialEnergy(objType: ObjType): Int? = energyRequirements[objType.id]

    public fun getSpecialDescription(objType: ObjType): String? = descriptions[objType.id]

    internal fun startup() {
        val energyRequirements = loadEnergyRequirements()
        this.energyRequirements = energyRequirements

        val descriptions = loadDescriptions()
        this.descriptions = descriptions
    }

    private fun loadEnergyRequirements(): Map<Int, Int> {
        val requirements = mutableMapOf<Int, Int>()

        val enum = enumResolver[energy_enums.energy_requirements].filterValuesNotNull()
        for ((obj, energy) in enum) {
            check(energy in 0..MAX_ENERGY) {
                "Expected `energy` values to be within range of [0..$MAX_ENERGY]: actual=$energy"
            }
            requirements[obj.id] = energy
        }

        // Some weapon variants are not yet present in the cache enum table.
        requirements.putIfAbsent(TOXIC_BLOWPIPE, 500)
        requirements.putIfAbsent(TOXIC_BLOWPIPE_LOADED, 500)
        requirements.putIfAbsent(TOXIC_BLOWPIPE_LOADED_ORNAMENT, 500)
        requirements.putIfAbsent(RUNE_THROWNAXE, 500)
        requirements.putIfAbsent(DRAGON_SPEAR, 250)
        requirements.putIfAbsent(DRAGON_SPEAR_P, 250)
        requirements.putIfAbsent(DRAGON_SPEAR_P_PLUS, 250)
        requirements.putIfAbsent(DRAGON_SPEAR_P_PLUS_PLUS, 250)
        requirements.putIfAbsent(TBWT_DRAGON_SPEAR_KP, 250)
        requirements.putIfAbsent(BRUT_DRAGON_SPEAR, 250)
        requirements.putIfAbsent(BRUT_DRAGON_SPEAR_P, 250)
        requirements.putIfAbsent(BRUT_DRAGON_SPEAR_P_PLUS, 250)
        requirements.putIfAbsent(BRUT_DRAGON_SPEAR_P_PLUS_PLUS, 250)
        requirements.putIfAbsent(BRUT_DRAGON_SPEAR_KP, 250)
        requirements.putIfAbsent(BH_DRAGON_SPEAR_CORRUPTED, 250)
        requirements.putIfAbsent(BH_DRAGON_SPEAR_P_CORRUPTED, 250)
        requirements.putIfAbsent(BH_DRAGON_SPEAR_P_PLUS_CORRUPTED, 250)
        requirements.putIfAbsent(BH_DRAGON_SPEAR_P_PLUS_PLUS_CORRUPTED, 250)
        requirements.putIfAbsent(ZAMORAK_SPEAR, 250)
        requirements.putIfAbsent(ZAMORAK_HASTA, 250)
        requirements.putIfAbsent(LIGHT_BALLISTA, 650)
        requirements.putIfAbsent(HEAVY_BALLISTA, 650)
        requirements.putIfAbsent(BR_LIGHT_BALLISTA, 650)
        requirements.putIfAbsent(BR_HEAVY_BALLISTA, 650)
        requirements.putIfAbsent(HEAVY_BALLISTA_ORNAMENT, 650)
        requirements.putIfAbsent(VOIDWAKER, 500)
        requirements.putIfAbsent(BR_VOIDWAKER, 500)
        requirements.putIfAbsent(DEADMAN_BLIGHTED_VOIDWAKER, 500)
        requirements.putIfAbsent(DEADMAN_VOIDWAKER, 500)
        requirements.putIfAbsent(ANCIENT_GODSWORD, 500)
        requirements.putIfAbsent(BR_ANCIENT_GODSWORD, 500)

        return requirements
    }

    private fun loadDescriptions(): Map<Int, String> {
        val descriptions = mutableMapOf<Int, String>()

        val enum = enumResolver[energy_enums.descriptions].filterValuesNotNull()
        for ((obj, description) in enum) {
            descriptions[obj.id] = description
        }

        return descriptions
    }

    private companion object {
        private const val MAX_ENERGY = SpecialAttackEnergy.MAX_ENERGY
        private const val TOXIC_BLOWPIPE = 12924
        private const val TOXIC_BLOWPIPE_LOADED = 12926
        private const val TOXIC_BLOWPIPE_LOADED_ORNAMENT = 28688
        private const val RUNE_THROWNAXE = 805
        private const val DRAGON_SPEAR = 1249
        private const val DRAGON_SPEAR_P = 1263
        private const val DRAGON_SPEAR_P_PLUS = 5716
        private const val DRAGON_SPEAR_P_PLUS_PLUS = 5730
        private const val TBWT_DRAGON_SPEAR_KP = 3176
        private const val BRUT_DRAGON_SPEAR = 22731
        private const val BRUT_DRAGON_SPEAR_P = 22734
        private const val BRUT_DRAGON_SPEAR_P_PLUS = 22737
        private const val BRUT_DRAGON_SPEAR_P_PLUS_PLUS = 22740
        private const val BRUT_DRAGON_SPEAR_KP = 22743
        private const val BH_DRAGON_SPEAR_CORRUPTED = 28041
        private const val BH_DRAGON_SPEAR_P_CORRUPTED = 28043
        private const val BH_DRAGON_SPEAR_P_PLUS_CORRUPTED = 28045
        private const val BH_DRAGON_SPEAR_P_PLUS_PLUS_CORRUPTED = 28047
        private const val ZAMORAK_SPEAR = 11824
        private const val ZAMORAK_HASTA = 11889
        private const val LIGHT_BALLISTA = 19478
        private const val HEAVY_BALLISTA = 19481
        private const val BR_LIGHT_BALLISTA = 27188
        private const val BR_HEAVY_BALLISTA = 23630
        private const val HEAVY_BALLISTA_ORNAMENT = 26712
        private const val VOIDWAKER = 27690
        private const val BR_VOIDWAKER = 27869
        private const val DEADMAN_BLIGHTED_VOIDWAKER = 28531
        private const val DEADMAN_VOIDWAKER = 29607
        private const val ANCIENT_GODSWORD = 26233
        private const val BR_ANCIENT_GODSWORD = 27184
    }
}
