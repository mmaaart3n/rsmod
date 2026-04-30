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

## Weapon matrix
| Weapon | Special name | Energy cost | Existing? | MVP behavior | Required systems | Status |
| --- | --- | --- | --- | --- | --- | --- |
| Dragon longsword | Cleave | enum-driven | yes | single hit, 1.25x acc/dmg | damage multiplier, accuracy multiplier, animation/spotanim | implemented |
| Dark bow | Descent of Darkness/Dragons | enum-driven | yes | double projectile hit, ammo checks | multi-hit, projectile, animation/sound | implemented |
| Dragon dagger | Puncture | enum-driven | no | fast double-hit, high acc | multi-hit, accuracy multiplier, animation | missing |
| Dragon scimitar | Sever | enum-driven | no | single hit, increased acc, disable protect prayer (partial) | accuracy multiplier, animation/spotanim, PvP-state hook | partial |
| Dragon mace | Shatter | enum-driven | no | single hit, acc/dmg boost | accuracy multiplier, damage multiplier, animation | missing |
| Dragon battleaxe | Rampage | enum-driven | no | instant stat shift | instant special, stat drain/boost | missing |
| Dragon warhammer | Smash | enum-driven | no | single hit, high acc, def drain on hit (partial) | accuracy multiplier, stat drain | partial |
| Granite maul | Quick Smash | enum-driven | no | immediate heavy melee hit | instant/combat hybrid behavior, next-attack delay | missing |
| Abyssal dagger | Abyssal Puncture | enum-driven | no | high-acc stab hit | accuracy multiplier, animation/spotanim | missing |
| Abyssal whip | Energy Drain | enum-driven | no | hit + run-disable (partial) | control effect, accuracy logic | partial |
| Dragon halberd | Sweep | enum-driven | no | primary hit, optional extra tile logic deferred | animation, multi-target/position logic | partial |
| Dragon claws | Slice and Dice | enum-driven | no | 4-hit chained roll (deferred exact formula) | multi-hit custom damage formula | blocked |
| Magic shortbow(i) | Snapshot | enum-driven | no | double-shot spec | ranged multi-hit projectile | missing |
| Toxic blowpipe | Toxic Siphon | enum-driven | no | ranged hit + heal fraction | projectile, healing | missing |
| Armadyl godsword | The Judgement | enum-driven | no | strong accuracy/damage melee hit | damage multiplier, accuracy multiplier | missing |
| Bandos godsword | Warstrike | enum-driven | no | hit + stat drain | stat drain | missing |
| Saradomin godsword | Healing Blade | enum-driven | no | hit + heal + prayer restore | healing | missing |
| Zamorak godsword | Ice Cleave | enum-driven | no | hit + freeze | freeze/bind | missing |
| Dragon crossbow | Annihilate | enum-driven | no | ranged hit with boosted damage | projectile, damage multiplier | missing |
| Armadyl crossbow | Armadyl Eye | enum-driven | no | ranged hit with armor pierce style behavior | projectile, custom defence roll | blocked |
| Light/Heavy ballista | Power Shot | enum-driven | no | ranged heavy hit | projectile, damage multiplier | missing |
| Elder maul | Pulverise | enum-driven | no | heavy hit + impact gfx | damage multiplier, animation/spotanim | missing |
| Voidwaker | Disrupt | enum-driven | no | magic-typed guaranteed-like hit behavior | custom hit type, projectile/spotanim | blocked |
| Ancient godsword | Blood Sacrifice | enum-driven | no | delayed effect | delayed effect queue, PvP tuning | skipped for MVP |

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
| Dragon dagger | `::item 1215` | NPC (single) | 2 hits, energy consumed | Spawn confirmed in server log; no `opnpc2` evidence due to missing NPC attack option in runtime | blocked |
| Dragon scimitar | `::item 4587` | NPC | boosted hit, energy consumed | Spawn confirmed in server log; no `opnpc2` evidence due to missing NPC attack option in runtime | blocked |
| Dragon mace | `::item 1434` | NPC | boosted hit, energy consumed | Spawn confirmed in server log; no `opnpc2` evidence due to missing NPC attack option in runtime | blocked |
| Abyssal dagger | `::item 13265` | NPC | boosted stab hit | Spawn confirmed in server log; no `opnpc2` evidence due to missing NPC attack option in runtime | blocked |
| Dragon battleaxe | `::item 1377` | self | instant stat changes + energy consumed | Spawn confirmed in server log; activation check blocked until stable combat/special validation pass | blocked |

## Runtime result summary
| Weapon | Runtime result | Evidence | Notes |
| --- | --- | --- | --- |
| Dragon dagger | blocked | Server log shows item spawn; no NPC `Attack` option available, so no `opnpc2`/combat packet path | Needs attackable NPC target in current runtime world |
| Dragon scimitar | blocked | Server log shows item spawn; no NPC `Attack` option available, so no `opnpc2`/combat packet path | Same blocker as above |
| Dragon mace | blocked | Server log shows item spawn; no NPC `Attack` option available, so no `opnpc2`/combat packet path | Same blocker as above |
| Abyssal dagger | blocked | Server log shows item spawn; no NPC `Attack` option available, so no `opnpc2`/combat packet path | Same blocker as above |
| Dragon battleaxe | blocked | Item spawn confirmed; cannot complete confidence pass while combat-target path is blocked | Re-test when attack option is available |
