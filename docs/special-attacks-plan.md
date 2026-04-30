# Special Attacks MVP Plan

## Current implementation
- Modules aanwezig:
  - `api/specials` (registry, energy, execution contracts)
  - `content/other/special-attacks` (content-spec handlers)
  - `content/other/special-weapons` (specialized normal weapon behavior, geen spec-dispatch)
  - `content/interfaces/combat-tab` (special bar/orb toggle)
- Bestaande handlers en flow:
  - `CombatTabScript` togglet `varps.sa_attack` via `special_attack` en `special_attack_orb`.
  - `PvNCombat` en PvP-combat gebruiken `activateMeleeSpecial` / `activateRangedSpecial` / `activateMagicSpecial`.
  - Energy-check + deductie loopt via `SpecialAttackEnergy` op `varps.sa_energy`.
  - Instant specs worden direct uitgevoerd via queue `sa_instant_spec`.
- Bestaande specials (nu al geregistreerd):
  - Non-combat boosts: dragon/crystal/infernal/3rd-age axe, harpoon, pickaxe varianten.
  - Combat: `dragon_longsword` (Cleave), `dark_bow` varianten (Descent).
- Bestaande beperkingen:
  - Nog weinig melee/ranged spec-wapens gemapt.
  - Geen testdekking in `api/specials` en `content/other/special-attacks`.
  - Shield-special path is nog TODO.

## Target scope
- Wel in scope (MVP voor deze branch):
  - Server-side special dispatch voor veelgebruikte OSRS spec-wapens.
  - Correcte energy consume/check, anim/gfx waar eenvoudig aanwezig, accuracy/damage multipliers.
  - Low-risk specs eerst, complex gedrag als partial of deferred.
- Niet in scope:
  - Volledige combat-engine rewrite.
  - Grote PvP-only edge-case set.
  - Nieuwe DB-schema's.
  - Full-fidelity van alle niche/raid-only specials als systemen ontbreken.

## Resterende specials matrix
| Weapon | Item ids / variants | Special name | Energy cost | Combat type | Complexity | Status | Required mechanics | Runtime smoke-test steps |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Granite maul | `granite_maul`, `granite_maul_pretty`, `granite_maul_plus`, `granite_maul_pretty_plus`, `br_granite_maul` | Quick Smash | enum-driven | melee | medium | implemented (partial) | melee hit + spec anim/spot; instant queue nuance ontbreekt | Spawn/equip -> toggle special -> `Attack` NPC -> verify energy drain + hit + no exception |
| Dragon longsword | `dragon_longsword`, `bh_dragon_longsword_imbue`, `bh_dragon_longsword_corrupted` | Cleave | enum-driven | melee | low | implemented | melee damage/accuracy multiplier + cleave gfx | Spawn/equip -> toggle special -> `Attack` NPC -> verify boosted hit + energy |
| Dragon spear family | `dragon_spear`, `dragon_spear_p`, `dragon_spear_p+`, `dragon_spear_p++`, BH/corrupt variants | Shove | enum-driven | melee utility | high | implemented (partial) | boosted shove-hit + recovery delay; forced movement/stun fidelity nog pending | Spawn/equip -> toggle special -> attack NPC/player -> verify hit + energy + shorter recovery window |
| Zamorakian spear/hasta | `zamorak_spear`, `zamorak_hasta` | Shove | enum-driven | melee utility | high | implemented (partial) | gedeelde spear-shove met boosted hit; exacte forced movement/stun nog pending | Spawn/equip -> toggle special -> attack target -> verify hit + energy |
| Magic shortbow | `magic_shortbow`, `magic_shortbow_i` | Snapshot | enum-driven | ranged | medium | implemented | quiver ammo checks, double projectile/hit queue | Spawn/equip -> load arrows -> toggle special -> `Attack` NPC -> verify 2 hits + energy |
| Dragon crossbow | `xbows_crossbow_dragon`, `bh_xbows_crossbow_dragon_corrupted` | Annihilate | enum-driven | ranged | low | implemented | crossbow projectile + boosted ranged hit | Spawn/equip -> load bolts -> toggle special -> `Attack` NPC -> verify hit + energy |
| Armadyl crossbow | `acb`, `br_acb` | Armadyl Eye | enum-driven | ranged | medium | implemented (partial) | boosted ranged roll; exact armour-pierce formula deferred | Spawn/equip -> load bolts -> toggle special -> `Attack` NPC -> verify hit + energy |
| Rune thrownaxe | `rune_thrownaxe` | Chainhit | enum-driven | ranged utility | high | implemented (partial) | primary ranged hit + delayed chain follow-up op target; multi-target bounce nog pending | Spawn/equip -> toggle special -> attack NPC -> verify 2nd delayed chain hit + energy |
| Dark bow | `dark_bow` variants (existing mapping) | Descent of Darkness/Dragons | enum-driven | ranged | medium | implemented | double projectile + ammo use + dragon-arrow branch | Spawn/equip -> load arrows -> toggle special -> `Attack` NPC -> verify 2 projectiles/hits + energy |
| Barrelchest anchor | `brain_anchor`, `bh_brain_anchor_imbue` | Sunder | enum-driven | melee | low | implemented | boosted crush hit + anchor spec gfx | Spawn/equip -> toggle special -> `Attack` NPC -> verify boosted hit + energy |

## Weapon matrix
| Weapon | Special name | Energy cost | Existing? | MVP behavior | Required systems | Status |
| --- | --- | --- | --- | --- | --- | --- |
| Dragon longsword | Cleave | enum-driven | yes | single hit, 1.25x acc/dmg | damage multiplier, accuracy multiplier, animation/spotanim | implemented |
| Dark bow | Descent of Darkness/Dragons | enum-driven | yes | double projectile hit, ammo checks | multi-hit, projectile, animation/sound | implemented |
| Dragon dagger | Puncture | enum-driven | no | fast double-hit, high acc | multi-hit, accuracy multiplier, animation | implemented |
| Dragon scimitar | Sever | enum-driven | no | single hit, increased acc, disable protect prayer (partial) | accuracy multiplier, animation/spotanim, PvP-state hook | partial |
| Dragon mace | Shatter | enum-driven | no | single hit, acc/dmg boost | accuracy multiplier, damage multiplier, animation | implemented |
| Dragon battleaxe | Rampage | enum-driven | no | instant stat shift | instant special, stat drain/boost | implemented |
| Dragon warhammer | Smash | enum-driven | no | single hit, high acc, def drain on hit (partial) | accuracy multiplier, stat drain | partial |
| Granite maul | Quick Smash | enum-driven | no | heavy melee hit + spec gfx (instant queue nuance deferred) | instant/combat hybrid behavior, next-attack delay | partial |
| Abyssal dagger | Abyssal Puncture | enum-driven | no | high-acc stab hit | accuracy multiplier, animation/spotanim | implemented |
| Abyssal whip | Energy Drain | enum-driven | no | hit + run-disable (partial) | control effect, accuracy logic | partial |
| Dragon halberd | Sweep | enum-driven | no | primary hit, optional extra tile logic deferred | animation, multi-target/position logic | partial |
| Dragon claws | Slice and Dice | enum-driven | no | 4-hit chained roll (deferred exact formula) | multi-hit custom damage formula | blocked |
| Magic shortbow(i) | Snapshot | enum-driven | no | double-shot spec | ranged multi-hit projectile | implemented |
| Toxic blowpipe | Toxic Siphon | enum-driven | no | ranged hit + heal fraction | projectile, healing | missing |
| Armadyl godsword | The Judgement | enum-driven | no | strong accuracy/damage melee hit | damage multiplier, accuracy multiplier | implemented |
| Bandos godsword | Warstrike | enum-driven | no | hit + stat drain | stat drain | partial |
| Saradomin godsword | Healing Blade | enum-driven | no | hit + heal + prayer restore | healing | partial |
| Zamorak godsword | Ice Cleave | enum-driven | no | hit + freeze | freeze/bind | partial |
| Dragon crossbow | Annihilate | enum-driven | no | ranged hit with boosted damage | projectile, damage multiplier | implemented |
| Armadyl crossbow | Armadyl Eye | enum-driven | no | ranged hit with boosted accuracy/damage (defence-pierce nuance deferred) | projectile, custom defence roll | partial |
| Light/Heavy ballista | Power Shot | enum-driven | no | ranged heavy hit | projectile, damage multiplier | implemented |
| Elder maul | Pulverise | enum-driven | no | heavy hit + impact gfx | damage multiplier, animation/spotanim | implemented |
| Voidwaker | Disrupt | enum-driven | no | magic-typed hit met projectile/impact | custom hit type, projectile/spotanim | implemented (partial) |
| Ancient godsword | Blood Sacrifice | enum-driven | no | delayed effect | delayed effect queue, PvP tuning | implemented (partial) |
| Barrelchest anchor | Sunder | enum-driven | no | heavy crush hit + spec gfx | melee multiplier + animation/spotanim | implemented |
| Dragon spear / Zamorakian spear / Zamorak hasta | Shove / Impale | enum-driven | no | shove-hit met combat delay control | forced movement, stun immunity, PvP-safe handling | partial |
| Rune thrownaxe | Chainhit | enum-driven | no | primary ranged hit + delayed chain follow-up | target chaining, multi-target queueing | partial |

## Implementation groups

### A. Simple damage/accuracy special
- Kandidaten: dragon longsword (bestaand), dragon scimitar, dragon mace, abyssal dagger, elder maul.
- Bestaande systemen: melee damage/accuracy multipliers, queue hit, anim/spotanim hooks.
- Ontbreekt: sommige specifieke debuff side-effects.
- Veilig in branch: ja.

### B. Multi-hit special
- Kandidaten: dragon dagger, magic shortbow, dark bow (bestaand), dragon claws.
- Bestaande systemen: meerdere queued hits + projectile delays.
- Ontbreekt: claws-specifieke chained formula.
- Veilig in branch: deels.

### C. Ranged projectile special
- Kandidaten: magic shortbow, ballista, dragon crossbow, blowpipe.
- Bestaande systemen: projectile spawn, ammo managers, queue ranged hits.
- Ontbreekt: sommige unique defence-roll varianten.
- Veilig in branch: ja voor basis.

### D. Healing/leech special
- Kandidaten: saradomin godsword, toxic blowpipe.
- Bestaande systemen: damage roll beschikbaar tijdens special execution.
- Ontbreekt: gestandaardiseerde healing helper per spec.
- Veilig in branch: deels.

### E. Stat drain/debuff special
- Kandidaten: bandos godsword, dragon warhammer, dragon battleaxe.
- Bestaande systemen: stat mutatie helpers bestaan.
- Ontbreekt: consistente NPC/player stat-drain applicatie per spec.
- Veilig in branch: ja met beperkte scope.

### F. Movement/teleport/control special
- Kandidaten: abyssal whip run-disable, zamorak godsword freeze, dragon spear control.
- Bestaande systemen: beperkt zichtbaar in huidige specials.
- Ontbreekt: solide control/debuff subsystem voor alle targets.
- Veilig in branch: beperkt (partial/deferred).

### G. Complex/deferred
- Ancient godsword delayed effect, voidwaker magic-like behavior, claws exact damage chain, PvP-only nuances.
- Reden: meerdere ontbrekende of niet-afgegrensde subsystemen; te risicovol voor MVP-first batch.
- Dragon/Zamorak spear forced movement + stun, en rune thrownaxe chain-bounce zijn hier expliciet ondergebracht.

## Implementation order
1. Low-risk melee specs
2. Ranged projectile specs
3. Stat drain specs
4. Healing specs
5. Multi-hit specs
6. Complex area/PvP/teleport specs
7. Skipped/deferred

## Acceptance criteria
- `./gradlew test` en `./gradlew build` groen.
- Bestaande specials blijven werken.
- Elke nieuwe special heeft minimaal server-side behavior.
- Geen combat rewrite.
- Geen unrelated wijzigingen.

## Runtime smoke-test matrix
| Weapon | Spawn command | Target | Expected | Capture/log evidence | Result |
| --- | --- | --- | --- | --- | --- |
| Dragon dagger | `::item 1215` | NPC (single) | 2 hits, energy consumed | Runtime pass confirmed after login attack-option varp sync fix; NPC attack path restored (`opnpc2`) | passed |
| Dragon scimitar | `::item 4587` | NPC | boosted hit, energy consumed | Runtime pass confirmed after login attack-option varp sync fix; NPC attack path restored (`opnpc2`) | passed |
| Dragon mace | `::item 1434` | NPC | boosted hit, energy consumed | Runtime pass confirmed after login attack-option varp sync fix; NPC attack path restored (`opnpc2`) | passed |
| Abyssal dagger | `::item 13265` | NPC | boosted stab hit | Runtime pass confirmed after login attack-option varp sync fix; NPC attack path restored (`opnpc2`) | passed |
| Dragon battleaxe | `::item 1377` | self | instant stat changes + energy consumed | Runtime pass confirmed; instant stat-shift spec triggers and consumes energy | passed |
| Granite maul | `::item 4153` | NPC | boosted crush hit + spec gfx | Runtime pass: activation + energy + hit path confirmed | partial |
| Barrelchest anchor | `::item 10887` | NPC | boosted crush hit + spec gfx | Runtime pass: activation + energy + hit path confirmed | partial |
| Magic shortbow | `::item 861` | NPC | double ranged hit + energy consumed | Runtime pass: double-hit flow + energy confirmed | partial |
| Magic shortbow (i) | `::item 12788` | NPC | double ranged hit + energy consumed | Runtime pass: double-hit flow + energy confirmed | partial |
| Dragon crossbow | `::item 21902` | NPC | boosted ranged hit + energy consumed | Runtime pass: activation + ranged hit + energy confirmed | partial |
| Armadyl crossbow | `::item 11785` | NPC | boosted ranged hit + energy consumed | Runtime pass: activation + ranged hit + energy confirmed | partial |

## Runtime result summary
| Weapon | Runtime result | Evidence | Notes |
| --- | --- | --- | --- |
| Dragon dagger | passed | Special smoke-test passed after NPC attack option unblock; combat dispatch restored | Fast 2-hit spec validated |
| Dragon scimitar | passed | Special smoke-test passed after NPC attack option unblock; combat dispatch restored | Spec activation and energy drain validated |
| Dragon mace | passed | Special smoke-test passed after NPC attack option unblock; combat dispatch restored | Spec activation and boosted hit path validated |
| Abyssal dagger | passed | Special smoke-test passed after NPC attack option unblock; combat dispatch restored | Spec activation and stab-hit path validated |
| Dragon battleaxe | passed | Instant special validated in runtime pass | Stat shift + energy consume validated |

## Runtime blocker status
- Previous blocker `"NPC Attack option missing"` is **unblocked**.
- Root cause was fixed via login-side varp sync for attack option priorities:
  - `option_attackpriority` (`1107`)
  - `option_attackpriority_npc` (`1306`)
- This fix is included in this branch via cherry-pick:
  - `fix(login): sync attack option varps on login`

## Final wave implementation snapshot
- Added special handlers for:
  - Dragon spear family + Zamorak spear/hasta
  - Rune thrownaxe
  - Light/heavy ballista families
  - Voidwaker
  - Ancient godsword
- Added explicit energy fallback mappings for new families in `SpecialAttackWeapons` to avoid enum-gaps blocking startup on variant items.
- RSProx packet-level verification for these newly added complex specials is prepared but still requires a dedicated interactive capture run (normal vs special per family) before marking full-fidelity as complete.

## Batch 2 runtime result summary
| Weapon | Runtime result | Evidence | Notes |
| --- | --- | --- | --- |
| Granite maul | partial | Runtime smoke passed for special activation, energy drain, hit path | Gfx/animation not always matching expected OSRS fidelity |
| Barrelchest anchor | partial | Runtime smoke passed for special activation, energy drain, hit path | Gfx/animation not always matching expected OSRS fidelity |
| Magic shortbow | partial | Runtime smoke passed for double-shot flow and energy drain | Visual fidelity (gfx/anim timing) inconsistent in some attempts |
| Magic shortbow (i) | partial | Runtime smoke passed for double-shot flow and energy drain | Visual fidelity (gfx/anim timing) inconsistent in some attempts |
| Dragon crossbow | partial | Runtime smoke passed for activation, ranged hit path, energy drain | Visual fidelity (gfx/anim) not always exact |
| Armadyl crossbow | partial | Runtime smoke passed for activation, ranged hit path, energy drain | Visual fidelity (gfx/anim) not always exact |

## Batch 3 refactor structure
- Refactor uitgevoerd naar weapon-family files (gedrag ongewijzigd):
  - `DragonDaggerSpecialAttack.kt`
  - `DragonScimitarSpecialAttack.kt`
  - `DragonMaceSpecialAttack.kt`
  - `AbyssalDaggerSpecialAttack.kt`
  - `DragonBattleaxeSpecialAttack.kt`
  - `GraniteMaulSpecialAttack.kt`
  - `BarrelchestAnchorSpecialAttack.kt`
  - `MagicShortbowSpecialAttack.kt`
  - `DragonCrossbowSpecialAttack.kt`
  - `ArmadylCrossbowSpecialAttack.kt`
- Legacy batch-files verwijderd:
  - `LowRiskMeleeSpecialAttacks.kt`
  - `BatchTwoMeleeSpecialAttacks.kt`
  - `BatchTwoRangedSpecialAttacks.kt`
- Fidelity pass status:
  - Functionele flow blijft stabiel (toggle, energy, hit/effect).
  - Exacte OSRS anim/gfx timing blijft gedeeltelijk `partial` voor batch-2 wapens.
  - Geen extra complexe mechanics toegevoegd in batch 3.
