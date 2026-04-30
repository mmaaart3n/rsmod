package org.rsmod.content.other.special.weapons.scripts.charge

import jakarta.inject.Inject
import org.rsmod.api.config.refs.varobjs
import org.rsmod.api.player.events.interact.HeldUDefaultEvents
import org.rsmod.api.player.events.interact.HeldUEvents
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.repo.obj.ObjRepository
import org.rsmod.api.script.onOpHeld3
import org.rsmod.api.script.onOpHeld5
import org.rsmod.api.script.onOpHeldU
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.game.inv.InvObj
import org.rsmod.game.inv.isType
import org.rsmod.game.type.obj.UnpackedObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext
import org.rsmod.utils.bits.getBits
import org.rsmod.utils.bits.withBits

object blowpipe_objs : ObjReferences() {
    val toxic_blowpipe = find("toxic_blowpipe")
    val toxic_blowpipe_loaded = find("toxic_blowpipe_loaded")
    val snakeboss_scale = find("snakeboss_scale")

    val bronze_dart = find("bronze_dart")
    val iron_dart = find("iron_dart")
    val steel_dart = find("steel_dart")
    val black_dart = find("black_dart")
    val mithril_dart = find("mithril_dart")
    val adamant_dart = find("adamant_dart")
    val rune_dart = find("rune_dart")
    val amethyst_dart = find("amethyst_dart")
    val dragon_dart = find("dragon_dart")
}

class BlowpipeCharging @Inject constructor(private val objRepo: ObjRepository) : PluginScript() {
    override fun ScriptContext.startup() {
        onOpHeldU(blowpipe_objs.toxic_blowpipe, blowpipe_objs.snakeboss_scale) {
            loadWithScales(it)
        }
        onOpHeldU(blowpipe_objs.toxic_blowpipe_loaded, blowpipe_objs.snakeboss_scale) {
            addScalesToLoaded(it)
        }
        onOpHeldU(blowpipe_objs.toxic_blowpipe) { useOnUnloadedBlowpipe(it) }
        for (dart in DartType.entries) {
            onOpHeldU(blowpipe_objs.toxic_blowpipe_loaded, dart.obj) {
                val blowpipeSlot = resolveLoadedBlowpipeSlot(it)
                if (blowpipeSlot == null) {
                    return@onOpHeldU
                }
                loadDarts(blowpipeSlot = blowpipeSlot, dartType = dart)
            }
            onOpHeldU(dart.obj, blowpipe_objs.toxic_blowpipe) {
                mes("Load your toxic blowpipe with scales first.")
            }
        }
        onOpHeld3(blowpipe_objs.toxic_blowpipe_loaded) { checkLoadState(it.inventory[it.slot]) }
        onOpHeld5(blowpipe_objs.toxic_blowpipe_loaded) { unload(it.slot) }
    }

    private suspend fun ProtectedAccess.loadWithScales(event: HeldUEvents.Type) {
        val scaleCount = invTotal(inv, blowpipe_objs.snakeboss_scale)
        if (scaleCount <= 0) {
            mes("You need Zulrah's scales to load the toxic blowpipe.")
            return
        }
        val add = minOf(MAX_SCALES, scaleCount)
        val removeScale =
            invDel(inv = inv, type = blowpipe_objs.snakeboss_scale, count = add, strict = true)
        if (removeScale.failure) return
        inv[event.firstSlot] =
            InvObj(
                blowpipe_objs.toxic_blowpipe_loaded,
                vars =
                    InvObj(blowpipe_objs.toxic_blowpipe_loaded)
                        .vars
                        .withBits(varobjs.snakeboss_blowpipe_flakes.bits, add),
            )
        mes("You load your toxic blowpipe with $add scales.")
    }

    private suspend fun ProtectedAccess.addScalesToLoaded(event: HeldUEvents.Type) {
        val blowpipe = inv[event.firstSlot] ?: return
        val current = blowpipe.vars.getBits(varobjs.snakeboss_blowpipe_flakes.bits)
        val scaleCount = invTotal(inv, blowpipe_objs.snakeboss_scale)
        if (scaleCount <= 0) return
        val add = minOf(MAX_SCALES - current, scaleCount)
        if (add <= 0) {
            mes("Your toxic blowpipe can't hold any more scales.")
            return
        }
        val removeScale =
            invDel(inv = inv, type = blowpipe_objs.snakeboss_scale, count = add, strict = true)
        if (removeScale.failure) return
        val updated = blowpipe.vars.withBits(varobjs.snakeboss_blowpipe_flakes.bits, current + add)
        inv[event.firstSlot] = blowpipe.copy(vars = updated)
        mes("You add $add scales to your toxic blowpipe.")
    }

    private suspend fun ProtectedAccess.useOnUnloadedBlowpipe(event: HeldUDefaultEvents.Type) {
        if (event.second.toDartType() == null) {
            return
        }
        mes("Load your toxic blowpipe with scales first.")
    }

    private suspend fun ProtectedAccess.loadDarts(blowpipeSlot: Int, dartType: DartType) {
        val dartCount = invTotal(inv, dartType.obj)
        if (dartCount <= 0) {
            mes("You don't have any darts to load.")
            return
        }
        val blowpipe = inv[blowpipeSlot] ?: return
        val currentDarts = blowpipe.vars.getBits(varobjs.snakeboss_blowpipe_dartcount.bits)
        val add = minOf(MAX_DARTS - currentDarts, dartCount)
        if (add <= 0) {
            mes("Your toxic blowpipe can't hold any more darts.")
            return
        }
        if (currentDarts > 0) {
            val currentType = blowpipe.vars.getBits(varobjs.snakeboss_blowpipe_darrtype.bits)
            if (currentType != dartType.type) {
                mes("Your blowpipe is already loaded with a different dart type.")
                return
            }
        }
        val removed = invDel(inv = inv, type = dartType.obj, count = add, strict = true)
        if (removed.failure) {
            return
        }
        val withType =
            blowpipe.vars.withBits(varobjs.snakeboss_blowpipe_darrtype.bits, dartType.type)
        val withCount =
            withType.withBits(varobjs.snakeboss_blowpipe_dartcount.bits, currentDarts + add)
        inv[blowpipeSlot] = blowpipe.copy(vars = withCount)
        mes("You load $add darts into your toxic blowpipe.")
    }

    private fun ProtectedAccess.resolveLoadedBlowpipeSlot(event: HeldUEvents.Type): Int? {
        if (inv[event.firstSlot]?.isType(blowpipe_objs.toxic_blowpipe_loaded) == true) {
            return event.firstSlot
        }
        if (inv[event.secondSlot]?.isType(blowpipe_objs.toxic_blowpipe_loaded) == true) {
            return event.secondSlot
        }
        return null
    }

    private fun ProtectedAccess.checkLoadState(blowpipe: InvObj?) {
        if (blowpipe == null) return
        val scales = blowpipe.vars.getBits(varobjs.snakeboss_blowpipe_flakes.bits)
        val darts = blowpipe.vars.getBits(varobjs.snakeboss_blowpipe_dartcount.bits)
        val dartType = blowpipe.vars.getBits(varobjs.snakeboss_blowpipe_darrtype.bits)
        val dartName = DartType.fromType(dartType)?.obj?.internalName ?: "none"
        mes("Scales: $scales/$MAX_SCALES, Darts: $darts/$MAX_DARTS ($dartName)")
    }

    private suspend fun ProtectedAccess.unload(slot: Int) {
        val blowpipe = inv[slot] ?: return
        val scales = blowpipe.vars.getBits(varobjs.snakeboss_blowpipe_flakes.bits)
        val darts = blowpipe.vars.getBits(varobjs.snakeboss_blowpipe_dartcount.bits)
        val dartType = blowpipe.vars.getBits(varobjs.snakeboss_blowpipe_darrtype.bits)
        val dartObj = DartType.fromType(dartType)?.obj

        inv[slot] = InvObj(blowpipe_objs.toxic_blowpipe)
        if (scales > 0) {
            invAddOrDrop(objRepo, blowpipe_objs.snakeboss_scale, scales)
        }
        if (darts > 0 && dartObj != null) {
            invAddOrDrop(objRepo, dartObj, darts)
        }
        mes("You unload your toxic blowpipe.")
    }

    private fun UnpackedObjType.toDartType(): DartType? = DartType.fromObj(this)

    private enum class DartType(val type: Int, val obj: org.rsmod.game.type.obj.ObjType) {
        Bronze(0, blowpipe_objs.bronze_dart),
        Iron(1, blowpipe_objs.iron_dart),
        Steel(2, blowpipe_objs.steel_dart),
        Black(3, blowpipe_objs.black_dart),
        Mithril(4, blowpipe_objs.mithril_dart),
        Adamant(5, blowpipe_objs.adamant_dart),
        Rune(6, blowpipe_objs.rune_dart),
        Amethyst(7, blowpipe_objs.amethyst_dart),
        Dragon(8, blowpipe_objs.dragon_dart);

        companion object {
            fun fromObj(type: UnpackedObjType): DartType? = entries.firstOrNull { it.obj == type }

            fun fromType(type: Int): DartType? = entries.firstOrNull { it.type == type }
        }
    }

    private companion object {
        const val MAX_DARTS = 16_383
        const val MAX_SCALES = 16_383
    }
}
