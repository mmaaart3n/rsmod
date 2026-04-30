# Bank MVP

## Scope
- Keep existing bank interface and inventory transaction logic unchanged.
- Expand bank booth location coverage by broadening `content.bank_booth` mapping.
- Do not modify social/settings/admin tooling in this task.

## Root Cause
- `BankBooth` listens on `onOpLoc2(content.bank_booth)`.
- `content.bank_booth` mapping was too narrow and only covered a subset of booth loc types.
- Runtime evidence from Edgeville showed incoming `oploc2` on `bankbooth` followed by `"Nothing interesting happens."`, which means the click reached server but no matching content handler was bound.

## Implemented Fix
- Expanded `content.bank_booth` mapping in `content/generic/generic-locs/.../BankConfigs.kt`.
- Coverage is now based on known booth symbols from generated loc symbols, not city-by-city manual guessing.
- Kept bank logic unchanged: no UI/deposit/withdraw/persistence changes.

## Symbol Classification

### Included Booth Symbol Patterns
- contains `bankbooth`
- contains `bank_booth`
- includes `_multi`, `_open_0x`, `_end_left`, `_end_right`, `_ap1` variants when not excluded

### Excluded Patterns
- `closed`
- `broken`
- `private`
- `noop`
- `deadman`
- `deposit`
- `chest`
- `coffer`
- `safe`
- `till`
- `counter`
- `placeholder`

### Candidate Counts
- candidates found: `83`
- included: `49`
- excluded: `34`

### Included Loc Symbols
- `dwarf_keldagrim_bankbooth`
- `bankbooth_end_right`
- `bankbooth_end_left`
- `newbiebankbooth`
- `bankbooth`
- `bankbooth_multi`
- `elid_bankbooth`
- `fai_varrock_bankbooth`
- `fever_bankbooth`
- `corscurs_bankbooth_multi`
- `burgh_bank_booth_multiloc`
- `burgh_bankbooth_repaired`
- `burgh_bankbooth_damaged`
- `burgh_bankbooth_too_damaged`
- `burgh_bankbooth_too_damaged2`
- `pest_bankbooth`
- `ahoy_bankbooth`
- `lunar_moonclan_bankbooth`
- `aide_bankbooth`
- `contact_bank_booth`
- `dorgesh_bank_booth`
- `fai_falador_bankbooth`
- `canafis_bankbooth`
- `kr_bankbooth`
- `fai_falador_bankbooth_multi`
- `pest_bankbooth_multi`
- `contact_bank_booth_multi`
- `kr_bankbooth_multi`
- `ahoy_bankbooth_multi`
- `aide_bankbooth_multi`
- `piscarilius_bank_booth_01`
- `piscarilius_bank_booth_02`
- `piscarilius_bank_booth_03`
- `piscarilius_bank_booth_04`
- `archeeus_bank_booth_open_01`
- `archeeus_bank_booth_open_02`
- `archeeus_bank_booth_open_03`
- `archeeus_bank_booth_open_04`
- `lova_bank_booth_01`
- `lova_bank_booth_02`
- `lova_bank_booth_03`
- `lova_bank_booth_04`
- `tob_surface_bankbooth`
- `fai_varrock_bankbooth_multi`
- `prif_bankbooth_open`
- `tut2_bankbooth`
- `darkm_bankbooth`
- `gim_island_bankbooth`
- `bankbooth_ap1`

### Explicitly Excluded Candidates
- `fai_varrock_bank_booth_private` (`private`)
- `fai_varrock_bank_booth_private_end` (`private`)
- `fai_varrock_bank_booth_private_end_mirror` (`private`)
- `dwarf_keldagrim_bankboothclosed` (`closed`)
- `bankboothclosed_end_right` (`closed`)
- `bankboothclosed_end_left` (`closed`)
- `bankbooth_deadman` (`deadman`)
- `elid_bankboothclosed` (`closed`)
- `bankboothclosed` (`closed`)
- `fai_varrock_bankbooth_deadman` (`deadman`)
- `fai_varrock_bankbooth_closed` (`closed`)
- `pest_bankbooth_closed` (`closed`)
- `ahoy_bankboothclosed` (`closed`)
- `aide_bankbooth_closed` (`closed`)
- `contact_private_bank_booth` (`private`)
- `contact_private_bank_booth_broken` (`broken`)
- `contact_bank_booth_broken_01` (`broken`)
- `contact_bank_booth_broken_02` (`broken`)
- `contact_bank_booth_broken_03` (`broken`)
- `canafis_bank_booth_private` (`private`)
- `kr_bankbooth_closed` (`closed`)
- `kr_bank_booth_private_end_mirror` (`private`)
- `kr_bank_booth_private_end` (`private`)
- `fai_falador_bankbooth_deadman` (`deadman`)
- `pest_bankbooth_deadman` (`deadman`)
- `contact_bank_booth_deadman` (`deadman`)
- `kr_bankbooth_deadman` (`deadman`)
- `ahoy_bankbooth_deadman` (`deadman`)
- `aide_bankbooth_deadman` (`deadman`)
- `archeeus_bank_booth_closed` (`closed`)
- `lova_bank_booth_closed` (`closed`)
- `tob_surface_bankbooth_closed` (`closed`)
- `prif_bankbooth_closed` (`closed`)
- `fai_varrock_bankbooth_noop` (`noop`)

## Known Limitations
- This change targets booth-type locs only.
- Bank chests and deposit boxes are separate interaction groups and are not auto-included.
- Some niche booth variants may use a different op index; add only with capture proof.

## Suggested Smoke Test Matrix
- Lumbridge booth
- Edgeville booth
- Varrock booth
- Falador booth
- Draynor booth
- Al Kharid booth
- Grand Exchange booth (if booth-type in that area)

Expected evidence per location:
- Incoming `oploc2` for booth loc
- Outgoing `if_opensub` for `bankmain`
- Outgoing `if_opensub` for `bankside`
- No decode errors
