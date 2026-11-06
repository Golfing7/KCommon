# HYTALE.md

You must add Hytale support for KCommon.
This will be a large-scale refactor of KCommon, and compatibility with Minecraft must be preserved.
Use AGENTS.md for more understanding of what KCommon is.

## Hytale Source
The source code of the Hytale server is found under ~/IdeaProjects/hytale-shared-source/.
Use this as your ground truth for how Hytale works and what API is available.

## Strategy
Survey the plugin and determine which classes/packages are:
PURE - Can work with no dependency on Minecraft
MIXED - Some functionality can exist outside of Minecraft
MINECRAFT - Completely dedicated to support for Minecraft.
(augment this/change it if you detect that this classification is insufficient)

Then, partition the support based upon these classes into the correct subprojects.
If required, you may change/refactor existing classes, but keep breaking changes to a minimum.

You'll want to create a dual release of KCommon, the main build being compatible with Minecraft, and one with a 'hytale' classifier built specifically with hytale support.

## Phases
1. Perform all necessary research, making NO edits to the code. Create an extensive report of what must be done to accomplish this.
2. Incrementally implement the plan.

## YAML
YAML should be kept for Hytale, rather than using JSON as YAML supports comments and JSON does not.
In this case, you must shade in snake YAML as Hytale does not contain it.

## Special Handling
### Commands
The commands system should NOT be ported to Hytale, Hytale already has an existing command system built in.

### Menus
The menus system should NOT be ported to Hytale, Hytale already has an existing menu system.