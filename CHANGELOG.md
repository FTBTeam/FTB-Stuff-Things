# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [21.1.15]

### Fixed
* Fixed Autohammer losing its current recipe on restore from NBT

## [21.1.14]

### Added
* Added `pt_br` translation (thanks @PrincessStelllar)

### Fixed
* Fixed bug introduced in 21.1.13 causing hammer recipes to sometimes have an empty resulting item

## [21.1.13]

### Fixed
* Reworked hammer & crook recipe handling somewhat
  * Hopefully fixes related occasional ConcurrentModificationException errors
  
## [21.1.12]

### Added
* Wooden basin now plays particles in addition to sound when block is jumped on successfully
* Added `ru_ru` translation (thanks @BazZziliuS)

### Changed
- Updated KubeJS support to KubeJS 7.2
  - **IMPORTANT**: this release will not work with earlier versions of KubeJS!

## [21.1.11]

### Added
* Added Wooden Basin block
  * Can be used to convert blocks into fluids by jumping or falling on them
  * Any (living) entity can be used for this purpose
* Added `hide_temperature_ingredients` config setting
* Added `ja_jp` translation (thanks @twister716)

### Fixed
* Fixed the SlowMelter and Super Cooler GUI's not showing the fluid in their tanks

## [21.1.10]

### Added
* Added block tag support for Dripper recipe block inputs
* Added `ftbstuff:water_strainer` item tag for all water strainers
* Water Strainers now support comparator output to indicate fullness

### Changed
* Mod archive file renamed for consistency. No functional changes.

### Fixed
* Dripper JEI recipe display now correctly shows output for fluid blocks (previously showed an empty slot)

## [21.1.9]

### Added
* More wooden Sluice types; there are now Sluices for every vanilla wood type
* Added zh_cn translation (thanks @nageih)

### Changed
* Cleaned up some block model JSONs, better block model re-use and more datagen (no player visible changes)

### Fixed
* Gracefully handle over-large itemstacks returned by attempting to insert into some modded storage blocks

## [21.1.8]

### Fixed
* Hotfix for last release: fixed cobblegen config getting lost

## [21.1.7]

### Added
* Added a Basalt Generator, similar to Cobblestone Generator
  * No recipe added for the block
* Added ru_ru translation (thanks ~BazZziliuS)

### Fixed
* Disabled wrench-rotation for Sluices, since they're a 2-part multiblock
  * Orphaned sluice funnels left by previous version can now be broken normally

## [21.1.6]

### Fixed
* Fixed loot table summary in JEI not drawing items in the last slot
* Auto-Hammers can now output to other Auto-Hammers; an inventory in between is no longer required (but still works)
* Reversed orientation when placing down Auto-Hammer blocks so input is to the left and output is to the right
* Drippers now autofill themselves from a water source block placed directly above
  * Note: only water works like this, not any other fluids

## [21.1.5]

### Fixed
* Fixed fluid usage modifiers not working for different tiers of sluice
* Sluice items now all have tooltips showing their speed/fluid/etc. modifiers and abilities
* Auto Hammers now correctly update their input/output locations when they're rotated by a wrench
* Auto Hammers client animation playing when machine is not active
* Auto Hammers not creating output buffer when no storage next to output

## [21.1.4]

### Added
* Initial public release

